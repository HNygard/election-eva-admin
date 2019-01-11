package no.valg.eva.admin.frontend.voting.ctrls.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import no.valg.eva.admin.frontend.voting.ctrls.ConfirmVotingTabs;
import no.valg.eva.admin.frontend.voting.ctrls.VotingViewModel;

import java.io.Serializable;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString(of = {"id"})
public class ConfirmVotingContentViewModel implements Serializable {

    private static final long serialVersionUID = -1588042259509879116L;

    @EqualsAndHashCode.Include
    private String id;

    private int numberOfVotingsToConfirm;

    private long numberOfApprovedVotings;

    private long numberOfRejectedVotings;

    private List<VotingViewModel> votingList;

    private List<VotingViewModel> selectedVotingList;
    
    @Setter
    private VotingPeriodViewModel phasePeriod;

    private VotingPeriodViewModel selectedVotingPeriod;

    ConfirmVotingTabs.Handler handler;

}
