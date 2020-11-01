package fr.ignishky.fma.generator.converter.dbf;

import lombok.Getter;

import static java.util.Arrays.stream;

public enum Language {
    AFR("af"),
    ALB("sq"),
    AMH("am"),
    AML(null),
    ARA("ar"),
    ARM("hy"),
    ARR(null),
    ASL(null),
    ASM("as"),
    AZE("az"),
    BAQ("eu"),
    BEL("be"),
    BEN("bn"),
    BEP(null),
    BET(null),
    BOS("bs"),
    BUL("bg"),
    BUN(null),
    CAT("ca"),
    CHI("zh"),
    CHQ(null),
    CHR(null),
    CHT(null),
    CZE("cs"),
    DAN("da"),
    DUT("nl"),
    ENG("en"),
    EST("et"),
    FIJ("fj"),
    FIL(null),
    FIN("fi"),
    FRE("fr"),
    FRY("fy"),
    GEL(null),
    GEO("ka"),
    GER("de"),
    GLA("gd"),
    GLE("ga"),
    GLG("gl"),
    GRE("el"),
    GRL(null),
    GUJ("gu"),
    GUL(null),
    HAU("ha"),
    HAW(null),
    HEB("he"),
    HEL(null),
    HID(null),
    HIN("hi"),
    HUN("hu"),
    IBO("ig"),
    ICE("is"),
    IKL(null),
    IND("in"),
    ITA("it"),
    JPL(null),
    JPN("ja"),
    KAD(null),
    KAN("kn"),
    KAO(null),
    KAZ("kk"),
    KHE(null),
    KHM("km"),
    KIL(null),
    KIN("rw"),
    KIR("ky"),
    KOK(null),
    KOL(null),
    KOO(null),
    KOR("ko"),
    KUL(null),
    KUR("ku"),
    LAV("lv"),
    LIT("lt"),
    LTZ("lb"),
    MAC("mk"),
    MAL("ml"),
    MAM(null),
    MAO("mi"),
    MAQ(null),
    MAR("mr"),
    MAT(null),
    MAY("ms"),
    MLT("mt"),
    MOC(null),
    MOM(null),
    NEL(null),
    NEP("ne"),
    NOR("no"),
    ORI("or"),
    ORL(null),
    PAB(null),
    PAN("pa"),
    PAO(null),
    PEL(null),
    PER("fa"),
    POL("pl"),
    POR("pt"),
    QUE("qu"),
    ROH("rm"),
    RUL(null),
    RUM("ro"),
    RUS("ru"),
    SCC("sr-Latn"),
    SCR(null),
    SCY("sr"),
    SIL(null),
    SIN("si"),
    SLO("sk"),
    SLV("sl"),
    SMC("sh"),
    SMO("sm"),
    SND("sd"),
    SNL(null),
    SPA("es"),
    SWE("sv"),
    SWL(null),
    TAH("ty"),
    TAL(null),
    TAM("ta"),
    TAN(null),
    TAT("tt"),
    TEL("te"),
    TEN(null),
    TGK("tg"),
    TGM(null),
    THA("th"),
    THL(null),
    TIL(null),
    TIR("ti"),
    TON("to"),
    TSN("tn"),
    TUK("tk"),
    TUR("tr"),
    UIG("ug"),
    UIL(null),
    UKL(null),
    UKR("uk"),
    URD("ur"),
    URL(null),
    UZB("uz"),
    VAL(null),
    VIE("vi"),
    WEL("cy"),
    WOL("wo"),
    XHO("xh"),
    YOR("yo"),
    ZUL("zu"),
    UND(null);

    @Getter
    private final String value;

    Language(String value) {
        this.value = value;
    }

    public static Language fromValue(String name) {
        return stream(Language.values())
                .filter(v -> v.name().equals(name))
                .findFirst()
                .orElse(UND);
    }
}