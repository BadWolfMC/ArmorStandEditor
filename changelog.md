# Changelog

Notable changes to the BadWolfMC ArmorStandEditor fork are recorded here.
Keep entries concise and limited to work that has actually been completed.

## Unreleased

### Maintenance
- Prepared the Maven-to-Gradle Kotlin DSL migration for Java 25 and Paper
  `26.3.build.+`, preserving version `26.3` and all plugin source/resources.
- Added targeted `plugin.yml` version expansion, a versioned JAR verification
  script, and one CI `package` artifact in place of the broken duplicate upload.
- Updated GitHub Actions for Gradle on `develop`/`main` and switched CodeQL to
  an explicit Gradle build; migrated Dependabot and added Actions updates.
- Added Gradle 9.8.2 Wrapper launchers and published SHA-256 checksums;
  the official Wrapper JAR itself still needs to be fetched, verified, and
  committed to `develop`. CI build and in-game acceptance remain outstanding.
- Established temporary modernization and regression-test plans under `docs/`.
- Updated `.gitignore` to exclude local ZIP archives and anticipated Gradle
  build/cache output, while retaining existing Maven exclusions.

_No gameplay or Java source code was changed in Phase 1._
