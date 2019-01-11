package no.valg.eva.admin.valgnatt.domain.service.resultat;

import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.counting.constants.CountingMode;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.ContestArea;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.configuration.domain.model.PollingDistrict;
import no.valg.eva.admin.configuration.repository.MvAreaRepository;
import no.valg.eva.admin.counting.domain.service.VoteCountService;
import no.valg.eva.admin.felles.sti.valggeografi.KommuneSti;
import no.valg.eva.admin.felles.sti.valggeografi.StemmekretsSti;
import no.valg.eva.admin.felles.sti.valggeografi.ValggeografiSti;
import no.valg.eva.admin.test.MockUtilsTestCase;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static no.evote.constants.AreaLevelEnum.COUNTY;
import static no.evote.constants.AreaLevelEnum.MUNICIPALITY;
import static no.valg.eva.admin.common.counting.model.CountCategory.FO;
import static no.valg.eva.admin.common.counting.model.CountCategory.VO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RapporteringsområdeDomainServiceTest extends MockUtilsTestCase {

    private static final long PK_1 = 1L;
    private static final long PK_2 = 2L;
    private static final long PK_3 = 3L;

    @Test
    public void kommunerForRapportering_areaErKommune_kommuneReturneres() {
        RapporteringsområdeDomainService rapporteringsområdeDomainService = new RapporteringsområdeDomainService(null, null);
        MvArea reportForArea = mock(MvArea.class, RETURNS_DEEP_STUBS);
        when(reportForArea.getAreaLevel()).thenReturn(MUNICIPALITY.getLevel());
        Contest fakeContest = createMock(Contest.class);
        Set<ContestArea> contestAreaSet = new HashSet<>();
        ContestArea fakeContestArea = createMock(ContestArea.class);
        contestAreaSet.add(fakeContestArea);
        when(fakeContestArea.getMvArea()).thenReturn(reportForArea);
        when(fakeContest.getContestAreaSet()).thenReturn(contestAreaSet);

        rapporteringsområdeDomainService.kommunerForRapportering(fakeContest);

        verify(reportForArea, times(1)).getMunicipality();
    }

    @Test
    public void kommunerForRapportering_areaErFylke_kommuneneIFylketReturneres() {
        RapporteringsområdeDomainService rapporteringsområdeDomainService = new RapporteringsområdeDomainService(null, null);
        MvArea reportForArea = mock(MvArea.class, RETURNS_DEEP_STUBS);
        when(reportForArea.getAreaLevel()).thenReturn(COUNTY.getLevel());
        when(reportForArea.getCounty().getMunicipalities()).thenReturn(Collections.emptySet());
        Contest fakeContest = createMock(Contest.class);
        Set<ContestArea> contestAreaSet = new HashSet<>();
        ContestArea fakeContestArea = createMock(ContestArea.class);
        contestAreaSet.add(fakeContestArea);
        when(fakeContestArea.getMvArea()).thenReturn(reportForArea);
        when(fakeContest.getContestAreaSet()).thenReturn(contestAreaSet);

        rapporteringsområdeDomainService.kommunerForRapportering(fakeContest);

        verify(reportForArea, times(1)).getCounty();
    }

    @Test
    public void kommunerForRapportering_areaErSamekrets_kommuneneSomIkkeErChildReturneres() {
        RapporteringsområdeDomainService rapporteringsområdeDomainService = new RapporteringsområdeDomainService(null, null);
        MvArea reportForArea = mock(MvArea.class, RETURNS_DEEP_STUBS);
        when(reportForArea.getAreaLevel()).thenReturn(MUNICIPALITY.getLevel());
        Contest fakeContest = createMock(Contest.class);
        Set<ContestArea> contestAreas = makeContestAreas();
        when(fakeContest.getContestAreaSet()).thenReturn(contestAreas);

        Set<Municipality> municipalities = rapporteringsområdeDomainService.kommunerForRapportering(fakeContest);

        assertThat(municipalities.size()).isEqualTo(2);
        assertThat(municipalities).containsOnly(makeMunicipality(PK_1, "Østre valgkrets"), makeMunicipality(PK_2, "Vadsø"));
    }

    private Set<ContestArea> makeContestAreas() {
        Set<ContestArea> contestAreas = new HashSet<>();
        contestAreas.add(makeContestArea(PK_1, true, false, makeMunicipality(PK_1, "Østre valgkrets")));
        contestAreas.add(makeContestArea(PK_2, false, false, makeMunicipality(PK_2, "Vadsø")));
        contestAreas.add(makeContestArea(PK_3, false, true, makeMunicipality(PK_3, "Berlevåg")));
        return contestAreas;
    }

    private Municipality makeMunicipality(Long pk, String name) {
        Municipality municipality = new Municipality();
        municipality.setPk(pk);
        municipality.setName(name);
        return municipality;
    }

    private ContestArea makeContestArea(long pk, boolean parentArea, boolean childArea, Municipality municipality) {
        ContestArea contestArea = new ContestArea();
        contestArea.setPk(pk);
        contestArea.setParentArea(parentArea);
        contestArea.setChildArea(childArea);
        contestArea.setMvArea(makeMvArea(municipality));
        return contestArea;
    }

    private MvArea makeMvArea(Municipality municipality) {
        MvArea mvArea = new MvArea();
        mvArea.setMunicipality(municipality);
        return mvArea;
    }

    @DataProvider
    public Object[][] kretserForRapporteringAvValgtingsstemmer() {
        return new Object[][]{
                {CountingMode.CENTRAL, 0},
                {CountingMode.BY_POLLING_DISTRICT, 2},
                {null, 0}
        };
    }

    @Test(dataProvider = "kretserForRapporteringAvValgtingsstemmer")
    public void kretserForRapporteringAvValgtingsstemmer_ikkePollingDistrictCount_returnerKretser(CountingMode countingMode, int forventetAntallKretser)
            throws Exception {
        RapporteringsområdeDomainService rapporteringsområdeDomainService = initializeMocks(RapporteringsområdeDomainService.class);
        Municipality municipality = createMock(Municipality.class);
        Collection<PollingDistrict> pollingDistricts = makePollingDistricts();
        when(municipality.regularPollingDistricts(true, false)).thenReturn(pollingDistricts);
        MvElection mvElectionContest = new MvElection();
        when(getInjectMock(VoteCountService.class).countingMode(VO, municipality, mvElectionContest)).thenReturn(countingMode);
        Set<MvArea> mvAreas = rapporteringsområdeDomainService.kretserForRapporteringAvValgtingsstemmer(municipality, mvElectionContest);

        assertThat(mvAreas).hasSize(forventetAntallKretser);
    }

    private Collection<PollingDistrict> makePollingDistricts() {
        List<PollingDistrict> pollingDistricts = new ArrayList<>();
        pollingDistricts.add(createMock(PollingDistrict.class));
        pollingDistricts.add(createMock(PollingDistrict.class));
        return pollingDistricts;
    }

    @DataProvider
    public Object[][] kretserForRapporteringAvForhåndsstemmerOgSentralValgting() {
        return new Object[][]{
                {CountingMode.CENTRAL, 1},

                {CountingMode.BY_TECHNICAL_POLLING_DISTRICT, 3}

        };
    }

    @Test(dataProvider = "kretserForRapporteringAvForhåndsstemmerOgSentralValgting")
    public void kretserForRapporteringAvForhåndsstemmerOgSentralValgting_ikkePollingDistrictCount_returnerKretser(CountingMode countingMode,
                                                                                                                  int forventetAntallKretser) throws Exception {
        RapporteringsområdeDomainService rapporteringsområdeDomainService = initializeMocks(RapporteringsområdeDomainService.class);
        Municipality municipality = createMock(Municipality.class);
        Collection<PollingDistrict> pollingDistricts = makePollingDistricts();
        when(municipality.technicalPollingDistricts()).thenReturn(pollingDistricts);
        MvElection mvElectionContest = new MvElection();
        when(getInjectMock(VoteCountService.class).countingMode(FO, municipality, mvElectionContest)).thenReturn(countingMode);
        Set<MvArea> mvAreas = rapporteringsområdeDomainService.kretserForRapporteringAvForhåndsstemmerOgSentralValgting(municipality, mvElectionContest);

        assertThat(mvAreas).hasSize(forventetAntallKretser);
    }

    @Test
    public void kommunekrets_finnerMvAreaForKrets0000() throws Exception {
        RapporteringsområdeDomainService rapporteringsområdeDomainService = initializeMocks(RapporteringsområdeDomainService.class);

        Municipality fakeMunicipality = createMock(Municipality.class);
        rapporteringsområdeDomainService.kommunekrets(fakeMunicipality);

        verify(fakeMunicipality.areaPath(), times(1)).toMunicipalityPollingDistrictPath();
    }

    @Test
    public void kretsForRapportering_ikkeTekniskKrets_returnerKrets() throws Exception {
        RapporteringsområdeDomainService service = initializeMocks(RapporteringsområdeDomainService.class);
        MvArea fakeMvAreaKrets0000 = createMock(MvArea.class);
        when(fakeMvAreaKrets0000.getPollingDistrict().isTechnicalPollingDistrict()).thenReturn(false);
        when(getInjectMock(MvAreaRepository.class).findSingleByPath(any(AreaPath.class))).thenReturn(fakeMvAreaKrets0000);

        assertThat(service.kretsForRapportering(new AreaPath("150001.47.03.0301.030100.0000"))).isEqualTo(fakeMvAreaKrets0000);
    }

    @Test
    public void kretsForRapportering_tekniskKrets_returnerKrets0000() throws Exception {
        RapporteringsområdeDomainService service = initializeMocks(RapporteringsområdeDomainService.class);
        MvArea fakeMvAreaKrets0001 = createMock(MvArea.class);
        when(fakeMvAreaKrets0001.getPollingDistrict().isTechnicalPollingDistrict()).thenReturn(true);
        AreaPath areaPathKrets0001 = new AreaPath("150001.47.03.0301.030100.0001");
        AreaPath areaPathKrets0000 = new AreaPath("150001.47.03.0301.030100.0000");
        when(fakeMvAreaKrets0001.getMunicipality().areaPath().toMunicipalityPollingDistrictPath()).thenReturn(areaPathKrets0000);
        MvArea fakeMvAreaKrets0000 = createMock(MvArea.class);
        when(getInjectMock(MvAreaRepository.class).findSingleByPath(eq(areaPathKrets0001))).thenReturn(fakeMvAreaKrets0001);
        when(getInjectMock(MvAreaRepository.class).findSingleByPath(eq(areaPathKrets0000))).thenReturn(fakeMvAreaKrets0000);

        assertThat(service.kretsForRapportering(areaPathKrets0001)).isEqualTo(fakeMvAreaKrets0000);
    }

    @Test
    public void finnOmråderForStemmegivningsstatistikk_vanligKrets_returnererKretsen() throws Exception {
        RapporteringsområdeDomainService service = initializeMocks(RapporteringsområdeDomainService.class);

        MvElection mockValgdistrikt = mock(MvElection.class);
        Contest mockContest = mock(Contest.class);
        when(mockValgdistrikt.getContest()).thenReturn(mockContest);
        when(mockContest.getContestAreaList()).thenReturn(new ArrayList<>());
        Set<ValggeografiSti> areaPaths = service.finnOmråderForStemmegivningsstatistikk(mock(Municipality.class), mockValgdistrikt, mock(StemmekretsSti.class));

        assertThat(areaPaths).hasSize(1);
    }

    @Test
    public void finnOmråderForStemmegivningsstatistikk_sjekkAtSamleKommuneKretseBlirReturnert() throws Exception {
        RapporteringsområdeDomainService service = initializeMocks(RapporteringsområdeDomainService.class);

        Set<ValggeografiSti> areaPaths = service.finnOmråderForStemmegivningsstatistikk(mock(Municipality.class), lagMockValgdistrikt(), mock(StemmekretsSti.class));

        assertThat(areaPaths).hasSize(2);
    }

    private MvElection lagMockValgdistrikt() {
        MvElection mockValgdistrikt = mock(MvElection.class);
        Contest mockContest = mock(Contest.class);
        when(mockValgdistrikt.getContest()).thenReturn(mockContest);
        ContestArea ca1 = new ContestArea();
        ca1.setChildArea(true);
        MvArea mvArea1 = new MvArea();
        mvArea1.setAreaPath("100100");
        ca1.setMvArea(mvArea1);
        ContestArea ca2 = new ContestArea();
        ca2.setParentArea(true);
        MvArea mvaArea2 = new MvArea();
        mvaArea2.setAreaPath("200100");
        ca2.setMvArea(mvaArea2);
        List<ContestArea> contestAreaList = new ArrayList<>();
        contestAreaList.add(ca2);
        contestAreaList.add(ca1);
        when(mockContest.getContestAreaList()).thenReturn(contestAreaList);
        return mockValgdistrikt;
    }


    @Test
    public void finnOmråderForStemmegivningsstatistikk_sjekkAtKommunekretsenBlirReturnert() throws Exception {
        RapporteringsområdeDomainService service = initializeMocks(RapporteringsområdeDomainService.class);
        when(getInjectMock(VoteCountService.class).countingMode(any(CountCategory.class), any(Municipality.class), any(MvElection.class)))
                .thenReturn(CountingMode.CENTRAL);
        Municipality fakeMunicipality = mock(Municipality.class);
        when(fakeMunicipality.kommuneSti()).thenReturn(KommuneSti.kommuneSti(new AreaPath("150001.47.03.0301")));

        StemmekretsSti stemmekretsSti = StemmekretsSti.stemmekretsSti(new AreaPath("150001.47.03.0301.030100.0000"));

        Set<ValggeografiSti> valggeografiStier = service.finnOmråderForStemmegivningsstatistikk(fakeMunicipality, mock(MvElection.class), stemmekretsSti);
        assertThat(valggeografiStier.contains(fakeMunicipality.kommuneSti())).isTrue();
        assertThat(valggeografiStier.size()).isEqualTo(1);
    }

    @Test
    public void finnOmråderForStemmegivningsstatistikk_tellekrets_returnererBarna() throws Exception {
        RapporteringsområdeDomainService service = initializeMocks(RapporteringsområdeDomainService.class);
        MvArea fakeMvArea = createMock(MvArea.class);
        when(fakeMvArea.getParentPollingDistrict()).thenReturn(true);
        when(getInjectMock(MvAreaRepository.class).findSingleByPath(any(AreaPath.class))).thenReturn(fakeMvArea);
        Set<PollingDistrict> pollingDistricts = new HashSet<>(makePollingDistricts());
        when(fakeMvArea.getPollingDistrict().getChildPollingDistricts()).thenReturn(pollingDistricts);

        Set<ValggeografiSti> valggeografiStier = service.finnOmråderForStemmegivningsstatistikk(mock(Municipality.class), mock(MvElection.class),
                mock(StemmekretsSti.class));

        assertThat(valggeografiStier).hasSize(2);
    }
}
