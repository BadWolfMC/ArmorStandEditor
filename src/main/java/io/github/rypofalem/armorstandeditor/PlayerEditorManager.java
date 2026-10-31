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
import io.github.rypofalem.armorstandeditor.menu.EquipmentMenu;
import io.github.rypofalem.armorstandeditor.modes.MovementControl;
import io.papermc.paper.event.player.PrePlayerAttackEntityEvent;
import java.util.ArrayList;
import java.util.Comparator;
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
import org.bukkit.FluidCollisionMode;
import org.bukkit.World;
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
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.event.world.EntitiesLoadEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.RayTraceResult;
import static net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText;

//Manages PlayerEditors and Player Events related to editing armorstands
public class PlayerEditorManager implements Listener {
  private ArmorStandEditorPlugin plugin;
  private HashMap < UUID, PlayerEditor > players;
  private ASEHolder menuHolder = new ASEHolder(); //Inventory holder that owns the main ase menu inventories for the plugin
  private ASEHolder equipmentHolder = new ASEHolder(); //Inventory holder that owns the equipment menu
  // Each equipment inventory is owned by one player; never commit a shared or stale view.
  private final HashMap<UUID, EquipmentMenu> equipmentMenus = new HashMap<>();
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

  // Invulnerable stands may never produce a damage event. Only handle these
  // attacks early; ordinary stands continue through the Phase 2 damage handler.
  // Cancelling here also avoids a second damage event/edit in creative mode.
  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  void onInvulnerableStandAttack(PrePlayerAttackEntityEvent event) {
    Player player = event.getPlayer();
    if (!event.willAttack() || !canUseTool(player) ||
        !(event.getAttacked() instanceof ArmorStand as) || !as.isMarkedInvulnerable()) return;

    // No unprotected fallback: the original synthetic interaction checks
    // still gate the clicked stand AND any selected target.
    event.setCancelled(true);
    applyLeftTool(player, as);
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
    selectTarget(player);
  }

  /** Cycle all stands intersecting the player's sight line, including zero-hitbox markers.
   * Physical block occlusion is checked first; permission/protection remains authoritative.
   */
  public void selectTarget(Player player) {
    if (!player.hasPermission("asedit.basic")) return;
    getPlayerEditor(player.getUniqueId()).setTarget(getTargets(player));
  }

  private ArrayList<ArmorStand> getTargets(Player player) {
    Location eye = player.getEyeLocation();
    Vector direction = eye.getDirection().normalize();
    final double range = 10.0;
    final double padding = 0.35;
    RayTraceResult block = player.getWorld().rayTraceBlocks(
        eye, direction, range, FluidCollisionMode.NEVER, true);
    double maxDistance = block == null ? range :
        Math.min(range, block.getHitPosition().distance(eye.toVector()));

    record Candidate(ArmorStand stand, double distance) {}
    ArrayList<Candidate> candidates = new ArrayList<>();
    // A 15x stand can extend around 30 blocks above its feet; search its
    // base too when the player aims at the visible upper portion.
    for (Entity entity : player.getWorld().getNearbyEntities(eye, range, range + 30, range)) {
      if (!(entity instanceof ArmorStand stand) || !stand.isValid()) continue;
      // The marker has no regular bounding box. Use an approximate visual
      // column with minimum width for tiny or highly scaled stands.
      double hit = Double.POSITIVE_INFINITY;
      if (!stand.isMarker()) {
        BoundingBox box = stand.getBoundingBox().expand(padding);
        RayTraceResult intersection = box.rayTrace(eye.toVector(), direction, maxDistance);
        if (intersection != null) hit = intersection.getHitPosition().distance(eye.toVector());
      } else {
        Vector base = stand.getLocation().toVector();
        double height = stand.isSmall() ? 1.0 : 2.0;
        org.bukkit.attribute.AttributeInstance attr =
            stand.getAttribute(org.bukkit.attribute.Attribute.SCALE);
        if (attr != null) height *= Math.max(0.1, attr.getValue());
        BoundingBox visual = new BoundingBox(
            base.getX() - 0.45, base.getY() - 0.35, base.getZ() - 0.45,
            base.getX() + 0.45, base.getY() + height + 0.35, base.getZ() + 0.45);
        RayTraceResult intersection = visual.rayTrace(eye.toVector(), direction, maxDistance);
        if (intersection != null) hit = intersection.getHitPosition().distance(eye.toVector());
      }
      if (Double.isFinite(hit) && canEdit(player, stand)) candidates.add(new Candidate(stand, hit));
    }
    candidates.sort(Comparator.comparingDouble(Candidate::distance)
        .thenComparing(c -> c.stand().getUniqueId().toString()));
    ArrayList<ArmorStand> result = new ArrayList<>();
    for (Candidate candidate : candidates) result.add(candidate.stand());
    return result;
  }

  /** GUI-only action. No fake clicked entity or damage event is manufactured. */
  public void applySelectedTarget(Player player, boolean reverse) {
    if (!player.hasPermission("asedit.basic")) return;
    PlayerEditor editor = getPlayerEditor(player.getUniqueId());
    ArmorStand selected = editor.selectedTarget();
    if (selected == null || !getTargets(player).contains(selected)) {
      editor.sendMessage("notargetinview", "warn", null);
      return;
    }
    if (!canEdit(player, selected)) return;
    editor.cancelOpenMenu();
    if (reverse) editor.reverseEditArmorStand(selected);
    else editor.editArmorStand(selected);
  }

  @EventHandler(priority = EventPriority.MONITOR)
  void onEntitiesLoad(EntitiesLoadEvent event) {
    for (Entity entity : event.getEntities()) {
      if (entity instanceof ArmorStand stand) {
        MovementControl.restore(stand, plugin.getMovementKey());
      }
    }
  }

  /** Restore explicit freeze settings on chunks that were loaded before enable. */
  void restoreLoadedMovement() {
    for (World world : Bukkit.getWorlds()) {
      for (org.bukkit.Chunk chunk : world.getLoadedChunks()) {
        for (Entity entity : chunk.getEntities()) {
          if (entity instanceof ArmorStand stand) {
            MovementControl.restore(stand, plugin.getMovementKey());
          }
        }
      }
    }
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

  /**
   * Equipment preview slots are NOT ordinary inventory slots: all vanilla top
   * inventory changes are cancelled. An explicit slot swap is committed to the
   * stand immediately, so closing a GUI can never discard equipment escrow.
   */
  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
  void onPlayerMenuSelect(InventoryClickEvent event) {
    Inventory top = event.getView().getTopInventory();
    if (top.getHolder() == menuHolder) {
      boolean denied = event.isCancelled();
      event.setCancelled(true);
      if (!denied && event.getClickedInventory() == top &&
          event.getWhoClicked() instanceof Player player &&
          player.hasPermission("asedit.basic")) {
        ItemStack item = event.getCurrentItem();
        if (item != null && item.hasItemMeta()) {
          String command = item.getItemMeta().getPersistentDataContainer()
              .get(plugin.getIconKey(), PersistentDataType.STRING);
          if ("ase target select".equals(command)) {
            selectTarget(player);
          } else if ("ase target apply".equals(command)) {
            applySelectedTarget(player, false);
          } else if ("ase target reverse".equals(command)) {
            applySelectedTarget(player, true);
          } else if (command != null) {
            player.performCommand(command);
          }
        }
      }
      return;
    }
    if (top.getHolder() != equipmentHolder) return;

    if (!(event.getWhoClicked() instanceof Player player)) {
      event.setCancelled(true);
      return;
    }
    EquipmentMenu menu = equipmentMenus.get(player.getUniqueId());
    if (menu == null || menu.getInventory() != top ||
        !menu.getOwner().equals(player.getUniqueId())) {
      event.setCancelled(true); // A stale inventory must never release its preview items.
      return;
    }

    if (event.getClickedInventory() != top) {
      // Allow normal cursor management in the player's own inventory, but
      // prevent shift-transfer, double-click collection, number-key exchange,
      // hotbar/offhand swaps, item drops and creative cloning.
      if (event.getClick() != ClickType.LEFT && event.getClick() != ClickType.RIGHT) {
        event.setCancelled(true);
      }
      return;
    }

    boolean denied = event.isCancelled();
    event.setCancelled(true);
    if (denied || !EquipmentMenu.isEquipmentSlot(event.getRawSlot()) ||
        (event.getClick() != ClickType.LEFT && event.getClick() != ClickType.RIGHT)) return;

    ArmorStand stand = menu.getStand();
    if (!canEditEquipment(player, stand)) {
      // Close later: closing an inventory inside its click event is unsafe.
      if (!stand.isValid()) {
        Bukkit.getScheduler().runTask(plugin, () -> {
          if (player.isOnline() && player.getOpenInventory().getTopInventory() == top) {
            player.closeInventory();
          }
        });
      }
      return;
    }

    menu.swapWithCursor(event.getRawSlot(), event.getCursor());
    for (EquipmentMenu other : equipmentMenus.values()) {
      if (other != menu && other.getStand() == stand) other.refreshPreview();
    }
    player.updateInventory(); // Resync the cancelled click and the new cursor item.
  }

  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
  void onPlayerMenuDrag(InventoryDragEvent event) {
    Inventory top = event.getView().getTopInventory();
    if (top.getHolder() != equipmentHolder && top.getHolder() != menuHolder) return;
    // Dragging over preview/icon slots can never transfer the displayed copies.
    for (int slot : event.getRawSlots()) {
      if (slot < top.getSize()) {
        event.setCancelled(true);
        return;
      }
    }
  }

  @EventHandler(priority = EventPriority.MONITOR)
  void onPlayerMenuClose(InventoryCloseEvent event) {
    if (event.getInventory().getHolder() != equipmentHolder) return;
    EquipmentMenu menu = equipmentMenus.get(event.getPlayer().getUniqueId());
    if (menu != null && menu.getInventory() == event.getInventory()) {
      equipmentMenus.remove(event.getPlayer().getUniqueId());
    }
    // No delayed commit: equipment was never cleared from the stand, and only
    // successful, authorized cursor swaps were applied to it while open.
  }

  @EventHandler(priority = EventPriority.MONITOR)
  void onPlayerLogOut(PlayerQuitEvent event) {
    equipmentMenus.remove(event.getPlayer().getUniqueId());
    removePlayerEditor(event.getPlayer().getUniqueId());
  }

  public EquipmentMenu openEquipmentMenu(PlayerEditor editor, ArmorStand stand) {
    EquipmentMenu menu = new EquipmentMenu(editor, stand);
    equipmentMenus.put(editor.getUUID(), menu);
    menu.open();
    return menu;
  }

  /** Recheck access at the time of each swap, not only when opening the GUI. */
  private boolean canEditEquipment(Player player, ArmorStand stand) {
    return player.hasPermission("asedit.basic") && stand.isValid() &&
        player.getWorld() == stand.getWorld() &&
        player.getLocation().distanceSquared(stand.getLocation()) <= 100 &&
        canEdit(player, stand);
  }

  public void closeEditorMenus() {
    // Do not leave either inventory type interactive after listeners disable.
    for (Player player : Bukkit.getOnlinePlayers()) {
      Inventory top = player.getOpenInventory().getTopInventory();
      if (top.getHolder() == equipmentHolder || top.getHolder() == menuHolder) {
        player.closeInventory();
      }
    }
    equipmentMenus.clear();
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
