package net.oktawia.crazyae2addons.blocks;

import appeng.block.crafting.PatternProviderBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.network.PacketDistributor;
import net.oktawia.crazyae2addons.CrazyAddons;
import net.oktawia.crazyae2addons.CrazyConfig;
import net.oktawia.crazyae2addons.defs.regs.CrazyItemRegistrar;
import net.oktawia.crazyae2addons.entities.CrazyPatternProviderBE;
import net.oktawia.crazyae2addons.network.SyncBlockClientPacket;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CrazyPatternProviderBlock extends PatternProviderBlock {

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CrazyPatternProviderBE(pos, state);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack heldItem, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hit) {

        if (!level.isClientSide && heldItem.is(CrazyItemRegistrar.CRAZY_PATTERN_PROVIDER_UPGRADE.get())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof CrazyPatternProviderBE crazyProvider) {
                int maxAdd = CrazyConfig.COMMON.CrazyProviderMaxAddRows.get();
                int cur = crazyProvider.getAdded();

                if (cur >= maxAdd && maxAdd != -1) {
                    player.displayClientMessage(
                            Component.literal("Reached the max size of that pattern provider"),
                            true
                    );
                    return ItemInteractionResult.CONSUME;
                }

                int next = cur + 1;
                crazyProvider.setAdded(next);
                int added = crazyProvider.getAdded();

                CrazyPatternProviderBE newBe = crazyProvider.refreshLogic(added);
                newBe.setAdded(added);

                level.sendBlockUpdated(pos, state, state, 3);

                if (!player.getAbilities().instabuild) {
                    heldItem.shrink(1);
                }

                PacketDistributor.sendToAllPlayers(new SyncBlockClientPacket(pos, added));

                return ItemInteractionResult.SUCCESS;
            }
        }

        return super.useItemOn(heldItem, state, level, pos, player, hand, hit);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
                                               BlockHitResult hitResult) {

        if (!level.isClientSide) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof CrazyPatternProviderBE crazyProvider) {
                PacketDistributor.sendToAllPlayers(new SyncBlockClientPacket(pos, crazyProvider.getAdded()));
            }
        }

        return super.useWithoutItem(state, level, pos, player, hitResult);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state,
                            LivingEntity placer, ItemStack stack) {
        if (!level.isClientSide) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof CrazyPatternProviderBE myBe) {
                CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
                if (customData != null) {
                    CompoundTag tag = customData.copyTag();
                    if (tag.contains("added")) {
                        myBe.loadTag(tag, level.registryAccess());
                        PacketDistributor.sendToAllPlayers(new SyncBlockClientPacket(pos, tag.getInt("added")));
                    }
                }
            }
        }
    }

    private ItemStack createDropStackWithData(CrazyPatternProviderBE myBe) {
        ItemStack stack = new ItemStack(this);
        CompoundTag tag = new CompoundTag();
        tag.putInt("added", myBe.getAdded());

        if (myBe.getLevel() == null) {
            CrazyAddons.LOGGER.warn("Could not save Pattern Provider data, Level was null!");
            return stack;
        }

        myBe.getLogic().writeToNBT(tag, myBe.getLevel().registryAccess());
        appeng.util.inv.AppEngInternalInventory inv =
                (appeng.util.inv.AppEngInternalInventory) myBe.getLogic().getPatternInv();
        inv.writeToNBT(tag, "dainv", myBe.getLevel().registryAccess());
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        myBe.getLogic().getPatternInv().clear();
        return stack;
    }

    @Override
    public @NotNull List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        BlockEntity be = builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if (be instanceof CrazyPatternProviderBE myBe) {
            ItemStack stack = createDropStackWithData(myBe);
            return List.of(stack);
        }
        return super.getDrops(state, builder);
    }

    @Override
    public @NotNull BlockState playerWillDestroy(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull Player player) {
        if (!level.isClientSide && player.isCreative()) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof CrazyPatternProviderBE myBe) {
                ItemStack stack = createDropStackWithData(myBe);
                Block.popResource(level, pos, stack);
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }
}