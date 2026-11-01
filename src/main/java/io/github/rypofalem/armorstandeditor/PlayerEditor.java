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
import io.github.rypofalem.armorstandeditor.menu.Menu;
import io.github.rypofalem.armorstandeditor.modes.AdjustmentMode;
import io.github.rypofalem.armorstandeditor.modes.ArmorStandData;
import io.github.rypofalem.armorstandeditor.modes.Axis;
import io.github.rypofalem.armorstandeditor.modes.CopySlots;
import io.github.rypofalem.armorstandeditor.modes.EditMode;
import io.github.rypofalem.armorstandeditor.modes.EquipmentLocks;
import io.github.rypofalem.armorstandeditor.modes.ScaleAdjustment;
import io.github.rypofalem.armorstandeditor.modes.MovementControl;
import io.github.rypofalem.armorstandeditor.modes.ModePermissions;

import java.util.ArrayList;
import java.util.UUID;

import net.kyori.adventure.text.Component;
import org.bukkit.GameMode;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.EulerAngle;

public class PlayerEditor {
  public ArmorStandEditorPlugin plugin;
  private UUID uuid;
  EditMode eMode;
  AdjustmentMode adjMode;
  CopySlots copySlots;
  Axis axis;
  double eulerAngleChange;
  double degreeAngleChange;
  double movChange;
  Menu chestMenu;
  ArmorStand target;
  ArrayList < ArmorStand > targetList = null;
  int targetIndex = 0;
  long lastCancelled = 0;
  private BukkitTask pendingMenuTask;

  public PlayerEditor(UUID uuid, ArmorStandEditorPlugin plugin) {
    this.uuid = uuid;
    this.plugin = plugin;
    eMode = EditMode.NONE;
    adjMode = AdjustmentMode.COARSE;
    axis = Axis.X;
    copySlots = new CopySlots();
    eulerAngleChange = getManager().coarseAdj;
    degreeAngleChange = eulerAngleChange / Math.PI * 180;
    movChange = getManager().coarseMov;
    chestMenu = new Menu(this);
  }

  public void setMode(EditMode editMode) {
    // Internal callers must not bypass the command's mode-specific permission check.
    if (!requireModePermission(editMode)) {
      clearMode();
      return;
    }
    this.eMode = editMode;
    sendMessage("setmode", editMode.toString().toLowerCase());
  }

  /** A rejected mode selection must not leave a previously active mode armed. */
  void clearMode() {
    this.eMode = EditMode.NONE;
  }

  private boolean requireModePermission(EditMode mode) {
    Player player = getPlayer();
    if (player == null || !player.hasPermission("asedit.basic")) return false;
    if (!ModePermissions.permitted(mode, player::hasPermission)) {
      sendMessage("noperm", "warn", null);
      return false;
    }
    return true;
  }

  // Permissions can be revoked while the menu is open or after selecting a mode.
  // Both direct flint edits and GUI target actions must revalidate on use.
  boolean requireCurrentModePermission() {
    return requireModePermission(eMode);
  }

  public void setAxis(Axis axis) {
    this.axis = axis;
    sendMessage("setaxis", axis.toString().toLowerCase());
  }

  public void setAdjMode(AdjustmentMode adjMode) {
    this.adjMode = adjMode;
    if (adjMode == AdjustmentMode.COARSE) {
      eulerAngleChange = getManager().coarseAdj;
      movChange = getManager().coarseMov;
    } else {
      eulerAngleChange = getManager().fineAdj;
      movChange = getManager().fineMov;
    }
    degreeAngleChange = eulerAngleChange / Math.PI * 180;
    sendMessage("setadj", adjMode.toString().toLowerCase());
  }

  public void setCopySlot(byte slot) {
    copySlots.changeSlots(slot);
    sendMessage("setslot", String.valueOf((slot + 1)));
  }

  public void editArmorStand(ArmorStand armorStand) {
    if (!requireCurrentModePermission()) return;
    armorStand = attemptTarget(armorStand);
    switch (eMode) {
    case LEFTARM:
      armorStand.setLeftArmPose(subEulerAngle(armorStand.getLeftArmPose()));
      break;
    case RIGHTARM:
      armorStand.setRightArmPose(subEulerAngle(armorStand.getRightArmPose()));
      break;
    case BODY:
      armorStand.setBodyPose(subEulerAngle(armorStand.getBodyPose()));
      break;
    case HEAD:
      armorStand.setHeadPose(subEulerAngle(armorStand.getHeadPose()));
      break;
    case LEFTLEG:
      armorStand.setLeftLegPose(subEulerAngle(armorStand.getLeftLegPose()));
      break;
    case RIGHTLEG:
      armorStand.setRightLegPose(subEulerAngle(armorStand.getRightLegPose()));
      break;
    case SHOWARMS:
      toggleArms(armorStand);
      break;
    case SIZE:
      toggleSize(armorStand);
      break;
    case SCALE:
      adjustScale(armorStand, 1);
      break;
    case INVULNERABLE:
      toggleInvulnerable(armorStand);
      break;
    case MARKER:
      toggleMarker(armorStand);
      break;
    case NOTICK:
      toggleNoTick(armorStand);
      break;
    case NOMOVEMENT:
      toggleNoMovement(armorStand);
      break;
    case INVISIBLE:
      toggleVisible(armorStand);
      break;
    case BASEPLATE:
      togglePlate(armorStand);
      break;
    case GRAVITY:
      toggleGravity(armorStand);
      break;
    case COPY:
      copy(armorStand);
      break;
    case PASTE:
      paste(armorStand);
      break;
    case PLACEMENT:
      move(armorStand);
      break;
    case ROTATE:
      rotate(armorStand);
      break;
    case LOCKEQUIPMENT:
      toggleLockEquipment(armorStand);
      break;
    case EQUIPMENT:
      openEquipment(armorStand);
      break;
    case RESET:
      resetPosition(armorStand);
      break;
    case NONE:
      sendMessage("nomode", null);
      break;
    }
  }

  private void resetPosition(ArmorStand armorStand) {
    armorStand.setHeadPose(new EulerAngle(0, 0, 0));
    armorStand.setBodyPose(new EulerAngle(0, 0, 0));
    armorStand.setLeftArmPose(new EulerAngle(0, 0, 0));
    armorStand.setRightArmPose(new EulerAngle(0, 0, 0));
    armorStand.setLeftLegPose(new EulerAngle(0, 0, 0));
    armorStand.setRightLegPose(new EulerAngle(0, 0, 0));
  }

  private void openEquipment(ArmorStand armorStand) {
    getManager().openEquipmentMenu(this, armorStand);
  }

  public void reverseEditArmorStand(ArmorStand armorStand) {
    if (!requireCurrentModePermission()) return;
    armorStand = attemptTarget(armorStand);
    switch (eMode) {
    case LEFTARM:
      armorStand.setLeftArmPose(addEulerAngle(armorStand.getLeftArmPose()));
      break;
    case RIGHTARM:
      armorStand.setRightArmPose(addEulerAngle(armorStand.getRightArmPose()));
      break;
    case BODY:
      armorStand.setBodyPose(addEulerAngle(armorStand.getBodyPose()));
      break;
    case HEAD:
      armorStand.setHeadPose(addEulerAngle(armorStand.getHeadPose()));
      break;
    case LEFTLEG:
      armorStand.setLeftLegPose(addEulerAngle(armorStand.getLeftLegPose()));
      break;
    case RIGHTLEG:
      armorStand.setRightLegPose(addEulerAngle(armorStand.getRightLegPose()));
      break;
    case SCALE:
      adjustScale(armorStand, -1);
      break;
    case PLACEMENT:
      reverseMove(armorStand);
      break;
    case ROTATE:
      reverseRotate(armorStand);
      break;
    default:
      editArmorStand(armorStand);
    }
  }

  private void move(ArmorStand armorStand) {
    Location loc = armorStand.getLocation();
    switch (axis) {
    case X:
      loc.add(movChange, 0, 0);
      break;
    case Y:
      loc.add(0, movChange, 0);
      break;
    case Z:
      loc.add(0, 0, movChange);
      break;
    }
    armorStand.teleport(loc);
  }

  private void reverseMove(ArmorStand armorStand) {
    Location loc = armorStand.getLocation();
    switch (axis) {
    case X:
      loc.subtract(movChange, 0, 0);
      break;
    case Y:
      loc.subtract(0, movChange, 0);
      break;
    case Z:
      loc.subtract(0, 0, movChange);
      break;
    }
    armorStand.teleport(loc);
  }

  private void rotate(ArmorStand armorStand) {
    Location loc = armorStand.getLocation();
    float yaw = loc.getYaw();
    loc.setYaw((yaw + 180 + (float) degreeAngleChange) % 360 - 180);
    armorStand.teleport(loc);
  }

  private void reverseRotate(ArmorStand armorStand) {
    Location loc = armorStand.getLocation();
    float yaw = loc.getYaw();
    loc.setYaw((yaw + 180 - (float) degreeAngleChange) % 360 - 180);
    armorStand.teleport(loc);
  }

  private void copy(ArmorStand armorStand) {
    copySlots.copyDataToSlot(armorStand);
    sendMessage("copied", "" + (copySlots.currentSlot + 1));
    setMode(EditMode.PASTE);
  }

  private void paste(ArmorStand armorStand) {
    ArmorStandData data = copySlots.getDataToPaste();
    if (data == null) return;
    armorStand.setHeadPose(data.headPos);
    armorStand.setBodyPose(data.bodyPos);
    armorStand.setLeftArmPose(data.leftArmPos);
    armorStand.setRightArmPose(data.rightArmPos);
    armorStand.setLeftLegPose(data.leftLegPos);
    armorStand.setRightLegPose(data.rightLegPos);
    armorStand.setSmall(data.size);
    armorStand.setGravity(data.gravity);
    armorStand.setBasePlate(data.basePlate);
    armorStand.setArms(data.showArms);
    // BadWolfMC 2026-11-01: paste must respect the same permission as Invisible mode.
    if (ModePermissions.permitted(EditMode.INVISIBLE, getPlayer()::hasPermission)) {
      armorStand.setVisible(data.visible);
    }
    // Copy only the base scale; keep destination-specific attribute modifiers.
    AttributeInstance scale = armorStand.getAttribute(Attribute.SCALE);
    if (scale != null && data.scale != null) scale.setBaseValue(ScaleAdjustment.clamp(data.scale));
    armorStand.setInvulnerable(data.invulnerable);
    // Powerful entity states require their own permissions even when pasted.
    if (ModePermissions.permitted(EditMode.MARKER, getPlayer()::hasPermission)) {
      armorStand.setMarker(data.marker);
    }
    MovementControl.setCanMove(armorStand, data.canMove, plugin.getMovementKey());
    if (ModePermissions.permitted(EditMode.NOTICK, getPlayer()::hasPermission) &&
        armorStand.canTick() != data.canTick) {
      setTicking(armorStand, data.canTick);
    }
    // Equipment duplication has historically been Creative-only. Keep this
    // guard: allowing Survival pastes would create items from snapshots.
    if (getPlayer().getGameMode() == GameMode.CREATIVE) {
      armorStand.getEquipment().setHelmet(data.head.clone());
      armorStand.getEquipment().setChestplate(data.body.clone());
      armorStand.getEquipment().setLeggings(data.legs.clone());
      armorStand.getEquipment().setBoots(data.feetsies.clone());
      armorStand.getEquipment().setItemInMainHand(data.rightHand.clone());
      armorStand.getEquipment().setItemInOffHand(data.leftHand.clone());
    }
    // Restore lock state only after updating the gear, so a destination's
    // previous/new restrictions cannot interfere with equipment application.
    // The lock mode has its own permission; paste must not bypass it.
    if (ModePermissions.permitted(EditMode.LOCKEQUIPMENT, getPlayer()::hasPermission)) {
      EquipmentLocks.restore(armorStand, data.equipmentLocks);
    }
    sendMessage("pasted", "" + (copySlots.currentSlot + 1));
    if (getPlayer().getGameMode() != GameMode.CREATIVE) {
      sendMessage("pasteequipmentcreativeonly", "warn", null);
    }
  }

  private void toggleLockEquipment(ArmorStand armorStand) {
    if (!ModePermissions.permitted(EditMode.LOCKEQUIPMENT, getPlayer()::hasPermission)) return;
    boolean locked = !EquipmentLocks.isFullyLocked(armorStand);
    EquipmentLocks.setFullyLocked(armorStand, locked);
    sendMessage("setlockequipment", locked ? "on" : "off");
  }

  private void adjustScale(ArmorStand armorStand, int direction) {
    AttributeInstance scale = armorStand.getAttribute(Attribute.SCALE);
    if (scale == null) {
      sendMessage("noscaleattribute", "warn", null);
      return;
    }
    double next = ScaleAdjustment.adjust(scale.getBaseValue(), direction, adjMode);
    scale.setBaseValue(next);
    sendMessage("setscale", String.format(java.util.Locale.ROOT, "%.1f", next));
  }

  private void toggleInvulnerable(ArmorStand armorStand) {
    armorStand.setInvulnerable(!armorStand.isMarkedInvulnerable());
    sendMessage("setinvulnerable", armorStand.isMarkedInvulnerable() ? "on" : "off");
  }

  private void toggleMarker(ArmorStand armorStand) {
    if (!ModePermissions.permitted(EditMode.MARKER, getPlayer()::hasPermission)) return;
    armorStand.setMarker(!armorStand.isMarker());
    sendMessage("setmarker", armorStand.isMarker() ? "on" : "off");
  }

  private void toggleNoTick(ArmorStand armorStand) {
    if (!ModePermissions.permitted(EditMode.NOTICK, getPlayer()::hasPermission)) return;
    setTicking(armorStand, !armorStand.canTick());
    sendMessage("setnotick", armorStand.canTick() ? "off" : "on");
  }

  private void setTicking(ArmorStand armorStand, boolean canTick) {
    if (!canTick) plugin.getHighlightControl().clearOwnedGlow(armorStand);
    armorStand.setCanTick(canTick);
  }

  private void toggleNoMovement(ArmorStand armorStand) {
    boolean wasMovable = armorStand.canMove();
    MovementControl.setCanMove(armorStand, !wasMovable, plugin.getMovementKey());
    sendMessage("setnomovement", wasMovable ? "on" : "off");
  }

  private void toggleGravity(ArmorStand armorStand) {
    armorStand.setGravity(!armorStand.hasGravity());
    String state = armorStand.hasGravity() ? "on" : "off";
    sendMessage("setgravity", state);
  }

  void togglePlate(ArmorStand armorStand) {
    armorStand.setBasePlate(!armorStand.hasBasePlate());
  }

  void toggleArms(ArmorStand armorStand) {
    armorStand.setArms(!armorStand.hasArms());
  }

  void toggleVisible(ArmorStand armorStand) {
    if (!ModePermissions.permitted(EditMode.INVISIBLE, getPlayer()::hasPermission)) return;
    armorStand.setVisible(!armorStand.isVisible());
  }

  void toggleSize(ArmorStand armorStand) {
    armorStand.setSmall(!armorStand.isSmall());
  }

  void cycleAxis(int i) {
    int index = axis.ordinal();
    index += i;
    index = index % Axis.values().length;
    while (index < 0) {
      index += Axis.values().length;
    }
    setAxis(Axis.values()[index]);
  }

  private EulerAngle addEulerAngle(EulerAngle angle) {
    switch (axis) {
    case X:
      angle = angle.setX(Util.addAngle(angle.getX(), eulerAngleChange));
      break;
    case Y:
      angle = angle.setY(Util.addAngle(angle.getY(), eulerAngleChange));
      break;
    case Z:
      angle = angle.setZ(Util.addAngle(angle.getZ(), eulerAngleChange));
      break;
    default:
      break;
    }
    return angle;
  }

  private EulerAngle subEulerAngle(EulerAngle angle) {
    switch (axis) {
    case X:
      angle = angle.setX(Util.subAngle(angle.getX(), eulerAngleChange));
      break;
    case Y:
      angle = angle.setY(Util.subAngle(angle.getY(), eulerAngleChange));
      break;
    case Z:
      angle = angle.setZ(Util.subAngle(angle.getZ(), eulerAngleChange));
      break;
    default:
      break;
    }
    return angle;
  }

  public void setTarget(ArrayList < ArmorStand > armorStands) {
    if (armorStands == null || armorStands.isEmpty()) {
      target = null;
      targetList = null;
      sendMessage("notarget", null);
      return;
    }

    if (targetList == null) {
      targetList = armorStands;
      targetIndex = 0;
      sendMessage("target", null);
    } else {
      boolean same = targetList.size() == armorStands.size();
      if (same)
        for (ArmorStand as: armorStands) {
          same = targetList.contains(as);
          if (!same) break;
        }

      if (same) {
        targetIndex = ++targetIndex % targetList.size();
      } else {
        targetList = armorStands;
        targetIndex = 0;
        sendMessage("target", null);
      }
    }
    target = targetList.get(targetIndex);
    highlight(target);
  }

  // Resolve without side effects so protection can be checked before editing.
  ArmorStand selectedTarget() {
    if (target == null || !target.isValid() ||
        target.getWorld() != getPlayer().getWorld()) {
      target = null;
      targetList = null;
      return null;
    }
    return target;
  }

  ArmorStand resolveTarget(ArmorStand armorStand) {
    ArmorStand selected = selectedTarget();
    if (selected == null || selected == armorStand) return armorStand;

    // A hand-swap selection can substitute for a physically clicked stand
    // only when their bases are essentially co-located (stacked displays).
    // Otherwise a stale selection would make clicking AS2 silently edit AS1;
    // this is especially confusing for per-stand toggles like No Ticking.
    // Marker or distant targets are still explicitly editable through the
    // GUI's Apply/Reverse on Target buttons without a physical click.
    if (selected.getWorld() != armorStand.getWorld() ||
        selected.getLocation().distanceSquared(armorStand.getLocation()) > 0.0625 ||
        selected.getLocation().distanceSquared(getPlayer().getLocation()) > 100) {
      return armorStand;
    }
    return selected;
  }

  ArmorStand attemptTarget(ArmorStand armorStand) {
    ArmorStand selected = resolveTarget(armorStand);
    if (target != null && selected == target) highlight(selected);
    return selected;
  }

  void sendMessage(String path, String format, String option) {
    Component message = Language.component(plugin.getLang().getMessage(path, format, option));
    if (plugin.sendToActionBar) {
      plugin.getServer().getPlayer(getUUID()).sendActionBar(message);
    } else {
      plugin.getServer().getPlayer(getUUID()).sendMessage(message);
    }
  }

  void sendMessage(String path, String option) {
    sendMessage(path, "info", option);
  }

  private void highlight(ArmorStand armorStand) {
    plugin.getHighlightControl().highlight(armorStand);
  }

  public PlayerEditorManager getManager() {
    return plugin.editorManager;
  }

  public Player getPlayer() {
    return plugin.getServer().getPlayer(getUUID());
  }

  public UUID getUUID() {
    return uuid;
  }

  public void openMenu() {
    // Only one next-tick open may be pending for a player at a time.
    if (!isMenuCancelled() && pendingMenuTask == null) {
      pendingMenuTask = plugin.getServer().getScheduler().runTaskLater(plugin, new OpenMenuTask(), 1);
    }
  }

  public void cancelOpenMenu() {
    lastCancelled = getManager().getTime();
    if (pendingMenuTask != null) {
      pendingMenuTask.cancel();
      pendingMenuTask = null;
    }
  }

  boolean isMenuCancelled() {
    return getManager().getTime() - lastCancelled < 2;
  }

  private class OpenMenuTask implements Runnable {
    @Override
    public void run() {
      pendingMenuTask = null;
      Player player = getPlayer();
      if (player == null || !player.isOnline() ||
          !player.hasPermission("asedit.basic") || isMenuCancelled()) return;
      chestMenu.openMenu();
    }
  }
}
