package no.valg.eva.admin.common.voting.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import no.valg.eva.admin.configuration.domain.model.ElectionGroup;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.PollingPlace;
import no.valg.eva.admin.configuration.domain.model.VotingCategory;
import no.valg.eva.admin.util.DateUtil;

import java.io.Serializable;
import java.time.LocalDateTime;

@Builder
@Getter
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class VotingDto implements Serializable {

    private static final long serialVersionUID = -5057999776413476641L;

    @EqualsAndHashCode.Include
    @ToString.Include
    private Integer votingNumber;
    private MvArea mvArea;
    private VotingCategory votingCategory;
    private VotingRejectionDto votingRejectionDto;
    private VotingRejectionDto suggestedVotingRejectionDto;
    private VoterDto voterDto;
    private PollingPlace pollingPlace;
    private ElectionGroup electionGroup;
    private LocalDateTime castTimestamp;
    private LocalDateTime receivedTimestamp;
    private LocalDateTime validationTimestamp;
    private boolean approved;
    private String removalRequest;
    private boolean lateValidation;
    private String ballotBoxId;
    private String suggestedProcessing;
    private String voteReceiverName;
    private boolean suggestedApproved;
    private ProcessingType suggestedProcessingType;

    public org.joda.time.LocalDateTime getReceivedTimeStampAsJodaTime() {
        return DateUtil.toJodaLocalDateTime(receivedTimestamp);
    }

    public org.joda.time.LocalDateTime getValidationTimeStampAsJodaTime() {
        return DateUtil.toJodaLocalDateTime(validationTimestamp);
    }

    public org.joda.time.LocalDateTime getCastTimeStampAsJodaTime() {
        return DateUtil.toJodaLocalDateTime(castTimestamp);
    }

    public boolean isEarlyVoting() {
        return votingCategory != null && votingCategory.isEarlyVoting();
    }

    public String getVotingNumberDisplay() {
        return votingNumber != null ? String.format("%s-%d",votingCategory.getId(), votingNumber) : "Rett i urne";
    }
}
