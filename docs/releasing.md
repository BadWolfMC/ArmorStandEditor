# Release process — BadWolfMC ArmorStandEditor

## Versioning

The release number is **SemVer**, independent of Minecraft/Paper versions:
`major.minor.patch`, beginning with `1.0.0` once accepted. Only change
`version=` in the root `gradle.properties` for a new release.
`build.gradle.kts` reads Gradle's standard project version; `plugin.yml`
uses `${project.version}` resource expansion. `scripts/verify-jar.py` reads the
same property and verifies the packaged descriptor, files, and Java 25 bytecode.

- **PATCH** (`1.0.1`): bug fixes and maintenance without intentional interface changes.
- **MINOR** (`1.1.0`): backward-compatible enhancements; use this for a routine
  new Paper target *if it entails a new compatibility baseline* and behavior/API
  improvements, or choose a patch for a no-feature compatibility-only rebuild.
- **MAJOR** (`2.0.0`): intentional breaking changes to commands, config,
  permissions, persisted stand data, or another publicly relied-upon contract.

The required Paper API stays in `src/main/resources/plugin.yml` as
`api-version: "26.3"` and the build dependency stays dynamic at
`compileOnly("io.papermc.paper:paper-api:26.3.build.+")` for this release.
These values are **not** plugin release numbers. Keep Java 25 in Gradle.

## Branch and Dependabot behavior

- Develop and validate changes on `develop`. CI and CodeQL run for pushes and
  pull requests to **both** `develop` and `main`.
- `.github/dependabot.yml` specifies `target-branch: develop` for **version
  updates** in Gradle and GitHub Actions. GitHub reads this file from the
  repository's **default branch**; copying the config to `develop` alone does
  not activate it if the default branch remains `main`.
- Dependabot **security-update** PRs still target the repository's default
  branch, regardless of `target-branch`. Process those promptly and validate
  equivalent changes on `develop` before promotion if necessary.
- Dependabot creates PRs; CI tests those PRs. Dependabot does not itself
  guarantee a passing build. Keep automatic merging disabled unless separately
  reviewed and approved.

## Release candidate to stable

1. Keep `main` untouched until explicit final approval. Bring the versioning
   changes onto `develop` and run `./gradlew clean build` (Windows:
   `.\gradlew.bat clean build`) and `python3 scripts/verify-jar.py`
   (Windows: `python scripts/verify-jar.py`).
2. Commit and push `develop`; confirm Gradle CI **and** CodeQL are green for
   the candidate commit, including all JUnit tests. Do not mistake the green
   status of an earlier commit for validation of uncommitted source.
3. Inspect the `package` Actions artifact (`package.zip`), confirming it
   contains the versioned JAR, and check the JAR/plugin.yml version, API,
   language files, and Java 25 bytecode.
4. Complete the final `docs/regression-checklist.md` against a test Paper 26.3
   server, especially native command registration, permission isolation,
   GUI transitions, equipment handling, marker recovery, copy/paste, and
   restart persistence. Record build number and artifact SHA-256.
5. Only after acceptance, deliberately merge `develop` into `main`, ensuring
   `.github/dependabot.yml` is present there; rerun checks on `main`.
6. Create an annotated tag `v1.0.0` on the accepted release commit. Publish
   the verified JAR and concise release notes if desired. Retain the previous
   production artifact for rollback. Do not automatically deploy.

If `1.0.0` has *not* passed live testing, call it a **release candidate** in
prose and keep it untagged; the version bump itself does not imply acceptance.
