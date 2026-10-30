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

import io.github.rypofalem.armorstandeditor.menu.ASEHolder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;
import static net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText;

//Manages PlayerEditors and Player Events related to editing armorstands
public class PlayerEditorManager implements Listener {
  private ArmorStandEditorPlugin plugin;
  private HashMap < UUID, PlayerEditor > players;
  private ASEHolder menuHolder = new ASEHolder(); //Inventory holder that owns the main ase menu inventories for the plugin
  private ASEHolder equipmentHolder = new ASEHolder(); //Inventory holder that owns the equipment menu
  double coarseAdj;
  double fineAdj;
  double coarseMov;
  double fineMov;
  // Ignore only the exact synthetic event we dispatch, never another player's event.
  private final Set<PlayerInteractAtEntityEvent> syntheticInteractions =
      Collections.newSetFromMap(new IdentityHashMap<>());
  private TickCounter counter;

  PlayerEditorManager(ArmorStandEditorPlugin plugin) {
    this.plugin = plugin;
    players = new HashMap < > ();
    coarseAdj = Util.FULLCIRCLE / plugin.coarseRot;
    fineAdj = Util.FULLCIRCLE / plugin.fineRot;
    coarseMov = 1;
    fineMov = .03125; // 1/32
    counter = new TickCounter();
    Bukkit.getServer().getScheduler().runTaskTimer(plugin, counter, 0, 1);
  }

  // The declared asedit.basic permission is the editor's master gate. The old
  // air-menu-only asedit.use check was undeclared and disagreed with commands.
  private boolean canUseTool(Player player) {
    return player.hasPermission("asedit.basic") &&
        plugin.isEditTool(player.getInventory().getItemInMainHand());
  }

  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  void onArmorStandDamage(EntityDamageByEntityEvent event) {
    if (!(event.getDamager() instanceof Player player) || !canUseTool(player)) return;
    // Damage to any entity must suppress a pending air-menu task, but only
    // armor stands are edited or have their damage event cancelled.
    getPlayerEditor(player.getUniqueId()).cancelOpenMenu();
    if (!(event.getEntity() instanceof ArmorStand as)) return;

    // Tool attacks should not damage stands, even when a region denies editing.
    event.setCancelled(true);
    applyLeftTool(player, as);
  }

  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  void onArmorStandInteract(PlayerInteractAtEntityEvent event) {
    if (syntheticInteractions.contains(event) || event.getHand() != EquipmentSlot.HAND ||
        !(event.getRightClicked() instanceof ArmorStand as)) return;

    Player player = event.getPlayer();
    if (canUseTool(player)) {
      event.setCancelled(true); // Never use vanilla equipment manipulation as an edit.
      applyRightTool(player, as);
      return;
    }

    // The special formatting behavior requires both the master gate and rename.
    // All other items (including unauthorized flint) retain vanilla interaction.
    if (!player.hasPermission("asedit.basic") || !player.hasPermission("asedit.rename") ||
        player.getInventory().getItemInMainHand().getType() != Material.NAME_TAG ||
        !canEdit(player, as)) return;

    ItemStack nameTag = player.getInventory().getItemInMainHand();
    final String name;
    if (nameTag.getItemMeta() != null && nameTag.getItemMeta().hasDisplayName()) {
      name = nameTag.getItemMeta().getDisplayName().replace('&', ChatColor.COLOR_CHAR);
    } else {
      name = null;
    }

    if (name == null) {
      as.setCustomName(null);
      as.setCustomNameVisible(false);
      event.setCancelled(true);
    } else if (!name.equals("")) { // nametag is not blank
      event.setCancelled(true);

      if (player.getGameMode() != GameMode.CREATIVE) {
        if (nameTag.getAmount() > 1) {
          nameTag.setAmount(nameTag.getAmount() - 1);
        } else {
          nameTag = new ItemStack(Material.AIR);
        }
        player.getInventory().setItemInMainHand(nameTag);
      }

      // Minecraft may overwrite the name even if this event is cancelled.
      Bukkit.getScheduler().runTaskLater(plugin, () -> {
        as.setCustomName(name);
        as.setCustomNameVisible(true);
      }, 1);
    }
  }

  @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
  public void onSwitchHands(PlayerSwapHandItemsEvent event) {
    Player player = event.getPlayer();
    // The event's offhand item represents the proposed post-swap state.
    if (!player.hasPermission("asedit.basic") || !plugin.isEditTool(event.getOffHandItem())) return;
    event.setCancelled(true);
    getPlayerEditor(player.getUniqueId()).setTarget(getTargets(player));
  }

  private ArrayList < ArmorStand > getTargets(Player player) {
    Location eyeLaser = player.getEyeLocation();
    Vector direction = player.getLocation().getDirection();
    ArrayList < ArmorStand > armorStands = new ArrayList < > ();

    final double STEPSIZE = .5;
    final Vector STEP = direction.multiply(STEPSIZE);
    final double RANGE = 10;
    final double LASERRADIUS = .3;
    List < Entity > nearbyEntities = player.getNearbyEntities(RANGE, RANGE, RANGE);
    if (nearbyEntities.isEmpty()) return null;

    for (double i = 0; i < RANGE; i += STEPSIZE) {
      List < Entity > nearby = (List < Entity > ) player.getWorld().getNearbyEntities(eyeLaser, LASERRADIUS, LASERRADIUS, LASERRADIUS);
      if (!nearby.isEmpty()) {
        boolean endLaser = false;
        for (Entity e: nearby) {
          if (e instanceof ArmorStand) {
            if (canEdit(player, (ArmorStand) e)) {
              armorStands.add((ArmorStand) e);
              endLaser = true;
            }
          }
        }
        if (endLaser) break;
      }
      if (eyeLaser.getBlock().getType().isSolid()) break;
      eyeLaser.add(STEP);
    }
    return armorStands;
  }

  boolean canEdit(Player player, ArmorStand as) {
    // Keep both synthetic interaction checks for protection plugins. These are
    // synchronous: only our precise event is excluded from our own listener.
    PlayerInteractEntityEvent interact = new PlayerInteractEntityEvent(player, as, EquipmentSlot.HAND);
    PlayerInteractAtEntityEvent interactAt =
        new PlayerInteractAtEntityEvent(player, as, new Vector(0, 0, 0), EquipmentSlot.HAND);
    syntheticInteractions.add(interactAt);
    try {
      plugin.getServer().getPluginManager().callEvent(interact);
      if (interact.isCancelled()) return false;
      plugin.getServer().getPluginManager().callEvent(interactAt);
      return !interactAt.isCancelled();
    } catch (IllegalStateException exception) {
      plugin.getLogger().warning("Unable to check armor stand interaction protection: " + exception.getMessage());
      return false; // Fail closed rather than edit without the protection check.
    } finally {
      syntheticInteractions.remove(interactAt);
    }
  }

  // Check both the physically clicked stand and the effective selected target.
  // A target selected earlier must not bypass protection after permissions or
  // region membership change, or after the player targets another stand.
  private boolean canApplyTool(Player player, ArmorStand clicked, PlayerEditor editor) {
    if (!canEdit(player, clicked)) return false;
    ArmorStand selected = editor.resolveTarget(clicked);
    return selected == clicked || canEdit(player, selected);
  }

  void applyLeftTool(Player player, ArmorStand as) {
    PlayerEditor editor = getPlayerEditor(player.getUniqueId());
    editor.cancelOpenMenu();
    if (canApplyTool(player, as, editor)) editor.editArmorStand(as);
  }

  void applyRightTool(Player player, ArmorStand as) {
    PlayerEditor editor = getPlayerEditor(player.getUniqueId());
    editor.cancelOpenMenu();
    if (canApplyTool(player, as, editor)) editor.reverseEditArmorStand(as);
  }

  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
  void onRightClickTool(PlayerInteractEvent event) {
    // Only genuine air clicks open the menu; never consume any block action.
    // Air events are often already "cancelled" by Paper's vanilla prediction.
    if (event.getHand() != EquipmentSlot.HAND ||
        (event.getAction() != Action.LEFT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_AIR)) return;
    Player player = event.getPlayer();
    if (!canUseTool(player) || event.useItemInHand() == Event.Result.DENY) return;

    // Air's default block-use denial is not a plugin item-use denial.
    // Any entity hit is handled by vanilla/entity events, never by an air menu.
    if (player.getTargetEntity(5) != null) return;
    getPlayerEditor(player.getUniqueId()).openMenu();
  }

  @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
  void onScrollNCrouch(PlayerItemHeldEvent e) {
    Player player = e.getPlayer();
    if (!player.isSneaking() || !player.hasPermission("asedit.basic")) return;
    if (!plugin.isEditTool(player.getInventory().getItem(e.getPreviousSlot()))) return;

    e.setCancelled(true);
    if (e.getNewSlot() == e.getPreviousSlot() + 1 || (e.getNewSlot() == 0 && e.getPreviousSlot() == 8)) {
      getPlayerEditor(player.getUniqueId()).cycleAxis(1);
    } else if (e.getNewSlot() == e.getPreviousSlot() - 1 || (e.getNewSlot() == 8 && e.getPreviousSlot() == 0)) {
      getPlayerEditor(player.getUniqueId()).cycleAxis(-1);
    }
  }

  @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
  void onPlayerMenuSelect(InventoryClickEvent e) {
    if (e.getInventory().getHolder() == null) return;
    if (!(e.getInventory().getHolder() instanceof ASEHolder)) return;
    if (e.getInventory().getHolder() == menuHolder) {
      e.setCancelled(true);
      ItemStack item = e.getCurrentItem();
      if (item != null && item.hasItemMeta()) {
        Player player = (Player) e.getWhoClicked();
        String command = item.getItemMeta().getPersistentDataContainer().get(plugin.getIconKey(), PersistentDataType.STRING);
        if (command != null) {
          player.performCommand(command);
          return;
        }
      }
    }
    if (e.getInventory().getHolder() == equipmentHolder) {
      ItemStack item = e.getCurrentItem();
      if (item == null) return;
      if (item.getItemMeta() == null) return;
      if (item.getItemMeta().getPersistentDataContainer().has(plugin.getIconKey(), PersistentDataType.STRING)) {
        e.setCancelled(true);
      }
    }
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  void onPlayerMenuClose(InventoryCloseEvent e) {
    if (e.getInventory().getHolder() == null) return;
    if (!(e.getInventory().getHolder() instanceof ASEHolder)) return;
    if (e.getInventory().getHolder() == equipmentHolder) {
      PlayerEditor pe = players.get(e.getPlayer().getUniqueId());
      pe.equipMenu.equipArmorstand();
    }
  }

  @EventHandler(priority = EventPriority.MONITOR)
  void onPlayerLogOut(PlayerQuitEvent e) {
    removePlayerEditor(e.getPlayer().getUniqueId());
  }

  public PlayerEditor getPlayerEditor(UUID uuid) {
    return players.containsKey(uuid) ? players.get(uuid) : addPlayerEditor(uuid);
  }

  PlayerEditor addPlayerEditor(UUID uuid) {
    PlayerEditor pe = new PlayerEditor(uuid, plugin);
    players.put(uuid, pe);
    return pe;
  }

  private void removePlayerEditor(UUID uuid) {
    players.remove(uuid);
  }

  public ASEHolder getMenuHolder() {
    return menuHolder;
  }

  public ASEHolder getEquipmentHolder() {
    return equipmentHolder;
  }

  long getTime() {
    return counter.ticks;
  }

  class TickCounter implements Runnable {
    long ticks = 0; //I am optimistic
    @Override
    public void run() {
      ticks++;
    }
    public long getTime() {
      return ticks;
    }
  }
}
