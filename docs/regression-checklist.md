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

## Phase 2 — Flint interactions and authorization (accepted; command follow-up pending)

**Live status (2026-10-31):** BadWolfMC administration confirmed green Gradle
CI and CodeQL, and that all eight targeted Phase 2 test groups passed on the
server. Earlier local compilation remained unavailable (Java 21 only, no
cached Gradle distribution/network). Exact Paper build and protection-plugin
versions were not supplied. `/ase gui` is a subsequent convenience change
and has not been built or tested in game yet.

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
nodes, defaults or inheritance were introduced. `asedit.disableslots` is
currently only a nonfunctional placeholder, left for Phase 4.

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

### Phase 2 confined-space GUI follow-up — pending verification

- [ ] GitHub Actions Gradle build, package verifier and CodeQL pass with
      the `/ase gui` command addition.
- [ ] Editor with `asedit.basic` opens the same GUI using `/ase gui` while
      looking at stone, a chest, and a door, and with no flint held.
- [ ] User lacking `asedit.basic` cannot use `/ase gui`; console use is denied.
- [ ] Air-click GUI remains unchanged; flint on blocks remains vanilla,
      including all interactable blocks and attacks against unrelated entities.
- [ ] Offhand-only flint still has no editing effect, while the hand-swap
      targeting shortcut still functions.

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

## New-feature gates (initially NOT YET IMPLEMENTED)

- [ ] Scale: 0.1 minimum, 15.0 maximum, 1.0 base; fine/coarse changes; bounds
      don't drift; normal and small stands stay independent.
- [ ] Scale: copying/pasting and reload retain values; target very small and
      large stands successfully.
- [ ] Invulnerability: on/off, persistence, creative behavior and authorized
      left/right editing work without unintended entity damage.
- [ ] Lock Equipment: normal interaction cannot add/remove/swap any of six
      equipment slots, even for a builder; authorized GUI still can.
- [ ] Lock Equipment: unlocked state restores normal vanilla manipulation;
      copy/paste retains state.
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
| October 31, 2026 / GUI command follow-up | Source-only change; CI/server not yet rerun | PENDING | New `/ase gui` access for confined spaces; existing flint click events unchanged. |
