# Changelog

Notable changes to the BadWolfMC ArmorStandEditor fork are recorded here.
Keep entries concise and limited to work that has actually been completed.

## Unreleased

### Phase 2 — Flint interactions and authorization (pending live acceptance)
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

**Verification:** Source diff and static invariants reviewed; build and
in-game acceptance are pending GitHub Actions and BadWolfMC test-server runs.


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
