package no.valg.eva.admin.counting.domain.validation;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import no.evote.exception.ValidateException;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.counting.constants.CountingMode;
import no.valg.eva.admin.common.counting.model.DailyMarkOffCount;
import no.valg.eva.admin.common.counting.model.DailyMarkOffCounts;
import no.valg.eva.admin.common.counting.model.ProtocolCount;
import no.valg.eva.admin.common.mockups.MunicipalityMockups;
import no.valg.eva.admin.common.mockups.MvAreaMockups;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.MvArea;

import org.joda.time.LocalDate;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;


public class ProtocolCountValidatorTest {

	private ProtocolCountValidator protocolCountValidator;
	private Municipality municipality;
	private MvArea area;
	private ProtocolCount protocolCount;

	@BeforeMethod
	public void setUp() {
		protocolCountValidator = new ProtocolCountValidator();
		municipality = MunicipalityMockups.municipality(false);
		area = MvAreaMockups.pollingDistrictMvArea(municipality);
		protocolCount = new ProtocolCount("PVO1", new AreaPath(MvAreaMockups.MV_AREA_PATH_POLLING_DISTRICT), "", "", true);
	}

	@Test
	public void testValid() {
		protocolCountValidator.applyValidationRules(protocolCount, null, area, CountingMode.CENTRAL, null);
	}

	@Test(expectedExceptions = ValidateException.class, expectedExceptionsMessageRegExp = "expected the MvArea area level to be.*")
	public void testAreaNotPollingDistrict() {
		protocolCountValidator.applyValidationRules(protocolCount, null, MvAreaMockups.municipalityMvArea(municipality), CountingMode.CENTRAL, null);
	}

	@Test(expectedExceptions = ValidateException.class, expectedExceptionsMessageRegExp = "Protocol count should be required for municipality with id.*")
	public void testAreaWithMunicipalityNotRequiringProtocolCount() {
		MvArea area = mock(MvArea.class, RETURNS_DEEP_STUBS);
		when(area.getMunicipality().isRequiredProtocolCount()).thenReturn(false);
		
		protocolCountValidator.applyValidationRules(protocolCount, null, area, CountingMode.CENTRAL, null);
	}

	@Test(expectedExceptions = ValidateException.class, expectedExceptionsMessageRegExp = "illegal count mode for protocol count.*")
	public void testInvalidCountMode() {
		protocolCountValidator.applyValidationRules(protocolCount, null, area, CountingMode.BY_TECHNICAL_POLLING_DISTRICT, null);
	}

	@Test(expectedExceptions = ValidateException.class, expectedExceptionsMessageRegExp = "Cannot contain ballot count for other contests.*")
	public void municipalityContest_withBallotCountForOtherContests_isInvalid() {
		protocolCount.setBallotCountForOtherContests(100);

		protocolCountValidator.applyValidationRules(protocolCount, null, area, CountingMode.CENTRAL, null);
	}

	@Test(expectedExceptions = ValidateException.class, expectedExceptionsMessageRegExp = "Ballot count for other contests is required.*")
	public void boroughContest_withoutBallotCountForOtherContests_isInvalid() {
		protocolCount.setDailyMarkOffCountsForOtherContests(getDailyMarkOffCounts(asList(new DailyMarkOffCount(new LocalDate(2014, 1, 1)))));

		protocolCountValidator.applyValidationRules(protocolCount, null, area, CountingMode.CENTRAL, null);
	}

	@Test
	public void boroughContest_withBallotCountForOtherContests_isValid() {
		protocolCount.setDailyMarkOffCountsForOtherContests(getDailyMarkOffCounts(asList(new DailyMarkOffCount(new LocalDate(2014, 1, 1)))));
		protocolCount.setBallotCountForOtherContests(100);

		protocolCountValidator.applyValidationRules(protocolCount, null, area, CountingMode.CENTRAL, null);
	}
	
	private DailyMarkOffCounts getDailyMarkOffCounts(List<DailyMarkOffCount> dailyMarkOffCountList) {
		return new DailyMarkOffCounts(dailyMarkOffCountList);
	}
}

