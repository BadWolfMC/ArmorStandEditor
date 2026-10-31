# ArmorStandEditor modernization plan (temporary)

> **Working document — not release documentation.** This records the decisions and
> implementation order agreed before development. Update it as phases are completed;
> remove this plan and the companion regression checklist after the modernization
> project is finished. Keep the concise, permanent history in `changelog.md`.

**Status:** Phase 0 complete; **Phase 1 complete on `develop` (2026-10-30)**;
**Phase 2 accepted on `develop` (2026-10-31)**, including `/ase gui`;
**Phase 3 accepted on `develop` (2026-10-31)** after green GitHub CI/CodeQL
and all ten live regression groups passed (BadWolfMC administration).
**Phase 4 accepted on `develop` (2026-10-31):** operator confirmed green
CI and creative/survival locking and copy/paste matrix, with no further
regressions observed. **Phase 5 is implemented locally, awaiting CI and live
Paper verification.** Phases 6–7 have not started; `main` remains stable.
Gradle CI and CodeQL passed for Phase 1; the checksum-verified Gradle Wrapper
JAR is present in the repository; the Gradle-built plugin loaded and basic
editing worked on a test server. The complete release regression suite is a
Phase 7 acceptance gate.
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
- Build and inspect the packaged JAR, then perform a focused runtime load/edit
  smoke check. Defer the full regression checklist until behavior-changing
  phases and the final release gate.

**Exit (accepted 2026-10-30 on `develop`):** GitHub Actions successfully built
`armorstandeditor-26.3.jar` using Gradle 9.8.2 and Java 25. The dynamic
`26.3.build.+` Paper dependency resolved to `26.3.build.231-stable` in this
build (recorded for troubleshooting, **not** pinned). The automated JAR verifier
passed for Java 25 bytecode, packaged resources, and `plugin.yml` version `26.3`.
The `package` artifact retained the downloadable `package.zip` convention.
CodeQL passed (operator confirmed). The official Gradle Wrapper JAR was
checksum-verified and added to the repository. The test-server plugin loaded
and basic editing worked (operator confirmed; precise Paper build not separately
recorded). No Java gameplay or resource source files were changed; this is a
build/packaging equivalence check, **not** a claim that every gameplay scenario
was retested. `main` is not marked merged by this closeout.

### Phase 2 — Flint interactions and authorization

- Fix event gates so unauthorized flint use is completely vanilla, and editor
  flint does not intercept unrelated block/entity interactions.
- Review per-event hand use, priorities, cancellation, permission nodes, and
  the synthetic-event/protection approach without bypassing protection plugins.
- Regression-test authorized and unauthorized players, inventory access,
  shortcuts, creative/survival, and overlapping player events.

**Implementation (2026-10-30, live-accepted 2026-10-31):** All tool interception
now uses the already-declared `asedit.basic` gate, consistent with `/ase`, the
GUI, and `PlayerEditor`'s edit methods. The formerly undeclared `asedit.use`
was only checked on air-menu opening; removing that inconsistent check does
**not** grant editing to `asedit.use`-only players. Players granted only
`asedit.basic` were already able to edit stands and use `/ase`; they now also
get the air-menu shortcut. Validate actual BadWolfMC permission-group grants
before deployment. `asedit.invisible` and `asedit.rename` remain additional
checks; `asedit.disableslots` selection is permission-gated, but the mode
remains unimplemented until Phase 4. No permissions were added to `plugin.yml`.

Real entity events are handled at `HIGHEST` with `ignoreCancelled=true` so
existing cancellations are respected; only authorized main-hand flint against
an armor stand is consumed. Only main-hand air clicks can open the menu, and
no block interaction is cancelled. Air interactions have a vanilla-predicted
block-use cancellation state, so the handler tests `useItemInHand()` for an
explicit item-use denial instead. Both synthetic protection events remain,
using an identity-scoped guard rather than the shared boolean. A previously
selected stand is rechecked by the protection hooks before editing; if its
protection now denies access, that edit is refused. The synthetic
`PlayerInteractAtEntityEvent` uses an entity-relative zero click vector
instead of the old world position. Verify behavior with BadWolfMC's actual
protection plugins; synthetic events are not a universal authorization API.

**Exit (accepted by BadWolfMC administration, 2026-10-31):** Gradle CI
and CodeQL passed; authorized and unauthorized gameplay, both hands, target
selection, unrelated blocks/entities and protection-plugin scenarios passed
targeted live tests. Offhand-only flint intentionally does not activate the
editor. CMI armor-stand invulnerability/non-interactable settings did not
block ASE editing in this environment; do not treat them as a region edit
prohibition or infer compatibility with every protection plugin.

**Confined-space GUI follow-up (accepted 2026-10-31):** `/ase gui` opens
the existing menu with `asedit.basic`, without needing a flint click or
changing block events. Retain air-only click access, because the deprecated
Material interactability categorization is incomplete. No extra permission
node or GUI layout change. BadWolfMC administration confirmed green CI/CodeQL
and successful in-game use. Individual command-denial and edge-case tests
not separately reported remain in the regression checklist for later rechecks.

### Phase 3 — Scale and Invulnerability

- Add new GUI modes, controls, messages/translation fallbacks, permission checks,
  and copy/paste properties. Preserve existing Size behavior.
- Test adjustment bounds and increments on small and normal stands.
- Ensure invulnerable stands remain editable (including left-click editing).

**Implementation (2026-10-31; accepted after live testing):** Added
`SCALE` and `INVULNERABLE` modes to the existing GUI and command enum; scale
changes the SCALE attribute *base value* by exactly 1 or 5 tenths, with bounds
0.1–15.0. Existing attribute modifiers remain attached and can influence the
effective final scale. Size remains independent. Copy/paste saves the scale base
value and marked invulnerability; paste preserves the destination's modifier set
and clamps the copied base into the supported range.

Invulnerable stands receive a narrowly scoped `PrePlayerAttackEntityEvent`
handler because their attacks may not generate `EntityDamageByEntityEvent`.
The pre-attack handler ignores cancelled or non-attackable attacks, requires
master permission and main-hand tool, checks the Phase 2 synthetic interaction
protection gates for clicked/selected stands, and cancels the attack to avoid
repeated edits in creative mode. Non-invulnerable stands retain the Phase 2
damage handler. **Integration caveat:** plugins that enforce protection *only*
by cancelling damage events (and not the pre-attack or synthetic interaction
events) cannot necessarily prevent an early invulnerable-stand edit; verify with
the production protection plugins. No plugin can promise generalized damage-
only protection compatibility without an integration strategy.

**Exit (accepted 2026-10-31 on `develop`):** GitHub Actions and CodeQL
remained green (operator confirmed); all ten live test groups passed, covering
Scale bounds/directions, Size independence, copy/paste and modifiers,
invulnerability in survival/creative and across restarts, protection regions,
permissions, legacy edits, flint interactions, GUI commands and language
fallback. The production-specific damage-only protection caveat remains noted;
these tests do not establish universal third-party plugin compatibility.

### Phase 4 — Equipment locks and Equipment GUI safety

- Remove unfinished `DISABLESLOTS` scaffolding and implement **Lock Equipment**.
- Apply both lock types to all six equipment slots. Preserve intentional
  editing through the authorized GUI, not vanilla armor-stand manipulation.
- Replace the current equipment-menu approach that temporarily clears the
  stand's gear with a safe snapshot/commit workflow. Handle inventory close,
  disconnect, entity removal and shutdown without loss or duplication.
- Include equipment-lock state in copy/paste and regression tests.

**Implementation prepared locally (2026-10-31; acceptance pending):**
Removed the nonfunctional `DISABLESLOTS` mode and old undeclared
`asedit.disableslots` selection gate. New `LOCKEQUIPMENT` mode uses
`asedit.lockequipment` (explicit `plugin.yml` permission; default `op`) and
both `ADDING_OR_CHANGING` and `REMOVING_OR_CHANGING` lock types on all six
slots. An incomplete lock set toggles to fully locked; fully locked toggles
to unlocked. Snapshot/paste preserves each managed lock independently, with
`ADDING` locks untouched. Pasting lock state requires the new permission,
so users with only `asedit.basic` cannot toggle locks indirectly.

The Equipment GUI no longer calls `equipment.clear()` or retains equipment
in a temporary inventory until close. It displays cloned **previews**, cancels
vanilla top-inventory movement, and commits an intentional left/right click
as an immediate, explicit *full-stack cursor swap* with the real stand slot.
Icon slots, shift-transfer, double-click collection, hotbar/offhand exchanges,
creative cloning and drags into the top inventory cannot move the previews.
Players can pick up/put down the cursor item in their lower inventory normally.
The manager binds each view to its player, rechecks basic permission, stand
validity, proximity and synthetic interaction-protection hooks on each swap,
and refreshes previews for concurrent editors. Close, quit, stand removal and
plugin disable require **no pending equipment commit**. Do not represent this
as transactional across a physical server crash between player/entity disk
saves; no extra persistence layer was added. Observe Paper's cursor behavior
with a full inventory on quit/restart during live testing.

**GUI follow-up:** Property toggles are grouped in the right-hand columns;
Size and Scale are adjacent, and Invulnerability sits beside Lock Equipment
(Trial Key), followed by Gravity and Baseplate. All property icons remain
visible to editors; the existing `asedit.lockequipment` command permission
still gates use of the lock mode. Future property slots remain reserved,
without implementing later-phase features. English lore is kept to short,
practical lines and existing locale fallback is retained.

**Exit (accepted 2026-10-31):** After the compilation and GUI icon fixes,
CI remained green; the operator confirmed the lock and copy/paste matrix in
Creative and Survival, normal authorized Equipment GUI editing and no other
regressions. The later copy/paste fix preserves Creative-only item duplication
safety; the original suspected regression was due to testing in Survival.
Extraordinary hard-crash atomicity is not claimed.

### Phase 5 — Targeting, Marker and No Movement

- Make targeting robust for tiny, scaled, invisible, overlapping, and Marker
  stands. Provide a reliable way to reverse Marker via selection/GUI.
- Add Marker and No Movement toggles; verify persistence and protection rules.
- Keep the normal editor highlight where it remains applicable.

**Phase 5 implementation (local, pending CI and live acceptance):** The
existing sneak-free hand-swap selection and new main-GUI **Select Target**
action cycle armor stands intersecting a 10-block sight ray. Sight checks
include expanded non-marker bounding boxes and a deliberately approximate
visual volume for zero-hitbox Marker stands; solid blocks block selection.
The GUI includes **Apply to Target** and **Reverse on Target** controls, so
editors can use a selected stand's current editing mode without a vanilla
entity click. Actions require `asedit.basic`, a valid stand still in view,
and fresh protection-plugin checks. These are GUI-only actions, not new `/ase`
commands. Marker's approximate selection radius is intentionally generous,
so confirm overlapping, behind-wall and nearby-stand cases on Paper.

Marker (`setMarker`/`isMarker`) and No Movement (`setCanMove`/`canMove`) are
new independent modes; both are included in copy/paste. An explicitly frozen
stand records a single `no_movement` BYTE in its own PDC, restored when chunk
entities load and for chunks already loaded on plugin enable. Re-enabling
movement removes that PDC marker; untouched armor stands remain unaffected.
Gravity, Ticking, Marker and ordinary authorized teleport-based placement
remain independent. Marker does not introduce a special tick/highlight system.
All new labels use existing English fallback. Phase 6's Ticking toggle and
Phase 7's native Brigadier registration remain untouched.

**Exit (pending):** All affected stands can be selected, changed, and recovered
safely, including after saves/restarts and through protection plugins; verify
CI/CodeQL, creative and survival behavior and no regression to Phases 2–4.

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

### Phase 7 — Command modernization, targeted cleanup and release

- **Migrate `/ase` to Paper's native Brigadier command API** using the plugin
  lifecycle `LifecycleEvents.COMMANDS` registrar. `plugin.yml` remains the
  plugin descriptor; no switch to `paper-plugin.yml` is needed. Replace the
  existing Bukkit command executor and `plugin.yml` command declaration in a
  controlled migration, without registering `/ase` twice.
- Provide client-side tab suggestions and argument validation for `gui`,
  `help`, `?`, `mode`, `axis`, `adj` and `slot`, including enum values and slots
  1–9. Preserve existing command spellings, case-insensitive inputs, actual
  effects, and user-facing help/feedback where applicable. Do not suggest
  unfinished/unauthorized modes, including `DISABLESLOTS` prior to its Phase 4
  replacement. Newly introduced mode names from Phases 3–6 must be included.
- Apply the existing `asedit.basic` master gate and mode-specific permissions
  consistently to execution and suggestions. Verify console/non-player
  behavior and denied subcommands; visibility must not replace server-side
  authorization. Add no permissions or command aliases unless justified.
- Check startup, server reload/command re-registration, command help and
  possible `/ase` name collisions. Retain the current `plugin.yml` plugin
  identity, Java packages, Gradle Kotlin DSL, Java 25 and Paper `26.3.build.+`.
- Address other narrowly scoped deprecated API usage, translation fallbacks,
  permission metadata, tests and docs. Preserve existing public interfaces.
- Run the complete regression checklist, review the changelog, and prepare a
  release candidate from `develop`; do not assume release without testing.

**Exit:** Native Brigadier registration and permission-aware completions are
verified on Paper 26.3; release artifact and concise changelog entry pass all
acceptance checks. Archive or remove the temporary planning/checklist files
when their work is complete.

## Open verification points (not settled implementation facts)

- **Phases 0–1 resolved:** Original Maven `plugin.yml` included an unresolved
  `${project.version}` token; the legacy CI `dist` upload used a nonexistent
  `target/armorstandeditor.jar` path. Phase 1 uses targeted Gradle resource
  expansion and one verified artifact upload; Gradle and CodeQL CI passed.
- **Deferred to Phase 7 (explicit commitment):** Native Brigadier command
  registration and tab completion for the entire `/ase` command tree. Do not
  introduce a legacy `TabCompleter` as a temporary substitute during Phase 3.
  Paper supports lifecycle registration from a `JavaPlugin` with `plugin.yml`.
- **Phase 2 implementation decision:** `asedit.basic` is the unified master
  gate; the one undeclared `asedit.use` check was removed from air-menu access.
  Existing BadWolfMC permission-group assignments must still be reviewed in
  live testing, particularly non-op `asedit.basic` users. No new edit grants
  are intended. `asedit.rename` still requires the master gate and the
  mode-specific node. `asedit.disableslots` remains deferred to Phase 4.
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
