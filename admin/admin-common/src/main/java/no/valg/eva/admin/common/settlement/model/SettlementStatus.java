package no.valg.eva.admin.common.settlement.model;

import static no.valg.eva.admin.common.counting.model.CountStatus.APPROVED;
import static no.valg.eva.admin.common.counting.model.CountStatus.REVOKED;
import static no.valg.eva.admin.common.counting.model.CountStatus.SAVED;
import static no.valg.eva.admin.common.counting.model.CountStatus.TO_SETTLEMENT;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import no.valg.eva.admin.common.counting.constants.CountingMode;
import no.valg.eva.admin.common.counting.model.CountCategory;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class SettlementStatus implements Serializable {
	private final CountCategory countCategory;
	private final CountingMode countingMode;
	private final List<CountingArea> countingAreaList;

	public SettlementStatus(CountCategory countCategory, CountingMode countingMode, List<CountingArea> countingAreaList) {
		this.countCategory = countCategory;
		this.countingMode = countingMode;
		this.countingAreaList = Collections.unmodifiableList(new ArrayList<>(countingAreaList));
	}

	public SettlementStatus(CountCategory countCategory, List<CountingArea> countingAreaList) {
		this(countCategory, null, countingAreaList);
	}

	public CountCategory getCountCategory() {
		return countCategory;
	}

	public CountingMode getCountingMode() {
		return countingMode;
	}

	public int getTotalCountingAreaCount() {
		return countingAreaList.size();
	}

	public List<CountingArea> getCountingAreaList() {
		return countingAreaList;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		if (obj.getClass() != getClass()) {
			return false;
		}
		SettlementStatus rhs = (SettlementStatus) obj;
		return new EqualsBuilder()
				.append(this.countCategory, rhs.countCategory)
				.append(this.countingMode, rhs.countingMode)
				.append(this.countingAreaList, rhs.countingAreaList)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder()
				.append(countCategory)
				.append(countingMode)
				.append(countingAreaList)
				.toHashCode();
	}

	public int getCountingAreaNotReadyForSettlementCount() {
		int notReadyForSettlementCount = 0;
		for (CountingArea countingArea : countingAreaList) {
			if (countingArea.getCountStatus() == SAVED || countingArea.getCountStatus() == REVOKED) {
				notReadyForSettlementCount++;
			}
		}
		return notReadyForSettlementCount;
	}

	public int getCountingAreaApprovedCount() {
		int approvedCount = 0;
		for (CountingArea countingArea : countingAreaList) {
			if (countingArea.getCountStatus() == APPROVED) {
				approvedCount++;
			}
		}
		return approvedCount;
	}

	public int getCountingAreaReadyForSettlementCount() {
		int readyForSettlementCount = 0;
		for (CountingArea countingArea : countingAreaList) {
			if (countingArea.getCountStatus() == TO_SETTLEMENT) {
				readyForSettlementCount++;
			}
		}
		return readyForSettlementCount;
	}

	public boolean isCountingAreasNotReadyForSettlement() {
		return getCountingAreaReadyForSettlementCount() != getTotalCountingAreaCount();
	}

	public boolean isCountingAreasReadyForSettlement() {
		return getCountingAreaReadyForSettlementCount() == getTotalCountingAreaCount();
	}
}
