package pmstation.core.plantower;

public enum Unit {
    PARTICULATE_MATTER("µg/m³"),
    HCHO_UG("µg/m³"),
    HCHO_MG("mg/m³"),
    TEMPERATURE("°C"),
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
