package no.valg.eva.admin.frontend.rbac;

import no.evote.exception.EvoteException;
import no.evote.security.UserData;
import no.evote.util.MvAreaBuilder;
import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.common.rbac.ImportOperatorRoleInfo;
import no.valg.eva.admin.common.rbac.PollingPlaceResponsibleOperator;
import no.valg.eva.admin.common.rbac.VoteReceiver;
import no.valg.eva.admin.common.rbac.service.ImportOperatorService;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.util.ExcelUtil;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.faces.application.FacesMessage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class OperatorImportHelperTest extends BaseFrontendTest {

	private OperatorImportHelper operatorImportHelper;

	@BeforeMethod
	public void setUp() throws Exception {
		setUpWith(new OperatorImportHelper());
	}

	private void setUpWith(OperatorImportHelper operatorImportHelper) throws Exception {
		this.operatorImportHelper = initializeMocks(operatorImportHelper);
		MvArea mvArea = new MvAreaBuilder(AREA_PATH_MUNICIPALITY).getValue();
		when(getUserDataMock().getOperatorMvArea()).thenReturn(mvArea);
	}

	@Test
	public void testImportAdvanceVoteReceivers() throws Exception {
		mockEarlyVoteReceiverParserList(2);
		operatorImportHelper.importAdvanceVoteReceivers(getInputStreamWrapper("/EarlyVoteReceivers-oslo-2.xlsx"));
		verify(getInjectMock(ImportOperatorService.class)).importEarlyVoteReceiverOperator(eq(getUserDataMock()),
                argThat(argument -> argument.size() == 2));
	}

	@Test
	public void testImportVoteReceiverAndPollingPlaceResponsibles() throws Exception {
		mockElectionDayOperatorParserList();
		operatorImportHelper.importVoteReceiverAndPollingPlaceResponsibles(getInputStreamWrapper("/pollingPlaceOperators-oslo.xlsx"));
		verify(getInjectMock(ImportOperatorService.class)).importVotingAndPollingPlaceResponsibleOperators(eq(getUserDataMock()),
                argThat(argument -> argument.size() == 2), argThat(argument -> argument.size() == 2));
	}

	@Test
    public void importVoteReceiverAndPollingPlaceResponsibles_withWrongNumberOfColumns_shouldAddInvalidNumberOfColumnsError() {
		try {
			operatorImportHelper.importVoteReceiverAndPollingPlaceResponsibles(getInputStreamWrapper("/pollingPlaceOperators-withWrongNumberOfColumns.xlsx"));
		} catch (SpreadSheetValidationException e) {
			assertThat(e.getMessage()).isEqualTo("Spreadsheet had validation errors: [[@excel.import.invalidNumberOfColumns, 7, 4]]");
		}
	}

	@Test
    public void importVoteReceiverAndPollingPlaceResponsibles_withMissingColumns_throwsSpreadSheetValidationException() {
		try {
			operatorImportHelper.importVoteReceiverAndPollingPlaceResponsibles(getInputStreamWrapper("/pollingPlaceOperators-withMissingColumns.xlsx"));
			AssertJUnit.fail("SpreadSheetValidationException not thrown");
		} catch (SpreadSheetValidationException e) {
			assertThat(e.getMessage()).isEqualTo("Spreadsheet had validation errors: [[@excel.import.invalidColumnName, 1, Fødselsnummer], "
					+ "[@excel.import.invalidColumnName, 2, Fornavn], [@excel.import.invalidColumnName, 3, Etternavn], [@excel.import.invalidColumnName, "
					+ "4, E-post], [@excel.import.invalidColumnName, 5, Mobiltelefon], [@excel.import.invalidColumnName, "
					+ "6, Stemmekrets], [@excel.import.invalidColumnName, 7, Ansvarlig]]");
		}
	}

	@Test
	public void importAdvanceVoteReceivers_withNoInputStream_verifyImportOperatorServiceNeverCalled() throws Exception {
		OperatorImportHelper.InputStreamWrapper wrapperMock = createMock(OperatorImportHelper.InputStreamWrapper.class);
		when(wrapperMock.getWrappedInputStream()).thenReturn(null);

		operatorImportHelper.importAdvanceVoteReceivers(wrapperMock);

		verify(getInjectMock(ImportOperatorService.class), never()).importEarlyVoteReceiverOperator(any(UserData.class), anyList());
	}

	@Test
	public void importVoteReceiverAndPollingPlaceResponsibles_withNoInputStream_verifyImportOperatorServiceNeverCalled() throws Exception {
		OperatorImportHelper.InputStreamWrapper wrapperMock = createMock(OperatorImportHelper.InputStreamWrapper.class);
		when(wrapperMock.getWrappedInputStream()).thenReturn(null);

		operatorImportHelper.importVoteReceiverAndPollingPlaceResponsibles(wrapperMock);

		verify(getInjectMock(ImportOperatorService.class), never()).importVotingAndPollingPlaceResponsibleOperators(any(UserData.class), anyList(), anyList());
	}

	@Test
	public void importAdvanceVoteReceivers_withIOException_verifyErrorMessage() throws Exception {
		OperatorImportHelper.InputStreamWrapper wrapperStub = getInputStreamWrapper(null);

		operatorImportHelper.importAdvanceVoteReceivers(wrapperStub);

		assertFacesMessage(FacesMessage.SEVERITY_ERROR, "IO ERROR");
	}

	@Test
	public void importerTemplateTransform_withIOException_assertFacesMessage() throws Exception {
		setUpWith(new OperatorImportHelper() {
			@Override
            ExcelUtil.RowData getRowDataFromExcelFile(InputStream inputStream) throws IOException {
				throw new IOException("IO ERROR");
			}
		});

		operatorImportHelper.importAdvanceVoteReceivers(getInputStreamWrapper("/EarlyVoteReceivers-oslo-2.xlsx"));

		assertFacesMessage(FacesMessage.SEVERITY_ERROR, "@common.message.exception.general");
	}

	@Test
	public void importerTemplateTransform_withEvoteException_assertFacesMessage() throws Exception {
		setUpWith(new OperatorImportHelper() {
			@Override
            ExcelUtil.RowData getRowDataFromExcelFile(InputStream inputStream) {
				throw new EvoteException("IO ERROR");
			}
		});

		operatorImportHelper.importAdvanceVoteReceivers(getInputStreamWrapper("/EarlyVoteReceivers-oslo-2.xlsx"));

		assertFacesMessage(FacesMessage.SEVERITY_ERROR, "IO ERROR");
	}

	private void mockEarlyVoteReceiverParserList(int size) throws SpreadSheetValidationException {
		List<ImportOperatorRoleInfo> mockedList = new ArrayList<>();
		for (int i = 0; i < size; i++) {
			mockedList.add(mock(ImportOperatorRoleInfo.class));
		}
		when(getInjectMock(EarlyVoteReceiverParser.class).toOperatorList(anyList())).thenReturn(mockedList);
	}

	private void mockElectionDayOperatorParserList() throws SpreadSheetValidationException {
		List<ImportOperatorRoleInfo> mockedList = new ArrayList<>();
		mockedList.add(mock(VoteReceiver.class));
		mockedList.add(mock(VoteReceiver.class));
		mockedList.add(mock(PollingPlaceResponsibleOperator.class));
		mockedList.add(mock(PollingPlaceResponsibleOperator.class));
		when(getInjectMock(ElectionDayOperatorParser.class).toOperatorList(anyList())).thenReturn(mockedList);
	}

	private OperatorImportHelper.InputStreamWrapper getInputStreamWrapper(final String file) {
		return new OperatorImportHelper.InputStreamWrapper() {
			@Override
			public InputStream getInputStream() throws IOException {
				if (file == null) {
					throw new IOException("IO ERROR");
				}
				return getClass().getResourceAsStream(file);
			}
		};
	}

}
