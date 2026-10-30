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

## Interaction and permissions (repeat after Phase 2)

- [ ] No editor permission + flint: ordinary chest/door/button interaction.
- [ ] No editor permission + flint: ordinary block and entity interactions;
      no unsolicited GUI, cancellation, messages, or sound.
- [ ] No editor permission + flint: ordinary sneak-scroll and hand swap.
- [ ] Authorized editor + flint: chest, door, button and other interactive
      blocks still work; opening the chest doesn't open the editor instead.
- [ ] Authorized editor + flint: attacks on unrelated entities stay vanilla.
- [ ] Authorized editor + flint: air menu and selected stand editing still work.
- [ ] Authorized editor + flint: left/right editing, axis change and target
      cycling have expected direction and outcome.
- [ ] Both hands: offhand events do not trigger duplicate or unexpected edits.
- [ ] Protected region: editing is refused where region protection forbids it;
      normal unprotected editing is allowed.
- [ ] Multi-player simultaneous use: no shared event guard or selected-target
      state leaks between players.
- [ ] Creative and survival behaviors match intended editor permissions.

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
