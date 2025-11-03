package net.oktawia.crazyae2addons.defs;

import appeng.core.localization.LocalizationEnum;

public enum LangDefs implements LocalizationEnum {
    CREATIVE_TAB("creativetab.title", "Crazy AE2 Addons"),
    AUTO_BUILDER_0("translatable.crazyae2addons.auto_builder.pattern", "Pattern"),
    AUTO_BUILDER_1("translatable.crazyae2addons.auto_builder.missing", "Missing:");

    private final String key;
    private final String value;

    LangDefs(String key, String value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public String getTranslationKey() {
        return key;
    }

    @Override
    public String getEnglishText() {
        return value;
    }
}