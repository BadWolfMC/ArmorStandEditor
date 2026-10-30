# ArmorStandEditor

Bukkit/Paper plugin to allow players to edit armor stands without commands.

Read the [wiki](https://github.com/RypoFalem/ArmorStandEditor/wiki) for more info.

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

**Wrapper bootstrap:** The project archive does not include the official
`gradle-wrapper.jar` binary because it could not be downloaded in the migration
environment. The `gradlew` launchers fetch the exact Gradle 9.8.2 Wrapper JAR
from Gradle's published GitHub tag the first time they run and verify it against
Gradle's published SHA-256 checksum. GitHub Actions likewise downloads and
verifies the official file *before* the Gradle setup action validates it.
Once obtained, **commit** `gradle/wrapper/gradle-wrapper.jar` to `develop`
for the standard self-contained Wrapper layout; no local Gradle install is
necessary. The launchers can still use the committed file on later runs.

The distributed Gradle ZIP is separately checksum-verified through
`gradle/wrapper/gradle-wrapper.properties`. Java sources and plugin features
have not been changed as part of the build migration.

## Phase 1 acceptance before merging `develop` into `main`

1. Keep `main` unchanged while preparing a PR from `develop`.
2. Trigger the **Java CI with Gradle** and **CodeQL** workflows on `develop`
   or the pull request. Both must pass with JDK 25. The CI log should report
   the resolved Paper API build and show the `verify-jar.py` success message.
3. Download the `package` artifact from the Gradle workflow; extract
   `package.zip` and confirm it contains `armorstandeditor-26.3.jar`.
   Do not deploy `package.zip` itself to Paper.
4. If the official Wrapper JAR was not yet committed, run `gradlew.bat --version`
   (Windows) or `./gradlew --version` (Unix), then commit the verified
   `gradle/wrapper/gradle-wrapper.jar` to `develop`. Repeat the CI checks.
5. On a disposable Paper 26.3 server, install the Gradle-built JAR and perform
   the existing operations in `docs/regression-checklist.md`: menu/flint,
   pose/placement and direction, toggles, equipment, copy/paste, targeting,
   permissions and normal highlight. Compare against the production Maven
   baseline; record any pre-existing issues without fixing them in Phase 1.
6. Merge only after build, CodeQL, descriptor/JAR checks and in-game smoke
   tests pass. Preserve the last known-good Maven JAR for rollback.

For a local descriptor/resource inspection on a machine with Java 25, run
`python scripts/verify-jar.py` after building. The check uses only Python's
standard library.
