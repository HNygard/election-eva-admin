package no.valg.eva.admin.common.voting.model;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.EnumSet.allOf;

public enum VotingConfirmationStatus {
    TO_BE_CONFIRMED,
    APPROVED,
    REJECTED;

    public String getName() {
        switch (this) {
            case TO_BE_CONFIRMED:
                return "@voting.envelope.overview.heading.toBeConfirmed";
            case APPROVED:
                return "@voting.envelope.overview.heading.approved";
            case REJECTED:
                return "@voting.envelope.overview.heading.rejected";
            default:
                throw new IllegalStateException("Unknown enum value!");
        }
    }

    public boolean isValidatedStatus() {
        if (this == VotingConfirmationStatus.APPROVED || this == VotingConfirmationStatus.REJECTED) {
            return true;
        } else if (this == VotingConfirmationStatus.TO_BE_CONFIRMED) {
            return false;
        } else {
            throw new IllegalStateException("Unknown VotingConfirmationStatus: " + this);
        }
    }

    public static List<VotingConfirmationStatus> votingConfirmationStatuses(boolean validated) {
        return allOf(VotingConfirmationStatus.class).stream()
                .filter(votingConfirmationStatus -> votingConfirmationStatus.isValidatedStatus() == validated)
                .collect(Collectors.toList());
    }
}
