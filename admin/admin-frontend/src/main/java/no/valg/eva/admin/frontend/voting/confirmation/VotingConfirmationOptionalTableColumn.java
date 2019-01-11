package no.valg.eva.admin.frontend.voting.confirmation;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum VotingConfirmationOptionalTableColumn {

    VOTING_NUMBER("VOTING_NUMBER", "@voting.number"),
    DATE("DATE", "@common.date"),
    TIME("TIME", "@common.time"),
    VOTING_REGISTERED_BY("VOTING_REGISTERED_BY", "@voting.registeredBy"),
    VOTER_LISTED_IN("VOTER_LISTED_IN", "@voter.listedIn");
    
    private String id;
    private String localizedMsgKey;
}
