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

import java.util.function.Predicate;

/** One permission policy for command selection, menu visibility and actual edits. */
public final class ModePermissions {
  private ModePermissions() {}

  public static String requiredPermission(EditMode mode) {
    return switch (mode) {
      case INVISIBLE -> "asedit.invisible";
      case LOCKEQUIPMENT -> "asedit.lockequipment";
      case MARKER -> "asedit.marker";
      case NOTICK -> "asedit.notick";
      default -> null;
    };
  }

  /** Requires asedit.basic to be checked separately at the editor entry point. */
  public static boolean permitted(EditMode mode, Predicate<String> hasPermission) {
    String permission = requiredPermission(mode);
    return permission == null || hasPermission.test(permission);
  }
}
