package no.valg.eva.admin.counting.application.modifiedballots;

import static no.valg.eva.admin.configuration.domain.model.VoteCategory.VoteCategoryValues.personal;
import static no.valg.eva.admin.configuration.domain.model.VoteCategory.VoteCategoryValues.writein;
import static no.valg.eva.admin.counting.domain.model.CastBallot.Type.MODIFIED;
import static no.valg.eva.admin.counting.domain.model.ModifiedBallotBatchProcess.MODIFIED_BALLOTS_PROCESS;
import static no.valg.eva.admin.counting.domain.model.ModifiedBallotBatchProcess.REJECTED_BALLOTS_PROCESS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import no.evote.security.UserData;
import no.valg.eva.admin.common.counting.model.BallotCountRef;
import no.valg.eva.admin.common.counting.model.BatchId;
import no.valg.eva.admin.common.counting.model.modifiedballots.BallotId;
import no.valg.eva.admin.common.counting.model.modifiedballots.Candidate;
import no.valg.eva.admin.common.counting.model.modifiedballots.CandidateRef;
import no.valg.eva.admin.common.counting.model.modifiedballots.ModifiedBallot;
import no.valg.eva.admin.common.counting.model.modifiedballots.ModifiedBallots;
import no.valg.eva.admin.configuration.domain.model.Affiliation;
import no.valg.eva.admin.configuration.domain.model.Ballot;
import no.valg.eva.admin.configuration.domain.model.Party;
import no.valg.eva.admin.configuration.domain.model.VoteCategory;
import no.valg.eva.admin.configuration.repository.CandidateRepository;
import no.valg.eva.admin.counting.domain.model.BallotCount;
import no.valg.eva.admin.counting.domain.model.CandidateVote;
import no.valg.eva.admin.counting.domain.model.CastBallot;
import no.valg.eva.admin.counting.domain.model.ContestReport;
import no.valg.eva.admin.counting.domain.model.ModifiedBallotBatch;
import no.valg.eva.admin.counting.domain.model.ModifiedBallotBatchMember;
import no.valg.eva.admin.counting.domain.model.ModifiedBallotBatchProcess;
import no.valg.eva.admin.counting.repository.ContestReportRepository;
import no.valg.eva.admin.counting.repository.CountingCodeValueRepository;
import no.valg.eva.admin.counting.repository.ModifiedBallotBatchRepository;
import no.valg.eva.admin.test.MockUtilsTestCase;

import org.testng.annotations.Test;


public class ModifiedBallotApplicationServiceTest extends MockUtilsTestCase {

	public static final BatchId BATCH_ID = new BatchId("1");
	public static final int SERIAL_NUMBER = 1;
	public static final String ANY_STRING = "1";
	public static final long ANY_LONG = 1L;

	@Test
	public void constructor_noArgs_isPresent() {
		new ModifiedBallotApplicationService();
	}

	@Test
	public void update_personalVoteOnModifiedBallot_candidateVoteIsRemovedAndCreated() {

		ModifiedBallotBatchRepository fakeModifiedBallotBatchRepository = mock(ModifiedBallotBatchRepository.class);
		ModifiedBallotApplicationService service = createModifiedBallotApplicationService(fakeModifiedBallotBatchRepository);
		ModifiedBallotBatch modifiedBallotBatch = createModifiedBallotBatch(SERIAL_NUMBER, BATCH_ID, personal);
		when(fakeModifiedBallotBatchRepository.findByBatchId(BATCH_ID)).thenReturn(modifiedBallotBatch);

		service.update(mock(UserData.class), createModifiedBallot(SERIAL_NUMBER, BATCH_ID));

		assertThat(modifiedBallotBatch.memberWithSerialnumber(SERIAL_NUMBER).getCastBallot().getCandidateVotes()).hasSize(1);
	}

	@Test
	public void load_personalCandidateVoteOnCastVote_personalVoteIsAddedOnModifiedBallot() {

		ModifiedBallotBatchRepository fakeModifiedBallotBatchRepository = mock(ModifiedBallotBatchRepository.class);
		ModifiedBallotApplicationService service = createModifiedBallotApplicationService(fakeModifiedBallotBatchRepository);
		ModifiedBallotBatch modifiedBallotBatch = createModifiedBallotBatch(SERIAL_NUMBER, BATCH_ID, personal);
		when(fakeModifiedBallotBatchRepository.findByBatchId(BATCH_ID)).thenReturn(modifiedBallotBatch);

		ModifiedBallot result = service.load(mock(UserData.class), createModifiedBallot(SERIAL_NUMBER, BATCH_ID));

		assertThat(result.personalVotes()).hasSize(1);
	}

	@Test
	public void load_writeInCandidateVoteOnCastVote_writeInVoteIsAddedOnModifiedBallot() {

		ModifiedBallotBatchRepository fakeModifiedBallotBatchRepository = mock(ModifiedBallotBatchRepository.class);
		ModifiedBallotApplicationService service = createModifiedBallotApplicationService(fakeModifiedBallotBatchRepository);
		ModifiedBallotBatch modifiedBallotBatch = createModifiedBallotBatch(SERIAL_NUMBER, BATCH_ID, writein);
		when(fakeModifiedBallotBatchRepository.findByBatchId(BATCH_ID)).thenReturn(modifiedBallotBatch);

		ModifiedBallot result = service.load(mock(UserData.class), createModifiedBallot(SERIAL_NUMBER, BATCH_ID));

		assertThat(result.getWriteIns()).hasSize(1);
	}

	@Test
	public void modifiedBallotsFor_ballotCountWithModifiedBallotsAndModifiedBallotsProcess_modifiedBallotsAreReturned() {

		CandidateRepository fakeCandidateRepository = mock(CandidateRepository.class);
		ContestReportRepository fakeContestReportRepository = mock(ContestReportRepository.class);
		ModifiedBallotApplicationService service = createModifiedBallotApplicationService(fakeCandidateRepository, fakeContestReportRepository);
		BallotCountRef ballotCountRef = new BallotCountRef(1L);
		ContestReport contestReport = mock(ContestReport.class, RETURNS_DEEP_STUBS);
		BallotCount ballotCount = createBallotCount(MODIFIED_BALLOTS_PROCESS);
		when(contestReport.getBallotCount(ballotCountRef)).thenReturn(ballotCount);
		when(fakeContestReportRepository.findByBallotCount(ballotCountRef)).thenReturn(contestReport);

		ModifiedBallots modifiedBallots = service.modifiedBallotsFor(mock(UserData.class), ballotCountRef, MODIFIED_BALLOTS_PROCESS);

		assertThat(modifiedBallots.getModifiedBallots()).hasSize(1);
	}

	@Test
	public void modifiedBallotsFor_ballotCountWithModifiedBallotsAndRejectedBallotsProcess_modifiedBallotsAreReturned() {
		CandidateRepository fakeCandidateRepository = mock(CandidateRepository.class);
		ContestReportRepository fakeContestReportRepository = mock(ContestReportRepository.class);
		ModifiedBallotApplicationService service = createModifiedBallotApplicationService(fakeCandidateRepository, fakeContestReportRepository);
		BallotCountRef ballotCountRef = new BallotCountRef(1L);
		ContestReport contestReport = mock(ContestReport.class, RETURNS_DEEP_STUBS);
		BallotCount ballotCount = createBallotCount(REJECTED_BALLOTS_PROCESS);
		when(contestReport.getBallotCount(ballotCountRef)).thenReturn(ballotCount);
		when(fakeContestReportRepository.findByBallotCount(ballotCountRef)).thenReturn(contestReport);

		ModifiedBallots modifiedBallots = service.modifiedBallotsFor(mock(UserData.class), ballotCountRef, REJECTED_BALLOTS_PROCESS);

		assertThat(modifiedBallots.getModifiedBallots()).hasSize(1);
	}

	private BallotCount createBallotCount(ModifiedBallotBatchProcess process) {
		BallotCount ballotCount = new BallotCount();
		ballotCount.addCastBallotAndSetCastBallotId(createModifiedCastBallot(process));
		ballotCount.setBallot(createBallot());
		return ballotCount;
	}

	private Ballot createBallot() {
		Ballot ballot = new Ballot();
		Affiliation affiliation = new Affiliation();
		affiliation.setParty(new Party());
		ballot.setAffiliation(affiliation);
		return ballot;
	}

	private CastBallot createModifiedCastBallot(ModifiedBallotBatchProcess process) {
		CastBallot castBallot = mock(CastBallot.class, RETURNS_DEEP_STUBS);
		when(castBallot.getType()).thenReturn(MODIFIED);
		when(castBallot.getModifiedBallotBatchMember().getModifiedBallotBatch().getProcess()).thenReturn(process);
		return castBallot;
	}

	private ModifiedBallotApplicationService createModifiedBallotApplicationService(ModifiedBallotBatchRepository fakeModifiedBallotBatchRepository) {
		return new ModifiedBallotApplicationService(fakeModifiedBallotBatchRepository, mock(CountingCodeValueRepository.class),
				mock(CandidateRepository.class),
				null);
	}

	private ModifiedBallotApplicationService createModifiedBallotApplicationService(ContestReportRepository fakeContestReportRepository) {
		return new ModifiedBallotApplicationService(mock(ModifiedBallotBatchRepository.class), mock(CountingCodeValueRepository.class),
				mock(CandidateRepository.class),
                fakeContestReportRepository);
	}

	private ModifiedBallotApplicationService createModifiedBallotApplicationService(CandidateRepository fakeCandidateRepository,
			ContestReportRepository fakeContestReportRepository) {
		return new ModifiedBallotApplicationService(mock(ModifiedBallotBatchRepository.class), mock(CountingCodeValueRepository.class),
				fakeCandidateRepository,
				fakeContestReportRepository);
	}

	private ModifiedBallot createModifiedBallot(int serialNumber, BatchId batchId) {
		ModifiedBallot modifiedBallot = new ModifiedBallot(batchId, serialNumber, ANY_STRING, anyBallotId(), false);
        Candidate candidate = anyCandidate();
        candidate.setPersonalVote(true);
        modifiedBallot.addPersonVotesFor(candidate);
		return modifiedBallot;
	}

	private Candidate anyCandidate() {
		return new Candidate("kandidatnavn", new CandidateRef(ANY_LONG), "someParty", 0);
	}

	private BallotId anyBallotId() {
		return new BallotId(ANY_STRING);
	}

	private ModifiedBallotBatch createModifiedBallotBatch(int serialNumber, BatchId batchId, VoteCategory.VoteCategoryValues voteCategoryId) {
		ModifiedBallotBatch modifiedBallotBatch = new ModifiedBallotBatch();
		modifiedBallotBatch.setId(batchId.getId());
		ModifiedBallotBatchMember batchMember = createModifiedBallotBatchMember(serialNumber, voteCategoryId);
		modifiedBallotBatch.addModifiedBallotBatchMember(batchMember);
		return modifiedBallotBatch;
	}

	private ModifiedBallotBatchMember createModifiedBallotBatchMember(int serialNumber, VoteCategory.VoteCategoryValues voteCategoryId) {
		ModifiedBallotBatchMember batchMember = new ModifiedBallotBatchMember();
		CastBallot castBallot = new CastBallot();
		castBallot.getCandidateVotes().add(createCandidateVote(voteCategoryId));
		batchMember.setCastBallot(castBallot);
		batchMember.setSerialNumber(serialNumber);
		return batchMember;
	}

	private CandidateVote createCandidateVote(VoteCategory.VoteCategoryValues voteCategoryId) {
		CandidateVote candidateVote = new CandidateVote();
		candidateVote.setVoteCategory(createVoteCategory(voteCategoryId));
		candidateVote.setCandidate(mock(no.valg.eva.admin.configuration.domain.model.Candidate.class, RETURNS_DEEP_STUBS));
		return candidateVote;
	}

	private VoteCategory createVoteCategory(VoteCategory.VoteCategoryValues voteCategoryId) {
		VoteCategory voteCategory = new VoteCategory();
		voteCategory.setId(voteCategoryId.name());
		return voteCategory;
	}
}
