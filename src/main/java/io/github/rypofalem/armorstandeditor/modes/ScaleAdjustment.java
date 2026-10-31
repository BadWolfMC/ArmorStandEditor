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

package io.github.rypofalem.armorstandeditor.modes;

/** Scale is edited in whole tenths, so repeated steps cannot accumulate drift. */
public final class ScaleAdjustment {
  private static final int MIN_TENTHS = 1;
  private static final int MAX_TENTHS = 150;

  private ScaleAdjustment() {}

  public static double clamp(double base) {
    if (!Double.isFinite(base)) return 1.0;
    return Math.max(MIN_TENTHS, Math.min(MAX_TENTHS, Math.round(base * 10.0))) / 10.0;
  }

  public static double adjust(double base, int direction, AdjustmentMode mode) {
    int tenths = Double.isFinite(base) ? (int) Math.max(MIN_TENTHS,
        Math.min(MAX_TENTHS, Math.round(base * 10.0))) : 10;
    int delta = mode == AdjustmentMode.COARSE ? 5 : 1;
    return Math.max(MIN_TENTHS, Math.min(MAX_TENTHS, tenths + Integer.signum(direction) * delta)) / 10.0;
  }
}
