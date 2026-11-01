package io.github.rypofalem.armorstandeditor;

import io.github.rypofalem.armorstandeditor.modes.AdjustmentMode;
import io.github.rypofalem.armorstandeditor.modes.Axis;
import io.github.rypofalem.armorstandeditor.modes.EditMode;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CommandOptionsTest {
    @Test
    void allImplementedModesAreSuggestedInLowercaseWithPermissions() {
        List<String> values = CommandOptions.modes(permission -> true);
        assertEquals(EditMode.values().length, values.size());
        for (String mode : List.of("scale", "invulnerable", "lockequipment", "marker", "nomovement", "notick")) {
            assertTrue(values.contains(mode), mode);
        }
        assertFalse(values.contains("disableslots"));
        assertFalse(values.contains("Scale"));
    }

    @Test
    void modeSuggestionsEnforceRestrictions() {
        List<String> basicOnly = CommandOptions.modes(permission -> false);
        for (String mode : List.of("invisible", "lockequipment", "marker", "notick")) {
            assertFalse(basicOnly.contains(mode), mode);
        }
        assertTrue(basicOnly.contains("scale"));
        assertTrue(basicOnly.contains("nomovement"));

        Set<String> granted = Set.of("asedit.marker", "asedit.notick");
        List<String> partial = CommandOptions.modes(granted::contains);
        assertTrue(partial.contains("marker"));
        assertTrue(partial.contains("notick"));
        assertFalse(partial.contains("invisible"));
        assertFalse(partial.contains("lockequipment"));
    }

    @Test
    void modeExecutionAndSuggestionsSharePermissionRules() {
        assertEquals("asedit.invisible", CommandOptions.permissionFor(EditMode.INVISIBLE));
        assertEquals("asedit.lockequipment", CommandOptions.permissionFor(EditMode.LOCKEQUIPMENT));
        assertEquals("asedit.marker", CommandOptions.permissionFor(EditMode.MARKER));
        assertEquals("asedit.notick", CommandOptions.permissionFor(EditMode.NOTICK));
        assertNull(CommandOptions.permissionFor(EditMode.NOMOVEMENT));
        assertFalse(CommandOptions.permitted(EditMode.NOTICK, permission -> false));
    }

    @Test
    void lowercaseArgumentsAreRecognizedAndUppercaseRejected() {
        assertEquals(EditMode.LOCKEQUIPMENT, CommandOptions.matchingEnum(EditMode.class, "lockequipment"));
        assertEquals(EditMode.NOTICK, CommandOptions.matchingEnum(EditMode.class, "notick"));
        assertEquals(Axis.Z, CommandOptions.matchingEnum(Axis.class, "z"));
        assertEquals(AdjustmentMode.COARSE, CommandOptions.matchingEnum(AdjustmentMode.class, "coarse"));
        assertNull(CommandOptions.matchingEnum(EditMode.class, "LockEquipment"));
        assertNull(CommandOptions.matchingEnum(Axis.class, "Z"));
        assertNull(CommandOptions.matchingEnum(EditMode.class, "disableslots"));
    }

    @Test
    void completionsAreLowercaseAndPrefixMatched() {
        assertEquals(List.of("lockequipment"),
            CommandOptions.matching(CommandOptions.modes(permission -> true), "lock"));
        assertEquals(List.of("x", "y", "z"), CommandOptions.axes());
        assertEquals(List.of("coarse", "fine"), CommandOptions.adjustments());
        assertEquals(List.of("1"), CommandOptions.matching(CommandOptions.SLOTS, "1"));
        assertEquals(9, CommandOptions.SLOTS.size());
    }
}
