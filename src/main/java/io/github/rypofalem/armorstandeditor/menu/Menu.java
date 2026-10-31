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

package io.github.rypofalem.armorstandeditor.menu;

import io.github.rypofalem.armorstandeditor.ArmorStandEditorPlugin;
import io.github.rypofalem.armorstandeditor.PlayerEditor;
import java.util.ArrayList;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.*;

public class Menu {
  private Inventory menuInv;
  private PlayerEditor pe;
  private static String name = "Armor Stand Editor Menu";

  public Menu(PlayerEditor pe) {
    this.pe = pe;
    name = pe.plugin.getLang().getMessage("mainmenutitle", "menutitle");
    menuInv = Bukkit.createInventory(pe.getManager().getMenuHolder(), 54, name);
    fillInventory();
  }

  private void fillInventory() {
    menuInv.clear();

    ItemStack xAxis = null, yAxis = null, zAxis = null, coarseAdj = null, fineAdj = null, rotate = null, place = null, headPos = null,
      rightArmPos = null, bodyPos = null, leftArmPos = null, reset = null, showArms = null, visibility = null, size = null,
      rightLegPos = null, equipment = null, leftLegPos = null, lockEquipment = null, gravity = null, plate = null, scale = null, invulnerable = null, copy = null, paste = null,
      slot1 = null, slot2 = null, slot3 = null, slot4 = null, help = null;

    xAxis = createIcon(new ItemStack(Material.RED_WOOL, 1),
      "xaxis", "axis x");

    yAxis = createIcon(new ItemStack(Material.GREEN_WOOL, 1),
      "yaxis", "axis y");

    zAxis = createIcon(new ItemStack(Material.BLUE_WOOL, 1),
      "zaxis", "axis z");

    coarseAdj = createIcon(new ItemStack(Material.DIRT, 1),
      "coarseadj", "adj coarse");

    fineAdj = createIcon(new ItemStack(Material.SANDSTONE),
      "fineadj", "adj fine");

    reset = createIcon(new ItemStack(Material.LEVER),
      "reset", "mode reset");

    headPos = createIcon(new ItemStack(Material.LEATHER_HELMET),
      "head", "mode head");

    bodyPos = createIcon(new ItemStack(Material.LEATHER_CHESTPLATE),
      "body", "mode body");

    leftLegPos = createIcon(new ItemStack(Material.LEATHER_LEGGINGS),
      "leftleg", "mode leftleg");

    rightLegPos = createIcon(new ItemStack(Material.LEATHER_LEGGINGS),
      "rightleg", "mode rightleg");

    leftArmPos = createIcon(new ItemStack(Material.STICK),
      "leftarm", "mode leftarm");

    rightArmPos = createIcon(new ItemStack(Material.STICK),
      "rightarm", "mode rightarm");

    showArms = createIcon(new ItemStack(Material.STICK),
      "showarms", "mode showarms");

    if (pe.getPlayer().hasPermission("asedit.invisible")) {
      visibility = new ItemStack(Material.POTION, 1);
      PotionMeta potionMeta = (PotionMeta) visibility.getItemMeta();
      PotionEffect eff1 = new PotionEffect(PotionEffectType.INVISIBILITY, 1, 0);
      potionMeta.addCustomEffect(eff1, true);
      visibility.setItemMeta(potionMeta);
      visibility = createIcon(visibility, "invisible", "mode invisible");
    }

    size = createIcon(new ItemStack(Material.PUFFERFISH, 1),
      "size", "mode size");

    scale = createIcon(new ItemStack(Material.AMETHYST_SHARD),
      "scale", "mode scale");

    invulnerable = createIcon(new ItemStack(Material.TOTEM_OF_UNDYING),
      "invulnerable", "mode invulnerable");

    // Always show the property in the menu. CommandEx independently enforces
    // asedit.lockequipment when the icon is selected; hiding the icon made it
    // look as though the feature had never been added for basic-only editors.
    lockEquipment = createIcon(new ItemStack(Material.TRIAL_KEY),
      "lockequipment", "mode lockequipment");

    gravity = createIcon(new ItemStack(Material.SAND), "gravity", "mode gravity");

    plate = createIcon(new ItemStack(Material.STONE_SLAB, 1),
      "baseplate", "mode baseplate");

    place = createIcon(new ItemStack(Material.MINECART, 1),
      "placement", "mode placement");

    rotate = createIcon(new ItemStack(Material.COMPASS, 1),
      "rotate", "mode rotate");

    equipment = createIcon(new ItemStack(Material.CHEST, 1),
      "equipment", "mode equipment");

    copy = createIcon(new ItemStack(Material.WRITABLE_BOOK),
      "copy", "mode copy");

    paste = createIcon(new ItemStack(Material.ENCHANTED_BOOK),
      "paste", "mode paste");

    slot1 = createIcon(new ItemStack(Material.DANDELION),
      "copyslot", "slot 1", "1");

    slot2 = createIcon(new ItemStack(Material.AZURE_BLUET, 2),
      "copyslot", "slot 2", "2");

    slot3 = createIcon(new ItemStack(Material.BLUE_ORCHID, 3),
      "copyslot", "slot 3", "3");

    slot4 = createIcon(new ItemStack(Material.PEONY, 4),
      "copyslot", "slot 4", "4");

    help = createIcon(new ItemStack(Material.NETHER_STAR), "helpgui", "help");

    // Position tools (top), pose controls (left), and stand properties (right).
    // Keep property slots together for the Phase 5/6 additions; in particular,
    // Size and Scale are neighbors and remain separate editing modes.
    ItemStack[] items = new ItemStack[54];
    items[0] = xAxis;
    items[1] = yAxis;
    items[2] = zAxis;
    items[4] = coarseAdj;
    items[5] = fineAdj;
    items[7] = rotate;
    items[8] = place;

    items[10] = headPos;
    items[18] = rightArmPos;
    items[19] = bodyPos;
    items[20] = leftArmPos;
    items[21] = reset;
    items[27] = rightLegPos;
    items[28] = equipment;
    items[29] = leftLegPos;

    items[23] = showArms;
    items[24] = visibility;
    items[25] = size;
    items[26] = scale;
    items[32] = invulnerable;
    items[33] = lockEquipment;
    items[34] = gravity;
    items[35] = plate;
    // Slots 41-43 remain available for future property toggles.

    items[37] = copy;
    items[38] = paste;
    items[45] = slot1;
    items[46] = slot2;
    items[47] = slot3;
    items[48] = slot4;
    items[53] = help;
    menuInv.setContents(items);
  }

  private ItemStack createIcon(ItemStack icon, String path, String command) {
    return createIcon(icon, path, command, null);
  }

  private ItemStack createIcon(ItemStack icon, String path, String command, String option) {
    ItemMeta meta = icon.getItemMeta();
    meta.getPersistentDataContainer().set(ArmorStandEditorPlugin.instance().getIconKey(), PersistentDataType.STRING, "ase " + command);
    meta.displayName(text(getIconName(path, option)));
    ArrayList < Component > loreList = new ArrayList < > ();
    loreList.add(text(getIconDescription(path, option)));
    meta.lore(loreList);
    meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
    meta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
    icon.setItemMeta(meta);
    return icon;
  }

  private String getIconName(String path) {
    return getIconName(path, null);
  }

  private String getIconName(String path, String option) {
    return pe.plugin.getLang().getMessage(path, "iconname", option);
  }

  private String getIconDescription(String path) {
    return getIconDescription(path, null);
  }

  private String getIconDescription(String path, String option) {
    return pe.plugin.getLang().getMessage(path + ".description", "icondescription", option);
  }

  public void openMenu() {
    if (pe.getPlayer().hasPermission("asedit.basic")) {
      fillInventory();
      pe.getPlayer().openInventory(menuInv);
    }
  }

  public static String getName() {
    return name;
  }
}
