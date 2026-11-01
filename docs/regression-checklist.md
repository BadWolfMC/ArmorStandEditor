# ArmorStandEditor regression checklist (temporary)

> **Working checklist.** Establish results against the unchanged deployed or
> baseline plugin during Phase 0. Re-run affected checks after each phase and
> the full list before release. Remove after the project is complete; summarize
> shipped changes in root `changelog.md`.

**Baseline commit:** `09da6ec` (`develop` and `main` at source archive time)
**Paper build / Java runtime:** Paper 26.3 / Java 25 bytecode; exact server build not recorded
**Baseline plugin JAR / version:** `armorstandeditor-26.3.jar` / 26.3 (Maven-built GitHub artifact, production-tested)
**Other relevant plugins / world protection:** Not recorded for Phase 0; verify before testing protections
**Tester and date:** BadWolfMC administration / October 2026 (user-reported production checks)

Use `PASS`, `FAIL`, `BLOCKED`, or `NOT YET IMPLEMENTED` in test notes; a
pre-existing failure is **not** evidence of a regression. Record reproduction
steps and compare to the baseline before filing a regression.

## Phase 0 — Baseline capture

- [x] Record active branch and commit; verify working tree and preserve a
      known-good release JAR (Phase 0 production artifact retained by operators).
- [x] Run the current Maven build with Java 25, or document why it cannot run.
      Maven build passed in GitHub Actions; no local Maven was installed.
- [x] Inspect the packaged JAR's `plugin.yml`, including its resolved version.
      Maven artifact preserved unresolved `${project.version}`; Phase 1 addresses this.
- [ ] Inventory existing GUI layout/modes, command behavior, config defaults,
      translation fallback, and permission nodes (including undeclared nodes).
- [ ] Record current behavior for flint interacting with chests, doors,
      buttons, unrelated entities, and air — even if currently inconvenient.
- [ ] Record existing pose/rotation/placement, Size, Visibility, Gravity,
      arms/baseplate, equipment, rename, copy/paste and reset behavior.
- [ ] Record basic restart/persistence behavior in a disposable test world.

## Phase 2 — Flint interactions and authorization (closed 2026-10-31)

**Live status (2026-10-31):** BadWolfMC administration confirmed green Gradle
CI and CodeQL, and that all eight targeted Phase 2 test groups passed on the
server. Earlier local compilation remained unavailable (Java 21 only, no
cached Gradle distribution/network). Exact Paper build and protection-plugin
versions were not supplied. `/ase gui` was subsequently CI-tested and verified in game; see the follow-up
section below.

**Permission matrix to verify** (check via LuckPerms/equivalent, using actual
BadWolfMC permission groups, not an assumed role hierarchy):

| Effective permissions | Expected editor behavior |
| --- | --- |
| Neither `asedit.basic` nor `asedit.use` | Completely vanilla flint; `/ase` denied |
| `asedit.use` only | Completely vanilla flint; `/ase` denied |
| `asedit.basic` only | Tool editing, air menu, commands and GUI enabled; invisible/rename additionally require their own permissions |
| `asedit.basic` + `asedit.use` | Same as `asedit.basic` only; no additional edit powers |
| `asedit.basic` with `asedit.invisible` explicitly denied | Invisible mode cannot be selected via `/ase` in any letter case and cannot be applied |
| `asedit.basic` with `asedit.rename` denied | No plugin-enhanced name-tag formatting (vanilla naming remains available where permitted) |

`asedit.use` was never declared in `plugin.yml` and previously gated only
the air menu. `asedit.basic` was already the command/GUI/actual-edit gate;
Phase 2 uses it for all tool events. **Confirm this explicit policy with the
real permission assignments before accepting Phase 2.** No new permission
nodes, defaults or inheritance were introduced. `asedit.disableslots` was a nonfunctional placeholder replaced in Phase 4
with `asedit.lockequipment` (default `op`), pending live verification.

### Targeted steps (run in Survival and Creative as applicable)

1. **Unprivileged** (no `asedit.basic`): hold plain flint in main hand; open a
   chest, barrel, door, trapdoor, gate; press stone/wood buttons, use levers,
   interact with ordinary and protected blocks. Compare to empty hand. No
   editor GUI, action bar, sound, cancellation or lost interactions.
2. **Unprivileged**: left-click a mob/player/item frame/other entity, and
   right-click an armor stand with flint. Damage/interactions must be normal
   for their game mode and region. Sneak-scroll and press F: vanilla slot
   switching and hand swapping, even with flint in offhand.
3. **Authorized** (`asedit.basic`): repeat the block/other-entity checks while
   holding flint. Blocks and unrelated mobs must behave as vanilla; no editor
   menu when hitting a mob. Left/right click air with clear sightline: one
   menu opens. Test while looking directly toward an entity: no false menu.
4. **Authorized**: select a rotation/movement/pose mode. Left click an armor
   stand, then right click; both directions work and the armor stand takes no
   attack damage. Sneak-scroll cycles axes without moving hotbar selection;
   swap flint from main hand to offhand cycles/selects nearby overlapping
   armor stands without actually swapping items.
5. **Both hands**: flint in main hand, regular item in offhand; repeat right
   clicks on stands, air, chests/doors and entity attacks. Confirm one edit
   maximum per click. Then put flint only in offhand; no editor interception
   on offhand events. Swapping offhand flint back should remain vanilla unless
   the configured main-to-offhand targeting shortcut is in use.
6. **Protections**: place stands inside denied and allowed regions. Test
   left/right click, name-tag formatting, and hand-swap target selection in
   both regions; denial must block edits. Select a stand while permitted,
   then change its region permission or move to a restricted situation before
   editing again. No stale-target bypass. Also test already-cancelled entity
   damage/interact events from other plugins.
7. **Two players**: Player A target-cycles stands while Player B interacts
   with another stand (one protected, one allowed); neither user's event is
   silently skipped or authorized by the other's synthetic check. Repeatedly
   alternate and test in the same tick if practical.
8. **Mode permissions / GUI**: non-op editor with only `asedit.basic` can
   select a regular mode and use the air menu; player with only `asedit.use`
   cannot. `/ase mode invisible` and mixed-case variants require
   `asedit.invisible`; formatted name tags require `asedit.rename` plus basic.
   Exercise Equipment, copy/paste and ordinary editing to confirm parity.
9. **Protection-specific**: if your protection plugin cancels air item use,
   confirm its item-use `DENY` prevents the air menu; test region-boundary
   interactions where another plugin cancels at the same/higher priority.

### Acceptance checkboxes

User confirmed the targeted test groups passed on 2026-10-31. The individual
checks below record the accepted Phase 2 scenarios rather than an independently
observed test run.

- [x] Real BadWolfMC group grants match the permission matrix above; no
      unintended access has been given or removed.
- [x] No editor permission + flint: ordinary chest/door/button interaction.
- [x] No editor permission + flint: ordinary block and entity interactions;
      no unsolicited GUI, cancellation, messages, or sound.
- [x] No editor permission + flint: ordinary sneak-scroll and hand swap.
- [x] Authorized editor + flint: chest, door, button and other interactive
      blocks still work; opening the chest doesn't open the editor instead.
- [x] Authorized editor + flint: attacks on unrelated entities stay vanilla.
- [x] Authorized editor + flint: air menu and selected stand editing still work.
- [x] Authorized editor + flint: left/right editing, axis change and target
      cycling have expected direction and outcome.
- [x] Both hands: offhand events do not trigger duplicate or unexpected edits.
- [ ] Edge-case protection: explicit `DENY` of air-item use by another
      plugin prevents opening the menu (not specifically confirmed in the
      reported eight test groups).
- [x] Protected region: editing is refused where region protection forbids it;
      normal unprotected editing is allowed; stale selections cannot bypass it.
- [x] Multi-player simultaneous use: no shared event guard or selected-target
      state leaks between players.
- [x] Creative and survival behaviors match intended editor permissions.

### Phase 2 confined-space GUI follow-up — accepted 2026-10-31

BadWolfMC administration confirmed that CI remained green and `/ase gui`
worked as intended in game. These are operator-reported results, not a fresh
independent test run. Keep more granular cases that were not separately
reported for the final regression pass.

- [x] GitHub Actions Gradle build and CodeQL remain green with `/ase gui`.
- [x] Authorized editor can open the existing GUI with `/ase gui`, including
      when air-click access is impractical (operator-confirmed behavior).
- [ ] Recheck `/ase gui` with no flint held while looking at stone, a chest,
      and a door; confirm identical inventory layout (not separately recorded).
- [ ] Explicitly test that a user lacking `asedit.basic` and the console
      cannot invoke `/ase gui` (not separately recorded).
- [ ] Recheck air-click GUI, vanilla block interactions, unrelated entity
      attacks, offhand-only flint and swap-targeting after any command changes.

**Known compatibility observation (2026-10-31):** CMI's non-interactable and
invulnerable armor-stand protection did not stop an authorized ASE editor,
although tested region protections did. The two mechanisms should not be
assumed equivalent; recheck when relevant protection configuration changes.

## Existing editor behavior (repeat after each affected phase)

- [ ] Pose rotations: head/body, both arms, both legs, all axes and steps.
- [ ] Placement and rotation: coarse/fine increments and negative direction.
- [ ] Existing Size toggle remains normal/small independently of Scale.
- [ ] Visibility, Gravity, arms and baseplate toggles still work.
- [ ] Rename with name tag keeps expected formatting and item usage.
- [ ] Editor menu and Equipment GUI open/close normally with permissions.
- [ ] Copy/paste slots preserve intended existing properties and equipment.
- [ ] Equipment GUI disconnect, stand removal, restart/shutdown do not lose or
      duplicate items; note if a scenario is an existing baseline limitation.
- [ ] Locale selection and English fallback handle existing and new labels.

## Phase 3 — accepted 2026-10-31

BadWolfMC administration reported green CI/CodeQL and completion of all ten
Phase 3 live test groups. The groups covered the checks below; the more
specialized damage-event-only integration scenario remains an explicitly
unverified compatibility edge, not a claim of universal protection support.

- [x] Scale: default 1.0 base; `SCALE` mode left-click increases and right-click
      decreases by exactly 0.1 (Fine) or 0.5 (Coarse) regardless of selected axis;
      repeat ≥100 times without drift. At 0.1/15.0, further clicks clamp.
- [x] Scale: modifiers previously attached to a stand remain present after
      adjustment and paste. The base clamps even if modifiers change the final
      effective scale. `SIZE` still changes only `isSmall()`, and vice versa.
- [x] Scale: check normal, small, 0.1× and 15.0× stands, including direct hits,
      alternate targeting and GUI via `/ase gui` in tight spaces. Verify values
      after world save/restart and after copy/paste between different stands.
- [x] Copy/paste: copy all poses/boolean attributes and scale plus invulnerability
      between two stands; verify destination attribute modifiers are not erased,
      and copy slots 1–9 retain independent snapshots.
- [x] Invulnerability: toggle on/off in both click directions; left-click again
      with flint on an invulnerable stand and check toggling works; test other
      modes (Scale, poses, movement, rotation, equipment) on that stand too.
- [x] Invulnerability: confirm survival-mode damage resistance, creative-mode
      destruction behavior without flint, and no double edits or destruction
      with authorized flint in creative. Verify persistence after restart.
- [x] Permissions: players without `asedit.basic` get vanilla flint behavior;
      editor permissions do not bypass protected-region denial for either click
      direction, including pre-attack on invulnerable stands and stored targets.
      Damage-only event integration remains a separate, unverified edge case
      (early attack edits may not be vetoed by damage-only integrations).
- [x] Existing behavior: clicking blocks, attacking unrelated entities, hand
      swapping, sneak-scrolling, name tags, `/ase gui`, GUI icons and language
      English fallback still behave as established in Phase 2.
## Phase 4 — equipment locks and GUI safety (accepted 2026-10-31)

- [x] At Phase 4 acceptance, Lock Equipment displayed as a Trial Key immediately
      after Invulnerability and was visible to basic-only editors, but selecting
      it required `asedit.lockequipment`. **This earlier icon-visibility choice
      was superseded by the 2026-11-01 release-candidate hardening below:**
      restricted icons are now hidden when not permitted. `asedit.basic`
      remains required. Old `disableslots` mode is unavailable.
- [x] Lock all six slots (helmet, chest, leggings, boots, main hand, offhand):
      normal survival/creative interactions cannot add, remove or swap items,
      even for a builder; toggle off restores normal vanilla manipulation.
- [ ] After a full lock, save/restart the server and confirm it persists.
      An intentionally partially locked stand becomes fully locked on toggle;
      a fully locked stand becomes unlocked on the next toggle.
- [ ] Copy/paste lock state between stands and across copy slots 1–9, including
      per-slot partial lock states and unrelated `ADDING` locks. A player with
      basic-only permission must NOT change locks by pasting a locked snapshot.
- [x] **Phase 4 copy/paste follow-up:** in Creative mode copy a fully equipped
      stand and paste to an empty stand for all four source/destination lock
      combinations (locked→locked, locked→unlocked, unlocked→locked,
      unlocked→unlocked). Verify all six items match the source, as well as
      poses, Size/Scale and Invulnerability; repeat with source gear later
      changed to confirm the stored snapshot is independent.
- [ ] Repeat equipment paste with a basic-only editor lacking
      `asedit.lockequipment`: gear pastes in Creative mode, but the destination's
      lock state must remain unchanged. Test independently in Survival:
      properties paste, no items are created, and the Creative-only gear
      warning appears. Do not treat this longstanding anti-duplication rule
      as successful equipment restoration in Survival.
- [x] With equipment locking ON, open Equipment GUI using left/right flint.
      In both creative and survival, pick up a helmet/held item using the empty
      cursor, then insert/swap another item; changes appear immediately on the
      stand. Right click intentionally performs the same full-stack swap.
- [ ] While the GUI is open, stand gear stays equipped and visible to another
      player. Normal close, Escape, opening another inventory or `/ase gui`
      never returns gear to an old snapshot or overwrites external changes.
- [ ] Try shift-click from the bottom inventory, number keys, offhand swap,
      double-click collection, middle-click/creative clone, dropping via keys,
      and dragging across icon/preview slots. No icon or duplicate preview
      item can enter a player's inventory or be left in a filler slot.
- [ ] Leave an item on the cursor; test normal close and disconnect (including
      full player inventory), then reconnect. Confirm item counts, without
      assuming cursor persistence; check for drops, loss or duplication.
- [ ] Remove/despawn the stand while its Equipment GUI is open; click preview
      slots. No ghost gear can be withdrawn. Similarly test a permission revocation
      or newly protected region while GUI remains open: all swaps fail closed.
- [ ] Open two Equipment GUIs to the same stand from two authorized accounts;
      alternate swaps. Both previews update, and the actual stand equipment is
      authoritative; neither editor can duplicate or overwrite stale gear.
- [ ] Disable/reload the plugin normally while a GUI is open; inventory closes,
      gear remains on the stand, and no pending close-time commit occurs.
      A power loss across distinct player/entity disk saves is not guaranteed
      transactional; assess separately rather than claiming absolute immunity.
- [ ] Check updated GUI grouping and short lore, the `asedit.lockequipment`
      permission default, English fallback on non-English languages, normal
      item naming and equipment copy/paste, and the complete Phase 2/3 flint,
      targeting, protection, Scale and Invulnerability regression baseline.

**Phase 4 closeout (2026-10-31):** Operator confirmed CI/CodeQL green,
locking and equipment-paste matrix in both Creative and Survival (with
Creative-only equipment pasting understood), and no other detected regressions.
The original Phase 4 source correction, GUI visibility fix and equipment
paste ordering/snapshot follow-up are included. Unchecked extraordinary cases
remain in the checklist for the full Phase 7 regression gate; these are not
separately claimed as witnessed.

## Phase 5 — Targeting, Marker and No Movement (accepted 2026-10-31)

- [x] After switching modes normally, look toward an ordinary armor stand and
      press the hand-swap key with flint; target selection still works. Press
      again to cycle overlapping stands, and verify that protected stands are
      not made selectable by the new targeting code.
- [x] Select **Marker** from the GUI or `/ase mode marker`, enable it, then
      recover the same stand without its normal hitbox: look toward it, use
      hand-swap or the **Select Target** GUI icon, then **Apply to Target** to
      disable Marker. Repeat on invisible, small, Scale 0.1, Scale 15 and
      overlapping stands, in Creative and Survival.
- [x] With a selected stand in view, **Apply to Target** performs the left-click
      direction, **Reverse on Target** the right-click direction (especially
      Scale, Placement and Rotate); toggles apply only once. With no target,
      out of range or looking away, no edit occurs and a warning appears.
- [x] Test walls, floors and dense displays: no targeting through solid blocks,
      no accidental editing of a different stand, and repeat cycling when
      selection changes or an entity is removed.
- [x] Verify normal flint on blocks and unrelated entities, sneak-scroll,
      the offhand policy, `/ase gui`, original modes and Equipment GUI still
      behave as they did after Phases 2–4.
- [x] Test **No Movement** ON/OFF with and without Gravity: collisions/pushing
      are blocked when frozen, while intentional placement/rotation/poses and
      equipment editing remain possible; thaw does not unintentionally move
      the stand or alter gravity.
- [x] Restart the server and reload chunks with frozen and unfrozen stands.
      Frozen stands remain frozen via the PDC setting, thawed/untouched stands
      move normally, and other plugins' armor stands are not automatically
      modified. Repeat across unloaded chunks and plugin startup.
- [x] Copy/paste Marker and No Movement alongside Scale, Invulnerability and
      locks, including a marker source/destination and locked/unlocked target;
      Creative pastes equipment, Survival skips it with existing warning.
- [x] As a player lacking `asedit.basic`, ensure target GUI, selecting, editing
      or toggling does not work, and ordinary flint remains vanilla. With a
      region-protection denial or permissions revoked after target selection,
      **Apply to Target** and reverse are denied and properties remain intact.
- [x] Confirm Phase 5 GUI positions in its accepted build (41=Marker,
      42=No Movement; 43 reserved for Ticking); Phase 6 relocates these controls
      and introduces the tick toggle after this baseline.
- [x] Operator confirmed Phase 5 tests passed with no regressions; Material.CHAIN
      was corrected to EXPOSED_COPPER_CHAIN in the uploaded baseline.

## Phase 6 — No Ticking and restricted Marker (accepted 2026-10-31)

BadWolfMC administration reports that the full requested live Phase 6 tests
passed following the shared-target isolation correction and CI remained green.
The checks below are marked **operator-reported**, not independently rerun
against Paper by this documentation update. The optional spark comparison
is summarized separately and is not considered a performance guarantee.

- [x] `asedit.marker` and `asedit.notick` default to `op`. An editor with only
      `asedit.basic` sees both icons but cannot select either mode, including
      `/ase mode marker` and `/ase mode notick`; the permission denial is clear.
- [x] Grant/revoke each permission at runtime: direct left/right clicks,
      selected-target Apply/Reverse and typed commands cannot bypass either
      gate after revocation. No Movement still works under `asedit.basic`.
- [x] Paste snapshots containing Marker ON/OFF and No Ticking ON/OFF into
      destination stands in all four source/destination combinations, first
      with then without each permission. Unauthorized Paste must not change
      the protected property, while ordinary properties still paste normally.
      Verify the two permissions independently, including after revoke.
- [x] Confirm GUI slots: Show Arms 14, Visibility 15, Size 16, Scale 17;
      Invulnerability 23, Lock Equipment 24, Gravity 25, Baseplate 26;
      Marker 32, No Movement 33, No Ticking 34; Select Target 41, Apply 42,
      Reverse 43. No missing/overlapping icons; `EXPOSED_COPPER_CHAIN` works.
- [x] No Ticking toggles `canTick()` per stand in Creative and Survival,
      independent of Gravity, No Movement, Marker, Scale and Invulnerability.
      Untouched stands retain their existing ticking state when plugin enables.
- [x] **Target isolation / Phase 6 follow-up:** select AS1 with F or the
      Select Target GUI, then physically click different, separately placed
      AS2 and AS3 using No Ticking. Each click changes **only the stand hit**,
      not the previously selected AS1. Alternate AS1/AS2/AS3 and verify the
      feedback matches each stand's state; repeat in both game modes. Use the
      GUI's Apply/Reverse controls to deliberately edit the selected stand.
- [x] Place two precisely co-located/stacked stands; verify hand-swap cycling
      can still direct a physical tool click to the selected stand, including
      toggles and region-protection checks. Click an unrelated stand farther
      than 0.25 blocks from that stack: only the clicked stand changes.
- [x] Check each stand's ticking independently via the native Paper API or
      `data get entity` with a nearest-entity selector after deliberately
      isolating the stand (Paper writes `Paper.CanTickOverride` for an explicit
      setter call). Compare before/after chunk reload and restart. Do not
      interpret an alternating status message alone as evidence of a global
      or world-level setting change.
- [x] Explicit Ticking ON/OFF survives chunk unload/load and server restart;
      updating No Movement PDC does not overwrite the Paper ticking override.
- [x] Pose/placement/rotation, all six equipment slots in the authorized GUI,
      copy/paste, targeting and invulnerable-stand left clicks work when no-tick.
      Confirm client display sync and persistence after a save/restart.
- [x] Ordinary ticking stands show the normal brief (~15 tick) selection glow.
      A stand marked no-tick receives no new editor-applied glow, but can still
      be selected and edited with the marker-recovery target controls.
- [x] Switch ticking OFF while ASE's short glow is active: glow clears rather
      than becoming stuck. Test a deliberate pre-existing or separately applied
      Glowing effect: selecting a stand must not overwrite it, and No Ticking
      must not strip it. Check distinct amplifiers/durations and effect refresh.
- [x] Compare burning, status-effect expiry, physics/gravity, collisions,
      normal/creative damage and interaction on ticking vs non-ticking stands;
      record behavior differences without assuming optimization is universal.
- [x] Test combined Marker + No Ticking + small/0.1-scale, invisible,
      Invulnerability and Lock Equipment; recover each toggle using Select
      Target + Apply/Reverse without bypassing protection plugins.
- [x] Recheck standard flint permissions, chest/door/button interactions,
      sneak-scroll/swap, `/ase gui`, Equipment GUI, copy/paste Creative-only
      equipment guard and Phase 2–5 editing modes.
- [x] GitHub Actions Gradle Java 25 build and CodeQL remain green; record Paper
      build and in-game test results before Phase 6 acceptance.

## Phase 7 — Native Brigadier commands and tab completion (release candidate; pending acceptance)

- [ ] `/ase` is registered exactly once using Paper's lifecycle command API;
      `plugin.yml` remains the plugin descriptor, without a duplicate legacy
      `/ase` command declaration.
- [ ] Verify suggestions and execution for `/ase gui`, `help`, `?`, `mode`,
      `axis`, `adj` and `slot` with lowercase input and partial completion.
- [ ] Tab completion suggests valid edit modes, `X/Y/Z` axes, `COARSE/FINE`
      adjustments, and copy slots 1–9; all new modes are included.
- [ ] Unimplemented modes are not suggested; missing `asedit.basic` hides
      or denies editor commands, and restricted modes are omitted/denied in
      accordance with their existing permission nodes.
- [ ] Server-side authorization still rejects manually typed commands that
      were hidden from suggestions; permission changes are reflected when
      command trees are refreshed as appropriate.
- [ ] Existing `/ase` behavior, help text, error feedback and `/ase gui`
      access work for authorized players; non-player command behavior remains
      appropriately denied. Verify no unintended aliases or name collisions.
- [ ] Command registration works after restart and supported reload paths;
      Gradle/CodeQL pass and full gameplay regression remains green.

## Phase 7 hardening follow-up (pending live acceptance)

These checks cover fixes identified in the source-level deep review. No live
results are claimed here; keep the Phase 7 sign-off unchecked until retested.

- [ ] **Click-handler inventory transitions:** from main GUI with Equipment mode
      selected, use **Apply to Target** and **Reverse on Target**. Equipment GUI
      should open on the next tick without errors, cursor loss or duplication.
      Use Help icon and check menu closes with help displayed. Independently
      confirm `/ase help` and `/ase gui` still work when typed directly.
- [ ] **Deferred permission and UI checks:** revoke `asedit.basic` after opening
      the GUI, and close/switch inventory immediately after clicking Apply or
      Help. Neither stale deferred action nor permission revocation may open
      another inventory or edit a stand. Stand targeting and protection should
      still be rechecked when Apply/Reverse executes.
- [ ] **Pending flint air-menu:** rapid-click air repeatedly; only one menu
      should open. Click air and immediately attack/edit a stand; the pending
      menu must not override the interaction. Quit immediately after a click,
      then reconnect; no stale menu/task or exception should occur. Repeat
      around plugin disable/restart.
- [ ] **Invalid config test on disposable server:** try `tool: NOT_A_MATERIAL`,
      `coarse: 0`, `fine: 0`, negative values and extremely small divisors.
      Startup logs should warn and use FLINT/12/120. Help must not throw,
      poses must remain finite, and restoring valid custom values should
      retain their original behavior.
- [ ] **Language rendering:** verify translated colors on GUI item display
      names, lore, Equipment GUI title/icon labels, and player action-bar/chat
      messages; preserve actual language strings/fallbacks. Test help text
      and switching between shipped locales as before.
- [ ] **Name-tag race:** rename a valid stand as before; if a stand is removed
      immediately afterward, the delayed task must not throw. A consumed
      name tag is not refunded when the target becomes invalid (unchanged).
- [ ] **Existing sensitive cases:** two editors with the same Equipment GUI,
      full inventory and alternate hotbar/cursor interactions, locked gear,
      15× stand upper-body equipment reach, region protections, Marker recovery,
      per-stand No Ticking and legacy unprivileged flint interactions.

## 2026-11-01 permission / GUI follow-up (implementation complete; live test pending)

A reported permission regression involved a basic-only editor selecting a Marker
stand, attempting the forbidden Marker mode and clicking Apply to Target. The
mode command already denied selection and the toggle/paste methods had their
own restricted-property checks; the complete live reproduction remains
unverified against this source. Release-candidate hardening additionally
checks the active mode at the edit entry points and clears a stale mode after
any forbidden command selection. Four restricted icons are hidden on menu
refresh; server-side authorization is still mandatory.

- [ ] With **only** `asedit.basic` (non-op), open `/ase gui` and the flint air
      menu: slots 15 (Invisible), 24 (Lock Equipment), 32 (Marker) and 34 (No
      Ticking) are blank. Nonrestricted properties (Size, Scale, No Movement,
      Invulnerability, Equipment, Copy/Paste) remain visible and usable.
- [ ] Grant/revoke **each** restricted node independently. Reopening the GUI
      reveals/hides **only** its corresponding icon. No logout or restart
      should be necessary; direct `/ase mode <restricted>` and tab suggestions
      follow the same current permissions. The name-tag permission has no
      corresponding main-GUI icon.
- [ ] With basic-only, select a valid mode, then manually type a **denied**
      `/ase mode marker` (and repeat for invisible, lockequipment, notick).
      Verify that denial is shown and the old mode is cleared: **Apply to Target**
      and **Reverse on Target** must not modify the selected armor stand.
- [ ] With Marker permission temporarily granted, select Marker and target a
      stand; then revoke Marker without changing modes. Check **Apply**, **Reverse**,
      left-click flint and right-click flint. All must leave Marker unchanged
      and show denial. Repeat this mid-session revocation for Invisible,
      Lock Equipment, and No Ticking. Regrant, select the mode again and verify
      authorized editing works in both directions.
- [ ] Copy a stand with all four restricted properties ON. As a basic-only
      editor, paste to a destination with distinct state and verify Invisible,
      Marker, Lock Equipment and No Ticking stay **unchanged** while permitted
      attributes still paste. Grant each node independently and confirm its
      property can then paste. Preserve Creative-only gear-paste rules.
- [ ] Repeat GUI Apply/Reverse with region-protection denial and with
      `asedit.basic` revoked after the menu is opened. Neither restricted
      edits nor ordinary edits may bypass the existing protection checks.

## Final Phase 7 in-game release-candidate regression (ALL UNCHECKED)

Run on a disposable Paper **26.3 / Java 25** test server with the candidate
built from `develop`, using actual BadWolfMC permission groups and protection
plugins. Record full Paper build, candidate JAR SHA-256 and source commit in
**Test notes**. Test permissions using a non-op account, plus a second editor
and an unprivileged player. Preserve the production JAR as rollback.

### A. Command tree, completion and authorization

- [ ] Start with the old production JAR absent, load the new candidate, and
      inspect `minecraft:help`, `/help ase`, console command output and server
      logs: exactly one `/ase` from ArmorStandEditor, no command-map collisions,
      duplicate labels or legacy registration warnings; `/ase:...` naming works
      as Paper defines it. Test command refresh via Paper's supported reload
      path; avoid legacy `/reload` on production.
- [ ] As an editor, run `/ase`, `/ase help`, `/ase ?`, `/ase gui` using lowercase-only
      syntax. Confirm uppercase inputs (`/ASE`, `/ase GUI`,
      `/ase mode Scale`) are rejected cleanly. Match help, sounds and menu behavior. Check `/ase gui` without flint while looking at
      a chest/door and while another inventory is open.
- [ ] Complete `/ase <TAB>`, `/ase mo<TAB>`, `/ase mode <TAB>`,
      `/ase mode lo<TAB>`, `/ase axis <TAB>`, `/ase adj <TAB>` and
      `/ase slot <TAB>`; assert suggestions use lowercase and offer
      all implemented modes (including `size`, `scale`, `invulnerable`,
      `marker`, `nomovement`, `notick` and `lockequipment`),
      `x/y/z`, `coarse/fine`, slots 1–9.
      No `disableslots` or unrelated modes may appear.
- [ ] Check `/ase mode`/`axis`/`adj`/`slot` missing value warnings, bad values,
      out-of-range slots (`0`, `10`, `257`, `-1`, nonnumbers), unknown first
      subcommands and additional unexpected arguments. Confirm no player
      state changes or stack traces for invalid input.
- [ ] Run each valid command as non-op `asedit.basic` holder; confirm commands
      take effect and GUI icons continue to dispatch modes and copy slots.
      Verify help and completion reflect the same current mode options.
- [ ] With `asedit.basic` missing (including an account with only `asedit.use`),
      check `/ase`, `/ase gui`, typed `/ase mode scale`, and tab suggestions.
      All attempts must fail safely with no GUI, state changes or permissions
      leaked through suggestions. Console/RCON and command blocks must not
      open menus or edit armor stands; check denial/unknown-syntax responses.
- [ ] Grant `asedit.basic` but deny each of `asedit.invisible`,
      `asedit.lockequipment`, `asedit.marker`, `asedit.notick` independently;
      check suggestion omission, direct lowercase command denial, GUI icon
      **hiding** (reopen GUI), denied Apply/Reverse behavior and paste permission
      isolation. Regrant in session, refresh the command tree as needed, and
      check changes take effect without logging out. Test revoked `asedit.basic`
      after GUI opened.
- [ ] Restart and perform a supported command-tree refresh to confirm `/ase`
      remains single and suggestions still function; check any command aliases
      or name collisions with other plugins installed on BadWolfMC.

### B. Unchanged flint and GUI behavior

- [ ] Without editor rights, repeat flint chest/barrel/door/button/lever,
      unrelated mobs/players and armor-stand interaction in both game modes;
      hand swap, sneak-scroll and offhand-only flint remain vanilla.
- [ ] With rights, ensure normal block/entity interactions are untouched;
      flint air-click opens exactly one menu, and targeting hand-swap, axes,
      physical click selection and intentional stacked cycling work normally.
      Physical hits on stands more than 0.25 blocks apart must not be stolen
      by a previously selected target.
- [ ] Select each pose, placement/rotate, body part, axis and adjustment mode
      via both GUI and commands. Verify left vs right edit directions, reset,
      arms, visibility, gravity, baseplate and normal/small Size. Verify
      name-tag formatting and `asedit.rename` permissions.
- [ ] Select and recover very small, very large, invisible, hitbox-less Marker
      and overlapping stands using Select/Apply/Reverse Target GUI controls;
      test walls and region boundaries (no target-through-block bypass).
- [ ] Change region protection permissions after selecting a target; edit,
      target actions, invulnerable pre-attack and Equipment GUI swaps must
      recheck permissions. Two players' targets and synthetic events must
      remain isolated; verify CMI-specific limitations documented above.

### C. Phase 3–6 features and persisted stand data

- [ ] Scale 0.1–15.0 bounds, Fine 0.1/Coarse 0.5 increments, 1.0 base and
      preserved attribute modifiers; Size independent; test direct edits,
      command/GUI selection and copy/paste between stands.
- [ ] Invulnerability ON/OFF including survival damage vs creative behavior,
      creative flint left click and no duplicate changes; other modes still
      operate on invulnerable stands.
- [ ] Lock Equipment blocks vanilla changes in all six slots, including for
      editors, but authorized Equipment GUI swaps still work. Test mixed
      creative/survival lock/equipment paste combinations, partial lock states,
      default `ADDING` locks and unauthorized lock-state paste.
- [ ] Equipment GUI: preview remains live without removing gear; deny shift,
      drag, number/offhand swap, clone, double-click and ghost-item extraction.
      Check disconnect/reconnect, full inventory, stand deletion, GUI close,
      permission revocation and two concurrent viewers for item integrity.
- [ ] Marker can be enabled/disabled by explicit targeting after its hitbox
      disappears; No Movement remains independent of gravity and persists via
      PDC after chunk unload/reload and restart.
- [ ] No Ticking applies to a **single** stand, persists through world save,
      chunk reload and server restart, and does not affect neighboring stands.
      Perform poses, equipment sync and copy/paste while ticking is disabled;
      no editor glow applied to no-tick stands, normal brief glow retained on
      ticking stands, and deliberate external glow preserved as far as can be
      distinguished by Bukkit's API.
- [ ] Copy multiple independent slots, pasting all recorded properties and
      equipment (Creative only) including Scale, Invulnerability, locks,
      Marker, No Movement and No Ticking. Repeat with basic-only permissions
      to ensure restricted state is neither copied into the destination nor
      accidentally cleared. Retest restart persistence.
- [ ] **Invisible paste permission:** Using a non-op editor with `asedit.basic`
      but without `asedit.invisible`, copy a visible stand and paste onto an
      invisible stand, then copy an invisible stand and paste onto a visible
      stand. The destination must retain its original visibility in both cases
      while other permitted snapshot properties still apply. Grant
      `asedit.invisible` and repeat both directions; visibility must then paste
      normally. Revoke the permission again without restarting and repeat.
      Verify direct `/ase mode invisible` denial and normal visibility editing
      after regranting permission. Test both Creative and Survival, retaining
      the existing Creative-only equipment-paste rule.
- [ ] Change language from English to another shipped locale and confirm
      English fallback for new strings and icons; no null/missing text.

### D. Release sign-off

- [ ] `./gradlew clean build` and `python3 scripts/verify-jar.py` green
      with JDK 25, including focused unit tests and plugin descriptor checks.
- [ ] GitHub Gradle and CodeQL workflows green on `develop`; download
      `package.zip`, confirm exactly one `armorstandeditor-1.0.0.jar`, inspect
      metadata/resources, Java 25 bytecode and SHA-256 before deployment.
- [ ] Test real Paper restart with the candidate JAR; plugin enable logs clean,
      `/ase` commands and GUI work, and armor stands are unchanged unless
      deliberately edited. Complete all outstanding exceptional cases in the
      earlier Phase 2–6 sections or document explicit release exceptions.
- [ ] Record tester/date/server build/source commit/artifact checksum, roll
      back once using the preserved production JAR, and obtain explicit
      operator acceptance before separately merging or deploying `main`.

## Build and release checks (Phases 1 and 7)

**Phase 1 accepted on `develop` (2026-10-30):** Gradle CI and CodeQL passed.
The official Wrapper JAR is present and its published SHA-256 was verified.
GitHub's `package` artifact downloads as `package.zip`, containing
`armorstandeditor-26.3.jar`. Only a **focused load/basic-edit smoke test** was
performed in game; the complete Phase 0 checklist was not rerun because no
Java gameplay code or source resources changed. The exact test-server Paper
build was not independently recorded. Full regression remains a Phase 7 gate.

- [x] Gradle 9.8.2 Wrapper builds on Java 25 against `26.3.build.+`;
      this CI run selected `26.3.build.231-stable` (not pinned).
- [x] The packaged JAR passes automated descriptor, resource, and Java 25
      bytecode checks; packaged `plugin.yml` reports version `26.3`.
- [x] Gradle CI and CodeQL passed on `develop` (operator confirmed).
- [x] CI triggers are configured for `develop`/`main` pushes and relevant PRs;
      execution on `main`/PRs has not been separately observed.
- [x] The expected single `package` artifact is produced; Dependabot is
      configured for Gradle and Actions (actual update PRs not tested).
- [x] The official Wrapper JAR is present in the repository with the published
      checksum; Wrapper launchers are the project's custom scripts.
- [x] The Gradle-built JAR loaded on a test server and basic armor stand
      editing still worked (operator confirmed; server build not recorded).
- [x] Changelog and modernization plan reflect completed Phase 1 work.
- [ ] Full gameplay regression suite and release-candidate review (Phase 7).

## Performance comparison (Phase 6)

- [x] Captured an optional single-server, 50-stand spark profile with
      ticking enabled, a changeover period, then all stands set to No Ticking.
      Same loaded area and stable ~210–212 total entities; the changeover was
      not separately timestamped, so this is exploratory rather than A/B/A.
- [x] Recorded `OJ1fIRAFxh.sparkprofile` (Paper 26.3; UTC start 2026-11-01
      02:26:14, end 02:33:42; duration ~7m28s). Comparing the three complete
      pre-transition and four post-transition one-minute windows:
      system CPU mean 33.1% → 10.6%, process CPU mean 1.79% → 1.54%,
      averaged per-window median MSPT 2.23 ms → 2.22 ms, TPS ~20 both.
- [x] Documented limits: host-wide system CPU was variable and includes
      unrelated workload; small MSPT difference is not meaningful on its own.
      No performance guarantee or mandatory larger-scale benchmark.
      Optional future A/B/A or call-tree analysis is outside the acceptance gate.

## Test notes

| Date / phase | Build + environment | Result | Details / regression link |
| --- | --- | --- | --- |
| October 2026 / Phase 0 | Paper 26.3, Maven CI artifact 26.3 | PASS (core behavior) | Reported production testing: editing modes, equipment, copy/paste, targeting, permissions, normal highlighting; known limitations remain documented for later phases. |
| October 30, 2026 / Phase 1 | Gradle 9.8.2, Java 25; Paper API `26.3.build.231-stable` resolved | PASS (build and focused smoke test) | GitHub Actions Gradle build, JAR verifier and CodeQL passed; verified Wrapper JAR present. Operator confirmed plugin loads and basic editing works on a test server. Full gameplay suite deferred; test-server build not recorded. |
| October 30, 2026 / Phase 2 preparation | Source and static review on `develop`; Gradle offline attempt failed (no distribution/network; Java 21 runtime) | BLOCKED (local build) | Permission/event fixes prepared for CI and in-game acceptance. |
| October 31, 2026 / Phase 2 acceptance | GitHub Actions Gradle and CodeQL (operator confirmed), Paper 26.3 | PASS (targeted gameplay) | All eight targeted test groups passed, including region protection, permission groups and both hands. Offhand-only flint does nothing by design. CMI-specific non-interactable/invulnerable settings did not prevent ASE edits; not accepted as a protection boundary. |
| October 31, 2026 / GUI command follow-up | GitHub Actions/CodeQL green; live Paper test (operator confirmed) | PASS (command smoke test) | `/ase gui` opens the editor as intended. Granular denial/console/target-block cases are retained for the final regression pass. |
| October 31, 2026 / Phase 3 acceptance | CI and CodeQL green; Paper 26.3 in-game checks (operator reported) | PASS (10 targeted groups) | Scale bounds, directions, copy/paste and modifiers, invulnerability persistence/creative-mode editing, permissions and regions, legacy interactions and fallback language all passed. |
| October 31, 2026 / Phase 4 prepared | Static source review; Gradle unavailable offline (Java 21 installed) | PENDING | Equipment locks, safe live Equipment GUI preview and grouped property layout prepared; CI/CodeQL and live acceptance required. |
| October 31, 2026 / Phase 4 closeout | CI/CodeQL (operator confirmed), Paper 26.3 live | PASS (targeted) | Lock matrix and equipment paste across Creative/Survival passed; no additional regressions observed. Hard-crash atomicity and separately unchecked extraordinary scenarios remain final regression gates. |
| October 31, 2026 / Phase 5 acceptance | Paper 26.3 live testing (operator reported) | PASS (targeted) | Marker recovery and all Phase 5 checks passed with no new regressions; CHAIN icon corrected to EXPOSED_COPPER_CHAIN in supplied baseline. |
| October 31, 2026 / Phase 6 accepted | Paper 26.3 live testing and Gradle CI/CodeQL (operator-reported) | PASS (targeted) | Restricted Marker/No Ticking, per-stand restart persistence, target isolation, glow, copy/paste and legacy behavior passed. 50-stand spark export suggests lower process CPU but nearly unchanged MSPT; not a controlled performance guarantee. Full Phase 7 regression remains outstanding. |

## Final API/Adventure cleanup regression checks (1.0.0 candidate)

- [ ] Main GUI and Equipment GUI icons show their localized custom names and
      description lore on hover; no unwanted potion effects, enchanted-book
      enchantments, attack attributes or other vanilla component details appear.
- [ ] `/ase`, `/ase help`, invalid command feedback and mode permission errors
      show translated colors/formatting consistently in chat and action bar.
- [ ] An anvil-renamed name tag containing `&6`, `&l`, or plain text applies
      the expected stand name; creative and survival consumption remain unchanged.
- [ ] With `requireToolData` enabled, zero-damage and worn damageable items
      match the configured `toolData` correctly; default FLINT still works
      with the setting disabled.
- [ ] With `requireToolLore` enabled, a tool with the exact configured first
      lore line works while missing, empty, differently styled, or mismatched
      lore fails; verify unchanged vanilla flint for non-editors.
- [ ] Localized inventory titles appear correctly; existing lang/*.yml remain
      unchanged and missing custom locale falls back to English.
