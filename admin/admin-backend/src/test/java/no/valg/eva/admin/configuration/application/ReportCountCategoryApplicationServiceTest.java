package no.valg.eva.admin.configuration.application;

import no.evote.exception.EvoteException;
import no.evote.security.UserData;
import no.evote.service.configuration.ReportCountCategoryServiceBean;
import no.valg.eva.admin.common.counting.constants.CountingMode;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.Election;
import no.valg.eva.admin.configuration.domain.model.ElectionGroup;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.ReportCountCategory;
import no.valg.eva.admin.configuration.domain.model.VoteCountCategory;
import no.valg.eva.admin.configuration.domain.service.CountingModeDomainService;
import no.valg.eva.admin.configuration.repository.ContestRepository;
import no.valg.eva.admin.counting.domain.service.settlement.CountCategoryDomainService;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.Collections.singletonList;
import static no.valg.eva.admin.common.counting.constants.CountingMode.BY_POLLING_DISTRICT;
import static no.valg.eva.admin.common.counting.constants.CountingMode.BY_TECHNICAL_POLLING_DISTRICT;
import static no.valg.eva.admin.common.counting.constants.CountingMode.CENTRAL;
import static no.valg.eva.admin.common.counting.constants.CountingMode.CENTRAL_AND_BY_POLLING_DISTRICT;
import static no.valg.eva.admin.common.counting.model.CountCategory.BF;
import static no.valg.eva.admin.common.counting.model.CountCategory.FO;
import static no.valg.eva.admin.common.counting.model.CountCategory.FS;
import static no.valg.eva.admin.common.counting.model.CountCategory.VB;
import static no.valg.eva.admin.common.counting.model.CountCategory.VF;
import static no.valg.eva.admin.common.counting.model.CountCategory.VO;
import static no.valg.eva.admin.common.counting.model.CountCategory.VS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class ReportCountCategoryApplicationServiceTest extends LocalConfigApplicationServiceTest {

    @Test
    public void findCountCategoriesByArea_withArea_verifyResponse() throws Exception {
        ReportCountCategoryApplicationService service = initializeMocks(ReportCountCategoryApplicationService.class);
        stub_findReportCountCategoryElementByArea(Arrays.asList(
                forFindCountCategoriesByArea(VO, true, true, true, false),
                forFindCountCategoriesByArea(VS, true, false, true, false),
                forFindCountCategoriesByArea(VF, false, true, true, false)));

        List<no.valg.eva.admin.common.configuration.model.local.ReportCountCategory> result = service.findCountCategoriesByArea(userData(),
                MUNICIPALITY, ELECTION_PATH_ELECTION_GROUP);

        assertThat(result).hasSize(3);
        assertCategory(result.get(0), VO, CENTRAL_AND_BY_POLLING_DISTRICT, 3, true, CENTRAL, CENTRAL_AND_BY_POLLING_DISTRICT,
                BY_TECHNICAL_POLLING_DISTRICT);
        assertCategory(result.get(1), VF, CENTRAL_AND_BY_POLLING_DISTRICT, 1, false, CENTRAL_AND_BY_POLLING_DISTRICT);
        assertCategory(result.get(2), VS, BY_POLLING_DISTRICT, 3, true, CENTRAL, BY_POLLING_DISTRICT, BY_TECHNICAL_POLLING_DISTRICT);
    }

    @Test
    public void sortVotingCategoryList_withAllCategories_verifySortOrder() throws Exception {
        ReportCountCategoryApplicationService service = initializeMocks(ReportCountCategoryApplicationService.class);
        List<no.valg.eva.admin.common.configuration.model.local.ReportCountCategory> categories = Arrays.asList(
                countCategory(VO),
                countCategory(VF),
                countCategory(VS),
                countCategory(VB),
                countCategory(FO),
                countCategory(FS),
                countCategory(BF));

        List<no.valg.eva.admin.common.configuration.model.local.ReportCountCategory> result = service.sortVotingCategoryList(categories);

        assertThat(result).hasSize(7);
        assertSortOrder(result, VO, FO, BF, FS, VF, VS, VB);
    }

    @Test(dataProvider = "includeCentralPollingDistrict")
    public void includeCentralPollingDistrict_withDataProvider_verifyExpected(ReportCountCategory category, boolean expected) throws Exception {
        ReportCountCategoryApplicationService service = initializeMocks(ReportCountCategoryApplicationService.class);

        assertThat(service.includeCentralPollingDistrict(category)).isEqualTo(expected);
    }

    @DataProvider(name = "includeCentralPollingDistrict")
    public Object[][] includeCentralPollingDistrict() {
        return new Object[][]{
                {forIncludeCentralPollingDistrict(true, true, true, true), false},
                {forIncludeCentralPollingDistrict(false, false, true, true), false},
                {forIncludeCentralPollingDistrict(false, false, false, true), false},
                {forIncludeCentralPollingDistrict(false, false, false, false), true}
        };
    }

    @Test(dataProvider = "includePollingDistrict")
    public void includePollingDistrict_withDataProvider_verifyExpected(ReportCountCategory category, boolean expected) throws Exception {
        ReportCountCategoryApplicationService service = initializeMocks(ReportCountCategoryApplicationService.class);

        assertThat(service.includePollingDistrict(category)).isEqualTo(expected);
    }

    @DataProvider(name = "includePollingDistrict")
    public Object[][] includePollingDistrict() {
        return new Object[][]{
                {forIncludeCentralPollingDistrict(true, true, true, true), false},
                {forIncludeCentralPollingDistrict(false, false, true, true), false},
                {forIncludeCentralPollingDistrict(false, false, false, true), false},
                {forIncludeCentralPollingDistrict(false, false, false, false), true}
        };
    }

    @Test(dataProvider = "includeTechnicalPollingDistrict")
    public void includeTechnicalPollingDistrict_withDataProvider_verifyExpected(ReportCountCategory category, boolean expected) throws Exception {
        ReportCountCategoryApplicationService service = initializeMocks(ReportCountCategoryApplicationService.class);

        assertThat(service.includeTechnicalPollingDistrict(category)).isEqualTo(expected);
    }

    @DataProvider(name = "includeTechnicalPollingDistrict")
    public Object[][] includeTechnicalPollingDistrict() {
        return new Object[][]{
                {forIncludeTechnicalPollingDistrict(false, true), false},
                {forIncludeTechnicalPollingDistrict(true, false), false},
                {forIncludeTechnicalPollingDistrict(true, true), true}
        };
    }

    @Test(expectedExceptions = EvoteException.class, expectedExceptionsMessageRegExp = "Could not locate ReportCountCategory VO for 111111.22.33.4444")
    public void updateCountCategories_withNoDataFoundInDataBase_throwsException() throws Exception {
        ReportCountCategoryApplicationService service = initializeMocks(ReportCountCategoryApplicationService.class);
        stub_findReportCountCategoryElementByArea(new ArrayList<>());
        List<no.valg.eva.admin.common.configuration.model.local.ReportCountCategory> categories = singletonList(countCategory(VO, CENTRAL));

        service.updateCountCategories(userData(), MUNICIPALITY, ELECTION_PATH_ELECTION_GROUP, categories);
    }

    @Test
    public void updateCountCategories_withCategories_verifyUpdate() throws Exception {
        ReportCountCategoryApplicationService service = initializeMocks(ReportCountCategoryApplicationService.class);
        List<ReportCountCategory> reportCountCategories = stub_findReportCountCategoryElementByArea(Arrays.asList(
                forUpdateCountCategories(VO),
                forUpdateCountCategories(FO),
                forUpdateCountCategories(FS),
                forUpdateCountCategories(VF)));
        List<no.valg.eva.admin.common.configuration.model.local.ReportCountCategory> categories = Arrays.asList(
                countCategory(VO, CENTRAL),
                countCategory(FO, CENTRAL_AND_BY_POLLING_DISTRICT),
                countCategory(FS, BY_POLLING_DISTRICT),
                countCategory(VF, BY_TECHNICAL_POLLING_DISTRICT));
        UserData userData = userData();

        service.updateCountCategories(userData, MUNICIPALITY, ELECTION_PATH_ELECTION_GROUP, categories);

        verify_reportCountCategory(reportCountCategories.get(0), true, false, false);
        verify_reportCountCategory(reportCountCategories.get(1), true, true, false);
        verify_reportCountCategory(reportCountCategories.get(2), false, true, false);
        verify_reportCountCategory(reportCountCategories.get(3), true, false, true);
        verify(getInjectMock(ReportCountCategoryServiceBean.class))
                .updateCategories(eq(userData), any(Municipality.class), any(ElectionGroup.class), anyList());
    }

    @Test
    public void findBoroughCountCategoriesByArea_withNoBoroughs_returnsEmptyList() throws Exception {
        ReportCountCategoryApplicationService service = initializeMocks(ReportCountCategoryApplicationService.class);
        municipality(false);

        assertThat(service.findBoroughCountCategoriesByArea(userData(), MUNICIPALITY)).isEmpty();
    }

    @Test
    public void findBoroughCountCategoriesByArea_withNoContest_returnsEmptyList() throws Exception {
        ReportCountCategoryApplicationService service = initializeMocks(ReportCountCategoryApplicationService.class);
        municipality(true);
        stub_findBoroughContestsInMunicipality();

        assertThat(service.findBoroughCountCategoriesByArea(userData(), MUNICIPALITY)).isEmpty();
    }

    @Test
    public void findBoroughCountCategoriesByArea_withContest_returnsCategories() throws Exception {
        ReportCountCategoryApplicationService service = initializeMocks(ReportCountCategoryApplicationService.class);
        municipality(true);
        stub_findBoroughContestsInMunicipality(contest("11"));
        stub_countCategories(Arrays.asList(
                CountCategory.VO,
                CountCategory.BF));
        stub_countingModeMapper();

        List<no.valg.eva.admin.common.configuration.model.local.ReportCountCategory> cats = service.findBoroughCountCategoriesByArea(userData(), MUNICIPALITY);

        assertThat(cats).hasSize(2);
        assertCategory(cats.get(0), VO, CENTRAL_AND_BY_POLLING_DISTRICT, 1, false, CENTRAL_AND_BY_POLLING_DISTRICT);
        assertCategory(cats.get(1), BF, CENTRAL, 1, false, CENTRAL);
    }

    private void verify_reportCountCategory(ReportCountCategory categoryStub, boolean centralPreliminaryCount, boolean pollingDistrictCount,
                                            boolean technicalPollingDistrictCount) {
        verify(categoryStub).setCentralPreliminaryCount(centralPreliminaryCount);
        verify(categoryStub).setPollingDistrictCount(pollingDistrictCount);
        verify(categoryStub).setTechnicalPollingDistrictCount(technicalPollingDistrictCount);
    }

    private void assertSortOrder(List<no.valg.eva.admin.common.configuration.model.local.ReportCountCategory> result, CountCategory... order) {
        for (int i = 0; i < result.size(); i++) {
            assertThat(result.get(i).getCategory()).isSameAs(order[i]);
        }
    }

    private void assertCategory(no.valg.eva.admin.common.configuration.model.local.ReportCountCategory category,
                                CountCategory cc, CountingMode mode, int modesSize, boolean isEditable,
                                CountingMode... modes) {
        assertThat(category.getCategory().getId()).isEqualTo(cc.getId());
        assertThat(category.getCountingMode()).isSameAs(mode);
        assertThat(category.getCountingModes()).hasSize(modesSize);
        assertThat(category.isEditable()).isEqualTo(isEditable);
        for (CountingMode m : modes) {
            assertThat(category.getCountingModes()).contains(m);
        }
    }

    private no.valg.eva.admin.common.configuration.model.local.ReportCountCategory countCategory(CountCategory countCategory) {
        return new no.valg.eva.admin.common.configuration.model.local.ReportCountCategory(countCategory, new ArrayList<>());
    }

    private no.valg.eva.admin.common.configuration.model.local.ReportCountCategory countCategory(CountCategory countCategory, CountingMode mode) {
        no.valg.eva.admin.common.configuration.model.local.ReportCountCategory result = countCategory(countCategory);
        result.setCountingMode(mode);
        return result;
    }

    private ReportCountCategory forFindCountCategoriesByArea(CountCategory category, boolean isEditable,
                                                             boolean isCentralPreliminaryCount, boolean isPollingDistrictCount, boolean isTechnicalPollingDistrictCount) {
        return reportCountCategory(category, isEditable, isCentralPreliminaryCount, isPollingDistrictCount, isTechnicalPollingDistrictCount,
                true, true, true, true, true, true);
    }

    private ReportCountCategory forIncludeCentralPollingDistrict(boolean isCategoryForOrdinaryAdvanceVotes, boolean isAdvanceVoteInBallotBox,
                                                                 boolean isMandatoryCentralCount, boolean isMandatoryTotalCount) {
        CountCategory category = isCategoryForOrdinaryAdvanceVotes ? FO : VO;
        return reportCountCategory(category, true, true, true, true, isMandatoryCentralCount, isMandatoryTotalCount, isAdvanceVoteInBallotBox, true, true,
                true);
    }

    private ReportCountCategory forIncludeTechnicalPollingDistrict(boolean isTechnicalPollingDistrictsAllowed,
                                                                   boolean isTechnicalPollingDistrictCountConfigurable) {
        return reportCountCategory(FO, true, true, true, true, true, true, true, isTechnicalPollingDistrictsAllowed,
                isTechnicalPollingDistrictCountConfigurable, true);
    }

    private ReportCountCategory forUpdateCountCategories(CountCategory category) {
        ReportCountCategory result = createMock(ReportCountCategory.class);
        when(result.getCountCategory()).thenReturn(category);
        return result;
    }

    private ReportCountCategory reportCountCategory(CountCategory category, boolean isEditable, boolean isCentralPreliminaryCount,
                                                    boolean isPollingDistrictCount,
                                                    boolean isTechnicalPollingDistrictCount, boolean isMandatoryCentralCount, boolean isMandatoryTotalCount,
                                                    boolean isAdvanceVoteInBallotBox, boolean isTechnicalPollingDistrictsAllowed, boolean isTechnicalPollingDistrictCountConfigurable,
                                                    boolean isSpecialCover) {
        ReportCountCategory result = new ReportCountCategory();
        result.setEditable(isEditable);
        result.setCentralPreliminaryCount(isCentralPreliminaryCount);
        result.setPollingDistrictCount(isPollingDistrictCount);
        result.setTechnicalPollingDistrictCount(isTechnicalPollingDistrictCount);
        result.setTechnicalPollingDistrictCountConfigurable(isTechnicalPollingDistrictCountConfigurable);
        result.setSpecialCover(isSpecialCover);

        VoteCountCategory voteCountCategory = new VoteCountCategory();
        voteCountCategory.setId(category.getId());
        voteCountCategory.setMandatoryCentralCount(isMandatoryCentralCount);
        voteCountCategory.setMandatoryTotalCount(isMandatoryTotalCount);
        result.setVoteCountCategory(voteCountCategory);

        Municipality municipality = new Municipality();
        municipality.setTechnicalPollingDistrictsAllowed(isTechnicalPollingDistrictsAllowed);
        result.setMunicipality(municipality);

        ElectionGroup electionGroup = new ElectionGroup();
        electionGroup.setAdvanceVoteInBallotBox(isAdvanceVoteInBallotBox);
        result.setElectionGroup(electionGroup);
        return result;
    }

    private List<ReportCountCategory> stub_findReportCountCategoryElementByArea(List<ReportCountCategory> categories) {
        when(getInjectMock(ReportCountCategoryServiceBean.class)
                .findReportCountCategoryElementByArea(any(Municipality.class), any(ElectionGroup.class))).thenReturn(categories);
        return categories;
    }

    private Contest contest(String id) {
        Contest contest = createMock(Contest.class);
        Election election = contest.getElection();
        when(election.getId()).thenReturn(id);
        when(election.getName()).thenReturn("Election " + id);
        return contest;
    }

    private void stub_findBoroughContestsInMunicipality() {
        stub_findBoroughContestsInMunicipality(null);
    }

    private void stub_findBoroughContestsInMunicipality(Contest contest) {
        List<Contest> list = new ArrayList<>();
        if (contest != null) {
            list.add(contest);
        }
        when(getInjectMock(ContestRepository.class).findBoroughContestsInMunicipality(any(Municipality.class))).thenReturn(list);
    }

    private void municipality(boolean hasBoroughs) {
        Municipality municipality = createMock(Municipality.class);
        when(municipality.hasBoroughs()).thenReturn(hasBoroughs);
        stub_municipalityByElectionEventAndId(municipality);
    }

    private void stub_countCategories(List<CountCategory> list) {
        when(getInjectMock(CountCategoryDomainService.class).countCategories(any(Contest.class), any(Municipality.class))).thenReturn(list);
    }

    private void stub_countingModeMapper() {
        when(getInjectMock(CountingModeDomainService.class).countingModeMapper(any(Contest.class), any(Municipality.class)))
                .thenReturn(this::countingModeForBorughContestCategory);
    }

    private CountingMode countingModeForBorughContestCategory(CountCategory category) {
        if (category == VO) {
            return CENTRAL_AND_BY_POLLING_DISTRICT;
        }
        return CENTRAL;
    }
}

