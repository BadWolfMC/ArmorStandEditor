package io.github.rypofalem.armorstandeditor;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConfigValidationTest {
    @Test
    void defaultAndTypicalDivisorsRemainValid() {
        assertTrue(ConfigValidation.isUsableRotationDivisor(12));
        assertTrue(ConfigValidation.isUsableRotationDivisor(120));
        assertTrue(ConfigValidation.isUsableRotationDivisor(1));
        assertTrue(ConfigValidation.isUsableRotationDivisor(360));
    }

    @Test
    void zeroNegativeAndNonfiniteDivisorsAreRejected() {
        for (double divisor : new double[] {0, -1, -120, Double.NaN,
                Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.MIN_VALUE,
                1.0e-307}) {
            assertFalse(ConfigValidation.isUsableRotationDivisor(divisor), "divisor: " + divisor);
        }
    }
}
