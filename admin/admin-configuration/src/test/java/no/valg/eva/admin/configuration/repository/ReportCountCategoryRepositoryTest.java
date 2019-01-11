package no.valg.eva.admin.configuration.repository;

import static no.valg.eva.admin.common.counting.model.CountCategory.VF;
import static no.valg.eva.admin.common.counting.model.CountCategory.VO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.Assert.assertEquals;

import java.math.BigDecimal;
import java.util.List;

import no.evote.constants.AreaLevelEnum;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.counting.constants.CountingMode;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.ContestStatus;
import no.valg.eva.admin.configuration.domain.model.Country;
import no.valg.eva.admin.configuration.domain.model.County;
import no.valg.eva.admin.configuration.domain.model.CountyStatus;
import no.valg.eva.admin.configuration.domain.model.Election;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.ElectionEventStatus;
import no.valg.eva.admin.configuration.domain.model.ElectionGroup;
import no.valg.eva.admin.configuration.domain.model.ElectionType;
import no.valg.eva.admin.configuration.domain.model.Locale;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.MunicipalityStatus;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.ReportCountCategory;
import no.valg.eva.admin.configuration.domain.model.VoteCountCategory;
import no.valg.eva.admin.test.AbstractJpaTestBase;
import no.valg.eva.admin.test.GenericTestRepository;
import no.valg.eva.admin.test.TestGroups;

import org.joda.time.LocalDate;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;



@Test(groups = TestGroups.REPOSITORY)
public class ReportCountCategoryRepositoryTest extends AbstractJpaTestBase {

	private static final long MUNICIPALITY_PK_UNKNOWN = 9999;
	private static final long ELECTION_GROUP_PK_1 = 1;

	private ReportCountCategoryRepository reportCountCategoryRepository;
	private GenericTestRepository genericRepository;

	@BeforeMethod(alwaysRun = true)
	public void setUp() throws Exception {
		reportCountCategoryRepository = new ReportCountCategoryRepository(getEntityManager());
		genericRepository = new GenericTestRepository(getEntityManager());
	}

	@Test
	public void zeroTest() {
		assertThat(new ReportCountCategoryRepository()).isNotNull();
	}

	@Test
	public void findByMunicipalityElectionGroupAndVoteCountCategoryReturnsCorrectReportCountCategory() {
		ElectionGroup electionGroup = (ElectionGroup) getEntityManager()
				.createQuery("select eg from ElectionGroup eg, MvElection mve where mve.electionPath = '200701.01' and eg = mve.electionGroup")
				.getSingleResult();
		Municipality halden = (Municipality) getEntityManager()
				.createQuery("select m from Municipality m, MvArea mva where mva.areaPath = '200701.47.01.0101' and mva.municipality = m").getSingleResult();

		ReportCountCategory result = reportCountCategoryRepository.findByMunicipalityElectionGroupAndVoteCountCategory(
				halden,
				electionGroup,
				CountCategory.VO);

		assertThat(result).isNotNull();
		assertThat(result.getMunicipality()).isEqualTo(halden);
		assertThat(result.getElectionGroup()).isEqualTo(electionGroup);
		assertThat(result.getCountingMode()).isEqualTo(CountingMode.CENTRAL_AND_BY_POLLING_DISTRICT);
		assertThat(result.getCountCategory()).isEqualTo(VO);
	}

	@Test
	public void findByCountyElectionGroupAndCountCategory_givenCountElectionGroupAndCategory_returnsReportCountCategories() {
		ElectionGroup electionGroup = (ElectionGroup) getEntityManager()
				.createQuery("select eg from ElectionGroup eg, MvElection mve where mve.electionPath = '200701.01' and eg = mve.electionGroup")
				.getSingleResult();
		Municipality hvaler = (Municipality) getEntityManager()
				.createQuery("select m from Municipality m, MvArea mva where mva.areaPath = '200701.47.01.0111' and mva.municipality = m").getSingleResult();

		List<ReportCountCategory> result = reportCountCategoryRepository.findByCountyElectionGroupAndCountCategory(AreaPath.from("200701.47.01"),
				electionGroup.getPk(), VF);

		assertThat(result).isNotNull();
		assertThat(result.size()).isEqualTo(5);
		assertThat(result.get(0).getMunicipality()).isEqualTo(hvaler);
		assertThat(result.get(0).getElectionGroup()).isEqualTo(electionGroup);
		assertThat(result.get(0).getCountingMode()).isEqualTo(CountingMode.CENTRAL);
		assertThat(result.get(0).getCountCategory()).isEqualTo(VF);
	}

	@Test
	public void findByMunicipalityElectionGroupAndVoteCountCategoryReturnsNullWhenNoResult() throws Exception {
		Municipality municipality = new Municipality();
		municipality.setPk(MUNICIPALITY_PK_UNKNOWN);
		ElectionGroup electionGroup = new ElectionGroup();
		electionGroup.setPk(ELECTION_GROUP_PK_1);

		ReportCountCategory result = reportCountCategoryRepository.findByMunicipalityElectionGroupAndVoteCountCategory(
				municipality, electionGroup, CountCategory.VF);

		assertThat(result).isNull();
	}

	/**
	 * Please note that all dates and numbers are randomly chosen, and are not necessarily correct according to the election domain.
	 */
	@Test
	public void findCountingModeByMvAreaForMunicipalityAndContest_findsCountMode() {
		ElectionEventStatus eeConfigurationApproved = (ElectionEventStatus) getEntityManager().createQuery(
				"SELECT ees FROM ElectionEventStatus ees WHERE ees.id = 3").getSingleResult();
		Locale norskBokmaal = (Locale) getEntityManager().createQuery("SELECT locale from Locale locale WHERE locale.id = 'nb-NO'").getSingleResult();

		ElectionEvent electionEvent = new ElectionEvent("000042", "Enhetstestvalg", norskBokmaal);
		electionEvent.setElectionEventStatus(eeConfigurationApproved);
		electionEvent.setElectoralRollCutOffDate(new LocalDate(2015, 7, 1));
		electionEvent.setVotingCardDeadline(new LocalDate(2015, 8, 3));
		electionEvent.setVotingCardElectoralRollDate(new LocalDate(2015, 7, 11));
		electionEvent = genericRepository.createEntity(electionEvent);

		ElectionGroup electionGroup = new ElectionGroup("42", "Enhetstestvalg", electionEvent);
		genericRepository.createEntity(electionGroup);

		ElectionType referendum = (ElectionType) getEntityManager().createQuery("SELECT et FROM ElectionType et WHERE et.id = 'R'").getSingleResult();

		Election election = new Election("42", "Enhetstestvalg", referendum, AreaLevelEnum.COUNTRY.getLevel(), electionGroup);
		election.setCandidateRankVoteShareThreshold(BigDecimal.ZERO);
		election.setEndDateOfBirth(new LocalDate(2016, 1, 1).minusYears(18));
		election.setLevelingSeatsVoteShareThreshold(new BigDecimal("0.04"));
		election.setSettlementFirstDivisor(new BigDecimal("1.4"));
		genericRepository.createEntity(election);

		ContestStatus cConfigurationApproved = (ContestStatus) getEntityManager().createQuery("SELECT cs FROM ContestStatus cs WHERE cs.id = 3")
				.getSingleResult();

		Contest contest = new Contest();
		contest.setId("000042");
		contest.setContestStatus(cConfigurationApproved);
		contest.setName("Enhetstestvalg");
		contest.setElection(election);
		genericRepository.createEntity(contest);

		Country norway = new Country("47", "Norge", electionEvent);
		genericRepository.createEntity(norway);

		County austAgder = new County("09", "Aust-Agder", norway);
		austAgder.setLocale(norskBokmaal);
		austAgder.setCountyStatus(genericRepository.findEntityByProperty(CountyStatus.class, "id", 3));
		genericRepository.createEntity(austAgder);

		MunicipalityStatus mConfigurationApproved = (MunicipalityStatus) getEntityManager().createQuery("SELECT ms from MunicipalityStatus ms WHERE ms.id = 3")
				.getSingleResult();

		Municipality arendal = new Municipality("0906", "Arendal", austAgder);
		arendal.setLocale(norskBokmaal);
		arendal.setMunicipalityStatus(mConfigurationApproved);
		genericRepository.createEntity(arendal);

		MvArea arendalArea = (MvArea) getEntityManager().createQuery(
				"SELECT area FROM MvArea area WHERE area.areaPath = '" + electionEvent.getId() + "." + norway.getId() + "." + austAgder.getId() + "."
						+ arendal.getId() + "'")
				.getSingleResult();

		VoteCountCategory valgtingOrdinaere = (VoteCountCategory) getEntityManager().createQuery("SELECT vcc FROM VoteCountCategory vcc WHERE vcc.id = 'VO'")
				.getSingleResult();

		ReportCountCategory arendalReportCountCategory = new ReportCountCategory();
		arendalReportCountCategory.setMunicipality(arendal);
		arendalReportCountCategory.setElectionGroup(electionGroup);
		arendalReportCountCategory.setVoteCountCategory(valgtingOrdinaere);
		arendalReportCountCategory.setCentralPreliminaryCount(CountingMode.BY_POLLING_DISTRICT.isCentralPreliminaryCount());
		arendalReportCountCategory.setTechnicalPollingDistrictCount(CountingMode.BY_POLLING_DISTRICT.isTechnicalPollingDistrictCount());
		arendalReportCountCategory.setPollingDistrictCount(CountingMode.BY_POLLING_DISTRICT.isPollingDistrictCount());
		genericRepository.createEntity(arendalReportCountCategory);

		assertEquals(
				reportCountCategoryRepository.findCountingModeByMvAreaForMunicipalityAndContest(arendalArea.getPk(), contest.getPk()),
				CountingMode.BY_POLLING_DISTRICT);

		Municipality risoer = new Municipality("0901", "Risør", austAgder);
		risoer.setLocale(norskBokmaal);
		risoer.setMunicipalityStatus(mConfigurationApproved);
		genericRepository.createEntity(risoer);

		MvArea risoerArea = (MvArea) getEntityManager().createQuery(
				"SELECT area FROM MvArea area WHERE area.areaPath = '" + electionEvent.getId() + "." + norway.getId() + "." + austAgder.getId() + "."
						+ risoer.getId() + "'")
				.getSingleResult();

		ReportCountCategory risoerReportCountCategory = new ReportCountCategory();
		risoerReportCountCategory.setMunicipality(risoer);
		risoerReportCountCategory.setElectionGroup(electionGroup);
		risoerReportCountCategory.setVoteCountCategory(valgtingOrdinaere);
		risoerReportCountCategory.setCentralPreliminaryCount(CountingMode.CENTRAL.isCentralPreliminaryCount());
		risoerReportCountCategory.setTechnicalPollingDistrictCount(CountingMode.CENTRAL.isTechnicalPollingDistrictCount());
		risoerReportCountCategory.setPollingDistrictCount(CountingMode.CENTRAL.isPollingDistrictCount());
		genericRepository.createEntity(risoerReportCountCategory);

		assertEquals(
				reportCountCategoryRepository.findCountingModeByMvAreaForMunicipalityAndContest(risoerArea.getPk(), contest.getPk()),
				CountingMode.CENTRAL);
	}
}

