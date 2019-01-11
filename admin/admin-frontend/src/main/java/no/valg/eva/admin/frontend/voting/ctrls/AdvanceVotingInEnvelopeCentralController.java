package no.valg.eva.admin.frontend.voting.ctrls;

import lombok.Getter;
import lombok.Setter;
import no.valg.eva.admin.common.voting.VotingCategory;
import no.valg.eva.admin.common.voting.VotingPhase;
import no.valg.eva.admin.common.voting.service.VotingRegistrationService;
import no.valg.eva.admin.configuration.application.MunicipalityMapper;
import no.valg.eva.admin.configuration.domain.model.ElectionGroup;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.configuration.domain.model.PollingPlace;
import no.valg.eva.admin.configuration.domain.model.Voter;
import no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa;
import no.valg.eva.admin.frontend.common.PageTitleMetaModel;
import no.valg.eva.admin.frontend.common.dialog.Dialog;
import no.valg.eva.admin.voting.domain.model.StemmegivningsType;
import no.valg.eva.admin.voting.domain.model.Voting;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

import static javax.faces.application.FacesMessage.SEVERITY_INFO;
import static no.valg.eva.admin.common.AreaPath.CENTRAL_ENVELOPE_REGISTRATION_POLLING_PLACE_ID;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.KOMMUNE;
import static no.valg.eva.admin.frontend.common.dialog.Dialogs.ADVANCE_VOTING_LATE_ARRIVAL_CONFIRM_SEARCH_DIALOG;
import static no.valg.eva.admin.frontend.util.MessageUtil.VOTING_NUMBER_ENVELOPE;
import static no.valg.eva.admin.frontend.util.MessageUtil.buildDetailMessage;
import static no.valg.eva.admin.voting.domain.model.StemmegivningsType.FORHANDSSTEMME_KONVOLUTTER_SENTRALT;
import static no.valg.eva.admin.voting.domain.model.StemmegivningsType.FORHANDSSTEMME_SENT_INNKOMNE_KONVOLUTTER;

@Named
@ViewScoped
public class AdvanceVotingInEnvelopeCentralController extends VotingController {

    private static final long serialVersionUID = 3811622700783382126L;
    private boolean dialogShown;
    
    @Inject
    private VotingRegistrationService votingRegistrationService;
    
    @Getter
    @Setter
    private Voting voting;

    @Override
    public ValggeografiNivaa getPollingPlaceElectionGeoLevel() {
        return KOMMUNE;
    }

    @Override
    public void resetData() {
        setVoting(null);
    }

    @Override
    public StemmegivningsType votingType() {
        return getVotingPhase().isBefore(VotingPhase.LATE) ? FORHANDSSTEMME_KONVOLUTTER_SENTRALT : FORHANDSSTEMME_SENT_INNKOMNE_KONVOLUTTER;
    }

    @Override
    public void registerVoting(Voter voter, MvElection mvElection, Municipality municipality,  VotingPhase votingPhase) {
        execute(() -> {
            PollingPlace pollingPlace = getPollingPlaceMvArea().getPollingPlace();
            ElectionGroup electionGroup = mvElection.getElectionGroup();
            no.valg.eva.admin.common.configuration.model.Municipality municipalityDto = MunicipalityMapper.toDto(municipality);
            
            voting = votingRegistrationService.registerAdvanceVotingInEnvelope(getUserData(), pollingPlace, electionGroup, municipalityDto, voter, getVotingCategory(), isLateVotingPhase(), votingPhase);

            addMessage(voter, voting);
        });
    }

    private void addMessage(final Voter voter, final Voting voting) {
        String melding = buildVotingMessage(voter, voting, "@voting.markOff.voterMarkedOffAdvance");
        if (!voting.getVotingCategory().getId().equalsIgnoreCase(VotingCategory.FA.getId())) {
            melding += " " + getMessageProvider().get(VOTING_NUMBER_ENVELOPE, voting.getVotingCategory().getId(), voting.getVotingNumber());
        } else {
            melding += " " + getMessageProvider().get("@voting.markOff.advanceForeignEnvelope");
        }
        buildDetailMessage(melding, SEVERITY_INFO);
    }

    private boolean isLateVotingPhase() {
        return VotingPhase.LATE.equals(getVotingPhase());
    }

    @Override
    public void onSelectedVotingCategory(MvArea selectedMvArea, VotingCategory votingCategory, VotingPhase votingPhase) {
        super.onSelectedVotingCategory(selectedMvArea, votingCategory, votingPhase);

        Long municipalityPk = getPollingPlaceMvArea().getMunicipality().getPk();
        MvArea pollingPlaceEnvelopeVotesCentral = getMvAreaService().findByMunicipalityAndPollingPlaceId(getUserData(), municipalityPk, CENTRAL_ENVELOPE_REGISTRATION_POLLING_PLACE_ID);
        setPollingPlaceMvArea(pollingPlaceEnvelopeVotesCentral);
    }

    @Override
    public List<PageTitleMetaModel> getPageTitleMeta() {
        return getPageTitleMetaBuilder().area(getCounty());
    }

    /**
     * Sletter siste avgitte stemme.
     */
    @Override
    public void deleteAdvanceVoting() {
        execute(() -> {
            String navn = getVoterName(getVoting().getVoter());
            votingService.delete(getUserData(), getVoting());
            if (getVoting().getVotingCategory().getId().equals(VotingCategory.FA.getId())) {
                buildDetailMessage("@voting.requestRemoveAdvanceVotingFA.response", new String[]{navn}, SEVERITY_INFO);
            } else {
                buildDetailMessage("@voting.requestRemoveAdvanceVoting.response",
                        new String[]{getVoting().getVotingCategory().getId(), getVoting().getVotingNumber() + "", navn},
                        SEVERITY_INFO);
            }
            setVoting(null);
        });
    }

    /**
     * Skal link til opprett fiktiv velger (etter tomt søk) vises?
     */
    public boolean isVisOpprettFiktivVelgerLink() {
        return emptyVoterSearchResult() && !isForhandsstemmeRettIUrne();
    }

    /**
     * Skal link til "slett stemmegivning" vises etter et stemmegivning?
     */
    @Override
    public boolean isShowDeleteAdvanceVotingLink() {
        return getVoting() != null
                && !isForhandsstemmeRettIUrne();
    }

    public boolean isShowDialog() {
        if (getPollingPlaceMvArea() == null || getPollingPlaceMvArea().getMunicipality() == null) {
            return false;
        }

        Municipality municipality = getPollingPlaceMvArea().getMunicipality();
        return !dialogShown
                && isLateVotingPhase()
                && !municipality.isAvkrysningsmanntallKjort()
                && !municipality.isElectronicMarkoffs();
    }

    public Dialog getConfirmLateValidationSearchModal() {
        return ADVANCE_VOTING_LATE_ARRIVAL_CONFIRM_SEARCH_DIALOG;
    }

    public void onConfirmLateArrivalDialogClick() {
        dialogShown = true;
    }
}
