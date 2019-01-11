package no.valg.eva.admin.frontend.voting.ctrls;

import lombok.Getter;
import lombok.NoArgsConstructor;
import no.evote.service.producer.EjbProxy;
import no.evote.service.voting.VotingService;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.configuration.domain.model.Voter;
import no.valg.eva.admin.felles.melding.Melding;
import no.valg.eva.admin.felles.sti.valggeografi.KommuneSti;
import no.valg.eva.admin.felles.sti.valggeografi.ValggeografiSti;
import no.valg.eva.admin.felles.sti.valghierarki.ValggruppeSti;
import no.valg.eva.admin.felles.sti.valghierarki.ValghierarkiSti;
import no.valg.eva.admin.frontend.i18n.MessageProvider;
import no.valg.eva.admin.frontend.manntall.models.ManntallsSokType;
import no.valg.eva.admin.frontend.manntall.widgets.ManntallsSokListener;
import no.valg.eva.admin.frontend.manntall.widgets.ManntallsSokWidget;
import no.valg.eva.admin.frontend.user.ctrls.UserDataController;
import no.valg.eva.admin.voting.domain.model.StemmegivningsType;
import no.valg.eva.admin.voting.domain.model.VelgerSomSkalStemme;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;

import static no.valg.eva.admin.felles.melding.Alvorlighetsgrad.WARN;
import static no.valg.eva.admin.frontend.manntall.models.ManntallsSokType.AVANSERT;
import static no.valg.eva.admin.frontend.manntall.models.ManntallsSokType.FODSELSNUMMER;
import static no.valg.eva.admin.frontend.manntall.models.ManntallsSokType.MANNTALLSNUMMER;
import static org.apache.commons.lang3.StringUtils.isEmpty;

@Named
@ViewScoped
@NoArgsConstructor // For CDI
public class RegisterVotingInEnvelopeSearchListener implements ManntallsSokListener, Serializable {

    private static final long serialVersionUID = -7543238287741829250L;

    @Inject
    @EjbProxy
    protected VotingService votingService;

    @Inject
    private UserDataController userDataController;

    @Inject
    @Getter
    private MessageProvider messageProvider;

    @Inject
    private ManntallsSokWidget electoralRollSearchWidget;

    @Getter
    private RegisterVotingInEnvelopeController registerVotingInEnvelopeController;

    public void addListener(RegisterVotingInEnvelopeController registerVotingInEnvelopeController) {
        this.registerVotingInEnvelopeController = registerVotingInEnvelopeController;
        electoralRollSearchWidget.addListener(this);
    }

    @Override
    public void manntallsSokInit() {
        registerVotingInEnvelopeController.setEmptySearchResult(false);
        registerVotingInEnvelopeController.setVoter(null);
    }

    @Override
    public void manntallsSokVelger(Voter voter) {
        registerVotingInEnvelopeController.setVoter(voter);
        registerVotingInEnvelopeController.setEmptySearchResult(false);

        if (voter != null) {
            registerVotingInEnvelopeController.setShowElectoralRollSearchView(false);
            registerVotingInEnvelopeController.setShowRegisterVotingView(true);
        }

        VotingController votingController = registerVotingInEnvelopeController.getSelectedMenuItem().getVotingController();

        if (checkIfElectoralRolCheckOffDone(votingController)) {
            registerVotingInEnvelopeController.addMessage(new Melding(WARN, "@voting.search.forhåndsstemmerStengtPgaAvkrysningsmanntallKjort"));
        }

        votingController.onSelectedVoterClick(registerVotingInEnvelopeController.getElectionGroupAsMvElection());
        findVoterForVoting(voter, registerVotingInEnvelopeController.getElectionGroupAsMvElection());
    }

    private void findVoterForVoting(Voter voter, MvElection mvElection) {
        VotingController registerVoteController = registerVotingInEnvelopeController.getSelectedMenuItem().getVotingController();

        registerVotingInEnvelopeController.setVoteToOtherMunicipalityConfirmDialog(false);
        if (voter != null) {
            VelgerSomSkalStemme velgerSomSkalStemme = votingService.hentVelgerSomSkalStemme(
                    userDataController.getUserData(),
                    registerVoteController.votingType(),
                    mvElection.electionPath(),
                    registerVotingInEnvelopeController.getSelectedMvArea().areaPath(),
                    registerVotingInEnvelopeController.getVoter());
            registerVotingInEnvelopeController.setCanRegisterVoting(velgerSomSkalStemme.isKanRegistrereStemmegivning());
            registerVotingInEnvelopeController.buildVoterMessages(velgerSomSkalStemme);
        }
    }

    private boolean checkIfElectoralRolCheckOffDone(VotingController votingController) {
        MvArea selectedPollingPlace = registerVotingInEnvelopeController.getSelectedMvArea();
        return votingController.votingType().isForhandIkkeSentInnkomne() && selectedPollingPlace != null
                && selectedPollingPlace.getMunicipality().isAvkrysningsmanntallKjort();
    }

    @Override
    public void manntallsSokTomtResultat() {
        registerVotingInEnvelopeController.onEmptyElectoralRollSearchResult();
    }

    @Override
    public ValggruppeSti getValggruppeSti() {
        MvElection electionGroupAsMvElection = registerVotingInEnvelopeController.getElectionGroupAsMvElection();
        return electionGroupAsMvElection == null ? null : ValghierarkiSti.valggruppeSti(electionGroupAsMvElection.electionPath());
    }

    @Override
    public KommuneSti getKommuneSti() {
        MvArea selectedPollingPlace = registerVotingInEnvelopeController.getSelectedMvArea();
        return selectedPollingPlace == null ? null : ValggeografiSti.kommuneSti(selectedPollingPlace.areaPath());
    }

    @Override
    public String manntallsTomtResultatMelding(ManntallsSokType manntallsSokType) {
        String result = "";
        StemmegivningsType votingType = registerVotingInEnvelopeController.getSelectedMenuItem().getVotingController().votingType();
        if (manntallsSokType == FODSELSNUMMER) {
            result = getMessageProvider().get("@electoralRoll.ssnNotInElectoralRoll", electoralRollSearchWidget.getFodselsnummer());
        } else if (manntallsSokType == MANNTALLSNUMMER) {
            result = getMessageProvider().get("@electoralRoll.numberNotInElectoralRoll", electoralRollSearchWidget.getManntallsnummer());
        } else {
            if (manntallsSokType == AVANSERT) {
                if (votingType.isForhand() && !registerVotingInEnvelopeController.isForhandsstemmeRettIUrne()) {
                    result = getMessageProvider().get("@electoralRoll.personNotInElectoralRoll.special");
                } else {
                    result = getMessageProvider().get("@electoralRoll.personNotInElectoralRoll");
                }
            }
        }

        if (votingType.isValgtingOrdinaere()) {
            result += " " + getMessageProvider().get("@voting.mustUseSpecialCover");
        }
        return isEmpty(result) ? null : result;
    }
}

