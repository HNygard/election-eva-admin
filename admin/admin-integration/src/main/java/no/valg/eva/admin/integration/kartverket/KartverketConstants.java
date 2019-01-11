package no.valg.eva.admin.integration.kartverket;

import no.evote.util.EvoteProperties;

class KartverketConstants {

    private KartverketConstants() {
    }

    static final String KARTVERKET_WS_ENDPOINT = EvoteProperties.getProperty(EvoteProperties.GEONORGE_WS_ENDPOINT, EvoteProperties.GEONORGE_WS_ENDPOINT_DEFAULT);
    static final String ADRESSE_WS_ADRESSE_SOK = "/AdresseWS/adresse/sok";
    static final String ADRESSE_WS_LOCATION_SOK = "/SKWS3Index/ssr/sok";
    static final String APPLICATION_JSON_CHARSET_UTF_8 = "application/json;charset=utf-8";
}
