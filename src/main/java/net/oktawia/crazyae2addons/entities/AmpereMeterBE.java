package net.oktawia.crazyae2addons.entities;

import appeng.blockentity.AEBaseBlockEntity;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuHostLocator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.oktawia.crazyae2addons.Utils;
import net.oktawia.crazyae2addons.defs.regs.CrazyBlockEntityRegistrar;
import net.oktawia.crazyae2addons.defs.regs.CrazyMenuRegistrar;
import net.oktawia.crazyae2addons.menus.AmpereMeterMenu;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

public class AmpereMeterBE extends AEBaseBlockEntity implements MenuProvider {

    public AmpereMeterMenu menu;
    public boolean direction = false;
    public String transfer = "-";
    public String unit = "-";
    public Integer numTransfer = 0;
    public HashMap<Integer, Integer> maxTrans = new HashMap<>();

    private int tickCounter = 0;
    private long feTransferredInTick = 0;
    private long feBufferForAvg = 0;

    public AmpereMeterBE(BlockPos pos, BlockState blockState) {
        super(CrazyBlockEntityRegistrar.AMPERE_METER_BE.get(), pos, blockState);
    }

    public AmpereMeterBE(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    public static <T extends BlockEntity> void serverTick(Level level, BlockPos pos, BlockState state, T obe) {
        if (!(obe instanceof AmpereMeterBE be)) return;
        be.tickCounter++;
        be.feBufferForAvg += be.feTransferredInTick;
        be.feTransferredInTick = 0;
        if (be.tickCounter >= 20) {
            int fePerTick = (int) (be.feBufferForAvg / be.tickCounter);

            be.unit = "FE/t";
            be.numTransfer = fePerTick;
            be.transfer = Utils.shortenNumber(fePerTick);

            if (be.getMenu() != null) {
                be.getMenu().unit = be.unit;
                be.getMenu().transfer = be.transfer;
                be.getMenu().broadcastChanges();
            }

            be.feBufferForAvg = 0;
            be.tickCounter = 0;
        }
    }

    @Nullable
    private IEnergyStorage getNeighborEnergy(Direction mySide) {
        Level lvl = getLevel();
        if (lvl == null) return null;

        BlockPos nPos = getBlockPos().relative(mySide);
        if (!lvl.isLoaded(nPos)) return null;

        BlockState nState = lvl.getBlockState(nPos);
        BlockEntity nBe = lvl.getBlockEntity(nPos);
        Direction neighborFace = mySide.getOpposite();

        IEnergyStorage cap = Capabilities.EnergyStorage.BLOCK.getCapability(lvl, nPos, nState, nBe, neighborFace);
        if (cap != null) return cap;

        return Capabilities.EnergyStorage.BLOCK.getCapability(lvl, nPos, nState, nBe, null);
    }

    public void toggleDirection() {
        setChanged();
        Level lvl = getLevel();
        if (lvl != null) {
            lvl.invalidateCapabilities(getBlockPos());
        }
    }

    public void setMenu(AmpereMeterMenu menu){
        this.menu = menu;
    }

    public AmpereMeterMenu getMenu(){
        return this.menu;
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int i, Inventory inventory, Player pPlayer) {
        return new AmpereMeterMenu(i, inventory, this);
    }

    @Override
    public void loadTag(CompoundTag data, HolderLookup.Provider registries) {
        super.loadTag(data, registries);
        if(data.contains("dir")){
            this.direction = data.getBoolean("dir");
        }
    }

    @Override
    public void saveAdditional(CompoundTag data, HolderLookup.Provider registries){
        super.saveAdditional(data, registries);
        data.putBoolean("dir", this.direction);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.crazyae2addons.ampere_meter");
    }

    public void openMenu(Player player, MenuHostLocator locator) {
        MenuOpener.open(CrazyMenuRegistrar.AMPERE_METER_MENU.get(), player, locator);
    }

    public final IEnergyStorage feLogicInput = new IEnergyStorage() {
        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            if (AmpereMeterBE.this.getLevel() == null) return 0;

            Direction outputSide = !AmpereMeterBE.this.direction
                    ? Utils.getRightDirection(getBlockState())
                    : Utils.getLeftDirection(getBlockState());

            IEnergyStorage out = AmpereMeterBE.this.getNeighborEnergy(outputSide);
            if (out == null || maxReceive <= 0) return 0;

            int accepted = out.receiveEnergy(maxReceive, simulate);
            if (!simulate && accepted > 0) {
                feTransferredInTick += accepted;
            }
            return accepted;
        }

        @Override public int getEnergyStored() {
            Direction outputSide = !AmpereMeterBE.this.direction
                    ? Utils.getRightDirection(getBlockState())
                    : Utils.getLeftDirection(getBlockState());
            IEnergyStorage out = AmpereMeterBE.this.getNeighborEnergy(outputSide);
            return out != null ? out.getEnergyStored() : 0;
        }

        @Override public int getMaxEnergyStored() {
            Direction outputSide = !AmpereMeterBE.this.direction
                    ? Utils.getRightDirection(getBlockState())
                    : Utils.getLeftDirection(getBlockState());
            IEnergyStorage out = AmpereMeterBE.this.getNeighborEnergy(outputSide);
            return out != null ? out.getMaxEnergyStored() : Integer.MAX_VALUE;
        }

        @Override public boolean canReceive() { return true; }
        @Override public int extractEnergy(int maxExtract, boolean simulate) { return 0; }
        @Override public boolean canExtract() { return false; }
    };


    public final IEnergyStorage feLogicOutput = new IEnergyStorage() {
        @Override public boolean canReceive() { return false; }
        @Override public int receiveEnergy(int maxReceive, boolean simulate) { return 0; }
        @Override public boolean canExtract() { return true; }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            if (AmpereMeterBE.this.getLevel() == null) return 0;

            Direction inputSide = AmpereMeterBE.this.direction
                    ? Utils.getRightDirection(getBlockState())
                    : Utils.getLeftDirection(getBlockState());

            IEnergyStorage in = AmpereMeterBE.this.getNeighborEnergy(inputSide);
            if (in == null || maxExtract <= 0) return 0;

            int pulled = in.extractEnergy(maxExtract, simulate);
            if (!simulate && pulled > 0) {
                feTransferredInTick += pulled;
            }
            return pulled;
        }

        @Override public int getEnergyStored() {
            Direction inputSide = AmpereMeterBE.this.direction
                    ? Utils.getRightDirection(getBlockState())
                    : Utils.getLeftDirection(getBlockState());
            IEnergyStorage in = AmpereMeterBE.this.getNeighborEnergy(inputSide);
            return in != null ? in.getEnergyStored() : 0;
        }

        @Override public int getMaxEnergyStored() {
            Direction inputSide = AmpereMeterBE.this.direction
                    ? Utils.getRightDirection(getBlockState())
                    : Utils.getLeftDirection(getBlockState());
            IEnergyStorage in = AmpereMeterBE.this.getNeighborEnergy(inputSide);
            return in != null ? in.getMaxEnergyStored() : 0;
        }
    };

}