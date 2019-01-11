package no.valg.eva.admin.voting.domain.electoralroll;

import java.util.HashMap;
import java.util.Map;

import no.valg.eva.admin.common.AreaPath;

public final class BoroughIdResolver {

	private BoroughIdResolver() {
	}
	
	private static final String OSLO = AreaPath.OSLO_MUNICIPALITY_ID;

	private static final Map<String, String> POLLING_DISTRICT_TO_BOROUGH_FOR_OSLO = new HashMap();
	static {
		POLLING_DISTRICT_TO_BOROUGH_FOR_OSLO.put("1601", "030104");
		POLLING_DISTRICT_TO_BOROUGH_FOR_OSLO.put("1701", "030107");
		POLLING_DISTRICT_TO_BOROUGH_FOR_OSLO.put("1702", "030108");
	}

	/**
	 * Determines boroughId for voterRecord. PollingDistricts 1601, 1701 and 1702 in Oslo have special rules, else borughId is given by municipality plus first
	 * two digits in polling district id.
	 */
	public static String boroughIdFor(VoterRecord voterRecord) {
		String kommunenr = voterRecord.kommunenr();
		String kretsnummer = voterRecord.valgkrets();
		if (OSLO.equals(kommunenr) && POLLING_DISTRICT_TO_BOROUGH_FOR_OSLO.containsKey(kretsnummer)) {
			return POLLING_DISTRICT_TO_BOROUGH_FOR_OSLO.get(kretsnummer);
		}
		return boroughId(kommunenr, kretsnummer);
	}

	private static String boroughId(String kommunenr, String kretsnummer) {
		return kommunenr.trim() + kretsnummer.substring(0, 2);
	}
}
