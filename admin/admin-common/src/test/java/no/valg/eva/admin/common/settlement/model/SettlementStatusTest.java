package no.valg.eva.admin.common.settlement.model;

import static java.util.Arrays.asList;
import static no.valg.eva.admin.common.counting.constants.CountingMode.BY_POLLING_DISTRICT;
import static no.valg.eva.admin.common.counting.model.CountCategory.VO;
import static no.valg.eva.admin.common.counting.model.CountStatus.APPROVED;
import static no.valg.eva.admin.common.counting.model.CountStatus.NEW;
import static no.valg.eva.admin.common.counting.model.CountStatus.REVOKED;
import static no.valg.eva.admin.common.counting.model.CountStatus.SAVED;
import static no.valg.eva.admin.common.counting.model.CountStatus.TO_SETTLEMENT;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.testng.annotations.Test;



public class SettlementStatusTest {

	@Test
	public void getCountingAreaNotReadyForSettlementCount_givenSettlementStatus_returnsNotReadyCount() throws Exception {
		assertThat(settlementStatus().getCountingAreaNotReadyForSettlementCount()).isEqualTo(2);
	}

	@Test
	public void getCountingAreaApprovedCount_givenSettlementStatus_returnsApprovedCount() throws Exception {
		assertThat(settlementStatus().getCountingAreaApprovedCount()).isEqualTo(3);
	}

	@Test
	public void getCountingAreaReadyForSettlementCount_givenSettlementStatus_returnsToSettlementCount() throws Exception {
		assertThat(settlementStatus().getCountingAreaReadyForSettlementCount()).isEqualTo(4);
	}

	@Test
	public void isCountingAreasNotReadyForSettlement_givenNotReadySettlementStatus_returnsTrue() throws Exception {
		assertThat(settlementStatus().isCountingAreasNotReadyForSettlement()).isTrue();
	}

	@Test
	public void isCountingAreasNotReadyForSettlement_givenReadySettlementStatus_returnsFalse() throws Exception {
		assertThat(readySettlementStatus().isCountingAreasNotReadyForSettlement()).isFalse();
	}

	@Test
	public void isCountingAreasReadyForSettlement_givenNotReadySettlementStatus_returnsFalse() throws Exception {
		assertThat(settlementStatus().isCountingAreasReadyForSettlement()).isFalse();
	}

	@Test
	public void isCountingAreasReadyForSettlement_givenReadySettlementStatus_returnsTrue() throws Exception {
		assertThat(readySettlementStatus().isCountingAreasReadyForSettlement()).isTrue();
	}

	private SettlementStatus readySettlementStatus() {
		List<CountingArea> countingAreaList = asList(
				toSettlementCountingArea(),
				toSettlementCountingArea(),
				toSettlementCountingArea(),
				toSettlementCountingArea(),
				toSettlementCountingArea(),
				toSettlementCountingArea(),
				toSettlementCountingArea(),
				toSettlementCountingArea());
		return new SettlementStatus(VO, BY_POLLING_DISTRICT, countingAreaList);
	}

	private SettlementStatus settlementStatus() {
		List<CountingArea> countingAreaList = asList(
				savedCountingArea(),
				revokedCountingArea(),
				approvedCountingArea(),
				approvedCountingArea(),
				approvedCountingArea(),
				toSettlementCountingArea(),
				toSettlementCountingArea(),
				toSettlementCountingArea(),
				toSettlementCountingArea(),
				newCountingArea());
		return new SettlementStatus(VO, BY_POLLING_DISTRICT, countingAreaList);
	}

	private CountingArea savedCountingArea() {
		return new CountingArea("municipality", "borough", "pollingDistrict", SAVED);
	}

	private CountingArea revokedCountingArea() {
		return new CountingArea("municipality", "borough", "pollingDistrict", REVOKED);
	}

	private CountingArea approvedCountingArea() {
		return new CountingArea("municipality", "borough", "pollingDistrict", APPROVED);
	}

	private CountingArea toSettlementCountingArea() {
		return new CountingArea("municipality", "borough", "pollingDistrict", TO_SETTLEMENT);
	}

	private CountingArea newCountingArea() {
		return new CountingArea("municipality", "borough", "pollingDistrict", NEW);
	}
}

