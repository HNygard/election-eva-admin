package no.valg.eva.admin.common.counting.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;


public class CountStatusTest {

	@Test(dataProvider = "getIdTestData")
	public void getId_returnsExpectedValue(CountStatus countStatus, int expectedId) throws Exception {
		int id = countStatus.getId();
		assertThat(id).isEqualTo(expectedId);
	}

	@DataProvider(name = "getIdTestData")
	public Object[][] getIdTestData() {
		return new Object[][]{
			{ CountStatus.NEW, -1 },
			{ CountStatus.SAVED, 0 },
			{ CountStatus.APPROVED, 2 },
			{ CountStatus.TO_SETTLEMENT, 3 },
			{ CountStatus.REVOKED, 5 }
		};
	}

	@Test(dataProvider = "getIdTestData")
	public void fromId_convertsTheOppositeWayAsGetId(CountStatus expectedStatus, int id) throws Exception {
		CountStatus status = CountStatus.fromId(id);
		assertThat(status).isSameAs(expectedStatus);
	}

	@Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "unknown count status id: <\\d+>")
	public void fromIdShouldThrowExceptionOnInvalidStatusId() throws Exception {
		CountStatus.fromId(6);
	}

	@Test(dataProvider = "getNameTestData")
	public void getName_returnsCorrectName(CountStatus countStatus, String substring) throws Exception {
		String name = countStatus.getName();
		assertThat(name).isEqualTo("@vote_count_status[" + substring + "].name");
	}

	@DataProvider(name = "getNameTestData")
	public Object[][] getNameTestData() {
		return new Object[][]{
			{ CountStatus.NEW, "NEW" },
			{ CountStatus.SAVED, "SAVED" },
			{ CountStatus.APPROVED, "APPROVED" },
			{ CountStatus.TO_SETTLEMENT, "TO_SETTLEMENT" },
			{ CountStatus.REVOKED, "REVOKED" }
		};
	}
}

