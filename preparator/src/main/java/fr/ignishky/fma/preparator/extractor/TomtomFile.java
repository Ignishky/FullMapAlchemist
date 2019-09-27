package fr.ignishky.fma.preparator.extractor;

import java.util.stream.Stream;

public enum TomtomFile {

    BOUNDARIES_LEVEL0("mn", "______________a0."),
    ALTERNATE_NAMES("mn", "_an."),
    RAILROADS("mn", "_rr."),
    CENTERS_OF_SETTLEMENT("mn", "_sm."),
    WATER_AREA("mn", "_wa.");

    private final String product;
    private final String value;

    TomtomFile(String product, String value) {
        this.product = product;
        this.value = value;
    }

    public static Stream<String> allFilesFrom(String product) {
        return Stream.of(TomtomFile.values())
                .filter(tomtomFile -> tomtomFile.product.equals(product))
                .map(tomtomFile -> tomtomFile.value);
    }
}
