package no.valg.eva.admin.configuration.domain.model;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.TEN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import no.evote.constants.AreaLevelEnum;
import no.evote.util.MvAreaBuilder;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.counting.constants.ReportingUnitTypeId;
import no.valg.eva.admin.configuration.domain.visitor.ConfigurationVisitor;
import no.valg.eva.admin.test.MockUtilsTestCase;

import org.testng.annotations.Test;


public class ContestTest extends MockUtilsTestCase {

	private static final int MIN_PROPOSERS_NEW_PARTY = 500;
	private static final int MIN_PROPOSERS_OLD_PARTY = 2;
	private static final String ANY_SUBPATH = "01";
	private static final String COUNTRY_PATH = "150001.47.";
	private static final String SUBPATH_OSTFOLD = "01";
	private static final String SUBPATH_VESTFOLD = "07";

	@Test
	public void isOnBoroughLevel_withOneContestAreaOnBoroughLevel_returnsTrue() throws Exception {
		Contest contest = new Contest();
		ContestArea contestArea = new ContestArea();
		MvArea mvArea = new MvArea();
		mvArea.setAreaLevel(AreaLevelEnum.BOROUGH.getLevel());
		contestArea.setMvArea(mvArea);
		contest.getContestAreaSet().add(contestArea);

		boolean onBoroughLevel = contest.isOnBoroughLevel();

		assertThat(onBoroughLevel).isTrue();
	}

	@Test
	public void hasContestAreaForAreaPath_withNoContestAreas_returnsFalse() throws Exception {
		Contest contest = new Contest();
		mockFieldValue("contestAreaSet", new HashSet<>());

		assertThat(contest.hasContestAreaForAreaPath(AreaPath.from("111111.11.11"))).isFalse();
	}

	@Test
	public void hasContestAreaForAreaPath_withSubContestArea_returnsTrue() throws Exception {
		Contest contest = initializeMocks(new Contest());
		Set<ContestArea> contestAreaSet = new HashSet<>();
		contestAreaSet.add(createMock(ContestArea.class));
		when(contestAreaSet.iterator().next().getMvArea().getAreaPath()).thenReturn("111111.11.11");
		mockFieldValue("contestAreaSet", contestAreaSet);

		assertThat(contest.hasContestAreaForAreaPath(AreaPath.from("111111.11.11"))).isTrue();
	}

	@Test
	public void hasContestAreaForAreaPath_withNoContestAreaButAtMunicipalityLevelAnd_returnsTrue() throws Exception {
		Contest contest = initializeMocks(new Contest());
		Set<ContestArea> contestAreaSet = new HashSet<>();
		contestAreaSet.add(createMock(ContestArea.class));
		when(contestAreaSet.iterator().next().getMvArea().getAreaPath()).thenReturn("111111.11.11.1111.111111");
		mockFieldValue("contestAreaSet", contestAreaSet);

		assertThat(contest.hasContestAreaForAreaPath(AreaPath.from("111111.11.11.1111"))).isTrue();
	}

	@Test
	public void finalReportingUnitTypeForArea_whenSamiElection_returns_idForOpptellingsvalgstyret() {
		boolean singleArea = false;
		Contest contest = createContest(singleArea, AreaLevelEnum.COUNTY);

		assertThat(contest.finalReportingUnitTypeForArea()).isEqualTo(ReportingUnitTypeId.OPPTELLINGSVALGSTYRET);
	}

	@Test
	public void finalReportingUnitTypeForArea_whenContestIsOnCounty_returns_idForFylkesvalgsstyret() {
		boolean singleArea = true;
		Contest contest = createContest(singleArea, AreaLevelEnum.COUNTY);

		assertThat(contest.finalReportingUnitTypeForArea()).isEqualTo(ReportingUnitTypeId.FYLKESVALGSTYRET);
	}

	@Test
	public void finalReportingUnitTypeForArea_whenContestIsNotCounty_returns_idForValgsstyret() {
		boolean singleArea = true;
		Contest contest = createContest(singleArea, AreaLevelEnum.MUNICIPALITY);

		assertThat(contest.finalReportingUnitTypeForArea()).isEqualTo(ReportingUnitTypeId.VALGSTYRET);
	}

	private Contest createContest(boolean singleArea, AreaLevelEnum areaLevelEnum) {
		Contest contest = new Contest();
		Election election = createMock(Election.class);
		when(election.isSingleArea()).thenReturn(singleArea);
		contest.setElection(election);
		Set<ContestArea> contestAreas = new HashSet<>();
		ContestArea contestArea = createMock(ContestArea.class);
		when(contestArea.getActualAreaLevel()).thenReturn(areaLevelEnum);
		contestAreas.add(contestArea);
		contest.setContestAreaSet(contestAreas);
		return contest;
	}

	@Test
	public void hasRenumbering_whenElectionIsRenumberIsTrue_returnsTrue() throws Exception {
		Contest contest = new Contest();
		Election election = new Election();
		election.setRenumber(true);
		contest.setElection(election);
		assertThat(contest.hasRenumbering()).isTrue();
	}

	@Test
	public void hasRenumbering_whenElectionIsRenumberIsFalse_returnsFalse() throws Exception {
		Contest contest = new Contest();
		Election election = new Election();
		election.setRenumber(false);
		contest.setElection(election);
		assertThat(contest.hasRenumbering()).isFalse();
	}

	@Test
	public void getCandidateRankVoteShareThreshold_givenContest_returnsValueFromElection() throws Exception {
		Election election = mock(Election.class);
		when(election.getCandidateRankVoteShareThreshold()).thenReturn(TEN);
		Contest contest = new Contest();
		contest.setElection(election);
		assertThat(contest.getCandidateRankVoteShareThreshold()).isEqualTo(TEN);
	}

	@Test
	public void getSettlementFirstDivisor_givenElectionWithSettlementFirstDivisor_returnsSettlementFirstDivisor() throws Exception {
		BigDecimal settlementFirstDivisor = ONE;
		Election election = mock(Election.class);
		when(election.getSettlementFirstDivisor()).thenReturn(settlementFirstDivisor);
		Contest contest = new Contest();
		contest.setElection(election);
		assertThat(contest.getSettlementFirstDivisor()).isEqualTo(settlementFirstDivisor);
	}

	@Test
	public void getBaselineVoteFactor_givenElectionWithBaselineVoteFactor_returnsBaselineVoteFactor() throws Exception {
		BigDecimal baselineVoteFactor = ONE;
		Election election = mock(Election.class);
		when(election.getBaselineVoteFactor()).thenReturn(baselineVoteFactor);
		Contest contest = new Contest();
		contest.setElection(election);
		assertThat(contest.getBaselineVoteFactor()).isEqualTo(baselineVoteFactor);
	}

	@Test
	public void accept_givenVisitorWithIncludeTrue_callsVisitOnVisitor() throws Exception {
		ConfigurationVisitor visitor = mock(ConfigurationVisitor.class);
		Contest contest = new Contest();
		contest.setPk(new Random().nextLong());
		when(visitor.include(contest)).thenReturn(true);
		contest.accept(visitor);
		verify(visitor).visit(contest);
	}

	@Test
	public void accept_givenVisitorWithIncludeFalse_doesNotCallVisitOnVisitor() throws Exception {
		ConfigurationVisitor visitor = mock(ConfigurationVisitor.class);
		Contest contest = new Contest();
		contest.setPk(new Random().nextLong());
		when(visitor.include(contest)).thenReturn(false);
		contest.accept(visitor);
		verify(visitor, never()).visit(contest);
	}

	@Test
	public void accept_givenVisitorWithIncludeTrue_callsAcceptOnBallots() throws Exception {
		ConfigurationVisitor visitor = mock(ConfigurationVisitor.class);
		Contest contest = new Contest();
		contest.setPk(new Random().nextLong());
		Ballot ballot = mock(Ballot.class);
		contest.getBallots().add(ballot);
		when(visitor.include(contest)).thenReturn(true);
		contest.accept(visitor);
		verify(ballot).accept(visitor);
	}

	@Test
	public void accept_givenVisitorWithIncludeFalse_doesNotCallAcceptOnBallots() throws Exception {
		ConfigurationVisitor visitor = mock(ConfigurationVisitor.class);
		Contest contest = new Contest();
		contest.setPk(new Random().nextLong());
		Ballot ballot = mock(Ballot.class);
		contest.getBallots().add(ballot);
		when(visitor.include(contest)).thenReturn(false);
		contest.accept(visitor);
		verify(ballot, never()).accept(visitor);
	}

	@Test
	public void affiliationBaselineVotesFactor_givenContestWithWriteIns_returnsNumberOfPositions() throws Exception {
		Election election = mock(Election.class);
		when(election.isWritein()).thenReturn(true);
		Contest contest = new Contest();
		contest.setNumberOfPositions(10);
		contest.setElection(election);
		assertThat(contest.affiliationBaselineVotesFactor()).isEqualTo(10);
	}

	@Test
	public void affiliationBaselineVotesFactor_givenContestWithoutWriteIns_returnsOne() throws Exception {
		Election election = mock(Election.class);
		when(election.isWritein()).thenReturn(false);
		Contest contest = new Contest();
		contest.setNumberOfPositions(10);
		contest.setElection(election);
		assertThat(contest.affiliationBaselineVotesFactor()).isEqualTo(1);
	}
	
	@Test
	public void minNumberOfProposersFor_partyHasForenkletBehandling_returnsMinNumberForOldParty() {
		Contest contest = makeContest(MIN_PROPOSERS_OLD_PARTY, MIN_PROPOSERS_NEW_PARTY);
		Party party = makeParty(true);
		
		assertThat(contest.minNumberOfProposersFor(party)).isEqualTo(MIN_PROPOSERS_OLD_PARTY);
	}
    
	@Test
	public void minNumberOfProposersFor_partyHasNotForenkletBehandling_returnsMinNumberForNewParty() {
		Contest contest = makeContest(MIN_PROPOSERS_OLD_PARTY, MIN_PROPOSERS_NEW_PARTY);
		Party party = makeParty(false);
		
		assertThat(contest.minNumberOfProposersFor(party)).isEqualTo(MIN_PROPOSERS_NEW_PARTY);
	}

	@Test(expectedExceptions = IllegalStateException.class)
	public void minNumberOfProposersFor_minNumbersNotConfigured_throwsIllegalStateException() {
		Contest contest = makeContest(MIN_PROPOSERS_OLD_PARTY, null);
		Party party = makeParty(false);
		
		assertThat(contest.minNumberOfProposersFor(party)).isEqualTo(MIN_PROPOSERS_NEW_PARTY);
	}

	private Party makeParty(boolean simplifiedTreatment) {
		Party party = new Party();
		party.setForenkletBehandling(simplifiedTreatment);
		return party;
	}

	private Contest makeContest(Integer minProposersOldParty, Integer minProposersNewParty) {
		Contest contest = new Contest();
		contest.setMinProposersOldParty(minProposersOldParty);
		contest.setMinProposersNewParty(minProposersNewParty);
		return contest;
	}
	
	@Test
	public void getSortedContestAreaList_returnsListWithParentFirst() {
		Contest contest = new Contest();
		contest.setContestAreaSet(makeContestAreas(ANY_SUBPATH));

		List<ContestArea> contestAreaList = contest.getContestAreaList();
		assertThat(contestAreaList.get(0).isParentArea()).isTrue();
	}

	@Test
	public void areaIdForLocalParties_contestOnCounty_areaIdIsCountyId() {
		Contest contest = new Contest();
		String countyId = "03";
		contest.setContestAreaSet(makeContestAreas(countyId));

		assertThat(contest.areaIdForLocalParties()).isEqualTo(countyId);
	}

	@Test
	public void areaIdForLocalParties_contestOnMunicipality_areaIdIsMunicipalityId() {
		Contest contest = new Contest();
		String countyId = "03";
		String municipalityId = AreaPath.OSLO_MUNICIPALITY_ID;
		contest.setContestAreaSet(makeContestAreas(countyId + "." + municipalityId));

		assertThat(contest.areaIdForLocalParties()).isEqualTo(municipalityId);
	}
	
	@Test
	public void areaIdForLocalParties_contestOnBorough_areaIdIsMunicipalityId() {
		Contest contest = new Contest();
		String countyId = "03";
		String municipalityId = AreaPath.OSLO_MUNICIPALITY_ID;
		String boroughId = "030101";
		contest.setContestAreaSet(makeContestAreas(countyId + "." + municipalityId + "." + boroughId));

		assertThat(contest.areaIdForLocalParties()).isEqualTo(municipalityId);
	}
	
	@Test
	public void isForArea_matchingAreaPath_true() {
		Contest contest = new Contest();
		Set<ContestArea> contestAreas = new HashSet<>();
		contestAreas.add(makeContestArea(1L, false, SUBPATH_OSTFOLD));
		contest.setContestAreaSet(contestAreas);
		
		assertThat(contest.isForArea(AreaPath.from(COUNTRY_PATH + SUBPATH_OSTFOLD))).isTrue();
	}

	@Test
	public void isForArea_notMatchingAreaPath_false() {
		Contest contest = new Contest();
		Set<ContestArea> contestAreas = new HashSet<>();
		contestAreas.add(makeContestArea(1L, false, SUBPATH_VESTFOLD));
		contest.setContestAreaSet(contestAreas);
		
		assertThat(contest.isForArea(AreaPath.from(COUNTRY_PATH + SUBPATH_OSTFOLD))).isFalse();
	}
	
	private Set<ContestArea> makeContestAreas(String subpath) {
		HashSet<ContestArea> contestAreas = new HashSet<>();
		contestAreas.add(makeContestArea(1L, false, ANY_SUBPATH));
		contestAreas.add(makeContestArea(2L, true, subpath));
		return contestAreas;
	}

	private ContestArea makeContestArea(long pk, boolean parentArea, String subpath) {
		ContestArea contestArea = new ContestArea();
		contestArea.setPk(pk);
		contestArea.setParentArea(parentArea);
		contestArea.setMvArea(makeMvArea(subpath));
		return contestArea;
	}

	private MvArea makeMvArea(String subpath) {
		return new MvAreaBuilder(AreaPath.from(COUNTRY_PATH + subpath)).getValue();
	}
}

