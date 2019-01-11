package no.valg.eva.admin.backend.web;

import static java.util.Arrays.stream;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static no.evote.util.EvoteProperties.TEST_KAN_KLONE_ENDELIGE_TELLINGER;
import static org.apache.commons.lang3.StringUtils.isEmpty;

import java.io.IOException;
import java.util.List;
import java.util.function.Function;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import no.evote.util.EvaConfigProperty;
import no.valg.eva.admin.common.web.EvaAdminServlet;
import no.valg.eva.admin.counting.domain.model.KlonEndeligeTellingerResultat;
import no.valg.eva.admin.counting.domain.service.KlonEndeligeTellingerDomainService;
import no.valg.eva.admin.felles.sti.valggeografi.FylkeskommuneSti;
import no.valg.eva.admin.felles.sti.valggeografi.KommuneSti;
import no.valg.eva.admin.felles.sti.valggeografi.LandSti;
import no.valg.eva.admin.felles.sti.valghierarki.ValghendelseSti;

import org.apache.log4j.Logger;

@WebServlet(urlPatterns = "/klonEndeligeTellinger")
public class KlonEndeligeTellingerServlet extends EvaAdminServlet {
	static final String PARAMETER_VALGHENDELSE_ID = "valghendelsesId";
	static final String PARAMETER_LOKALT_FORDELT_PAA_KRETS = "lokaltFordeltPaaKrets";
	static final String PARAMETER_SENTRALT_FORDELT_PAA_KRETS = "sentraltFordeltPaaKrets";
	static final String PARAMETER_SENTRALT_SAMLET = "sentraltSamlet";
	static final String PARAMETER_KOMMUNER = "kommuner";
	private static final String ADVARSEL_IKKE_TILGANG = "Et forsøk på å nå tjenesten KlonEndeligeTellingerServlet ble registrert."
			+ " Denne funksjonaliteten er skrudd av så et kall på denne tjenesten tyder på et mulig forsøk på innbrudd i systemet";

	private final KlonEndeligeTellingerDomainService klonEndeligeTellingerDomainService;
	
	private final boolean erTjenestenTilkoblet;
	
	@Inject
	public KlonEndeligeTellingerServlet(KlonEndeligeTellingerDomainService klonEndeligeTellingerDomainService,
										@EvaConfigProperty @Named(TEST_KAN_KLONE_ENDELIGE_TELLINGER) boolean erTjenestenTilkoblet) {
		this.klonEndeligeTellingerDomainService = klonEndeligeTellingerDomainService;
		this.erTjenestenTilkoblet = erTjenestenTilkoblet;
	}

	@Override
	protected Logger getLogger() {
		return Logger.getLogger(KlonEndeligeTellingerServlet.class);
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		if (erTjenestenTilkoblet) {
			sendHtmlTilbake(resp, lagGeografiSpesifikasjonForm());
		} else {
			sendNotFoundResponse(resp, ADVARSEL_IKKE_TILGANG);
		}
	}

	private String lagGeografiSpesifikasjonForm() {
		return "<form method=\"POST\">"
				+ "<p>Klon kommunens endelige tellinger til fylkeskommunens kontrolltellinger for alle kommuner med unntak av (pr fylke):</p>"
				+ "<p><input type='text' name='" + PARAMETER_LOKALT_FORDELT_PAA_KRETS + "' value='0'/> kommuner med lokalt fordelt på krets<br/> "
				+ "<input type='text' name='" + PARAMETER_SENTRALT_FORDELT_PAA_KRETS + "' value='0'/> kommuner med sentralt fordelt på krets<br/> "
				+ "<input type='text' name='" + PARAMETER_SENTRALT_SAMLET + "' value='0'/> kommuner med sentralt samlet</p>"
				+ "<p>Unnta følgende kommuner (kommaseparert liste med id-er): <input type='text' name='" + PARAMETER_KOMMUNER + "' ></p>"
				+ "Valghendelsesid: <input type='text' name='" + PARAMETER_VALGHENDELSE_ID + "' /><br/> "
				+ "<p><input type='submit' value='Klon endelige tellinger'/></p>"
				+ "</form>";
	}

	@Override
	protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
		if (erTjenestenTilkoblet) {
			klonEndeligeTellinger(req, resp);
		} else {
			sendNotFoundResponse(resp, ADVARSEL_IKKE_TILGANG);
		}
	}

	private void klonEndeligeTellinger(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		ValghendelseSti valghendelseSti = new ValghendelseSti(req.getParameter(PARAMETER_VALGHENDELSE_ID));
		int skipAntallLokaltFordeltPaaKrets = Integer.valueOf(req.getParameter(PARAMETER_LOKALT_FORDELT_PAA_KRETS));
		int skipAntallSentraltFordeltPaaKrets = Integer.valueOf(req.getParameter(PARAMETER_SENTRALT_FORDELT_PAA_KRETS));
		int skipAntallSentraltSamlet = Integer.valueOf(req.getParameter(PARAMETER_SENTRALT_SAMLET));
		List<KommuneSti> skippedeKommuneStier = kommuneStier(valghendelseSti, req.getParameter(PARAMETER_KOMMUNER));
		KlonEndeligeTellingerResultat resultat = klonEndeligeTellingerDomainService.klonEndeligeTellinger(
				valghendelseSti, skipAntallLokaltFordeltPaaKrets, skipAntallSentraltFordeltPaaKrets, skipAntallSentraltSamlet, skippedeKommuneStier);
		sendHtmlTilbake(resp, resultat.toHtml());
	}

	private List<KommuneSti> kommuneStier(ValghendelseSti valghendelseSti, String parameter) {
		if (isEmpty(parameter)) {
			return emptyList();
		}
		String[] ider = parameter.split("\\s*,\\s*");
		return stream(ider).map(kommuneSti(valghendelseSti.valghendelseId())).collect(toList());
	}

	private Function<String, KommuneSti> kommuneSti(String valghendelseId) {
		return id -> {
			no.valg.eva.admin.felles.sti.valggeografi.ValghendelseSti valghendelseSti =
					new no.valg.eva.admin.felles.sti.valggeografi.ValghendelseSti(valghendelseId);
			return new KommuneSti(new FylkeskommuneSti(new LandSti(valghendelseSti, "47"), id.substring(0, 2)), id);
		};
	}
}
