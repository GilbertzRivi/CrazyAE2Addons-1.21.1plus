package net.oktawia.crazyae2addons.datagen;

import appeng.core.definitions.AEBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.common.conditions.IConditionBuilder;
import net.oktawia.crazyae2addons.CrazyAddons;
import net.oktawia.crazyae2addons.defs.BlockRecipes;
import net.oktawia.crazyae2addons.defs.ItemRecipes;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class CrazyRecipeProvider extends RecipeProvider implements IConditionBuilder {
    public CrazyRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(RecipeOutput recipeOutput) {
        ItemRecipes.registerRecipes();
        BlockRecipes.registerRecipes();
        for (var entry : BlockRecipes.getBlockRecipes().entrySet()){
            ShapedRecipeBuilder builder = ShapedRecipeBuilder.shaped(RecipeCategory.MISC, entry.getKey());
            for (var recipe : entry.getValue().getKey().split("/")) {
                builder.pattern(recipe);
            }
            for (Map.Entry<String, Item> e : entry.getValue().getValue().entrySet()) {
                builder.define(e.getKey().charAt(0), e.getValue());
            }
            builder.unlockedBy(getHasName(AEBlocks.CONTROLLER.asItem()), has(AEBlocks.CONTROLLER.asItem()));
            builder.save(recipeOutput);
        }
        for (var entry : ItemRecipes.getItemRecipes().entrySet()) {
            int recipeIndex = 0;
            for (var recipeEntry : entry.getValue()) {
                ShapedRecipeBuilder builder = ShapedRecipeBuilder.shaped(RecipeCategory.MISC, entry.getKey());
                for (var recipe : recipeEntry.getKey().split("/")) {
                    builder.pattern(recipe);
                }
                for (Map.Entry<String, Item> e : recipeEntry.getValue().entrySet()) {
                    builder.define(e.getKey().charAt(0), e.getValue());
                }
                builder.unlockedBy(getHasName(AEBlocks.CONTROLLER.asItem()), has(AEBlocks.CONTROLLER.asItem()));

                ResourceLocation recipeId = ResourceLocation.fromNamespaceAndPath(
                        CrazyAddons.MODID,
                        BuiltInRegistries.ITEM.getKey(entry.getKey()).getPath() + (recipeIndex == 0 ? "" : "_alt" + recipeIndex)
                );

                builder.save(recipeOutput, recipeId);
                recipeIndex++;
            }
        }
    }
}
