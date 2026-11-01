# ArmorStandEditor

**ArmorStandEditor** is a Minecraft Paper plugin for adjusting armor stands in-game with an editing tool and inventory menus. Pose and rotate stands, place and resize them, change their properties, edit equipment, and copy setups between stands without needing vanilla data commands.

This is the **BadWolfMC-maintained fork** of [ArmorStandEditor by RypoFalem](https://github.com/RypoFalem/ArmorStandEditor). It preserves the original plugin's identity and Java package lineage while adding modern Paper support and editing features. See [Credits and license](#credits-and-license) for attribution.

**Current release candidate:** `1.0.0` · **Target server:** Paper `26.3` · **Java:** 25. The version is a candidate until final server testing and release approval are complete.

## Features

- **Pose and position:** Adjust head, body, arms and legs along the X/Y/Z axes; rotate or move the whole stand; reset poses; switch between coarse and fine adjustments.
- **Appearance:** Toggle arms, base plate, gravity, visibility and the original small/normal **Size** setting. Independently adjust **Scale** from 0.1× to 15.0× (Fine: 0.1×; Coarse: 0.5×).
- **Protection and display:** Toggle invulnerability, lock equipment against vanilla manipulation, enable **Marker** (no normal hitbox), prevent movement, or opt an individual stand out of ticking. These settings are independent.
- **Equipment:** Swap armor, head items and held items using an inventory-based editor. Locked equipment can still be changed through the authorized editor GUI.
- **Copy and paste:** Store stand setups in nine selectable slots, including the new properties. Equipment is pasted **only in Creative mode** to prevent item duplication. Restricted properties require their respective permissions when pasted.
- **Targeting:** Cycle nearly overlapping stands, and use **Select Target**, **Apply to Target** and **Reverse on Target** for stands that are tiny, invisible or marked.
- **Compatibility:** Ordinary block interactions and unrelated attacks are not intercepted simply because a player holds the editing tool. Region-protection checks continue to apply to edits.

## Using the editor

1. With `asedit.basic`, hold **flint** in your **main hand** (or the material configured as `tool`).
2. Click **air** with the tool to open the menu, or run `/ase gui` without holding the tool. Clicking a chest, door or button with flint is normal Minecraft interaction.
3. Select an editing mode, axis and adjustment size. **Left-click** a stand to apply an edit; **right-click** to reverse where supported (such as pose, rotation, placement and scale).
4. While holding the tool, **sneak + scroll** to change the axis, or use the **swap-hands key** (normally `F`) to cycle nearby targets. Use the explicit target buttons in the menu for stands without an ordinary clickable hitbox.
5. For **Equipment**, open its GUI, place an item on your cursor, and click an equipment preview slot to swap the **full stack**. Shift-click and drag transfers into preview slots are intentionally disabled.

A selection glow is shown briefly for normally ticking stands. The editor deliberately skips this highlight for stands with ticking disabled. No Ticking changes only the selected stand; it does not change Paper's world configuration.

## Commands

All command names and values are **lowercase only**. `/ase` is registered using Paper's native Brigadier API and supplies tab-completion suggestions filtered by permissions. Commands are player-only and require `asedit.basic`.

| Command | Action |
| --- | --- |
| `/ase` | Show available editor command syntax. |
| `/ase help` or `/ase ?` | Show in-game editing instructions. |
| `/ase gui` | Open the main editor menu. |
| `/ase mode <mode>` | Select an editing mode. |
| `/ase axis <x\|y\|z>` | Choose the axis for pose or position adjustments. |
| `/ase adj <coarse\|fine>` | Choose the adjustment size. |
| `/ase slot <1-9>` | Select one of nine copy/paste slots. |

Available modes (subject to permissions):

```text
head  body  leftarm  rightarm  leftleg  rightleg
placement  rotate  reset  equipment  copy  paste
showarms  invisible  gravity  baseplate  size  scale
invulnerable  lockequipment  marker  nomovement  notick  none
```

For example, `/ase mode scale` selects Scale; `/ase adj fine` changes the step to 0.1×. Mode selection alone does not change a stand until you edit one.

## Permissions

All declared permission nodes default to **`op`**. Grant `asedit.basic` to players who should use the editor and separately grant restricted capabilities as needed. The master permission is required even if another node is granted.

| Permission | Controls |
| --- | --- |
| `asedit.basic` | Commands, menus, tool interactions and ordinary stand editing. |
| `asedit.invisible` | Selecting and applying Invisible mode, **including visibility changes during paste**. |
| `asedit.lockequipment` | Toggling equipment locks and applying copied equipment-lock state. |
| `asedit.marker` | Toggling Marker and applying copied Marker state. |
| `asedit.notick` | Toggling No Ticking and applying copied ticking state. |
| `asedit.rename` | Enhanced name-tag renaming (also requires `asedit.basic`). |

Other modes, including Scale, Invulnerability and No Movement, use `asedit.basic`. The Invisible, Lock Equipment, Marker and No Ticking icons are hidden if their respective permission is missing; reopening the menu refreshes icon visibility. Permissions are rechecked when applying edits (including GUI Apply/Reverse and flint clicks), not only when selecting commands or GUI controls. Denied mode selection clears the previous editing mode to avoid accidental subsequent edits. The equipment-lock feature prevents **vanilla** equipment changes, not deliberate changes by other plugins or administrative APIs.

## Installation

1. Run a **Paper 26.3** server with **Java 25**. Other Minecraft/Paper versions have not been established as supported by this release candidate.
2. Download the verified `armorstandeditor-1.0.0.jar` from the approved build/release. If using GitHub Actions, extract the **JAR** from the downloadable `package.zip` artifact.
3. Put the JAR in the server's `plugins/` directory and **restart** the server.
4. On first start, the plugin creates `plugins/ArmorStandEditor/config.yml` and bundled language files. Configure permissions using your permission manager and test with a non-operator account.

Existing armor stands do **not** need to be recreated. The plugin does not automatically rescale, lock or disable ticking on untouched stands. Preserve your previous plugin JAR and configuration as a rollback backup before upgrading.

### Configuration

The existing `config.yml` format is retained; no new configuration system is required.

| Key | Default | Purpose |
| --- | --- | --- |
| `tool` | `FLINT` | Editing tool's Bukkit material name. |
| `coarse` / `fine` | `12` / `120` | Pose rotation divisors (steps per full rotation). |
| `requireToolData` / `toolData` | `false` / `0` | Optionally require matching legacy item data. |
| `requireToolLore` / `toolLore` | `false` / `Let's get dangerous` | Optionally require a matching first lore line. |
| `lang` | `en_US.yml` | Preferred language file; missing translations fall back to English. |
| `sendMessagesToActionBar` | `true` | Show editor messages in the action bar instead of chat. |
| `debug` | `false` | Additional plugin diagnostic output. |

Invalid `tool` values and nonpositive/invalid rotation divisors are reported on startup and safely fall back to the defaults. The tool-data option retains its legacy matching behavior. The bundled English resource is the fallback; edit or create a different language file instead of modifying the generated English default.

## Building from source

Requirements: **JDK 25** and **Python 3** (for the optional JAR verification script). The committed Gradle Wrapper supplies Gradle **9.8.2**; Maven and a system-wide Gradle installation are not required.

Windows PowerShell:

```powershell
.\gradlew.bat clean build
python scripts/verify-jar.py
```

Linux/macOS:

```sh
./gradlew clean build
python3 scripts/verify-jar.py
```

The compiled JAR is `build/libs/armorstandeditor-1.0.0.jar`. The verification script checks the plugin descriptor, packaged resources and Java 25 bytecode. Unit tests run as part of `build`.

The build retains Gradle Kotlin DSL and Paper's dynamic dependency:

```kotlin
compileOnly("io.papermc.paper:paper-api:26.3.build.+")
```

GitHub Actions runs Gradle build/JAR verification and CodeQL on pushes and pull requests for `develop` and `main`. Dependabot **version-update** pull requests target `develop` when its configuration is active on the default branch; security updates follow GitHub's default-branch behavior. See [the release procedure](docs/releasing.md) and [the regression checklist](docs/regression-checklist.md) before tagging or deploying a release.

The plugin version is defined in `gradle.properties`, independently of the Paper `api-version` in `plugin.yml`. See [changelog.md](changelog.md) for the changes since the original release lineage.

## Credits and license

**RypoFalem** created the original ArmorStandEditor, including the foundational editing experience and code **through the original 0.1.18-era releases**. This fork retains that authorship and source lineage. Original project and historical documentation: [RypoFalem/ArmorStandEditor](https://github.com/RypoFalem/ArmorStandEditor) and its [wiki](https://github.com/RypoFalem/ArmorStandEditor/wiki). The original wiki may not describe newer BadWolfMC functionality.

**BadWolfMC / mercurialmusic** maintain this fork and its subsequent compatibility updates and modernizations. Original copyright notices and the original author's credits are retained. See [LICENSE.md](LICENSE.md) for the GNU General Public License version 2; retained source headers specify GPL-2.0-or-later. This is a continuation of the original project, **not** a claim that the original author wrote the modern additions.
