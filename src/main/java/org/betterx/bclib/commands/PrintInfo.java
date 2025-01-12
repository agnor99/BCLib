package org.betterx.bclib.commands;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.client.gui.screens.UpdatesScreen;
import org.betterx.bclib.config.Configs;
import org.betterx.bclib.networking.VersionChecker;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.level.Level;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

public class PrintInfo {
    static int printDimensions(CommandContext<CommandSourceStack> ctx) {
        MutableComponent result = Component.literal("World Dimensions: ")
                                           .setStyle(Style.EMPTY.withBold(true).withColor(ChatFormatting.BLUE));

        for (var serverLevel : ctx.getSource().getLevel().getServer().getAllLevels()) {
            var generator = serverLevel.getChunkSource().getGenerator();
            String output = "\n - " + serverLevel.dimension().location().toString() + ": " +
                    "\n     " + generator.toString().trim() + " " +
                    generator
                            .getBiomeSource()
                            .toString()
                            .replace("\n", "\n     ");
            var cl = ChatFormatting.LIGHT_PURPLE;
            if (serverLevel.dimension().location().equals(Level.OVERWORLD.location()))
                cl = ChatFormatting.WHITE;
            else if (serverLevel.dimension().location().equals(Level.NETHER.location()))
                cl = ChatFormatting.RED;
            if (serverLevel.dimension().location().equals(Level.END.location()))
                cl = ChatFormatting.YELLOW;
            Component dimComponent = Component.literal(output)
                                              .setStyle(Style.EMPTY.withBold(false).withColor(cl));
            result.append(dimComponent);
        }
        ctx.getSource().sendSuccess(result, false);
        return Command.SINGLE_SUCCESS;
    }

    static int printUpdates(CommandContext<CommandSourceStack> ctx, boolean withUI) {
        boolean hasOne = false;
        MutableComponent header = Component.literal("Mod Updates:")
                                           .setStyle(Style.EMPTY.withBold(false)
                                                                .withColor(ChatFormatting.WHITE));
        ctx.getSource().sendSuccess(header, false);

        VersionChecker.forEachUpdate((mod, cur, updated) -> {
            ModContainer nfo = FabricLoader.getInstance().getModContainer(mod).orElse(null);
            MutableComponent result = Component.literal(" - ")
                                               .setStyle(Style.EMPTY.withBold(false)
                                                                    .withUnderlined(false)
                                                                    .withColor(ChatFormatting.WHITE));
            if (nfo != null)
                result.append(Component.literal(nfo.getMetadata().getName())
                                       .setStyle(Style.EMPTY.withBold(false).withColor(ChatFormatting.WHITE)));
            else
                result.append(Component.literal(mod)
                                       .setStyle(Style.EMPTY.withBold(false).withColor(ChatFormatting.WHITE)));
            result.append(Component.literal(": ")
                                   .setStyle(Style.EMPTY.withBold(false).withColor(ChatFormatting.WHITE)));
            result.append(Component.literal(cur).setStyle(Style.EMPTY.withBold(false).withColor(ChatFormatting.WHITE)));
            result.append(Component.literal(" -> ")
                                   .setStyle(Style.EMPTY.withBold(false).withColor(ChatFormatting.WHITE)));
            if (nfo != null && nfo.getMetadata().getContact().get("homepage").isPresent()) {
                var ce = new ClickEvent(
                        ClickEvent.Action.OPEN_URL,
                        nfo.getMetadata().getContact().get("homepage").get()
                );

                result.append(Component.literal(updated)
                                       .setStyle(Style.EMPTY.withClickEvent(ce)
                                                            .withBold(false)
                                                            .withItalic(true)
                                                            .withColor(ChatFormatting.GREEN)));
                result.append(Component.literal("  ")
                                       .setStyle(Style.EMPTY.withClickEvent(ce).withBold(true).withItalic(false)));

                result = result.append(Component.literal("[CurseForge]")
                                                .setStyle(Style.EMPTY.withClickEvent(ce)
                                                                     .withBold(true)
                                                                     .withColor(ChatFormatting.GREEN)
                                                                     .withUnderlined(true)));

            } else {
                result.append(Component.literal(updated)
                                       .setStyle(Style.EMPTY.withBold(false)
                                                            .withItalic(true)
                                                            .withColor(ChatFormatting.WHITE)));
                result.append(Component.literal("  ").setStyle(Style.EMPTY.withBold(true).withItalic(false)));

            }
            ctx.getSource().sendSuccess(result, false);
        });
        MutableComponent footer = Component.literal("\n")
                                           .setStyle(Style.EMPTY.withBold(false)
                                                                .withUnderlined(true)
                                                                .withColor(ChatFormatting.WHITE));
        ctx.getSource().sendSuccess(footer, false);

        if (withUI && BCLib.isClient() && Configs.CLIENT_CONFIG.showUpdateInfo() && !VersionChecker.isEmpty()) {
            if (!RenderSystem.isOnRenderThread()) {
                RenderSystem.recordRenderCall(() -> Minecraft.getInstance()
                                                             .setScreen(new UpdatesScreen(Minecraft.getInstance().screen)));
            } else {
                Minecraft.getInstance().setScreen(new UpdatesScreen(Minecraft.getInstance().screen));
            }
        }
        return Command.SINGLE_SUCCESS;
    }
}
