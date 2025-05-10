package pmstation.core.plantower;

public enum Unit {
    PARTICULATE_MATTER("\u00B5g/m\u00B3"), // µg/m³
    HCHO_UG("\u00B5g/m\u00B3"), // µg/m³
    HCHO_MG("mg/m\u00B3"), // mg/m³
    TEMPERATURE("\u00B0C"), // °C
    HUMIDITY("%"),
    ;

    private final String unit;

    Unit(String unit) {
        this.unit = unit;
    }

    @Override
    public String toString() {
        return unit;
    }
}
