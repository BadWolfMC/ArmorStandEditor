# Changelog

Notable changes to the BadWolfMC ArmorStandEditor fork are recorded here.
Keep entries concise and limited to work that has actually been completed.

## Unreleased

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
