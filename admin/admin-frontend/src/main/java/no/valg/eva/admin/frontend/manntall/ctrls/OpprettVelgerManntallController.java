package no.valg.eva.admin.frontend.manntall.ctrls;

import static com.google.common.collect.ImmutableList.of;
import static javax.faces.application.FacesMessage.SEVERITY_INFO;
import static no.evote.constants.AreaLevelEnum.ROOT;
import static no.valg.eva.admin.configuration.domain.model.PollingDistrictType.CHILD;
import static no.valg.eva.admin.configuration.domain.model.PollingDistrictType.MUNICIPALITY;
import static no.valg.eva.admin.configuration.domain.model.PollingDistrictType.REGULAR;
import static no.valg.eva.admin.felles.sti.valggeografi.ValggeografiSti.kommuneSti;
import static no.valg.eva.admin.felles.sti.valggeografi.ValggeografiSti.stemmekretsSti;

import java.io.IOException;
import java.util.List;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import no.valg.eva.admin.frontend.util.MessageUtil;
import no.evote.service.configuration.VoterService;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.configuration.domain.model.Voter;
import no.valg.eva.admin.felles.valggeografi.model.Kommune;
import no.valg.eva.admin.felles.valggeografi.model.Stemmekrets;
import no.valg.eva.admin.frontend.common.ctrls.RedirectInfo;
import no.valg.eva.admin.frontend.kontekstvelger.Kontekst;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

@Named
@ViewScoped
public class OpprettVelgerManntallController extends BaseManntallController {

	// Inject
	private VoterService voterService;

	private Voter velger;
	private List<Kommune> kommuneListe;
	private List<Stemmekrets> stemmekretsListe;
	private String kommuneId;
	private String stemmekretsSti;
	private String denneSideURL;

	public OpprettVelgerManntallController() {
		// For CDI
	}

	@Inject
	public OpprettVelgerManntallController(VoterService voterService) {
		this.voterService = voterService;
	}

	@Override
	public void initialized(Kontekst kontekst) {
		super.initialized(kontekst);
		stemmekretsSti = null;
		velger = new Voter();
        denneSideURL = getPageURL();
        kommuneListe = of(kommune(getKommune()));
		stemmekretsListe = stemmekretser(kommuneSti(getKommune().areaPath()), REGULAR, MUNICIPALITY, CHILD);

		setKommuneId(getKommune().getMunicipalityId());
		if (getUserDataController().getUserData().getOperatorMvArea().getActualAreaLevel() != ROOT
				&& getUserDataController().getUserAccess().isManntallSøkKommune()) {
			setKommuneId(getUserDataController().getUserData().getOperatorMvArea().getMunicipalityId());
		}
	}

	public void opprettVelger() {
		execute(() -> {
			populerVelger();
			Voter voterCreated = voterService.create(getUserDataController().getUserData(), getVelger());
			MessageUtil.buildDetailMessage("@electoralRoll.newVoterCreated", new String[] { voterCreated.getNameLine() }, SEVERITY_INFO);
			try {
				leggRedirectInfoPaSession(new RedirectInfo(voterCreated, denneSideURL, "@menu.electoralRoll.create"));
				String redirectTil = denneSideURL.replace("opprett.xhtml", "sok.xhtml");
				getFacesContext().getExternalContext().redirect(redirectTil);
			} catch (IOException e) {
				throw new RuntimeException("Failed to redirect " + e, e);
			}
		});
	}

	private void populerVelger() {
		getVelger().setImportBatchNumber(null);
		getVelger().setEndringstype('T');
		getVelger().setDateTimeSubmitted(DateTime.now().toDate());
		getVelger().setRegDato(LocalDate.now());
		getVelger().setNameLine(byggNavnelinje(getVelger()));
		getVelger().setSpesRegType(voterService.findAllSpesRegTypes(getUserData()).get(0).getId());
		getVelger().setStatuskode(voterService.findAllStatuskoder(getUserData()).get(0).getId());
		getVelger().setAarsakskode(voterService.findAllAarsakskoder().get(0).getId());
		endreStemmekrets(getVelger(), stemmekretsSti(AreaPath.from(stemmekretsSti)));
		getVelger().setMailingCountryCode("000");
		setMailingAddressSpecified(getVelger());
		getVelger().setEligible(true);
		getVelger().setApproved(false); // Creates person directly in the person registry, needs to be approved in the electoral roll later
		getVelger().setElectionEvent(getUserDataController().getElectionEvent());
	}

	public Voter getVelger() {
		return velger;
	}

	public String getKommuneId() {
		return kommuneId;
	}

	public void setKommuneId(String kommuneId) {
		this.kommuneId = kommuneId;
	}

	public List<Kommune> getKommuneListe() {
		return kommuneListe;
	}

	public String getStemmekretsSti() {
		return stemmekretsSti;
	}

	public void setStemmekretsSti(String stemmekretsSti) {
		this.stemmekretsSti = stemmekretsSti;
	}

	public List<Stemmekrets> getStemmekretsListe() {
		return stemmekretsListe;
	}
}
