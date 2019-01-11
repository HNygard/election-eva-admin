package no.valg.eva.admin.frontend.voting.ctrls;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import no.valg.eva.admin.common.voting.model.ProcessingType;
import no.valg.eva.admin.common.voting.model.VoterDto;
import no.valg.eva.admin.common.voting.model.VotingDto;
import no.valg.eva.admin.configuration.domain.model.ElectionGroup;
import no.valg.eva.admin.configuration.domain.model.VotingCategory;
import no.valg.eva.admin.frontend.common.search.Searchable;
import no.valg.eva.admin.util.DateUtil;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

import static java.util.Arrays.asList;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class VotingViewModel implements Serializable, Searchable {

    private static final long serialVersionUID = 3681588384905769954L;

    @EqualsAndHashCode.Include
    private String personId;
    private String firstName;
    private String middleName;
    private String lastName;
    private String nameLine;
    private VoterDto voter;
    private String votingDate;
    private String votingTime;
    private String votingRegisteredBy;
    private String voterListedIn;
    private int votingNumber;
    private String votingNumberDisplay;
    private boolean suggestedRejected;
    private VotingCategory votingCategory;
    private ElectionGroup electionGroup;
    private LocalDateTime castTimestamp;
    private LocalDateTime receivedTimestamp;
    private LocalDateTime validationTimestamp;
    private String ballotBoxId;
    private String suggestedProcessing;
    private boolean lateValidation;
    private String status;
    private String rejectionReason;
    private String suggestedRejectionReason;
    private ProcessingType suggestedProcessingType;

    boolean isSuggestedApproved() {
        return !isSuggestedRejected();
    }

    public String getPersonId() {
        return blankIfFictitious(personId);
    }

    private String blankIfFictitious(String value) {
        if (voter == null) {
            return null;
        }
        return voter.isFictitious() ? "" : value;
    }

    public String getFirstName() {
        return blankIfFictitious(firstName);
    }

    public String getMiddleName() {
        return blankIfFictitious(middleName);
    }

    public String getLastName() {
        return blankIfFictitious(lastName);
    }

    public String getFullName() {
        return blankIfFictitious(nameLine);
    }

    @Override
    public List<String> getSearchableProperties() {
        return asList("firstName", "middleName", "lastName", "votingNumber");
    }

    static VotingDto toVotingDto(VotingViewModel votingViewModel) {
        if (votingViewModel == null) {
            return null;
        }

        return VotingDto.builder()
                .votingNumber(votingViewModel.getVotingNumber())
                .voterDto(votingViewModel.getVoter())
                .electionGroup(votingViewModel.getElectionGroup())
                .votingCategory(votingViewModel.getVotingCategory())
                .build();
    }

    static VotingViewModel votingViewModel(final VotingDto votingDto) {
        final VoterDto voterDto = votingDto.getVoterDto();
        return VotingViewModel.builder()
                .voter(voterDto)
                .nameLine(voterDto.getNameLine())
                .firstName(voterDto.getFirstName())
                .middleName(voterDto.getMiddleName())
                .lastName(voterDto.getLastName())
                .personId(voterDto.getId())
                .votingDate(DateUtil.formatToShortDate(votingDto.getCastTimestamp()))
                .votingTime(DateUtil.formatToShortTime(votingDto.getCastTimestamp()))
                .receivedTimestamp(votingDto.getReceivedTimestamp())
                .castTimestamp(votingDto.getCastTimestamp())
                .validationTimestamp(votingDto.getValidationTimestamp())
                .votingCategory(votingDto.getVotingCategory())
                .votingNumber(votingDto.getVotingNumber())
                .votingNumberDisplay(votingDto.getVotingNumberDisplay())
                .votingRegisteredBy(votingDto.getVoteReceiverName())
                .suggestedRejected(votingDto.isSuggestedApproved())
                .suggestedRejectionReason(votingDto.getSuggestedProcessing())
                .suggestedProcessing(votingDto.getSuggestedProcessing())
                .voterListedIn(voterDto.getMvArea().getMunicipalityName())
                .ballotBoxId(votingDto.getBallotBoxId())
                .lateValidation(votingDto.isLateValidation())
                .status(votingStatus(votingDto))
                .rejectionReason(rejectionReason(votingDto))
                .electionGroup(votingDto.getElectionGroup())
                .suggestedProcessingType(votingDto.getSuggestedProcessingType())
                .build();
    }

    private static String rejectionReason(VotingDto votingDto) {
        return votingDto.getVotingRejectionDto() != null ? votingDto.getVotingRejectionDto().getName() : "";
    }

    private static String votingStatus(VotingDto voting) {
        if (voting.getValidationTimestamp() != null) {
            if (voting.isApproved()) {
                return "@voting.confirmation.status.approved";
            } else {
                return "@voting.confirmation.status.rejected";
            }
        }

        return null;
    }

    public org.joda.time.LocalDateTime getReceivedTimeStampAsJodaTime() {
        return DateUtil.toJodaLocalDateTime(receivedTimestamp);
    }

    public org.joda.time.LocalDateTime getValidationTimeStampAsJodaTime() {
        return DateUtil.toJodaLocalDateTime(validationTimestamp);
    }

    public org.joda.time.LocalDateTime getCastTimeStampAsJodaTime() {
        return DateUtil.toJodaLocalDateTime(castTimestamp);
    }

}
