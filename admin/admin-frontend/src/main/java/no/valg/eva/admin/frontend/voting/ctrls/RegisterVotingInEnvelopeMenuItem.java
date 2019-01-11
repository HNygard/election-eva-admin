package no.valg.eva.admin.frontend.voting.ctrls;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import no.valg.eva.admin.common.voting.VotingCategory;
import no.valg.eva.admin.common.voting.VotingPhase;


@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterVotingInEnvelopeMenuItem {

    private String id;

    private String dataAftId;

    private boolean enabled;

    private String iconCss;

    private String backgroundCss;

    private VotingCategory votingCategory;

    private VotingPhase votingPhase;

    private String menuLabel;

    private VotingController votingController;

    private String view;

    private boolean openForRegistration;

    private String notOpenForRegistrationMessage;

    public boolean isDisabled() {
        return !enabled;
    }

    public boolean isNotOpenForRegistration() {
        return !openForRegistration;
    }
}

