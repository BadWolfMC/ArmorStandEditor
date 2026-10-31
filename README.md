# ArmorStandEditor

Bukkit/Paper plugin to allow players to edit armor stands without commands.

Read the [wiki](https://github.com/RypoFalem/ArmorStandEditor/wiki) for more info.

## Opening the editor in confined spaces

Players with `asedit.basic` can run `/ase gui` to open the normal editor menu
without having to find a clear line of sight or hold the flint tool. Clicking
air with flint still opens the same menu; block clicks remain vanilla. This
command is an accepted Phase 2 follow-up: GitHub CI and CodeQL remained
green, and BadWolfMC administration confirmed it works in game (2026-10-31).
The remaining command tree will be moved to native Paper Brigadier with tab
completion in Phase 7; see `docs/modernization-plan.md`.

## Equipment locking (Phase 4 candidate)

The **Lock Equipment** editor mode uses `asedit.lockequipment` (default `op`)
in addition to `asedit.basic`. It blocks normal armor stand equipment changes
across all six slots; authorized builders can still edit locked stands through
the Equipment GUI. It is a native armor stand lock, not a separate item database.

The Equipment GUI previews live equipment; left or right click the lower
helmet/chest/legs/boots/hand slot to **swap its full stack with the cursor**.
Pick up equipment or put items on your cursor in your own inventory first.
No items are unequipped merely by opening the GUI. Shift-click transfers and
drags into preview slots are intentionally disabled to avoid duplicate gear.
The Phase 4 implementation still requires CI and live regression testing.

## Building this BadWolfMC fork

The plugin targets Paper 26.3 and Java 25. Build with the Gradle Wrapper;
no separate Maven or Gradle installation is needed. The build uses Paper's
`26.3.build.+` API dependency (the newest matching build at resolution time).

On Windows (PowerShell):

```powershell
.\gradlew.bat clean build
python scripts/verify-jar.py
```

On Linux/macOS:

```sh
./gradlew clean build
python3 scripts/verify-jar.py
```

The resulting server plugin is `build/libs/armorstandeditor-26.3.jar`.
GitHub Actions uploads this as the `package` artifact (downloaded as
`package.zip`). The ZIP contains the plugin JAR, not a second nested ZIP.

**Gradle Wrapper:** The official Gradle 9.8.2 `gradle-wrapper.jar` is now in
`gradle/wrapper/` and has been checked against Gradle's published SHA-256.
This fork's `gradlew` and `gradlew.bat` are compact custom launchers (not the
standard Gradle-generated scripts); they verify the JAR checksum before running
it and include a download fallback if the JAR is missing. The Gradle
**distribution** ZIP is separately checked using
`gradle/wrapper/gradle-wrapper.properties`. No global Gradle or Maven install
is required. Java 25 is required to perform the build; GitHub Actions supplies
that JDK.

## Phase 1 closeout (verified on `develop`, 2026-10-30)

- **Java CI with Gradle:** Passed using Gradle 9.8.2 and Java 25. The dynamic
  `26.3.build.+` dependency resolved to `26.3.build.231-stable` in this run;
  individual Paper builds remain unpinned.
- **Artifact integrity:** `scripts/verify-jar.py` passed for version `26.3`,
  unchanged packaged resources, and Java 25 bytecode. The `package` GitHub
  artifact downloads as `package.zip`, containing `armorstandeditor-26.3.jar`.
  Install the JAR, **not** the downloaded ZIP, on a server.
- **CodeQL:** Passed (reported by BadWolfMC administration).
- **Runtime smoke test:** The Gradle-built plugin loaded and basic armor stand
  editing worked on a test server (reported by BadWolfMC administration).
  The exact Paper server build was not separately recorded. The full gameplay
  regression suite was intentionally deferred because Phase 1 did not change
  Java gameplay code or source resources.

The migration is accepted on `develop`. Preserve the last known-good Maven JAR
for rollback until the normal merge/deployment process is complete; merging to
`main` is a separate repository action. Re-run affected cases during Phases 2–6
and the full `docs/regression-checklist.md` before the final release (Phase 7).

For a local descriptor/resource inspection on a machine with Java 25, run
`python scripts/verify-jar.py` after building. The check uses only Python's
standard library.
