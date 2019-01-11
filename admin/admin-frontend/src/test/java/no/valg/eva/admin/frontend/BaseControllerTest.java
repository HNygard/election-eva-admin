package no.valg.eva.admin.frontend;

import static javax.faces.application.FacesMessage.SEVERITY_ERROR;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collection;

import javax.ejb.EJBException;
import javax.faces.application.FacesMessage;

import no.evote.exception.ErrorCode;
import no.evote.exception.EvoteException;
import no.evote.exception.EvoteSecurityException;
import no.evote.exception.ReadOnlyPrivilegeException;
import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.frontend.common.ErrorCodeHandler;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;



public class BaseControllerTest extends BaseFrontendTest {

	@Test(dataProvider = "getScrollHeight")
	public void getScrollHeight_with_should(int size, int maxHeight, int expectedResult) throws Exception {
		BaseController ctrl = initializeMocks(new MyBaseController());

		assertThat(ctrl.getScrollHeight(getCollection(size), maxHeight)).isEqualTo(expectedResult);
	}

	@DataProvider(name = "getScrollHeight")
	public Object[][] getScrollHeight() {
		return new Object[][] {
				{ 2, 500, 110 },
				{ 10, 500, 500 },
		};
	}

	@Test
	public void process_withNoHandler_returnsErrorMessage() throws Exception {
		BaseController ctrl = initializeMocks(new MyBaseController());

		ctrl.process(new EvoteException("Hello"), null, null);

		assertFacesMessage(SEVERITY_ERROR, "[@common.error.unexpected, 8b1a9953]");
	}

	@DataProvider(name = "handle")
	public Object[][] handle() {
		return new Object[][] {
				{ new EvoteException(""), false },
				{ new EJBException(""), false },
				{ new EvoteException(ErrorCode.ERROR_CODE_0501_OPTIMISTIC_LOCK, null, null), true },
				{ new EvoteException(ErrorCode.ERROR_CODE_0504_STALE_OBJECT, null, null), true },
				{ new EvoteException(ErrorCode.ERROR_CODE_0502_ENTITY_NOT_FOUND, null, null), true }
		};
	}

	@Test(dataProvider = "process_withConstraintViolationDataProvider")
	public void process_withConstraintViolationDataProvider_verifyExpected(String constaintName, String expected) throws Exception {
		BaseController ctrl = initializeMocks(new MyBaseController());
		EvoteException evoteException = new EvoteException(ErrorCode.ERROR_CODE_0503_CONSTRAINT_VIOLATION, null, constaintName);

		ctrl.process(evoteException, null, null);

		assertFacesMessage(FacesMessage.SEVERITY_ERROR, expected);
	}

	@DataProvider(name = "process_withConstraintViolationDataProvider")
	public Object[][] process_withConstraintViolationDataProvider() {
		return new Object[][] {
				{ "xxx", "[@database.error.constraint_violation, xxx]" },
				{ "fk_voting_x_yyy", "@common.message.voting_constraint_error" },
				{ "fk_vote_count_x_yyy", "@common.message.vote_count_constraint_error" },
				{ "fk_ballot_count_x_yyy", "@common.message.vote_count_constraint_error" },
				{ "fk_voter_x_yyy", "@common.message.voter_constraint_error" },
				{ "fk_affiliation_vote_count_x_affiliation", "@common.message.affiliation_count_constraint_error" }

		};
	}

	@Test
	public void process_withReadOnlyPrivilegeException_addsNotWriteableAccessMessage() throws Exception {
		BaseController ctrl = initializeMocks(new MyBaseController());
		ReadOnlyPrivilegeException e = new ReadOnlyPrivilegeException("@error");

		ctrl.process(e, null, null);

		assertFacesMessage(FacesMessage.SEVERITY_ERROR, "@common.message.evote_application_exception.READ_ONLY_PRIVILEGE");
	}

	@Test
	public void process_withEvoteSecurityException_addsSecurityMessage() throws Exception {
		BaseController ctrl = initializeMocks(new MyBaseController());
		EvoteSecurityException e = new EvoteSecurityException("@error");

		ctrl.process(e, null, null);

		assertFacesMessage(FacesMessage.SEVERITY_ERROR, "@common.message.evote_application_exception.SECURITY");
	}

	@Test(dataProvider = "addRedirect")
	public void addRedirect_withDataProvider_verifyExpected(String url, String expected) throws Exception {
		BaseController ctrl = initializeMocks(new MyBaseController());

		assertThat(ctrl.addRedirect(url)).isEqualTo(expected);
	}

	@DataProvider(name = "addRedirect")
	public Object[][] addRedirect() {
		return new Object[][] {
				{ "test.html", "test.html?faces-redirect=true" },
				{ "test.html?a=b", "test.html?a=b&faces-redirect=true" },
				{ "test.html?faces-redirect=true", "test.html?faces-redirect=true" }
		};
	}

	@Test(dataProvider = "isOptimisticLockingException")
	public void isOptimisticLockingException_withDataProvider_verifyExpected(ErrorCode errorCode, boolean expected) throws Exception {
		BaseController ctrl = initializeMocks(new MyBaseController());

		assertThat(ctrl.isOptimisticLockingException(errorCode)).isEqualTo(expected);
	}

	@DataProvider(name = "isOptimisticLockingException")
	public Object[][] isOptimisticLockingException() {
		return new Object[][] {
				{ ErrorCode.ERROR_CODE_0500_UNEXPECTED, false },
				{ ErrorCode.ERROR_CODE_0501_OPTIMISTIC_LOCK, true },
				{ ErrorCode.ERROR_CODE_0502_ENTITY_NOT_FOUND, true },
				{ ErrorCode.ERROR_CODE_0504_STALE_OBJECT, true }
		};
	}

	@Test(dataProvider = "getRequestParameter")
	public void getRequestParameter_withDataProvider_verifyExpected(String key, String[] values, String expected) throws Exception {
		BaseController ctrl = initializeMocks(new MyBaseController());
		if (values == null) {
			getServletContainer().setRequestParameter(key, null);
		} else {
			for (String val : values) {
				getServletContainer().setRequestParameter(key, val);
			}
		}
		assertThat(ctrl.getRequestParameter(key)).isEqualTo(expected);
	}

	@DataProvider(name = "getRequestParameter")
	public Object[][] getRequestParameter() {
		return new Object[][] {
				{ "a", null, null },
				{ "a", new String[] { "b" }, "b" },
				{ "a", new String[] { "a", "b" }, "a" }
		};
	}

	private Collection getCollection(int size) {
		return Arrays.asList(new Object[size]);
	}

	public class MyBaseController extends BaseController {
	}

	public class MyOptimisticLockingExceptionHandler implements ErrorCodeHandler {
		private boolean called;

		@Override
		public String onError(ErrorCode errorCode, String... params) {
			called = true;
			return "@yes";
		}

		public boolean isCalled() {
			return called;
		}
	}
}

