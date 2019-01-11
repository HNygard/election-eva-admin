package no.evote.service;

import no.evote.constants.AreaLevelEnum;
import no.evote.security.UserData;
import no.valg.eva.admin.backend.reporting.jasperserver.JasperReportServiceBean;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.configuration.model.Manntallsnummer;
import no.valg.eva.admin.common.counting.model.ContestInfo;
import no.valg.eva.admin.common.reporting.model.ReportExecution;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.ContestArea;
import no.valg.eva.admin.configuration.domain.model.County;
import no.valg.eva.admin.configuration.domain.model.Election;
import no.valg.eva.admin.configuration.domain.model.ElectionDay;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.ElectionGroup;
import no.valg.eva.admin.configuration.domain.model.Locale;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.configuration.domain.model.PollingDistrict;
import no.valg.eva.admin.configuration.domain.model.ReportingUnit;
import no.valg.eva.admin.configuration.domain.model.Valgtype;
import no.valg.eva.admin.configuration.domain.model.Voter;
import no.valg.eva.admin.configuration.domain.service.ManntallsnummerDomainService;
import no.valg.eva.admin.configuration.repository.ContestRepository;
import no.valg.eva.admin.configuration.repository.ElectionRepository;
import no.valg.eva.admin.configuration.repository.MvAreaRepository;
import no.valg.eva.admin.configuration.repository.MvElectionRepository;
import no.valg.eva.admin.configuration.repository.ReportingUnitRepository;
import no.valg.eva.admin.configuration.repository.VoterRepository;
import no.valg.eva.admin.counting.repository.ContestInfoRepository;
import no.valg.eva.admin.felles.sti.valghierarki.ValghierarkiSti;
import no.valg.eva.admin.test.MockUtilsTestCase;
import no.valg.eva.admin.test.TestGroups;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDate;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.ImmutableMap.of;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static no.evote.constants.AreaLevelEnum.MUNICIPALITY;
import static no.evote.service.SpecialPurposeReportsServiceEjb.BOROUGH_ID_PARAM;
import static no.evote.service.SpecialPurposeReportsServiceEjb.BOROUGH_NAME_PARAM;
import static no.evote.service.SpecialPurposeReportsServiceEjb.CATEGORY_ID_PARAM;
import static no.evote.service.SpecialPurposeReportsServiceEjb.CATEGORY_NAME_PARAM;
import static no.evote.service.SpecialPurposeReportsServiceEjb.CONTEST_NAME;
import static no.evote.service.SpecialPurposeReportsServiceEjb.COUNTRY_ID_PARAM;
import static no.evote.service.SpecialPurposeReportsServiceEjb.COUNTY_ID_PARAM;
import static no.evote.service.SpecialPurposeReportsServiceEjb.DATE_MASK_PARAM;
import static no.evote.service.SpecialPurposeReportsServiceEjb.ELECTION_EVENT_NAME_PARAM;
import static no.evote.service.SpecialPurposeReportsServiceEjb.ELECTION_EVENT_PK_PARAM;
import static no.evote.service.SpecialPurposeReportsServiceEjb.ELECTION_NAME;
import static no.evote.service.SpecialPurposeReportsServiceEjb.ENDRINGSTYPE_PARAM;
import static no.evote.service.SpecialPurposeReportsServiceEjb.END_DATE_PARAM;
import static no.evote.service.SpecialPurposeReportsServiceEjb.EXCEL_2007;
import static no.evote.service.SpecialPurposeReportsServiceEjb.IS_SAME_ELECTION_CARD_PARAM;
import static no.evote.service.SpecialPurposeReportsServiceEjb.MUNICIPALITY_ID_PARAM;
import static no.evote.service.SpecialPurposeReportsServiceEjb.MUNICIPALITY_NAME_PARAM;
import static no.evote.service.SpecialPurposeReportsServiceEjb.MV_ELECTION_PK_PARAM;
import static no.evote.service.SpecialPurposeReportsServiceEjb.NUMBER_OF_STICKERS_PARAM;
import static no.evote.service.SpecialPurposeReportsServiceEjb.POLLING_DISTRICT_ID_PARAM;
import static no.evote.service.SpecialPurposeReportsServiceEjb.POLLING_DISTRICT_NAME_PARAM;
import static no.evote.service.SpecialPurposeReportsServiceEjb.REPORTING_UNIT_ADDR_HEADER_NAME_PARAM;
import static no.evote.service.SpecialPurposeReportsServiceEjb.REPORTING_UNIT_ADDR_HEADER_PARAM;
import static no.evote.service.SpecialPurposeReportsServiceEjb.REPORTING_UNIT_ADDR_PARAM;
import static no.evote.service.SpecialPurposeReportsServiceEjb.SEARCH_ONLY_APPROVED_PARAM;
import static no.evote.service.SpecialPurposeReportsServiceEjb.SELECTED_SEARCH_MODE_PARAM;
import static no.evote.service.SpecialPurposeReportsServiceEjb.START_DATE_PARAM;
import static no.evote.service.SpecialPurposeReportsServiceEjb.TYPE_PARAM;
import static no.evote.service.SpecialPurposeReportsServiceEjb.VOTER_PK_PARAM;
import static no.valg.eva.admin.backend.reporting.jasperserver.JasperReportServiceBean.CONTEST;
import static no.valg.eva.admin.backend.reporting.jasperserver.JasperReportServiceBean.ELECTION;
import static no.valg.eva.admin.backend.reporting.jasperserver.JasperReportServiceBean.ELECTION_EVENT;
import static no.valg.eva.admin.backend.reporting.jasperserver.JasperReportServiceBean.ELECTION_GROUP;
import static no.valg.eva.admin.backend.reporting.jasperserver.JasperReportServiceBean.REPORT_LOCALE;
import static no.valg.eva.admin.common.ElectionPath.from;
import static no.valg.eva.admin.common.counting.model.CountCategory.FO;
import static no.valg.eva.admin.common.counting.model.CountCategory.VO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.MapEntry.entry;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Test(groups = TestGroups.RESOURCES)
public class SpecialPurposeReportsServiceEjbTest extends MockUtilsTestCase {
    private static final String ELECTION_EVENT_ID = "752900";
    private static final String ELECTION_GROUP_ID = "01";
    private static final String ELECTION_ID_BOROUGH = "03";
    private static final String ELECTION_ID_MUNICIPALITY = "02";
    private static final String COUNTRY_ID = "47";
    private static final String COUNTY_ID = "03";
    private static final String MUNICIPALITY_ID = AreaPath.OSLO_MUNICIPALITY_ID;
    private static final String BOROUGH_ID = "030107";
    private static final String BOROUGH_NAME = "Borough name";
    private static final String POLLING_DISTRICT_ID = "0001";
    private static final String POLLING_DISTRICT_NAME = "Krets 0001";
    private static final byte[] REPORT_CONTENT = "content".getBytes();
    private static final ElectionPath ELECTION_PATH_BOROUGH_ELECTION = from(ELECTION_EVENT_ID + "." + ELECTION_GROUP_ID + "." + ELECTION_ID_BOROUGH);
    private static final ElectionPath ELECTION_PATH_MUNICIPALITY_ELECTION = from(ELECTION_EVENT_ID + "." + ELECTION_GROUP_ID + "." + ELECTION_ID_MUNICIPALITY);
    private static final String ELECTION_NAME_BOROUGH_ELECTION = "Valg til bydelsvalg";
    private static final AreaPath AREA_PATH_BOROUGH = AreaPath.from("752900." + COUNTRY_ID + "." + COUNTY_ID + "." + MUNICIPALITY_ID + "." + BOROUGH_ID);
    private static final AreaPath AREA_PATH_POLLING_DISTRICT = AreaPath.from(AREA_PATH_BOROUGH + "." + POLLING_DISTRICT_ID);
    private static final String FO_CATEGORY_NAME = "@vote_count_category[FO].name";
    private static final String VO_CATEGORY_NAME = "@vote_count_category[VO].name";
    private static final long MV_ELECTION_BOROUGH_PK = 3L;
    private static final long MV_ELECTION_MUNICIPALITY_PK = 4L;
    private static final String MUNICIPALITY_NAME = "Municipality Name";
    private static final long ELECTION_GROUP_PK = 12L;
    private static final long BOROUGH_MV_AREA_PK = 1L;
    private static final long POLLING_DISTRICT_MV_AREA_PK = 2L;
    private static final long MUNICIPALITY_ELECTION_PK = 5L;
    private static final long BOROUGH_ELECTION_PK = 6L;
    private static final String ELECTION_EVENT_NAME = "Election Event Name";
    private static final long ELECTION_EVENT_PK = 10L;
    private static final String SELECTED_SEARCH_MODE = "M";
    private static final long MV_ELECTION_SAMI_ELECTION_PK = 11L;
    private static final String SAMI_ELECTION_GROUP_NAME = "Sami Election Group";
    private static final String ADDRESS_LINE_1 = "Address line 1";
    private static final String ADDRESS_LINE_2 = "Address line 2";
    private static final String SAMEVALGSTYRET_I_0_KOMMUNE = "@voting.card.reporting_unit_sami_in";
    private static final String POSTAL_CODE = "9999";
    private static final long NO_VOTER = -1L;
    private static final String POST_TOWN = "Post Town";
    private static final String PDF = "pdf";
    private static final int AREA_LEVEL_MUNICIPALITY = 3;
    private SpecialPurposeReportsServiceEjb reportServiceEjb;
    private UserData userDataStub;

    @Mock
    private MvArea fakeMvAreaBorough;

    @Mock
    private MvArea fakeMvAreaPollingDistrict;

    @Mock
    private ElectionEvent fakeElectionEvent;

    @Mock
    private ElectionGroup fakeSamiElectionGroup;

    @Mock
    private Election fakeSamiElection;

    @Mock
    private Election fakeMunicipalityElection;

    @Mock
    private MvElection fakeMvElectionForMunicipality;

    @Mock
    private MvElection fakeMvElectionForSamiElection;

    @Mock
    private Municipality fakeMunicipality;

    @Mock
    private Election fakeBoroughElection;

    @Mock
    private MvElection fakeMvElectionForBorough;

    @Mock
    private MvElection fakeMvElectionContestForBorough;

    @Mock
    private PollingDistrict fakePollingDistrict;

    @BeforeSuite
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    @BeforeMethod
    public void setUp() throws Exception {
        reportServiceEjb = initializeMocks(SpecialPurposeReportsServiceEjb.class);
        when(getInjectMock(JasperReportServiceBean.class).executeReport(any(UserData.class), anyString(), any(Map.class), anyString()))
                .thenReturn(new ReportExecution(REPORT_CONTENT, "reportName", "fileName", PDF, emptyMap(), emptyMap()));
        userDataStub = createMock(UserData.class);
        when(userDataStub.getElectionEventId()).thenReturn(ELECTION_EVENT_ID);

        when(fakeElectionEvent.getName()).thenReturn(ELECTION_EVENT_NAME);
        when(fakeElectionEvent.getId()).thenReturn(ELECTION_EVENT_ID);
        when(fakeElectionEvent.getPk()).thenReturn(ELECTION_EVENT_PK);

        when(fakeSamiElectionGroup.getPk()).thenReturn(ELECTION_GROUP_PK);
        when(fakeSamiElectionGroup.getName()).thenReturn(SAMI_ELECTION_GROUP_NAME);

        when(fakeSamiElection.isSingleArea()).thenReturn(false);
        when(fakeSamiElection.getAreaLevel()).thenReturn(MUNICIPALITY.getLevel());

        when(getInjectMock(ElectionRepository.class).findElectionsByElectionGroup(ELECTION_GROUP_PK)).thenReturn(newArrayList(fakeSamiElection));

        when(fakeBoroughElection.getPk()).thenReturn(BOROUGH_ELECTION_PK);
        when(fakeBoroughElection.getName()).thenReturn(BOROUGH_NAME);

        when(fakeMvElectionForBorough.getElection()).thenReturn(fakeBoroughElection);
        when(fakeMvElectionForBorough.getPk()).thenReturn(MV_ELECTION_BOROUGH_PK);
        when(fakeMvElectionForBorough.getElectionPath()).thenReturn(ELECTION_PATH_BOROUGH_ELECTION.path());
        when(fakeMvElectionForBorough.getElectionEventId()).thenReturn(ELECTION_EVENT_ID);
        when(fakeMvElectionForBorough.getElectionGroupId()).thenReturn(ELECTION_GROUP_ID);
        when(fakeMvElectionForBorough.getElectionId()).thenReturn(ELECTION_ID_BOROUGH);
        when(fakeMvElectionForBorough.getElectionName()).thenReturn(ELECTION_NAME_BOROUGH_ELECTION);
        when(getInjectMock(MvElectionRepository.class).finnEnkeltMedSti(ELECTION_PATH_BOROUGH_ELECTION.tilValghierarkiSti())).thenReturn(fakeMvElectionForBorough);

        when(fakeMvElectionContestForBorough.getElectionPath()).thenReturn(ELECTION_PATH_BOROUGH_ELECTION.path() + "." + BOROUGH_ID);
        when(fakeMvElectionContestForBorough.getElectionEventId()).thenReturn(ELECTION_EVENT_ID);
        when(fakeMvElectionContestForBorough.getElectionId()).thenReturn(ELECTION_GROUP_ID);
        when(fakeMvElectionContestForBorough.getElectionId()).thenReturn(ELECTION_ID_BOROUGH);
        when(fakeMvElectionContestForBorough.getContestId()).thenReturn(BOROUGH_ID);
        when(fakeMvElectionContestForBorough.getContestName()).thenReturn(BOROUGH_NAME);
        when(fakeMvElectionContestForBorough.getElectionName()).thenReturn(ELECTION_NAME_BOROUGH_ELECTION);

        when(fakeMunicipalityElection.getPk()).thenReturn(MUNICIPALITY_ELECTION_PK);

        when(fakeMvElectionForMunicipality.getElection()).thenReturn(fakeMunicipalityElection);
        when(fakeMvElectionForMunicipality.getElectionEvent()).thenReturn(fakeElectionEvent);
        when(fakeMvElectionForMunicipality.getPk()).thenReturn(MV_ELECTION_MUNICIPALITY_PK);
        when(fakeMvElectionForMunicipality.getAreaLevel()).thenReturn(AREA_LEVEL_MUNICIPALITY);

        when(fakeMvElectionForSamiElection.getElection()).thenReturn(fakeSamiElection);
        when(fakeMvElectionForSamiElection.getElectionEvent()).thenReturn(fakeElectionEvent);
        when(fakeMvElectionForSamiElection.getPk()).thenReturn(MV_ELECTION_SAMI_ELECTION_PK);
        when(fakeMvElectionForSamiElection.getElectionGroup()).thenReturn(fakeSamiElectionGroup);

        when(getInjectMock(MvElectionRepository.class).finnEnkeltMedSti(ELECTION_PATH_MUNICIPALITY_ELECTION.tilValghierarkiSti())).thenReturn(fakeMvElectionForMunicipality);

        when(fakeMunicipality.getId()).thenReturn(MUNICIPALITY_ID);
        when(fakeMunicipality.getName()).thenReturn(MUNICIPALITY_NAME);
        Locale locale = new Locale();
        locale.setId("nb-NO");
        when(fakeMunicipality.getLocale()).thenReturn(locale);

        when(fakePollingDistrict.getId()).thenReturn(POLLING_DISTRICT_ID);
        when(fakePollingDistrict.getName()).thenReturn(POLLING_DISTRICT_NAME);

        when(fakeMvAreaBorough.getPk()).thenReturn(BOROUGH_MV_AREA_PK);
        when(fakeMvAreaBorough.getBoroughId()).thenReturn(BOROUGH_ID);
        when(fakeMvAreaBorough.getBoroughName()).thenReturn(BOROUGH_NAME);
        when(fakeMvAreaBorough.getCountryId()).thenReturn(COUNTRY_ID);
        when(fakeMvAreaBorough.getCountyId()).thenReturn(COUNTY_ID);
        when(fakeMvAreaBorough.getBoroughName()).thenReturn(BOROUGH_NAME);
        when(fakeMvAreaBorough.getMunicipality()).thenReturn(fakeMunicipality);
        when(fakeMvAreaBorough.getAreaPath()).thenReturn(AREA_PATH_BOROUGH.path());
        when(getInjectMock(MvAreaRepository.class).findSingleByPath(AREA_PATH_BOROUGH)).thenReturn(fakeMvAreaBorough);

        when(fakeMvAreaPollingDistrict.getPk()).thenReturn(POLLING_DISTRICT_MV_AREA_PK);
        when(fakeMvAreaPollingDistrict.getCountryId()).thenReturn(COUNTRY_ID);
        when(fakeMvAreaPollingDistrict.getCountyId()).thenReturn(COUNTY_ID);
        when(fakeMvAreaPollingDistrict.getMunicipalityId()).thenReturn(MUNICIPALITY_ID);
        when(fakeMvAreaPollingDistrict.getBoroughId()).thenReturn(BOROUGH_ID);
        when(fakeMvAreaPollingDistrict.getBoroughName()).thenReturn(BOROUGH_NAME);
        when(fakeMvAreaPollingDistrict.getPollingDistrictId()).thenReturn(POLLING_DISTRICT_ID);
        when(fakeMvAreaPollingDistrict.getPollingDistrict()).thenReturn(fakePollingDistrict);
        when(fakeMvAreaPollingDistrict.getPollingDistrictName()).thenReturn(POLLING_DISTRICT_NAME);
        when(fakeMvAreaPollingDistrict.getMunicipality()).thenReturn(fakeMunicipality);
        when(fakeMvAreaPollingDistrict.getMunicipalityName()).thenReturn(MUNICIPALITY_NAME);

        when(getInjectMock(MvAreaRepository.class).findSingleByPath(AREA_PATH_POLLING_DISTRICT)).thenReturn(fakeMvAreaPollingDistrict);

        when(getInjectMock(ContestInfoRepository.class)
                .contestForElectionAndArea(fakeBoroughElection, fakeMvAreaBorough))
                .thenReturn(new ContestInfo(ELECTION_PATH_BOROUGH_ELECTION.path(), "Borough election", "Borough 1", AREA_PATH_BOROUGH.path()));

        when(getInjectMock(ContestInfoRepository.class)
                .contestForElectionAndArea(fakeMunicipalityElection, fakeMvAreaPollingDistrict))
                .thenReturn(
                        new ContestInfo(ELECTION_PATH_MUNICIPALITY_ELECTION.path(), "Municipality election", "Municipality Name",
                                AREA_PATH_POLLING_DISTRICT
                                        .path()));

        when(getInjectMock(ManntallsnummerDomainService.class).beregnFulltManntallsnummer(anyLong(), eq(fakeElectionEvent)))
                .thenReturn(new Manntallsnummer("123456789080"));

        ReportingUnit reportingUnit = new ReportingUnit();
        reportingUnit.setMvArea(fakeMvAreaPollingDistrict);
        reportingUnit.setAddressLine1(ADDRESS_LINE_1);
        reportingUnit.setAddressLine2(ADDRESS_LINE_2);
        reportingUnit.setPostalCode(POSTAL_CODE);
        reportingUnit.setPostTown(POST_TOWN);
        when(getInjectMock(ReportingUnitRepository.class).findByMvElectionMvArea(eq(fakeMvElectionForSamiElection.getPk()), anyLong())).thenReturn(
                reportingUnit);

        when(getInjectMock(MvElectionRepository.class).findContestsForElectionAndArea(ELECTION_PATH_BOROUGH_ELECTION, AREA_PATH_BOROUGH)).thenReturn(
                newArrayList(fakeMvElectionContestForBorough));

        Voter mockVoter = mock(Voter.class);
        MvArea fakeMvAreaMunicipality = mock(MvArea.class);
        when(fakeMvAreaMunicipality.getMunicipality()).thenReturn(fakeMunicipality);
        when(mockVoter.getMvArea()).thenReturn(fakeMvAreaMunicipality);
        when(getInjectMock(VoterRepository.class).findByPk(1L)).thenReturn(mockVoter);
    }

    @Test
    public void generateBarCodeLabel_forBoroughElection_sendsExpectedReportArgumentsAndReturnsBytes() {
        final Map<String, String> expectedParameterValues = new HashMap<>();

        expectedParameterValues.putAll(of(
                COUNTRY_ID_PARAM, COUNTRY_ID,
                MV_ELECTION_PK_PARAM, String.valueOf(MV_ELECTION_BOROUGH_PK),
                NUMBER_OF_STICKERS_PARAM, "2",
                CATEGORY_ID_PARAM, FO.getId(),
                MUNICIPALITY_ID_PARAM, MUNICIPALITY_ID));

        expectedParameterValues.putAll(of(
                BOROUGH_ID_PARAM, BOROUGH_ID,
                CATEGORY_NAME_PARAM, FO_CATEGORY_NAME,
                TYPE_PARAM, "1",
                BOROUGH_NAME_PARAM, BOROUGH_NAME,
                COUNTY_ID_PARAM, COUNTY_ID));

        expectedParameterValues.putAll(of(
                MUNICIPALITY_NAME_PARAM, "",
                POLLING_DISTRICT_ID_PARAM, "",
                POLLING_DISTRICT_NAME_PARAM, ""));

        when(fakeMvElectionForMunicipality.getAreaLevel()).thenReturn(AreaLevelEnum.COUNTY.getLevel());

        byte[] result = reportServiceEjb.generateScanningBoxLabel(userDataStub, ELECTION_PATH_BOROUGH_ELECTION, FO, AREA_PATH_BOROUGH, 2);

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(REPORT_CONTENT);
        verify(getInjectMock(JasperReportServiceBean.class)).executeReport(any(UserData.class), anyString(), eq(expectedParameterValues), eq(PDF));
    }

    @Test
    public void generateBarCodeLabel_forMunicipalityElection_sendsExpectedReportArgumentsAndReturnsBytes() {

        byte[] result = reportServiceEjb.generateScanningBoxLabel(userDataStub, ELECTION_PATH_MUNICIPALITY_ELECTION, VO, AREA_PATH_POLLING_DISTRICT, 2);

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(REPORT_CONTENT);

        ArgumentCaptor<Map> parameters = ArgumentCaptor.forClass(Map.class);
        verify(getInjectMock(JasperReportServiceBean.class)).executeReport(any(UserData.class), anyString(), parameters.capture(), eq(PDF));

        assertThat(parameters.getValue()).contains(
                entry(COUNTRY_ID_PARAM, COUNTRY_ID),
                entry(MV_ELECTION_PK_PARAM, String.valueOf(MV_ELECTION_MUNICIPALITY_PK)),
                entry(NUMBER_OF_STICKERS_PARAM, "2"),
                entry(MUNICIPALITY_ID_PARAM, MUNICIPALITY_ID),
                entry(BOROUGH_ID_PARAM, BOROUGH_ID),
                entry(CATEGORY_NAME_PARAM, VO_CATEGORY_NAME),
                entry(TYPE_PARAM, "1"),
                entry(BOROUGH_NAME_PARAM, BOROUGH_NAME),
                entry(COUNTY_ID_PARAM, COUNTY_ID),
                entry(MUNICIPALITY_NAME_PARAM, ""), // when election is not on county level, municipality name is not set
                entry(POLLING_DISTRICT_ID_PARAM, POLLING_DISTRICT_ID),
                entry(POLLING_DISTRICT_NAME_PARAM, POLLING_DISTRICT_NAME));
    }

    @Test
    public void generateElectionCard_returnsBytes() {
        byte[] result = reportServiceEjb.generateElectionCard(userDataStub, -1L, fakeMvAreaPollingDistrict, fakeMvElectionForMunicipality);
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(REPORT_CONTENT);
    }

    @Test
    public void generateElectionCard_forSpecificVoter_setsReportLocale() {
        Locale nnLocale = new Locale();
        nnLocale.setId("nn-NO");
        when(fakeMunicipality.getLocale()).thenReturn(nnLocale);
        ArgumentCaptor<Map> reportParameterCaptor = ArgumentCaptor.forClass(Map.class);
        reportServiceEjb.generateElectionCard(userDataStub, 1L, fakeMvAreaPollingDistrict, fakeMvElectionForMunicipality);
        verify(getInjectMock(JasperReportServiceBean.class)).executeReport(any(UserData.class), anyString(), reportParameterCaptor.capture(),
                anyString());
        assertThat(reportParameterCaptor.getValue().get(REPORT_LOCALE)).isEqualTo(nnLocale.toJavaLocale().toString() + "_" + ELECTION_EVENT_ID);
    }

    @Test
    public void generateElectionCardForSamiElection_returnsBytes() {

        final Map<String, String> expectedParameterValues = new HashMap<>(of(
                VOTER_PK_PARAM, String.valueOf(NO_VOTER),
                IS_SAME_ELECTION_CARD_PARAM, "true",
                ELECTION_EVENT_NAME_PARAM, SAMI_ELECTION_GROUP_NAME,
                REPORTING_UNIT_ADDR_HEADER_PARAM, SAMEVALGSTYRET_I_0_KOMMUNE));

        expectedParameterValues.put(REPORTING_UNIT_ADDR_HEADER_NAME_PARAM, MUNICIPALITY_NAME);
        expectedParameterValues.put(REPORTING_UNIT_ADDR_PARAM, ("\n" + ADDRESS_LINE_1 + "\n" + ADDRESS_LINE_2 + "\n" + POSTAL_CODE + " " + POST_TOWN));
        expectedParameterValues.put(DATE_MASK_PARAM, "DD.MM.YYYY");

        byte[] result = reportServiceEjb.generateElectionCard(userDataStub, NO_VOTER, fakeMvAreaPollingDistrict, fakeMvElectionForSamiElection);
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(REPORT_CONTENT);

        verify(getInjectMock(JasperReportServiceBean.class)).executeReport(any(UserData.class), anyString(), eq(expectedParameterValues), eq(PDF));
    }

    @Test
    public void generateElectoralRollHistoryForMunicipality_sendsExpectedReportArgumentsAndReturnsBytes() {
        final Map<String, String> expectedParameterValues = new HashMap<>();

        expectedParameterValues.putAll(of(
                START_DATE_PARAM, "2014-01-01 00:00:00",
                ENDRINGSTYPE_PARAM, " ",
                SEARCH_ONLY_APPROVED_PARAM, "true",
                MUNICIPALITY_ID_PARAM, MUNICIPALITY_ID,
                END_DATE_PARAM, "2015-01-01 23:59:59"));

        expectedParameterValues.putAll(of(
                ELECTION_EVENT_PK_PARAM, String.valueOf(ELECTION_EVENT_PK),
                DATE_MASK_PARAM, "YYYY-MM-DD HH24:MI:SS",
                SELECTED_SEARCH_MODE_PARAM, SELECTED_SEARCH_MODE));

        byte[] result = reportServiceEjb.generateElectoralRollHistoryForMunicipality(
                userDataStub, fakeMunicipality, ' ', new LocalDate(2014, 1, 1), new LocalDate(2015, 1, 1), ELECTION_EVENT_PK, SELECTED_SEARCH_MODE, true);
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(REPORT_CONTENT);

        verify(getInjectMock(JasperReportServiceBean.class)).executeReport(any(UserData.class), anyString(), eq(expectedParameterValues), eq(PDF));
    }

    @Test
    public void generateBallots_sendsExpectedReportArgumentsAndReturnsBytes() {
        String electionName = fakeMvElectionForBorough.getElectionName();
        String contestName = fakeMvElectionContestForBorough.getContestName();
        Map<String, String> expectedParameterValues = new HashMap<>(of(
                ELECTION_EVENT, ELECTION_EVENT_ID,
                ELECTION_GROUP, ELECTION_GROUP_ID,
                ELECTION, ELECTION_ID_BOROUGH,
                CONTEST, BOROUGH_ID,
                ELECTION_NAME, electionName));

        expectedParameterValues.put(CONTEST_NAME, contestName);
        Contest contest = createMock(Contest.class);
        when(contest.getContestAreaSet()).thenReturn(new HashSet<>(singletonList(createMock(ContestArea.class))));
        when(contest.getElection().getElectionGroup().getElectionEvent().getId()).thenReturn(ELECTION_EVENT_ID);
        when(contest.getElection().getElectionGroup().getId()).thenReturn(ELECTION_GROUP_ID);
        when(contest.getElection().getId()).thenReturn(ELECTION_ID_BOROUGH);
        when(contest.getElectionName()).thenReturn(electionName);
        when(contest.getName()).thenReturn(contestName);
        when(contest.getId()).thenReturn(BOROUGH_ID);
        when(getInjectMock(ContestRepository.class).findSingleByPath(any(ElectionPath.class))).thenReturn(contest);

        byte[] result = reportServiceEjb.generateBallots(userDataStub, ValghierarkiSti.valgdistriktSti(ELECTION_PATH_CONTEST));
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(REPORT_CONTENT);

        verify(getInjectMock(JasperReportServiceBean.class)).executeReport(any(UserData.class), anyString(), eq(expectedParameterValues), eq(EXCEL_2007));
    }

    @Test
    public void testElectionName_givenKommunestyrevalg_onCountyLevel_andNynorskMalform() {
        prepareAndRunElectionNameTest(
                "Kommunestyrevalet 2019",
                "Kommunestyrevalget 2019",
                Valgtype.KOMMUNESTYREVALG,
                "2019",
                contestArea(false, false, AreaLevelEnum.COUNTY, "nn-NO")
        );
    }

    private ContestArea contestArea(boolean parentArea, boolean childArea, AreaLevelEnum ale, String localeId) {

        Locale loc = new Locale();
        loc.setId(localeId);

        MvArea mva = createMock(MvArea.class);
        when(mva.getActualAreaLevel()).thenReturn(ale);
        if (ale == AreaLevelEnum.COUNTY) {
            County cou = createMock(County.class);
            when(cou.getLocale()).thenReturn(loc);
            when(mva.getCounty()).thenReturn(cou);
        } else if (ale.equalOrlowerThan(AreaLevelEnum.MUNICIPALITY)) {
            Municipality mun = createMock(Municipality.class);
            when(mun.getLocale()).thenReturn(loc);
            when(mva.getMunicipality()).thenReturn(mun);
        }

        ContestArea ca = createMock(ContestArea.class);
        when(ca.isParentArea()).thenReturn(parentArea);
        when(ca.isChildArea()).thenReturn(childArea);
        when(ca.getMvArea()).thenReturn(mva);
        return ca;
    }
    
    private void prepareAndRunElectionNameTest(String expectedElectionName, String fallbackElectionName, Valgtype valgtype, String electionYear, ContestArea... contestAreas) {

        Contest contest = createMock(Contest.class);
        when(contest.getElection().getValgtype()).thenReturn(valgtype);
        when(contest.getElectionName()).thenReturn(fallbackElectionName);
        when(contest.getContestAreaList()).thenReturn(Arrays.asList(contestAreas));

        if (StringUtils.isEmpty(electionYear)) {
            when(contest.getElection().getElectionGroup().getElectionEvent().getElectionDays()).thenReturn(new HashSet<>());
        }
        else {
            Set<ElectionDay> electionDays = electionDaysInYear(electionYear);
            when(contest.getElection().getElectionGroup().getElectionEvent().getElectionDays()).thenReturn(electionDays);
        }

        assertThat(reportServiceEjb.resolveElectionNameFor(contest)).isEqualTo(expectedElectionName);
    }

    private Set<ElectionDay> electionDaysInYear(String year) {
        ElectionDay ed = createMock(ElectionDay.class);
        when(ed.electionYear()).thenReturn(year);

        Set<ElectionDay> eds = new HashSet<>();
        eds.add(ed);
        return eds;
    }
    
    @Test
    public void testElectionName_givenNoElectionType_returnsFallbackElectionName() {
        prepareAndRunElectionNameTest(
                "Kommunestyrevalget 2019",
                "Kommunestyrevalget 2019",
                null,
                "2019"
        );
    }

    @Test
    public void testElectionName_givenStortingsvalg_onMunicipalityLevel_andNynorskMalform() {
        prepareAndRunElectionNameTest(
                "Stortingsvalet 2021",
                "Stortingsvalget 2021",
                Valgtype.STORTINGSVALG,
                "2021",
                contestArea(false, false, AreaLevelEnum.MUNICIPALITY, "nn-NO")
        );
    }

    @Test
    public void testElectionName_givenSametingsvalg_onMunicipalityLevel_andBokmalMalform() {
        prepareAndRunElectionNameTest(
                "Sametingsvalget 2021",
                "Sametingsvalget 2021",
                Valgtype.SAMETINGSVALG,
                "2021",
                contestArea(false, true, AreaLevelEnum.MUNICIPALITY, "nn-NO"),
                contestArea(true, false, AreaLevelEnum.MUNICIPALITY, "nb-NO"),
                contestArea(false, true, AreaLevelEnum.MUNICIPALITY, "nn-NO")
        );
    }

    @Test
    public void testElectionName_givenKommunestyrevalg_onCountryLevel_returnsSystemDefaultBokmalForm() {
        prepareAndRunElectionNameTest(
                "Kommunestyrevalget 2021",
                "Kommunestyrevalet 2021",
                Valgtype.KOMMUNESTYREVALG,
                "2021",
                contestArea(false, false, AreaLevelEnum.COUNTRY, "nn-NO")
        );
    }

    @Test
    public void testElectionName_givenKommunestyrevalg_withoutElectionDays_returnsNameWithoutYear() {
        prepareAndRunElectionNameTest(
                "Kommunestyrevalget ",
                "Kommunestyrevalget 2021",
                Valgtype.KOMMUNESTYREVALG,
                "",
                contestArea(false, false, AreaLevelEnum.MUNICIPALITY, "nb-NO")
        );
    }
}
