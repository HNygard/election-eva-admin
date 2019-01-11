package no.valg.eva.admin.counting.application;

import static java.util.Arrays.asList;
import static no.valg.eva.admin.common.counting.constants.ReportingUnitTypeId.VALGSTYRET;
import static no.valg.eva.admin.common.counting.model.CountCategory.VO;
import static no.valg.eva.admin.counting.domain.model.CastBallot.Type.MODIFIED;
import static no.valg.eva.admin.counting.domain.model.CastBallot.Type.REJECTED;
import static no.valg.eva.admin.counting.domain.model.CastBallot.Type.UNMODIFIED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import no.evote.security.UserData;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.counting.model.ApprovedBallot;
import no.valg.eva.admin.common.counting.model.ApprovedFinalCountRef;
import no.valg.eva.admin.common.counting.model.CountContext;
import no.valg.eva.admin.common.counting.model.RejectedBallot;
import no.valg.eva.admin.configuration.domain.model.Ballot;
import no.valg.eva.admin.configuration.domain.model.BallotRejection;
import no.valg.eva.admin.counting.domain.model.BallotCount;
import no.valg.eva.admin.counting.domain.model.CastBallot;
import no.valg.eva.admin.counting.domain.service.CastBallotDomainService;
import no.valg.eva.admin.counting.domain.service.VoteCountService;
import no.valg.eva.admin.test.MockUtilsTestCase;

import org.testng.annotations.Test;

public class CastBallotApplicationServiceTest extends MockUtilsTestCase {
	private static final String B1 = "B1";
	private static final String B2 = "B2";
	private static final String B3 = "B3";
	private static final String B4 = "B4";
	private static final String BR1 = "BR1";
	private static final String BR2 = "BR2";
	private static final String BR3 = "BR3";
	private static final String BR4 = "BR4";
	private static final String CB1 = "CB1";
	private static final String CB2 = "CB2";
	private static final String CB3 = "CB3";

	@Test
	public void approvedBallots_givenApprovedFinalCountRef_returnApprovedBallots() throws Exception {
		CastBallotApplicationService service = initializeMocks(CastBallotApplicationService.class);
		UserData userData = mock(UserData.class, RETURNS_DEEP_STUBS);

		when(getInjectMock(VoteCountService.class)
				.findApprovedFinalVoteCount(approvedFinalCountRef(), userData.getOperatorAreaPath())
				.getBallotCountMap()
				.values())
				.thenReturn(ballotCounts(
						approvedBallotCount(B1, 1, 0, castBallot(CB1, MODIFIED)),
						approvedBallotCount(B2, 0, 1, castBallot(CB2, UNMODIFIED)),
						approvedBallotCount(B3, 1, 0, castBallot(CB3, MODIFIED)),
						approvedBallotCount(B4, 1, 0)
						));

		List<ApprovedBallot> approvedBallots = service.approvedBallots(userData, approvedFinalCountRef());

		assertThat(approvedBallots).containsExactly(modifiedBallot(CB1, B1), unmodifiedBallot(CB2, B2), modifiedBallot(CB3, B3));
	}

	@Test
	public void rejectedBallot_givenApprovedFinalCountRef_returnRejectedBallots() throws Exception {
		CastBallotApplicationService service = initializeMocks(CastBallotApplicationService.class);
		UserData userData = mock(UserData.class, RETURNS_DEEP_STUBS);

		when(getInjectMock(VoteCountService.class)
				.findApprovedFinalVoteCount(approvedFinalCountRef(), userData.getOperatorAreaPath())
				.getRejectedBallotCountMap()
				.values())
				.thenReturn(ballotCounts(
						rejectedBallotCount(BR1, 1, castBallot(CB1, REJECTED)),
						rejectedBallotCount(BR2, 1, castBallot(CB2, REJECTED)),
						rejectedBallotCount(BR3, 1, castBallot(CB3, REJECTED)),
						rejectedBallotCount(BR4, 0)
						));

		List<RejectedBallot> rejectedBallots = service.rejectedBallots(userData, approvedFinalCountRef());

		assertThat(rejectedBallots).containsExactly(rejectedBallot(CB1, BR1), rejectedBallot(CB2, BR2), rejectedBallot(CB3, BR3));
	}

	private ApprovedFinalCountRef approvedFinalCountRef() {
		return new ApprovedFinalCountRef(
				VALGSTYRET, new CountContext(ElectionPath.from("111111.11.11.111111"), VO), AreaPath.from("111111.11.11.1111.111111.1111"));
	}

	private Collection<BallotCount> ballotCounts(BallotCount... ballotCounts) {
		return asList(ballotCounts);
	}

	private BallotCount approvedBallotCount(String ballotId, int modifiedBallots, int unmodifiedBallots, CastBallot... castBallots) {
		return ballotCount(ballot(ballotId), null, modifiedBallots, unmodifiedBallots, castBallots);
	}

	private BallotCount rejectedBallotCount(String ballotRejectionId, int counts, CastBallot... castBallots) {
		return ballotCount(null, ballotRejection(ballotRejectionId), 0, counts, castBallots);
	}

	private BallotCount ballotCount(Ballot ballot, BallotRejection ballotRejection, int modifiedBallots, int unmodifiedBallots, CastBallot... castBallots) {
		BallotCount ballotCount = new BallotCount();
		ballotCount.setPk(new Random().nextLong());
		if (ballot != null) {
			ballotCount.setBallot(ballot);
		} else {
			ballotCount.setBallotRejection(ballotRejection);
		}
		ballotCount.setModifiedBallots(modifiedBallots);
		ballotCount.setUnmodifiedBallots(unmodifiedBallots);
		for (CastBallot castBallot : castBallots) {
			castBallot.setBallotCount(ballotCount);
			ballotCount.getCastBallots().add(castBallot);
		}
		return ballotCount;
	}

	private Ballot ballot(String id) {
		Ballot ballot = new Ballot();
		ballot.setPk(new Random().nextLong());
		ballot.setId(id);
		return ballot;
	}

	private BallotRejection ballotRejection(String ballotRejectionId) {
		BallotRejection ballotRejection = new BallotRejection();
		ballotRejection.setPk(new Random().nextLong());
		ballotRejection.setId(ballotRejectionId);
		return ballotRejection;
	}

	private CastBallot castBallot(String id, CastBallot.Type type) {
		CastBallot castBallot = new CastBallot();
		castBallot.setPk(new Random().nextLong());
		castBallot.setId(id);
		castBallot.setType(type);
		return castBallot;
	}

	private ApprovedBallot modifiedBallot(String castBallotId, String ballotId) {
		return new ApprovedBallot(castBallotId, ballotId, true);
	}

	private ApprovedBallot unmodifiedBallot(String castBallotId, String ballotId) {
		return new ApprovedBallot(castBallotId, ballotId, false);
	}

	private RejectedBallot rejectedBallot(String castBallotId, String ballotRejectionId) {
		return new RejectedBallot(castBallotId, ballotRejectionId);
	}

	@Test
	public void processRejectedBallots_givenApprovedFinalCountRefAndRejectedBallots_processesRejectedBallots() throws Exception {
		CastBallotApplicationService service = initializeMocks(CastBallotApplicationService.class);
		UserData userData = mock(UserData.class, RETURNS_DEEP_STUBS);
		List<RejectedBallot> rejectedBallots = new ArrayList<>();

		service.processRejectedBallots(userData, approvedFinalCountRef(), rejectedBallots);

		verify(getInjectMock(CastBallotDomainService.class)).processRejectedBallots(userData, approvedFinalCountRef(), rejectedBallots);
	}
}
