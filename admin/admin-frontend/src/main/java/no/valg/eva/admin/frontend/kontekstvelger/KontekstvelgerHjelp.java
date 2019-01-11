package no.valg.eva.admin.frontend.kontekstvelger;

import static java.lang.String.format;
import static no.valg.eva.admin.frontend.kontekstvelger.KontekstvelgerParam.KONTEKST;
import static no.valg.eva.admin.frontend.kontekstvelger.KontekstvelgerParam.OPPSETT;

import no.valg.eva.admin.frontend.BaseController;
import no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerOppsett;

public final class KontekstvelgerHjelp {
	private KontekstvelgerHjelp() {
	}

	public static String requestParameterValue(BaseController baseController, KontekstvelgerParam param) {
		return baseController.getRequestParameter(param.toString());
	}

	public static String kontekstvelgerURL(KontekstvelgerOppsett oppsett) {
		return kontekstvelgerURL(oppsett, null);
	}

	public static String kontekstvelgerURL(KontekstvelgerOppsett oppsett, Kontekst kontekst) {
		String url = "/secure/kontekstvelger.xhtml?%s=%s";
		if (kontekst == null) {
			return format(url, OPPSETT, oppsett.serialize());
		}
		return format(url + "&%s=%s", OPPSETT, oppsett.serialize(), KONTEKST, kontekst.serialize());
	}
}
