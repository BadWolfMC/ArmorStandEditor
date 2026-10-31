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

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.ArmorStand;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.EulerAngle;

public final class ArmorStandData {
  public final EulerAngle headPos;
  public final EulerAngle leftArmPos;
  public final EulerAngle rightArmPos;
  public final EulerAngle bodyPos;
  public final EulerAngle leftLegPos;
  public final EulerAngle rightLegPos;
  public final boolean visible;
  public final boolean size;
  public final Double scale;
  public final boolean invulnerable;
  public final boolean[] equipmentLocks;
  public final boolean basePlate;
  public final boolean gravity;
  public final boolean showArms;
  public final ItemStack head;
  public final ItemStack body;
  public final ItemStack legs;
  public final ItemStack feetsies;
  public final ItemStack rightHand;
  public final ItemStack leftHand;

  ArmorStandData(final ArmorStand as) {
    this.headPos = as.getHeadPose();
    this.leftArmPos = as.getLeftArmPose();
    this.rightArmPos = as.getRightArmPose();
    this.bodyPos = as.getBodyPose();
    this.leftLegPos = as.getLeftLegPose();
    this.rightLegPos = as.getRightLegPose();
    this.size = as.isSmall();
    AttributeInstance scaleAttribute = as.getAttribute(Attribute.SCALE);
    this.scale = scaleAttribute == null ? null : scaleAttribute.getBaseValue();
    this.invulnerable = as.isMarkedInvulnerable();
    this.equipmentLocks = EquipmentLocks.snapshot(as);
    this.basePlate = as.hasBasePlate();
    this.gravity = as.hasGravity();
    this.showArms = as.hasArms();
    this.visible = as.isVisible();
    this.head = copyItem(as.getEquipment().getHelmet());
    this.body = copyItem(as.getEquipment().getChestplate());
    this.legs = copyItem(as.getEquipment().getLeggings());
    this.feetsies = copyItem(as.getEquipment().getBoots());
    this.rightHand = copyItem(as.getEquipment().getItemInMainHand());
    this.leftHand = copyItem(as.getEquipment().getItemInOffHand());
  }

  private static ItemStack copyItem(ItemStack item) {
    return item == null ? new ItemStack(Material.AIR) : item.clone();
  }
}
