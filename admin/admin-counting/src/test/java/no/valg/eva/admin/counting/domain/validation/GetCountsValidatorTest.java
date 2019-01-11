package no.valg.eva.admin.counting.domain.validation;

import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.common.counting.model.CountContext;
import no.valg.eva.admin.common.mockups.MvAreaMockups;
import no.valg.eva.admin.common.mockups.MvElectionMockups;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class GetCountsValidatorTest {

	private GetCountsValidator getCountsValidator;
	
	private CountContext context;
	private AreaPath countingAreaPath;
	private AreaPath userSelectedAreaPath;

	@BeforeMethod
	public void setUp() {
		getCountsValidator = new GetCountsValidator();
		countingAreaPath = AreaPath.from(MvAreaMockups.MV_AREA_PATH_POLLING_DISTRICT);
		userSelectedAreaPath = AreaPath.from(MvAreaMockups.MV_AREA_PATH_MUNICIPALITY);
		ElectionPath contestPath = new ElectionPath(MvElectionMockups.MV_ELECTION_PATH_CONTEST);
		CountCategory category = CountCategory.VO;
		context = new CountContext(contestPath, category);
	}

	@Test
	public void testValid() {
		getCountsValidator.validate(context, countingAreaPath, userSelectedAreaPath);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testNotSameElectionEventIdOnContestAndArea() {
		getCountsValidator.validate(context, AreaPath.from(MvAreaMockups.MV_AREA_PATH_POLLING_DISTRICT_OTHER), userSelectedAreaPath);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testCountingAreaPathNotInUsersAreaPath() {
		getCountsValidator.validate(context, countingAreaPath, AreaPath.from(MvAreaMockups.MV_AREA_PATH_OSLO_MUNICIPALITY));
	}

	
}
