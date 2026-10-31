/*
 * ArmorStandEditor: Bukkit plugin to allow editing armor stand attributes
 * Copyright (C) 2016 RypoFalem
 * Licensed under GPL-2.0-or-later; see LICENSE.md.
 */
package io.github.rypofalem.armorstandeditor.modes;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.ArmorStand;
import org.bukkit.persistence.PersistentDataType;

/** Persists only explicit ASE movement restrictions, leaving other stands alone. */
public final class MovementControl {
  private MovementControl() {}

  public static void setCanMove(ArmorStand stand, boolean canMove, NamespacedKey key) {
    stand.setCanMove(canMove);
    if (canMove) {
      stand.getPersistentDataContainer().remove(key);
    } else {
      stand.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 0);
    }
  }

  public static void restore(ArmorStand stand, NamespacedKey key) {
    Byte frozen = stand.getPersistentDataContainer().get(key, PersistentDataType.BYTE);
    if (frozen != null && frozen == 0) stand.setCanMove(false);
  }
}
