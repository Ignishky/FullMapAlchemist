package fr.ignishky.fma.generator.converter.dbf;

import lombok.Getter;

import java.util.stream.Stream;

public enum Language {
    ALB("sq"),
    ARA("ar"),
    BAQ("eu"),
    BEL("be"),
    BOS("bs"),
    BUL("bg"),
    BUN(null),
    CAT("ca"),
    CHI("zh"),
    CHT(null),
    CZE("cs"),
    DAN("da"),
    DUT("nl"),
    ENG("en"),
    EST("et"),
    FIN("fi"),
    FRE("fr"),
    FRY("fy"),
    GER("de"),
    GLE("ga"),
    GLG("gl"),
    GRE("el"),
    GRL(null),
    HEB(null),
    HUN("hu"),
    ICE("is"),
    IND("in"),
    ITA("it"),
    JPN("ja"),
    KAZ("kk"),
    KOR("ko"),
    LAV("lv"),
    LIT("lt"),
    LTZ("lb"),
    MAC("mk"),
    MAY("ms"),
    MLT("mt"),
    NOR("no"),
    POL("pl"),
    POR("pt"),
    ROH("rm"),
    RUL(null),
    RUM("ro"),
    RUS("ru"),
    SCC("sr-Latn"),
    SCR(null),
    SCY("sr"),
    SLO("sk"),
    SLV("sl"),
    SMC("sh"),
    SPA("es"),
    SWE("sv"),
    THA("th"),
    TUR("tr"),
    UKL(null),
    UKR("uk"),
    VIE("vi"),
    WEL("cy"),
    UND(null);

    @Getter
    private final String value;

    Language(String value) {
        this.value = value;
    }

    public static Language fromValue(String name) {
        return Stream.of(Language.values()).filter(v -> v.name().equals(name)).findFirst().orElse(UND);
    }
}