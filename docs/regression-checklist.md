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
## Phase 4 — equipment locks and GUI safety (pending CI and live verification)

- [ ] Lock Equipment displays as a Trial Key immediately after Invulnerability,
      followed by Gravity and Baseplate; the GUI icon is visible to basic-only
      editors, but selecting it (or `/ase mode lockequipment`) requires
      `asedit.lockequipment`, including with mixed-case commands.
      `asedit.basic` remains required. Old `disableslots` mode is unavailable.
- [ ] Lock all six slots (helmet, chest, leggings, boots, main hand, offhand):
      normal survival/creative interactions cannot add, remove or swap items,
      even for a builder; toggle off restores normal vanilla manipulation.
- [ ] After a full lock, save/restart the server and confirm it persists.
      An intentionally partially locked stand becomes fully locked on toggle;
      a fully locked stand becomes unlocked on the next toggle.
- [ ] Copy/paste lock state between stands and across copy slots 1–9, including
      per-slot partial lock states and unrelated `ADDING` locks. A player with
      basic-only permission must NOT change locks by pasting a locked snapshot.
- [ ] **Phase 4 copy/paste follow-up:** in Creative mode copy a fully equipped
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
- [ ] With equipment locking ON, open Equipment GUI using left/right flint.
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

## Later-feature gates (not begun)

- [ ] No Movement: toggle controls ordinary movement, not direct authorized
      editing; verify save/reload and any PDC fallback.
- [ ] Marker: stand can still be selected and Marker reversed without relying
      on the ordinary entity hitbox; selection works for overlapping stands.
- [ ] Ticking: new toggle is opt-in; default/previous state isn't changed on
      plugin load; explicit change survives save/restart.
- [ ] Ticking: pose/equipment updates reach the client with ticking disabled.
- [ ] Ticking: effects, fire, physics, damage and interaction consequences are
      understood and documented; compare against a ticking control stand.
- [ ] Highlight: normal ticking stands retain short glow; no new highlight is
      applied to non-ticking stands; disabling ticking leaves no stuck
      **plugin-owned** glow and does not clear an unrelated deliberate glow.
- [ ] With Marker + no-tick + small/scale and Invulnerability in combination,
      editors can still select, edit and restore all toggles.

## Phase 7 — Native Brigadier commands and tab completion (planned)

- [ ] `/ase` is registered exactly once using Paper's lifecycle command API;
      `plugin.yml` remains the plugin descriptor, without a duplicate legacy
      `/ase` command declaration.
- [ ] Verify suggestions and execution for `/ase gui`, `help`, `?`, `mode`,
      `axis`, `adj` and `slot` with partial/mixed-case input where supported.
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

- [ ] Capture similar server load and positions for ticking/on-off tests.
- [ ] Measure MSPT and spark traces for comparable loaded armor stand counts;
      record sample time, server build, configuration and test conditions.
- [ ] Report measured differences rather than claiming generic performance
      improvement; compare rendering/tracking cost where possible.

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
