package no.valg.eva.admin.frontend.voting.ctrls;

import no.evote.security.UserData;
import no.valg.eva.admin.common.voting.model.VoterDto;
import no.valg.eva.admin.common.voting.model.VotingConfirmationStatus;
import no.valg.eva.admin.configuration.domain.model.ElectionGroup;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.frontend.voting.ctrls.VoterConfirmation.VoterConfirmationContext;

import java.util.Arrays;
import java.util.List;

public class VotingConfirmationTestData {

    static VoterConfirmationContext voterConfirmationViewModel(UserData userData, ElectionGroup electionGroup) {
        return voterConfirmationViewModel(userData, electionGroup, null);
    }

    static VoterConfirmationContext voterConfirmationViewModel(UserData userData, ElectionGroup electionGroup,
                                                               VoterConfirmation.Handler handler) {
        return VoterConfirmationContext.builder()
                .userData(userData)
                .electionGroup(electionGroup)
                .voterDto(voter())
                .mvArea(mvArea())
                .handler(handler)
                .mvArea(mvArea())
                .build();
    }

    public static MvArea mvArea() {
        Municipality municipality = Municipality.builder()
                .name("Andeby")
                .electronicMarkoffs(true)
                .build();
        municipality.setPk(1L);

        return MvArea.builder()
                .municipality(municipality)
                .build();
    }

    public static VoterDto voter() {
        return voter(true);
    }

    public static VoterDto voter(boolean approvedForVoting) {
        return VoterDto.builder()
                .id("12483988532")
                .nameLine("Navn Navnesen")
                .approved(approvedForVoting)
                .build();
    }

    static List<VotingConfirmationStatus> votingConfirmationStatusList(VotingConfirmationStatus... categories) {
        return Arrays.asList(categories);
    }
}
