package no.valg.eva.admin.counting.application;

import no.evote.model.BinaryData;
import no.evote.security.UserData;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.counting.model.ApprovedCastBallotRefForApprovedFinalCount;
import no.valg.eva.admin.common.counting.model.BallotId;
import no.valg.eva.admin.common.counting.model.BallotRejectionId;
import no.valg.eva.admin.common.counting.model.CastBallotBinaryData;
import no.valg.eva.admin.common.counting.model.CastBallotId;
import no.valg.eva.admin.common.counting.model.CountContext;
import no.valg.eva.admin.common.counting.model.RejectedCastBallotRefForApprovedFinalCount;
import no.valg.eva.admin.counting.domain.service.VoteCountService;
import no.valg.eva.admin.test.MockUtilsTestCase;
import org.testng.annotations.Test;

import static no.valg.eva.admin.common.counting.constants.ReportingUnitTypeId.VALGSTYRET;
import static no.valg.eva.admin.common.counting.model.CountCategory.VO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CastBallotBinaryDataApplicationServiceTest extends MockUtilsTestCase {
	private static final String FILE_NAME = "fileName.ext";
	private static final String MIME_TYPE = "mime/type";
	private static final byte[] BYTES = { 1, 1, 1, 1, 1, 1, 1, 1 };

	@Test
	public void rejectedCastBallotBinaryData_givenRef_returnRejectedCastBallotBinaryData() throws Exception {
		CastBallotBinaryDataApplicationService service = initializeMocks(CastBallotBinaryDataApplicationService.class);
		UserData userData = mock(UserData.class);
		RejectedCastBallotRefForApprovedFinalCount ref = rejectedCastBallotRefForApprovedFinalCount();

		BinaryData binaryData = getInjectMock(VoteCountService.class)
				.findApprovedFinalVoteCount(eq(ref.reportingUnitTypeId()), eq(ref.countContext()), eq(ref.countingAreaPath()), any())
				.getRejectedBallotCount(ref.ballotRejectionId())
				.getCastBallot(ref.castBallotId())
				.getBinaryData();
		when(binaryData.getFileName()).thenReturn(FILE_NAME);
		when(binaryData.getMimeType()).thenReturn(MIME_TYPE);
		when(binaryData.getBinaryData()).thenReturn(BYTES);

		CastBallotBinaryData castBallotBinaryData = service.rejectedCastBallotBinaryData(userData, ref);

		assertThat(castBallotBinaryData).isEqualTo(expectedCastBallotBinaryData());
	}

	@Test
	public void approvedCastBallotBinaryData_givenRef_returnApprovedCastBallotBinaryData() throws Exception {
		CastBallotBinaryDataApplicationService service = initializeMocks(CastBallotBinaryDataApplicationService.class);
		UserData userData = mock(UserData.class);
		ApprovedCastBallotRefForApprovedFinalCount ref = approvedCastBallotRefForApprovedFinalCount();

		BinaryData binaryData = getInjectMock(VoteCountService.class)
				.findApprovedFinalVoteCount(eq(ref.reportingUnitTypeId()), eq(ref.countContext()), eq(ref.countingAreaPath()), any())
				.getBallotCount(ref.ballotId())
				.getCastBallot(ref.castBallotId())
				.getBinaryData();
		when(binaryData.getFileName()).thenReturn(FILE_NAME);
		when(binaryData.getMimeType()).thenReturn(MIME_TYPE);
		when(binaryData.getBinaryData()).thenReturn(BYTES);

		CastBallotBinaryData castBallotBinaryData = service.approvedCastBallotBinaryData(userData, ref);

		assertThat(castBallotBinaryData).isEqualTo(expectedCastBallotBinaryData());
	}

	private RejectedCastBallotRefForApprovedFinalCount rejectedCastBallotRefForApprovedFinalCount() {
		ElectionPath contestPath = ElectionPath.from("111111.11.11.111111");
		CountContext countContext = new CountContext(contestPath, VO);
		AreaPath countingAreaPath = AreaPath.from("111111.11.11.1111.111111.1111");
		CastBallotId castBallotId = new CastBallotId("CB");
		BallotRejectionId ballotRejectionId = new BallotRejectionId("BR");
		return new RejectedCastBallotRefForApprovedFinalCount(VALGSTYRET, countContext, countingAreaPath, castBallotId, ballotRejectionId);
	}

	private ApprovedCastBallotRefForApprovedFinalCount approvedCastBallotRefForApprovedFinalCount() {
		ElectionPath contestPath = ElectionPath.from("111111.11.11.111111");
		CountContext countContext = new CountContext(contestPath, VO);
		AreaPath countingAreaPath = AreaPath.from("111111.11.11.1111.111111.1111");
		CastBallotId castBallotId = new CastBallotId("CB");
		BallotId ballotId = new BallotId("B");
		return new ApprovedCastBallotRefForApprovedFinalCount(VALGSTYRET, countContext, countingAreaPath, castBallotId, ballotId);
	}

	private CastBallotBinaryData expectedCastBallotBinaryData() {
		return new CastBallotBinaryData(FILE_NAME, MIME_TYPE, BYTES);
	}
}
