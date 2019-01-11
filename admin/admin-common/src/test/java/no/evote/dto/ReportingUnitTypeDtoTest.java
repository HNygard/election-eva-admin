package no.evote.dto;

import static org.assertj.core.api.Java6Assertions.assertThat;

import org.testng.annotations.Test;

public class ReportingUnitTypeDtoTest {

	private static final int ELECTION_LEVEL_3 = 3;
	private static final String ELECTION_LEVEL_3_NAME = "@election_level[3].name";

	@Test
	public void getElectionLevelName_gir_name_basert_paa_levelVerdi() {
		assertThat(makeReportingUnitTypeDto().getElectionLevelName()).isEqualTo(ELECTION_LEVEL_3_NAME);
	}

	private ReportingUnitTypeDto makeReportingUnitTypeDto() {
		ReportingUnitTypeDto reportingUnitTypeDto = new ReportingUnitTypeDto();
		reportingUnitTypeDto.setElectionLevel(ELECTION_LEVEL_3);
		return reportingUnitTypeDto;
	}
}
