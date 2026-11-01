package io.github.rypofalem.armorstandeditor;

import io.github.rypofalem.armorstandeditor.modes.EditMode;
import io.github.rypofalem.armorstandeditor.modes.ModePermissions;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Shared policy exercised by mode commands, icon visibility, pastes and edits. */
class ModePermissionsTest {
    private static final Map<EditMode, String> RESTRICTED = Map.of(
        EditMode.INVISIBLE, "asedit.invisible",
        EditMode.LOCKEQUIPMENT, "asedit.lockequipment",
        EditMode.MARKER, "asedit.marker",
        EditMode.NOTICK, "asedit.notick");

    @Test
    void allRestrictedModesUseTheirDistinctPermission() {
        for (EditMode mode : EditMode.values()) {
            String required = RESTRICTED.get(mode);
            assertEquals(required, ModePermissions.requiredPermission(mode), mode.toString());
            if (required == null) {
                assertTrue(ModePermissions.permitted(mode, ignored -> false), mode.toString());
            } else {
                assertFalse(ModePermissions.permitted(mode, ignored -> false), mode.toString());
                assertTrue(ModePermissions.permitted(mode, required::equals), mode.toString());
            }
        }
    }

    @Test
    void modePermissionChangesAreObservedOnEveryCheck() {
        Set<String> granted = new java.util.HashSet<>(Set.of("asedit.marker"));
        assertTrue(ModePermissions.permitted(EditMode.MARKER, granted::contains));
        granted.remove("asedit.marker");
        assertFalse(ModePermissions.permitted(EditMode.MARKER, granted::contains));
        assertTrue(ModePermissions.permitted(EditMode.NOMOVEMENT, granted::contains));
        assertFalse(ModePermissions.permitted(EditMode.NOTICK, granted::contains));
    }

    @Test
    void menuAndCommandModeListsMustNotRevealRestrictedModes() {
        for (Map.Entry<EditMode, String> entry : RESTRICTED.entrySet()) {
            List<String> suggestions = CommandOptions.modes(p -> !p.equals(entry.getValue()));
            assertFalse(suggestions.contains(entry.getKey().toString().toLowerCase(java.util.Locale.ROOT)));
            assertFalse(CommandOptions.permitted(entry.getKey(), p -> !p.equals(entry.getValue())));
        }
        assertTrue(CommandOptions.modes(p -> false).contains("paste"));
        assertTrue(CommandOptions.modes(p -> false).contains("equipment"));
    }
}
