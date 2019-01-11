package no.valg.eva.admin.common.counting.model;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import no.valg.eva.admin.common.AreaPath;

import org.testng.annotations.Test;

public class ProtocolAndPreliminaryCountTest {
	private static final int ORDINARY_BALLOT_COUNT = 3;

	@Test
	public void toString_whenNullEmergencyCovers_showNotThrowException() throws Exception {
		AreaPath areaPath = AreaPath.from("111111.11.11.1111.111111");
		String areaName = "areaName";
		String reportingUnitAreaName = "reportingUnitAreaName";
		ProtocolCount protocolCount = new ProtocolCount("PVO1", areaPath, areaName, reportingUnitAreaName, true);
		protocolCount.setSpecialCovers(0);
		protocolCount.setEmergencySpecialCovers(null);
		PreliminaryCount preliminaryCount = new PreliminaryCount("FVO1", areaPath, CountCategory.VO, areaName, reportingUnitAreaName, true);
		ProtocolAndPreliminaryCount protocolAndPreliminaryCount = ProtocolAndPreliminaryCount.from(protocolCount, preliminaryCount);
		protocolAndPreliminaryCount.toString();
	}

	@Test
	public void getProtocolCount_givenProtocolCountAndPreliminaryCount_returnsUpdatedProtocolCount() throws Exception {
		ProtocolCount protocolCount = mock(ProtocolCount.class, RETURNS_DEEP_STUBS);
		PreliminaryCount preliminaryCount = mock(PreliminaryCount.class, RETURNS_DEEP_STUBS);
		ProtocolAndPreliminaryCount protocolAndPreliminaryCount = ProtocolAndPreliminaryCount.from(protocolCount, preliminaryCount);
		when(preliminaryCount.getOrdinaryBallotCount()).thenReturn(ORDINARY_BALLOT_COUNT);

		protocolCount = protocolAndPreliminaryCount.getProtocolCount();

		verify(protocolCount).setOrdinaryBallotCount(ORDINARY_BALLOT_COUNT);
	}
}
