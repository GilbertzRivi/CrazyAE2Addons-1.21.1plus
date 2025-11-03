package net.oktawia.crazyae2addons;

import net.neoforged.fml.ModList;

public class IsModLoaded {
    public static boolean isGTCEuLoaded() {
        return ModList.get().isLoaded("gtceu");
    }
    public static boolean isApothLoaded() {
        return ModList.get().isLoaded("apotheosis");
    }
    public static boolean isCCLoaded() { return ModList.get().isLoaded("computercraft"); }
    public static boolean isAppFluxLoaded() { return ModList.get().isLoaded("appflux"); }
}
