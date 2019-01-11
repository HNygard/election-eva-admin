package no.valg.eva.admin.valgnatt.domain.model.grunnlagsdata.candidates;

import java.util.List;
import java.util.Map;

import no.valg.eva.admin.common.configuration.model.ballot.PartyData;

/**
 * Keeps party data pr contest
 */
public class PartyDataHolder {

	private final Map<Long, List<PartyData>> partyDataMap;

	/**
	 * Creates instance.
	 * @param partyDataMap maps contestPk to list of PartyData instances
	 */
	public PartyDataHolder(final Map<Long, List<PartyData>> partyDataMap) {
		this.partyDataMap = partyDataMap;
	}

	public List<PartyData> getPartyData(final Long contestPk) {
		return partyDataMap.get(contestPk);
	}
}
