package net.oktawia.crazyae2addons.datagen;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.client.model.generators.ItemModelBuilder;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.oktawia.crazyae2addons.CrazyAddons;
import net.oktawia.crazyae2addons.defs.regs.CrazyItemRegistrar;

public class CrazyItemModelProvider extends ItemModelProvider {
    public CrazyItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, CrazyAddons.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        for (var item : CrazyItemRegistrar.getItems()){
            if (!CrazyItemRegistrar.getParts().contains(item)){
                simpleItem(item);
            }
        }
    }

    private ItemModelBuilder simpleItem(Item item){
        String path = BuiltInRegistries.ITEM.getKey(item).getPath();

        return withExistingParent(path,
                ResourceLocation.withDefaultNamespace("item/generated"))
                .texture("layer0",
                        ResourceLocation.fromNamespaceAndPath(CrazyAddons.MODID, "item/" + path));
    }
}