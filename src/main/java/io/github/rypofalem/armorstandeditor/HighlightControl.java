package io.github.rypofalem.armorstandeditor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.ArmorStand;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Tracks only the short Glowing effect applied by this editor, in memory.
 * Never overwrites an effect we cannot identify as our own, and never
 * applies a new highlight to a no-ticking stand.
 */
final class HighlightControl {
  private static final PotionEffect HIGHLIGHT =
      new PotionEffect(PotionEffectType.GLOWING, 15, 1, false, false);
  private final Map<UUID, Integer> lastAppliedTick = new HashMap<>();
  private int lastPrunedTick;

  void highlight(ArmorStand stand) {
    if (!stand.canTick()) return;
    UUID id = stand.getUniqueId();
    int tick = Bukkit.getCurrentTick();
    pruneExpired(tick);
    PotionEffect active = stand.getPotionEffect(PotionEffectType.GLOWING);
    Integer previous = lastAppliedTick.get(id);
    // An unknown glow is not ours to remove or replace. The exact effect
    // parameters plus a recent application are used as conservative evidence.
    if (active != null && (previous == null || tick - previous > 15 || !matches(active))) {
      lastAppliedTick.remove(id);
      return;
    }
    // Refresh the same 15-tick highlight only when tracking confirms ownership.
    if (active != null) stand.removePotionEffect(PotionEffectType.GLOWING);
    if (stand.addPotionEffect(HIGHLIGHT)) {
      lastAppliedTick.put(id, tick);
    } else {
      lastAppliedTick.remove(id);
    }
  }

  void clearOwnedGlow(ArmorStand stand) {
    Integer previous = lastAppliedTick.remove(stand.getUniqueId());
    if (previous == null || Bukkit.getCurrentTick() - previous > 15) return;
    PotionEffect active = stand.getPotionEffect(PotionEffectType.GLOWING);
    if (matches(active)) stand.removePotionEffect(PotionEffectType.GLOWING);
  }

  private static boolean matches(PotionEffect effect) {
    return effect != null && effect.getHiddenPotionEffect() == null
        && effect.getAmplifier() == HIGHLIGHT.getAmplifier()
        && effect.getDuration() > 0 && effect.getDuration() <= HIGHLIGHT.getDuration()
        && effect.isAmbient() == HIGHLIGHT.isAmbient()
        && effect.hasParticles() == HIGHLIGHT.hasParticles()
        && effect.hasIcon() == HIGHLIGHT.hasIcon();
  }

  private void pruneExpired(int tick) {
    if (tick - lastPrunedTick < 200) return;
    lastAppliedTick.entrySet().removeIf(entry -> tick - entry.getValue() > 15);
    lastPrunedTick = tick;
  }

  void clearTracking() {
    lastAppliedTick.clear();
  }
}
