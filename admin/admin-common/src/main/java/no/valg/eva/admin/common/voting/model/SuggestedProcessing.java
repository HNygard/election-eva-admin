package no.valg.eva.admin.common.voting.model;

public enum SuggestedProcessing {
    DEAD,
    APPROVE;

    public String getName() {
        return "@suggested_voting_rejection[" + name() + "].name";
    }
}
