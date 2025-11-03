package net.oktawia.crazyae2addons.logic;

import appeng.api.implementations.menuobjects.ItemMenuHost;
import appeng.menu.locator.ItemMenuHostLocator;
import com.mojang.logging.LogUtils;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.storage.LevelResource;
import net.oktawia.crazyae2addons.items.Nokia3310;
import net.oktawia.crazyae2addons.misc.ProgramExpander;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.function.Consumer;

public class GadgetHost extends ItemMenuHost<Nokia3310> {

    private boolean code;
    private int delay = 0;

    public GadgetHost(Nokia3310 item, Player player, ItemMenuHostLocator locator) {
        super(item, player, locator);

        CompoundTag tag = this.getCustomTag();
        if (tag != null) {
            if (tag.contains("code")){
                this.code = tag.getBoolean("code");
            }
            if (tag.contains("delay")){
                this.delay = tag.getInt("delay");
            }
        }
    }

    public String getProgram() {
        return BuilderPatternHost.loadProgramFromFile(this.getItemStack(), getPlayer().getServer());
    }

    public int getDelay() {
        return this.delay;
    }

    public void setProgram(String program) {
        ProgramExpander.Result result = ProgramExpander.expand(program);
        this.code = result.success;

        // 4. Użyj helpera do zapisu NBT do komponentu
        CustomData customData = this.getItemStack().getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = customData.copyTag();

        tag.putBoolean("code", this.code);
        String programId = null;
        if (result.success){
            if (!tag.contains("program_id")){
                tag.putString("program_id", UUID.randomUUID().toString());
            }
            programId = tag.getString("program_id");
        }

        this.getItemStack().set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

        if (result.success){
            // Używamy tej samej statycznej metody, co w Nokia3310.java
            BuilderPatternHost.saveProgramToFile(
                    programId,
                    program,
                    getPlayer().getServer()
            );
        }
    }

    public void setDelay(int delay) {
        this.delay = delay;
        // 5. Użyj helpera do zapisu NBT do komponentu
        updateCustomTag(tag -> tag.putInt("delay", delay));
    }

    public static String loadProgramFromFile(ItemStack stack, MinecraftServer server) {
        try {
            if (server == null || stack == null || stack.isEmpty()) return "";

            CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
            if (customData == null) return "";
            CompoundTag tag = customData.copyTag();

            if (!tag.getBoolean("code") || !tag.contains("program_id")) return "";
            String id = tag.getString("program_id");
            if (id.isEmpty()) return "";

            Path file = server.getWorldPath(new LevelResource("serverdata"))
                    .resolve("autobuilder")
                    .resolve(id);
            if (!Files.exists(file)) return "";
            return Files.readString(file, StandardCharsets.UTF_8);
        } catch (Exception ignored) {
            return "";
        }
    }

    public static void saveProgramToFile(String id, String code, MinecraftServer server) {
        Path file = server.getWorldPath(new LevelResource("serverdata"))
                .resolve("autobuilder")
                .resolve(id);

        try {
            Files.createDirectories(file.getParent());
            Files.writeString(file, code, StandardCharsets.UTF_8);
        } catch (IOException e) {
            LogUtils.getLogger().info(e.toString());
        }
    }


    @Nullable
    private CompoundTag getCustomTag() {
        CustomData customData = this.getItemStack().get(DataComponents.CUSTOM_DATA);
        return (customData != null) ? customData.copyTag() : null;
    }

    private void updateCustomTag(Consumer<CompoundTag> tagConsumer) {
        CustomData customData = this.getItemStack().getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = customData.copyTag();
        tagConsumer.accept(tag);
        this.getItemStack().set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }
}