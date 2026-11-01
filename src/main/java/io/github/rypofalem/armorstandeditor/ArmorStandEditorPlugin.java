/*
 * ArmorStandEditor: Bukkit plugin to allow editing armor stand attributes
 * Copyright (C) 2016  RypoFalem
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package io.github.rypofalem.armorstandeditor;

import io.github.rypofalem.armorstandeditor.language.Language;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class ArmorStandEditorPlugin extends JavaPlugin {
  private NamespacedKey iconKey;
  private NamespacedKey movementKey;
  private final HighlightControl highlightControl = new HighlightControl();
  private static ArmorStandEditorPlugin instance;
  private Language lang;
  public PlayerEditorManager editorManager;
  public Material editTool = Material.FLINT;
  boolean requireToolData = false;
  boolean sendToActionBar = true;
  int editToolData = Integer.MIN_VALUE;
  boolean requireToolLore = false;
  String editToolLore = null;
  boolean debug = false; //weather or not to broadcast messages via print(String message)
  double coarseRot;
  double fineRot;

  public ArmorStandEditorPlugin() {
    instance = this;
  }

  @Override
  public void onEnable() {
    //saveResource doesn't accept File.seperator on windows, need to hardcode unix seperator "/" instead
    updateConfig("", "config.yml");
    updateConfig("lang/", "test_NA.yml");
    updateConfig("lang/", "nl_NL.yml");
    updateConfig("lang/", "uk_UA.yml");
    updateConfig("lang/", "zh.yml");
    updateConfig("lang/", "fr_FR.yml");
    updateConfig("lang/", "ro_RO.yml");
    updateConfig("lang/", "ja_JP.yml");
    updateConfig("lang/", "de_DE.yml");
    //English is the default language and needs to be unaltered to so that there is always a backup message string
    saveResource("lang/en_US.yml", true);
    lang = new Language(getConfig().getString("lang"), this);

    coarseRot = getConfig().getDouble("coarse");
    if (!ConfigValidation.isUsableRotationDivisor(coarseRot)) {
      getLogger().warning("Invalid 'coarse' rotation divisor; using default 12.");
      coarseRot = 12;
    }
    fineRot = getConfig().getDouble("fine");
    if (!ConfigValidation.isUsableRotationDivisor(fineRot)) {
      getLogger().warning("Invalid 'fine' rotation divisor; using default 120.");
      fineRot = 120;
    }
    String toolType = getConfig().getString("tool", "FLINT");
    Material configuredTool = toolType == null ? null : Material.getMaterial(toolType);
    if (configuredTool == null) {
      getLogger().warning("Invalid 'tool' material '" + toolType + "'; using default FLINT.");
      configuredTool = Material.FLINT;
    }
    editTool = configuredTool;
    requireToolData = getConfig().getBoolean("requireToolData", false);
    if (requireToolData) editToolData = getConfig().getInt("toolData", Integer.MIN_VALUE);
    requireToolLore = getConfig().getBoolean("requireToolLore", false);
    if (requireToolLore) editToolLore = getConfig().getString("toolLore", null);
    debug = getConfig().getBoolean("debug", true);
    sendToActionBar = getConfig().getBoolean("sendMessagesToActionBar", true);

    editorManager = new PlayerEditorManager(this);
    CommandEx commands = new CommandEx(this);
    // A single lifecycle callback also handles Paper command-tree rebuilds.
    getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event ->
        commands.register(event.registrar()));
    getServer().getPluginManager().registerEvents(editorManager, this);
    editorManager.restoreLoadedMovement();
  }

  @Override
  public void onDisable() {
    if (editorManager != null) editorManager.closeEditorMenus();
    highlightControl.clearTracking();
  }

  private void updateConfig(String folder, String config) {
    if (!new File(getDataFolder() + File.separator + folder + config).exists()) {
      saveResource(folder + config, false);
    }
  }

  public static ArmorStandEditorPlugin instance() {
    return instance;
  }

  public Language getLang() {
    return lang;
  }

  public boolean isEditTool(ItemStack item) {
    if (item == null) return false;
    if (editTool != item.getType()) return false;
    // The old item data byte was replaced by item metadata in modern Minecraft.
    // Damageable materials expose their wear through ItemMeta; other materials
    // (including the default FLINT) have a damage value of zero.
    ItemMeta meta = null;
    if (requireToolData || (requireToolLore && editToolLore != null)) {
      meta = item.getItemMeta();
    }
    if (requireToolData) {
      int damage = meta instanceof Damageable damageable ? damageable.getDamage() : 0;
      if (damage != editToolData) return false;
    }
    if (requireToolLore && editToolLore != null) {
      if (meta == null) return false;
      java.util.List<Component> lore = meta.lore();
      if (lore == null || lore.isEmpty()) return false;
      // Preserve the configured first-line comparison, including section codes.
      if (!LegacyComponentSerializer.legacySection().serialize(lore.get(0)).equals(editToolLore)) return false;
    }
    return true;
  }

  HighlightControl getHighlightControl() {
    return highlightControl;
  }

  public NamespacedKey getMovementKey() {
    if (movementKey == null) movementKey = new NamespacedKey(this, "no_movement");
    return movementKey;
  }

  public NamespacedKey getIconKey() {
    if (iconKey == null) iconKey = new NamespacedKey(this, "command_icon");
    return iconKey;
  }
}
