package no.valg.eva.admin.frontend.voting.ctrls;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import no.valg.eva.admin.common.voting.VotingCategory;
import no.valg.eva.admin.common.voting.VotingPhase;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode
public class ConfirmVotingMenuItem {

    @EqualsAndHashCode.Include
    @Getter
    private int id;

    private String backgroundCss;

    private String iconCss = "";

    private String menuLabel;

    private String dataAftId;

    private boolean categoryOpen;

    private String categoryClosedMessage;

    private VotingCategory votingCategory;

    private VotingPhase votingPhase;
    
    private LocalDateTime startDate;
    
    private LocalDateTime endDateIncluding;
}
