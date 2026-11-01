/*
 * ArmorStandEditor: Bukkit plugin to allow editing armor stand attributes
 * Copyright (C) 2016  RypoFalem
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */
package io.github.rypofalem.armorstandeditor.modes;

import org.bukkit.entity.ArmorStand;
import org.bukkit.inventory.EquipmentSlot;

/** Manages only the two equipment locks used by ArmorStandEditor. */
public final class EquipmentLocks {
  private static final EquipmentSlot[] SLOTS = {
      EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS,
      EquipmentSlot.FEET, EquipmentSlot.HAND, EquipmentSlot.OFF_HAND
  };
  private static final ArmorStand.LockType[] LOCKS = {
      ArmorStand.LockType.ADDING_OR_CHANGING,
      ArmorStand.LockType.REMOVING_OR_CHANGING
  };

  private EquipmentLocks() {}

  public static boolean isFullyLocked(ArmorStand stand) {
    for (EquipmentSlot slot : SLOTS) {
      for (ArmorStand.LockType type : LOCKS) {
        if (!stand.hasEquipmentLock(slot, type)) return false;
      }
    }
    return true;
  }

  public static void setFullyLocked(ArmorStand stand, boolean locked) {
    for (EquipmentSlot slot : SLOTS) {
      for (ArmorStand.LockType type : LOCKS) {
        if (locked) stand.addEquipmentLock(slot, type);
        else stand.removeEquipmentLock(slot, type);
      }
    }
  }

  /** A copy of the exact per-slot state for both managed lock types. */
  public static boolean[] snapshot(ArmorStand stand) {
    boolean[] locks = new boolean[SLOTS.length * LOCKS.length];
    int index = 0;
    for (EquipmentSlot slot : SLOTS) {
      for (ArmorStand.LockType type : LOCKS) {
        locks[index++] = stand.hasEquipmentLock(slot, type);
      }
    }
    return locks;
  }

  /** Retains any unrelated ADDING locks that other plugins may have set. */
  public static void restore(ArmorStand stand, boolean[] locks) {
    if (locks.length != SLOTS.length * LOCKS.length) {
      throw new IllegalArgumentException("Invalid armor stand equipment lock snapshot");
    }
    int index = 0;
    for (EquipmentSlot slot : SLOTS) {
      for (ArmorStand.LockType type : LOCKS) {
        boolean locked = locks[index++];
        if (locked) stand.addEquipmentLock(slot, type);
        else stand.removeEquipmentLock(slot, type);
      }
    }
  }
}
