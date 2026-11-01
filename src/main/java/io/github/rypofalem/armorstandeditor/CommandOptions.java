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

import io.github.rypofalem.armorstandeditor.modes.AdjustmentMode;
import io.github.rypofalem.armorstandeditor.modes.Axis;
import io.github.rypofalem.armorstandeditor.modes.EditMode;
import io.github.rypofalem.armorstandeditor.modes.ModePermissions;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;

/** Lowercase-only command values and shared mode authorization. */
final class CommandOptions {
    static final List<String> SLOTS = List.of("1", "2", "3", "4", "5", "6", "7", "8", "9");

    private CommandOptions() {}

    static <T extends Enum<T>> T matchingEnum(Class<T> type, String value) {
        for (T entry : type.getEnumConstants()) {
            if (entry.toString().toLowerCase(Locale.ROOT).equals(value)) return entry;
        }
        return null;
    }

    static String permissionFor(EditMode mode) {
        return ModePermissions.requiredPermission(mode);
    }

    static boolean permitted(EditMode mode, Predicate<String> hasPermission) {
        return ModePermissions.permitted(mode, hasPermission);
    }

    static List<String> modes(Predicate<String> hasPermission) {
        return Arrays.stream(EditMode.values())
            .filter(mode -> permitted(mode, hasPermission))
            .map(mode -> mode.toString().toLowerCase(Locale.ROOT))
            .toList();
    }

    static List<String> axes() {
        return Arrays.stream(Axis.values()).map(axis -> axis.toString().toLowerCase(Locale.ROOT)).toList();
    }

    static List<String> adjustments() {
        return Arrays.stream(AdjustmentMode.values()).map(adj -> adj.toString().toLowerCase(Locale.ROOT)).toList();
    }

    static List<String> matching(List<String> values, String partial) {
        return values.stream().filter(value -> value.startsWith(partial)).toList();
    }
}
