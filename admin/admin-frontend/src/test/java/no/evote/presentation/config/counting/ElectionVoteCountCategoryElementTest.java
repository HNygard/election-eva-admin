package no.evote.presentation.config.counting;

import static no.evote.presentation.config.counting.ElectionVoteCountCategoryElement.COUNT_TYPE_BY_POLLING_DISTRICT;
import static no.evote.presentation.config.counting.ElectionVoteCountCategoryElement.COUNT_TYPE_CENTRAL;
import static no.evote.presentation.config.counting.ElectionVoteCountCategoryElement.COUNT_TYPE_CENTRAL_AND_BY_POLLING_DISTRICT;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import java.util.List;

import javax.faces.component.UIComponent;
import javax.faces.component.UISelectOne;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;

import no.valg.eva.admin.configuration.domain.model.ElectionVoteCountCategory;

import org.testng.annotations.Test;

/**
 * Test cases for ElectionVoteCountCategoryElement
 */
public class ElectionVoteCountCategoryElementTest {

	private final UIComponent component = new UISelectOne();

	@Test
	public void getChoicesReturnsListOfDisabledItemsWhenEnabledParameterIsFalse() {
		ElectionVoteCountCategoryElement element = new ElectionVoteCountCategoryElement(new ElectionVoteCountCategory());
		List<SelectItem> choices = element.getChoices(false);
		assertTrue("Expected item to be disabled", choices.get(0).isDisabled());
	}

	@Test
	public void centralPreliminaryAndPollingDistrictCountTrueGivesCountModeCentralByPollingDistrict() {
		ElectionVoteCountCategory electionVoteCountCategory = new ElectionVoteCountCategory();
		electionVoteCountCategory.setCentralPreliminaryCount(true);
		electionVoteCountCategory.setPollingDistrictCount(true);
		assertEquals(COUNT_TYPE_CENTRAL_AND_BY_POLLING_DISTRICT, new ElectionVoteCountCategoryElement(electionVoteCountCategory).getCountMode());
	}

	@Test
	public void centralPreliminaryTrueAndPollingDistrictCountFalseGivesCountModeCentral() {
		ElectionVoteCountCategory electionVoteCountCategory = new ElectionVoteCountCategory();
		electionVoteCountCategory.setCentralPreliminaryCount(true);
		electionVoteCountCategory.setPollingDistrictCount(false);
		assertEquals(COUNT_TYPE_CENTRAL, new ElectionVoteCountCategoryElement(electionVoteCountCategory).getCountMode());
	}

	@Test
	public void centralPreliminaryFalseAndPollingDistrictCountTrueGivesCountModeByPollingDistrict() {
		ElectionVoteCountCategory electionVoteCountCategory = new ElectionVoteCountCategory();
		electionVoteCountCategory.setCentralPreliminaryCount(false);
		electionVoteCountCategory.setPollingDistrictCount(true);
		assertEquals(COUNT_TYPE_BY_POLLING_DISTRICT, new ElectionVoteCountCategoryElement(electionVoteCountCategory).getCountMode());
	}

	@Test
	public void countTypeCentralGivesCentralPreliminaryTrueAndPollingDistrictFalse() {
		ElectionVoteCountCategoryElement element = new ElectionVoteCountCategoryElement(new ElectionVoteCountCategory());
		element.changeCountMode(new ValueChangeEvent(component, null, COUNT_TYPE_CENTRAL));
		assertTrue("Expected central preliminary count", element.getElectionVoteCountCategory().isCentralPreliminaryCount());
		assertFalse("Expected polling district count false", element.getElectionVoteCountCategory().isPollingDistrictCount());
	}

	@Test
	public void countTypeCentralByPollingDistrictGivesCentralPreliminaryTrueAndPollingDistrictTrue() {
		ElectionVoteCountCategoryElement element = new ElectionVoteCountCategoryElement(new ElectionVoteCountCategory());
		element.changeCountMode(new ValueChangeEvent(component, null, COUNT_TYPE_CENTRAL_AND_BY_POLLING_DISTRICT));
		assertTrue("Expected central preliminary count", element.getElectionVoteCountCategory().isCentralPreliminaryCount());
		assertTrue("Expected polling district count", element.getElectionVoteCountCategory().isPollingDistrictCount());
	}

	@Test
	public void countTypeByPollingDistrictGivesCentralPreliminaryFalseAndPollingDistrictTrue() {
		ElectionVoteCountCategoryElement element = new ElectionVoteCountCategoryElement(new ElectionVoteCountCategory());
		element.changeCountMode(new ValueChangeEvent(component, null, COUNT_TYPE_BY_POLLING_DISTRICT));
		assertFalse("Expected central preliminary count false", element.getElectionVoteCountCategory().isCentralPreliminaryCount());
		assertTrue("Expected polling district count", element.getElectionVoteCountCategory().isPollingDistrictCount());
	}

}
