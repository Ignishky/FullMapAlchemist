package fr.ignishky.fma.preparator.extractor;

import lombok.Getter;

import java.util.stream.Stream;

public enum TomtomFile {

    BOUNDARIES_LEVEL0("mn", "______________a0."),
    ALTERNATE_NAMES("mn", "_an."),
    CENTERS_OF_SETTLEMENT("mn", "_sm.");

    @Getter
    private final String product;
    @Getter
    private final String value;

    TomtomFile(String product, String value) {
        this.product = product;
        this.value = value;
    }

    public static Stream<String> allFilesFrom(String product) {
        return Stream.of(TomtomFile.values())
                .filter(tomtomFile -> tomtomFile.product.equals(product))
                .map(TomtomFile::getValue);
    }
}
