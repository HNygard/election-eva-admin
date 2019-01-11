package no.valg.eva.admin.frontend.rbac.ctrls;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;


public class RbacViewTest {

	@Test(dataProvider = "getTemplate")
	public void getTemplate_withDataProvider_verifyExpected(RbacView view, String expected) throws Exception {
		assertThat(view.getTemplate()).isEqualTo(expected);
	}

	@Test(dataProvider = "isNewMode")
	public void isNewMode_withDataProvider_verifyExpected(RbacView view, boolean expected) throws Exception {
		assertThat(view.isNewMode()).isEqualTo(expected);
	}

	@DataProvider(name = "getTemplate")
	public Object[][] getTemplate() {
		return new Object[][] {
				{ RbacView.EDIT, "operatorEdit.xhtml" },
				{ RbacView.SEARCH, "operatorSearch.xhtml" },
				{ RbacView.NEW_FROM_EXISTING_VOTER, "operatorEdit.xhtml" },
				{ RbacView.NEW, "operatorEdit.xhtml" },
				{ RbacView.CREATED, "operatorCreated.xhtml" },
				{ RbacView.ELECTORAL_ROLL_LIST, "electoralRollList.xhtml" },
				{ RbacView.EXISTING, "existingOperator.xhtml" },
				{ RbacView.LIST, "operatorList.xhtml" },
				{ RbacView.NEW_FROM_ELECTORAL_ROLL, "operatorEdit.xhtml" },
		};
	}

	@DataProvider(name = "isNewMode")
	public Object[][] isNewMode() {
		return new Object[][] {
				{ RbacView.EDIT, false },
				{ RbacView.SEARCH, false },
				{ RbacView.NEW_FROM_EXISTING_VOTER, true },
				{ RbacView.NEW, true },
				{ RbacView.CREATED, false },
				{ RbacView.ELECTORAL_ROLL_LIST, false },
				{ RbacView.EXISTING, false },
				{ RbacView.LIST, false },
				{ RbacView.NEW_FROM_ELECTORAL_ROLL, true },
		};
	}
}
