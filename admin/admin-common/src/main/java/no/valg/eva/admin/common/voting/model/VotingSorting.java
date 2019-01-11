package no.valg.eva.admin.common.voting.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

import static lombok.AccessLevel.PRIVATE;

@Builder
@Getter
@ToString
@NoArgsConstructor(access = PRIVATE)
@AllArgsConstructor
@EqualsAndHashCode
public class VotingSorting implements Serializable {

    private static final long serialVersionUID = 8097197871378090452L;
    private String sortField;
    private String sortOrder;

    public boolean isSortBySuggestedProcessing() {
        return sortFieldEquals("suggestedRejectionReason");
    }

    public boolean isSortByPersonId() {
        return sortFieldEquals("personId");
    }
    
    public boolean isSortByNameLine(){
        return sortFieldEquals("nameLine");
    }
    
    public boolean isSortByVotingCategory(){
        return sortFieldEquals("votingCategory.name");
    }

    public boolean isSortByVotingStatus() {
        return sortFieldEquals("status");
    }

    public boolean isSortByRejectionReason() {
        return sortFieldEquals("rejectionReason");
    }

    private boolean sortFieldEquals(String field) {
        return field.equalsIgnoreCase(getSortField());
    }
}
