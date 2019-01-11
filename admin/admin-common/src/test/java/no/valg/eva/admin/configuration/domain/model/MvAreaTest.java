package no.valg.eva.admin.configuration.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import no.evote.constants.AreaLevelEnum;
import no.evote.constants.PollingPlaceType;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.test.MockUtilsTestCase;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class MvAreaTest extends MockUtilsTestCase {

	private static final String TO_VIEW_OBJECT_TEST_BASE_BASE = "201301.47.03.0301.030100";

	@Test(dataProvider = "toViewObject")
	public void toViewObject_withDataProvider_verifyExpected(String path, PollingPlaceType type) throws Exception {
		MvArea mvArea = new MvArea();
		AreaPath areaPath = AreaPath.from(path);
		mvArea.setAreaPath(path);
		mvArea.setAreaLevel(areaPath.getLevel().getLevel());
		mvArea.setBoroughName(path);
		mvArea.setPollingDistrictName(path);
		mvArea.setPollingPlaceName(path);
		if (areaPath.getLevel().getLevel() >= AreaLevelEnum.POLLING_DISTRICT.getLevel()) {
			mvArea.setPollingDistrict(createMock(PollingDistrict.class));
		}

		assertThat(mvArea.toViewObject().getPollingPlaceType()).isEqualTo(type);
	}

	@DataProvider(name = "toViewObject")
	public Object[][] toViewObject() {
		return new Object[][] {
				{ TO_VIEW_OBJECT_TEST_BASE_BASE, PollingPlaceType.NOT_APPLICABLE },
				{ TO_VIEW_OBJECT_TEST_BASE_BASE + ".0000", PollingPlaceType.ADVANCE_VOTING },
				{ TO_VIEW_OBJECT_TEST_BASE_BASE + ".0000.1111", PollingPlaceType.ADVANCE_VOTING },
				{ TO_VIEW_OBJECT_TEST_BASE_BASE + ".1111", PollingPlaceType.ELECTION_DAY_VOTING },
				{ TO_VIEW_OBJECT_TEST_BASE_BASE + ".1111.1111", PollingPlaceType.ELECTION_DAY_VOTING }
		};
	}

}
