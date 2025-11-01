package net.oktawia.crazyae2addons.entities;

import appeng.api.stacks.AEItemKey;
import appeng.blockentity.crafting.PatternProviderBlockEntity;
import appeng.helpers.patternprovider.PatternProviderLogic;
import appeng.menu.ISubMenu;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuHostLocator;
import appeng.util.inv.AppEngInternalInventory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.PacketDistributor;
import net.oktawia.crazyae2addons.defs.regs.CrazyBlockEntityRegistrar;
import net.oktawia.crazyae2addons.defs.regs.CrazyBlockRegistrar;
import net.oktawia.crazyae2addons.defs.regs.CrazyMenuRegistrar;
import net.oktawia.crazyae2addons.mixins.PatternProviderBlockEntityAccessor;
import net.oktawia.crazyae2addons.network.SyncBlockClientPacket;

import java.util.List;

public class CrazyPatternProviderBE extends PatternProviderBlockEntity {

    private int added = 0;
    private CompoundTag nbt;

    public CrazyPatternProviderBE(BlockPos pos, BlockState blockState) {
        this(pos, blockState, 9 * 8);
    }

    public CrazyPatternProviderBE(BlockPos pos, BlockState blockState, int patternSize) {
        super(CrazyBlockEntityRegistrar.CRAZY_PATTERN_PROVIDER_BE.get(), pos, blockState);
        this.getMainNode().setVisualRepresentation(CrazyBlockRegistrar.CRAZY_PATTERN_PROVIDER_BLOCK.get().asItem());
        ((PatternProviderBlockEntityAccessor) this).setLogic(new PatternProviderLogic(getMainNode(), this, patternSize));
    }

    public CrazyPatternProviderBE refreshLogic(int added) {
        if (getLevel() == null) return this;

        CompoundTag snap = new CompoundTag();
        var reg = getLevel().registryAccess();
        this.getLogic().writeToNBT(snap, reg);
        var oldInv = (AppEngInternalInventory) this.getLogic().getPatternInv();
        oldInv.writeToNBT(snap, "dainv", reg);

        BlockPos pos = this.getBlockPos();
        BlockState state = this.getBlockState();
        getLevel().removeBlockEntity(pos);
        getLevel().setBlockEntity(new CrazyPatternProviderBE(pos, state, 8 * 9 + 9 * added));

        var newBE = (CrazyPatternProviderBE) getLevel().getBlockEntity(pos);
        if (newBE == null) return this;

        newBE.added = added;

        newBE.getLogic().readFromNBT(snap, reg);

        var newInv = (AppEngInternalInventory) newBE.getLogic().getPatternInv();
        newInv.readFromNBT(snap, "dainv", reg);

        newBE.setChanged();
        if (!getLevel().isClientSide) {
            getLevel().sendBlockUpdated(pos, state, state, Block.UPDATE_ALL);
            PacketDistributor.sendToAllPlayers(new SyncBlockClientPacket(pos, added));
        }

        return newBE;
    }

    public int getAdded() {
        return added;
    }

    public void setAdded(int amt) {
        if (amt != this.added) {
            this.added = amt;
            this.refreshLogic(amt);
        }
    }

    @Override
    public void saveAdditional(CompoundTag data, HolderLookup.Provider reg) {
        super.saveAdditional(data, reg);
        data.putInt("added", added);
        getLogic().writeToNBT(data, reg);
        ((AppEngInternalInventory) getLogic().getPatternInv()).writeToNBT(data, "dainv", reg);
    }

    @Override
    public void loadTag(CompoundTag data, HolderLookup.Provider reg) {
        super.loadTag(data, reg);
        added = data.getInt("added");
        this.nbt = data;
    }

    @Override
    public void onReady() {
        super.onReady();
        int expected = 8 * 9 + 9 * added;
        if (this.getLogic().getPatternInv().size() != expected && getLevel() != null) {
            var be = refreshLogic(added);
            be.added = added;
            var reg = getLevel().registryAccess();
            be.getLogic().readFromNBT(this.nbt, reg);
            ((AppEngInternalInventory) (be.getLogic().getPatternInv())).readFromNBT(this.nbt, "dainv", reg);
        }
    }

    @Override
    public PatternProviderLogic createLogic() {
        return new PatternProviderLogic(this.getMainNode(), this, 9 * 8 + (this.getAdded() * 9));
    }

    @Override
    public void openMenu(Player player, MenuHostLocator locator) {
        MenuOpener.open(CrazyMenuRegistrar.CRAZY_PATTERN_PROVIDER_MENU.get(), player, locator);
    }

    @Override
    public void returnToMainMenu(Player player, ISubMenu subMenu) {
        MenuOpener.returnTo(CrazyMenuRegistrar.CRAZY_PATTERN_PROVIDER_MENU.get(), player, subMenu.getLocator());
    }

    @Override
    public AEItemKey getTerminalIcon() {
        return AEItemKey.of(CrazyBlockRegistrar.CRAZY_PATTERN_PROVIDER_BLOCK.get());
    }

    @Override
    public ItemStack getMainMenuIcon() {
        return CrazyBlockRegistrar.CRAZY_PATTERN_PROVIDER_BLOCK.get().asItem().getDefaultInstance();
    }

    @Override
    public void addAdditionalDrops(Level level, BlockPos pos, List<ItemStack> drops) {
    }
}