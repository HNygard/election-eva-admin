package no.valg.eva.admin.counting.domain.validation;

import static no.valg.eva.admin.common.counting.model.CountCategory.VO;

import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.counting.constants.CountingMode;
import no.valg.eva.admin.common.counting.constants.ReportingUnitTypeId;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.common.counting.model.CountContext;
import no.valg.eva.admin.common.counting.model.FinalCount;
import no.valg.eva.admin.common.mockups.MunicipalityMockups;
import no.valg.eva.admin.common.mockups.MvAreaMockups;
import no.valg.eva.admin.common.mockups.MvElectionMockups;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.MvArea;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class FinalCountValidatorTest {

	private MvArea area;
	private FinalCount finalCount;
	private CountContext context;

	@BeforeMethod
	public void setUp() {
		Municipality municipality = MunicipalityMockups.municipality(false);
		area = MvAreaMockups.pollingDistrictMvArea(municipality);
		finalCount = new FinalCount("EVO1", new AreaPath(MvAreaMockups.MV_AREA_PATH_POLLING_DISTRICT), VO, "", ReportingUnitTypeId.FYLKESVALGSTYRET, "", false);
		context = getCountContext(VO);
	}

	@Test
	public void testValidStemmestyret() {
		CountingMode countingMode = null;
		ReportingUnitTypeId typeId = ReportingUnitTypeId.STEMMESTYRET;
		new FinalCountValidator().applyValidationRules(finalCount, context, area, countingMode, typeId);
	}

	@Test
	public void testValidValgstyret() {
		CountingMode countingMode = CountingMode.CENTRAL;
		ReportingUnitTypeId typeId = ReportingUnitTypeId.VALGSTYRET;
		new FinalCountValidator().applyValidationRules(finalCount, context, area, countingMode, typeId);
	}

	private CountContext getCountContext(CountCategory countCategory) {
		return new CountContext(new ElectionPath(MvElectionMockups.MV_ELECTION_PATH_CONTEST), countCategory);
	}
}
