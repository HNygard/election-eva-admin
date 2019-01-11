package no.valg.eva.admin.configuration.domain.service;

import static no.valg.eva.admin.common.Process.COUNTING;
import static no.valg.eva.admin.common.Process.FORECASTING;
import static no.valg.eva.admin.common.Process.LOCAL_CONFIGURATION;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import no.evote.security.SecurityLevel;
import no.evote.security.UserData;
import no.valg.eva.admin.common.Process;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.factory.ElectionFilterFactory;
import no.valg.eva.admin.configuration.domain.model.filter.ElectionFilterEnum;
import no.valg.eva.admin.rbac.domain.model.Operator;
import no.valg.eva.admin.rbac.domain.model.OperatorRole;
import no.valg.eva.admin.test.MockUtilsTestCase;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class ElectionFilterFactoryTest extends MockUtilsTestCase {

	private static final long ELECTION_EVENT_PK = 1234L;

	@Test(dataProvider = "electionFilterFactoryTestData")
	public void build_choosesAppropriateFilter(UserData userData, CountCategory countCategory, Process process, ElectionFilterEnum expected) throws Exception {

		Optional<ElectionFilterEnum> filter = ElectionFilterFactory.build(userData, countCategory, process);
		
		if (expected == null) {
			assertThat(filter).isEmpty();
		} else {
			assertThat(filter).contains(expected);
		}
	}

	@DataProvider(name = "electionFilterFactoryTestData")
	public Object[][] electionFilterFactoryTestData() throws Exception {
		return new Object[][] {
			{ userData(false), CountCategory.BF, COUNTING, 			 ElectionFilterEnum.FOR_BF },
			{ userData(false), CountCategory.BF, FORECASTING, 		 null },
			{ userData(true),  CountCategory.VO, LOCAL_CONFIGURATION, ElectionFilterEnum.FOR_MUNICIPALITY_LIST_PROPOSALS},
			{ userData(true),  CountCategory.VO, FORECASTING, 		 null },
			{ userData(false), CountCategory.VO, FORECASTING, 		 null }
		};
	}

	private UserData userData(boolean isOnMunicipalityLevel) {
		UserData userData = new UserData(null, SecurityLevel.TWO_FACTOR_OF_WHICH_ONE_DYNAMIC, null, null);
		populateOperatorRole(userData, isOnMunicipalityLevel);
		return userData;
	}

	private void populateOperatorRole(UserData userData, boolean isOnMunicipalityLevel) {
		OperatorRole operatorRole = new OperatorRole();
		operatorRole.setOperator(new Operator());
		operatorRole.getOperator().setElectionEvent(new ElectionEvent(ELECTION_EVENT_PK));
		userData.setOperatorRole(operatorRole);
		userData.getOperatorRole().setMvArea(new MvArea());
		if (isOnMunicipalityLevel) {
			userData.getOperatorRole().getMvArea().setAreaPath("123456.12.12.1234");
		} else {
			userData.getOperatorRole().getMvArea().setAreaPath("123456.12.12");
		}
	}

}
