package no.valg.eva.admin.frontend.rbac;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.common.rbac.EarlyVoteReceiver;
import no.valg.eva.admin.common.rbac.ImportOperatorRoleInfo;
import no.valg.eva.admin.util.ExcelUtil;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

/**
 */
public class EarlyVoteReceiverParserTest extends BaseFrontendTest {

	private static final String VALID_FNR = "11058508014";
	private static final String IN_VALID_FNR = "11058508013";
	private static final String VALID_FIRST_NAME = "Kåre";
	private static final String VALID_LAST_NAME = "Olderbolle";
	private static final String VALID_EMAIL = "kare.olderbolle@oslo.kommune.no";
	private static final String VALID_PHONE_NO = "12345678";
	private static final String VALID_POLLING_DISTRICT = "0001";
	private static final String CELL_REFERENCE = "A1";
	private static final String INVALID_EMAIL = "a@";
	private static final String INVALID_PHONE = "12";
	private static final String EMPTY_POLLING_PLACE = "";
	private static final String EMPTY_FIRST_NAME = "";
	private static final String EMPTY_LAST_NAME = "";
	private static final String INVALID_POLLING_PLACE = "01";
	private static final String VALID_PHONE = "12345678";

	private static final Function<String[], List<Pair<String, String>>> STRING_ARRAY_TO_PAIR_LIST_F = new Function<String[], List<Pair<String, String>>>() {
		@Override
		public List<Pair<String, String>> apply(String[] input) {
			return Lists.transform(Lists.newArrayList(input), new Function<String, Pair<String, String>>() {
				@Override
				public Pair<String, String> apply(String input) {
					return new ImmutablePair<>(CELL_REFERENCE, input);
				}
			});
		}
	};

	private EarlyVoteReceiverParser earlyVoteReceiverParser;

	@BeforeClass
	public void setUp() throws Exception {
		earlyVoteReceiverParser = initializeMocks(EarlyVoteReceiverParser.class);
	}

	@Test
	public void testGetRowDataFromXlsFile() throws Exception {
		List<List<Pair<String, String>>> dataList;
		InputStream inputStream = getClass().getResourceAsStream("/EarlyVoteReceivers-oslo-2.xlsx");
		dataList = ExcelUtil.getRowDataFromExcelFile(inputStream).getRows();
		inputStream.close();
		assertNotNull(dataList);
		assertSame(dataList.size(), 2);
		List<ImportOperatorRoleInfo> advancedVotingOperators = earlyVoteReceiverParser.toOperatorList(dataList);
		assertSame(advancedVotingOperators.size(), 2);
		EarlyVoteReceiver advancedVotingOperator = (EarlyVoteReceiver) advancedVotingOperators.get(0);
		assertEquals(advancedVotingOperator.getAdvancedPollingPlaceId(), "0001");
		assertEquals(advancedVotingOperator.getEmail(), "person1@example.com");
		assertEquals(advancedVotingOperator.getFirstName(), "Olaf");
		assertEquals(advancedVotingOperator.getLastName(), "Myrland");
		assertEquals(advancedVotingOperator.getOperatorId(), "11058508014");
		assertEquals(advancedVotingOperator.getTelephoneNumber(), "12345678");
		assertEquals(advancedVotingOperator.getAdvancedPollingPlaceId(), "0001");
	}

	@Test
	public void testInvalidFnrInput() {
		checkInvalidRow(new String[] { IN_VALID_FNR, VALID_FIRST_NAME, VALID_LAST_NAME, VALID_EMAIL, VALID_PHONE_NO, VALID_POLLING_DISTRICT }, "invalid.fnr",
				IN_VALID_FNR);
	}

	@Test
	public void testEmptyFirstNameInput() {
		checkInvalidRow(new String[] { VALID_FNR, EMPTY_FIRST_NAME, VALID_LAST_NAME, VALID_EMAIL, VALID_PHONE_NO, VALID_POLLING_DISTRICT }, "empty.first_name",
				null);
	}

	@Test
	public void testEmptyLastNameInput() {
		checkInvalidRow(new String[] { VALID_FNR, VALID_FIRST_NAME, EMPTY_LAST_NAME, VALID_EMAIL, VALID_PHONE_NO, VALID_POLLING_DISTRICT }, "empty.last_name",
				null);
	}

	@Test
	public void testInvalidEmailInput() {
		checkInvalidRow(new String[] { VALID_FNR, VALID_FIRST_NAME, VALID_LAST_NAME, INVALID_EMAIL, VALID_PHONE_NO, VALID_POLLING_DISTRICT }, "invalid.email",
				INVALID_EMAIL);

	}

	@Test
	public void testInvalidPhoneInput() {
		checkInvalidRow(new String[] { VALID_FNR, VALID_FIRST_NAME, VALID_LAST_NAME, VALID_EMAIL, INVALID_PHONE, VALID_POLLING_DISTRICT }, "invalid.phone",
				INVALID_PHONE);
	}

	@Test
	public void testInvalidPollingPlace() {
		checkInvalidRow(new String[] { VALID_FNR, VALID_FIRST_NAME, VALID_LAST_NAME, VALID_EMAIL, VALID_PHONE, INVALID_POLLING_PLACE },
				"invalid.polling_place", INVALID_POLLING_PLACE);
	}

	@Test
	public void testInvalidEmailAndPollingPlace() {
		checkInvalidFieldsInRow(new String[] { VALID_FNR, VALID_FIRST_NAME, VALID_LAST_NAME, INVALID_EMAIL, VALID_PHONE, INVALID_POLLING_PLACE },
				new String[] { "invalid.email", "invalid.polling_place" }, new String[] { INVALID_EMAIL, INVALID_POLLING_PLACE });
	}

	@Test
	public void testEmptyPollingPlace() {
		checkValidRow(new String[] { VALID_FNR, VALID_FIRST_NAME, VALID_LAST_NAME, VALID_EMAIL, VALID_PHONE, EMPTY_POLLING_PLACE });
	}

	private void checkInvalidRow(String[] row, String errorMessageFragment, String invalidString) {
		try {
			earlyVoteReceiverParser.toOperatorList(Lists.transform(Arrays.<String[]> asList(row), STRING_ARRAY_TO_PAIR_LIST_F));
			fail("SpreadSheetValidationException did not happen");
		} catch (SpreadSheetValidationException e) {
			String error = e.getErrors().get(0);
			assertTrue(error.contains(CELL_REFERENCE), "Error '" + error + "' did not contain '" + CELL_REFERENCE + "'");
			assertTrue(error.contains(errorMessageFragment), "Error '" + error + "' did not contain '" + errorMessageFragment + "'");
			if (invalidString != null) {
				assertTrue(error.contains(invalidString), "Error '" + error + "' did not contain '" + invalidString + "'");
			}
		}
	}

	private void checkInvalidFieldsInRow(String[] row, String[] msgFragments, String[] invalidStrings) {
		try {
			earlyVoteReceiverParser.toOperatorList(Lists.transform(Arrays.<String[]> asList(row), STRING_ARRAY_TO_PAIR_LIST_F));
			fail("SpreadSheetValidationException did not happen");
		} catch (SpreadSheetValidationException e) {
			for (int i = 0; i < msgFragments.length; i++) {
				boolean errorFound = false;
				for (String error : e.getErrors()) {
					if (error.contains(CELL_REFERENCE) && error.contains(msgFragments[i]) && error.contains(invalidStrings[i])) {
						errorFound = true;
						break;
					}
				}
				assertTrue(errorFound, "Could not find the following strings in errors: cell:" + CELL_REFERENCE + ", msgFragment:" + msgFragments[i]
						+ ", invalidString:" + invalidStrings[i] + ". Errors: " + e.getErrors());
			}
		}
	}

	private void checkValidRow(String[] row) {
		try {
			earlyVoteReceiverParser.toOperatorList(Lists.transform(Arrays.<String[]> asList(row), STRING_ARRAY_TO_PAIR_LIST_F));
		} catch (SpreadSheetValidationException e) {
			throw new RuntimeException(e);
		}
	}
}
