package net.oktawia.crazyae2addons.menus;

import appeng.api.inventories.InternalInventory;
import appeng.core.definitions.AEItems;
import appeng.menu.AEBaseMenu;
import appeng.menu.SlotSemantics;
import appeng.menu.guisync.GuiSync;
import appeng.menu.slot.AppEngSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.oktawia.crazyae2addons.defs.regs.CrazyMenuRegistrar;
import net.oktawia.crazyae2addons.entities.DataControllerBE;

public class MEDataControllerMenu extends AEBaseMenu {

    @GuiSync(874)
    public int variableNum = 0;
    @GuiSync(875)
    public int maxVariables = 0;

    public DataControllerBE host;

    public MEDataControllerMenu(int id, Inventory ip, DataControllerBE host) {
        super(CrazyMenuRegistrar.ME_DATA_CONTROLLER_MENU.get(), id, ip, host);
        for(int i = 0; i < 6; i = i + 1){
            this.addSlot(new AppEngSlot(host.inv, i), SlotSemantics.STORAGE);
        }
        this.host = host;
        variableNum = getVariableNum();
        maxVariables = getMaxVariables();
        createPlayerInventorySlots(ip);
    }

    public int getVariableNum() {
        return host.variables.size();
    }

    public int getMaxVariables() {
        int maxVariables = 0;
        InternalInventory cellInv = host.getInternalInventory();
        for (ItemStack stack : cellInv){
            if (stack.getItem() == AEItems.CELL_COMPONENT_1K.asItem()){
                maxVariables = maxVariables + 1;
            } else if (stack.getItem() == AEItems.CELL_COMPONENT_4K.asItem()) {
                maxVariables = maxVariables + 4;
            } else if (stack.getItem() == AEItems.CELL_COMPONENT_16K.asItem()) {
                maxVariables = maxVariables + 16;
            } else if (stack.getItem() == AEItems.CELL_COMPONENT_64K.asItem()) {
                maxVariables = maxVariables + 64;
            } else if (stack.getItem() == AEItems.CELL_COMPONENT_256K.asItem()) {
                maxVariables = maxVariables + 256;
            }
        }
        return maxVariables;
    }
}