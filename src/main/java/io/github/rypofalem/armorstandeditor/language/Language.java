/*
 * ArmorStandEditor: Bukkit plugin to allow editing armor stand attributes
 * Copyright (C) 2016  RypoFalem
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package io.github.rypofalem.armorstandeditor.language;

import io.github.rypofalem.armorstandeditor.ArmorStandEditorPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class Language {
    final String DEFAULTLANG = "en_US.yml";
    private YamlConfiguration langConfig = null;
    private YamlConfiguration defConfig = null;
    private File langFile = null;
    ArmorStandEditorPlugin plugin;

    public Language(String langFileName, ArmorStandEditorPlugin plugin) {
        this.plugin = plugin;
        reloadLang(langFileName);
    }

    public void reloadLang(String langFileName) {
        if (langFileName == null) langFileName = DEFAULTLANG;
        File langFolder = new File(plugin.getDataFolder().getPath() + File.separator + "lang");
        langFile = new File(langFolder, langFileName);

        InputStream fallback = plugin.getResource("lang/" + DEFAULTLANG);
        if (fallback == null) {
            throw new IllegalStateException("Missing packaged English language fallback: " + DEFAULTLANG);
        }
        try (Reader reader = new InputStreamReader(fallback, StandardCharsets.UTF_8)) {
            defConfig = YamlConfiguration.loadConfiguration(reader);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to read packaged English fallback", e);
        }

        // Missing custom locale must fall back to English instead of leaving
        // langConfig null and crashing on the first message lookup.
        langConfig = new YamlConfiguration();
        if (langFile.isFile()) {
            try (Reader reader = new InputStreamReader(new FileInputStream(langFile), StandardCharsets.UTF_8)) {
                langConfig = YamlConfiguration.loadConfiguration(reader);
            } catch (IOException e) {
                plugin.getLogger().warning("Unable to load locale " + langFile.getName() + "; using English fallback");
            }
        } else {
            plugin.getLogger().warning("Locale " + langFile.getName() + " was not found; using English fallback");
        }
    }

    /** Interpret the existing section-code formatting when using Adventure components. */
    public static Component component(String legacyText) {
        return LegacyComponentSerializer.legacySection().deserialize(legacyText);
    }

    //path: yml path to message in language file
    //format: yml path to format in language file
    //option: path-specific variable that may be used
    public String getMessage(String path, String format, String option) {
        if (path == null) return "";
        if (option == null) option = "";

        String formatCodes = getFormat(format);
        StringBuilder legacyPrefix = new StringBuilder(formatCodes.length() * 2);
        for (int i = 0; i < formatCodes.length(); i++) {
            legacyPrefix.append('\u00a7').append(formatCodes.charAt(i));
        }

        if (getString(path + "." + option) != null) option = getString(path + "." + option);
        String message = getString(path + ".msg");
        if (message == null) return "";
        return legacyPrefix + message.replace("<x>", option);
    }

    public String getMessage(String path, String format) {
        return getMessage(path, format, null);
    }

    public String getMessage(String path) {
        return getMessage(path, "info");
    }

    /** Return the component as valid JSON, including escaped text and formatting.
     * Kept for compatibility with integrations that use the legacy method name.
     */
    public String getRawMessage(String path, String format, String option) {
        return GsonComponentSerializer.gson().serialize(component(getMessage(path, format, option)));
    }

    private String getFormat(String format){
        format = getString(format);
        return format == null ? "" : format;
    }

    private String getString(String path) {
        String message = null;
        if (langConfig != null && langConfig.contains(path)) {
            message = langConfig.getString(path);
        } else if (defConfig.contains(path)) {
            message = defConfig.getString(path);
        }
        return message;
    }
}