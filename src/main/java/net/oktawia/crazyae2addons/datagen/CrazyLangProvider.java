package net.oktawia.crazyae2addons.datagen;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;
import net.oktawia.crazyae2addons.CrazyAddons;
import net.oktawia.crazyae2addons.Utils;
import net.oktawia.crazyae2addons.defs.LangDefs;
import net.oktawia.crazyae2addons.defs.regs.CrazyBlockRegistrar;
import net.oktawia.crazyae2addons.defs.regs.CrazyItemRegistrar;

public class CrazyLangProvider extends LanguageProvider {
    public CrazyLangProvider(PackOutput output, String locale) {
        super(output, CrazyAddons.MODID, locale);
    }

    @Override
    protected void addTranslations() {
        for (var item : CrazyItemRegistrar.getItems()){
            this.add(item.getDescriptionId(), Utils.toTitle(BuiltInRegistries.ITEM.getKey(item).getPath()));
        }
        for (var block : CrazyBlockRegistrar.getBlocks()){
            this.add(block.getDescriptionId(), Utils.toTitle(BuiltInRegistries.BLOCK.getKey(block).getPath()));
        }
        for (var entry : LangDefs.values()) {
            this.add(entry.getTranslationKey(), entry.getEnglishText());
        }
    }
}