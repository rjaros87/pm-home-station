package pmstation.core.plantower;

public enum Unit {
    PARTICULATE_MATTER("\u03BCg/m\u00B3"),
    HCHO_UG("\u03BCg/m\u00B3"),
    HCHO_MG("mg/m\u00B3"),
    TEMPERATURE("\u2103"),
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
