package net.oktawia.crazyae2addons.defs;

import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemRecipes {
    private static final Map<Item, List<Map.Entry<String, Map<String, Item>>>> ITEM_RECIPES = new HashMap<>();

    public static Map<Item, List<Map.Entry<String, Map<String, Item>>>> getItemRecipes() {
        return ITEM_RECIPES;
    }

    public static void item(Item item, String recipe, Map<String, Item> recipeMap) {
        ITEM_RECIPES.computeIfAbsent(item, k -> new ArrayList<>())
                .add(Map.entry(recipe, recipeMap));
    }

    public static void registerRecipes(){

    }
}
