package no.valg.eva.admin.frontend.manntall.ctrls;

import static javax.faces.application.FacesMessage.SEVERITY_WARN;
import static no.evote.constants.AreaLevelEnum.ROOT;
import static no.valg.eva.admin.frontend.manntall.models.ManntallsSokType.FODSELSNUMMER;
import static no.valg.eva.admin.frontend.manntall.models.ManntallsSokType.MANNTALLSNUMMER;

import java.util.ArrayList;
import java.util.List;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import no.valg.eva.admin.frontend.util.MessageUtil;
import no.valg.eva.admin.configuration.domain.model.Voter;
import no.valg.eva.admin.felles.sti.valggeografi.KommuneSti;
import no.valg.eva.admin.felles.sti.valggeografi.ValggeografiSti;
import no.valg.eva.admin.felles.sti.valghierarki.ValggruppeSti;
import no.valg.eva.admin.felles.valggeografi.model.Kommune;
import no.valg.eva.admin.frontend.common.ctrls.RedirectInfo;
import no.valg.eva.admin.frontend.kontekstvelger.Kontekst;
import no.valg.eva.admin.frontend.manntall.models.ManntallsSokType;
import no.valg.eva.admin.frontend.manntall.widgets.ManntallsSokListener;
import no.valg.eva.admin.frontend.manntall.widgets.ManntallsSokWidget;

@Named
@ViewScoped
public class SokManntallController extends BaseManntallController implements ManntallsSokListener {

	// Inject
	private ManntallsSokWidget manntallsSokWidget;

	private List<Kommune> kommuneListeKomplett;
	private List<Kommune> kommuneListe;
	private Voter velger;

	public SokManntallController() {
		// For CDI
	}

	@Inject
	public SokManntallController(ManntallsSokWidget manntallsSokWidget) {
		this.manntallsSokWidget = manntallsSokWidget;
	}

	@Override
	public void initialized(Kontekst kontekst) {
		super.initialized(kontekst);
		manntallsSokWidget.addListener(this);
		RedirectInfo redirectInfo = getAndRemoveRedirectInfo();
		if (redirectInfo != null) {
			Voter opprettetVelger = (Voter) redirectInfo.getData();
			manntallsSokVelger(opprettetVelger);
		}

	}

	@Override
	public void manntallsSokInit() {
		velger = null;
	}

	@Override
	public void manntallsSokVelger(Voter velger) {
		this.velger = velger;
		if (isHarVelger()) {
			if (velger.getMvArea() == null) {
				MessageUtil.buildDetailMessage("@electoralRoll.validation.noArea",
						new String[] { velger.getMunicipalityId(), velger.getPollingDistrictId() }, SEVERITY_WARN);
			} else {
				if (velger.isApproved() && !isSammeKommune(velger)) {
					MessageUtil.buildDetailMessage("@electoralRoll.validation.wrongMunicipality",
							new String[] { velger.getMvArea().getMunicipalityName() }, SEVERITY_WARN);
				}
			}
		}
	}

	@Override
	public void manntallsSokTomtResultat() {
		velger = null;
	}

	@Override
	public String manntallsTomtResultatMelding(ManntallsSokType manntallsSokType) {
		if (manntallsSokType == FODSELSNUMMER) {
			return getMessageProvider().get("@electoralRoll.ssnNotInElectoralRoll", manntallsSokWidget.getFodselsnummer());
		} else if (manntallsSokType == MANNTALLSNUMMER) {
			return getMessageProvider().get("@electoralRoll.numberNotInElectoralRoll", manntallsSokWidget.getManntallsnummer());
		} else {
			return getMessageProvider().get("@electoralRoll.personNotInElectoralRoll") + " ";
		}
	}

	@Override
	public KommuneSti getKommuneSti() {
		return getKommune() == null ? null : ValggeografiSti.kommuneSti(getKommune().areaPath());
	}

	@Override
	public ValggruppeSti getValggruppeSti() {
		return null;
	}

	public String getManntallsnummerMasked() {
		return manntallsSokWidget.getManntallsnummerObject() == null ? null : manntallsSokWidget.getManntallsnummerObject().getManntallsnummerMasked();
	}

	boolean isSammeKommune(Voter velger) {
		return getKommune().getMunicipalityId().equals(velger.getMvArea().getMunicipalityId());
	}

	private boolean isDisabledKommuneListe() {
		return getUserData().getOperatorMvArea().getActualAreaLevel().getLevel() != ROOT.getLevel()
				&& getUserDataController().getUserAccess().isManntallSøkKommune();
	}

	public Voter getVelger() {
		return velger;
	}

	public void setVelger(Voter velger) {
		this.velger = velger;
	}

	public boolean isHarVelger() {
		return getVelger() != null;
	}

	public List<Kommune> getKommuneListe() {
		if (kommuneListe == null) {
			kommuneListe = new ArrayList<>();
			if (isDisabledKommuneListe()) {
				kommuneListe.add(kommune(getKommune()));
			} else {
				kommuneListe = getKommuneListeKomplett();
			}
		}
		return kommuneListe;
	}

	public List<Kommune> getKommuneListeKomplett() {
		if (kommuneListeKomplett == null) {
			kommuneListeKomplett = kommunerForValghendelse();
		}
		return kommuneListeKomplett;
	}
}
