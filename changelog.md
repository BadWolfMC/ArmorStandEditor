# Changelog

Notable changes to the BadWolfMC ArmorStandEditor fork are recorded here.
Keep entries concise and limited to work that has actually been completed.

## Unreleased

### Phase 6 — No Ticking and Phase 5 refinements (prepared; awaiting live acceptance)
- Added opt-in per-stand `NOTICK` mode (`/ase mode notick`), with a Clock icon
  and native `canTick`/`setCanTick` state included in copy/paste. Neither
  world configuration nor untouched stands are modified.
- Added `asedit.marker` and `asedit.notick` (both default `op`) for direct
  mode use and paste, with enforcement after permission changes. No Movement
  remains controlled by `asedit.basic`.
- Moved property icons up one row: Show Arms at 14, Invulnerability at 23,
  Marker at 32, No Ticking at 34, and target actions at 41–43. Retained
  adjacent Size/Scale and Invulnerability/Lock Equipment controls.
- Retained 15-tick editor glow for ticking stands; skip non-ticking stands,
  avoid overwriting unrelated glowing effects and cautiously clear only a
  recently recognized editor-applied effect on switching ticking off.
  Identically matching effects from another plugin are not distinguishable
  by Bukkit's effect API; verify against any such plugins in game.
- Phase 6 targeting follow-up: fixed a stale hand-swap/GUI-selected target
  intercepting clicks on other, separately placed armor stands. Physical
  clicks now operate on the clicked stand unless its base is essentially
  co-located with the selected stand (within 0.25 blocks), retaining stacked
  stand cycling. Explicit GUI target actions remain unchanged. This resolves
  a misleading alternating No Ticking status caused by repeated edits to the
  same selected stand; no world-level tick configuration is accessed.
- **Pending:** live confirmation of the targeting follow-up, persistence/client
  synchronization, permissions, glow and performance regressions on Paper 26.3.
  Operator reports GitHub CI green before this follow-up patch.

### Phase 5 — Targeting, Marker and No Movement (accepted 2026-10-31)
- Added independent Marker and No Movement controls to the grouped GUI and
  `/ase mode`, with copy/paste snapshots for both properties. Marker uses the
  native API; No Movement uses `setCanMove` and an entity PDC override restored
  on chunk entity loads (and loaded chunks on plugin enable), without changing
  untouched stands.
- Reworked hand-swap selection to search a 10-block sight ray for tiny,
  overlapping, scaled and hitbox-less marker stands, stopping at obstructing
  blocks. Added **Select Target**, **Apply to Target** and **Reverse on Target**
  GUI buttons so Marker can be reversed without physically clicking the stand.
  Editor authorization and region protection are rechecked on explicit edits.
- Added concise English fallback strings and a focused Phase 5 checklist.
  **Accepted:** BadWolfMC administration reported all Phase 5 tests passing
  with no regressions. Replaced the unavailable CHAIN material with
  EXPOSED_COPPER_CHAIN in the supplied baseline; Marker permissions and the
  requested GUI layout update are included in Phase 6.

### Phase 4 — Equipment locks and Equipment GUI safety (accepted 2026-10-31)
- Replaced unfinished `DISABLESLOTS` with `LOCKEQUIPMENT` using both native
  equipment lock types on all six slots. Added `asedit.lockequipment` (default
  `op`) to the descriptor; mode selection and pasting lock state require it.
- Copy/paste records each managed lock per slot, leaving unrelated `ADDING`
  locks alone.
- Rebuilt the Equipment GUI as a live equipment preview with explicit,
  immediately committed cursor swaps, rather than clearing the stand until
  inventory close. Revalidates access on swaps, updates concurrent previews,
  and blocks inventory extraction/drag/shift/clone bypasses.
- Grouped property modes in the main GUI, with Size beside Scale and
  Invulnerability beside Lock Equipment (Trial Key). The lock icon now remains
  visible to all editors, while the lock permission still controls selection;
  updated concise English fallback descriptions and instructions.
- Phase 4 copy/paste follow-up: take independent ItemStack snapshots, apply
  Creative-mode equipment before restoring destination equipment locks, and
  explicitly tell non-Creative editors why gear was skipped. Preserved the
  original Creative-only duplication guard and `asedit.lockequipment` gate.
  Subsequent tests established the original reported missing equipment was
  due to Survival-mode copying restrictions, not a reproducible lock regression.
- **Accepted:** CI/CodeQL remained green; BadWolfMC administration reported
  passing equipment-lock and copy/paste matrices in Creative/Survival, with no
  other regressions detected. Unusual crash and concurrency scenarios remain
  tracked for the full Phase 7 gate.

### Phase 3 — Scale and Invulnerability (accepted on `develop` 2026-10-31)
- Added independent `SCALE` editing mode, adjusting the SCALE attribute's base
  from 0.1× to 15.0× in 0.1× Fine or 0.5× Coarse increments without cumulative
  floating-point drift. Original Size mode and existing scale modifiers remain
  independent.
- Added invulnerability toggle with `setInvulnerable` and
  `isMarkedInvulnerable`; retained ordinary damage-event editing and added a
  pre-attack fallback only for marked-invulnerable armor stands, using the
  existing authorization and interaction-based protection gates.
- Included both properties in copy/paste and English fallback messages.
- **Verified (2026-10-31):** GitHub CI/CodeQL stayed green and BadWolfMC
  administration reported all ten Phase 3 in-game regression groups passing,
  including creative-mode attacks, persistence and protected regions.


### Phase 2 — Flint interactions and authorization (accepted on `develop`)
- Restricted editor event handling to players with `asedit.basic` and the
  configured main-hand tool; unauthorized flint, hand-swaps and sneak-scroll
  now leave vanilla events alone. The undeclared `asedit.use` air-menu-only
  check has been replaced by the existing `asedit.basic` master gate, matching
  commands, GUI and actual editing permission. Existing `asedit.basic` holders
  can now open the air menu without a separate `asedit.use` grant.
- Stopped menu opening/cancellation on block clicks and unrelated entity
  attacks; retained air-menu and armor-stand editing controls for editors.
- Kept protection-plugin synthetic interaction checks, replacing the shared
  recursion boolean with an identity-scoped event guard; recheck selected
  targets before edits, respect already-cancelled entity events, and use
  explicit item-use denial to suppress air menus.
- Enforced `asedit.basic` alongside `asedit.rename` for enhanced name-tag
  behavior; corrected case-insensitive invisible-mode permission enforcement
  and gated selection of the still-unimplemented `disableslots` mode.

**Verification (2026-10-31):** BadWolfMC administration confirmed green
GitHub Actions/CodeQL and passing targeted in-game tests, including permissions,
region protection, flint interactions, both hands and target switching.
Offhand-only flint intentionally remains inactive. CMI-protected stands marked
non-interactable/invulnerable can still be edited by authorized ASE users;
this is documented as an integration limitation, not an ASE region bypass.

### Phase 2 follow-up — GUI access in tight spaces (accepted on `develop`)
- Added `/ase gui` for players with the existing `asedit.basic` permission.
  It opens the unchanged editor inventory without requiring flint or an air
  click. No new permission nodes, block-event handling, or GUI changes.
- Kept menu opening on air interactions only; did not use the deprecated,
  incomplete `Material.isInteractable()` classification to intercept block
  clicks.

**Verification (2026-10-31):** BadWolfMC administration confirmed CI and
CodeQL remained green and `/ase gui` worked in game. Native Brigadier command
registration and tab completion are intentionally deferred to Phase 7; no
command migration was implemented in Phase 2.


### Maintenance
- Completed the Maven-to-Gradle Kotlin DSL migration with Java 25 and Paper
  `26.3.build.+`, retaining plugin version `26.3` and unchanged gameplay sources.
- Committed the checksum-verified official Gradle 9.8.2 Wrapper JAR; kept the
  existing Wrapper launchers and distribution checksum verification.
- Corrected packaged `plugin.yml` version substitution, added a JAR integrity
  check, and retained a single downloadable `package.zip` artifact containing
  `armorstandeditor-26.3.jar`.
- Updated GitHub Actions for `develop`/`main`, CodeQL's Gradle build, and
  Dependabot for Gradle and GitHub Actions. Gradle CI and CodeQL passed on
  `develop`; the build selected Paper API `26.3.build.231-stable` dynamically.
- Verified packaged resources, plugin descriptor, and Java 25 bytecode;
  confirmed the plugin loads and basic editing works on a test server.
- Established temporary modernization and regression documents and updated
  `.gitignore` for Gradle output and local archives.

_No gameplay or Java source code was changed in Phase 1. Full in-game regression
checks remain for affected future phases and final release validation._
