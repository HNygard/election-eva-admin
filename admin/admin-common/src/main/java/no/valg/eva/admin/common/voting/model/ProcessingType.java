package no.valg.eva.admin.common.voting.model;

public enum ProcessingType {

    SUGGESTED_REJECTED,
    SUGGESTED_APPROVED;

    public String getId() {
        return name();
    }

    public String getDisplayName() {
        if (this == ProcessingType.SUGGESTED_APPROVED) {
            return "@voting.confirmation.processing.type.suggestedApproved";
        } else if (this == ProcessingType.SUGGESTED_REJECTED) {
            return "@voting.confirmation.processing.type.suggestedRejected";
        }

        return null;
    }

}
