package tr.depo.backend.controller;

/** API yolları; controller'lar sürüm önekini ve ortak üst yolları buradan alır. */
public final class ApiPaths {

    public static final String V1 = "/api/v1";

    /** Envanter alt kaynaklarının (barkod, not, aparat) üst yolu: hepsi envanter üzerinden erişilir. */
    public static final String ENVANTER = V1 + "/envanterler/{envanterId}";

    private ApiPaths() {
    }
}
