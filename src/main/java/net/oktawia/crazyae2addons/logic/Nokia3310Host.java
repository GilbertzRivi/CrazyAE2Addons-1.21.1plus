package net.oktawia.crazyae2addons.logic;

import appeng.api.implementations.menuobjects.ItemMenuHost;
import appeng.menu.locator.ItemMenuHostLocator;
import com.mojang.logging.LogUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.LevelResource;
// --- USUNIĘTE IMPORTY ---
// import net.minecraft.core.component.DataComponents;
// import net.minecraft.world.item.component.CustomData;
// import java.util.function.Consumer;
// --- NOWE IMPORTY ---
import net.oktawia.crazyae2addons.defs.regs.CrazyDataComponents; // <-- Twój trwały komponent
import net.oktawia.crazyae2addons.items.Nokia3310;
import net.oktawia.crazyae2addons.misc.ProgramExpander;
import org.jetbrains.annotations.NotNull; // <-- NOWY IMPORT
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

public class Nokia3310Host extends ItemMenuHost<Nokia3310> {

    private boolean code;
    private int delay = 0;

    public Nokia3310Host(Nokia3310 item, Player player, ItemMenuHostLocator locator) {
        super(item, player, locator);

        CompoundTag tag = this.getProgramDataTag();
        if (tag.contains("code")){
            this.code = tag.getBoolean("code");
        }
        if (tag.contains("delay")){
            this.delay = tag.getInt("delay");
        }
    }

    public String getProgram() {
        return Nokia3310Host.loadProgramFromFile(this.getItemStack(), getPlayer().getServer());
    }

    public void setProgram(String program) {
        ProgramExpander.Result result = ProgramExpander.expand(program);
        this.code = result.success;

        CompoundTag tag = getProgramDataTag();

        tag.putBoolean("code", this.code);
        String programId = null;
        if (result.success){
            if (!tag.contains("program_id")){
                tag.putString("program_id", UUID.randomUUID().toString());
            }
            programId = tag.getString("program_id");
        }

        setProgramDataTag(tag);

        if (result.success){
            Nokia3310Host.saveProgramToFile(
                    programId,
                    program,
                    getPlayer().getServer()
            );
        }
    }

    public static String loadProgramFromFile(ItemStack stack, MinecraftServer server) {
        try {
            if (server == null || stack == null || stack.isEmpty()) return "";

            CompoundTag tag = stack.get(CrazyDataComponents.BUILDER_PROGRAM_DATA.get());
            if (tag == null) return "";

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


    @NotNull
    private CompoundTag getProgramDataTag() {
        CompoundTag tag = this.getItemStack().getOrDefault(CrazyDataComponents.BUILDER_PROGRAM_DATA.get(), new CompoundTag());
        return tag.copy();
    }

    private void setProgramDataTag(CompoundTag tag) {
        this.getItemStack().set(CrazyDataComponents.BUILDER_PROGRAM_DATA.get(), tag);
    }
}