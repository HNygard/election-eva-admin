package no.valg.eva.admin.frontend.manntall.ctrls;

import static javax.faces.application.FacesMessage.SEVERITY_INFO;
import static no.evote.constants.ElectionLevelEnum.ELECTION_GROUP;
import static no.valg.eva.admin.configuration.domain.model.PollingDistrictType.CHILD;
import static no.valg.eva.admin.configuration.domain.model.PollingDistrictType.MUNICIPALITY;
import static no.valg.eva.admin.configuration.domain.model.PollingDistrictType.REGULAR;
import static no.valg.eva.admin.felles.sti.valggeografi.ValggeografiSti.stemmekretsSti;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.event.ValueChangeEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import no.valg.eva.admin.frontend.util.MessageUtil;
import no.evote.security.UserData;
import no.evote.service.configuration.MvElectionService;
import no.evote.service.configuration.VoterService;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.configuration.domain.model.Aarsakskode;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.configuration.domain.model.Voter;
import no.valg.eva.admin.felles.sti.valggeografi.KommuneSti;
import no.valg.eva.admin.felles.sti.valggeografi.ValggeografiSti;
import no.valg.eva.admin.felles.valggeografi.model.Kommune;
import no.valg.eva.admin.felles.valggeografi.model.Stemmekrets;
import no.valg.eva.admin.felles.valggeografi.service.ValggeografiService;
import no.valg.eva.admin.frontend.BaseController;
import no.valg.eva.admin.frontend.user.ctrls.UserDataController;

import org.joda.time.DateTime;

@Named
@ViewScoped
public class EndreVelgerManntallController extends BaseController {

	// Inject
	private UserDataController userDataController;
	private SokManntallController sokManntallController;
	private VoterService voterService;
	private ValggeografiService valggeografiService;
	private MvElectionService mvElectionService;

	private MvElection valgGruppe;

	// Rediger velger data
	private String velgerKommuneId;
	private List<Stemmekrets> velgerStemmekretsListe = new ArrayList<>();
	private String stemmekretsSti;
	private boolean redigereStemmeberettiget;
	private boolean redigereVelger;

	public EndreVelgerManntallController() {
		// For CDI
	}

	@Inject
	public EndreVelgerManntallController(
			UserDataController userDataController, SokManntallController sokManntallController,
			VoterService voterService, ValggeografiService valggeografiService, MvElectionService mvElectionService) {
		this.userDataController = userDataController;
		this.sokManntallController = sokManntallController;
		this.voterService = voterService;
		this.valggeografiService = valggeografiService;
		this.mvElectionService = mvElectionService;
	}

	@PostConstruct
	public void init() {
		List<MvElection> mveList = mvElectionService.findByPathAndLevel(userDataController.getElectionEvent().electionPath().path(), ELECTION_GROUP.getLevel());
		if (mveList.size() == 1) {
			valgGruppe = mveList.get(0);
		} else {
			MessageUtil.buildDetailMessage("@voting.card.electiongroups", SEVERITY_INFO);
		}
	}

	private UserData getUserData() {
		return userDataController.getUserData();
	}

	public void endreStemmeberettiget() {
		redigereStemmeberettiget = true;
	}

	public void lagreEndreStemmeberettiget() {
		if (getVelger() != null) {
			execute(() -> {
				getVelger().oppdaterStemmerett();
				lagreVelger("@electoralRoll.updatedApprovedInformation");
				redigereStemmeberettiget = false;
			});
		}

	}

	public void avbrytEndreStemmeberettiget() {
		redigereStemmeberettiget = false;
	}

	public void endreVelger() {
		if (isHarVelger()) {
			redigereVelger = true;
			KommuneSti kommuneSti = kommuneSti(
					userDataController.getElectionEvent(),
					getVelger().getMvArea().getCountryId(),
					getVelger().getMvArea().getCountyId(),
					getVelger().getMvArea().getMunicipalityId());
			velgerStemmekretsListe = valggeografiService.stemmekretser(kommuneSti, REGULAR, MUNICIPALITY, CHILD);
			setVelgerKommuneId(getVelger().getMvArea().getMunicipalityId());
			setStemmekretsSti(getVelger().getMvArea().areaPath().path());
		}
	}

	public void oppdaterStemmekretsListe(ValueChangeEvent event) {
		if (isHarVelger()) {
			KommuneSti kommuneSti = kommuneSti(
					userDataController.getElectionEvent(),
					getKommune().getCountryId(),
					event.getNewValue().toString().substring(0, 2),
					event.getNewValue().toString());
			velgerStemmekretsListe = valggeografiService.stemmekretser(kommuneSti, REGULAR, MUNICIPALITY, CHILD);
			if (velgerStemmekretsListe.isEmpty()) {
				setStemmekretsSti(null);
			} else {
				setStemmekretsSti(velgerStemmekretsListe.get(0).sti().areaPath().path());
			}
		}
	}

	public void lagreEndreVelger() {
		if (isHarVelger()) {
			execute(() -> {
				getVelger().setNameLine(sokManntallController.byggNavnelinje(getVelger()));
				getVelger().setDateTimeSubmitted(DateTime.now().toDate());
				getVelger().setImportBatchNumber(null);
				sokManntallController.endreStemmekrets(getVelger(), stemmekretsSti(AreaPath.from(stemmekretsSti)));
				sokManntallController.setMailingAddressSpecified(getVelger());
				lagreVelger("@electoralRoll.updatedPersonalInformation");
				redigereVelger = false;
			});
		}
	}

	private void lagreVelger(String key) {
		List<Aarsakskode> aarsakskoder = voterService.findAllAarsakskoder();
		getVelger().setAarsakskode(aarsakskoder.get(0).getId());
		sokManntallController.setVelger(voterService.updateWithManualData(getUserData(), getVelger()));
		MessageUtil.buildDetailMessage(key, new String[] { getVelger().getNameLine() }, SEVERITY_INFO);
	}

	public void avbrytEndreVelger() {
		redigereVelger = false;
	}

	public boolean isVisEndreStemmeberettigetKnapp() {
		if (!isHarVelger() || !userDataController.getUserAccess().isManntallRedigerPerson()) {
			return false;
		}

		return !isRediger() && getVelger().getMvArea() != null && isSammeKommune(getVelger());
	}

	public boolean isVisEndreVelgerKnapp() {
		if (!isHarVelger() || !userDataController.getUserAccess().isManntallRedigerPerson()) {
			return false;
		}
		boolean ikkeVis = getVelger().isApproved() && !isSammeKommune(getVelger());
		return !isRediger() && getVelger().getMvArea() != null && !ikkeVis;
	}

	public boolean isEditerManntallTilgjengelig() {
		return sokManntallController.isEditerManntallTilgjengelig();
	}

	public String getManntallsnummerMasked() {
		return sokManntallController.getManntallsnummerMasked();
	}

	private KommuneSti kommuneSti(ElectionEvent electionEvent, String countryId, String countyId, String municipalityId) {
		return ValggeografiSti.kommuneSti(AreaPath.from(electionEvent.getId(), countryId, countyId, municipalityId));
	}

	private boolean isSammeKommune(Voter velger) {
		return sokManntallController.isSammeKommune(velger);
	}

	public MvElection getValgGruppe() {
		return valgGruppe;
	}

	public MvArea getKommune() {
		return sokManntallController.getKommune();
	}

	public Voter getVelger() {
		return sokManntallController.getVelger();
	}

	private boolean isHarVelger() {
		return sokManntallController.isHarVelger();
	}

	public boolean isRediger() {
		return isRedigereStemmeberettiget() || isRedigereVelger();
	}

	public boolean isRedigereStemmeberettiget() {
		return redigereStemmeberettiget;
	}

	public boolean isRedigereVelger() {
		return redigereVelger;
	}

	public String getVelgerKommuneId() {
		return velgerKommuneId;
	}

	public void setVelgerKommuneId(String velgerKommuneId) {
		this.velgerKommuneId = velgerKommuneId;
	}

	public String getStemmekretsSti() {
		return stemmekretsSti;
	}

	public void setStemmekretsSti(String stemmekretsSti) {
		this.stemmekretsSti = stemmekretsSti;
	}

	public List<Kommune> getVelgerKommuneListe() {
		return sokManntallController.getKommuneListeKomplett();
	}

	public List<Stemmekrets> getVelgerStemmekretsListe() {
		return velgerStemmekretsListe;
	}
}
