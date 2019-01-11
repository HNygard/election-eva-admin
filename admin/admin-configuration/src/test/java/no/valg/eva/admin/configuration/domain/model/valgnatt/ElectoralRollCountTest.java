package no.valg.eva.admin.configuration.domain.model.valgnatt;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigInteger;

import org.testng.annotations.Test;

public class ElectoralRollCountTest {

	private static final Long PARENT_POLLING_DISTRICT_PK = 1L;
	private static final Long CHILD_POLLING_DISTRICT_PK = 2L;
	private static final String PARENT_POLLING_DISTRICT_ID = "0005";
	private static final String CHILD_POLLING_DISTRICT_ID = "0001";
	private static final int VOTER_TOTAL = 3;
	private static final String PARENT_POLLING_DISTRICT_NAME = "Stemmekrets";
	private static final String MUNICIPALITY_ID = "0101";
	private static final String MUNICIPALITY_NAME = "Halden";
	private static final String EMPTY = "";
	private static final String VALGDISTRIKT_ID = "0101";
	private static final String VALGDISTRIKT_NAME = "Halden";

	@Test
	public void add_addsTogetherVotersAndKeepsParentKeyData() {
		ElectoralRollCount parent = ElectoralRollCount.emptyInstance(PARENT_POLLING_DISTRICT_PK, PARENT_POLLING_DISTRICT_ID,
				PARENT_POLLING_DISTRICT_NAME, MUNICIPALITY_ID, MUNICIPALITY_NAME, EMPTY, EMPTY, EMPTY, EMPTY, VALGDISTRIKT_ID, VALGDISTRIKT_NAME, null);

		ElectoralRollCount result = parent.add(makeElectoralRollForPollingDistrict());

		assertThat(result.getPollingDistrictPk()).isEqualTo(PARENT_POLLING_DISTRICT_PK);
		assertThat(result.getPollingDistrictId()).isEqualTo(PARENT_POLLING_DISTRICT_ID);
		assertThat(result.getPollingDistrictName()).isEqualTo(PARENT_POLLING_DISTRICT_NAME);
		assertThat(result.getVoterTotal()).isEqualTo(VOTER_TOTAL);
		assertThat(result.getMunicipalityId()).isEqualTo(MUNICIPALITY_ID);
		assertThat(result.getMunicipalityName()).isEqualTo(MUNICIPALITY_NAME);
		assertThat(result.getValgdistriktId()).isEqualTo(VALGDISTRIKT_ID);
		assertThat(result.getValgdistriktName()).isEqualTo(VALGDISTRIKT_NAME);
	}

	private ElectoralRollCount makeElectoralRollForPollingDistrict() {
		return new ElectoralRollCount(
				MUNICIPALITY_ID, MUNICIPALITY_NAME, "Østfold", "01", CHILD_POLLING_DISTRICT_ID, "Hele kommunen", BigInteger.valueOf(VOTER_TOTAL), false,
				CHILD_POLLING_DISTRICT_PK.intValue(), EMPTY, EMPTY, EMPTY, EMPTY, null);
	}
}
