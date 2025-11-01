package net.oktawia.crazyae2addons.defs;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class BlockRecipes {
    private static final Map<Block, Map.Entry<String, Map<String, Item>>> BLOCK_RECIPES = new HashMap<>();

    public static Map<Block, Map.Entry<String, Map<String, Item>>> getBlockRecipes() {
        return BLOCK_RECIPES;
    }

    public static void block(
            Block block,
            String recipe,
            Supplier<Map<String, Item>> recipe_map) {
        BLOCK_RECIPES.put(block, Map.entry(recipe, recipe_map.get()));
    }

    public static void registerRecipes() {
    }
}
