package net.oktawia.crazyae2addons.items;

import appeng.block.AEBaseBlockItem;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.Block;

import java.util.List;

public class CrazyPatternProviderBlockItem extends AEBaseBlockItem {
    public CrazyPatternProviderBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void addCheckedInformation(ItemStack stack,
                                      Item.TooltipContext context,
                                      List<Component> tooltip,
                                      TooltipFlag flag) {

        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);

        int addedRows = 0;
        int filled = 0;

        if (customData != null) {
            CompoundTag tag = customData.copyTag();

            if (tag.contains("added")) {
                addedRows = tag.getInt("added");
            }

            if (tag.contains("dainv", Tag.TAG_LIST)) {
                ListTag invTag = tag.getList("dainv", Tag.TAG_COMPOUND);
                filled = invTag.size();
            }
        }

        if (addedRows < 0) addedRows = 0;
        int totalRows = 8 + addedRows;
        int totalSlots = totalRows * 9;

        if (filled > totalSlots) filled = totalSlots;
        int percent = totalSlots > 0 ? (int)Math.round(100.0 * filled / (double) totalSlots) : 0;

        tooltip.add(Component.literal("Capacity: " + totalSlots)
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("(" + percent + "%)")
                .withStyle(ChatFormatting.AQUA));
    }
}