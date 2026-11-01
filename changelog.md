# Changelog

This changelog records the **BadWolfMC ArmorStandEditor fork** beginning with its modernization from the original `0.1.18` lineage. Plugin releases now use independent semantic versions rather than the target Minecraft/Paper version.

## 1.0.0 — Modernized BadWolfMC fork (release candidate; unpublished)

This release preserves the original editing workflow while modernizing the build and adding native Paper armor stand controls. **Final in-game acceptance and release tagging remain pending.**

### Editing and gameplay

- Added **Scale** editing (0.1×–15.0×, 0.1× Fine / 0.5× Coarse), independent of the original Size toggle, and **Invulnerability** editing with support for editing marked-invulnerable stands.
- Added **Lock Equipment** across all six armor/hand slots. Normal Minecraft manipulation is blocked; authorized Equipment GUI edits remain possible.
- Reworked the Equipment GUI to use live previews and immediate full-stack cursor swaps instead of temporarily unequipping a stand. Blocked drag, shift-transfer and other preview-extraction paths.
- Added **Marker**, **No Movement** and opt-in **No Ticking** controls. Marker stands can be edited through explicit target actions; No Movement is persisted using entity data; No Ticking applies per stand and does not modify world configuration.
- Improved target selection for overlapping, scaled, invisible and hitbox-less stands. Physical clicks apply to the clicked stand except when deliberately cycling nearly co-located stands.
- Extended copy/paste snapshots to include Scale, Invulnerability, Marker, movement, ticking and managed equipment-lock state. Kept **Creative-only equipment paste** as the intentional item-duplication safeguard.
- Enforced separate permissions for Invisible, Lock Equipment, Marker and No Ticking, including during **paste**. Visibility paste now leaves the destination unchanged without `asedit.invisible`.
- Release-candidate permission hardening: hide all four restricted mode icons when unavailable, share their authorization policy across commands/GUI/paste, recheck the active mode when applying or reversing edits, and clear the previous mode following denied selection.
- Preserved ordinary flint use for players without editor permission, and ordinary block interactions and unrelated attacks for authorized editors. Retained region-protection checks for stand edits.
- Added `/ase gui` for opening the editor without using the tool, including in tight spaces.

### Commands and reliability

- Migrated `/ase` to **Paper's native Brigadier command registration**, with lowercase syntax, permission-aware completion and execution-time authorization. Removed the competing legacy command declaration.
- Hardened delayed GUI transitions, pending air-click menu tasks, logout handling and delayed name-tag updates.
- Added safer validation and fallback for the configured editing-tool material and pose-adjustment divisors; improved English translation fallbacks and Adventure formatting of legacy language text.
- Finished migrating item metadata, GUI titles, name tags and command messages away from deprecated Bukkit text APIs; GUI icons suppress vanilla detail lines while retaining custom names and lore. Updated optional tool durability/lore checks for modern item metadata.
- Preserved existing configuration keys, languages, plugin identity and Java packages; avoided automatic changes to deployed armor stands.

### Build, quality and release process

- Migrated Maven to **Gradle Kotlin DSL** with Gradle Wrapper and **Java 25**, building against Paper `26.3.build.+` without pinning individual Paper builds.
- Updated GitHub Actions build/artifact verification and **CodeQL** for `develop` and `main`; configured Dependabot version-update pull requests to target `develop` (effective once the configuration is present on the repository's default branch).
- Added unit tests, a packaged-JAR verification script and an explicit release/regression workflow.
- Separated plugin version `1.0.0` from Paper `api-version: 26.3`; release artifact is `armorstandeditor-1.0.0.jar`.

## 0.1.18 and earlier — Original ArmorStandEditor lineage

**Original author: [RypoFalem](https://github.com/RypoFalem/ArmorStandEditor).** The foundational armor stand editor—including tool-based pose editing, axis/adjustment selection, equipment handling and the original editor workflow—comes from RypoFalem's project. The `0.1.18` reference denotes the historical original-lineage version used before this fork's modernization; this section is an attribution and baseline marker, **not** a reconstructed list of individual historical upstream releases.

The BadWolfMC fork retains the original project identity, Java package lineage, copyright notices and applicable GPL license. Features listed under `1.0.0` describe subsequent work on this fork and are not attributed to the original author.
