package net.oktawia.crazyae2addons.parts;

import java.util.*;

import appeng.api.features.P2PTunnelAttunement;
import appeng.api.ids.AEComponents;
import appeng.api.implementations.items.IMemoryCard;
import appeng.api.implementations.items.MemoryCardColors;
import appeng.api.implementations.items.MemoryCardMessages;
import appeng.items.parts.PartModels;
import appeng.items.tools.MemoryCardItem;
import appeng.util.InteractionUtil;
import appeng.util.SettingsFrom;
import com.mojang.logging.LogUtils;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import appeng.api.networking.GridFlags;
import appeng.api.networking.GridHelper;
import appeng.api.networking.IGridConnection;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.parts.IPartHost;
import appeng.api.parts.IPartItem;
import appeng.api.parts.IPartModel;
import appeng.api.stacks.AEKeyType;
import appeng.api.util.AECableType;
import appeng.hooks.ticking.TickHandler;
import appeng.parts.PartAdjacentApi;
import appeng.parts.p2p.P2PModels;
import appeng.parts.p2p.P2PTunnelPart;
import appeng.util.Platform;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;

import net.oktawia.crazyae2addons.CrazyAddons;
import net.oktawia.crazyae2addons.CrazyConfig;
import net.oktawia.crazyae2addons.misc.WormholeAnchor;
import org.jetbrains.annotations.Nullable;

public class WormholeP2PTunnelPart extends P2PTunnelPart<WormholeP2PTunnelPart> implements IGridTickable {

    private static final P2PModels MODELS = new P2PModels(ResourceLocation.fromNamespaceAndPath(CrazyAddons.MODID, "part/wormhole_p2p_tunnel"));

    private static final Set<BlockPos> wormholeUpdateBlacklist = new HashSet<>();
    private int redstonePower = 0;
    private boolean redstoneRecursive = false;

    private ConnectionUpdate pendingUpdate = ConnectionUpdate.NONE;
    private final Map<WormholeP2PTunnelPart, IGridConnection> connections = new IdentityHashMap<>();

    private final IManagedGridNode outerNode = CrazyConfig.COMMON.NestedP2PWormhole.get()
            ? GridHelper.createManagedNode(this, NodeListener.INSTANCE)
            .setTagName("outer").setInWorldNode(true).setFlags(GridFlags.DENSE_CAPACITY)
            : GridHelper.createManagedNode(this, NodeListener.INSTANCE)
            .setTagName("outer").setInWorldNode(true).setFlags(GridFlags.DENSE_CAPACITY, GridFlags.CANNOT_CARRY_COMPRESSED);

    private final PartAdjacentApi<IItemHandler> itemAdjacent =
            new PartAdjacentApi<>(this, Capabilities.ItemHandler.BLOCK, this::forwardCapabilityInvalidation);
    private final PartAdjacentApi<IFluidHandler> fluidAdjacent =
            new PartAdjacentApi<>(this, Capabilities.FluidHandler.BLOCK, this::forwardCapabilityInvalidation);
    private final PartAdjacentApi<IEnergyStorage> energyAdjacent =
            new PartAdjacentApi<>(this, Capabilities.EnergyStorage.BLOCK, this::forwardCapabilityInvalidation);

    private final CapabilityGuard<IItemHandler> itemGuard = new CapabilityGuard<>(itemAdjacent);
    private final CapabilityGuard<IFluidHandler> fluidGuard = new CapabilityGuard<>(fluidAdjacent);
    private final CapabilityGuard<IEnergyStorage> energyGuard = new CapabilityGuard<>(energyAdjacent);

    private final EmptyCapabilityGuard<IItemHandler> emptyItemGuard =
            new EmptyCapabilityGuard<>(new NullItemHandler());
    private final EmptyCapabilityGuard<IFluidHandler> emptyFluidGuard =
            new EmptyCapabilityGuard<>(new NullFluidHandler());
    private final EmptyCapabilityGuard<IEnergyStorage> emptyEnergyGuard =
            new EmptyCapabilityGuard<>(new NullEnergyStorage());

    private final IItemHandler inputItemHandler  = new InputItemHandler();
    private final IItemHandler outputItemHandler = new OutputItemHandler();
    private final IFluidHandler inputFluidHandler   = new InputFluidHandler();
    private final IFluidHandler outputFluidHandler  = new OutputFluidHandler();
    private final IEnergyStorage inputEnergyHandler  = new InputEnergyHandler();
    private final IEnergyStorage outputEnergyHandler = new OutputEnergyHandler();

    public WormholeP2PTunnelPart(IPartItem<?> partItem) {
        super(partItem);
        this.getMainNode()
                .setFlags(GridFlags.REQUIRE_CHANNEL,
                        CrazyConfig.COMMON.NestedP2PWormhole.get() ? GridFlags.DENSE_CAPACITY : GridFlags.COMPRESSED_CHANNEL)
                .setIdlePowerUsage(this.getPowerDrainPerTick())
                .addService(IGridTickable.class, this);
    }

    @PartModels
    public static List<IPartModel> getModels() {
        return MODELS.getModels();
    }

    @Override
    public IPartModel getStaticModels() {
        return MODELS.getModel(this.isPowered(), this.isActive());
    }

    /* ============================ Redstone ============================ */

    private void readRedstoneInput() {
        var level = getLevel();
        var targetPos = getBlockEntity().getBlockPos().relative(getSide());
        var state = level.getBlockState(targetPos);
        var block = state.getBlock();

        Direction inputSide = block instanceof RedStoneWireBlock ? Direction.UP : getSide();
        int newPower = level.getSignal(targetPos, inputSide);
        sendRedstoneToOutput(newPower);
    }

    private void sendRedstoneToOutput(int power) {
        int reducedPower = Math.max(0, power - 1);
        for (var output : getOutputs()) {
            output.receiveRedstoneInput(reducedPower);
        }
    }

    private void receiveRedstoneInput(int power) {
        if (redstoneRecursive) return;
        redstoneRecursive = true;

        if (isOutput() && getMainNode().isActive()) {
            if (this.redstonePower != power) {
                this.redstonePower = power;
                notifyRedstoneUpdate();
            }
        }

        redstoneRecursive = false;
    }

    @Override
    public boolean canConnectRedstone() {
        return true;
    }

    @Override
    public int isProvidingStrongPower() {
        return isOutput() ? redstonePower : 0;
    }

    @Override
    public int isProvidingWeakPower() {
        return isOutput() ? redstonePower : 0;
    }

    private void notifyRedstoneUpdate() {
        var world = getLevel();
        var pos = getBlockEntity().getBlockPos();
        if (world != null) {
            Platform.notifyBlocksOfNeighbors(world, pos);
            Platform.notifyBlocksOfNeighbors(world, pos.relative(getSide()));
        }
    }

    /* ======================== Interakcje / UI ======================== */

    @Override
    public void exportSettings(SettingsFrom mode, DataComponentMap.Builder builder) {
        if (mode != SettingsFrom.MEMORY_CARD) {
            super.exportSettings(mode, builder);
            return;
        }

        short freq = getFrequency();
        if (freq != 0) {
            builder.set(AEComponents.EXPORTED_P2P_FREQUENCY, freq);

            var colors = appeng.util.Platform.p2p().toColors(freq);
            builder.set(AEComponents.MEMORY_CARD_COLORS, new MemoryCardColors(
                    colors[0], colors[0], colors[1], colors[1],
                    colors[2], colors[2], colors[3], colors[3]));
        }
    }


    @Override
    public boolean onUseItemOn(net.minecraft.world.item.ItemStack heldItem, Player player, InteractionHand hand, Vec3 hitPos) {
        if (isClientSide() || hand == InteractionHand.OFF_HAND) return false;

        var attuned = P2PTunnelAttunement.getTunnelPartByTriggerItem(heldItem);
        if (!attuned.isEmpty()) {
            return true;
        }

        if (heldItem.getItem() instanceof IMemoryCard mc) {
            if (player.isShiftKeyDown()) {
                MemoryCardItem.clearCard(heldItem);
                var b = net.minecraft.core.component.DataComponentMap.builder();
                exportSettings(SettingsFrom.MEMORY_CARD, b);
                heldItem.applyComponents(b.build());
                mc.notifyUser(player, MemoryCardMessages.SETTINGS_SAVED);
                return true;
            } else {
                var p2pType = heldItem.get(AEComponents.EXPORTED_P2P_TYPE);
                boolean sameType = (p2pType instanceof IPartItem<?> pi) && (pi == getPartItem());
                boolean noTypeOnCard = (p2pType == null);
                if (sameType || noTypeOnCard) {
                    importSettings(SettingsFrom.MEMORY_CARD, heldItem.getComponents(), player);
                    mc.notifyUser(player, MemoryCardMessages.SETTINGS_LOADED);
                } else {
                    mc.notifyUser(player, MemoryCardMessages.INVALID_MACHINE);
                }
                return true;
            }
        }

        final boolean sneaking = player.isShiftKeyDown();
        if (sneaking && heldItem.getItem() instanceof BlockItem) {
            return false;
        }

        var level = getLevel();
        if (!(player instanceof ServerPlayer sp) || level == null) return false;

        BlockPos targetPos = null;
        Direction hitFace = null;

        if (isOutput()) {
            var input = getInput();
            if (input != null && input.getHost() != null) {
                var remoteHost = input.getHost().getBlockEntity();
                targetPos = remoteHost.getBlockPos().relative(input.getSide());
                hitFace = input.getSide().getOpposite();
            }
        } else {
            var outs = getOutputs();
            if (!outs.isEmpty()) {
                var out = outs.iterator().next();
                var remoteHost = out.getHost().getBlockEntity();
                targetPos = remoteHost.getBlockPos().relative(out.getSide());
                hitFace = out.getSide().getOpposite();
            }
        }

        if (targetPos == null) return false;

        var state = level.getBlockState(targetPos);
        var hit = new BlockHitResult(
                Vec3.atCenterOf(targetPos), hitFace, targetPos, false
        );

        WormholeAnchor.set(sp, targetPos);
        net.minecraft.world.InteractionResult result;
        try {
            result = state.useWithoutItem(level, player, hit);
        } catch (Throwable t) {
            WormholeAnchor.clear(player);
            return false;
        }

        if (!result.consumesAction()) {
            WormholeAnchor.clear(player);
            return false;
        }

        return true;
    }


    /* ============================ NBT ============================ */

    @Override
    public void readFromNBT(CompoundTag data, HolderLookup.Provider registries) {
        super.readFromNBT(data, registries);
        this.outerNode.loadFromNBT(data);
    }

    @Override
    public void writeToNBT(CompoundTag data, HolderLookup.Provider registries) {
        super.writeToNBT(data, registries);
        this.outerNode.saveToNBT(data);
    }
    /* ======================== Sieć / Tickowanie ======================== */

    @Override
    protected float getPowerDrainPerTick() {
        return 2.0f;
    }

    @Override
    public void onTunnelNetworkChange() {
        getBlockEntity().invalidateCapabilities();
        sendBlockUpdateToOppositeSide();
    }

    private void forwardCapabilityInvalidation() {
        if (isOutput()) {
            var in = getInput();
            if (in != null) in.getBlockEntity().invalidateCapabilities();
        } else {
            for (var out : getOutputs()) out.getBlockEntity().invalidateCapabilities();
        }
    }

    private void sendBlockUpdateToOppositeSide() {
        var world = getLevel();
        if (world == null || world.isClientSide) return;

        if (isOutput()) {
            var input = getInput();
            if (input != null && input.getHost() != null) {
                var be = input.getHost().getBlockEntity();
                var pos = be.getBlockPos().relative(input.getSide());
                sendNeighborUpdatesAt(pos, input.getSide());
            }
        } else {
            for (var out : getOutputs()) {
                if (out.getHost() != null) {
                    var be = out.getHost().getBlockEntity();
                    var pos = be.getBlockPos().relative(out.getSide());
                    sendNeighborUpdatesAt(pos, out.getSide());
                }
            }
        }
    }

    private void sendNeighborUpdatesAt(BlockPos pos, Direction facing) {
        var world = getLevel();
        if (world == null || world.isClientSide) return;
        if (wormholeUpdateBlacklist.contains(pos)) return;

        wormholeUpdateBlacklist.add(pos);
        TickHandler.instance().addCallable(world, wormholeUpdateBlacklist::clear);

        BlockState state = world.getBlockState(pos);
        world.sendBlockUpdated(pos, state, state, 3);
        world.updateNeighborsAt(pos, state.getBlock());

        var neighbor = pos.relative(facing.getOpposite());
        BlockState neighborState = world.getBlockState(neighbor);
        world.updateNeighborsAt(neighbor, neighborState.getBlock());
    }

    @Override
    public void onNeighborChanged(BlockGetter level, BlockPos pos, BlockPos neighbor) {
        super.onNeighborChanged(level, pos, neighbor);
        sendBlockUpdateToOppositeSide();
        if (!isOutput()) {
            readRedstoneInput();
        }
    }

    @Override
    public AECableType getExternalCableConnectionType() {
        return AECableType.DENSE_SMART;
    }

    @Override
    public void removeFromWorld() {
        super.removeFromWorld();
        this.outerNode.destroy();
    }

    @Override
    public void addToWorld() {
        super.addToWorld();
        this.outerNode.create(getLevel(), getBlockEntity().getBlockPos());
    }

    @Override
    public void setPartHostInfo(Direction side, IPartHost host, BlockEntity blockEntity) {
        super.setPartHostInfo(side, host, blockEntity);
        this.outerNode.setExposedOnSides(EnumSet.of(side));
    }

    @Override
    public IGridNode getExternalFacingNode() {
        return this.outerNode.getNode();
    }

    @Override
    public void onPlacement(Player player) {
        super.onPlacement(player);
        this.outerNode.setOwningPlayer(player);
    }

    @Override
    public TickingRequest getTickingRequest(IGridNode node) {
        return new TickingRequest(1, 1, true);
    }

    @Override
    public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
        pendingUpdate = node.isOnline() ? ConnectionUpdate.CONNECT : ConnectionUpdate.DISCONNECT;
        TickHandler.instance().addCallable(getLevel(), this::updateConnections);
        return TickRateModulation.SLEEP;
    }

    private void updateConnections() {
        var operation = pendingUpdate;
        pendingUpdate = ConnectionUpdate.NONE;

        var mainGrid = getMainNode().getGrid();

        if (isOutput() || mainGrid == null) {
            operation = ConnectionUpdate.DISCONNECT;
        }

        if (operation == ConnectionUpdate.DISCONNECT) {
            for (var cw : connections.values()) {
                cw.destroy();
            }
            connections.clear();
        } else if (operation == ConnectionUpdate.CONNECT) {
            var outputs = getOutputs();

            Iterator<Map.Entry<WormholeP2PTunnelPart, IGridConnection>> it = connections.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<WormholeP2PTunnelPart, IGridConnection> entry = it.next();
                WormholeP2PTunnelPart output = entry.getKey();
                var connection = entry.getValue();

                if (output.getMainNode().getGrid() != mainGrid
                        || !output.getMainNode().isOnline()
                        || !outputs.contains(output)) {
                    connection.destroy();
                    it.remove();
                }
            }

            for (var output : outputs) {
                if (!output.getMainNode().isOnline() || connections.containsKey(output)) {
                    continue;
                }

                var connection = GridHelper.createConnection(getExternalFacingNode(),
                        output.getExternalFacingNode());
                connections.put(output, connection);
            }
        }
    }

    /* ===================== Capability forwardery ===================== */

    public IItemHandler getExposedItemApi() {
        return isOutput() ? outputItemHandler : inputItemHandler;
    }
    public IFluidHandler getExposedFluidApi() {
        return isOutput() ? outputFluidHandler : inputFluidHandler;
    }
    public IEnergyStorage getExposedEnergyApi() {
        return isOutput() ? outputEnergyHandler : inputEnergyHandler;
    }

    private class CapabilityGuard<T> implements AutoCloseable {
        private final PartAdjacentApi<T> api;
        private int accessDepth = 0;

        private CapabilityGuard(PartAdjacentApi<T> api) { this.api = api; }

        CapabilityGuard<T> open() { accessDepth++; return this; }

        public T get(T emptyHandler) {
            if (accessDepth == 0) {
                throw new IllegalStateException("get() called after close()");
            } else if (accessDepth == 1) {
                if (isActive()) {
                    T found = api.find();
                    return found != null ? found : emptyHandler;
                }
                return emptyHandler;
            } else {
                return emptyHandler; // zapobiegamy rekurencji
            }
        }

        @Override
        public void close() {
            if (--accessDepth < 0) {
                throw new IllegalStateException("close() called multiple times");
            }
        }
    }

    private final class EmptyCapabilityGuard<T> extends CapabilityGuard<T> {
        private final T empty;
        private EmptyCapabilityGuard(T empty) { super(null); this.empty = empty; }
        @Override CapabilityGuard<T> open() { return this; }
        @Override public T get(T ignored) { return empty; }
        @Override public void close() { /* no-op */ }
    }

    // ===== Items =====
    private final class InputItemHandler implements IItemHandler {
        @Override
        public int getSlots() {
            int total = 0;
            for (WormholeP2PTunnelPart target : getOutputs()) {
                try (var g = target.itemGuard.open()) {
                    total += g.get(new NullItemHandler()).getSlots();
                }
            }
            return total;
        }

        @Override
        public @NotNull net.minecraft.world.item.ItemStack getStackInSlot(int slot) {
            var ref = resolveOutputItemSlot(slot);
            if (ref == null) return net.minecraft.world.item.ItemStack.EMPTY;
            return ref.handler.getStackInSlot(ref.localSlot);
        }

        @Override
        public @NotNull net.minecraft.world.item.ItemStack insertItem(int slot, @NotNull net.minecraft.world.item.ItemStack stack, boolean simulate) {
            // Jeśli slot wskazuje konkretny kontener – wstawiamy dokładnie tam
            var ref = resolveOutputItemSlot(slot);
            if (ref != null) {
                var before = stack;
                var rem = ref.handler.insertItem(ref.localSlot, stack, simulate);
                int moved = before.getCount() - rem.getCount();
                if (!simulate && moved > 0) {
                    deductTransportCost(moved, appeng.api.stacks.AEKeyType.items());
                }
                return rem;
            }

            // Fallback: rozdystrybuuj jak wcześniej (round-robin po outputach)
            int remainder = stack.getCount();
            final int outputTunnels = getOutputs().size();
            final int amount = stack.getCount();
            if (outputTunnels == 0 || amount == 0) return stack;

            final int amountPerOutput = amount / outputTunnels;
            int overflow = amountPerOutput == 0 ? amount : amount % amountPerOutput;

            for (WormholeP2PTunnelPart target : getOutputs()) {
                try (var guard = target.itemGuard.open()) {
                    final IItemHandler output = guard.get(new NullItemHandler());
                    final int toSend = amountPerOutput + overflow;
                    if (toSend <= 0) break;

                    var copy = stack.copy();
                    copy.setCount(toSend);
                    int sent = toSend - net.neoforged.neoforge.items.ItemHandlerHelper.insertItem(output, copy, simulate).getCount();

                    overflow = toSend - sent;
                    remainder -= sent;
                }
            }

            if (!simulate) {
                deductTransportCost(amount - remainder, appeng.api.stacks.AEKeyType.items());
            }

            if (remainder == stack.getCount()) return stack;
            if (remainder == 0) return net.minecraft.world.item.ItemStack.EMPTY;
            var copy = stack.copy(); copy.setCount(remainder); return copy;
        }

        @Override
        public @NotNull net.minecraft.world.item.ItemStack extractItem(int slot, int amount, boolean simulate) {
            var ref = resolveOutputItemSlot(slot);
            if (ref == null || amount <= 0) return net.minecraft.world.item.ItemStack.EMPTY;
            var res = ref.handler.extractItem(ref.localSlot, amount, simulate);
            if (!simulate && !res.isEmpty()) {
                deductTransportCost(res.getCount(), appeng.api.stacks.AEKeyType.items());
            }
            return res;
        }

        @Override
        public int getSlotLimit(int slot) {
            var ref = resolveOutputItemSlot(slot);
            if (ref == null) return 0;
            return ref.handler.getSlotLimit(ref.localSlot);
        }

        @Override
        public boolean isItemValid(int slot, @NotNull net.minecraft.world.item.ItemStack stack) {
            var ref = resolveOutputItemSlot(slot);
            if (ref == null) return false;
            return ref.handler.isItemValid(ref.localSlot, stack);
        }
    }

    private final class OutputItemHandler implements IItemHandler {
        @Override public int getSlots() {
            WormholeP2PTunnelPart in = getInput();
            if (in == null) return 0;
            try (var g = in.itemGuard.open()) { return g.get(new NullItemHandler()).getSlots(); }
        }
        @Override public @NotNull ItemStack getStackInSlot(int slot) {
            WormholeP2PTunnelPart in = getInput();
            if (in == null) return ItemStack.EMPTY;
            try (var g = in.itemGuard.open()) { return g.get(new NullItemHandler()).getStackInSlot(slot); }
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            WormholeP2PTunnelPart in = getInput();
            if (in == null || stack.isEmpty()) return stack;
            try (var g = in.itemGuard.open()) {
                IItemHandler inv = g.get(new NullItemHandler());
                ItemStack rem = ItemHandlerHelper.insertItem(inv, stack, simulate);
                int moved = stack.getCount() - rem.getCount();
                if (!simulate && moved > 0) deductTransportCost(moved, AEKeyType.items());
                return rem;
            }
        }

        @Override public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            WormholeP2PTunnelPart in = getInput();
            if (in == null) return ItemStack.EMPTY;
            try (var g = in.itemGuard.open()) {
                ItemStack res = g.get(new NullItemHandler()).extractItem(slot, amount, simulate);
                if (!simulate && !res.isEmpty()) deductTransportCost(res.getCount(), AEKeyType.items());
                return res;
            }
        }

        @Override public int getSlotLimit(int slot) {
            WormholeP2PTunnelPart in = getInput();
            if (in == null) return 0;
            try (var g = in.itemGuard.open()) { return g.get(new NullItemHandler()).getSlotLimit(slot); }
        }
        @Override public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            WormholeP2PTunnelPart in = getInput();
            if (in == null) return false;
            try (var g = in.itemGuard.open()) { return g.get(new NullItemHandler()).isItemValid(slot, stack); }
        }
    }


    // ===== Fluids =====
    private final class InputFluidHandler implements IFluidHandler {
        @Override public int getTanks() {
            int total = 0;
            for (WormholeP2PTunnelPart target : getOutputs()) {
                try (var g = target.fluidGuard.open()) {
                    total += g.get(new NullFluidHandler()).getTanks();
                }
            }
            return total;
        }

        @Override public @NotNull net.neoforged.neoforge.fluids.FluidStack getFluidInTank(int tank) {
            var ref = resolveOutputTank(tank);
            if (ref == null) return net.neoforged.neoforge.fluids.FluidStack.EMPTY;
            return ref.handler.getFluidInTank(ref.localTank);
        }

        @Override public int getTankCapacity(int tank) {
            var ref = resolveOutputTank(tank);
            if (ref == null) return 0;
            return ref.handler.getTankCapacity(ref.localTank);
        }

        @Override public boolean isFluidValid(int tank, @NotNull net.neoforged.neoforge.fluids.FluidStack stack) {
            var ref = resolveOutputTank(tank);
            if (ref == null) return false;
            return ref.handler.isFluidValid(ref.localTank, stack);
        }

        @Override
        public int fill(net.neoforged.neoforge.fluids.FluidStack resource, FluidAction action) {
            if (resource.isEmpty()) return 0;

            int outputs = getOutputs().size();
            if (outputs == 0) return 0;

            int amount = resource.getAmount();
            int per = amount / outputs;
            int overflow = per == 0 ? amount : amount % per;
            int acceptedTotal = 0;

            for (WormholeP2PTunnelPart target : getOutputs()) {
                try (var guard = target.fluidGuard.open()) {
                    var out = guard.get(new NullFluidHandler());
                    int toSend = per + overflow;
                    if (toSend <= 0) break;

                    var copy = resource.copy(); copy.setAmount(toSend);
                    int filled = out.fill(copy, action);
                    overflow = toSend - filled;
                    acceptedTotal += filled;
                }
            }

            if (action.execute() && acceptedTotal > 0) {
                deductTransportCost(acceptedTotal, appeng.api.stacks.AEKeyType.fluids());
            }
            return acceptedTotal;
        }

        @Override
        public @NotNull net.neoforged.neoforge.fluids.FluidStack drain(net.neoforged.neoforge.fluids.FluidStack resource, FluidAction action) {
            if (resource.isEmpty()) return net.neoforged.neoforge.fluids.FluidStack.EMPTY;
            for (WormholeP2PTunnelPart target : getOutputs()) {
                try (var guard = target.fluidGuard.open()) {
                    var res = guard.get(new NullFluidHandler()).drain(resource, action);
                    if (!res.isEmpty()) {
                        if (action.execute()) deductTransportCost(res.getAmount(), appeng.api.stacks.AEKeyType.fluids());
                        return res;
                    }
                }
            }
            return net.neoforged.neoforge.fluids.FluidStack.EMPTY;
        }

        @Override
        public @NotNull net.neoforged.neoforge.fluids.FluidStack drain(int maxDrain, FluidAction action) {
            if (maxDrain <= 0) return net.neoforged.neoforge.fluids.FluidStack.EMPTY;
            for (WormholeP2PTunnelPart target : getOutputs()) {
                try (var guard = target.fluidGuard.open()) {
                    var res = guard.get(new NullFluidHandler()).drain(maxDrain, action);
                    if (!res.isEmpty()) {
                        if (action.execute()) deductTransportCost(res.getAmount(), appeng.api.stacks.AEKeyType.fluids());
                        return res;
                    }
                }
            }
            return net.neoforged.neoforge.fluids.FluidStack.EMPTY;
        }
    }

    private final class OutputFluidHandler implements IFluidHandler {
        @Override public int getTanks() {
            WormholeP2PTunnelPart in = getInput();
            if (in == null) return 0;
            try (var g = in.fluidGuard.open()) { return g.get(new NullFluidHandler()).getTanks(); }
        }
        @Override public @NotNull FluidStack getFluidInTank(int tank) {
            WormholeP2PTunnelPart in = getInput();
            if (in == null) return FluidStack.EMPTY;
            try (var g = in.fluidGuard.open()) { return g.get(new NullFluidHandler()).getFluidInTank(tank); }
        }
        @Override public int getTankCapacity(int tank) {
            WormholeP2PTunnelPart in = getInput();
            if (in == null) return 0;
            try (var g = in.fluidGuard.open()) { return g.get(new NullFluidHandler()).getTankCapacity(tank); }
        }
        @Override public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
            WormholeP2PTunnelPart in = getInput();
            if (in == null) return false;
            try (var g = in.fluidGuard.open()) { return g.get(new NullFluidHandler()).isFluidValid(tank, stack); }
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            WormholeP2PTunnelPart in = getInput();
            if (in == null || resource.isEmpty()) return 0;
            try (var g = in.fluidGuard.open()) {
                int filled = g.get(new NullFluidHandler()).fill(resource, action);
                if (action.execute() && filled > 0) deductTransportCost(filled, AEKeyType.fluids());
                return filled;
            }
        }

        @Override public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
            WormholeP2PTunnelPart in = getInput();
            if (in == null) return FluidStack.EMPTY;
            try (var g = in.fluidGuard.open()) {
                FluidStack res = g.get(new NullFluidHandler()).drain(resource, action);
                if (action.execute() && !res.isEmpty()) deductTransportCost(res.getAmount(), AEKeyType.fluids());
                return res;
            }
        }

        @Override public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
            WormholeP2PTunnelPart in = getInput();
            if (in == null) return FluidStack.EMPTY;
            try (var g = in.fluidGuard.open()) {
                FluidStack res = g.get(new NullFluidHandler()).drain(maxDrain, action);
                if (action.execute() && !res.isEmpty()) deductTransportCost(res.getAmount(), AEKeyType.fluids());
                return res;
            }
        }
    }


    // ===== Energy (FE) =====
    private final class InputEnergyHandler implements IEnergyStorage {
        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            if (maxReceive <= 0) return 0;
            int outputs = getOutputs().size();
            if (outputs == 0) return 0;

            int per = maxReceive / outputs;
            int overflow = per == 0 ? maxReceive : maxReceive % per;
            int accepted = 0;

            for (WormholeP2PTunnelPart target : getOutputs()) {
                try (var guard = target.energyGuard.open()) {
                    var out = guard.get(new NullEnergyStorage());
                    int toSend = per + overflow;
                    if (toSend <= 0) break;
                    int got = out.receiveEnergy(toSend, simulate);
                    overflow = toSend - got;
                    accepted += got;
                }
            }

            if (!simulate && accepted > 0) deductEnergyCost(accepted, appeng.api.config.PowerUnit.FE);
            return accepted;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            if (maxExtract <= 0) return 0;
            int remaining = maxExtract;
            int total = 0;

            for (WormholeP2PTunnelPart target : getOutputs()) {
                if (remaining <= 0) break;
                try (var guard = target.energyGuard.open()) {
                    IEnergyStorage out = guard.get(new NullEnergyStorage());
                    int got = out.extractEnergy(remaining, simulate);
                    if (got > 0) {
                        total += got;
                        remaining -= got;
                    }
                }
            }

            if (!simulate && total > 0) deductEnergyCost(total, appeng.api.config.PowerUnit.FE);
            return total;
        }

        @Override public int getEnergyStored() { return 0; }
        @Override public int getMaxEnergyStored() { return Integer.MAX_VALUE; }
        @Override public boolean canExtract() { return true; }
        @Override public boolean canReceive() { return true; }
    }


    private final class OutputEnergyHandler implements IEnergyStorage {
        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            WormholeP2PTunnelPart in = getInput();
            if (in == null || maxReceive <= 0) return 0;
            try (var g = in.energyGuard.open()) {
                int got = g.get(new NullEnergyStorage()).receiveEnergy(maxReceive, simulate);
                if (!simulate && got > 0) deductEnergyCost(got, appeng.api.config.PowerUnit.FE);
                return got;
            }
        }

        @Override public boolean canReceive() { return true; }
        @Override public boolean canExtract() { return true; }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            WormholeP2PTunnelPart in = getInput();
            if (in == null) return 0;
            try (var g = in.energyGuard.open()) {
                int got = g.get(new NullEnergyStorage()).extractEnergy(maxExtract, simulate);
                if (!simulate && got > 0) deductEnergyCost(got, appeng.api.config.PowerUnit.FE);
                return got;
            }
        }

        @Override public int getEnergyStored() {
            WormholeP2PTunnelPart in = getInput();
            if (in == null) return 0;
            try (var g = in.energyGuard.open()) { return g.get(new NullEnergyStorage()).getEnergyStored(); }
        }
        @Override public int getMaxEnergyStored() {
            WormholeP2PTunnelPart in = getInput();
            if (in == null) return 0;
            try (var g = in.energyGuard.open()) { return g.get(new NullEnergyStorage()).getMaxEnergyStored(); }
        }
    }


    // ===== Null handlers =====
    private static final class NullItemHandler implements IItemHandler {
        @Override public int getSlots() { return 0; }
        @Override public @NotNull net.minecraft.world.item.ItemStack getStackInSlot(int slot) { return net.minecraft.world.item.ItemStack.EMPTY; }
        @Override public @NotNull net.minecraft.world.item.ItemStack insertItem(int slot, @NotNull net.minecraft.world.item.ItemStack stack, boolean simulate) { return stack; }
        @Override public @NotNull net.minecraft.world.item.ItemStack extractItem(int slot, int amount, boolean simulate) { return net.minecraft.world.item.ItemStack.EMPTY; }
        @Override public int getSlotLimit(int slot) { return 0; }
        @Override public boolean isItemValid(int slot, @NotNull net.minecraft.world.item.ItemStack stack) { return false; }
    }
    private static final class NullFluidHandler implements IFluidHandler {
        @Override public int getTanks() { return 0; }
        @Override public @NotNull FluidStack getFluidInTank(int tank) { return FluidStack.EMPTY; }
        @Override public int getTankCapacity(int tank) { return 0; }
        @Override public boolean isFluidValid(int tank, @NotNull FluidStack stack) { return false; }
        @Override public int fill(FluidStack resource, FluidAction action) { return 0; }
        @Override public @NotNull FluidStack drain(FluidStack resource, FluidAction action) { return FluidStack.EMPTY; }
        @Override public @NotNull FluidStack drain(int maxDrain, FluidAction action) { return FluidStack.EMPTY; }
    }
    private static final class NullEnergyStorage implements IEnergyStorage {
        @Override public int receiveEnergy(int maxReceive, boolean simulate) { return 0; }
        @Override public int extractEnergy(int maxExtract, boolean simulate) { return 0; }
        @Override public int getEnergyStored() { return 0; }
        @Override public int getMaxEnergyStored() { return 0; }
        @Override public boolean canExtract() { return false; }
        @Override public boolean canReceive() { return false; }
    }

    // ===== Pomocnicze =====
    private enum ConnectionUpdate { NONE, DISCONNECT, CONNECT }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.crazyae2addons.wormhole_p2p_tunnel");
    }

    private static final class ItemSlotRef {
        final IItemHandler handler;
        final int localSlot;
        ItemSlotRef(IItemHandler h, int s) { this.handler = h; this.localSlot = s; }
    }

    @Nullable
    private ItemSlotRef resolveOutputItemSlot(int globalSlot) {
        int base = 0;
        for (WormholeP2PTunnelPart target : getOutputs()) {
            try (var g = target.itemGuard.open()) {
                IItemHandler h = g.get(new NullItemHandler());
                int slots = h.getSlots();
                if (globalSlot < base + slots) {
                    return new ItemSlotRef(h, globalSlot - base);
                }
                base += slots;
            }
        }
        return null;
    }

    private static final class TankRef {
        final IFluidHandler handler;
        final int localTank;
        TankRef(IFluidHandler h, int t) { this.handler = h; this.localTank = t; }
    }
    @Nullable
    private TankRef resolveOutputTank(int globalTank) {
        int base = 0;
        for (WormholeP2PTunnelPart target : getOutputs()) {
            try (var g = target.fluidGuard.open()) {
                IFluidHandler h = g.get(new NullFluidHandler());
                int tanks = h.getTanks();
                if (globalTank < base + tanks) {
                    return new TankRef(h, globalTank - base);
                }
                base += tanks;
            }
        }
        return null;
    }
}
