# ArmorStandEditor modernization plan (temporary)

> **Working document — not release documentation.** This records the decisions and
> implementation order agreed before development. Update it as phases are completed;
> remove this plan and the companion regression checklist after the modernization
> project is finished. Keep the concise, permanent history in `changelog.md`.

**Status:** Phase 0 complete (production baseline confirmed); Phase 1 migration
prepared on `develop`, pending GitHub Actions and in-game acceptance. The
standard Wrapper JAR must still be committed after fetching its checksum-verified
published binary; see `README.md`. Phases 2–7 not started.
**Baseline:** BadWolfMC fork of RypoFalem/ArmorStandEditor; source archive reviewed
2026-10-30; baseline `develop` commit `09da6ec` (also `main` in the supplied
archive). Phase 0 Maven CI produced `armorstandeditor-26.3.jar` as an artifact,
which was deployed successfully on Paper 26.3. Java 25 bytecode verified.
Local Maven build was not run. The regression checklist covers the verified
existing features. The current plugin is working in production; there are **no known
runtime failures requiring a rewrite**.
**Development branch:** `develop` (retain `main` as the known-good reference).
**Target:** Paper 26.3 and Java 25.

## Guiding constraints

- **Preserve existing behavior first.** Change one subsystem at a time, validate,
  and keep changes reviewable; do not rewrite for style alone.
- Do not rename the plugin, Java packages, commands, or configuration keys without
  a specific compatibility reason. Keep `plugin.yml`, not `paper-plugin.yml`.
- Adopt **Gradle Kotlin DSL** and the Paper API dependency
  `compileOnly("io.papermc.paper:paper-api:26.3.build.+")`. Do **not** pin a specific
  Paper build; use Java 25 toolchains and the Gradle Wrapper.
- No NMS or packet implementation unless a genuinely unavoidable requirement is
  identified. Do not add a database, automatic world migration, or auto-optimization.
- A change to editing permissions must **never** make ordinary flint behavior worse
  for players without editor permission.
- Use the current repository as evidence, not the `api-version` declaration as
  proof that APIs and behavior have been modernized.
- The requested changes are for this private fork; avoid creating elaborate
  backwards-compatibility layers or public-facing configuration solely for them.

## Agreed features and rules

| Feature | Intended behavior / implementation guidance |
| --- | --- |
| **Size** | Preserve the existing small/normal toggle (`setSmall`) independently of Scale. |
| **Scale** | New, independently adjustable `Attribute.SCALE`; intended bounds **0.1–15.0**, default/base **1.0**. Proposed fine/coarse steps are **0.1/0.5**. Clamp and round cleanly, preserve other attribute modifiers, test tiny/large stands, and include in copy/paste. |
| **Invulnerability** | New toggle through `setInvulnerable`; read with `isMarkedInvulnerable`, not the deprecated `isInvulnerable`. Test creative-mode behavior and attack-event accessibility. |
| **Lock Equipment** | Replace the unimplemented `EditMode.DISABLESLOTS` scaffolding with a user-facing **Lock Equipment** mode. Apply `ADDING_OR_CHANGING` and `REMOVING_OR_CHANGING` locks to all six equipment slots. Block *vanilla* equipment manipulation for everyone, including authorized builders, but allow intentional changes through the authorized Equipment GUI. No partial-lock editor needed. |
| **No Movement** | Separate toggle using `setCanMove`; independent of Gravity and Ticking. Test whether state survives restarts and, if not, persist a minimal per-stand override in its PDC and restore safely on entity load. |
| **Marker** | Separate toggle using `setMarker`, not a substitute for Lock Equipment. Improve selection first so hitbox-less stands can still be found and Marker can be turned off again. |
| **Ticking** | Explicit, per-stand `setCanTick` toggle. Never silently disable ticking, including on existing stands. Preserve the current state by default. Test direct editing, equipment sync, persistence, physics/effects, and measurable performance benefit. |

**Selection glow exception:** Keep the existing short (15-tick) Glowing effect
for ticking stands; **do not apply editor highlighting to non-ticking stands**.
When ticking is switched off, remove *only* a temporary glow that this plugin
can safely identify as its own. Do not remove deliberate Glowing effects from
another plugin, command, or player. Verify the implementation's ownership
strategy, because the current `PlayerEditor.highlight()` removes/reapplies the
Glowing effect unconditionally. If safe ownership cannot be proven, resolve
that behavior before shipping the ticking toggle. No particles or packet-based
highlight system is planned. Highlighting must remain cosmetic: non-ticking
and Marker stands must still be editable.

## Flint and protection contract

Permission and event handling is an **early, distinct work phase**, not incidental
cleanup. Desired behavior:

- A player **without** editor-use permissions holding ordinary flint experiences
  entirely vanilla interactions (blocks, entities, scrolling, hand swapping).
- An authorized editor's flint **must not** consume chest, door, button, or other
  block interactions; attacking non-armor-stand entities behaves normally.
- Authorized armor stand edits through the tool continue to work. Clicking air
  may open the editor menu, as in the current experience, without stealing a
  legitimate block interaction.
- Sneak-scroll and hand-swap shortcuts require correct authorization. Consider
  both hands and canceled events so one action is not processed twice.
- Keep region/protection-plugin denial effective. Review the current synthetic
  interaction-event approach and shared `ignoreNextInteract` flag carefully;
  any replacement must be tested against protection plugins and cross-player
  event interactions.
- New properties (particularly Invulnerability and Marker) must not make the
  stand uneditable by someone with appropriate permissions.

## Phased implementation and acceptance gates

### Phase 0 — Baseline and regression inventory

1. Inspect repository status, active branch, existing source/build setup and
   workflows. Document the baseline commit and Paper/JDK environment.
2. Build the **unchanged** Maven project, if the necessary dependencies and
   toolchain are available. Record commands and outcome; inspect the resulting
   JAR, including resolution of `plugin.yml`'s `${project.version}` token.
3. Record existing GUI modes, commands, permissions, event priorities,
   cancellation behavior, copy/paste, equipment handling, and reload/restart
   behavior; compare intended behavior with current observations.
4. Run or prepare the manual baseline checks in `docs/regression-checklist.md`.
   Flag existing bugs separately from regressions so the next phases can be
   evaluated fairly.
5. Make **no functional edits** in Phase 0. Capture any discrepancies that
   Phase 1 or Phase 2 needs to address.

**Exit:** Baseline commit, build outcome, inventory, and a practical regression
checklist are documented. Existing production JAR remains available as fallback.

### Phase 1 — Gradle and GitHub modernization

- Migrate from Maven to Gradle Kotlin DSL and Gradle Wrapper; use a Java 25
  toolchain and `26.3.build.+` Paper API dependency, without pinning a build.
- Retain `plugin.yml` and make version substitution/output artifact names
  correct and verifiable. Do not change runtime behavior.
- Update CI on `develop` and `main` (including pull requests), Dependabot,
  CodeQL configuration as appropriate, and artifact publishing. Avoid duplicate
  or misleading artifact paths.
- Build, inspect the packaged JAR, and rerun baseline smoke checks.

**Exit (not yet signed off):** Gradle CI build/release artifact works and
matches intended Maven functionality; download, validate and commit the official
Wrapper JAR to `develop`; confirm the Gradle build in Actions and run the Paper
26.3 smoke tests. No intentional gameplay changes.

### Phase 2 — Flint interactions and authorization

- Fix event gates so unauthorized flint use is completely vanilla, and editor
  flint does not intercept unrelated block/entity interactions.
- Review per-event hand use, priorities, cancellation, permission nodes, and
  the synthetic-event/protection approach without bypassing protection plugins.
- Regression-test authorized and unauthorized players, inventory access,
  shortcuts, creative/survival, and overlapping player events.

**Exit:** Editing works; chest/door/button and unrelated entity interactions
remain normal; unauthorized users experience no editor interference.

### Phase 3 — Scale and Invulnerability

- Add new GUI modes, controls, messages/translation fallbacks, permission checks,
  and copy/paste properties. Preserve existing Size behavior.
- Test adjustment bounds and increments on small and normal stands.
- Ensure invulnerable stands remain editable (including left-click editing).

**Exit:** Both properties persist and edit correctly without harming other modes.

### Phase 4 — Equipment locks and Equipment GUI safety

- Remove unfinished `DISABLESLOTS` scaffolding and implement **Lock Equipment**.
- Apply both lock types to all six equipment slots. Preserve intentional
  editing through the authorized GUI, not vanilla armor-stand manipulation.
- Replace the current equipment-menu approach that temporarily clears the
  stand's gear with a safe snapshot/commit workflow. Handle inventory close,
  disconnect, entity removal and shutdown without loss or duplication.
- Include equipment-lock state in copy/paste and regression tests.

**Exit:** No accidental vanilla changes while locked; authorized GUI editing
works; equipment remains safe across interrupted sessions.

### Phase 5 — Targeting, Marker and No Movement

- Make targeting robust for tiny, scaled, invisible, overlapping, and Marker
  stands. Provide a reliable way to reverse Marker via selection/GUI.
- Add Marker and No Movement toggles; verify persistence and protection rules.
- Keep the normal editor highlight where it remains applicable.

**Exit:** All affected stands can be selected, changed, and recovered safely.

### Phase 6 — No-tick control and measurements

- Add the opt-in Ticking toggle with explicit state feedback.
- Skip Glowing highlights on non-ticking stands, with ownership-safe cleanup
  for any plugin-owned temporary highlight when the state changes.
- Test equipment/pose sync, persistence across world saves/restarts, gravity,
  fire/effects, damage, Marker interactions, and the editing tool.
- Benchmark comparable loaded stand counts (e.g. 250/500/1000 where safe)
  with spark/MSPT data. Do not promise a speedup without measurement.

**Exit:** Ticking can be toggled safely; no permanent highlight; behavioral
tradeoffs are documented and performance claims supported by measurements.

### Phase 7 — Targeted cleanup and release

- Address narrowly scoped deprecated API usage, translation fallbacks,
  permission metadata, tests and docs. Preserve existing public interfaces.
- Run the complete regression checklist, review the changelog, and prepare a
  release candidate from `develop`; do not assume release without testing.

**Exit:** Verified release artifact and concise changelog entry; archive or
remove these temporary planning/checklist files when their work is complete.

## Open verification points (not settled implementation facts)

- **Phase 0 resolved:** Original Maven `plugin.yml` included the unresolved
  `${project.version}` token; the legacy CI `dist` upload used a nonexistent
  `target/armorstandeditor.jar` path. Phase 1 uses targeted Gradle resource
  expansion and one artifact upload; CI verification remains pending.
- Which permission nodes are relied upon by BadWolfMC permission groups,
  especially `asedit.use` versus `asedit.basic`? Avoid quietly changing grants.
- What is the safest way to recognize editor-owned Glowing without removing
  other sources' effects? Verify under ticking transitions.
- Is `setCanMove` persisted by the target server version, or is a PDC restore
  necessary? Check behavior, not only API signatures.
- How are locked or invulnerable stands affected by entity interaction event
  delivery? Verify against Paper and any installed region protection plugins.

## Document lifecycle

Keep this plan and `docs/regression-checklist.md` during the development cycle.
Update completed phase statuses as work is verified. Do **not** copy phase
checklists wholesale into `changelog.md`: that file should describe only
completed, user/developer-relevant changes. After Phase 7, remove the temporary
planning documents in a separate housekeeping commit; keep `changelog.md`.
