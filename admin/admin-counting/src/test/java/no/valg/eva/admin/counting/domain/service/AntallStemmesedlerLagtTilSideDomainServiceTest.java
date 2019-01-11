package no.valg.eva.admin.counting.domain.service;

import no.evote.exception.EvoteException;
import no.evote.security.UserData;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.counting.model.AntallStemmesedlerLagtTilSideForValg;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.ElectionGroup;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.configuration.repository.MvElectionRepository;
import no.valg.eva.admin.counting.domain.model.AntallStemmesedlerLagtTilSide;
import no.valg.eva.admin.counting.domain.model.ContestReport;
import no.valg.eva.admin.counting.domain.model.VoteCount;
import no.valg.eva.admin.counting.repository.AntallStemmesedlerLagtTilSideRepository;
import no.valg.eva.admin.counting.repository.ContestReportRepository;
import no.valg.eva.admin.test.MockUtilsTestCase;
import org.mockito.ArgumentCaptor;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static no.evote.constants.AreaLevelEnum.BOROUGH;
import static no.evote.constants.ElectionLevelEnum.CONTEST;
import static no.evote.constants.ElectionLevelEnum.ELECTION_GROUP;
import static no.valg.eva.admin.common.counting.model.CountCategory.FO;
import static no.valg.eva.admin.common.counting.model.CountCategory.FS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AntallStemmesedlerLagtTilSideDomainServiceTest extends MockUtilsTestCase {
	private static final String ELECTION_EVENT_ID = "111111";
	private static final ElectionPath ELECTION_EVENT_PATH = ElectionPath.from(ELECTION_EVENT_ID);
	private static final AreaPath MUNICIPALITY_PATH = AreaPath.from("111111.11.11.1111");
	private static final ElectionPath NOT_ELECTION_GROUP_AND_NOT_CONTEST_PATH = ElectionPath.from("111111.11.11");
	private static final ElectionPath ELECTION_GROUP_PATH = ElectionPath.from("111111.11");
	private static final ElectionPath CONTEST_PATH_1 = ElectionPath.from("111111.11.11.111111");
	private static final ElectionPath CONTEST_PATH_2 = ElectionPath.from("111111.11.11.111112");
	private static final ElectionPath CONTEST_PATH_3 = ElectionPath.from("111111.11.11.111113");
	private static final String VALG_NAVN = "Valg";
	private static final String VALGDISTRIKT_NAVN_1 = "Valgdistrikt1";
	private static final String VALGDISTRIKT_NAVN_2 = "Valgdistrikt2";
	private static final String VALGDISTRIKT_NAVN_3 = "Valgdistrikt3";
	private static final String BYDEL = "Bydel";
	private static final String BYDELSVALG_VALGDISTRIKT_NAVN_1 = BYDEL + " " + VALGDISTRIKT_NAVN_1;
	private static final String BYDELSVALG_VALGDISTRIKT_NAVN_2 = BYDEL + " " + VALGDISTRIKT_NAVN_2;
	private static final String BYDELSVALG_VALGDISTRIKT_NAVN_3 = BYDEL + " " + VALGDISTRIKT_NAVN_3;

	private AntallStemmesedlerLagtTilSideDomainService service;
	private AntallStemmesedlerLagtTilSideRepository repository;
	private ContestReportRepository contestReportRepository;
	private MvElectionRepository mvElectionRepository;

	private UserData userData;
	private Municipality municipality;
	private Contest contest;
	private ElectionGroup electionGroup;
	private MvElection contestMvElection;

	private no.valg.eva.admin.common.counting.model.AntallStemmesedlerLagtTilSide antallStemmesedlerLagtTilSide;
	private AntallStemmesedlerLagtTilSide entity;

	@BeforeMethod
	public void setUp() throws Exception {
		service = initializeMocks(AntallStemmesedlerLagtTilSideDomainService.class);
		repository = getInjectMock(AntallStemmesedlerLagtTilSideRepository.class);
		contestReportRepository = getInjectMock(ContestReportRepository.class);
		mvElectionRepository = getInjectMock(MvElectionRepository.class);

		userData = createMock(UserData.class);
		municipality = municipality(ELECTION_EVENT_ID, MUNICIPALITY_PATH, "Kommune");
		contest = contest("Valgdistrikt", municipality);
		electionGroup = electionGroup(ELECTION_GROUP_PATH, "Valggruppe");
		contestMvElection = mvElection(contest);

		antallStemmesedlerLagtTilSide = createMock(no.valg.eva.admin.common.counting.model.AntallStemmesedlerLagtTilSide.class);
		entity = createMock(AntallStemmesedlerLagtTilSide.class);
	}

	
	@Test
    public void lagreAntallStemmesedlerLagtTilSide_givenAntallStemmesedlerLagtTilSideForContests_createsAntallStemmesedlerLagtTilSide() {
		Contest contest1 = contest();
		Contest contest2 = contest();
		Contest contest3 = contest();
		MvElection mvElection1 = mvElection(contest1);
		MvElection mvElection2 = mvElection(contest2);
		MvElection mvElection3 = mvElection(contest3);
		List<AntallStemmesedlerLagtTilSideForValg> antallStemmesedlerLagtTilSideForValgList =
				asList(new AntallStemmesedlerLagtTilSideForValg(CONTEST_PATH_1, "Valgdistrikt1", 1),
						new AntallStemmesedlerLagtTilSideForValg(CONTEST_PATH_2, "Valgdistrikt2", 2),
						new AntallStemmesedlerLagtTilSideForValg(CONTEST_PATH_3, "Valgdistrikt3", 3));
		antallStemmesedlerLagtTilSide =
				new no.valg.eva.admin.common.counting.model.AntallStemmesedlerLagtTilSide(MUNICIPALITY_PATH, antallStemmesedlerLagtTilSideForValgList, true);

		when(mvElectionRepository.finnEnkeltMedSti(CONTEST_PATH_1.tilValghierarkiSti())).thenReturn(mvElection1);
		when(mvElectionRepository.finnEnkeltMedSti(CONTEST_PATH_2.tilValghierarkiSti())).thenReturn(mvElection2);
		when(mvElectionRepository.finnEnkeltMedSti(CONTEST_PATH_3.tilValghierarkiSti())).thenReturn(mvElection3);
		when(contestReportRepository.findByContestAndMunicipality(any(Contest.class), any(Municipality.class))).thenReturn(emptyList());
		when(repository.findByMunicipalityAndContest(any(Municipality.class), any(Contest.class))).thenReturn(null);

		service.lagreAntallStemmesedlerLagtTilSide(userData, municipality, antallStemmesedlerLagtTilSide);

		ArgumentCaptor<AntallStemmesedlerLagtTilSide> argumentCaptor = ArgumentCaptor.forClass(AntallStemmesedlerLagtTilSide.class);
		verify(repository, times(3)).create(eq(userData), argumentCaptor.capture());
		List<AntallStemmesedlerLagtTilSide> values = argumentCaptor.getAllValues();
		assertThat(values.get(0)).isEqualToComparingFieldByField(new AntallStemmesedlerLagtTilSide(municipality, electionGroup, contest1, 1));
		assertThat(values.get(1)).isEqualToComparingFieldByField(new AntallStemmesedlerLagtTilSide(municipality, electionGroup, contest2, 2));
		assertThat(values.get(2)).isEqualToComparingFieldByField(new AntallStemmesedlerLagtTilSide(municipality, electionGroup, contest3, 3));
	}
	

	private Contest contest() {
		Contest contest = createMock(Contest.class);
		when(contest.getElection().getElectionGroup()).thenReturn(electionGroup);
		when(contest.isOnBoroughLevel()).thenReturn(true);
		when(contest.getFirstContestArea().getMvArea().getMunicipality()).thenReturn(municipality);
		return contest;
	}

	@Test
    public void lagreAntallStemmesedlerLagtTilSide_givenAntallStemmesedlerLagtTilSideForContest_updatesExistingAntallStemmesedlerLagtTilSide() {
		List<AntallStemmesedlerLagtTilSideForValg> antallStemmesedlerLagtTilSideForValgList =
				singletonList(new AntallStemmesedlerLagtTilSideForValg(CONTEST_PATH_1, "Valgdistrikt1", 1));
		antallStemmesedlerLagtTilSide =
				new no.valg.eva.admin.common.counting.model.AntallStemmesedlerLagtTilSide(MUNICIPALITY_PATH, antallStemmesedlerLagtTilSideForValgList, true);

		when(contest.isOnBoroughLevel()).thenReturn(true);
		when(repository.findByMunicipalityAndContest(municipality, contest)).thenReturn(entity);
		when(contestReportRepository.findByContestAndMunicipality(contest, municipality)).thenReturn(emptyList());
		when(mvElectionRepository.finnEnkeltMedSti(CONTEST_PATH_1.tilValghierarkiSti())).thenReturn(contestMvElection);

		service.lagreAntallStemmesedlerLagtTilSide(userData, municipality, antallStemmesedlerLagtTilSide);

		verify(entity).setAntallStemmesedler(1);
		verify(repository, never()).create(eq(userData), any(AntallStemmesedlerLagtTilSide.class));
	}

	@Test(expectedExceptions = EvoteException.class, dataProvider = "lagreTestDataFoOrFs",
			expectedExceptionsMessageRegExp = "antall stemmesedler lagt til side kan ikke lagres siden det eksisterer tellinger for forhåndsstemmer")
	public void lagreAntallStemmesedlerLagtTilSide_givenAntallStemmesedlerLagtTilSideForContest_throwsExceptionWhenCountForFoOrFsExists(
            String testName, CountCategory category) {
		List<AntallStemmesedlerLagtTilSideForValg> antallStemmesedlerLagtTilSideForValgList =
				singletonList(new AntallStemmesedlerLagtTilSideForValg(CONTEST_PATH_1, "Valgdistrikt1", 1));
		antallStemmesedlerLagtTilSide =
				new no.valg.eva.admin.common.counting.model.AntallStemmesedlerLagtTilSide(MUNICIPALITY_PATH, antallStemmesedlerLagtTilSideForValgList, true);
		VoteCount voteCount = voteCount(category);
		ContestReport contestReport = contestReport(voteCount);

		when(contest.isOnBoroughLevel()).thenReturn(true);
		when(userData.getOperatorAreaPath()).thenReturn(MUNICIPALITY_PATH);
		when(contestReportRepository.findByContestAndMunicipality(contest, municipality)).thenReturn(singletonList(contestReport));
		when(mvElectionRepository.finnEnkeltMedSti(CONTEST_PATH_1.tilValghierarkiSti())).thenReturn(contestMvElection);

		service.lagreAntallStemmesedlerLagtTilSide(userData, municipality, antallStemmesedlerLagtTilSide);
	}

	@Test
    public void lagreAntallStemmesedlerLagtTilSide_givenAntallStemmesedlerLagtTilSideForElectionGroup_createsAntallStemmesedlerLagtTilSide() {
		antallStemmesedlerLagtTilSide = new no.valg.eva.admin.common.counting.model.AntallStemmesedlerLagtTilSide(
				MUNICIPALITY_PATH, new AntallStemmesedlerLagtTilSideForValg(ELECTION_GROUP_PATH, "Valgruppe", 1), true);
		MvElection mvElection = createMock(MvElection.class);

		when(mvElection.getElectionGroup()).thenReturn(electionGroup);
		when(mvElectionRepository.finnEnkeltMedSti(ELECTION_GROUP_PATH.tilValghierarkiSti())).thenReturn(mvElection);
		when(contestReportRepository.findByElectionGroupAndMunicipality(any(ElectionGroup.class), any(Municipality.class))).thenReturn(emptyList());
		when(repository.findByMunicipalityAndElectionGroup(any(Municipality.class), any(ElectionGroup.class))).thenReturn(null);

		service.lagreAntallStemmesedlerLagtTilSide(userData, municipality, antallStemmesedlerLagtTilSide);

		ArgumentCaptor<AntallStemmesedlerLagtTilSide> argumentCaptor = ArgumentCaptor.forClass(AntallStemmesedlerLagtTilSide.class);
		verify(repository).create(eq(userData), argumentCaptor.capture());
		assertThat(argumentCaptor.getValue()).isEqualToComparingFieldByField(new AntallStemmesedlerLagtTilSide(municipality, electionGroup, null, 1));
	}

	@Test
    public void lagreAntallStemmesedlerLagtTilSide_givenAntallStemmesedlerLagtTilSideForElectionGroup_updatesExistingAntallStemmesedlerLagtTilSide() {
		antallStemmesedlerLagtTilSide = new no.valg.eva.admin.common.counting.model.AntallStemmesedlerLagtTilSide(
				MUNICIPALITY_PATH, new AntallStemmesedlerLagtTilSideForValg(ELECTION_GROUP_PATH, "Valgruppe", 1), true);
		MvElection mvElection = createMock(MvElection.class);

		when(mvElection.getElectionGroup()).thenReturn(electionGroup);
		when(mvElectionRepository.finnEnkeltMedSti(ELECTION_GROUP_PATH.tilValghierarkiSti())).thenReturn(mvElection);
		when(contestReportRepository.findByElectionGroupAndMunicipality(electionGroup, municipality)).thenReturn(emptyList());
		when(repository.findByMunicipalityAndElectionGroup(municipality, electionGroup)).thenReturn(entity);

		service.lagreAntallStemmesedlerLagtTilSide(userData, municipality, antallStemmesedlerLagtTilSide);

		verify(entity).setAntallStemmesedler(1);
		verify(repository, never()).create(eq(userData), any(AntallStemmesedlerLagtTilSide.class));
	}

	@Test(expectedExceptions = EvoteException.class, dataProvider = "lagreTestDataFoOrFs",
			expectedExceptionsMessageRegExp = "antall stemmesedler lagt til side kan ikke lagres siden det eksisterer tellinger for forhåndsstemmer")
	public void lagreAntallStemmesedlerLagtTilSide_givenAntallStemmesedlerLagtTilSideForElectionGroup_throwsExceptionWhenCountForFoOrFsExists(
            String testName, CountCategory category) {
		antallStemmesedlerLagtTilSide = new no.valg.eva.admin.common.counting.model.AntallStemmesedlerLagtTilSide(
				MUNICIPALITY_PATH, new AntallStemmesedlerLagtTilSideForValg(ELECTION_GROUP_PATH, "Valgruppe", 1), true);
		VoteCount voteCount = voteCount(category);
		ContestReport contestReport = contestReport(voteCount);
		MvElection mvElection = createMock(MvElection.class);

		when(mvElection.getElectionGroup()).thenReturn(electionGroup);
		when(mvElectionRepository.finnEnkeltMedSti(ELECTION_GROUP_PATH.tilValghierarkiSti())).thenReturn(mvElection);
		when(userData.getOperatorAreaPath()).thenReturn(MUNICIPALITY_PATH);
		when(contestReportRepository.findByElectionGroupAndMunicipality(electionGroup, municipality)).thenReturn(singletonList(contestReport));

		service.lagreAntallStemmesedlerLagtTilSide(userData, municipality, antallStemmesedlerLagtTilSide);
	}

	@Test(expectedExceptions = IllegalArgumentException.class,
			expectedExceptionsMessageRegExp = "forventet at valgdistriktet <Valgdistrikt> skal tilhøre kommunen <Kommune>")
    public void lagreAntallStemmesedlerLagtTilSide_givenMunicipalityAndContestNotMatching_throwsException() {
		List<AntallStemmesedlerLagtTilSideForValg> antallStemmesedlerLagtTilSideForValgList =
				singletonList(new AntallStemmesedlerLagtTilSideForValg(CONTEST_PATH_1, "Valgdistrikt", 1));
		antallStemmesedlerLagtTilSide =
				new no.valg.eva.admin.common.counting.model.AntallStemmesedlerLagtTilSide(MUNICIPALITY_PATH, antallStemmesedlerLagtTilSideForValgList, true);
		Municipality otherMunicipality = createMock(Municipality.class);
		MvElection contestMvElection = mvElection(contest("Valgdistrikt", otherMunicipality));

		when(mvElectionRepository.finnEnkeltMedSti(CONTEST_PATH_1.tilValghierarkiSti())).thenReturn(contestMvElection);

		service.lagreAntallStemmesedlerLagtTilSide(userData, municipality, antallStemmesedlerLagtTilSide);
	}

	@Test(expectedExceptions = IllegalArgumentException.class,
			expectedExceptionsMessageRegExp = "forventet at valgdistriktet <Valgdistrikt> skal være for et bydelsvalg")
    public void lagreAntallStemmesedlerLagtTilSide_givenMunicipalityAndNotBoroughContest_throwsException() {
		List<AntallStemmesedlerLagtTilSideForValg> antallStemmesedlerLagtTilSideForValgList =
				singletonList(new AntallStemmesedlerLagtTilSideForValg(CONTEST_PATH_1, "Valgdistrikt", 1));
		antallStemmesedlerLagtTilSide =
				new no.valg.eva.admin.common.counting.model.AntallStemmesedlerLagtTilSide(MUNICIPALITY_PATH, antallStemmesedlerLagtTilSideForValgList, true);

		when(contest.isOnBoroughLevel()).thenReturn(false);
		when(mvElectionRepository.finnEnkeltMedSti(CONTEST_PATH_1.tilValghierarkiSti())).thenReturn(contestMvElection);

		service.lagreAntallStemmesedlerLagtTilSide(userData, municipality, antallStemmesedlerLagtTilSide);
	}

	@Test
    public void lagreAntallStemmesedlerLagtTilSide_givenNormalUser_ensureCallToSjekkAtBrukerTilhørerKommune() {
		when(municipality.areaPath()).thenReturn(MUNICIPALITY_PATH);
		when(contestReportRepository.findByElectionGroupAndMunicipality(any(ElectionGroup.class), any(Municipality.class))).thenReturn(emptyList());

		service.lagreAntallStemmesedlerLagtTilSide(userData, municipality, antallStemmesedlerLagtTilSide);

		verify(userData).sjekkAtBrukerTilhørerKommune(MUNICIPALITY_PATH);
	}

	@Test
    public void lagreAntallStemmesedlerLagtTilSide_givenElectionEventAdmin_skipsCallToSjekkAtBrukerTilhørerKommune() {
		when(municipality.areaPath()).thenReturn(MUNICIPALITY_PATH);
		when(contest.isOnBoroughLevel()).thenReturn(true);
		when(contestReportRepository.findByContestAndMunicipality(any(Contest.class), any(Municipality.class))).thenReturn(emptyList());
		when(userData.isElectionEventAdminUser()).thenReturn(true);

		service.lagreAntallStemmesedlerLagtTilSide(userData, municipality, antallStemmesedlerLagtTilSide);

		verify(userData, never()).sjekkAtBrukerTilhørerKommune(MUNICIPALITY_PATH);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
    public void lagreAntallStemmesedlerLagtTilSide_givenNotElectionGroupAndNotContestPath_throwsException() {
		antallStemmesedlerLagtTilSide =
				new no.valg.eva.admin.common.counting.model.AntallStemmesedlerLagtTilSide(MUNICIPALITY_PATH,
						new AntallStemmesedlerLagtTilSideForValg(NOT_ELECTION_GROUP_AND_NOT_CONTEST_PATH, "", 1), true);

		service.lagreAntallStemmesedlerLagtTilSide(userData, municipality, antallStemmesedlerLagtTilSide);
	}

	@DataProvider
	public Object[][] lagreTestDataFoOrFs() {
		return new Object[][] {
				new Object[] { "FO vote count", FO },
				new Object[] { "FS vote count", FS }
		};
	}

	@Test
    public void isAntallStemmesedlerLagtTilSideLagret_givenHaveNotAntallStemmesedlerLagtTilSide_returnsFalse() {
		MvElection mvElection = mvElection(contest);

		when(repository.isAntallStemmerLagtTilSide(municipality, mvElection.getElectionGroup())).thenReturn(false);
		boolean antallStemmesedlerLagtTilSideLagret = service.isAntallStemmesedlerLagtTilSideLagret(mvElection, municipality);
		
		assertThat(antallStemmesedlerLagtTilSideLagret).isEqualTo(false);
	}

	@Test
    public void isAntallStemmesedlerLagtTilSideLagret_givenHavingAntallStemmesedlerLagtTilSide_returnsTrue() {
		MvElection mvElection = mvElection(contest);
		
		when(repository.isAntallStemmerLagtTilSide(municipality, mvElection.getElectionGroup())).thenReturn(true);
		boolean antallStemmesedlerLagtTilSideLagret = service.isAntallStemmesedlerLagtTilSideLagret(mvElection, municipality);
		
		assertThat(antallStemmesedlerLagtTilSideLagret).isEqualTo(true);
	}

	@Test
    public void hentAntallStemmesedlerLagtTilSide_givenHavingAntallStemmesedlerLagtTilSide_returnsAntallStemmesedlerLagtTilSide() {
		MvElection mvElection = mvElection(electionGroup);
		List<MvElection> mvElections = singletonList(mvElection);

		when(mvElectionRepository.findByPathAndLevelAndAreaLevel(ELECTION_EVENT_PATH, CONTEST, BOROUGH)).thenReturn(emptyList());
		when(mvElectionRepository.findByPathAndLevel(ELECTION_EVENT_PATH, ELECTION_GROUP)).thenReturn(mvElections);
		when(contestReportRepository.findByElectionGroupAndMunicipality(electionGroup, municipality)).thenReturn(emptyList());
		when(repository.findByMunicipalityAndElectionGroup(municipality, electionGroup))
				.thenReturn(antallStemmesedlerLagtTilSideEntity(municipality, electionGroup, null, 1));
		
		no.valg.eva.admin.common.counting.model.AntallStemmesedlerLagtTilSide antallStemmesedlerLagtTilSide = service.hentAntallStemmesedlerLagtTilSide(municipality);

		assertThat(antallStemmesedlerLagtTilSide).isEqualTo(antallStemmesedlerLagtTilSide(1, true));
	}

	@Test
    public void hentAntallStemmesedlerLagtTilSide_givenNotHavingAntallStemmesedlerLagtTilSide_returnsAntallStemmesedlerLagtTilSide() {
		MvElection mvElection = mvElection(electionGroup);
		List<MvElection> mvElections = singletonList(mvElection);

		when(mvElectionRepository.findByPathAndLevelAndAreaLevel(ELECTION_EVENT_PATH, CONTEST, BOROUGH)).thenReturn(emptyList());
		when(mvElectionRepository.findByPathAndLevel(ELECTION_EVENT_PATH, ELECTION_GROUP)).thenReturn(mvElections);
		when(contestReportRepository.findByElectionGroupAndMunicipality(electionGroup, municipality)).thenReturn(emptyList());
		when(repository.findByMunicipalityAndElectionGroup(municipality, electionGroup)).thenReturn(null);

		no.valg.eva.admin.common.counting.model.AntallStemmesedlerLagtTilSide antallStemmesedlerLagtTilSide = service.hentAntallStemmesedlerLagtTilSide(municipality);

		assertThat(antallStemmesedlerLagtTilSide).isEqualTo(antallStemmesedlerLagtTilSide(0, true));
	}

	@Test
    public void hentAntallStemmesedlerLagtTilSide_givenLagringAvAntallStemmesedlerLagtTilSideIkkeMulig_returnsAntallStemmesedlerLagtTilSide() {
		MvElection mvElection = mvElection(electionGroup);
		List<MvElection> mvElections = singletonList(mvElection);
		VoteCount voteCount = voteCount(FO);
		ContestReport contestReport = contestReport(voteCount);
		List<ContestReport> contestReports = singletonList(contestReport);

		when(mvElectionRepository.findByPathAndLevelAndAreaLevel(ELECTION_EVENT_PATH, CONTEST, BOROUGH)).thenReturn(emptyList());
		when(mvElectionRepository.findByPathAndLevel(ELECTION_EVENT_PATH, ELECTION_GROUP)).thenReturn(mvElections);
		when(contestReportRepository.findByElectionGroupAndMunicipality(electionGroup, municipality)).thenReturn(contestReports);
		when(repository.findByMunicipalityAndElectionGroup(municipality, electionGroup))
				.thenReturn(antallStemmesedlerLagtTilSideEntity(municipality, electionGroup, null, 1));

		no.valg.eva.admin.common.counting.model.AntallStemmesedlerLagtTilSide antallStemmesedlerLagtTilSide = service.hentAntallStemmesedlerLagtTilSide(municipality);

		assertThat(antallStemmesedlerLagtTilSide).isEqualTo(antallStemmesedlerLagtTilSide(1, false));
	}

	
	@Test
    public void hentAntallStemmesedlerLagtTilSide_givenBydelsvalgAndHavingAntallStemmesedlerLagtTilSide_returnsAntallStemmesedlerLagtTilSide() {
		Contest contest1 = contest(CONTEST_PATH_1, VALG_NAVN, VALGDISTRIKT_NAVN_1, municipality);
		Contest contest2 = contest(CONTEST_PATH_2, VALG_NAVN, VALGDISTRIKT_NAVN_2, municipality);
		Contest contest3 = contest(CONTEST_PATH_3, VALG_NAVN, VALGDISTRIKT_NAVN_3, municipality);
		MvElection contestMvElection1 = mvElection(contest1);
		MvElection contestMvElection2 = mvElection(contest2);
		MvElection contestMvElection3 = mvElection(contest3);
		List<MvElection> contestMvElections = asList(contestMvElection1, contestMvElection2, contestMvElection3);
		MvElection electionGroupMvElection = mvElection(electionGroup);
		List<MvElection> electionGroupMvElections = singletonList(electionGroupMvElection);

		when(mvElectionRepository.findByPathAndLevelAndAreaLevel(ELECTION_EVENT_PATH, CONTEST, BOROUGH)).thenReturn(contestMvElections);
		when(mvElectionRepository.findByPathAndLevel(ELECTION_EVENT_PATH, ELECTION_GROUP)).thenReturn(electionGroupMvElections);
		when(contestReportRepository.findByElectionGroupAndMunicipality(electionGroup, municipality)).thenReturn(emptyList());
		when(repository.findByMunicipalityAndContest(municipality, contest1))
				.thenReturn(antallStemmesedlerLagtTilSideEntity(municipality, electionGroup, contest1, 1));
		when(repository.findByMunicipalityAndContest(municipality, contest2))
				.thenReturn(antallStemmesedlerLagtTilSideEntity(municipality, electionGroup, contest2, 2));
		when(repository.findByMunicipalityAndContest(municipality, contest3))
				.thenReturn(antallStemmesedlerLagtTilSideEntity(municipality, electionGroup, contest3, 3));

		no.valg.eva.admin.common.counting.model.AntallStemmesedlerLagtTilSide antallStemmesedlerLagtTilSide = service
				.hentAntallStemmesedlerLagtTilSide(municipality);

		List<AntallStemmesedlerLagtTilSideForValg> antallStemmesedlerLagtTilSideForValgList = asList(
				antallStemmesedlerLagtTilForValg(CONTEST_PATH_1, BYDELSVALG_VALGDISTRIKT_NAVN_1, 1),
				antallStemmesedlerLagtTilForValg(CONTEST_PATH_2, BYDELSVALG_VALGDISTRIKT_NAVN_2, 2),
				antallStemmesedlerLagtTilForValg(CONTEST_PATH_3, BYDELSVALG_VALGDISTRIKT_NAVN_3, 3));
		assertThat(antallStemmesedlerLagtTilSide).isEqualTo(antallStemmesedlerLagtTilSide(antallStemmesedlerLagtTilSideForValgList, true));
	}
	

	@Test
    public void hentAntallStemmesedlerLagtTilSide_givenBydelsvalgAndNotHavingAntallStemmesedlerLagtTilSide_returnsAntallStemmesedlerLagtTilSide() {
		Contest contest1 = contest(CONTEST_PATH_1, VALG_NAVN, VALGDISTRIKT_NAVN_1, municipality);
		Contest contest2 = contest(CONTEST_PATH_2, VALG_NAVN, VALGDISTRIKT_NAVN_2, municipality);
		Contest contest3 = contest(CONTEST_PATH_3, VALG_NAVN, VALGDISTRIKT_NAVN_3, municipality);
		MvElection contestMvElection1 = mvElection(contest1);
		MvElection contestMvElection2 = mvElection(contest2);
		MvElection contestMvElection3 = mvElection(contest3);
		List<MvElection> contestMvElections = asList(contestMvElection1, contestMvElection2, contestMvElection3);
		MvElection electionGroupMvElection = mvElection(electionGroup);
		List<MvElection> electionGroupMvElections = singletonList(electionGroupMvElection);

		when(mvElectionRepository.findByPathAndLevelAndAreaLevel(ELECTION_EVENT_PATH, CONTEST, BOROUGH)).thenReturn(contestMvElections);
		when(mvElectionRepository.findByPathAndLevel(ELECTION_EVENT_PATH, ELECTION_GROUP)).thenReturn(electionGroupMvElections);
		when(contestReportRepository.findByElectionGroupAndMunicipality(electionGroup, municipality)).thenReturn(emptyList());
		when(repository.findByMunicipalityAndContest(any(Municipality.class), any(Contest.class))).thenReturn(null);

		no.valg.eva.admin.common.counting.model.AntallStemmesedlerLagtTilSide antallStemmesedlerLagtTilSide = service.hentAntallStemmesedlerLagtTilSide(municipality);

		List<AntallStemmesedlerLagtTilSideForValg> antallStemmesedlerLagtTilSideForValgList = asList(
				antallStemmesedlerLagtTilForValg(CONTEST_PATH_1, BYDELSVALG_VALGDISTRIKT_NAVN_1, 0),
				antallStemmesedlerLagtTilForValg(CONTEST_PATH_2, BYDELSVALG_VALGDISTRIKT_NAVN_2, 0),
				antallStemmesedlerLagtTilForValg(CONTEST_PATH_3, BYDELSVALG_VALGDISTRIKT_NAVN_3, 0));
		assertThat(antallStemmesedlerLagtTilSide).isEqualTo(antallStemmesedlerLagtTilSide(antallStemmesedlerLagtTilSideForValgList, true));
	}

	private no.valg.eva.admin.common.counting.model.AntallStemmesedlerLagtTilSide antallStemmesedlerLagtTilSide(
			int antallStemmesedler, boolean lagringAvAntallStemmesedlerLagtTilSideMulig) {
		return new no.valg.eva.admin.common.counting.model.AntallStemmesedlerLagtTilSide(MUNICIPALITY_PATH,
				antallStemmesedlerLagtTilForValg(antallStemmesedler),
				lagringAvAntallStemmesedlerLagtTilSideMulig);
	}

	private no.valg.eva.admin.common.counting.model.AntallStemmesedlerLagtTilSide antallStemmesedlerLagtTilSide(
			List<AntallStemmesedlerLagtTilSideForValg> antallStemmesedlerLagtTilSideForValgList, boolean lagringAvAntallStemmesedlerLagtTilSideMulig) {
		return new no.valg.eva.admin.common.counting.model.AntallStemmesedlerLagtTilSide(
				MUNICIPALITY_PATH, antallStemmesedlerLagtTilSideForValgList, lagringAvAntallStemmesedlerLagtTilSideMulig);
	}

	private AntallStemmesedlerLagtTilSideForValg antallStemmesedlerLagtTilForValg(int antallStemmesedler) {
		return new AntallStemmesedlerLagtTilSideForValg(ELECTION_GROUP_PATH, "Valggruppe", antallStemmesedler);
	}

	private AntallStemmesedlerLagtTilSideForValg antallStemmesedlerLagtTilForValg(ElectionPath contestPath, String navn, int antallStemmesedler) {
		return new AntallStemmesedlerLagtTilSideForValg(contestPath, navn, antallStemmesedler);
	}

	private AntallStemmesedlerLagtTilSide antallStemmesedlerLagtTilSideEntity(
			Municipality municipality, ElectionGroup electionGroup, Contest contest, int antallStemmesedler) {
		return new AntallStemmesedlerLagtTilSide(municipality, electionGroup, contest, antallStemmesedler);
	}

	private Municipality municipality(String electionEventId, AreaPath areaPath, String name) {
		Municipality municipality = createMock(Municipality.class);
		when(municipality.electionEventId()).thenReturn(electionEventId);
		when(municipality.getName()).thenReturn(name);
		when(municipality.areaPath()).thenReturn(areaPath);
		return municipality;
	}

	private MvElection mvElection(ElectionGroup electionGroup) {
		MvElection mvElection = createMock(MvElection.class);
		when(mvElection.getElectionGroup()).thenReturn(electionGroup);
		return mvElection;
	}

	private ElectionGroup electionGroup(ElectionPath electionGroupPath, String name) {
		ElectionGroup electionGroup = createMock(ElectionGroup.class);
		when(electionGroup.electionPath()).thenReturn(electionGroupPath);
		when(electionGroup.getName()).thenReturn(name);
		return electionGroup;
	}

	private MvElection mvElection(Contest contest) {
		MvElection mvElection = createMock(MvElection.class);
		when(mvElection.getContest()).thenReturn(contest);
		return mvElection;
	}

	private Contest contest(String name, Municipality municipality) {
		Contest contest = createMock(Contest.class);
		when(contest.getName()).thenReturn(name);
		when(contest.getFirstContestArea().getMvArea().getMunicipality()).thenReturn(municipality);
		return contest;
	}

	private Contest contest(ElectionPath contestPath, String electionName, String contestName, Municipality municipality) {
		Contest contest = createMock(Contest.class);
		when(contest.electionPath()).thenReturn(contestPath);
		when(contest.getElectionName()).thenReturn(electionName);
		when(contest.getName()).thenReturn(contestName);
		when(contest.isInMunicipality(municipality)).thenReturn(true);
		return contest;
	}

	private ContestReport contestReport(VoteCount voteCount) {
		ContestReport contestReport = createMock(ContestReport.class);
		Set<VoteCount> voteCounts = new HashSet<>();
		voteCounts.add(voteCount);
		when(contestReport.getVoteCountSet()).thenReturn(voteCounts);
		return contestReport;
	}

	private VoteCount voteCount(CountCategory category) {
		VoteCount voteCount = createMock(VoteCount.class);
		when(voteCount.getCountCategory()).thenReturn(category);
		return voteCount;
	}
}
