package no.valg.eva.admin.counting.service.impl;

import com.google.common.primitives.Ints;
import no.evote.constants.AreaLevelEnum;
import no.evote.constants.ElectionLevelEnum;
import no.evote.constants.EvoteConstants;
import no.evote.exception.EvoteException;
import no.evote.security.UserData;
import no.evote.service.CryptoServiceBean;
import no.evote.service.backendmock.BackendContainer;
import no.evote.service.backendmock.CountingImportTestFixture;
import no.evote.service.backendmock.ListProposalTestFixture;
import no.evote.service.backendmock.RBACTestFixture;
import no.evote.service.backendmock.ServiceBackedRBACTestFixture;
import no.evote.service.counting.CountingImportServiceBean;
import no.evote.service.counting.LegacyCountingServiceBean;
import no.evote.util.SignIt;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.configuration.domain.model.Candidate;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.configuration.domain.model.ReportingUnit;
import no.valg.eva.admin.configuration.domain.model.VoteCountCategory;
import no.valg.eva.admin.configuration.repository.CandidateRepository;
import no.valg.eva.admin.configuration.repository.MvAreaRepository;
import no.valg.eva.admin.configuration.repository.MvElectionRepository;
import no.valg.eva.admin.configuration.repository.ReportingUnitRepository;
import no.valg.eva.admin.configuration.repository.VoteCountCategoryRepository;
import no.valg.eva.admin.counting.domain.model.AntallStemmesedlerLagtTilSide;
import no.valg.eva.admin.counting.domain.model.BallotCount;
import no.valg.eva.admin.counting.domain.model.CandidateVote;
import no.valg.eva.admin.counting.domain.model.CastBallot;
import no.valg.eva.admin.counting.domain.model.ContestReport;
import no.valg.eva.admin.counting.domain.model.CountQualifier;
import no.valg.eva.admin.counting.domain.model.VoteCount;
import no.valg.eva.admin.counting.domain.model.VoteCountStatus;
import no.valg.eva.admin.counting.repository.AntallStemmesedlerLagtTilSideRepository;
import no.valg.eva.admin.counting.repository.BallotCountRepository;
import no.valg.eva.admin.counting.repository.CastBallotRepository;
import no.valg.eva.admin.counting.repository.ContestReportRepository;
import no.valg.eva.admin.counting.repository.CountingCodeValueRepository;
import no.valg.eva.admin.crypto.CryptoException;
import no.valg.eva.admin.felles.sti.valghierarki.ValgdistriktSti;
import no.valg.eva.admin.opptelling.repository.VoteCountRepository;
import no.valg.eva.admin.test.AbstractJpaTestBase;
import no.valg.eva.admin.test.GenericTestRepository;
import no.valg.eva.admin.test.TestGroups;
import no.valg.eva.admin.util.IOUtil;
import no.valg.eva.admin.util.XMLUtil;
import org.jdom2.Content;
import org.jdom2.Document;
import org.jdom2.Element;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.enterprise.event.Event;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static no.evote.service.backendmock.BaseTestFixture.fileFromResources;
import static no.valg.eva.admin.common.counting.constants.ReportingUnitTypeId.STEMMESTYRET;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

@Test(groups = TestGroups.REPOSITORY)
public class CountingImportServiceBeanTest extends AbstractJpaTestBase {

    private static final int APPROVED_BALLOTS = 5;
    private final VerificationFunction noVerification = new VerificationFunction() {
        @Override
        public void verify(BallotCount ballotCount) {
            // Intentionally empty
        }
    };
    private MvAreaRepository mvAreaRepository;
    private MvElectionRepository mvElectionRepository;
    private VoteCountRepository voteCountRepository;
    private ReportingUnitRepository reportingUnitRepository;
    private VoteCountCategoryRepository voteCountCategoryRepository;
    private LegacyCountingServiceBean legacyCountingService;
    private ContestReportRepository contestReportRepository;
    private CandidateRepository candidateRepository;
    private CastBallotRepository castBallotRepository;
    private CountingCodeValueRepository countingCodeValueRepository;
    private BallotCountRepository ballotCountRepository;
    private AntallStemmesedlerLagtTilSideRepository antallStemmesedlerLagtTilSideRepository;
    private GenericTestRepository genericTestRepository;
    private UserData userData;
    private BackendContainer backend;
    private CountingImportTestFixture countingImportTestFixture;

    private CountingImportServiceBean countingImportService;

    @BeforeMethod(alwaysRun = true)
    public void initBackend() throws Exception {
        // signaturvalidering er ikke interessant for denne testen
        CryptoServiceBean cryptoServiceBean = mock(CryptoServiceBean.class);
        byte[] someBytes = new byte[]{32, 32, 32, 32};
        when(cryptoServiceBean.encryptWithSystemPassword(any(byte[].class))).thenReturn(someBytes);

        Event mockEvent = mock(Event.class);
        backend = new BackendContainer(getEntityManager(), cryptoServiceBean, mockEvent);
        backend.initServices();
        setupTransactionSynchronizationRegistry();

        voteCountRepository = backend.getVoteCountRepository();
        reportingUnitRepository = backend.getReportingUnitRepository();
        voteCountCategoryRepository = backend.getVoteCountCategoryRepository();
        legacyCountingService = backend.getLegacyCountingService();
        contestReportRepository = backend.getContestReportRepository();
        candidateRepository = backend.getCandidateRepository();
        mvAreaRepository = backend.getMvAreaRepository();
        mvElectionRepository = backend.getMvElectionRepository();
        castBallotRepository = backend.getCastBallotRepository();
        countingCodeValueRepository = backend.getCountingCodeValueRepository();
        ballotCountRepository = backend.getBallotCountRepository();
        antallStemmesedlerLagtTilSideRepository = backend.getAntallStemmesedlerLagtTilSideRepository();
        genericTestRepository = backend.getGenericTestRepository();

        RBACTestFixture rbacTestFixture = new ServiceBackedRBACTestFixture(backend);
        rbacTestFixture.init();
        ListProposalTestFixture listProposalTestFixture = new ListProposalTestFixture(backend);
        listProposalTestFixture.init();

        userData = rbacTestFixture.getSysAdminUserData();
        backend.getSystemPasswordStore().setPassword("system");
        countingImportTestFixture = new CountingImportTestFixture(backend);
        countingImportTestFixture.init();

        countingImportService = backend.getCountingImportService();
    }

    @Test
    public void importCountEmlZip_lokaltFordeltPaaKretsForMoss_urneTellingOpprettesOgForelopigTellingBlirImportert() throws Exception {
        MvArea mvArea = mvAreaRepository.findSingleByPath(new AreaPath("900010.47.01.0104"));
        MvElection mvElection = mvElectionRepository.finnEnkeltMedSti(new ValgdistriktSti("900010", "01", "01", "000001"));
        setAntallStemmesedlerLagtTilSideTo0(mvArea, mvElection);
        importCountEML(CountXML.preliminaryCount()
                .forContest("900010", "01", "01", "000001")
                .forDistrict("47.01.0104.010400.0001")
                .withReportingUnitId("01-47.01.0101")
                .withVoteCountCategory("VO")
                .withPartyVotes("9998", "TESTIDA", 37)
                .withPartyVotes("9997", "TESTIDB", 200));
        verifyBallotCounts("900010.01.01.000001", "900010.47.01.0104.010400.0001", "F", 237, 0, new HashMap<String, List<Integer>>() {
            {
                put("TESTIDA", Ints.asList(37));
                put("TESTIDB", Ints.asList(200));
                put("TESTIDC", Ints.asList(0));
                put("TESTIDD", Ints.asList(0));
            }
        }, noVerification, "VO", true);
    }

    private void setAntallStemmesedlerLagtTilSideTo0(MvArea mvArea, MvElection mvElection) {
        AntallStemmesedlerLagtTilSide antallStemmesedlerLagtTilSide =
                new AntallStemmesedlerLagtTilSide(mvArea.getMunicipality(), mvElection.getElectionGroup(), mvElection.getContest(), 0);
        antallStemmesedlerLagtTilSideRepository.create(userData, antallStemmesedlerLagtTilSide);
    }

    @Test
    public void preliminaryResultForHalden0001ShouldBeImported() throws Exception {
        MvArea mvArea = mvAreaRepository.findSingleByPath(new AreaPath("900010.47.01.0101"));
        MvElection mvElection = mvElectionRepository.finnEnkeltMedSti(new ValgdistriktSti("900010", "01", "01", "000001"));
        setAntallStemmesedlerLagtTilSideTo0(mvArea, mvElection);
        importCountEML(CountXML.preliminaryCount().forContest("900010", "01", "01", "000001").forDistrict("47.01.0101.010100.0001")
                .withReportingUnitId("01-47.01.0101").withVoteCountCategory("VO").withPartyVotes("9998", "TESTIDA", 37).withPartyVotes("9997", "TESTIDB", 200));
        verifyBallotCounts("900010.01.01.000001", "900010.47.01.0101.010100.0001", "F", 237, 0, new HashMap<String, List<Integer>>() {
            {
                put("TESTIDA", Ints.asList(37));
                put("TESTIDB", Ints.asList(200));
                put("TESTIDC", Ints.asList(0));
                put("TESTIDD", Ints.asList(0));
            }
        }, noVerification, "VO", false);
    }

    private void importCountEML(final CountData countData) throws IOException, CryptoException, URISyntaxException {
        File tempDirectory = IOUtil.createTemporaryDirectory();
        countData.writeFiles(tempDirectory);
        importCountEML(tempDirectory.getAbsolutePath());
    }

    private void importCountEML(final CountXML countXML) throws IOException, CryptoException, URISyntaxException {
        File file = IOUtil.makeFile(countXML.toString().getBytes(), "Count-001.xml");
        importCountEML(file.getParentFile().getAbsolutePath());
    }

    @Test(dependsOnMethods = {"preliminaryResultForHalden0001ShouldBeImported"})
    // depends on har ingenting å si for tx tester - må skrives om
    public void shouldBePossibleToImportPreliminaryResultsTwice() throws Exception {
        MvArea mvArea = mvAreaRepository.findSingleByPath(new AreaPath("900010.47.01.0101"));
        MvElection mvElection = mvElectionRepository.finnEnkeltMedSti(new ValgdistriktSti("900010", "01", "01", "000001"));
        setAntallStemmesedlerLagtTilSideTo0(mvArea, mvElection);
        importCountEML(CountXML.preliminaryCount().forContest("900010", "01", "01", "000001").forDistrict("47.01.0101.010100.0001")
                .withReportingUnitId("01-47.01.0101").withVoteCountCategory("VO").withPartyVotes("9998", "TESTIDA", 101)
                .withPartyVotes("9997", "TESTIDB", 199));

        verifyBallotCounts("900010.01.01.000001", "900010.47.01.0101.010100.0001", "F", 300, 0, new HashMap<String, List<Integer>>() {
            {
                put("TESTIDA", Ints.asList(101));
                put("TESTIDB", Ints.asList(199));
                put("TESTIDC", Ints.asList(0));
                put("TESTIDD", Ints.asList(0));
            }
        }, noVerification, "VO", false);
    }

    @Test
    public void preliminaryResultForHalden0002ShouldBeImported() throws Exception {
        MvArea mvArea = mvAreaRepository.findSingleByPath(new AreaPath("900010.47.01.0101"));
        MvElection mvElection = mvElectionRepository.finnEnkeltMedSti(new ValgdistriktSti("900010", "01", "01", "000001"));
        setAntallStemmesedlerLagtTilSideTo0(mvArea, mvElection);
        CountData countData = new CountData().withCountXML(
                CountXML.preliminaryCount().forContest("900010", "01", "01", "000001").forDistrict("47.01.0101.010100.0002")
                        .withReportingUnitId("01-47.01.0101").withVoteCountCategory("VO").withPartyVotes("9998", "TESTIDA", 100, 1)
                        .withPartyVotes("9997", "TESTIDB", 200, 2))
                .withVotesXML(
                        new VotesXML()
                                .reportingOn("01.01.000001-47.01")
                                .forContest("900010.01.01.000001")
                                .forDistrict("47.01.0101.010100.0002")
                                .withModifiedBallot("9998", "TESTIDA", VotesXML.Correction.personalVote("01", "00000000001"))
                                .withModifiedBallot("9997", "TESTIDB", VotesXML.Correction.personalVote("02", "00000000003"))
                                .withModifiedBallot("9997", "TESTIDB", VotesXML.Correction.personalVote("01", "00000000002"),
                                        VotesXML.Correction.writeIn("02", "00000000002", "9998", "TESTIDA")));
        importCountEML(countData);

        verifyBallotCounts("900010.01.01.000001", "900010.47.01.0101.010100.0002", "F", 300, 0, new HashMap<String, List<Integer>>() {
            {
                put("TESTIDA", Ints.asList(99, 1));
                put("TESTIDB", Ints.asList(198, 2));
                put("TESTIDC", Ints.asList(0));
                put("TESTIDD", Ints.asList(0));
            }
        }, ballotCount -> {
            String ballotId = ballotCount.getBallot().getId();
            if ("TESTIDA".equals(ballotId)) {
                List<CastBallot> castBallots = castBallotRepository.findCastBallotsByBallotCount(ballotCount.getPk());
                assertEquals(castBallots.size(), 1, "There should be exactly one modified ballot for TESTIDA");

                List<CandidateVote> candidateVotes = castBallotRepository.findCandidateVoteByCastBallot(castBallots.get(0).getPk());
                assertEquals(candidateVotes.size(), 1, "There should be exactly 1 candidate vote");

                Candidate candidate = candidateRepository.findCandidateByPk(candidateVotes.get(0).getCandidate().getPk());
                assertEquals(candidate.getId(), "00000000000");
            } else if ("TESTIDB".equals(ballotId)) {
                List<CastBallot> castBallots = castBallotRepository.findCastBallotsByBallotCount(ballotCount.getPk());
                assertEquals(castBallots.size(), 2, "There should be exactly one modified ballot for TESTIDB");

                for (CastBallot castBallot : castBallots) {
                    List<CandidateVote> candidateVotes = castBallotRepository.findCandidateVoteByCastBallot(castBallot.getPk());
                    if (candidateVotes.size() == 1) {
                        shouldBePersonalVoteFor(candidateVotes.get(0), "00000000003");
                    } else if (candidateVotes.size() == 2) {
                        for (CandidateVote candidateVote : candidateVotes) {
                            if (isPersonalVote(candidateVote)) {
                                shouldBeVoteFor(candidateVote, "00000000002");
                            } else if (isWriteIn(candidateVote)) {
                                shouldBeVoteFor(candidateVote, "00000000001");
                                Candidate candidate = candidateRepository.findCandidateByPk(candidateVote.getCandidate().getPk());
                                assertEquals(candidate.getBallot().getId(), "TESTIDA");
                            } else {
                                fail("Unknown vote category");
                            }
                        }
                    }
                }
            }
        }, "VO", false);
    }

    @Test(expectedExceptions = EvoteException.class)
    public void importingFinalResultsForHalden0001BeforeApprovedPreliminaryShouldFail() throws Exception {
        importCountEML(CountXML.finalCount().forContest("900010", "01", "01", "000001").forDistrict("47.01.0101.010100.0001")
                .withReportingUnitId("01-47.01.0101").withVoteCountCategory("VO").withPartyVotes("9998", "TESTIDA", 100)
                .withPartyVotes("9997", "TESTIDB", 200));
        fail();
    }

    @Test
    public void finalResultsForHalden0001ShouldBeImported() throws Exception {
        MvArea mvArea = mvAreaRepository.findSingleByPath(new AreaPath("900010.47.01.0101"));
        MvElection mvElection = mvElectionRepository.finnEnkeltMedSti(new ValgdistriktSti("900010", "01", "01", "000001"));
        setAntallStemmesedlerLagtTilSideTo0(mvArea, mvElection);
        createAndApproveProtocolCount("900010", "900010.01.01.000001", "900010.47.01.0101.010100.0001");
        createAndApprovePreliminaryCount("900010.01.01.000001", "900010.47.01.0101.010100.0001");
        importCountEML(CountXML.finalCount().forContest("900010", "01", "01", "000001").forDistrict("47.01.0101.010100.0001")
                .withReportingUnitId("01-47.01.0101").withVoteCountCategory("VO").withPartyVotes("9998", "TESTIDA", 100)
                .withPartyVotes("9997", "TESTIDB", 200));
    }

    @Test
    public void finalResultsForHalden0002ShouldBeImported() throws Exception {
        MvArea mvArea = mvAreaRepository.findSingleByPath(new AreaPath("900010.47.01.0101"));
        MvElection mvElection = mvElectionRepository.finnEnkeltMedSti(new ValgdistriktSti("900010", "01", "01", "000001"));
        setAntallStemmesedlerLagtTilSideTo0(mvArea, mvElection);
        createAndApproveProtocolCount("900010", "900010.01.01.000001", "900010.47.01.0101.010100.0002");
        createAndApprovePreliminaryCount("900010.01.01.000001", "900010.47.01.0101.010100.0002");
        importCountEML(new CountData().withCountXML(
                CountXML.finalCount().forContest("900010", "01", "01", "000001").forDistrict("47.01.0101.010100.0002").withReportingUnitId("01-47.01.0101")
                        .withVoteCountCategory("VO").withPartyVotes("9998", "TESTIDA", 98, 1).withPartyVotes("9997", "TESTIDB", 199, 1))
                .withVotesXML(
                        new VotesXML().forContest("900010.01.01.000001").forDistrict("900010.47.01.0101.010100.0002").reportingOn("01-47.01.0101")
                                .withModifiedBallot("9998", "TESTIDA", VotesXML.Correction.personalVote("02", "00000000001"))
                                .withModifiedBallot("9997", "TESTIDB", VotesXML.Correction.writeIn("02", "00000000007", "9995", "TESTIDD"))));

        verifyBallotCounts("900010.01.01.000001", "900010.47.01.0101.010100.0002", "E", 297, 0, new HashMap<String, List<Integer>>() {
            {
                put("TESTIDA", Ints.asList(97, 1));
                put("TESTIDB", Ints.asList(198, 1));
                put("TESTIDC", Ints.asList(0));
                put("TESTIDD", Ints.asList(0));
            }
        }, ballotCount -> {
            String ballotId = ballotCount.getBallot().getId();
            if ("TESTIDA".equals(ballotId)) {
                List<CastBallot> castBallots = castBallotRepository.findCastBallotsByBallotCount(ballotCount.getPk());
                assertEquals(castBallots.size(), 1,
                        "There should be exactly one modified ballot for TESTIDA and ballot count pk " + ballotCount.getPk());

                List<CandidateVote> candidateVotes = castBallotRepository.findCandidateVoteByCastBallot(castBallots.get(0).getPk());
                assertEquals(candidateVotes.size(), 1, "There should be exactly 1 candidate vote");

                shouldBePersonalVoteFor(candidateVotes.get(0), "00000000001");
            } else if ("TESTIDB".equals(ballotId)) {
                List<CastBallot> castBallots = castBallotRepository.findCastBallotsByBallotCount(ballotCount.getPk());
                assertEquals(castBallots.size(), 1,
                        "There should be exactly one modified ballot for TESTIDB and ballot count pk " + ballotCount.getPk());

                List<CandidateVote> candidateVotes = castBallotRepository.findCandidateVoteByCastBallot(castBallots.get(0).getPk());
                assertEquals(candidateVotes.size(), 1, "There should be exactly 1 candidate vote");

                CandidateVote candidateVote = candidateVotes.get(0);
                shouldBeWriteInFor(candidateVote, "00000000007");
                Candidate candidate = candidateRepository.findCandidateByPk(candidateVote.getCandidate().getPk());
                assertEquals(candidate.getBallot().getId(), "TESTIDD");
            }
        }, "VO", false);
    }

    @Test(expectedExceptions = EvoteException.class, expectedExceptionsMessageRegExp = "Unable to find polling.*")
    public void importingPreliminaryAdvanceResultsForOsloOnNonTechnicalDistrictsShouldFail() throws Exception {
        importCountEML(CountXML.preliminaryCount().forContest("900010", "01", "01", "000001").forDistrict("47.03.0301.030101.0080")
                .withReportingUnitId("01-47.03.0301").withVoteCountCategory("FO").withPartyVotes("9998", "TESTIDA", 101)
                .withPartyVotes("9997", "TESTIDB", 99).withPartyVotes("9996", "TESTIDC", 106).withPartyVotes("9995", "TESTIDD", 94));
    }

    @Test
    public void importingPreliminaryResultsForOslo() throws Exception {
        MvArea mvArea = mvAreaRepository.findSingleByPath(new AreaPath("900010.47.03.0301"));
        MvElection mvElection = mvElectionRepository.finnEnkeltMedSti(new ValgdistriktSti("900010", "01", "01", "000001"));
        setAntallStemmesedlerLagtTilSideTo0(mvArea, mvElection);
        importCountEML(new CountData().withCountXML(
                CountXML.preliminaryCount().forContest("900010", "01", "01", "000001").forDistrict("47.03.0301.030101.0101")
                        .withReportingUnitId("01-47.03.0301").withVoteCountCategory("VO").withPartyVotes("9998", "TESTIDA", 421)
                        .withPartyVotes("9997", "TESTIDB", 240).withPartyVotes("9996", "TESTIDC", 339))
                .withCountXML(
                        CountXML.preliminaryCount().forContest("900010", "01", "01", "000001").forDistrict("47.03.0301.030102.0201")
                                .withReportingUnitId("01-47.03.0301").withVoteCountCategory("VO").withPartyVotes("9998", "TESTIDA", 430)
                                .withPartyVotes("9997", "TESTIDB", 240).withPartyVotes("9996", "TESTIDC", 330)));

        verifyBallotCounts("900010.01.01.000001", "900010.47.03.0301.030101.0101", "F", 1000, 0, new HashMap<String, List<Integer>>() {
            {
                put("TESTIDA", Ints.asList(421));
                put("TESTIDB", Ints.asList(240));
                put("TESTIDC", Ints.asList(339));
                put("TESTIDD", Ints.asList(0));
            }
        }, noVerification, "VO", false);

        verifyBallotCounts("900010.01.01.000001", "900010.47.03.0301.030102.0201", "F", 1000, 0, new HashMap<String, List<Integer>>() {
            {
                put("TESTIDA", Ints.asList(430));
                put("TESTIDB", Ints.asList(240));
                put("TESTIDC", Ints.asList(330));
                put("TESTIDD", Ints.asList(0));
            }
        }, noVerification, "VO", false);
    }

    @Test
    public void importingPreliminaryResultsForOsloBydelsvalg() throws Exception {
        MvArea mvArea = mvAreaRepository.findSingleByPath(new AreaPath("900010.47.03.0301"));
        MvElection mvElection = mvElectionRepository.finnEnkeltMedSti(new ValgdistriktSti("900010", "01", "01", "000001"));
        setAntallStemmesedlerLagtTilSideTo0(mvArea, mvElection);
        importCountEML(new CountData().withCountXML(
                CountXML.preliminaryCount().forContest("900010", "01", "01", "000001").forDistrict("47.03.0301.030101")
                        .withReportingUnitId("01-47.03.0301").withVoteCountCategory("VO").withPartyVotes("9998", "TESTIDA", 421)
                        .withPartyVotes("9997", "TESTIDB", 240).withPartyVotes("9996", "TESTIDC", 339))
                .withCountXML(
                        CountXML.preliminaryCount().forContest("900010", "01", "01", "000001").forDistrict("47.03.0301.030102")
                                .withReportingUnitId("01-47.03.0301").withVoteCountCategory("VO").withPartyVotes("9998", "TESTIDA", 430)
                                .withPartyVotes("9997", "TESTIDB", 240).withPartyVotes("9996", "TESTIDC", 330)));

        verifyBallotCounts("900010.01.01.000001", "900010.47.03.0301.030101", "F", 1000, 0, new HashMap<String, List<Integer>>() {
            {
                put("TESTIDA", Ints.asList(421));
                put("TESTIDB", Ints.asList(240));
                put("TESTIDC", Ints.asList(339));
                put("TESTIDD", Ints.asList(0));
            }
        }, noVerification, "VO", false);

        verifyBallotCounts("900010.01.01.000001", "900010.47.03.0301.030102", "F", 1000, 0, new HashMap<String, List<Integer>>() {
            {
                put("TESTIDA", Ints.asList(430));
                put("TESTIDB", Ints.asList(240));
                put("TESTIDC", Ints.asList(330));
                put("TESTIDD", Ints.asList(0));
            }
        }, noVerification, "VO", false);
    }

    @Test
    public void importCountEmlZip_withoutPartyVotes_PartyVotesWithZeroIsCreated() throws Exception {
        MvArea mvArea = mvAreaRepository.findSingleByPath(new AreaPath("900010.47.01.0104"));
        MvElection mvElection = mvElectionRepository.finnEnkeltMedSti(new ValgdistriktSti("900010", "01", "01", "000001"));
        setAntallStemmesedlerLagtTilSideTo0(mvArea, mvElection);

        importCountEML(CountXML.preliminaryCount()
                .forContest("900010", "01", "01", "000001")
                .forDistrict("47.01.0104.010400.0001")
                .withReportingUnitId("01-47.01.0101")
                .withVoteCountCategory("VO")
                .withPartyVotes("9998", "TESTIDA", 37)
                .withPartyVotes("9997", "TESTIDB", 200));

        verifyBallotCounts("900010.01.01.000001", "900010.47.01.0104.010400.0001", "F", 237, 0, new HashMap<String, List<Integer>>() {
            {
                put("TESTIDA", Ints.asList(37));
                put("TESTIDB", Ints.asList(200));
                put("TESTIDC", Ints.asList(0));
                put("TESTIDD", Ints.asList(0));
            }
        }, noVerification, "VO", true, true);
    }

    private VoteCountStatus getApprovedVoteCountStatus() {
        return countingCodeValueRepository.findVoteCountStatusById(2);
    }

    private ContestReport getContestReport(final String electionPath, final int electionLevel, final String contestPath, final String areaPath,
                                           final int areaLevel) {
        ReportingUnit reportingUnit = getReportingUnit(electionPath, electionLevel, areaPath, areaLevel);

        Contest contest = getContest(contestPath);
        ContestReport contestReport = contestReportRepository.findByReportingUnitContest(reportingUnit.getPk(), contest.getPk());
        assertNotNull(contestReport, String.format(
                "Should find contest report for reporting unit %d with election path %s, area path %s and contest with contest path %s and pk %d",
                reportingUnit.getPk(), electionPath, areaPath, contestPath, contest.getPk()));
        return contestReport;
    }

    private Contest getContest(final String contestPath) {
        List<MvElection> mvElections = mvElectionRepository.findByPathAndLevel(contestPath, ElectionLevelEnum.CONTEST.getLevel());
        assertFalse(mvElections.isEmpty());
        MvElection mvElection = mvElections.get(0);
        return mvElection.getContest();
    }

    private ReportingUnit getReportingUnit(final String electionPath, final int electionLevel, final String areaPath, final int areaLevel) {
        List<MvElection> mvElections = mvElectionRepository.findByPathAndLevel(electionPath, electionLevel);
        assertFalse(mvElections.isEmpty());
        MvElection mvElection = mvElections.get(0);

        List<MvArea> mvAreas = mvAreaRepository.findByPathAndLevel(areaPath, areaLevel);
        assertFalse(mvAreas.isEmpty());
        MvArea mvArea = mvAreas.get(0);

        ReportingUnit reportingUnit = reportingUnitRepository.findByMvElectionMvArea(mvElection.getPk(), mvArea.getPk());
        assertNotNull(reportingUnit,
                String.format("Should find reporting unit with election path %s and area path %s", mvElection.getElectionPath(), mvArea.getAreaPath()));
        return reportingUnit;
    }

    private List<VoteCount> getVoteCounts(final String countQualifierId, final ContestReport contestReport, final String pollingDistrictPath,
                                          final String voteCountCategoryId) {

        return contestReport.findVoteCountsByAreaPathQualifierAndCategory(AreaPath.from(pollingDistrictPath),
                no.valg.eva.admin.common.counting.model.CountQualifier.fromId(countQualifierId), CountCategory.fromId(voteCountCategoryId));
    }

    private VoteCountCategory getVoteCountCategory(String voteCountCategory) {
        return voteCountCategoryRepository.findById(voteCountCategory);
    }

    private MvArea getPollingDistrictMvArea(final String pollingDistrictPath) {
        return mvAreaRepository.findByPathAndLevel(pollingDistrictPath, AreaLevelEnum.POLLING_DISTRICT.getLevel()).get(0);
    }

    private CountQualifier getCountQualifierById(final String countQualifierId) {
        return countingCodeValueRepository.findCountQualifierById(countQualifierId);
    }

    private VoteCount assertCorrectVoteCountAndBallots(final String countQualifierId, final ContestReport contestReport, final String pollingDistrictPath,
                                                       final int expectedApprovedBallots, final int expectedRejectedBallots, final String voteCountCategoryId, boolean stemmestyret) {
        List<VoteCount> voteCounts = getVoteCounts(countQualifierId, contestReport, pollingDistrictPath, voteCountCategoryId);
        assertEquals(voteCounts.size(), 1);
        VoteCount voteCount = voteCounts.get(0);
        assertEquals(voteCount.getApprovedBallots().intValue(), expectedApprovedBallots);
        assertEquals(voteCount.getRejectedBallots().intValue(), expectedRejectedBallots);

        // sjekk at det også har blitt opprettet en ekstra urnetelling når det er stemmestyret som rapporterer
        if (stemmestyret) {
            List<VoteCount> urnetellinger = getVoteCounts(no.valg.eva.admin.common.counting.model.CountQualifier.PROTOCOL.getId(), contestReport,
                    pollingDistrictPath, voteCountCategoryId);
            assertThat(urnetellinger).hasSize(1);
        }
        return voteCount;
    }

    private void importCountEML(final String directory) throws IOException, CryptoException, URISyntaxException {
        File zipFile = makeImportZipFileFromDirectory(directory);
        countingImportService.importCountEmlZip(getElectionEventAdmin(countingImportTestFixture.getElectionEvent()), zipFile);
    }

    private void shouldBePersonalVoteFor(final CandidateVote candidateVote, final String id) {
        assertPersonalVote(candidateVote);
        shouldBeVoteFor(candidateVote, id);
    }

    private void shouldBeWriteInFor(final CandidateVote candidateVote, final String id) {
        assertWriteIn(candidateVote);
        shouldBeVoteFor(candidateVote, id);
    }

    private void assertWriteIn(final CandidateVote candidateVote) {
        assertTrue(isWriteIn(candidateVote));
    }

    private void shouldBeVoteFor(final CandidateVote candidateVote, final String id) {
        Candidate candidate = candidateRepository.findCandidateByPk(candidateVote.getCandidate().getPk());
        assertEquals(candidate.getId(), id);
    }

    private void assertPersonalVote(final CandidateVote candidateVote) {
        assertTrue(isPersonalVote(candidateVote));
    }

    private boolean isPersonalVote(final CandidateVote candidateVote) {
        return candidateVote.getVoteCategory().getPk().equals(countingImportService.findVoteCategoryById("personal").getPk());
    }

    private boolean isWriteIn(final CandidateVote candidateVote) {
        return candidateVote.getVoteCategory().getPk().equals(countingImportService.findVoteCategoryById("writein").getPk());
    }

    private void verifyBallotCounts(String contestPath, String pollingDistrictPath, String countQualifierId,
                                    int expectedApprovedBallots, int expectedRejectedBallots, Map<String, List<Integer>> votesPerParty,
                                    VerificationFunction verificationFunction, String voteCountCategoryId, boolean stemmestyret) {
        verifyBallotCounts(contestPath, pollingDistrictPath, countQualifierId, expectedApprovedBallots, expectedRejectedBallots, votesPerParty, verificationFunction,
                voteCountCategoryId, stemmestyret, false);
    }

    private void verifyBallotCounts(
            String contestPath, String pollingDistrictPath, String countQualifierId,
            int expectedApprovedBallots, int expectedRejectedBallots, Map<String, List<Integer>> votesPerParty,
            VerificationFunction verificationFunction, String voteCountCategoryId, boolean stemmestyret, boolean veryfyBallotCountNotNull) {

        ContestReport contestReport = stemmestyret ? findForStemmestyret(new ElectionPath(contestPath), new AreaPath(pollingDistrictPath))
                : getContestReport(extractElectionGroupPath(contestPath), ElectionLevelEnum.ELECTION_GROUP.getLevel(), contestPath,
                extractMunicipalityPath(pollingDistrictPath), AreaLevelEnum.MUNICIPALITY.getLevel());

        VoteCount voteCount = assertCorrectVoteCountAndBallots(countQualifierId, contestReport, pollingDistrictPath, expectedApprovedBallots,
                expectedRejectedBallots, voteCountCategoryId, stemmestyret);

        List<BallotCount> ballotCounts = ballotCountRepository.findUnapprovedBallotCounts(voteCount, contestReport);
        assertEquals(ballotCounts.size(), 4);

        int verified = 0;
        int verifiedBallotCountNotNull = 0;
        for (BallotCount ballotCount : ballotCounts) {
            // When there are no votes for a party, ballot count will be null
            if (ballotCount != null) {
                String ballotId = ballotCount.getBallot().getId();
                List<Integer> votes = votesPerParty.get(ballotId);

                verifyTotalBallots(ballotCount, votes);

                if (votes.size() >= 2) {
                    verifyModifiedBallots(ballotCount, votes);
                }

                verificationFunction.verify(ballotCount);
                verifiedBallotCountNotNull++;
            }
            verified++;
        }

        assertEquals(veryfyBallotCountNotNull ? verifiedBallotCountNotNull : verified, votesPerParty.size());
    }

    private ContestReport findForStemmestyret(ElectionPath contestPath, AreaPath areaPath) {
        ReportingUnit reportingUnit = reportingUnitRepository.byAreaPathElectionPathAndType(areaPath, contestPath.toElectionEventPath(), STEMMESTYRET);
        MvElection mvElection = mvElectionRepository.finnEnkeltMedSti(contestPath.tilValghierarkiSti());
        return contestReportRepository.findByReportingUnitContest(reportingUnit.getPk(), mvElection.getContest().getPk());
    }

    private String extractElectionGroupPath(final String electionPath) {
        return electionPath.substring(0, 9);
    }

    private String extractMunicipalityPath(final String areaPath) {
        return areaPath.substring(0, 17);
    }

    private void verifyModifiedBallots(final BallotCount ballotCount, final List<Integer> votes) {
        assertEquals(ballotCount.getModifiedBallots(), (int) votes.get(1));
    }

    private void verifyTotalBallots(final BallotCount ballotCount, final List<Integer> votes) {
        assertEquals(ballotCount.getUnmodifiedBallots(), (int) votes.get(0));
    }

    private UserData getElectionEventAdmin(final ElectionEvent electionEvent) {
        UserData userData = null;
        try {
            userData = backend.getUserDataService().getUserData("03011700143", "valghendelse_admin", electionEvent.getId(), electionEvent.getId(),
                    InetAddress.getLocalHost());
        } catch (UnknownHostException e) {
            fail(e.getMessage());
        }
        userData.setAccessCache(backend.getAccessService().findAccessCacheFor(userData));
        return userData;
    }

    private File makeImportZipFileFromDirectory(final String directoryName) throws IOException, CryptoException, URISyntaxException {
        File temporaryDirectory = IOUtil.createTemporaryDirectory();

        // Add any XML files to a new Counts.zip file
        String countsZipFilename = temporaryDirectory.getAbsolutePath() + File.separatorChar + "Counts.zip";
        try (FileOutputStream fos = new FileOutputStream(countsZipFilename); ZipOutputStream zip = new ZipOutputStream(fos)) {
            File directory = new File(directoryName);
            addXMLFilesToZip(zip, directory);
            addImagesToZip(directoryName, zip);
        }
        signZipFile(countsZipFilename);

        return createZipFileWithCountsZipAndSignature(countsZipFilename);
    }

    private File createZipFileWithCountsZipAndSignature(final String countsZipFilename) throws IOException {
        File importZipFile = IOUtil.makeFileInTemporaryDirectory("test-1.zip");
        try (FileOutputStream fos = new FileOutputStream(importZipFile); ZipOutputStream zip = new ZipOutputStream(fos)) {
            addFile(zip, "Counts.zip", IOUtil.getBytes(countsZipFilename));
            addFile(zip, "Counts.zip.signature", IOUtil.getBytes(countsZipFilename + ".signature"));
        }
        return importZipFile;
    }

    private void signZipFile(final String countsZipFilename) throws IOException, CryptoException, URISyntaxException {
        // Sign Counts.zip and put the signature in Counts.zip.pem
        new SignIt(new String[]{fileFromResources("counting-import-test/test-user.p12").getAbsolutePath(), "IAT4CMC6KX4T95FG2AKH",
                countsZipFilename,
                countsZipFilename + ".signature"}).run();
    }

    private void addXMLFilesToZip(final ZipOutputStream zip, final File directory) throws IOException {
        File[] files = Objects.requireNonNull(directory.listFiles());
        for (File file : files) {
            if (file.getName().endsWith(".xml")) {
                addFile(zip, file.getName(), IOUtil.getBytes(file));
            }
        }
    }

    private void addImagesToZip(final String directoryName, final ZipOutputStream zip) throws IOException {
        // Add images, if any
        File imagesDir = new File(directoryName + File.separatorChar + "images");
        if (imagesDir.exists() && imagesDir.isDirectory()) {
            zip.putNextEntry(new ZipEntry("images/"));

            File[] files = Objects.requireNonNull(imagesDir.listFiles());
            for (File file : files) {
                if (file.getName().endsWith(".tiff")) {
                    addFile(zip, "images/" + file.getName(), IOUtil.getBytes(file));
                }
            }
        }
    }

    private void addFile(final ZipOutputStream zip, final String fileName, final byte[] data) throws IOException {
        zip.putNextEntry(new ZipEntry(fileName));
        zip.write(data);
    }

    private void createAndApproveProtocolCount(final String electionEventPath, final String contestPath, final String pollingDistrictPath) {
        ContestReport contestReport = legacyCountingService
                .makeContestReport(
                        getContest(contestPath),
                        getReportingUnit(electionEventPath, ElectionLevelEnum.ELECTION_EVENT.getLevel(), pollingDistrictPath,
                                AreaLevelEnum.POLLING_DISTRICT.getLevel()));
        contestReport = contestReportRepository.update(userData, contestReport);
        VoteCount voteCount = makeVoteCount(pollingDistrictPath, contestReport, "PV01", "P");
        genericTestRepository.createEntity(voteCount);
    }

    private void createAndApprovePreliminaryCount(final String contestPath, final String pollingDistrictPath) {
        ContestReport contestReport = legacyCountingService
                .makeContestReport(
                        getContest(contestPath),
                        getReportingUnit(extractElectionGroupPath(contestPath), ElectionLevelEnum.ELECTION_GROUP.getLevel(),
                                extractMunicipalityPath(pollingDistrictPath),
                                AreaLevelEnum.MUNICIPALITY.getLevel()));
        contestReport = contestReportRepository.update(userData, contestReport);
        VoteCount voteCount = makeVoteCount(pollingDistrictPath, contestReport, "FV01", "F");
        genericTestRepository.createEntity(voteCount);
    }

    private VoteCount makeVoteCount(String pollingDistrictPath, ContestReport contestReport, String id, String qualifierId) {
        VoteCount voteCount = new VoteCount();
        voteCount.setId(id);
        voteCount.setManualCount(true);
        voteCount.setVoteCountStatus(getApprovedVoteCountStatus());
        voteCount.setVoteCountCategory(getVoteCountCategory("VO"));
        voteCount.setContestReport(contestReport);
        voteCount.setCountQualifier(getCountQualifierById(qualifierId));
        MvArea pollingDistrictMvArea = getPollingDistrictMvArea(pollingDistrictPath);
        voteCount.setMvArea(pollingDistrictMvArea);
        voteCount.setPollingDistrict(pollingDistrictMvArea.getPollingDistrict());
        voteCount.setApprovedBallots(APPROVED_BALLOTS);
        voteCount.setRejectedBallots(0);
        return voteCount;
    }

    interface VerificationFunction {
        void verify(final BallotCount ballotCount);
    }

    private static class EML {
        private static final String NS = "urn:oasis:names:tc:evs:schema:eml";

        protected String electionEventId;
        protected String electionId;
        protected String electionGroupId;
        protected String contestId;
        protected String districtPath;

        protected Element element(final String name) {
            return new Element(name, NS);
        }

        protected Document createEMLDocument() {
            Document doc = new Document();
            Element root = new Element("EML", NS);
            root.setAttribute("SchemaVersion", "5.0");
            doc.setRootElement(root);
            return doc;
        }

        protected Element election() {
            return element("Election").addContent(
                    element("ElectionIdentifier").setAttribute("Id", electionId).addContent(element("ElectionGroup").setAttribute("Id", electionGroupId)));
        }

        protected Element eventIdentifier() {
            return element("EventIdentifier").setAttribute("Id", electionEventId).addContent(element("EventQualifier").setAttribute("Id", districtPath));
        }

        protected Element contest() {
            return element("Contest").addContent(element("ContestIdentifier").setAttribute("Id", contestId));
        }
    }

    /**
     * Builder class to create a Votes-*.xml file for testing.
     */
    private static class VotesXML extends EML {
        private final List<Object[]> modifiedBallots = new ArrayList<>();
        private final String[] imageFilenames = new String[]{"1.tiff", "2.tiff", "3.tiff"};
        private String reportingUnitId;

        public VotesXML reportingOn(final String reportingUnitId) {
            this.reportingUnitId = reportingUnitId;
            return this;
        }

        public String[] getImageFilenames() {
            return imageFilenames;
        }

        VotesXML forContest(final String contestPath) {
            String[] pathElements = contestPath.split("\\.");
            this.electionEventId = pathElements[0];
            this.electionGroupId = pathElements[1];
            this.electionId = pathElements[2];
            this.contestId = pathElements[3];
            return this;
        }

        VotesXML forDistrict(final String districtPath) {
            this.districtPath = districtPath;
            return this;
        }

        VotesXML withModifiedBallot(final String shortCode, final String partyId, final Correction... corrections) {
            modifiedBallots.add(new Object[]{shortCode, partyId, corrections});
            return this;
        }

        @Override
        public String toString() {
            Document doc = createEMLDocument();
            doc.getRootElement().addContent(element("TransactionId").setText("1")).addContent(element("Votes").addContent(votes()));
            return XMLUtil.documentToString(doc);
        }

        private Collection<Element> votes() {
            List<Element> modifiedBallotList = new ArrayList<>();
            int i = 0;
            for (Object[] modifiedBallotData : modifiedBallots) {
                Element modifiedBallot = element("CastVote")
                        .setAttribute("Category", "VO")
                        .addContent(eventIdentifier())
                        .addContent(
                                election().addContent(
                                        contest().addContent(
                                                element("Selection").addContent(
                                                        element("AffiliationIdentifier").setAttribute("ShortCode", (String) modifiedBallotData[0])
                                                                .setAttribute("Id", (String) modifiedBallotData[1]).addContent(element("RegisteredName"))))
                                                .addContent(corrections(modifiedBallotData))))
                        .addContent(
                                element("BallotIdentifier").setAttribute("Id", String.format("00000000%d", i++)).addContent(
                                        element("BallotName").addContent("images/1.tiff")))
                        .addContent(element("ReportingUnitIdentifier").setAttribute("Id", reportingUnitId));

                modifiedBallotList.add(modifiedBallot);
            }
            return modifiedBallotList;
        }

        private Collection<Element> corrections(final Object[] modifiedBallotData) {
            List<Element> corrections = new ArrayList<>();
            if (modifiedBallotData.length == 3) {
                for (Correction correction : (Correction[]) modifiedBallotData[2]) {
                    Element selection = element("Selection").setAttribute("ShortCode", correction.voteCategory);
                    selection.addContent(element("CandidateIdentifier").setAttribute("ShortCode", correction.shortCode).setAttribute("Id",
                            correction.candidateId));
                    if (correction.isWriteIn()) {
                        selection.addContent(element("AffiliationIdentifier").setAttribute("ShortCode", correction.partyShortCode)
                                .setAttribute("Id", correction.partyId).addContent(element("RegisteredName")));
                    }
                    corrections.add(selection);
                }
            }
            return corrections;
        }

        /**
         * A ballot correction, such as a writein, renumeration or strikeout.
         */
        static final class Correction {
            private String voteCategory;
            private String shortCode;
            private String candidateId;
            private String partyShortCode;
            private String partyId;

            private Correction() {
                // Intentionally empty
            }

            static Correction personalVote(final String shortCode, final String candidateId) {
                return new Correction().asPersonalVote().forCandidate(shortCode, candidateId);
            }

            public static Correction writeIn(final String shortCode, final String candidateId, final String partyShortCode, final String partyId) {
                return new Correction().asWriteIn().forCandidate(shortCode, candidateId).fromParty(partyShortCode, partyId);
            }

            private Correction asPersonalVote() {
                voteCategory = EvoteConstants.CANDIDATE_VOTE_CATEGORY_PERSONAL;
                return this;
            }

            private Correction asWriteIn() {
                voteCategory = EvoteConstants.CANDIDATE_VOTE_CATEGORY_WRITEIN;
                return this;
            }

            private Correction forCandidate(final String shortCode, final String candidateId) {
                this.shortCode = shortCode;
                this.candidateId = candidateId;
                return this;
            }

            private Correction fromParty(final String partyShortCode, final String partyId) {
                this.partyShortCode = partyShortCode;
                this.partyId = partyId;
                return this;
            }

            public boolean isWriteIn() {
                return voteCategory.equals(EvoteConstants.CANDIDATE_VOTE_CATEGORY_WRITEIN);
            }
        }
    }

    /**
     * Builder class to create a Count-*.xml file for testing.
     */
    private static final class CountXML extends EML {
        private final List<Object[]> partyVotes = new ArrayList<>();
        private String reportingUnitId;
        private String voteCountCategoryId;
        private boolean finalCount;

        private CountXML() {
            // Intentionally empty
        }

        public static CountXML preliminaryCount() {
            CountXML countXML = new CountXML();
            countXML.finalCount = false;
            return countXML;
        }

        public static CountXML finalCount() {
            CountXML countXML = new CountXML();
            countXML.finalCount = true;
            return countXML;
        }

        CountXML forContest(final String electionEventId, final String electionId, final String electionGroupId, final String contestId) {
            this.electionEventId = electionEventId;
            this.electionGroupId = electionGroupId;
            this.electionId = electionId;
            this.contestId = contestId;
            return this;
        }

        CountXML forDistrict(final String districtPath) {
            this.districtPath = districtPath;
            return this;
        }

        CountXML withReportingUnitId(final String reportingUnitId) {
            this.reportingUnitId = reportingUnitId;
            return this;
        }

        CountXML withVoteCountCategory(final String voteCountCategoryId) {
            this.voteCountCategoryId = voteCountCategoryId;
            return this;
        }

        CountXML withPartyVotes(final String shortCode, final String partyId, final Integer validVotes) {
            partyVotes.add(new Object[]{shortCode, partyId, validVotes});
            return this;
        }

        CountXML withPartyVotes(final String shortCode, final String partyId, final Integer validVotes, final Integer modifiedVotes) {
            partyVotes.add(new Object[]{shortCode, partyId, validVotes, modifiedVotes});
            return this;
        }

        @Override
        public String toString() {
            Document doc = createEMLDocument();
            Element root = doc.getRootElement();

            root.addContent(element("TransactionId").setText("1"));
            root.addContent(element("Count").addContent(eventIdentifier()).addContent(
                    election().addContent(
                            element("Contests").addContent(
                                    contest().addContent(element("CountQualifier").addContent(element("Final").setText(isFinalYesOrNo()))).addContent(
                                            element("ReportingUnitVotes")
                                                    .addContent(element("ReportingUnitIndicator").setAttribute("Id", reportingUnitId))
                                                    .addContent(
                                                            element("CountMetric").setAttribute("Type", "vote_count_category")
                                                                    .setAttribute("Id", voteCountCategoryId).setText("1"))
                                                    .addContent(createVoteXML()))))));

            return XMLUtil.documentToString(doc);
        }

        private Collection<? extends Content> createVoteXML() {
            List<Element> elements = new ArrayList<>();
            int totalVotes = 0;
            for (Object[] votes : partyVotes) {
                Element selection = element("Selection").addContent(
                        element("AffiliationIdentifier").setAttribute("ShortCode", (String) votes[0]).setAttribute("Id", (String) votes[1])
                                .addContent(element("RegisteredName")))
                        .addContent(element("ValidVotes").setText(votes[2].toString()));

                if (votes.length == 4) {
                    selection.addContent(element("CountMetric").setAttribute("Type", "modified").setText(votes[3].toString()));
                }

                elements.add(selection);
                totalVotes += (int) votes[2];
            }

            elements.add(element("TotalCounted").setText(String.valueOf(totalVotes)));

            return elements;
        }

        private String isFinalYesOrNo() {
            return finalCount ? "yes" : "no";
        }
    }

    private class CountData {
        private final List<CountXML> countXMLs = new ArrayList<>();
        private final List<VotesXML> votesXMLs = new ArrayList<>();

        CountData withCountXML(final CountXML countXML) {
            this.countXMLs.add(countXML);
            return this;
        }

        CountData withVotesXML(final VotesXML votesXML) {
            this.votesXMLs.add(votesXML);
            return this;
        }

        public void writeFiles(final File tempDirectory) throws IOException {
            int counter = 1;
            for (CountXML countXML : countXMLs) {
                IOUtil.makeFile(tempDirectory.getAbsolutePath(), countXML.toString().getBytes(), String.format("Count-00%d.xml", counter++));
            }

            counter = 1;
            for (VotesXML votesXML : votesXMLs) {
                IOUtil.makeFile(tempDirectory.getAbsolutePath(), votesXML.toString().getBytes(), String.format("Votes-00%d.xml", counter++));

                String imagesPath = tempDirectory.getAbsolutePath() + File.separatorChar + "images";
                new File(imagesPath).mkdir();
                for (String imageFilename : votesXML.getImageFilenames()) {
                    new File(imagesPath + File.separatorChar + imageFilename).createNewFile();
                }
            }
        }
    }

}

