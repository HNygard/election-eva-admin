package no.valg.eva.admin.frontend.stemmegivning.ctrls.registrering;

import no.valg.eva.admin.configuration.domain.model.Voter;
import no.valg.eva.admin.configuration.domain.model.VotingCategory;
import no.valg.eva.admin.felles.melding.Melding;
import no.valg.eva.admin.frontend.common.MeldingerWidget;
import no.valg.eva.admin.frontend.manntall.models.ManntallsSokType;
import no.valg.eva.admin.frontend.stemmegivning.ctrls.StemmegivningController;
import no.valg.eva.admin.frontend.util.MessageUtil;
import no.valg.eva.admin.util.DateUtil;
import no.valg.eva.admin.voting.domain.model.AvgittStemme;
import no.valg.eva.admin.voting.domain.model.StemmegivningsType;
import no.valg.eva.admin.voting.domain.model.VelgerMelding;
import no.valg.eva.admin.voting.domain.model.VelgerMeldingType;
import no.valg.eva.admin.voting.domain.model.VelgerSomSkalStemme;
import no.valg.eva.admin.voting.domain.model.Voting;
import org.joda.time.DateTime;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;
import static no.valg.eva.admin.frontend.manntall.models.ManntallsSokType.AVANSERT;
import static no.valg.eva.admin.frontend.manntall.models.ManntallsSokType.FODSELSNUMMER;
import static no.valg.eva.admin.frontend.manntall.models.ManntallsSokType.MANNTALLSNUMMER;
import static org.apache.commons.lang3.StringUtils.isEmpty;

/**
 * Superklasse for alle registrer stemmegivning controllere med felles funksjonalitet.
 */
public abstract class RegistreringController extends StemmegivningController {

	private MeldingerWidget statiskeMeldinger = new MeldingerWidget();
	private String stemmetype;
	private Voting stemmegivning;
	private List<VotingCategory> stemmetypeListe = new ArrayList<>();
	private boolean kanRegistrereStemmegivning;
	private boolean voteToOtherMunicipalityConfirmDialog;

	/**
	 * Hva slags stemmegivningstype?
	 */
	public abstract StemmegivningsType getStemmegivningsType();

	/**
	 * Registrer stemmegivning.
	 */
	public abstract void registrerStemmegivning();

	@Override
	public void manntallsSokVelger(Voter velger) {
		super.manntallsSokVelger(velger);
		hentVelgerSomSkalStemme();
	}

	@Override
	public void manntallsSokInit() {
		super.manntallsSokInit();
		setStemmegivning(null);
		setStemmetype(null);
	}

	@Override
	public String manntallsTomtResultatMelding(ManntallsSokType manntallsSokType) {
		String result = "";
		if (manntallsSokType == FODSELSNUMMER) {
			result = getMessageProvider().get("@electoralRoll.ssnNotInElectoralRoll", manntallsSokWidget.getFodselsnummer());
		} else if (manntallsSokType == MANNTALLSNUMMER) {
			result = getMessageProvider().get("@electoralRoll.numberNotInElectoralRoll", manntallsSokWidget.getManntallsnummer());
		} else {
			if (manntallsSokType == AVANSERT) {
				if (getStemmegivningsType().isForhand() && !isForhandsstemmeRettIUrne()) {
					result = getMessageProvider().get("@electoralRoll.personNotInElectoralRoll.special");
				} else {
					result = getMessageProvider().get("@electoralRoll.personNotInElectoralRoll");
				}
			}
		}
		if (getStemmegivningsType().isValgtingOrdinaere()) {
			result += " " + getMessageProvider().get("@voting.mustUseSpecialCover");
		}
		return isEmpty(result) ? null : result;
	}

	public void opprettFiktivVelger() {
		execute(() -> {
			setVelger(voterService.createFictitiousVoter(getUserData(), getStemmested().getMunicipality().areaPath()));
			manntallsSokVelger(getVelger());
		});
	}

	void hentVelgerSomSkalStemme() {
		getStatiskeMeldinger().clear();
		setVoteToOtherMunicipalityConfirmDialog(false);
		if (isHarVelger()) {
			VelgerSomSkalStemme velgerSomSkalStemme = votingService.hentVelgerSomSkalStemme(getUserData(), getStemmegivningsType(),
					getValgGruppe().electionPath(), getStemmested().areaPath(), getVelger());
			setKanRegistrereStemmegivning(velgerSomSkalStemme.isKanRegistrereStemmegivning());
			setStemmetypeListe(velgerSomSkalStemme.getStemmetypeListe());
			byggVelgerMeldinger(velgerSomSkalStemme);
		}
	}

	String byggStemmegivningsMelding(Voter velger, Voting voting, String melding) {
		String weekDay = getNameOfDay(voting.getCastTimeStampAsJavaTime());
		return getMessageProvider().get(
				melding,
				getNavn(velger),
				weekDay,
				DateUtil.getFormattedShortDate(voting.getCastTimestamp()),
				timeString(voting.getCastTimestamp()),
				voting.getVotingCategory().getId(),
				"" + voting.getVotingNumber());
	}

	private String getNameOfDay(LocalDateTime localDateTime) {
		return getMessageProvider().get(format("@common.date.weekday[%s].name", DateUtil.dayOfWeek(localDateTime.toLocalDate()))).toLowerCase();
	}

	String timeString(DateTime dateTime) {
		return MessageUtil.timeString(dateTime, getUserData().getJavaLocale());
	}

	/**
	 * Returnerer navn på velger. I utgangspunktet [for-, mellom- og etternavn] eller "Fiktiv velger".
	 */
	String getNavn(Voter velger) {
		if (velger.isFictitious()) {
			return getMessageProvider().get("@person.fictitiousVoterNameLine");
		} else {
			return velger.getNameLine();
		}
	}

	private void byggVelgerMeldinger(VelgerSomSkalStemme velgerSomSkalStemme) {
		for (VelgerMelding velgerMelding : velgerSomSkalStemme.getVelgerMeldinger()) {
			VelgerMeldingType type = velgerMelding.getVelgerMeldingType();
			switch (type) {
			case FORHANDSTEMME_ANNEN_KOMMUNE:
				setVoteToOtherMunicipalityConfirmDialog(true);
				getStatiskeMeldinger().add(new Melding(velgerMelding.getAlvorlighetsgrad(), type.getKey()));
				break;
			case ALLEREDE_IKKE_GODKJENT_STEMME_FORKASTET:
			case ALLEREDE_IKKE_GODKJENT_STEMME:
			case ALLEREDE_GODKJENT_STEMME:
				getStatiskeMeldinger().add(new Melding(velgerMelding.getAlvorlighetsgrad(), getAvgittStemmeMeldingMelding(velgerMelding)));
				break;
			case VELGER_IKKE_MANNTALLSFORT_DENNE_KOMMUNEN:
			case VELGER_IKKE_MANNTALLSFORT_DENNE_KRETSEN:
				String navn = getNameLine(getVelger());
				String melding1 = getMessageProvider().get(velgerMelding.getVelgerMeldingType().getKey(), navn);
				String melding2 = getMessageProvider().get(velgerMelding.getTilleggsMelding().getVelgerMeldingType().getKey());
				getStatiskeMeldinger().add(new Melding(velgerMelding.getAlvorlighetsgrad(), melding1 + " " + melding2));
				break;
			default:
				if (velgerMelding.getTilleggsMelding() == null) {
					getStatiskeMeldinger().add(new Melding(velgerMelding.getAlvorlighetsgrad(), type.getKey()));
				} else {
					String m1 = getMessageProvider().get(velgerMelding.getVelgerMeldingType().getKey());
					String m2 = getMessageProvider().get(velgerMelding.getTilleggsMelding().getVelgerMeldingType().getKey());
					getStatiskeMeldinger().add(new Melding(velgerMelding.getAlvorlighetsgrad(), m1 + " " + m2));
				}
			}
		}
	}

	private String getAvgittStemmeMeldingMelding(VelgerMelding melding) {
		AvgittStemme avgittStemme = (AvgittStemme) melding.getData();
		String weekDay = getNameOfDay(avgittStemme.getVotingTimeStamp());
		return getMessageProvider().get(melding.getVelgerMeldingType().getKey(),
				getNameLine(getVelger()),
				avgittStemme.getVotingCategory().getId(),
				getMessageProvider().get(avgittStemme.getVotingCategory().getName()),
				weekDay,
				DateUtil.getFormattedShortDate(avgittStemme.getStemmegivningsTidspunkt()),
				timeString(avgittStemme.getStemmegivningsTidspunkt()));
	}

	private String getNameLine(Voter velger) {
		if (velger.isFictitious()) {
			return getMessageProvider().get("@person.fictitiousVoterNameLine");
		} else {
			return velger.getNameLine();
		}
	}

	public String getStemmetype() {
		return stemmetype;
	}

	public void setStemmetype(String stemmetype) {
		this.stemmetype = stemmetype;
	}

	public Voting getStemmegivning() {
		return stemmegivning;
	}

	public void setStemmegivning(Voting stemmegivning) {
		this.stemmegivning = stemmegivning;
	}

	public MeldingerWidget getStatiskeMeldinger() {
		return statiskeMeldinger;
	}

	public List<VotingCategory> getStemmetypeListe() {
		return stemmetypeListe;
	}

	public void setStemmetypeListe(List<VotingCategory> stemmetypeListe) {
		this.stemmetypeListe = stemmetypeListe;
	}

	public boolean isKanRegistrereStemmegivning() {
		return kanRegistrereStemmegivning;
	}

	public void setKanRegistrereStemmegivning(boolean kanAvgiStemme) {
		this.kanRegistrereStemmegivning = kanAvgiStemme;
	}

	public boolean isVoteToOtherMunicipalityConfirmDialog() {
		return voteToOtherMunicipalityConfirmDialog;
	}

	public void setVoteToOtherMunicipalityConfirmDialog(boolean voteToOtherMunicipalityConfirmDialog) {
		this.voteToOtherMunicipalityConfirmDialog = voteToOtherMunicipalityConfirmDialog;
	}

    @Override
    public void resetVoteRegistration() {
        super.resetVoteRegistration();
        statiskeMeldinger.clear();
    }
}
