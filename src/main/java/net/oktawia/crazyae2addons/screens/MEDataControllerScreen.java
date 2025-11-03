package net.oktawia.crazyae2addons.screens;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.implementations.UpgradeableScreen;
import appeng.client.gui.style.ScreenStyle;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.oktawia.crazyae2addons.menus.MEDataControllerMenu;

public class MEDataControllerScreen<C extends MEDataControllerMenu> extends AEBaseScreen<C> {

    public MEDataControllerScreen(
            C menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);
    }

    @Override
    protected void updateBeforeRender(){
        super.updateBeforeRender();
        setTextContent("info1", Component.literal("Currently Stored variables:"));
        setTextContent("info2", Component.literal(String.format("%d/%d", getMenu().variableNum, getMenu().maxVariables)));
    }
}