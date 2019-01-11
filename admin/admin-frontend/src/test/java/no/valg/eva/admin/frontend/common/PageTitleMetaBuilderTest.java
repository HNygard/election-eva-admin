package no.valg.eva.admin.frontend.common;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import no.evote.constants.AreaLevelEnum;
import no.evote.constants.ElectionLevelEnum;
import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;


public class PageTitleMetaBuilderTest extends BaseFrontendTest {

    private PageTitleMetaBuilder pageTitleMetaBuilder;
    private MvArea mvAreaStub;
    private MvElection mvElectionStub;

    @BeforeMethod
    public void setUp() throws Exception {
        pageTitleMetaBuilder = initializeMocks(PageTitleMetaBuilder.class);
        mvAreaStub = createMock(MvArea.class);
        mvElectionStub = createMock(MvElection.class);
    }

    @Test
    public void area_with_pollingPlaceLevel_returns_fiveItems() {

        setMvAreaLevel(AreaLevelEnum.POLLING_PLACE.getLevel(), false);

        List<PageTitleMetaModel> result = pageTitleMetaBuilder.area(mvAreaStub);

        assertThat(result.size()).isEqualTo(4);

        assertThat(result.get(0).getLabel()).isEqualTo("@area_level[2].name");
        assertThat(result.get(1).getLabel()).isEqualTo("@area_level[3].name");
        assertThat(result.get(2).getLabel()).isEqualTo("@area_level[5].name");
        assertThat(result.get(3).getLabel()).isEqualTo("@area_level[6].name");

    }

    @Test
    public void area_with_pollingPlaceLevel_returns_sixItems_whenOsloMunicipality() {

        setMvAreaLevel(AreaLevelEnum.POLLING_PLACE.getLevel(), true);

        List<PageTitleMetaModel> result = pageTitleMetaBuilder.area(mvAreaStub);

        assertThat(result.size()).isEqualTo(5);

        assertThat(result.get(0).getLabel()).isEqualTo("@area_level[2].name");
        assertThat(result.get(1).getLabel()).isEqualTo("@area_level[3].name");
        assertThat(result.get(2).getLabel()).isEqualTo("@area_level[4].name");
        assertThat(result.get(3).getLabel()).isEqualTo("@area_level[5].name");
        assertThat(result.get(4).getLabel()).isEqualTo("@area_level[6].name");

    }

    @Test
    public void election_with_electionEventLevel_returns_oneItem() {

        setMvElectionLevel(ElectionLevelEnum.ELECTION_EVENT.getLevel());

        List<PageTitleMetaModel> result = pageTitleMetaBuilder.election(mvElectionStub);

        assertThat(result.size()).isEqualTo(0);
    }

    @Test
    public void election_with_electionLevel_returns_threeItems() {

        setMvElectionLevel(ElectionLevelEnum.ELECTION.getLevel());

        List<PageTitleMetaModel> result = pageTitleMetaBuilder.election(mvElectionStub);

        assertThat(result.size()).isEqualTo(2);

        assertThat(result.get(0).getLabel()).isEqualTo("@election_level[1].name");
        assertThat(result.get(1).getLabel()).isEqualTo("@election_level[2].name");
    }

    @Test
    public void election_withNull_returnsEmptyList() {
        assertThat(pageTitleMetaBuilder.election(null)).isEmpty();
    }

    @Test
    public void area_withNull_returnsEmptyList() {
        assertThat(pageTitleMetaBuilder.area(null)).isEmpty();
    }

    @Test
    public void countCategory_withNull_returnsEmptyList() {
        assertThat(pageTitleMetaBuilder.countCategory(null)).isEmpty();
    }

    @Test
    public void countCategory_withFO_returnsOneItem() {
        List<PageTitleMetaModel> result = pageTitleMetaBuilder.countCategory(CountCategory.FO);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getLabel()).isEqualTo("@count.ballot.approve.rejected.category");
        assertThat(result.get(0).getStyleClass()).isNull();
        assertThat(result.get(0).getValue()).isEqualTo("@vote_count_category[FO].name");
    }

    @Test
    public void settlementTitle_withElectionAndCounty_verifyResult() {
        setMvElectionLevel(ElectionLevelEnum.ELECTION.getLevel());
        setMvAreaLevel(AreaLevelEnum.COUNTY.getLevel(), false);

        List<PageTitleMetaModel> result = pageTitleMetaBuilder.settlementTitle(mvElectionStub, mvAreaStub);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getLabel()).isEqualTo("@election_level[2].name");
        assertThat(result.get(0).getValue()).isEqualTo("foo9");
        assertThat(result.get(1).getLabel()).isEqualTo("@election_level[3].name");
        assertThat(result.get(1).getValue()).isEqualTo("foo2");
    }

    private void setMvElectionLevel(int level) {
        when(mvElectionStub.getElectionLevel()).thenReturn(level);
        when(mvElectionStub.getActualElectionLevel()).thenReturn(ElectionLevelEnum.getLevel(level));

        when(mvElectionStub.electionLevelName(ElectionLevelEnum.ELECTION_EVENT)).thenReturn("foo7");
        when(mvElectionStub.electionLevelName(ElectionLevelEnum.ELECTION_GROUP)).thenReturn("foo8");
        when(mvElectionStub.electionLevelName(ElectionLevelEnum.ELECTION)).thenReturn("foo9");
        when(mvElectionStub.electionLevelName(ElectionLevelEnum.CONTEST)).thenReturn("foo10");

        when(mvElectionStub.getElectionEventName()).thenReturn("foo7");
        when(mvElectionStub.getElectionGroupName()).thenReturn("foo8");
        when(mvElectionStub.getElectionName()).thenReturn("foo9");
        when(mvElectionStub.getContestName()).thenReturn("foo10");
    }

    private void verifyPageTitleMetaModels(List<PageTitleMetaModel> expectedResults, List<PageTitleMetaModel> result) {
        if (expectedResults.size() > 0) {
            for (int i = 0; i < expectedResults.size(); i++) {
                PageTitleMetaModel pageTitleMetaModel = expectedResults.get(i);
                PageTitleMetaModel actualResult = result.get(i);
                assertThat(actualResult.getLabel()).isEqualTo(pageTitleMetaModel.getLabel());
            }
        }
    }

    private void setMvAreaLevel(int level, boolean isOslo) {
        when(mvAreaStub.getAreaLevel()).thenReturn(level);
        when(mvAreaStub.getActualAreaLevel()).thenReturn(AreaLevelEnum.getLevel(level));

        when(mvAreaStub.getAreaName(AreaLevelEnum.COUNTRY)).thenReturn("foo1");
        when(mvAreaStub.getAreaName(AreaLevelEnum.COUNTY)).thenReturn("foo2");
        when(mvAreaStub.getAreaName(AreaLevelEnum.POLLING_DISTRICT)).thenReturn("foo3");
        when(mvAreaStub.getAreaName(AreaLevelEnum.POLLING_PLACE)).thenReturn("foo4");
        when(mvAreaStub.getAreaName(AreaLevelEnum.BOROUGH)).thenReturn("foo5");
        when(mvAreaStub.getAreaName(AreaLevelEnum.MUNICIPALITY)).thenReturn("foo6");

        when(mvAreaStub.getCountryName()).thenReturn("foo1");
        when(mvAreaStub.getCountyName()).thenReturn("foo2");
        when(mvAreaStub.getPollingDistrictName()).thenReturn("foo3");
        when(mvAreaStub.getPollingPlaceName()).thenReturn("foo4");
        when(mvAreaStub.getBoroughName()).thenReturn("foo5");
        when(mvAreaStub.getMunicipalityName()).thenReturn("foo6");

        if (isOslo) {
            when(mvAreaStub.getMunicipalityId()).thenReturn(AreaPath.OSLO_MUNICIPALITY_ID);
            when(mvAreaStub.hasMunicipalityPathId(AreaPath.OSLO_MUNICIPALITY_ID)).thenReturn(true);
        }
    }

    @Test(dataProvider = "forArea")
    public void area_withGivenAreaLevels_VerifyResults(List<AreaLevelEnum> areaLevels, boolean isOslo, List<PageTitleMetaModel> expectedResults) {

        List<PageTitleMetaModel> result;
        if (areaLevels.size() > 0) {
            for (AreaLevelEnum areaLevelEnum : areaLevels) {
                setMvAreaLevel(areaLevelEnum.getLevel(), isOslo);
            }
        } else {
            mvAreaStub = null;
        }

        result = pageTitleMetaBuilder.area(mvAreaStub);

        assertThat(result.size()).isEqualTo(expectedResults.size());

        if (result.size() > 0) {
            verifyPageTitleMetaModels(expectedResults, result);
        }
    }

    @DataProvider
    private Object[][] forArea() {
        return new Object[][]
                {
                        {Collections.emptyList(), false, Collections.emptyList()},
                        {singletonList(AreaLevelEnum.COUNTRY), false, Collections.emptyList()},
                        {singletonList(AreaLevelEnum.POLLING_PLACE), false, asList(
                                new PageTitleMetaModel("@area_level[2].name", ""),
                                new PageTitleMetaModel("@area_level[3].name", ""),
                                new PageTitleMetaModel("@area_level[5].name", ""),
                                new PageTitleMetaModel("@area_level[6].name", "")
                        )},
                        {singletonList(AreaLevelEnum.POLLING_PLACE), true, asList(
                                new PageTitleMetaModel("@area_level[2].name", ""),
                                new PageTitleMetaModel("@area_level[3].name", ""),
                                new PageTitleMetaModel("@area_level[4].name", ""),
                                new PageTitleMetaModel("@area_level[5].name", ""),
                                new PageTitleMetaModel("@area_level[6].name", "")
                        )}
                };
    }

    @Test(dataProvider = "forElection")
    public void election_withGivenElectionLevels_verifyResults(List<ElectionLevelEnum> electionLevels, boolean isOslo, List<PageTitleMetaModel> expectedResults) {

        List<PageTitleMetaModel> result;
        if (expectedResults.size() > 0) {
            for (ElectionLevelEnum electionLevelEnum : electionLevels) {
                setMvElectionLevel(electionLevelEnum.getLevel());
            }
        } else {
            mvElectionStub = null;
        }

        result = pageTitleMetaBuilder.election(mvElectionStub);

        assertThat(result.size()).isEqualTo(expectedResults.size());

        if (result.size() > 0) {
            verifyPageTitleMetaModels(expectedResults, result);
        }
    }

    @DataProvider
    private Object[][] forElection() {
        return new Object[][]
                {
                        {Collections.emptyList(), false, Collections.emptyList()},
                        {singletonList(ElectionLevelEnum.ELECTION_EVENT), false, Collections.emptyList()},
                        {singletonList(ElectionLevelEnum.ELECTION), false, asList(
                                new PageTitleMetaModel("@election_level[1].name", ""),
                                new PageTitleMetaModel("@election_level[2].name", ""))
                        }
                };
    }
}

