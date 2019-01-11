package no.valg.eva.admin.common.counting.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;


public class RejectedBallotTest {

	public static final String MANGLER_OFF_STEMPEL = "FA";
	public static final String FREMGAR_IKKE_HVILKEN_LISTE = "FC";

	@Test(dataProvider = "isRejected")
	public void isRejected_withDataProvider_verifyExpected(RejectedBallot.State state, boolean expected) throws Exception {
		RejectedBallot rejectedBallot = createRejectedBallot();
		rejectedBallot.setState(state);

		assertThat(rejectedBallot.isRejected()).isEqualTo(expected);
	}

	@DataProvider(name = "isRejected")
	public Object[][] isRejected() {
		return new Object[][] {
				{ RejectedBallot.State.REJECTED, true },
				{ RejectedBallot.State.MODIFIED, false },
				{ RejectedBallot.State.UNMODIFIED, false }
		};
	}

	@Test(dataProvider = "getSelectedBallotRejectionId")
	public void getSelectedBallotRejectionId_withDataProvider_verifyExpected(String selectedBallotRejectionId, String expected) throws Exception {
		RejectedBallot rejectedBallot = createRejectedBallot();
		rejectedBallot.setSelectedBallotRejectionId(selectedBallotRejectionId);

		assertThat(rejectedBallot.getSelectedBallotRejectionId()).isEqualTo(expected);
	}

	@DataProvider(name = "getSelectedBallotRejectionId")
	public Object[][] getSelectedBallotRejectionId() {
		return new Object[][] {
				{ null, MANGLER_OFF_STEMPEL },
				{ FREMGAR_IKKE_HVILKEN_LISTE, FREMGAR_IKKE_HVILKEN_LISTE }
		};
	}

	@Test(dataProvider = "isBallotRejectionChanged")
	public void isBallotRejectionChanged_withDataProvider_verifyExpected(String selectedBallotRejectionId, boolean expected) throws Exception {
		RejectedBallot rejectedBallot = createRejectedBallot();
		rejectedBallot.setSelectedBallotRejectionId(selectedBallotRejectionId);

		assertThat(rejectedBallot.isBallotRejectionChanged()).isEqualTo(expected);
	}

	@DataProvider(name = "isBallotRejectionChanged")
	public Object[][] isBallotRejectionChanged() {
		return new Object[][] {
				{ null, false },
				{ MANGLER_OFF_STEMPEL, false },
				{ FREMGAR_IKKE_HVILKEN_LISTE, true },
		};
	}

	@Test
	public void copyConstructor_returnsIdenticalObject() throws Exception {
		RejectedBallot rejectedBallot = createRejectedBallot();
		rejectedBallot.setState(RejectedBallot.State.MODIFIED);
		rejectedBallot.setSelectedBallotRejectionId("A");
		rejectedBallot.setSelectedBallotId("b");

		assertThat(new RejectedBallot(rejectedBallot)).isEqualToComparingFieldByField(rejectedBallot);
	}

	private RejectedBallot createRejectedBallot() {
		return new RejectedBallot("id", MANGLER_OFF_STEMPEL);
	}
}
