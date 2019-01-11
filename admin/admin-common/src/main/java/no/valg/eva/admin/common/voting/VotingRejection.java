package no.valg.eva.admin.common.voting;

import lombok.Getter;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public enum VotingRejection {

    VOTING_WRONGLY_REGISTERED_ADVANCE_F0("F0"),
    VOTER_NOT_IN_MUNICIPALITY_ELECTORAL_ROLL_FA("FA"),
    VOTING_HAS_INCOMPLETE_VOTER_INFO_FB("FB"),
    VOTING_NOT_DELIVERED_AT_CORRECT_TIME_FC("FC"),
    VOTING_NOT_DELIVERED_TO_CORRECT_RECEIVER_FD("FD"),
    VOTING_ENVELOPE_OPENED_OR_ATTEMPTED_OPENED_FE("FE"),
    VOTER_ALREADY_VOTED_FF("FF"),
    VOTING_RECEIVED_TOO_LATE_FG("FG"),
    VOTER_NOT_IN_MUNICIPALITY_ELECTORAL_ROLL_FH("FH"),
    VOTER_ALREADY_VOTED_FI("FI"),
    VOTING_WRONGLY_REGISTERED_ELECTION_DAY_V0("V0"),
    VOTER_NOT_IN_MUNICIPALITY_ELECTORAL_ROLL_ELECTION_DAY_VA("VA"),
    VOTER_DID_NOT_HAVE_OPPORTUNITY_TO_VOTE_VC("VC");

    private static Map<String, VotingRejection> rejectionMap;

    private static final String REJECTION_PREFIX_ADVANCE_VOTING = "F";
    private static final String REJECTION_PREFIX_ELECTION_DAY_VOTING = "V";

    @Getter
    private String id;

    static {
        rejectionMap = Arrays.stream(VotingRejection.values())
                .collect(Collectors.toMap(VotingRejection::getId, e -> e));
    }

    VotingRejection(String id) {
        this.id = id;
    }

    public static List<VotingRejection> fromVotingCategory(VotingCategory votingCategory) {
        switch (votingCategory) {
            case VO:
            case VF:
            case VS:
            case VB:
                return electionDayVotingRejections();
            case FI:
            case FU:
            case FA:
            case FB:
            case FE:
                return advanceVotingRejections();
            default:
                throw new IllegalStateException();
        }
    }

    public static VotingRejection getById(String votingRejectionId) {
        if (rejectionMap.get(votingRejectionId) == null) {
            throw new IllegalArgumentException("No VotingRejection found for id: " + votingRejectionId);
        }

        return rejectionMap.get(votingRejectionId);
    }

    private static List<VotingRejection> electionDayVotingRejections() {
        return votingRejections(votingRejectionPredicate(REJECTION_PREFIX_ELECTION_DAY_VOTING));
    }

    private static List<VotingRejection> votingRejections(Predicate<VotingRejection> predicate) {
        return EnumSet.allOf(VotingRejection.class).stream()
                .filter(predicate)
                .collect(Collectors.toList());
    }

    private static Predicate<VotingRejection> votingRejectionPredicate(String rejectionIdPrefix) {
        return votingRejection -> votingRejection.getId().startsWith(rejectionIdPrefix);
    }

    public boolean isAdvanceRejection() {
        return advanceVotingRejections().contains(this);
    }

    public boolean isElectionDayRejection() {
        return electionDayVotingRejections().contains(this);
    }

    private static List<VotingRejection> advanceVotingRejections() {
        return votingRejections(votingRejectionPredicate(REJECTION_PREFIX_ADVANCE_VOTING));
    }
}
