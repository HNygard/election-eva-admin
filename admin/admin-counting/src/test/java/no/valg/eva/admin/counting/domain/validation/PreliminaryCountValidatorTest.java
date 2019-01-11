package no.valg.eva.admin.counting.domain.validation;

import static no.valg.eva.admin.common.counting.model.CountCategory.VO;

import no.evote.exception.ValidateException;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.counting.constants.CountingMode;
import no.valg.eva.admin.common.counting.constants.ReportingUnitTypeId;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.common.counting.model.CountContext;
import no.valg.eva.admin.common.counting.model.PreliminaryCount;
import no.valg.eva.admin.common.mockups.MunicipalityMockups;
import no.valg.eva.admin.common.mockups.MvAreaMockups;
import no.valg.eva.admin.common.mockups.MvElectionMockups;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.MvArea;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class PreliminaryCountValidatorTest {

	private PreliminaryCountValidator preliminaryCountValidator;
	private MvArea area;
	private PreliminaryCount preliminaryCount;
	private CountContext context;

	@BeforeMethod
	public void setUp() {
		preliminaryCountValidator = new PreliminaryCountValidator();
		Municipality municipality = MunicipalityMockups.municipality(false);
		area = MvAreaMockups.pollingDistrictMvArea(municipality);
		preliminaryCount = new PreliminaryCount("FVO1", new AreaPath(MvAreaMockups.MV_AREA_PATH_POLLING_DISTRICT), VO, "", "", false);
		context = getCountContext(VO);
	}

	@Test
	public void testValid() {
		preliminaryCountValidator.applyValidationRules(preliminaryCount, context, area, CountingMode.CENTRAL, ReportingUnitTypeId.VALGSTYRET);
	}

	@Test(expectedExceptions = ValidateException.class)
	public void testInvalidCountModeForValgstyret() {
		preliminaryCountValidator.applyValidationRules(preliminaryCount, context, area, CountingMode.BY_POLLING_DISTRICT, ReportingUnitTypeId.VALGSTYRET);
	}

	@Test(expectedExceptions = ValidateException.class)
	public void testInvalidCountModeForStemmestyret() {
		preliminaryCountValidator.applyValidationRules(preliminaryCount, context, area, CountingMode.CENTRAL, ReportingUnitTypeId.STEMMESTYRET);
	}

	private CountContext getCountContext(CountCategory countCategory) {
		return new CountContext(new ElectionPath(MvElectionMockups.MV_ELECTION_PATH_CONTEST), countCategory);
	}

}
