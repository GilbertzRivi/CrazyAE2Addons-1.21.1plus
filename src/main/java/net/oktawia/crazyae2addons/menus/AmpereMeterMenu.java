package net.oktawia.crazyae2addons.menus;

import appeng.menu.AEBaseMenu;
import appeng.menu.guisync.GuiSync;
import appeng.util.BlockUpdate;
import net.minecraft.world.entity.player.Inventory;
import net.oktawia.crazyae2addons.defs.regs.CrazyMenuRegistrar;
import net.oktawia.crazyae2addons.entities.AmpereMeterBE;


public class AmpereMeterMenu extends AEBaseMenu {

    public AmpereMeterBE host;
    @GuiSync(48)
    public boolean direction;
    @GuiSync(49)
    public String transfer = "-";
    @GuiSync(51)
    public String unit = "-";

    public String CHANGE_DIRECTION = "actionChangeDirection";

    public AmpereMeterMenu(int id, Inventory ip, AmpereMeterBE host) {
        super(CrazyMenuRegistrar.AMPERE_METER_MENU.get(), id, ip, host);
        this.host = host;
        this.direction = host.direction;
        this.host.setMenu(this);
        registerClientAction(CHANGE_DIRECTION, Boolean.class, this::changeDirection);
    }

    public void changeDirection(boolean dir) {
        this.host.direction = dir;
        this.direction = dir;
        this.host.toggleDirection();
        if (isClientSide()){
            sendClientAction(CHANGE_DIRECTION, dir);
        } else if (host.getLevel() != null) {
            host.getLevel().sendBlockUpdated(host.getBlockPos(), host.getBlockState(), host.getBlockState(), 3);
        }
    }
}