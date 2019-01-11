package no.valg.eva.admin.frontend.voting.ctrls.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import no.evote.security.UserData;
import no.valg.eva.admin.common.voting.VotingCategory;
import no.valg.eva.admin.common.voting.VotingPhase;
import no.valg.eva.admin.configuration.domain.model.ElectionGroup;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.frontend.voting.ctrls.ConfirmVotingTabs;

import java.time.LocalDateTime;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
@Setter
@EqualsAndHashCode
public class ConfirmVotingViewModel {

    private ElectionGroup electionGroup;

    private MvArea mvArea;

    private UserData userData;

    private VotingCategory votingCategory;

    private VotingPhase votingPhase;
    
    private boolean categoryOpen;

    private LocalDateTime startDate;

    private LocalDateTime endDateIncluding;

    private String requestUrlQueryString;

    private ConfirmVotingTabs.Handler confirmVotingContentHandler;
}
