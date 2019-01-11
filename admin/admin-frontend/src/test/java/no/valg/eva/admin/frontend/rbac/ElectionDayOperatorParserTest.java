package no.valg.eva.admin.frontend.rbac;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.common.rbac.ImportOperatorRoleInfo;
import no.valg.eva.admin.common.rbac.PollingPlaceResponsibleOperator;
import no.valg.eva.admin.common.rbac.VoteReceiver;
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
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

/**
 */

public class ElectionDayOperatorParserTest extends BaseFrontendTest {
	private static final String EXPECTED = "11058508014";
	private static final String VALID_FNR = EXPECTED;
	private static final String INVALID_FNR = "11058508013";
	private static final String VALID_FIRST_NAME_1 = "Kåre";
	private static final String VALID_LAST_NAME_1 = "Olderbolle";
	private static final String VALID_EMAIL_1 = "kare.olderbolle@oslo.kommune.no";
	private static final String VALID_PHONE_NO_1 = "12345678";
	private static final String VALID_POLLING_DISTRICT_1 = "0001";
	private static final String CELL_REFERENCE_1 = "A1";
	private static final Function<String[], List<Pair<String, String>>> STRING_ARRAY_TO_PAIR_LIST_F = new Function<String[], List<Pair<String, String>>>() {
		@Override
		public List<Pair<String, String>> apply(String[] input) {
			return Lists.transform(Lists.newArrayList(input), new Function<String, Pair<String, String>>() {
				@Override
				public Pair<String, String> apply(String input) {
					return new ImmutablePair<>(CELL_REFERENCE_1, input);
				}
			});
		}
	};
	private static final String INVALID_EMAIL = "a@";
	private static final String INVALID_PHONE = "12";
	private static final String EMPTY_FIRST_NAME = "";
	private static final String EMPTY_LAST_NAME = "";
	private static final String INVALID_POLLING_DISTRICT = "01";
	private static final String VALID_PHONE = "12345678";
	private static final String EMPTY_FNR = "";
	private static final String NULL_FNR = null;
	private ElectionDayOperatorParser electionDayOperatorParser;

	@BeforeClass
	public void setUp() throws Exception {
		electionDayOperatorParser = initializeMocks(ElectionDayOperatorParser.class);
	}

	@Test
	public void testGetRowDataFromXlsFile() throws Exception {
		List<List<Pair<String, String>>> dataList;
		InputStream inputStream = getClass().getResourceAsStream("/pollingPlaceOperators-oslo.xlsx");
		dataList = ExcelUtil.getRowDataFromExcelFile(inputStream).getRows();
		inputStream.close();
		assertNotNull(dataList);
		assertSame(dataList.size(), 4);
		List<ImportOperatorRoleInfo> operatorList = electionDayOperatorParser.toOperatorList(dataList);
		assertSame(operatorList.size(), 4);
		PollingPlaceResponsibleOperator pollingPlaceSupervisor = (PollingPlaceResponsibleOperator) operatorList.get(0);
		assertEquals(pollingPlaceSupervisor.getVotingDistrict(), "0001");
		assertEquals(pollingPlaceSupervisor.getEmail(), "person1@example.com");
		assertEquals(pollingPlaceSupervisor.getFirstName(), "Olaf");
		assertEquals(pollingPlaceSupervisor.getLastName(), "Myrland");
		assertEquals(pollingPlaceSupervisor.getOperatorId(), EXPECTED);
		assertEquals(pollingPlaceSupervisor.getTelephoneNumber(), "12345678");

		VoteReceiver votingOperator = (VoteReceiver) operatorList.get(1);
		assertEquals(votingOperator.getVotingDistrict(), "0002");
		assertEquals(votingOperator.getEmail(), "person2@example.com");
		assertEquals(votingOperator.getFirstName(), "Else");
		assertEquals(votingOperator.getLastName(), "Bakken");
		assertEquals(votingOperator.getOperatorId(), EXPECTED);
		assertEquals(votingOperator.getTelephoneNumber(), "12345678");

		PollingPlaceResponsibleOperator responsibleWithAllEmptyFields = (PollingPlaceResponsibleOperator) operatorList.get(3);
		assertEquals(responsibleWithAllEmptyFields.getVotingDistrict(), "0002");
		assertNull(responsibleWithAllEmptyFields.getEmail());
		assertEquals(responsibleWithAllEmptyFields.getFirstName(), "Cornelia-Gyda");
		assertEquals(responsibleWithAllEmptyFields.getLastName(), "Juliussen");
		assertEquals(responsibleWithAllEmptyFields.getOperatorId(), EXPECTED);
		assertNull(responsibleWithAllEmptyFields.getTelephoneNumber());

	}

	@Test
	public void testInvalidFnrInput() {
		checkInvalidRow(new String[] { INVALID_FNR, VALID_FIRST_NAME_1, VALID_LAST_NAME_1, VALID_EMAIL_1, VALID_PHONE_NO_1, VALID_POLLING_DISTRICT_1 },
				"invalid.fnr",
				INVALID_FNR);
	}

	@Test
	public void testEmptyFnrInput() {
		checkInvalidRow(new String[] { EMPTY_FNR, VALID_FIRST_NAME_1, VALID_LAST_NAME_1, VALID_EMAIL_1, VALID_PHONE_NO_1, VALID_POLLING_DISTRICT_1 },
				"invalid.fnr",
				EMPTY_FNR);
	}

	@Test
	public void testNullFnrInput() {
		checkInvalidRow(new String[] { NULL_FNR, VALID_FIRST_NAME_1, VALID_LAST_NAME_1, VALID_EMAIL_1, VALID_PHONE_NO_1, VALID_POLLING_DISTRICT_1 },
				"invalid.fnr", null);
	}

	@Test
	public void testEmptyFirstNameInput() {
		checkInvalidRow(new String[] { VALID_FNR, EMPTY_FIRST_NAME, VALID_LAST_NAME_1, VALID_EMAIL_1, VALID_PHONE_NO_1, VALID_POLLING_DISTRICT_1 },
				"empty.first_name",
				null);
	}

	@Test
	public void testEmptyLastNameInput() {
		checkInvalidRow(new String[] { VALID_FNR, VALID_FIRST_NAME_1, EMPTY_LAST_NAME, VALID_EMAIL_1, VALID_PHONE_NO_1, VALID_POLLING_DISTRICT_1 },
				"empty.last_name",
				null);
	}

	@Test
	public void testInvalidEmailInput() {
		checkInvalidRow(new String[] { VALID_FNR, VALID_FIRST_NAME_1, VALID_LAST_NAME_1, INVALID_EMAIL, VALID_PHONE_NO_1, VALID_POLLING_DISTRICT_1 },
				"invalid.email",
				INVALID_EMAIL);

	}

	@Test
	public void testInvalidPhoneInput() {
		checkInvalidRow(new String[] { VALID_FNR, VALID_FIRST_NAME_1, VALID_LAST_NAME_1, VALID_EMAIL_1, INVALID_PHONE, VALID_POLLING_DISTRICT_1 },
				"invalid.phone",
				INVALID_PHONE);
	}

	@Test
	public void testInvalidPollingDistrict() {
		checkInvalidRow(new String[] { VALID_FNR, VALID_FIRST_NAME_1, VALID_LAST_NAME_1, VALID_EMAIL_1, VALID_PHONE, INVALID_POLLING_DISTRICT },
				"invalid.polling_district", INVALID_POLLING_DISTRICT);
	}

	@Test
	public void testInvalidPollingDistrictAndPhone() {
		checkInvalidFieldsInRow(new String[] { VALID_FNR, VALID_FIRST_NAME_1, VALID_LAST_NAME_1, VALID_EMAIL_1, INVALID_PHONE, INVALID_POLLING_DISTRICT },
				new String[] { "invalid.polling_district", "invalid.phone" }, new String[] { INVALID_POLLING_DISTRICT, INVALID_PHONE });
	}

	private void checkInvalidRow(String[] row, String errorMessageFragment, String invalidString) {
		try {
			electionDayOperatorParser.toOperatorList(Lists.transform(Arrays.<String[]> asList(row), STRING_ARRAY_TO_PAIR_LIST_F));
			fail("SpreadSheetValidationException did not happen");
		} catch (SpreadSheetValidationException e) {
			String error = e.getErrors().get(0);
			assertTrue(error.contains(CELL_REFERENCE_1));
			assertTrue(error.contains(errorMessageFragment));
			if (invalidString != null) {
				assertTrue(error.contains(invalidString));
			}
		}
	}

	private void checkInvalidFieldsInRow(String[] row, String[] msgFragments, String[] invalidStrings) {
		try {
			electionDayOperatorParser.toOperatorList(Lists.transform(Arrays.<String[]> asList(row), STRING_ARRAY_TO_PAIR_LIST_F));
			fail("SpreadSheetValidationException did not happen");
		} catch (SpreadSheetValidationException e) {
			for (int i = 0; i < msgFragments.length; i++) {
				boolean errorFound = false;
				for (String error : e.getErrors()) {
					if (error.contains(CELL_REFERENCE_1) && error.contains(msgFragments[i]) && error.contains(invalidStrings[i])) {
						errorFound = true;
						break;
					}
				}
				assertTrue(errorFound, "Could not find the following strings in errors: cell:" + CELL_REFERENCE_1 + ", msgFragment:" + msgFragments[i]
						+ ", invalidString:" + invalidStrings[i] + ". Errors: " + e.getErrors());
			}
		}
	}
}

