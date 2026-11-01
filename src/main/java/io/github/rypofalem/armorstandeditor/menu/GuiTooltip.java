/*
 * ArmorStandEditor: Bukkit plugin to allow editing armor stand attributes
 * Copyright (C) 2016 RypoFalem
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License, version 2 or later.
 */
package io.github.rypofalem.armorstandeditor.menu;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.TooltipDisplay;
import org.bukkit.inventory.ItemStack;

/** Hide vanilla component detail lines without hiding the GUI item's name or lore.
 * Minecraft's hideTooltip flag also hides the name and lore and must not be used.
 */
final class GuiTooltip {
  private GuiTooltip() {}

  static void hideVanillaDetails(ItemStack icon) {
    icon.setData(DataComponentTypes.TOOLTIP_DISPLAY, TooltipDisplay.tooltipDisplay()
        .addHiddenComponents(
            DataComponentTypes.ATTRIBUTE_MODIFIERS,
            DataComponentTypes.ENCHANTMENTS,
            DataComponentTypes.STORED_ENCHANTMENTS,
            DataComponentTypes.POTION_CONTENTS,
            DataComponentTypes.TRIM,
            DataComponentTypes.BANNER_PATTERNS,
            DataComponentTypes.CONTAINER,
            DataComponentTypes.BUNDLE_CONTENTS,
            DataComponentTypes.FIREWORK_EXPLOSION,
            DataComponentTypes.FIREWORKS,
            DataComponentTypes.INSTRUMENT,
            DataComponentTypes.MAP_ID,
            DataComponentTypes.POT_DECORATIONS,
            DataComponentTypes.WRITTEN_BOOK_CONTENT)
        .build());
  }
}
