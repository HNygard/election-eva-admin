package no.valg.eva.admin.common.voting;

public enum VotingPhase {
    EARLY,
    ADVANCE,
    ELECTION_DAY,
    LATE;

    public boolean isBefore(VotingPhase otherPhase) {
        return ordinal() < otherPhase.ordinal();
    }
}
