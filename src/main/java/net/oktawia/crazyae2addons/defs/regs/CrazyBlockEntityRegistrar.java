package net.oktawia.crazyae2addons.defs.regs;

import appeng.block.AEBaseEntityBlock;
import appeng.blockentity.AEBaseBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block; // <-- Potrzebny import
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder; // <-- Zmiana z RegistryObject
import net.neoforged.neoforge.registries.DeferredRegister;
import net.oktawia.crazyae2addons.CrazyAddons;
import net.oktawia.crazyae2addons.entities.BrokenPatternProviderBE;

import java.util.ArrayList;
import java.util.List;

public class CrazyBlockEntityRegistrar {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, CrazyAddons.MODID);

    private static final List<Runnable> BLOCK_ENTITY_SETUP = new ArrayList<>();

    private static <T extends AEBaseBlockEntity> DeferredHolder<BlockEntityType<?>, BlockEntityType<T>> reg(
            String id,
            DeferredHolder<Block, ? extends AEBaseEntityBlock<?>> block,
            BlockEntityType.BlockEntitySupplier<T> factory,
            Class<T> blockEntityClass
    ) {
        return BLOCK_ENTITIES.register(id, () -> {
            var blk = block.get();
            var type = BlockEntityType.Builder.of(factory, blk).build(null);

            BLOCK_ENTITY_SETUP.add(() -> blk.setBlockEntity(
                    (Class) blockEntityClass, (BlockEntityType) type, null, null
            ));

            return type;
        });
    }

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BrokenPatternProviderBE>> BROKEN_PATTERN_PROVIDER_BE =
            reg("broken_pattern_provider", CrazyBlockRegistrar.BROKEN_PATTERN_PROVIDER_BLOCK, BrokenPatternProviderBE::new, BrokenPatternProviderBE.class);

    public static void runBlockEntitySetup() {
        BLOCK_ENTITY_SETUP.forEach(Runnable::run);
        BLOCK_ENTITY_SETUP.clear();
    }

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}