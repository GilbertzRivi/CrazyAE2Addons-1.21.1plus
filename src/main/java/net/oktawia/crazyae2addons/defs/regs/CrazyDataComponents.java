package net.oktawia.crazyae2addons.defs.regs;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.oktawia.crazyae2addons.CrazyAddons;

public class CrazyDataComponents {

    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS =
            DeferredRegister.create(BuiltInRegistries.DATA_COMPONENT_TYPE, CrazyAddons.MODID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<CompoundTag>> BUILDER_PROGRAM_DATA =
            DATA_COMPONENTS.register("builder_program_data",
                    () -> DataComponentType.<CompoundTag>builder()
                            .persistent(CompoundTag.CODEC)
                            .networkSynchronized(ByteBufCodecs.COMPOUND_TAG)
                            .build()
            );

    public static void register(IEventBus eventBus) {
        DATA_COMPONENTS.register(eventBus);
    }
}