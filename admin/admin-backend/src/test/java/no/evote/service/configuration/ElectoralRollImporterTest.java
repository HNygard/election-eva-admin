package no.evote.service.configuration;

import com.google.common.io.Files;
import no.evote.exception.EvoteException;
import no.evote.service.TestService;
import no.valg.eva.admin.backend.configuration.repository.ElectionEventRepository;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.auditlog.auditevents.CompositeAuditEvent;
import no.valg.eva.admin.common.voting.VotingPhase;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.ElectionGroup;
import no.valg.eva.admin.configuration.domain.model.PollingPlace;
import no.valg.eva.admin.configuration.domain.model.Voter;
import no.valg.eva.admin.configuration.domain.model.VoterImportBatch;
import no.valg.eva.admin.configuration.domain.service.ElectionEventDomainService;
import no.valg.eva.admin.configuration.repository.MvAreaRepository;
import no.valg.eva.admin.configuration.repository.MvElectionRepository;
import no.valg.eva.admin.configuration.repository.VoterImportBatchRepository;
import no.valg.eva.admin.configuration.repository.VoterRepository;
import no.valg.eva.admin.test.TestGroups;
import no.valg.eva.admin.voting.service.VotingServiceBean;
import org.apache.commons.io.FileUtils;
import org.mockito.Mockito;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.ejb.SessionContext;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import static org.assertj.core.api.Assertions.assertThat;

@Test(groups = { TestGroups.SLOW, TestGroups.REPOSITORY })

public class ElectoralRollImporterTest extends ElectionBaseTest {
	private static final String VOTER_ID_9 = "01010100009";
	private static final String VOTER_ID_8 = "01010100008";
	private static final String VOTER_ID_7 = "01010100007";
	private static final String VOTER_ID_6 = "01010100006";
	private static final String VOTER_ID_5 = "01010100005";
	private static final String VOTER_ID_4 = "01010100004";
	private static final String VOTER_ID_3 = "01010100003";
	private static final String VOTER_ID_2 = "01010100002";
	private static final String VOTER_ID_1 = "01010100001";

	private static final String INITIAL_ELECTORAL_ROLL_FILE = fileOnClasspath("/INIT_20110302.TXT");

	private static final String SCHEDULED_ELECTORAL_ROLL_FILE = fileOnClasspath("/electoralRoll/UPDATE_20101122.TXT");
	private static final int SCHEDULED_ELECTORAL_ROLL_FILE_RECORDS_TOTAL = 6;
	private static final int SCHEDULED_ELECTORAL_ROLL_FILE_RECORDS_INSERT = 0;
	private static final int SCHEDULED_ELECTORAL_ROLL_FILE_RECORDS_DELETE = 0;
	private static final int SCHEDULED_ELECTORAL_ROLL_FILE_RECORDS_UPDATE = 6;
	private static final int SCHEDULED_ELECTORAL_ROLL_FILE_RECORDS_SKIP = 0;

	private static final String SCHEDULED_ELECTORAL_ROLL_NEXT_DAY_FILE = fileOnClasspath("/electoralRoll/UPDATE_20101123.TXT");
	private static final int SCHEDULED_ELECTORAL_ROLL_NEXT_DAY_FILE_RECORDS_TOTAL = 12;
	private static final int SCHEDULED_ELECTORAL_ROLL_NEXT_DAY_FILE_RECORDS_INSERT = 0;
	private static final int SCHEDULED_ELECTORAL_ROLL_NEXT_DAY_FILE_RECORDS_DELETE = 0;
	private static final int SCHEDULED_ELECTORAL_ROLL_NEXT_DAY_FILE_RECORDS_UPDATE = 6;
	private static final int SCHEDULED_ELECTORAL_ROLL_NEXT_DAY_FILE_RECORDS_SKIP = 6;

	private static final String SCHEDULED_ELECTORAL_ROLL_MOVING_FILE = fileOnClasspath("/electoralRoll/movingToNewMunicipality/UPDATE_20101125.TXT");
	private static final int SCHEDULED_ELECTORAL_ROLL_MOVING_FILE_RECORDS_TOTAL = 3;
	private static final int SCHEDULED_ELECTORAL_ROLL_MOVING_FILE_RECORDS_INSERT = 1;
	private static final int SCHEDULED_ELECTORAL_ROLL_MOVING_FILE_RECORDS_DELETE = 1;
	private static final int SCHEDULED_ELECTORAL_ROLL_MOVING_FILE_RECORDS_UPDATE = 0;
	private static final int SCHEDULED_ELECTORAL_ROLL_MOVING_FILE_RECORDS_SKIP = 1;

	private static final String NO_FILE = fileOnClasspath("/noFile.txt");
	private static final String INITIAL_ELECTORAL_ROLL_FILE_WRONG_HEADER_SHORT = fileOnClasspath("/INIT_WRONG_HEADER_SHORT.txt");
	private static final String SKD_TEST_FILE_6_PERSON_4_IN_SAMI = fileOnClasspath("/INIT_20110302.TXT");
	private static final String ELECTORAL_ROLL_FOLDER = fileOnClasspath("/electoralRoll");

	private static final long A_SECOND_IN_MILLISECONDS = 1000L;
	private static final int UPDATE_FILE_LAST_BATCH_NUMBER = 3;

	// Last update file is UPDATE_20101124.TXT
	private static final int LAST_UPDATE_FILE_NO_OF_VALID_INSERTS = 3;
	private static final int LAST_UPDATE_FILE_NO_OF_VALID_UPDATES = 2;
	private static final int LAST_UPDATE_FILE_NO_OF_SKIPS = 4;
	private static final int LAST_UPDATE_FILE_NO_OF_DELETIONS = 3;
	private static final int LAST_UPDATE_FILE_NO_OF_RECORDS_TOTAL = 12;

	private ImportElectoralRollService importElectoralRollService;
	private FullElectoralRollImporter fullElectoralRollImporter;
	private IncrementalElectoralRollImporter incrementalElectoralRollImporter;
	private VoterImportBatchRepository voterImportBatchRepository;
	private ElectionEvent electionEvent;
	private ElectionEventDomainService electionEventService;
	private ElectionEventRepository electionEventRepository;
	private TestService testService;
	private VoterServiceBean voterService;
	private MvElectionRepository mvElectionRepository;
	private VoterRepository voterRepository;
	private VotingServiceBean votingService;
	private MvAreaRepository mvAreaRepository;
	private SessionContext sessionContext;

	@Override
	@BeforeMethod(dependsOnMethods = "initDependencies")
	public void init() {
		sessionContext = backend.getSessionContext();
		electionEventService = backend.getElectionEventService();
		electionEventRepository = backend.getElectionEventRepository();
		importElectoralRollService = backend.getImportElectoralRollService();
		fullElectoralRollImporter = backend.getFullElectoralRollImporter();
		incrementalElectoralRollImporter = backend.getIncrementalElectoralRollImporter();
		voterImportBatchRepository = backend.getVoterImportBatchRepository();
		testService = backend.getTestService();
		voterService = backend.getVoterService();
		voterRepository = backend.getVoterRepository();
		votingService = backend.getVotingService();
		mvElectionRepository = backend.getMvElectionRepository();
		mvAreaRepository = backend.getMvAreaRepository();

		electionEvent = electionEventRepository.findById("200801");
	}

	@BeforeMethod
	public void initialize() {
		CompositeAuditEvent.initializeForThread();
	}

	@AfterMethod
	public void cleanUp() {
		CompositeAuditEvent.clearCollectedEvents();
	}

	@Test
	public void testFullImportElectoralRoll() throws Exception {
		prepareImport(null, electionEvent);
		importElectoralRollService.preliminaryFullImportElectoralRoll(rbacTestFixture.getSysAdminUserData(), electionEvent, INITIAL_ELECTORAL_ROLL_FILE);
		Thread.sleep(A_SECOND_IN_MILLISECONDS);
		verifyThatThereAreNoImportBatches();
	}

	@Test
	public void testFullImportElectoralRollOverrideDistrict() throws Exception {
		prepareImport(null, electionEvent);

		electionEvent.setVoterImportMunicipality(true);
		electionEvent = electionEventService.update(rbacTestFixture.getUserData(), electionEvent, null);
		importElectoralRollService.preliminaryFullImportElectoralRoll(rbacTestFixture.getUserData(), electionEvent, INITIAL_ELECTORAL_ROLL_FILE);
		verifyThatThereAreNoImportBatches();
		assertThat(voterRepository.findByElectionEventAndId(electionEvent.getPk(), VOTER_ID_1).get(0).getPollingDistrictId()).isEqualTo("0000");
		testService.deleteVoterImportBatch(rbacTestFixture.getUserData(), voterImportBatchRepository.findSingleByElectionEvent(electionEvent.getPk()).getPk());

		testService.deleteVotersByElectionEvent(rbacTestFixture.getUserData(), electionEvent.getPk());
		electionEvent = electionEventRepository.findById(electionEvent.getId());
		electionEvent.setVoterImportMunicipality(false);
		electionEvent = electionEventService.update(rbacTestFixture.getUserData(), electionEvent, null);
		importElectoralRollService.preliminaryFullImportElectoralRoll(rbacTestFixture.getUserData(), electionEvent, INITIAL_ELECTORAL_ROLL_FILE);

		assertThat(voterImportBatchRepository.findSingleByElectionEvent(electionEvent.getPk()).getLastImportBatchNumber()).isEqualTo(0);
		assertThat(voterRepository.findByElectionEventAndId(electionEvent.getPk(), VOTER_ID_1).get(0).getPollingDistrictId()).isEqualTo("0001");
	}

	@Test
	public void testScheduledImportElectoralRoll() throws IOException {
		File tempDir = Files.createTempDir();

		try {
			prepareImport(tempDir, electionEvent);
			importElectoralRollService.preliminaryFullImportElectoralRoll(rbacTestFixture.getUserData(), electionEvent, INITIAL_ELECTORAL_ROLL_FILE);
			assertThat(voterImportBatchRepository.findSingleByElectionEvent(electionEvent.getPk()).getLastImportBatchNumber()).isEqualTo(0);
			Mockito.when(sessionContext.getRollbackOnly()).thenReturn(false);

			incrementalElectoralRollImporter.incrementalImportElectoralRoll(rbacTestFixture.getUserData(), electionEvent, sessionContext);

			VoterImportBatch lastImportBatch = voterImportBatchRepository.findSingleByElectionEvent(electionEvent.getPk());
			assertThat(lastImportBatch.getLastImportBatchNumber()).isEqualTo(UPDATE_FILE_LAST_BATCH_NUMBER);
			assertThat(lastImportBatch.getLastImportRecordsInsert()).isEqualTo(LAST_UPDATE_FILE_NO_OF_VALID_INSERTS);
			assertThat(lastImportBatch.getLastImportRecordsUpdate()).isEqualTo(LAST_UPDATE_FILE_NO_OF_VALID_UPDATES);
			assertThat(lastImportBatch.getLastImportRecordsSkip()).isEqualTo(LAST_UPDATE_FILE_NO_OF_SKIPS);
			assertThat(lastImportBatch.getLastImportRecordsDelete()).isEqualTo(LAST_UPDATE_FILE_NO_OF_DELETIONS);
			assertThat(lastImportBatch.getLastImportRecordsTotal()).isEqualTo(LAST_UPDATE_FILE_NO_OF_RECORDS_TOTAL);
		} finally {
			deleteDirectory(tempDir);
		}
	}

	@Test(expectedExceptions = EvoteException.class, expectedExceptionsMessageRegExp = "@electoralRoll.importElectoralRoll.fileDoesNotExist")
	public void preliminaryFullImportElectoralRoll_withBogusFile_throwsError() throws Exception {
		prepareImport(null, electionEvent);
		importElectoralRollService.preliminaryFullImportElectoralRoll(rbacTestFixture.getUserData(), electionEvent, NO_FILE);
	}

	@Test(expectedExceptions = EvoteException.class, expectedExceptionsMessageRegExp = "@electoralRoll.importElectoralRoll.fileNotInitBatch")
	public void preliminaryFullImportElectoralRoll_withWrongHeader_throwsError() throws Exception {
		prepareImport(null, electionEvent);
		importElectoralRollService.preliminaryFullImportElectoralRoll(rbacTestFixture.getUserData(), electionEvent,
				INITIAL_ELECTORAL_ROLL_FILE_WRONG_HEADER_SHORT);
	}

	@Test(expectedExceptions = EvoteException.class, expectedExceptionsMessageRegExp = "@electoralRoll.importElectoralRoll.fileNotInitBatch")
	public void preliminaryFullImportElectoralRoll_withWrongRunNumber_throwsError() throws Exception {
		prepareImport(null, electionEvent);
		importElectoralRollService.preliminaryFullImportElectoralRoll(rbacTestFixture.getUserData(), electionEvent, SCHEDULED_ELECTORAL_ROLL_FILE);
	}

	@Test
	public void testScheduledImportElectoralRollBogusFolder() throws Exception {
		prepareImport(null, electionEvent);
		Mockito.when(sessionContext.getRollbackOnly()).thenReturn(false);
		electionEvent.setVoterImportDirName("/foo/bogusfolder");

		importElectoralRollService.preliminaryFullImportElectoralRoll(rbacTestFixture.getUserData(), electionEvent, INITIAL_ELECTORAL_ROLL_FILE);
		assertThat(voterImportBatchRepository.findSingleByElectionEvent(electionEvent.getPk()).getLastImportBatchNumber()).isEqualTo(0);

		incrementalElectoralRollImporter.incrementalImportElectoralRoll(rbacTestFixture.getUserData(), electionEvent, sessionContext);
		assertThat(voterImportBatchRepository.findSingleByElectionEvent(electionEvent.getPk()).getLastImportBatchNumber()).isEqualTo(0);
	}

	@Test
	public void testIsFileInitialBatchFile() {
		assertThat(fullElectoralRollImporter.isFileInitialBatchFile(INITIAL_ELECTORAL_ROLL_FILE)).isTrue();
		assertThat(fullElectoralRollImporter.isFileInitialBatchFile(INITIAL_ELECTORAL_ROLL_FILE_WRONG_HEADER_SHORT)).isFalse();
		assertThat(fullElectoralRollImporter.isFileInitialBatchFile("")).isFalse();
		assertThat(fullElectoralRollImporter.isFileInitialBatchFile(null)).isFalse();
		assertThat(fullElectoralRollImporter.isFileInitialBatchFile(SCHEDULED_ELECTORAL_ROLL_FILE)).isFalse();
	}

	@Test
	public void testDoesFileExist() {
		assertThat(fullElectoralRollImporter.doesFileExist(INITIAL_ELECTORAL_ROLL_FILE)).isTrue();
		assertThat(fullElectoralRollImporter.doesFileExist(NO_FILE)).isFalse();
		assertThat(fullElectoralRollImporter.doesFileExist("")).isFalse();
		assertThat(fullElectoralRollImporter.doesFileExist(null)).isFalse();
	}

	@Test
	public void testDeleteVoters() throws Exception {
		prepareImport(null, electionEvent);

		importElectoralRollService.preliminaryFullImportElectoralRoll(rbacTestFixture.getSysAdminUserData(), electionEvent, INITIAL_ELECTORAL_ROLL_FILE);
		verifyThatThereAreNoImportBatches();
		assertThat(voterRepository.areVotersInElectionEvent(electionEvent.getPk())).isTrue();

		voterService.deleteVoters(rbacTestFixture.getUserData(), mvElectionRepository.findRoot(electionEvent.getPk()),
				mvAreaRepository.findRoot(electionEvent.getPk()), false);
		assertThat(voterImportBatchRepository.findSingleByElectionEvent(electionEvent.getPk())).isNull();
		assertThat(voterRepository.areVotersInElectionEvent(electionEvent.getPk())).isFalse();
	}

	@Test
	public void testFullImportElectoralRollInformationOfSamiEligibilityBeingAdded() throws Exception {
		prepareImport(null, electionEvent);

		importElectoralRollService.preliminaryFullImportElectoralRoll(rbacTestFixture.getSysAdminUserData(), electionEvent, SKD_TEST_FILE_6_PERSON_4_IN_SAMI);

		verifyThatThereAreNoImportBatches();
		assertThat(doesVoterHaveSamiEligibilityInfoAdded(VOTER_ID_1)).isTrue();
		assertThat(doesVoterHaveSamiEligibilityInfoAdded(VOTER_ID_2)).isTrue();
		assertThat(doesVoterHaveSamiEligibilityInfoAdded(VOTER_ID_3)).isTrue();
		assertThat(doesVoterHaveSamiEligibilityInfoAdded(VOTER_ID_4)).isFalse();
		assertThat(doesVoterHaveSamiEligibilityInfoAdded(VOTER_ID_5)).isTrue();
		assertThat(doesVoterHaveSamiEligibilityInfoAdded(VOTER_ID_6)).isFalse();
	}

	private void verifyThatThereAreNoImportBatches() {
		VoterImportBatch batch = voterImportBatchRepository.findSingleByElectionEvent(electionEvent.getPk());
		assertThat(batch).isNotNull();
		assertThat(batch.getLastImportBatchNumber()).isEqualTo(0);
	}

	@Test
	public void testScheduledImportElectoralRollInformationOfSamiEligibilityBeingAdded() throws Exception {
		File tempDir = Files.createTempDir();
		Mockito.when(sessionContext.getRollbackOnly()).thenReturn(false);

		try {
			prepareImport(tempDir, electionEvent);
			importElectoralRollService.preliminaryFullImportElectoralRoll(rbacTestFixture.getUserData(), electionEvent, INITIAL_ELECTORAL_ROLL_FILE);

			assertThat(doesVoterHaveSamiEligibilityInfoAdded(VOTER_ID_1)).isTrue();
			assertThat(doesVoterHaveSamiEligibilityInfoAdded(VOTER_ID_2)).isTrue();
			assertThat(doesVoterHaveSamiEligibilityInfoAdded(VOTER_ID_3)).isTrue();
			assertThat(doesVoterHaveSamiEligibilityInfoAdded(VOTER_ID_4)).isFalse();
			assertThat(doesVoterHaveSamiEligibilityInfoAdded(VOTER_ID_5)).isTrue();
			assertThat(doesVoterHaveSamiEligibilityInfoAdded(VOTER_ID_6)).isFalse();

			incrementalElectoralRollImporter.incrementalImportElectoralRoll(rbacTestFixture.getUserData(), electionEvent, sessionContext);

			// UPDATE_20101123.TXT
			assertThat(doesVoterHaveSamiEligibilityInfoAdded(VOTER_ID_1)).isTrue();
			assertThat(doesVoterHaveSamiEligibilityInfoAdded(VOTER_ID_2)).isTrue();
			assertThat(doesVoterHaveSamiEligibilityInfoAdded(VOTER_ID_3)).isFalse();
			assertThat(doesVoterHaveSamiEligibilityInfoAdded(VOTER_ID_4)).isTrue();
			assertThat(doesVoterHaveSamiEligibilityInfoAdded(VOTER_ID_5)).isTrue();
			assertThat(doesVoterHaveSamiEligibilityInfoAdded(VOTER_ID_6)).isFalse();
			assertThat(doesVoterHaveSamiEligibilityInfoAdded(VOTER_ID_7)).isFalse();
			assertThat(doesVoterHaveSamiEligibilityInfoAdded(VOTER_ID_8)).isTrue();
			assertThat(doesVoterHaveSamiEligibilityInfoAdded(VOTER_ID_9)).isTrue();
		} finally {
			deleteDirectory(tempDir);
		}
	}

	@Test
	public void scheduledImportElectoralRoll_whenThereAlreadyIsAnApprovedVoting_entryIsSkipped() throws Exception {
		File tempDir = Files.createTempDir();
		Mockito.when(sessionContext.getRollbackOnly()).thenReturn(false);

		try {
			prepareImport(tempDir, electionEvent);

			importElectoralRollService.preliminaryFullImportElectoralRoll(rbacTestFixture.getUserData(), electionEvent, INITIAL_ELECTORAL_ROLL_FILE);

			// Add and approve voting for one of the voters
			String areaPathString = electionEvent.getId() + ".47.01.0101.010100.0002.0002"; // Hjortsberghallen i Halden
			String voterId = VOTER_ID_3;
			registerVotingFor(voterId, electionEvent, areaPathString);

			incrementalElectoralRollImporter.incrementalImportElectoralRoll(rbacTestFixture.getUserData(), electionEvent, sessionContext);

			VoterImportBatch finalVoterImportBatch = voterImportBatchRepository.findSingleByElectionEvent(electionEvent.getPk());
			assertThat(finalVoterImportBatch.getLastImportRecordsInsert()).isEqualTo(LAST_UPDATE_FILE_NO_OF_VALID_INSERTS);
			assertThat(finalVoterImportBatch.getLastImportRecordsUpdate()).isEqualTo(LAST_UPDATE_FILE_NO_OF_VALID_UPDATES);
			assertThat(finalVoterImportBatch.getLastImportRecordsSkip()).isEqualTo(LAST_UPDATE_FILE_NO_OF_SKIPS + 1); // One more because voter already had
																														// voted
			assertThat(finalVoterImportBatch.getLastImportRecordsDelete()).isEqualTo(LAST_UPDATE_FILE_NO_OF_DELETIONS - 1); // One less because voter already
																															// had voted
			assertThat(finalVoterImportBatch.getLastImportRecordsTotal()).isEqualTo(LAST_UPDATE_FILE_NO_OF_RECORDS_TOTAL);
		} finally {
			deleteDirectory(tempDir);
		}
	}

	@Test
	public void scheduledImportElectoralRoll_whenARecordAlreadyHasBeenProcessed_itIsNotProcessedTheNextTime() throws Exception {
		File tempDir = Files.createTempDir();

		try {
			prepareImport(tempDir, electionEvent);
			importElectoralRollService.preliminaryFullImportElectoralRoll(rbacTestFixture.getUserData(), electionEvent, INITIAL_ELECTORAL_ROLL_FILE);

			performAndVerifyIncrementalImportFor(SCHEDULED_ELECTORAL_ROLL_FILE, SCHEDULED_ELECTORAL_ROLL_FILE_RECORDS_TOTAL,
					SCHEDULED_ELECTORAL_ROLL_FILE_RECORDS_INSERT,
					SCHEDULED_ELECTORAL_ROLL_FILE_RECORDS_UPDATE, SCHEDULED_ELECTORAL_ROLL_FILE_RECORDS_DELETE, SCHEDULED_ELECTORAL_ROLL_FILE_RECORDS_SKIP,
					tempDir);

			performAndVerifyIncrementalImportFor(SCHEDULED_ELECTORAL_ROLL_NEXT_DAY_FILE, SCHEDULED_ELECTORAL_ROLL_NEXT_DAY_FILE_RECORDS_TOTAL,
					SCHEDULED_ELECTORAL_ROLL_NEXT_DAY_FILE_RECORDS_INSERT, SCHEDULED_ELECTORAL_ROLL_NEXT_DAY_FILE_RECORDS_UPDATE,
					SCHEDULED_ELECTORAL_ROLL_NEXT_DAY_FILE_RECORDS_DELETE, SCHEDULED_ELECTORAL_ROLL_NEXT_DAY_FILE_RECORDS_SKIP, tempDir);
		} finally {
			deleteDirectory(tempDir);
		}
	}

	@Test
	public void scheduledImportElectoralRoll_whenAPersonMovesToADifferentMunicipality_itIsRegisteredAsADeletionAndAnInsertion() throws Exception {
		File tempDir = Files.createTempDir();

		try {
			prepareImport(tempDir, electionEvent);
			importElectoralRollService.preliminaryFullImportElectoralRoll(rbacTestFixture.getUserData(), electionEvent, INITIAL_ELECTORAL_ROLL_FILE);

			performAndVerifyIncrementalImportFor(SCHEDULED_ELECTORAL_ROLL_MOVING_FILE, SCHEDULED_ELECTORAL_ROLL_MOVING_FILE_RECORDS_TOTAL,
					SCHEDULED_ELECTORAL_ROLL_MOVING_FILE_RECORDS_INSERT, SCHEDULED_ELECTORAL_ROLL_MOVING_FILE_RECORDS_UPDATE,
					SCHEDULED_ELECTORAL_ROLL_MOVING_FILE_RECORDS_DELETE, SCHEDULED_ELECTORAL_ROLL_MOVING_FILE_RECORDS_SKIP, tempDir);
		} finally {
			deleteDirectory(tempDir);
		}
	}

	private void performAndVerifyIncrementalImportFor(String incrementalElectoralRollFile, int recordsTotal, int recordsInsert, int recordsUpdate,
			int recordsDelete, int recordsSkip, File tempDir) throws IOException {
		Mockito.when(sessionContext.getRollbackOnly()).thenReturn(false);
		prepareScheduledImportFor(incrementalElectoralRollFile, tempDir);
		incrementalElectoralRollImporter.incrementalImportElectoralRoll(rbacTestFixture.getUserData(), electionEvent, sessionContext);
		verifyIncrementalImportBatch(recordsTotal, recordsInsert, recordsUpdate, recordsDelete, recordsSkip);
	}

	private void prepareScheduledImportFor(String incrementalElectoralRollFileName, File tempDir) throws IOException {
		deleteDirectory(tempDir);
		File incrementalElectoralRollFile = new File(incrementalElectoralRollFileName);
		FileUtils.copyFileToDirectory(incrementalElectoralRollFile, tempDir);
	}

	private void verifyIncrementalImportBatch(int recordsTotal, int recordsInsert, int recordsUpdate, int recordsDelete, int recordsSkip) {
		VoterImportBatch voterImportBatch = voterImportBatchRepository.findSingleByElectionEvent(electionEvent.getPk());
		assertThat(voterImportBatch.getLastImportRecordsTotal()).isEqualTo(recordsTotal);
		assertThat(voterImportBatch.getLastImportRecordsInsert()).isEqualTo(recordsInsert);
		assertThat(voterImportBatch.getLastImportRecordsUpdate()).isEqualTo(recordsUpdate);
		assertThat(voterImportBatch.getLastImportRecordsDelete()).isEqualTo(recordsDelete);
		assertThat(voterImportBatch.getLastImportRecordsSkip()).isEqualTo(recordsSkip);
	}

	private void registerVotingFor(String voterId, ElectionEvent electionEvent, String areaPathString) {
		AreaPath areaPath = AreaPath.from(areaPathString);
		Voter voter = voterRepository.voterOfId(voterId, electionEvent.getPk());
		PollingPlace pollingPlace = mvAreaRepository.findSingleByPath(areaPath).getPollingPlace();
		ElectionGroup electionGroup = mvElectionRepository.finnEnkeltMedSti(new ElectionPath("200801.01").tilValghierarkiSti()).getElectionGroup();
		votingService.markOffVoter(rbacTestFixture.getUserData(), pollingPlace, electionGroup, voter, false, VotingPhase.ELECTION_DAY);
	}

	private void prepareImport(File tempDir, ElectionEvent electionEvent) throws IOException {
		if (voterImportBatchRepository.findSingleByElectionEvent(electionEvent.getPk()) != null) {
			testService.deleteVoterImportBatch(rbacTestFixture.getUserData(),
					voterImportBatchRepository.findSingleByElectionEvent(electionEvent.getPk()).getPk());
		}

		if (tempDir != null) {
			copyFilesTo(tempDir);
			electionEvent.setVoterImportDirName("/" + tempDir.getCanonicalPath());
		}
	}

	private boolean doesVoterHaveSamiEligibilityInfoAdded(final String id) {
		Voter voter = voterRepository.findByElectionEventAndId(electionEvent.getPk(), id).get(0);
		return voter.isStemmerettOgsaVedSametingsvalg();
	}

	private void copyFilesTo(File destDir) throws IOException {
		File directoryFrom = new File(ELECTORAL_ROLL_FOLDER);
		File[] filesFrom = directoryFrom.listFiles();
		for (File f : filesFrom) {
			if (f.getName().matches("UPDATE.*")) {
				FileUtils.copyFileToDirectory(f, destDir);
			}
		}
	}

	private void deleteDirectory(File dir) {
		FileUtils.deleteQuietly(dir);
	}

	private static String fileOnClasspath(String resourceOnClasspath) {
		URL resource = ElectoralRollImporterTest.class.getResource(resourceOnClasspath);
		return resource != null ? resource.getFile() : null;
	}
}

