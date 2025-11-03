package net.oktawia.crazyae2addons.items;

import appeng.items.parts.PartItem;
import net.oktawia.crazyae2addons.parts.WormholeP2PTunnelPart;

public class WormholeP2PTunnelPartItem extends PartItem<WormholeP2PTunnelPart> {
    public WormholeP2PTunnelPartItem(Properties properties) {
        super(properties, WormholeP2PTunnelPart.class, WormholeP2PTunnelPart::new);
    }
}