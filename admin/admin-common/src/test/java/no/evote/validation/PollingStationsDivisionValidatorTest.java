package no.evote.validation;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static no.evote.validation.PollingStationsDivisionValidator.VALIDATION_FEEDBACK_DIVISION_LIST_EMPTY;
import static no.evote.validation.PollingStationsDivisionValidator.VALIDATION_FEEDBACK_DOENST_START_WITH_A;
import static no.evote.validation.PollingStationsDivisionValidator.VALIDATION_FEEDBACK_ILLEGAL_SIGNS;
import static no.evote.validation.PollingStationsDivisionValidator.VALIDATION_FEEDBACK_NOT_WHOLE_ALPHABET;
import static no.evote.validation.PollingStationsDivisionValidator.VALIDATION_FEEDBACK_ONE_OR_TWO_LETTERS;
import static no.evote.validation.PollingStationsDivisionValidator.VALIDATION_FEEDBACK_RANGES_NOT_FOLLOWING;
import static org.testng.Assert.assertEquals;

import java.util.List;

import no.valg.eva.admin.common.configuration.model.local.Rode;
import no.valg.eva.admin.test.BaseTakeTimeTest;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class PollingStationsDivisionValidatorTest extends BaseTakeTimeTest {

	private PollingStationsDivisionValidator validator = new PollingStationsDivisionValidator();
	
	@Test(dataProvider = "rodefordelinger")
	public void isValid_getValidationFeedback_gittFordeling_girForventetResultat(List<Rode> rodefordelinger, boolean erGyldigFordeling, String forventetFeedback) {
		PollingStationsDivisionValidator validator = new PollingStationsDivisionValidator();
		assertEquals(validator.isValid(rodefordelinger), erGyldigFordeling);
		assertEquals(validator.getValidationFeedback(), forventetFeedback);
	}
	
	@DataProvider
	private Object[][] rodefordelinger() {
		return new Object[][] {
			// Datasett med 1 bokstav 
			{ asList(rode("A", "B"), rode("C", "G"), rode("H", "P"), rode("Q", "Å")), true, null },
			{ asList(rode("A", "B"), rode("c", "g"), rode("H", "P"), rode("Q", "Å")), true, null },
			{ asList(rode("A", "B"), rode("H", "P"), rode("C", "G"), rode("Q", "Å")), true, null },
			{ asList(rode("A", "A"), rode("B", "Å")), true, null },
			{ asList(rode("A", "B"), rode("*", "G"), rode("H", "P"), rode("Q", "Å")), false, VALIDATION_FEEDBACK_ILLEGAL_SIGNS },
			{ asList(rode("A", "B"), rode("9", "G"), rode("H", "P"), rode("Q", "Å")), false, VALIDATION_FEEDBACK_ILLEGAL_SIGNS },
			{ asList(rode("B", "C"), rode("D", "G"), rode("H", "P"), rode("Q", "Å")), false, VALIDATION_FEEDBACK_DOENST_START_WITH_A },
			{ asList(rode("A", "B"), rode("C", "G"), rode("H", "P"), rode("Q", "Z")), false, VALIDATION_FEEDBACK_NOT_WHOLE_ALPHABET },
			{ asList(rode("A", "C"), rode("D", "G"), rode("H", "O"), rode("Q", "Z")), false, VALIDATION_FEEDBACK_RANGES_NOT_FOLLOWING },

			// Datasett med 2 bokstaver
			{ asList(rode("A", "AÅ"), rode("BA", "GÅ"), rode("HA", "PÅ"), rode("QA", "ÅÅ")), true, null },
			{ asList(rode("A", "AÅ"), rode("bA", "GÅ"), rode("HA", "pÅ"), rode("QA", "ÅÅ")), true, null },
			{ asList(rode("A", "AÅ"), rode("HA", "PÅ"), rode("BA", "GÅ"), rode("QA", "ÅÅ")), true, null },
			{ asList(rode("A", "AB"), rode("AC", "AC"), rode("AD", "ÅÅ")), true, null },
			{ asList(rode("A", "AÅ"), rode("BA", "*"), rode("HA", "PÅ"), rode("QA", "ÅÅ")), false, VALIDATION_FEEDBACK_ILLEGAL_SIGNS },
			{ asList(rode("A", "AÅ"), rode("BA", "99"), rode("HA", "PÅ"), rode("QA", "ÅÅ")), false, VALIDATION_FEEDBACK_ILLEGAL_SIGNS },
			{ asList(rode("BA", "GÅ"), rode("HA", "PÅ"), rode("QA", "ÅÅ")), false, VALIDATION_FEEDBACK_DOENST_START_WITH_A },
			{ asList(rode("AA", "TÅ"), rode("SA", "ÅÅ")), false, VALIDATION_FEEDBACK_DOENST_START_WITH_A },
			{ asList(rode("A", "AÅ"), rode("BA", "GÅ"), rode("HA", "PÅ")), false, VALIDATION_FEEDBACK_NOT_WHOLE_ALPHABET },
			{ asList(rode("A", "AÅ"), rode("HA", "PÅ"), rode("QA", "ÅÅ")), false, VALIDATION_FEEDBACK_RANGES_NOT_FOLLOWING },
			{ asList(rode("A", "AÅÅ"), rode("BAA", "GÅÅ"), rode("HAA", "PÅÅ"), rode("QAA", "ÅÅÅ")), false, VALIDATION_FEEDBACK_ONE_OR_TWO_LETTERS },
			{ asList(rode("A", "AÅ"), rode("BA", "G"), rode("HA", "PÅ"), rode("QA", "Å")), false, VALIDATION_FEEDBACK_ONE_OR_TWO_LETTERS },
			{ asList(rode("A", ""), rode("T", "Å")), false, VALIDATION_FEEDBACK_ONE_OR_TWO_LETTERS },
			{ emptyList(), false, VALIDATION_FEEDBACK_DIVISION_LIST_EMPTY },
			{ null, false, VALIDATION_FEEDBACK_DIVISION_LIST_EMPTY },
		};
	}

	private Rode rode(String fraBokstav, String tilBokstav) {
		return new Rode(fraBokstav, tilBokstav);
	}
}
