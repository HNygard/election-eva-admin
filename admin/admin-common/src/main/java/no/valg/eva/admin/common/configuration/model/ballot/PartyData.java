package no.valg.eva.admin.common.configuration.model.ballot;

import static no.evote.constants.EvoteConstants.VALGNATT_PARTY_ID_BLANKE;
import static no.evote.constants.EvoteConstants.VALGNATT_PARTY_NAME_BLANKE;

import java.util.ArrayList;
import java.util.List;

import no.valg.eva.admin.configuration.domain.model.Candidate;

/**
 * Contains data for party - bør flyttes til admin-configuration når Ballot er flyttet
 */
public class PartyData {

	private final String partyId;
	private final String partyName;
	private final String partyCategoryId;
	private final Integer affiliationPk;
	private final List<Candidate> candidateList = new ArrayList<>();

	public PartyData(String partyId, String partyName, String partyCategoryId, Integer affiliationPk) {
		this.partyId = partyId;
		this.partyName = partyName;
		this.partyCategoryId = partyCategoryId;
		this.affiliationPk = affiliationPk;
	}

	public void addCandidates(List<Candidate> candidates) {
		candidateList.addAll(candidates);
	}
	
	public List<Candidate> getCandidateList() {
		return candidateList;
	}

	public String getPartyId() {
		return partyId;
	}

	public String getPartyName() {
		return partyName;
	}

	public Integer getAffiliationPk() {
		return affiliationPk;
	}

	public String getPartyCategoryId() {
		return partyCategoryId;
	}

    public static PartyData createBlanke() {
        return new PartyData(VALGNATT_PARTY_ID_BLANKE, VALGNATT_PARTY_NAME_BLANKE, "", null);
    }
}
