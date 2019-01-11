package no.evote.presentation.config.counting;

import java.util.ArrayList;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;

import no.valg.eva.admin.frontend.util.FacesUtil;
import no.valg.eva.admin.configuration.domain.model.ElectionVoteCountCategory;
import no.valg.eva.admin.configuration.domain.model.VoteCountCategory;

/**
 * Wraps an ElectionVoteCountCategory instance.  Used for configuring categories centrally.
 */
public class ElectionVoteCountCategoryElement {

	public static final int COUNT_TYPE_CENTRAL = 1;
	public static final int COUNT_TYPE_CENTRAL_AND_BY_POLLING_DISTRICT = 2;
	public static final int COUNT_TYPE_BY_POLLING_DISTRICT = 3;

	public static final String COUNT_TYPE_CENTRAL_EXPR = "#{msgs['@report_count_category.count_mode_select.central']}";
	public static final String COUNT_TYPE_CENTRAL_AND_BY_POLLING_DISTRICT_EXPR = "#{msgs['@report_count_category.count_mode_select.central_and_by_polling_district']}";
	public static final String COUNT_TYPE_BY_POLLING_DISTRICT_EXPR = "#{msgs['@report_count_category.count_mode_select.by_polling_district']}";

	private final ElectionVoteCountCategory electionVoteCountCategory;
	private int countMode;

	/**
	 * Creates instance
	 * @param electionVoteCountCategory election vote count category from database
	 */
	public ElectionVoteCountCategoryElement(final ElectionVoteCountCategory electionVoteCountCategory) {
		this.electionVoteCountCategory = electionVoteCountCategory;
		if (electionVoteCountCategory.isCentralPreliminaryCount()) {
			if (electionVoteCountCategory.isPollingDistrictCount()) {
				countMode = COUNT_TYPE_CENTRAL_AND_BY_POLLING_DISTRICT;
			} else {
				countMode = COUNT_TYPE_CENTRAL;
			}
		} else {
			if (electionVoteCountCategory.isPollingDistrictCount()) {
				countMode = COUNT_TYPE_BY_POLLING_DISTRICT;
			}
		}

	}

	/**
	 * @return items for select one radio on page
	 */
	public List<SelectItem> getChoices(boolean enabled) {
		List<SelectItem> choices = new ArrayList<>();

		choices.add(new SelectItem(str(COUNT_TYPE_CENTRAL), text(COUNT_TYPE_CENTRAL_EXPR), null, !enabled));
		choices.add(new SelectItem(str(COUNT_TYPE_CENTRAL_AND_BY_POLLING_DISTRICT), text(COUNT_TYPE_CENTRAL_AND_BY_POLLING_DISTRICT_EXPR), null, !enabled));
		choices.add(new SelectItem(str(COUNT_TYPE_BY_POLLING_DISTRICT), text(COUNT_TYPE_BY_POLLING_DISTRICT_EXPR), null, !enabled));

		return choices;
	}

	private String str(final int countType) {
		return String.valueOf(countType);
	}

	private String text(final String countTypeKey) {
		if (FacesContext.getCurrentInstance() != null) {
			return (String) FacesUtil.resolveExpression(countTypeKey);
		}
		return null;
	}

	/**
	 * Updates flags on electionVoteCountCategory based on count mode
	 * @param event with new value for count mode
	 */
	public void changeCountMode(final ValueChangeEvent event) {
		countMode = (Integer) event.getNewValue();
		switch (countMode) {
			case COUNT_TYPE_CENTRAL:
				electionVoteCountCategory.setCentralPreliminaryCount(true);
				electionVoteCountCategory.setPollingDistrictCount(false);
				break;
			case COUNT_TYPE_CENTRAL_AND_BY_POLLING_DISTRICT:
				electionVoteCountCategory.setCentralPreliminaryCount(true);
				electionVoteCountCategory.setPollingDistrictCount(true);
				break;
			case COUNT_TYPE_BY_POLLING_DISTRICT:
				electionVoteCountCategory.setCentralPreliminaryCount(false);
				electionVoteCountCategory.setPollingDistrictCount(true);
				break;
			default:
				break;
		}
	}

	public void setCountMode(final int mode) {
		countMode = mode;
	}

	public int getCountMode() {
		return countMode;
	}

	public VoteCountCategory getVotingCountCategory() {
		return electionVoteCountCategory.getVoteCountCategory();
	}

	public ElectionVoteCountCategory getElectionVoteCountCategory() {
		return electionVoteCountCategory;
	}
}
