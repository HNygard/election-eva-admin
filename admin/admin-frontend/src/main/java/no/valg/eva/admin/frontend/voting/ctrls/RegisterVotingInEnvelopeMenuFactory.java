package no.valg.eva.admin.frontend.voting.ctrls;

import lombok.NoArgsConstructor;
import no.valg.eva.admin.common.voting.VotingCategory;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.frontend.cdi.BeanLookupSingleton;
import no.valg.eva.admin.frontend.i18n.MessageProvider;
import no.valg.eva.admin.util.DateUtil;
import no.valg.eva.admin.voting.domain.model.VotingCategoryStatus;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static no.valg.eva.admin.common.voting.Tense.FUTURE;
import static no.valg.eva.admin.common.voting.Tense.PRESENT;
import static no.valg.eva.admin.common.voting.VotingPhase.ELECTION_DAY;
import static no.valg.eva.admin.common.voting.VotingPhase.LATE;
import static no.valg.eva.admin.frontend.common.CssClass.UI_STATE_EXPIRED;
import static no.valg.eva.admin.frontend.common.CssClass.UI_STATE_NOT_STARTED;

@Named
@ViewScoped
@NoArgsConstructor        // For CDI
public class RegisterVotingInEnvelopeMenuFactory implements VotingMenuFactory, Serializable {

    private static final String ADVANCE_VOTING_IN_ENVELOPE_VIEW = "advanceVotingInEnvelope";
    private static final String ELECTION_DAY_VOTING_IN_ENVELOPE_VIEW = "electionDayVotingInEnvelope";
    private static final String ADVANCE_VOTING_LATE_ARRIVAL_VIEW = "advanceVotingLateArrival";
    private static final long serialVersionUID = 2839840875879329992L;

    @Inject
    private MessageProvider messageProvider;

    @Inject
    private BeanLookupSingleton beanLookupSingleton;
    
    List<RegisterVotingInEnvelopeMenuItem> buildMenuItems(List<VotingCategoryStatus> votingCategoryStatus, final Municipality municipality) {
        return votingCategoryStatus.stream()
                .map(status -> buildMenuItem(status, municipality.isElectronicMarkoffs()))
                .collect(Collectors.toList());
    }

    RegisterVotingInEnvelopeMenuItem buildMenuItem(VotingCategoryStatus votingCategoryStatus) {
        return buildMenuItem(votingCategoryStatus, true);
    }
    
    RegisterVotingInEnvelopeMenuItem buildMenuItem(VotingCategoryStatus votingCategoryStatus, boolean hasElectronicMarkoffs) {
        return RegisterVotingInEnvelopeMenuItem.builder()
                .enabled(resolveEnabled())
                .openForRegistration(resolveOpenForRegistration(votingCategoryStatus))
                .notOpenForRegistrationMessage(resolveNotOpenForRegistrationMessage(votingCategoryStatus, !hasElectronicMarkoffs))
                .menuLabel(toMessage(votingCategoryStatus))
                .iconCss(resolveIconCss(votingCategoryStatus.getLocked()))
                .backgroundCss(resolveMenuItemBackgroundCss(votingCategoryStatus))
                .votingCategory(votingCategoryStatus.getVotingCategory())
                .votingPhase(votingCategoryStatus.getVotingPhase())
                .votingController(resolveController(votingCategoryStatus))
                .view(resolveView(votingCategoryStatus))
                .id(resolveDataAftId(votingCategoryStatus))
                .dataAftId(resolveDataAftId(votingCategoryStatus))
                .build();
    }

    private String resolveDataAftId(VotingCategoryStatus votingCategoryStatus) {
        Pattern p = Pattern.compile("\\[(.*?)\\]");
        Matcher m = p.matcher(votingCategoryStatus.getMessageProperty());

        if (m.find()) {
            return m.group(1);
        }

        return "";
    }

    private boolean resolveEnabled() {

        //based on the phase and category
        return true;
    }

    private boolean resolveOpenForRegistration(VotingCategoryStatus votingCategoryStatus) {
        return PRESENT == votingCategoryStatus.getTense();
    }

    private String resolveNotOpenForRegistrationMessage(VotingCategoryStatus status, boolean notElectronicMarkoff) {
        
        if (status.getTense() == FUTURE &&
                notElectronicMarkoff &&
                isElectionDayOrLateRegistrationPhase(status)) {
            return messageProvider.get("@voting.registration.electionDayLateNotStartedForPaperMun", getVotingCategoryStartDate(status));
        }
        
        switch (status.getTense()) {
            case PAST:
                return messageProvider.get("@voting.registration.expired");
            case FUTURE:
                return messageProvider.get("@voting.registration.notStarted", getVotingCategoryStartDate(status));
            default:
                return "";
        }
    }
    
    private boolean isElectionDayOrLateRegistrationPhase(VotingCategoryStatus status) {
        return status.getVotingPhase() == ELECTION_DAY || status.getVotingPhase() == LATE;
    }

    private String getVotingCategoryStartDate(VotingCategoryStatus votingCategoryStatus) {
        return DateUtil.getFormattedShortDate(votingCategoryStatus.getStartingDate());
    }

    private String toMessage(VotingCategoryStatus votingCategoryStatus) {
        return messageProvider.get(votingCategoryStatus.getMessageProperty());
    }

    private String resolveMenuItemBackgroundCss(VotingCategoryStatus votingCategoryStatus) {
        switch (votingCategoryStatus.getTense()) {
            case FUTURE:
                return UI_STATE_NOT_STARTED.getId();
            case PAST:
                return UI_STATE_EXPIRED.getId();
            case PRESENT:
            default:
                return "";
        }
    }

    private VotingController resolveController(VotingCategoryStatus votingCategoryStatus) {
        return isElectionDayVotingCategory(votingCategoryStatus) ?
                beanLookupSingleton.lookup(ElectionDayVotingInEnvelopeCentralController.class) :
                beanLookupSingleton.lookup(AdvanceVotingInEnvelopeCentralController.class);
    }

    private boolean isElectionDayVotingCategory(VotingCategoryStatus votingCategoryStatus) {
        return VotingCategory.isElectionDayVotingCategory(votingCategoryStatus.getVotingCategory());
    }

    private String resolveAdvanceVotingView(VotingCategoryStatus votingCategoryStatus) {
        return isLateArrivalVotingRegistrationPhase(votingCategoryStatus) ? ADVANCE_VOTING_LATE_ARRIVAL_VIEW : ADVANCE_VOTING_IN_ENVELOPE_VIEW;
    }

    private boolean isLateArrivalVotingRegistrationPhase(VotingCategoryStatus votingCategoryStatus) {
        return LATE == votingCategoryStatus.getVotingPhase();
    }

    private String resolveView(VotingCategoryStatus votingCategoryStatus) {
        if (PRESENT != votingCategoryStatus.getTense()) {
            return "registerVotesInEnvelopes";
        }

        return isElectionDayVotingCategory(votingCategoryStatus) ? ELECTION_DAY_VOTING_IN_ENVELOPE_VIEW : resolveAdvanceVotingView(votingCategoryStatus);
    }
}
