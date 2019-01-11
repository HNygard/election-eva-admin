package no.valg.eva.admin.backend.web;

import static no.evote.util.EvoteProperties.TEST_KAN_REDUSERE_GEOGRAFI;

import java.io.IOException;
import java.io.Writer;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import no.evote.util.EvaConfigProperty;
import no.valg.eva.admin.common.configuration.model.GeografiSpesifikasjon;
import no.valg.eva.admin.common.web.EvaAdminServlet;
import no.valg.eva.admin.configuration.domain.service.GeografiReduksjonsmodus;
import no.valg.eva.admin.configuration.domain.service.ReduserGeografiDomainService;
import no.valg.eva.admin.configuration.domain.service.ReduserVelgereDomainService;
import no.valg.eva.admin.felles.sti.valggeografi.ValghendelseSti;
import no.valg.eva.admin.opptelling.domain.service.ReduserOpptellingerDomainService;
import no.valg.eva.admin.voting.domain.service.ReduserStemmegivningerDomainService;

import org.apache.log4j.Logger;

import com.google.gson.Gson;

@WebServlet(urlPatterns = "/reduserGeografi")
public class ReduserGeografiServlet extends EvaAdminServlet {

	static final String PARAMETER_GEOGRAFISPESIFIKASJON = "geografiSpesifikasjon";
	static final String PARAMETER_VALGHENDELSE_ID = "valghendelsesId";
	static final String PARAMETER_REDUKSJONSMODUS = "reduksjonsmodus";

	static final String VALUE_IKKE_SLETT = "ikkeSlett";
	static final String VALUE_SLETT = "slett";
	static final String VALUE_FLYTT = "flytt";

	private static final String ADVARSEL_INGEN_TILGANG = "Et forsøk på å nå tjenesten LagGeografiSpesifikasjonServlet ble registrert."
		+ " Denne funksjonaliteten er skrudd av så et kall på denne tjenesten tyder på et mulig forsøk på innbrudd i systemet";

	private final boolean erTjenestenTilkoblet;
	private final ReduserOpptellingerDomainService reduserOpptellingerDomainService;
	private final ReduserGeografiDomainService reduserGeografiDomainService;
	private final ReduserStemmegivningerDomainService reduserStemmegivningerDomainService;
	private final ReduserVelgereDomainService reduserVelgereDomainService;

	@Inject
	public ReduserGeografiServlet(ReduserGeografiDomainService reduserGeografiDomainService,
								  ReduserVelgereDomainService reduserVelgereDomainService,
								  ReduserStemmegivningerDomainService reduserStemmegivningerDomainService,
								  ReduserOpptellingerDomainService reduserOpptellingerDomainService,
								  @EvaConfigProperty @Named(TEST_KAN_REDUSERE_GEOGRAFI) boolean erTjenestenTilkoblet) {
		this.reduserGeografiDomainService = reduserGeografiDomainService;
		this.reduserVelgereDomainService = reduserVelgereDomainService;
		this.reduserStemmegivningerDomainService = reduserStemmegivningerDomainService;
		this.reduserOpptellingerDomainService = reduserOpptellingerDomainService;
		this.erTjenestenTilkoblet = erTjenestenTilkoblet;
	}

	@Override
	protected Logger getLogger() {
		return Logger.getLogger(ReduserGeografiServlet.class);
	}

	@Override
	protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
		if (erTjenestenTilkoblet) {
			sendHtmlTilbake(resp, buildReduserGeografiForm());
		} else {
			sendNotFoundResponse(resp, ADVARSEL_INGEN_TILGANG);
		}
	}

	private String buildReduserGeografiForm() {
		return "<form method=\"POST\">"
			+ "<p>Spesifiser konfigurasjon for geografi-reduksjon (merk: json-format, typisk med utgangspunkt i noe laget av lagGeografiSpesifikasjonServlet):</p>"
			+ "<textarea rows=\"50\" cols=\"80\" name=\"" + PARAMETER_GEOGRAFISPESIFIKASJON + "\" ></textarea><br/> "
			+ "Valghendelsesid: <input type='text' name='" + PARAMETER_VALGHENDELSE_ID + "' /><br/> "
			+ "<p>Velg hvordan du vil håndtere evt. eksisterende velgere/stemmegivninger::</p>"
			+ "<input type='radio' name='" + PARAMETER_REDUKSJONSMODUS + "' value='" + VALUE_IKKE_SLETT + "' checked />Ikke slett velgere/stemmegivninger/opptellinger<br/> "
			+ "<input type='radio' name='" + PARAMETER_REDUKSJONSMODUS + "' value='" + VALUE_SLETT + "'/>Slett også velgere/stemmegivninger/opptellinger<br/> "
			+ "<input type='radio' name='" + PARAMETER_REDUKSJONSMODUS + "' value='" + VALUE_FLYTT + "'/>Flytt velgere/stemmegivninger i kretser som fjernes<br/> "
			+ "<input type='submit' value='Update'/>"
			+ "</form>";
	}

	@Override
	protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
		if (erTjenestenTilkoblet) {
			reduserGeografi(req, resp);
		} else {
			sendNotFoundResponse(resp, ADVARSEL_INGEN_TILGANG);
		}
	}

	private void reduserGeografi(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String parameterGeografispesifikasjon = req.getParameter(PARAMETER_GEOGRAFISPESIFIKASJON);
		String parameterValghendelseId = req.getParameter(PARAMETER_VALGHENDELSE_ID);
		GeografiReduksjonsmodus parameterReduksjonsmodus = finnReduksjonsmodus(req);

		if (feilIParametre(parameterGeografispesifikasjon, parameterValghendelseId)) {
			sendFeilmeldingTilbake(resp);
		} else {
			reduserGeografiFor(parameterGeografispesifikasjon, parameterValghendelseId, parameterReduksjonsmodus);
			Writer w = getOutputWriter(resp);
			w.append(wrapInHtmlHeaders("<p>Reduksjon av geografi er ferdigstilt. Se i loggen for å se hva som ble utført.</p>"));
		}
	}

	private GeografiReduksjonsmodus finnReduksjonsmodus(HttpServletRequest req) {
		switch (req.getParameter(PARAMETER_REDUKSJONSMODUS)) {
			case VALUE_SLETT:
				return GeografiReduksjonsmodus.SLETT;
			case VALUE_FLYTT:
				return GeografiReduksjonsmodus.FLYTT;
			default:
				return GeografiReduksjonsmodus.IKKE_SLETT;	
		}
	}

	private boolean feilIParametre(String parameterGeografispesifikasjon, String parameterValghendelseId) {
		return nullEllerEmpty(parameterValghendelseId)
			|| nullEllerEmpty(parameterGeografispesifikasjon);
	}

	private void sendFeilmeldingTilbake(HttpServletResponse resp) throws IOException {
		Writer w = getOutputWriter(resp);
		w.append(wrapInHtmlHeaders(buildGeneriskFeilmelding() + buildReduserGeografiForm()));
	}

	private String buildGeneriskFeilmelding() {
		return "<p><b>Feil i inndata:</b> Du må fylle inn en geografispesifikasjon og valghendelsesid.</p>";
	}

	private void reduserGeografiFor(String geografispesifikasjonString, String valghendelseId, GeografiReduksjonsmodus reduksjonsmodus) {
		GeografiSpesifikasjon geografiSpesifikasjon = geografiSpesifikasjon(geografispesifikasjonString);
		ValghendelseSti valghendelse = new ValghendelseSti(valghendelseId);
		
		// Forberedelser før selve slettingen av geografi
		if (reduksjonsmodus == GeografiReduksjonsmodus.SLETT) {
			reduserOpptellingerDomainService.reduserOpptellinger(geografiSpesifikasjon, valghendelse);
			reduserStemmegivningerDomainService.reduserStemmegivninger(geografiSpesifikasjon, valghendelse);
			reduserVelgereDomainService.reduserVelgere(geografiSpesifikasjon, valghendelse);
			
		} else if (reduksjonsmodus == GeografiReduksjonsmodus.FLYTT) {
			reduserOpptellingerDomainService.reduserOpptellinger(geografiSpesifikasjon, valghendelse);
			reduserStemmegivningerDomainService.flyttStemmegivninger(geografiSpesifikasjon, valghendelse);
			reduserVelgereDomainService.flyttVelgere(geografiSpesifikasjon, valghendelse);
		}

		// Geografislettingen
		reduserGeografiDomainService.reduserGeografi(geografiSpesifikasjon, valghendelse);
		
	}

	private GeografiSpesifikasjon geografiSpesifikasjon(String json) {
		return new Gson().fromJson(json, GeografiSpesifikasjon.class);
	}

}
