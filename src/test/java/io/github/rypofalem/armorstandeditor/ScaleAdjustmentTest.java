package io.github.rypofalem.armorstandeditor;

import io.github.rypofalem.armorstandeditor.modes.AdjustmentMode;
import io.github.rypofalem.armorstandeditor.modes.ScaleAdjustment;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ScaleAdjustmentTest {
    @Test
    void fineAndCoarseStepsUseExactTenths() {
        assertEquals(1.1, ScaleAdjustment.adjust(1.0, 1, AdjustmentMode.FINE));
        assertEquals(0.9, ScaleAdjustment.adjust(1.0, -1, AdjustmentMode.FINE));
        assertEquals(1.5, ScaleAdjustment.adjust(1.0, 1, AdjustmentMode.COARSE));
        assertEquals(0.5, ScaleAdjustment.adjust(1.0, -1, AdjustmentMode.COARSE));
        assertEquals(1.0, ScaleAdjustment.adjust(1.0, 0, AdjustmentMode.COARSE));
    }

    @Test
    void boundsAreClampedAndNonfiniteInputsResetToOne() {
        assertEquals(0.1, ScaleAdjustment.clamp(-100));
        assertEquals(15.0, ScaleAdjustment.clamp(100));
        assertEquals(1.0, ScaleAdjustment.clamp(Double.NaN));
        assertEquals(1.0, ScaleAdjustment.clamp(Double.POSITIVE_INFINITY));
        assertEquals(1.1, ScaleAdjustment.adjust(Double.NEGATIVE_INFINITY, 1, AdjustmentMode.FINE));
        assertEquals(0.1, ScaleAdjustment.adjust(0.1, -1, AdjustmentMode.COARSE));
        assertEquals(15.0, ScaleAdjustment.adjust(15.0, 1, AdjustmentMode.COARSE));
    }

    @Test
    void repeatedFineStepsDoNotAccumulateDrift() {
        double scale = 0.1;
        for (int i = 0; i < 149; i++) scale = ScaleAdjustment.adjust(scale, 1, AdjustmentMode.FINE);
        assertEquals(15.0, scale);
        for (int i = 0; i < 149; i++) scale = ScaleAdjustment.adjust(scale, -1, AdjustmentMode.FINE);
        assertEquals(0.1, scale);
    }
}
