package fr.ignishky.fma.generator.helper;

import lombok.Getter;

import static java.util.Arrays.stream;

@Getter
public enum CityType {

    OTHER_SIMPLE(0, false, "isolated_dwelling"),
    OTHER_IMPORTANT(0, true, "village"),
    ADMINISTRATIVE_AREA_SIMPLE(1, false, "town"),
    ADMINISTRATIVE_AREA_IMPORTANT(1, true, "city"),
    ADMINISTRATIVE_PLACE_SIMPLE(2, false, "town"),
    ADMINISTRATIVE_PLACE_IMPORTANT(2, true, "city"),
    POSTAL(4, false, "locality"),
    BUILT_UP_AREA(8, false, "quarter"),
    CENSUS(16, false, "quarter"),
    HAMLET(32, false, "hamlet"),
    NEIGHBORHOOD(64, false, "neighbourhood");

    private final int key;
    private final boolean important;
    private final String value;

    CityType(int key, boolean important, String value) {
        this.key = key;
        this.important = important;
        this.value = value;
    }

    public static String getOsmValue(int value, int displayClass) {
        return stream(CityType.values())
                .filter(cityType -> isTheBiggestCityType(value, displayClass < 8, cityType))
                .map(CityType::getValue)
                .findFirst().orElse("town");
    }

    private static boolean isTheBiggestCityType(int value, boolean displayClass, CityType cityType) {
        return cityType.key == value && cityType.important == displayClass;
    }
}
