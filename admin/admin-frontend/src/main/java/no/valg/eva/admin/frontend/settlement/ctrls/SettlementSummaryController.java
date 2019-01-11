package no.valg.eva.admin.frontend.settlement.ctrls;

import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.common.settlement.model.BallotCount;
import no.valg.eva.admin.common.settlement.model.BallotCountSummary;
import no.valg.eva.admin.common.settlement.model.SettlementSummary;
import no.valg.eva.admin.frontend.common.Button;
import no.valg.eva.admin.frontend.common.ButtonType;
import no.valg.eva.admin.frontend.settlement.SummaryProvider;
import no.valg.eva.admin.frontend.settlement.SummaryRow;

import javax.faces.view.ViewScoped;
import javax.inject.Named;

import static java.lang.String.format;
import static no.valg.eva.admin.frontend.common.Button.enabled;
import static no.valg.eva.admin.frontend.common.ButtonType.NEXT;
import static no.valg.eva.admin.frontend.common.ButtonType.PREV;

@Named
@ViewScoped
public class SettlementSummaryController extends BaseSettlementController implements SummaryProvider {
	private SettlementSummary settlementSummary;
	private int countCategoryIndexForSettlementSummary;

	@Override
	protected void initView() {
		settlementSummary = settlementService.settlementSummary(userData, getContestInfo().getElectionPath());
		countCategoryIndexForSettlementSummary = 0;
	}

	@Override
	protected String getView() {
		return VIEW_SETTLEMENT_SUMMARY;
	}

	@Override
	public void prevSettlementSummaryPage() {
		if (countCategoryIndexForSettlementSummary - 2 >= 0) {
			countCategoryIndexForSettlementSummary -= 2;
		} else if (countCategoryIndexForSettlementSummary - 1 >= 0) {
			countCategoryIndexForSettlementSummary--;
		}
	}

	@Override
	public void nextSettlementSummaryPage() {
		
		if (countCategoryIndexForSettlementSummary + 2 < getSettlementSummary().getCountCategories().size() - 3) {
			countCategoryIndexForSettlementSummary += 2;
		} else if (countCategoryIndexForSettlementSummary + 1 < getSettlementSummary().getCountCategories().size() - 2) {
			countCategoryIndexForSettlementSummary++;
		}
		
	}

	@Override
	public SummaryRow getSummaryRow(final BallotCountSummary ballotCountSummary) {
		return new SummaryRow() {
			@Override
			public String getStyleClass() {
				return ballotCountSummary.getBallotInfo().getBallotId() == null ? "total" : "data";
			}

			@Override
			public String getName() {
				return ballotCountSummary.getBallotInfo().getBallotName();
			}

			@Override
			public String getId() {
				return ballotCountSummary.getBallotInfo().getBallotId();
			}

			@Override
			public String getRowStyleClass() {
				String id = getId();
				if (id == null || id.length() == 0) {
					String name = getName();
					int index = name.lastIndexOf('.');
					if (index > -1) {
						id = name.substring(index + 1);
					}
				}
				return id;
			}

			@Override
			public boolean isHasCategory(int index) {
				return isValidCategoryIndex(index);
			}

			@Override
			public String getCategoryName(int index) {
				return getCountCategory(index).messageProperty();
			}

			@Override
			public String getModifiedCount(int index) {
				BallotCount count = ballotCountSummary.getBallotCount(getCountCategory(index));
				return count == null ? "-" : getNumber(count.getModifiedBallotCount());
			}

			@Override
			public String getUnmodifiedCount(int index) {
				BallotCount count = ballotCountSummary.getBallotCount(getCountCategory(index));
				return count == null ? "-" : getNumber(count.getUnmodifiedBallotCount());
			}

			@Override
			public String getCount(int index) {
				BallotCount count = ballotCountSummary.getBallotCount(getCountCategory(index));
				return count == null ? "-" : getNumber(count.getBallotCount());
			}

			@Override
			public String getTotalModifiedCount() {
				BallotCount count = ballotCountSummary.getTotalBallotCount();
				return count == null ? "-" : getNumber(count.getModifiedBallotCount());
			}

			@Override
			public String getTotalUnmodifiedCount() {
				BallotCount count = ballotCountSummary.getTotalBallotCount();
				return count == null ? "-" : getNumber(count.getUnmodifiedBallotCount());
			}

			@Override
			public String getTotalCount() {
				BallotCount count = ballotCountSummary.getTotalBallotCount();
				return count == null ? "-" : getNumber(count.getBallotCount());
			}
		};
	}

	@Override
	public SettlementSummary getSettlementSummary() {
		return settlementSummary;
	}

	@Override
	public Button button(ButtonType type) {
		
		switch (type) {
		case PREV:
			boolean prevButtonEnabled = countCategoryIndexForSettlementSummary > 0;
			return enabled(prevButtonEnabled);
		case NEXT:
			boolean nextButtonEnabled = countCategoryIndexForSettlementSummary + 3 < getSettlementSummary().getCountCategories().size();
			return enabled(nextButtonEnabled);
		default:
			throw new IllegalArgumentException(format("expected button type <%s> or <%s>, but got <%s>", PREV, NEXT, type));
		}
		
	}

	private CountCategory getCountCategory(int index) {
		if (isValidCategoryIndex(index)) {
			return getSettlementSummary().getCountCategories().get(countCategoryIndexForSettlementSummary + index);
		}
		throw new IllegalArgumentException("Invalid index " + index);
	}

	public boolean isValidCategoryIndex(int index) {
		return countCategoryIndexForSettlementSummary + index < getSettlementSummary().getCountCategories().size();
	}

	private String getNumber(Integer i) {
		return i == null ? "-" : i.toString();
	}
}
