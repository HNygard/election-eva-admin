package no.valg.eva.admin.backend.web;

import static no.evote.util.EvoteProperties.TEST_KAN_LESE_GEOGRAFI;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import no.evote.util.EvaConfigProperty;
import no.valg.eva.admin.common.configuration.model.GeografiSpesifikasjon;
import no.valg.eva.admin.common.web.EvaAdminServlet;
import no.valg.eva.admin.configuration.domain.service.GeografiSpesifikasjonDomainService;

import org.apache.log4j.Logger;

@WebServlet(urlPatterns = "/lagGeografiSpesifikasjon")
public class LagGeografiSpesifikasjonServlet extends EvaAdminServlet {

	static final String PARAMETER_EKSPORTTYPE = "eksporttype";
	static final String PARAMETER_ANNTALL_KRETSER = "antallKretser";
	static final String PARAMETER_VALGHENDELSE_ID = "valghendelsesId";

	static final String VALUE_ALLE_KOMMUNER = "Kommuner";
	static final String VALUE_ALLE_KRETSER = "Kretser";
	static final String VALUE_BEGRENSET_ANTALL_KRETSER = "BegrensetAntallKretser";
	static final String VALUE_ALLE_STEMMESTEDER = "Stemmesteder";
	
	private static final String ADVARSEL_IKKE_TILGANG = "Et forsøk på å nå tjenesten LagGeografiSpesifikasjonServlet ble registrert."
		+ " Denne funksjonaliteten er skrudd av så et kall på denne tjenesten tyder på et mulig forsøk på innbrudd i systemet";

	private GeografiSpesifikasjonDomainService geografiSpesifikasjonDomainService;
	private boolean erTjenestenTilkoblet;

	@Override
	protected Logger getLogger() {
		return Logger.getLogger(LagGeografiSpesifikasjonServlet.class);
	}

	@Inject
	public LagGeografiSpesifikasjonServlet(GeografiSpesifikasjonDomainService geografiSpesifikasjonDomainService,
										   @EvaConfigProperty@Named(TEST_KAN_LESE_GEOGRAFI) boolean erTjenestenTilkoblet) {
		this.geografiSpesifikasjonDomainService = geografiSpesifikasjonDomainService;
		this.erTjenestenTilkoblet = erTjenestenTilkoblet;
	}
	
	@Override
	protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
		if (erTjenestenTilkoblet) {
			sendHtmlTilbake(resp, lagGeografiSpesifikasjonForm());
		} else {
			sendNotFoundResponse(resp, ADVARSEL_IKKE_TILGANG);
		}
	}

	private String lagGeografiSpesifikasjonForm() {
		return "<form method=\"POST\">"
			+ "<p>Velg hva slags type geografi-spesifikasjon du vil ha:</p>"
			+ "<input type='radio' name='" + PARAMETER_EKSPORTTYPE + "' value='" + VALUE_ALLE_KOMMUNER + "'/>Alle kommuner<br/> "
			+ "<input type='radio' name='" + PARAMETER_EKSPORTTYPE + "' value='" + VALUE_ALLE_KRETSER + "'/>Alle kretser<br/> "
			+ "<input type='radio' name='" + PARAMETER_EKSPORTTYPE + "' value='" + VALUE_BEGRENSET_ANTALL_KRETSER + "'/>Begrenset antall kretser: "
			+ "Maks antall kretser per kommune/bydel: <input type='text' name='" + PARAMETER_ANNTALL_KRETSER + "' /><br/> "
			+ "<input type='radio' name='" + PARAMETER_EKSPORTTYPE + "' value='" + VALUE_ALLE_STEMMESTEDER + "'/>Alle stemmesteder<br/> "
			+ "Valghendelsesid: <input type='text' name='" + PARAMETER_VALGHENDELSE_ID + "' /><br/> "
			+ "<input type='submit' value='Update'/>"
			+ "</form>";
	}

	@Override
	protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
		if (erTjenestenTilkoblet) {
			lagGeografiSpesifikasjon(req, resp);
		} else {
			sendNotFoundResponse(resp, ADVARSEL_IKKE_TILGANG);
		}
	}

	private void lagGeografiSpesifikasjon(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String parameterEksporttype = req.getParameter(PARAMETER_EKSPORTTYPE);
		String parameterAntallKretser = req.getParameter(PARAMETER_ANNTALL_KRETSER);
		String parameterValghendelseId = req.getParameter(PARAMETER_VALGHENDELSE_ID);
		
		if (feilIParametre(parameterEksporttype, parameterAntallKretser, parameterValghendelseId)) {
			sendHtmlTilbake(resp, lagGeneriskFeilmelding() + lagGeografiSpesifikasjonForm());
		} else {
			GeografiSpesifikasjon geografiSpesifikasjon = lagGeografiSpesifikasjonFor(parameterEksporttype, parameterAntallKretser, parameterValghendelseId);
			sendJsonTilbake(resp, geografiSpesifikasjon);
		}
	}

	private boolean feilIParametre(String parameterEksporttype, String parameterAntallKretser, String parameterValghendelseId) {
		return nullEllerEmpty(parameterValghendelseId)
			|| nullEllerEmpty(parameterEksporttype)
			|| begrensetEksportAvKretserUtenAntallSpesifisert(parameterEksporttype, parameterAntallKretser);
	}

	private boolean begrensetEksportAvKretserUtenAntallSpesifisert(String parameterEksporttype, String parameterAntallKretser) {
		return parameterEksporttype.equals(VALUE_BEGRENSET_ANTALL_KRETSER) && (parameterAntallKretser == null || parameterAntallKretser.trim().isEmpty());
	}

	private String lagGeneriskFeilmelding() {
		return "<p><b>Feil i inndata:</b> Du må ha skrevet inn valghendelsesid, og må velge 1 av eksporttypene."
			+ " Hvis du har valgt begrenset antall kretser, så må også antallet være spesifisert </p>";
	}

	private GeografiSpesifikasjon lagGeografiSpesifikasjonFor(String parameterEksporttype, String parameterAntallKretser, String parameterValghendelseId) {
		GeografiSpesifikasjon geografiSpesifikasjon = null;
		if (VALUE_ALLE_KOMMUNER.equals(parameterEksporttype)) {
			geografiSpesifikasjon = geografiSpesifikasjonDomainService.lagGeografiSpesifikasjonForKommuner(parameterValghendelseId);
		} else if (VALUE_ALLE_KRETSER.equals(parameterEksporttype)) {
			geografiSpesifikasjon = geografiSpesifikasjonDomainService.lagGeografiSpesifikasjonForKretser(parameterValghendelseId);
		} else if (VALUE_BEGRENSET_ANTALL_KRETSER.equals(parameterEksporttype)) {
			geografiSpesifikasjon = geografiSpesifikasjonDomainService
				.lagGeografiSpesifikasjonForBegrensetAntallKretser(parameterValghendelseId, Integer.parseInt(parameterAntallKretser));
		} else if (VALUE_ALLE_STEMMESTEDER.equals(parameterEksporttype)) {
			geografiSpesifikasjon = geografiSpesifikasjonDomainService.lagGeografiSpesifikasjonForStemmesteder(parameterValghendelseId);
		}
		return geografiSpesifikasjon;
	}
	
}
