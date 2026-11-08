package tr.depo.backend.envanter;

import tr.depo.backend.common.web.ApiYollari;

/** Envanter alt kaynaklarının yolları: hepsi envanter üzerinden ({@code /api/v1/envanterler/{envanterId}/...}). */
public final class EnvanterYollari {

    public static final String ENVANTER = ApiYollari.V1 + "/envanterler/{envanterId}";

    private EnvanterYollari() {
    }
}
