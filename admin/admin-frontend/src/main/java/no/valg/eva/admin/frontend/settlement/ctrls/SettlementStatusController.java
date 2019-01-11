package no.valg.eva.admin.frontend.settlement.ctrls;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.faces.application.FacesMessage;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

import no.valg.eva.admin.frontend.util.MessageUtil;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.common.counting.model.CountStatus;
import no.valg.eva.admin.common.settlement.model.CountingArea;
import no.valg.eva.admin.common.settlement.model.SettlementStatus;

@Named
@ViewScoped
public class SettlementStatusController extends BaseSettlementController {
	private Map<CountCategory, SettlementStatus> settlementStatusMap = new HashMap<>();
	private List<SettlementStatus> settlementStatusList = new ArrayList<>();
	private List<CountingArea> currentCountingAreaList;

	@Override
	protected void initView() {
		settlementStatusMap = settlementService.settlementStatusMap(userData, getContestInfo().getElectionPath());
		settlementStatusList = new ArrayList<>(settlementStatusMap.values());
		settlementStatusList = settlementStatusList.stream()
				.sorted((o1, o2) -> o1.getCountCategory().compareTo(o2.getCountCategory()))
				.collect(Collectors.toList());
	}

	public void makeSettlement() {
		if (!isSettlementDone() && isReadyForSettlement()) {
			execute(() -> {
				settlementService.createSettlement(userData, getContestInfo().getElectionPath());
				setSettlementDone(true);
				MessageUtil.buildDetailMessage(messageProvider.get("@settlement.make_settlement_done"), FacesMessage.SEVERITY_INFO);
			});
		}
	}

	public boolean isReadyForSettlement() {
		for (SettlementStatus settlementStatus : settlementStatusMap.values()) {
			if (settlementStatus.isCountingAreasNotReadyForSettlement()) {
				return false;
			}
		}
		return true;
	}

	@Override
	protected String getView() {
		return VIEW_SETTLEMENT_STATUS;
	}

	public List<SettlementStatus> getSettlementStatusList() {
		return settlementStatusList;
	}

	public boolean renderCountingModeInSettlementStatus() {
		return (getContestInfo().getAreaPath().isMunicipalityLevel() || getContestInfo().getAreaPath().isBoroughLevel());
	}

	public void selectSettlementStatusForCountingAreasNotReady(CountCategory countCategory) {
		filterCountingAreas(countCategory, countStatus -> countStatus != CountStatus.APPROVED && countStatus != CountStatus.TO_SETTLEMENT);
	}

	public void selectSettlementStatusForCountingAreasApproved(CountCategory countCategory) {
		filterCountingAreas(countCategory, countStatus -> countStatus == CountStatus.APPROVED);
	}

	public void selectSettlementStatusForCountingAreasReadyForSettlement(CountCategory countCategory) {
		filterCountingAreas(countCategory, countStatus -> countStatus == CountStatus.TO_SETTLEMENT);
	}

	public List<CountingArea> getSelectedCountingAreas() {
		return currentCountingAreaList;
	}

	public String getName(CountingArea countingArea) {
		StringBuilder name = new StringBuilder(countingArea.getMunicipalityName());
		name.append(", ").append(countingArea.getBoroughName());
		if (countingArea.getPollingDistrictName() != null && !countingArea.getBoroughName().equals(countingArea.getPollingDistrictName())) {
			name.append(", ").append(countingArea.getPollingDistrictName());
		}
		return name.toString();
	}

	private void filterCountingAreas(CountCategory countCategory, CountStatusFilter filter) {
		List<CountingArea> countingAreaList = settlementStatusMap.get(countCategory).getCountingAreaList();
		currentCountingAreaList = countingAreaList.stream().filter(area -> filter.include(area.getCountStatus())).collect(Collectors.toList());
	}

	interface CountStatusFilter {
		boolean include(CountStatus countStatus);
	}

}
