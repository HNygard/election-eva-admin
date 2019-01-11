package no.valg.eva.admin.frontend.stemmegivning.ctrls.proving;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static javax.faces.application.FacesMessage.SEVERITY_INFO;
import static javax.faces.application.FacesMessage.SEVERITY_WARN;
import static no.valg.eva.admin.frontend.util.MessageUtil.buildDetailMessage;
import static no.valg.eva.admin.common.AreaPath.from;
import static no.valg.eva.admin.common.counting.model.CountCategory.VF;
import static no.valg.eva.admin.felles.sti.valggeografi.ValggeografiSti.kommuneSti;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.KOMMUNE;
import static no.valg.eva.admin.frontend.manntall.models.ManntallsSokType.LOPENUMMER;

import java.util.ArrayList;
import java.util.List;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import lombok.Getter;
import lombok.Setter;
import no.evote.service.voting.VotingRejectionService;
import no.valg.eva.admin.configuration.domain.model.Voter;
import no.valg.eva.admin.configuration.domain.model.VotingCategory;
import no.valg.eva.admin.configuration.domain.model.VotingRejection;
import no.valg.eva.admin.felles.sti.valggeografi.KommuneSti;
import no.valg.eva.admin.felles.valggeografi.model.Kommune;
import no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa;
import no.valg.eva.admin.frontend.common.ctrls.RedirectInfo;
import no.valg.eva.admin.frontend.manntall.models.ManntallsSokType;
import no.valg.eva.admin.frontend.stemmegivning.ctrls.StemmegivningController;
import no.valg.eva.admin.voting.domain.model.Voting;
import org.joda.time.DateTime;

@Named
@ViewScoped
public class VotingConfirmationVoterController extends StemmegivningController {
	@Inject
	private VotingRejectionService votingRejectionService;

	private boolean canApproveVoting;
	@Getter private boolean canRejectVoting;
	
	@Getter private List<Voting> votingsApproved;
	@Getter private List<Voting> votingsUnconfirmed;
	@Getter private List<Voting> votingsRejected;
	@Getter private Voting voting;
	@Getter private List<VotingRejection> votingRejections = new ArrayList<>();
	@Getter @Setter	private Long rejectionReason;
	@Getter private RedirectInfo redirectInfo;

	@Override
	public ValggeografiNivaa getStemmestedNiva() {
		return KOMMUNE;
	}

	@Override
	public void kontekstKlar() {
		redirectInfo = getAndRemoveRedirectInfo();
		if (redirectInfo != null) {
			String voterId = (String) redirectInfo.getData();
			Voter voter = voterService.findByElectionEventAndId(getUserData(), voterId, getUserData().getElectionEventPk()).get(0);
			manntallsSokVelger(voter);
		}
	}

	@Override
	public List<Kommune> getKommuneListe() {
		if (kommuneListe == null) {
			kommuneListe = singletonList(valggeografiService.kommune(getUserData(), getKommuneSti()));
		}
		return kommuneListe;
	}

	@Override
	public void manntallsSokVelger(Voter voter) {
		super.manntallsSokVelger(voter);
		if (voter == null) {
			return;
		}

		canApproveVoting = true;
		canRejectVoting = true;

		populateVotingListsForVoter(fetchVoterVotings());

		if (getVotingsUnconfirmed().isEmpty()) {
			buildDetailMessage("@voting.verifyBallot.noBallots", new String[] { getVelger().getNameLine() }, SEVERITY_INFO);
			manntallsSokVelger(null);
			return;
		}

		if (!getVotingsApproved().isEmpty()) {
			canApproveVoting = false;
			String municipalityName = getVotingsApproved().get(0).getMvArea().getMunicipalityName();
			String[] parameters = { municipalityName };
			buildDetailMessage("@voting.approveBallot.acceptedVoting", parameters, SEVERITY_WARN);
		}

		if (getVelger().getMvArea() == null || !getStemmested().getMunicipality().getPk().equals(getVelger().getMvArea().getMunicipality().getPk())) {
			canApproveVoting = false;
			buildDetailMessage("@voting.approveBallot.notSameMunicipality", SEVERITY_WARN);
		}

		if ((!getVelger().isEligible() || !getVelger().isApproved()) && !getVelger().isFictitious()) {
			canApproveVoting = false;
		}
	}

	@Override
	public String manntallsTomtResultatMelding(ManntallsSokType electoralRollSearchType) {
		if (electoralRollSearchType == LOPENUMMER) {
			return "@voting.approveVoting.noMatchVotingNumber";
		}
		return "@electoralRoll.personNotInElectoralRoll";
	}

	public String prepareRedirect() {
		leggRedirectInfoPaSession(redirectInfo);
		return addRedirect(redirectInfo.getUrl());
	}

	public boolean isCanApproveVotings(Voting unconfirmedVoting) {
		if (!canApproveVoting) {
			return false;
		} else if (getStemmested().getMunicipality().isElectronicMarkoffs()) {
			return true;
		}
		return unconfirmedVoting.getVotingCategory().isEarlyVoting() && !unconfirmedVoting.isLateValidation();
	}

	public void approveVoting(Voting unconfirmedVoting) {
		execute(() -> {
			votingService.updateAdvanceVotingApproved(getUserData(), unconfirmedVoting);
			buildDetailMessage("@voting.approveBallot.votingApproved", SEVERITY_INFO);
			populateVotingListsForVoter(fetchVoterVotings());
			canApproveVoting = false;
		});
	}

	public void setVotingToRejection(Voting unconfirmedVoting) {
		voting = unconfirmedVoting;
		// Gets rejection reasons specified for FI/FU/FB or VS/VB/VF (not approved)
		votingRejections = votingRejectionService.findByEarly(getUserData(), unconfirmedVoting);
	}

	public void rejectVoting() {
		execute(() -> {
			voting.setVotingRejection(findRejectionReason());
			voting.setValidationTimestamp(DateTime.now());
			votingService.update(getUserData(), voting);
			rejectionReason = null;
			buildDetailMessage("@voting.approveBallot.votingRejected", new String[] { voting.getVotingRejection().getName() }, SEVERITY_INFO);
			populateVotingListsForVoter(fetchVoterVotings());
		});
	}

	public void cancelRejection(Voting rejectedVoting) {
		execute(() -> {
			rejectedVoting.setVotingRejection(null);
			rejectedVoting.setValidationTimestamp(null);
			votingService.update(getUserData(), rejectedVoting);
			buildDetailMessage("@voting.approveBallot.undoRejectionResponse", new String[] { getVelger().getNameLine() }, SEVERITY_INFO);
			populateVotingListsForVoter(fetchVoterVotings());
		});
	}

	public String getVotingCategoryName(Voting voting) {
		VotingCategory stemmekategori = voting.getVotingCategory();
		if (VF.getId().equals(stemmekategori.getId())) {
			return "@voting.evoting.votes";
		}
		return stemmekategori.getName();
	}

	public String getVotingNumber(Voting voting) {
		if (voting.getVotingCategory() == null) {
			return String.valueOf(voting.getVotingNumber());
		}
		return voting.getVotingCategory().getId() + "-" + voting.getVotingNumber();
	}

	public boolean isShowUnconfirmedLinks(Voting unconfirmedVoting) {
		return (getUserAccess().isStemmegivingPrøvingForhåndEnkelt() && unconfirmedVoting.getVotingCategory().isEarlyVoting())
				|| (getUserAccess().isStemmegivingPrøvingValgtingEnkelt() && !unconfirmedVoting.getVotingCategory().isEarlyVoting());
	}

	private VotingRejection findRejectionReason() {
		return votingRejections
				.stream()
				.filter(grunn -> grunn.getPk().equals(rejectionReason)).findFirst().orElse(null);
	}

	private void populateVotingListsForVoter(List<Voting> allVotings) {
		votingsApproved = new ArrayList<>();
		votingsUnconfirmed = new ArrayList<>();
		votingsRejected = new ArrayList<>();

        for (Voting currentVoting : allVotings) {
            if (!isSameMunicipality(currentVoting)) {
                if (currentVoting.isApproved()) {
                    getVotingsApproved().add(currentVoting);
				}
            } else if (!currentVoting.isApproved() && currentVoting.getVotingRejection() == null
                    && currentVoting.getVotingNumber() != null
                    && currentVoting.getVotingNumber() > 0) {
                getVotingsUnconfirmed().add(currentVoting);
            } else if (currentVoting.isApproved()) {
                getVotingsApproved().add(currentVoting);
            } else if (currentVoting.getVotingRejection() != null) {
                getVotingsRejected().add(currentVoting);
			}
		}
	}
	
	private boolean isSameMunicipality(Voting voting) {
		KommuneSti municipalityPathForConfirmation = getKommuneSti();
		KommuneSti municipalityPathForVoting = kommuneSti(from(voting.getMvArea().getAreaPath()));
		return municipalityPathForConfirmation.equals(municipalityPathForVoting);
	}

	private List<Voting> fetchVoterVotings() {
		return votingService.getVotingsByElectionGroupAndVoter(getUserData(), getVelger().getPk(), getValgGruppe().getElectionGroup().getPk());
	}

	public boolean isWasRedirectedTo() {
		return redirectInfo != null;
	}
	
	public List<Voting> getVotingsApprovedSameMunicipality() {
		return getVotingsApproved().stream()
			.filter(this::isSameMunicipality)
			.collect(toList());
	}
}
