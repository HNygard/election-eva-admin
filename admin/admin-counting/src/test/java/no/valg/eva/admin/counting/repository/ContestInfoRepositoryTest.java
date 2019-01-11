package no.valg.eva.admin.counting.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import no.evote.constants.AreaLevelEnum;
import no.valg.eva.admin.common.counting.model.ContestInfo;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.test.AbstractJpaTestBase;
import no.valg.eva.admin.test.GenericTestRepository;
import no.valg.eva.admin.test.ObjectAssert;
import no.valg.eva.admin.test.TestGroups;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;


@Test(groups = TestGroups.REPOSITORY)
public class ContestInfoRepositoryTest extends AbstractJpaTestBase {

	private static final String ELECTION_BYDEL_GRUNERLOKKA = "200701.01.03.030102";
	private static final String AREA_GRUNNERLOKKA_SKOLE = "200701.47.03.0301.030102.0204";
	private static final String AREA_GRUNERLOKKA = "200701.47.03.0301.030102";
	private static final String AREA_OSLO_MUNICIPALITY = "200701.47.03.0301";
	private static final String AREA_OSLO_COUNTY = "200701.47.03";
	private static final String AREA_NORWAY = "200701.47";

	private ContestInfoRepository contestInfoRepository;
	private GenericTestRepository genericTestRepository;

	@BeforeMethod(alwaysRun = true)
	public void setUp() throws Exception {
		contestInfoRepository = new ContestInfoRepository(getEntityManager());
		genericTestRepository = new GenericTestRepository(getEntityManager());
	}

	@Test
	public void contestForElectionAndArea_withValidElectionAndArea_returnsValidContestInfo() {
		MvElection mvElection = genericTestRepository.findEntityByProperty(MvElection.class, "electionPath", ELECTION_BYDEL_GRUNERLOKKA);
		MvArea mvArea = genericTestRepository.findEntityByProperty(MvArea.class, "areaPath", AREA_GRUNNERLOKKA_SKOLE);
		ContestInfo info = contestInfoRepository.contestForElectionAndArea(mvElection.getElection(), mvArea);
		ObjectAssert.assertThat(info.getAreaLevel()).isEqualTo(AreaLevelEnum.BOROUGH);
		assertThat(info.getContestName()).isEqualTo("Grünerløkka");
		assertThat(info.getElectionName()).isEqualTo("Bydelsvalg");
		assertThat(info.getElectionPath().path()).isEqualTo(ELECTION_BYDEL_GRUNERLOKKA);
	}

	@DataProvider(name = "electionAndArea")
	public static Object[][] electionAndArea() {
		return new Object[][] {
				{ ELECTION_BYDEL_GRUNERLOKKA, AREA_GRUNNERLOKKA_SKOLE, true },
				{ ELECTION_BYDEL_GRUNERLOKKA, AREA_NORWAY, false }
		};
	}

	@Test(dataProvider = "electionAndArea")
	public void hasContestsForElectionAndArea_givenElectionAndArea_returnsExpected(String election, String area, boolean expected) {
		MvElection mvElection = genericTestRepository.findEntityByProperty(MvElection.class, "electionPath", election);
		MvArea mvArea = genericTestRepository.findEntityByProperty(MvArea.class, "areaPath", area);
		boolean result = contestInfoRepository.hasContestsForElectionAndArea(mvElection.getElection().getPk(), mvArea.getPk());
		assertThat(result).isEqualTo(expected);
	}

	@DataProvider(name = "areas")
	public static Object[][] areas() {
		return new Object[][] {
				{ AREA_GRUNNERLOKKA_SKOLE, 3 },
				{ AREA_GRUNERLOKKA, 3 },
				{ AREA_OSLO_MUNICIPALITY, 19 },
				{ AREA_OSLO_COUNTY, 1 },
				{ AREA_NORWAY, 1 }
		};
	}

	@Test(dataProvider = "areas")
	public void contestsForArea_givenArea_returnsExpectedSize(String area, int expectedSize) {
		MvArea mvArea = genericTestRepository.findEntityByProperty(MvArea.class, "areaPath", area);
		List<ContestInfo> list = contestInfoRepository.contestsForArea(mvArea.getPk());
		assertThat(list.size()).isEqualTo(expectedSize);
	}

    @Test
    public void contestForSamiElection_givenContest_returnsContestInfoForContest() {
        // contest.txt
        String electionPath = "200901.02.02.000001";
        MvElection samiMvElectionContest = genericTestRepository.findEntityByProperty(MvElection.class, "electionPath", electionPath);

        ContestInfo contestInfo = contestInfoRepository.contestForSamiElection(samiMvElectionContest.getContest());
        
        assertThat(contestInfo.getElectionPath().path()).isEqualTo(electionPath);
    }
    
}

