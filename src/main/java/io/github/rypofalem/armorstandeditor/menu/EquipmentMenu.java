/*
 * ArmorStandEditor: Bukkit plugin to allow editing armor stand attributes
 * Copyright (C) 2016  RypoFalem
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */
package io.github.rypofalem.armorstandeditor.menu;

import io.github.rypofalem.armorstandeditor.PlayerEditor;
import io.github.rypofalem.armorstandeditor.language.Language;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.UUID;


/**
 * Read-only preview of an armor stand's live equipment. The six lower slots
 * are never a container of transferable item copies: click handling cancels
 * vanilla inventory movement and explicitly swaps with the stand instead.
 * Gear stays on the stand until a deliberate, immediate swap is committed.
 */
public final class EquipmentMenu {
  private static final int[] ITEM_SLOTS = {9, 10, 11, 12, 13, 14};
  private final Inventory menuInv;
  private final PlayerEditor editor;
  private final ArmorStand stand;
  private final UUID owner;

  public EquipmentMenu(PlayerEditor editor, ArmorStand stand) {
    this.editor = editor;
    this.stand = stand;
    this.owner = editor.getUUID();
    String title = editor.plugin.getLang().getMessage("equiptitle", "menutitle");
    menuInv = Bukkit.createInventory(editor.getManager().getEquipmentHolder(), 18, Language.component(title));
    fillInventory();
  }

  private void fillInventory() {
    ItemStack disabled = new ItemStack(Material.BARRIER);
    ItemMeta meta = disabled.getItemMeta();
    meta.displayName(Language.component(editor.plugin.getLang().getMessage("disabled", "warn")));
    meta.getPersistentDataContainer().set(editor.plugin.getIconKey(), PersistentDataType.STRING, "ase icon");
    disabled.setItemMeta(meta);

    ItemStack[] contents = {
      createIcon(Material.LEATHER_HELMET, "helm"),
      createIcon(Material.LEATHER_CHESTPLATE, "chest"),
      createIcon(Material.LEATHER_LEGGINGS, "pants"),
      createIcon(Material.LEATHER_BOOTS, "boots"),
      createIcon(Material.WOODEN_SWORD, "rhand"),
      createIcon(Material.SHIELD, "lhand"),
      disabled, disabled, disabled,
      null, null, null, null, null, null,
      disabled, disabled, disabled
    };
    menuInv.setContents(contents);
    refreshPreview();
  }

  private ItemStack createIcon(Material material, String slot) {
    ItemStack icon = new ItemStack(material);
    ItemMeta meta = icon.getItemMeta();
    meta.getPersistentDataContainer().set(editor.plugin.getIconKey(), PersistentDataType.STRING, "ase icon");
    meta.displayName(Language.component(editor.plugin.getLang().getMessage("equipslot", "iconname", slot)));
    ArrayList<Component> lore = new ArrayList<>();
    lore.add(Language.component(editor.plugin.getLang().getMessage("equipslot.description", "icondescription", slot)));
    meta.lore(lore);
    meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
    meta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
    icon.setItemMeta(meta);
    return icon;
  }

  /** Snapshot into display-only slots; no equipment is removed from the stand. */
  public void refreshPreview() {
    if (!stand.isValid()) return;
    for (int slot : ITEM_SLOTS) menuInv.setItem(slot, cloneOrEmpty(getEquipment(slot)));
  }

  /**
   * Called only for an authorized, non-cancelled LEFT/RIGHT click on a lower
   * display slot. The manager already cancelled the vanilla inventory action.
   * The server's current equipment is read again to avoid stale previews when
   * multiple staff members have the same stand open.
   */
  public void swapWithCursor(int slot, ItemStack onCursor) {
    ItemStack previous = cloneOrEmpty(getEquipment(slot));
    ItemStack offered = cloneOrEmpty(onCursor);
    setEquipment(slot, offered);
    editor.getPlayer().setItemOnCursor(previous);
    refreshPreview();
  }

  private ItemStack getEquipment(int slot) {
    EntityEquipment equipment = stand.getEquipment();
    return switch (slot) {
      case 9 -> equipment.getHelmet();
      case 10 -> equipment.getChestplate();
      case 11 -> equipment.getLeggings();
      case 12 -> equipment.getBoots();
      case 13 -> equipment.getItemInMainHand();
      case 14 -> equipment.getItemInOffHand();
      default -> throw new IllegalArgumentException("Not an equipment display slot: " + slot);
    };
  }

  private void setEquipment(int slot, ItemStack item) {
    EntityEquipment equipment = stand.getEquipment();
    switch (slot) {
      case 9 -> equipment.setHelmet(item);
      case 10 -> equipment.setChestplate(item);
      case 11 -> equipment.setLeggings(item);
      case 12 -> equipment.setBoots(item);
      case 13 -> equipment.setItemInMainHand(item);
      case 14 -> equipment.setItemInOffHand(item);
      default -> throw new IllegalArgumentException("Not an equipment display slot: " + slot);
    }
  }

  private static ItemStack cloneOrEmpty(ItemStack item) {
    return item == null || item.getType().isAir() ? new ItemStack(Material.AIR) : item.clone();
  }

  public void open() {
    editor.getPlayer().openInventory(menuInv);
  }

  public Inventory getInventory() { return menuInv; }
  public ArmorStand getStand() { return stand; }
  public UUID getOwner() { return owner; }
  public static boolean isEquipmentSlot(int rawSlot) { return rawSlot >= 9 && rawSlot <= 14; }
}
