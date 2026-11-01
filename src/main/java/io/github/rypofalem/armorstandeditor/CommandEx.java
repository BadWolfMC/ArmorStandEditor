/*
 * ArmorStandEditor: Bukkit plugin to allow editing armor stand attributes
 * Copyright (C) 2016  RypoFalem
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */
package io.github.rypofalem.armorstandeditor;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.github.rypofalem.armorstandeditor.modes.AdjustmentMode;
import io.github.rypofalem.armorstandeditor.modes.Axis;
import io.github.rypofalem.armorstandeditor.modes.EditMode;
import io.github.rypofalem.armorstandeditor.language.Language;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/** Native Paper Brigadier command. All literals and argument values are lowercase. */
public final class CommandEx {
    private final ArmorStandEditorPlugin plugin;

    public CommandEx(ArmorStandEditorPlugin plugin) {
        this.plugin = plugin;
    }

    /** One registration callback, rebuilt by Paper when the command lifecycle reruns. */
    public void register(Commands registrar) {
        registrar.register(Commands.literal("ase")
            .requires(source -> source.getSender() instanceof Player player
                && player.hasPermission("asedit.basic"))
            .executes(this::executeRoot)
            .then(Commands.literal("help").executes(this::executeHelp))
            .then(Commands.literal("?").executes(this::executeHelp))
            .then(Commands.literal("gui").executes(this::executeGui))
            .then(Commands.literal("mode")
                .executes(context -> missingValue(context, "nomodecom", modeUsage(player(context))))
                .then(Commands.argument("value", StringArgumentType.word())
                    .suggests(this::suggestModes)
                    .executes(this::executeMode)))
            .then(Commands.literal("axis")
                .executes(context -> missingValue(context, "noaxiscom", axisUsage()))
                .then(Commands.argument("value", StringArgumentType.word())
                    .suggests((context, builder) -> suggest(builder, CommandOptions.axes()))
                    .executes(this::executeAxis)))
            .then(Commands.literal("adj")
                .executes(context -> missingValue(context, "noadjcom", adjUsage()))
                .then(Commands.argument("value", StringArgumentType.word())
                    .suggests((context, builder) -> suggest(builder, CommandOptions.adjustments()))
                    .executes(this::executeAdj)))
            .then(Commands.literal("slot")
                .executes(context -> missingValue(context, "noslotnumcom", slotUsage()))
                .then(Commands.argument("value", StringArgumentType.word())
                    .suggests((context, builder) -> suggest(builder, CommandOptions.SLOTS))
                    .executes(this::executeSlot)))
            // Preserve friendly usage feedback on unknown first arguments without
            // turning known command names into case-insensitive word arguments.
            .then(Commands.argument("unknown", StringArgumentType.word())
                .executes(context -> {
                    Player player = authorizedPlayer(context);
                    if (player != null) showUsage(player, true);
                    return Command.SINGLE_SUCCESS;
                }))
            .build(), "Changes the function of the armor stand edit tool.");
    }

    private Player player(CommandContext<CommandSourceStack> context) {
        CommandSender sender = context.getSource().getSender();
        return sender instanceof Player p ? p : null;
    }

    private Player authorizedPlayer(CommandContext<CommandSourceStack> context) {
        CommandSender sender = context.getSource().getSender();
        if (!(sender instanceof Player player) || !player.hasPermission("asedit.basic")) {
            sender.sendMessage(Language.component(plugin.getLang().getMessage("noperm", "warn")));
            return null;
        }
        return player;
    }

    private CompletableFuture<Suggestions> suggestModes(CommandContext<CommandSourceStack> context,
                                                         SuggestionsBuilder builder) {
        Player player = player(context);
        if (player == null || !player.hasPermission("asedit.basic")) return builder.buildFuture();
        return suggest(builder, CommandOptions.modes(player::hasPermission));
    }

    private static CompletableFuture<Suggestions> suggest(SuggestionsBuilder builder, List<String> values) {
        for (String value : CommandOptions.matching(values, builder.getRemaining())) builder.suggest(value);
        return builder.buildFuture();
    }

    private static String value(CommandContext<CommandSourceStack> context) {
        return StringArgumentType.getString(context, "value");
    }

    private int executeRoot(CommandContext<CommandSourceStack> context) {
        Player player = authorizedPlayer(context);
        if (player != null) showUsage(player, false);
        return Command.SINGLE_SUCCESS;
    }

    private int executeHelp(CommandContext<CommandSourceStack> context) {
        Player player = authorizedPlayer(context);
        if (player != null) showHelp(player);
        return Command.SINGLE_SUCCESS;
    }

    private int executeGui(CommandContext<CommandSourceStack> context) {
        Player player = authorizedPlayer(context);
        if (player != null) plugin.editorManager.getPlayerEditor(player.getUniqueId()).chestMenu.openMenu();
        return Command.SINGLE_SUCCESS;
    }

    private int executeMode(CommandContext<CommandSourceStack> context) {
        Player player = authorizedPlayer(context);
        if (player == null) return Command.SINGLE_SUCCESS;
        EditMode mode = CommandOptions.matchingEnum(EditMode.class, value(context));
        if (mode == null) player.sendMessage(modeUsage(player));
        else if (!CommandOptions.permitted(mode, player::hasPermission))
            player.sendMessage(Language.component(plugin.getLang().getMessage("noperm", "warn")));
        else plugin.editorManager.getPlayerEditor(player.getUniqueId()).setMode(mode);
        return Command.SINGLE_SUCCESS;
    }

    private int executeAxis(CommandContext<CommandSourceStack> context) {
        Player player = authorizedPlayer(context);
        if (player == null) return Command.SINGLE_SUCCESS;
        Axis axis = CommandOptions.matchingEnum(Axis.class, value(context));
        if (axis == null) player.sendMessage(axisUsage());
        else plugin.editorManager.getPlayerEditor(player.getUniqueId()).setAxis(axis);
        return Command.SINGLE_SUCCESS;
    }

    private int executeAdj(CommandContext<CommandSourceStack> context) {
        Player player = authorizedPlayer(context);
        if (player == null) return Command.SINGLE_SUCCESS;
        AdjustmentMode adj = CommandOptions.matchingEnum(AdjustmentMode.class, value(context));
        if (adj == null) player.sendMessage(adjUsage());
        else plugin.editorManager.getPlayerEditor(player.getUniqueId()).setAdjMode(adj);
        return Command.SINGLE_SUCCESS;
    }

    private int executeSlot(CommandContext<CommandSourceStack> context) {
        Player player = authorizedPlayer(context);
        if (player == null) return Command.SINGLE_SUCCESS;
        try {
            int slot = Integer.parseInt(value(context));
            if (slot >= 1 && slot <= 9)
                plugin.editorManager.getPlayerEditor(player.getUniqueId()).setCopySlot((byte) (slot - 1));
            else player.sendMessage(slotUsage());
        } catch (NumberFormatException ex) {
            player.sendMessage(slotUsage());
        }
        return Command.SINGLE_SUCCESS;
    }

    private int missingValue(CommandContext<CommandSourceStack> context, String key, Component usage) {
        Player player = authorizedPlayer(context);
        if (player != null) {
            player.sendMessage(Language.component(plugin.getLang().getMessage(key, "warn")));
            player.sendMessage(usage);
        }
        return Command.SINGLE_SUCCESS;
    }

    private Component modeUsage(Player player) {
        List<String> modes = player != null ? CommandOptions.modes(player::hasPermission) : List.of();
        return Component.text("/ase mode <" + String.join(" | ", modes) + ">", NamedTextColor.GREEN);
    }

    private static Component axisUsage() { return Component.text("/ase axis <x|y|z>", NamedTextColor.GREEN); }
    private static Component adjUsage() { return Component.text("/ase adj <coarse|fine>", NamedTextColor.GREEN); }
    private static Component slotUsage() { return Component.text("/ase slot <1-9>", NamedTextColor.GREEN); }

    private void showUsage(Player player, boolean includeHelp) {
        player.sendMessage(modeUsage(player));
        player.sendMessage(axisUsage());
        player.sendMessage(slotUsage());
        player.sendMessage(adjUsage());
        if (includeHelp) player.sendMessage(Component.text("/ase help", NamedTextColor.GREEN));
        player.sendMessage(Component.text("/ase gui", NamedTextColor.GREEN));
    }

    private void showHelp(Player player) {
        player.closeInventory();
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
        player.sendMessage(Language.component(plugin.getLang().getMessage("help", "info", plugin.editTool.name())));
        player.sendMessage("");
        player.sendMessage(Language.component(plugin.getLang().getMessage("helptips", "info")));
        player.sendMessage("");
        player.sendMessage(Language.component(plugin.getLang().getMessage("helpurl", "")));
        player.sendMessage(Component.text("/ase gui", NamedTextColor.GREEN));
    }
}
