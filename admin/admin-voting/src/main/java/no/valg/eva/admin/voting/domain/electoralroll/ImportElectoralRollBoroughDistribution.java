package no.valg.eva.admin.voting.domain.electoralroll;

import java.util.HashMap;
import java.util.Map;

public class ImportElectoralRollBoroughDistribution {

	private static final String BOROUGH_ID_GAMLE_OSLO = "030101";
	private static final String BOROUGH_ID_GRUNERLOKKA = "030102";
	private static final String BOROUGH_ID_SAGENE = "030103";
	private static final String BOROUGH_ID_ST_HANSHAUGEN = "030104";
	private static final String BOROUGH_ID_FROGNER = "030105";
	private static final String BOROUGH_ID_ULLERN = "030106";
	private static final String BOROUGH_ID_VESTRE_AKER = "030107";
	private static final String BOROUGH_ID_NORDRE_AKER = "030108";
	private static final String BOROUGH_ID_BJERKE = "030109";
	private static final String BOROUGH_ID_GRORUD = "030110";
	private static final String BOROUGH_ID_STOVNER = "030111";
	private static final String BOROUGH_ID_ALNA = "030112";
	private static final String BOROUGH_ID_OSTENSJO = "030113";
	private static final String BOROUGH_ID_NORDSTRAND = "030114";
	private static final String BOROUGH_ID_SONDRE_NORDSTRAND = "030115";

	private static Map<String, String> boroughDistribution = new HashMap<>();
	static {
		boroughDistribution.put("10", BOROUGH_ID_GAMLE_OSLO);
		boroughDistribution.put("23", BOROUGH_ID_GAMLE_OSLO);
		boroughDistribution.put("14", BOROUGH_ID_GRUNERLOKKA);
		boroughDistribution.put("20", BOROUGH_ID_GRUNERLOKKA);
		boroughDistribution.put("08", BOROUGH_ID_SAGENE);
		boroughDistribution.put("28", BOROUGH_ID_SAGENE);
		boroughDistribution.put("03", BOROUGH_ID_ST_HANSHAUGEN);
		boroughDistribution.put("09", BOROUGH_ID_ST_HANSHAUGEN);
		boroughDistribution.put("16", BOROUGH_ID_FROGNER);
		boroughDistribution.put("30", BOROUGH_ID_FROGNER);
		boroughDistribution.put("31", BOROUGH_ID_FROGNER);
		boroughDistribution.put("12", BOROUGH_ID_ULLERN);
		boroughDistribution.put("17", BOROUGH_ID_ULLERN);
		boroughDistribution.put("02", BOROUGH_ID_VESTRE_AKER);
		boroughDistribution.put("19", BOROUGH_ID_VESTRE_AKER);
		boroughDistribution.put("04", BOROUGH_ID_NORDRE_AKER);
		boroughDistribution.put("27", BOROUGH_ID_NORDRE_AKER);
		boroughDistribution.put("22", BOROUGH_ID_BJERKE);
		boroughDistribution.put("24", BOROUGH_ID_BJERKE);
		boroughDistribution.put("01", BOROUGH_ID_GRORUD);
		boroughDistribution.put("18", BOROUGH_ID_GRORUD);
		boroughDistribution.put("21", BOROUGH_ID_STOVNER);
		boroughDistribution.put("29", BOROUGH_ID_STOVNER);
		boroughDistribution.put("06", BOROUGH_ID_ALNA);
		boroughDistribution.put("11", BOROUGH_ID_ALNA);
		boroughDistribution.put("07", BOROUGH_ID_OSTENSJO);
		boroughDistribution.put("25", BOROUGH_ID_OSTENSJO);
		boroughDistribution.put("05", BOROUGH_ID_NORDSTRAND);
		boroughDistribution.put("13", BOROUGH_ID_NORDSTRAND);
		boroughDistribution.put("15", BOROUGH_ID_SONDRE_NORDSTRAND);
		boroughDistribution.put("26", BOROUGH_ID_SONDRE_NORDSTRAND);
	}

	public String findBoroughWithResponsibilityForVoter(String fnr) {
		return boroughDistribution.get(fødselsdagIMåneden(fnr));
	}

	private String fødselsdagIMåneden(String fnr) {
		return fnr.substring(0, 2);
	}
}
