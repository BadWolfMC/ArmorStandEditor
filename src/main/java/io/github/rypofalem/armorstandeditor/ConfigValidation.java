package io.github.rypofalem.armorstandeditor;

/** Validation for numeric config values before deriving angle increments. */
final class ConfigValidation {
  private ConfigValidation() {}

  static boolean isUsableRotationDivisor(double value) {
    double radiansPerStep = (2 * Math.PI) / value;
    double degreesPerStep = radiansPerStep / Math.PI * 180.0;
    return Double.isFinite(value) && value > 0.0 &&
        Double.isFinite(radiansPerStep) && radiansPerStep > 0.0 &&
        Double.isFinite(degreesPerStep) && degreesPerStep > 0.0;
  }
}
