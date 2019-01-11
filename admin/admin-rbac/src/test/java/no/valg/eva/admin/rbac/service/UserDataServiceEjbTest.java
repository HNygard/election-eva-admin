package no.valg.eva.admin.rbac.service;

import no.evote.constants.AreaLevelEnum;
import no.evote.exception.EvoteSecurityException;
import no.evote.model.views.ContestRelArea;
import no.evote.security.AccessCache;
import no.evote.security.UserData;
import no.valg.eva.admin.backend.common.auditlog.AuditLogServiceBean;
import no.valg.eva.admin.backend.common.repository.LocaleRepository;
import no.valg.eva.admin.common.auditlog.auditevents.OperatorLoginAuditEvent;
import no.valg.eva.admin.common.rbac.UserDataMockups;
import no.valg.eva.admin.common.rbac.UserMenuMetadata;
import no.valg.eva.admin.configuration.domain.model.County;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.Locale;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.ScanningConfig;
import no.valg.eva.admin.configuration.domain.service.OpptellingskategoriDomainService;
import no.valg.eva.admin.configuration.repository.ContestRelAreaRepository;
import no.valg.eva.admin.configuration.repository.CountyRepository;
import no.valg.eva.admin.configuration.repository.MunicipalityRepository;
import no.valg.eva.admin.rbac.domain.model.Operator;
import no.valg.eva.admin.rbac.domain.model.OperatorRole;
import no.valg.eva.admin.rbac.repository.OperatorRepository;
import no.valg.eva.admin.rbac.repository.OperatorRoleRepository;
import no.valg.eva.admin.test.MockUtilsTestCase;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static java.util.Collections.singletonList;
import static no.evote.constants.AreaLevelEnum.COUNTRY;
import static no.evote.constants.AreaLevelEnum.COUNTY;
import static no.evote.constants.AreaLevelEnum.MUNICIPALITY;
import static no.valg.eva.admin.common.counting.model.CountCategory.VO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class UserDataServiceEjbTest extends MockUtilsTestCase {
    private static final String UID = "1234567891";
    private UserDataServiceEjb userDataServiceEjb;
    private OperatorRepository operatorRepository;
    private OperatorRoleRepository operatorRoleRepository;
    private AccessServiceBean accessServiceBean;
    private LocaleRepository localeRepository;
    private InetAddress inetAddress;
    private AuditLogServiceBean auditLogServiceBean;

    @BeforeMethod
    public void setUp() throws Exception {
        userDataServiceEjb = initializeMocks(UserDataServiceEjb.class);
        operatorRepository = getInjectMock(OperatorRepository.class);
        operatorRoleRepository = getInjectMock(OperatorRoleRepository.class);
        accessServiceBean = getInjectMock(AccessServiceBean.class);
        localeRepository = getInjectMock(LocaleRepository.class);
        auditLogServiceBean = getInjectMock(AuditLogServiceBean.class);
        inetAddress = InetAddress.getLocalHost();
    }

    @Test(dataProvider = "users")
    public void createUserDataAndCheckOperator_withUserInformation_returnsUserData(String uid, String securityLevel, String locale, Integer expectedSecurityLevel, 
                                                                                   String expectedLocale) {
        when(operatorRepository.hasOperator(any())).thenReturn(true);
        when(localeRepository.findById("nb-NO")).thenReturn(getLocale("nb-NO"));
        when(localeRepository.findById("nn-NO")).thenReturn(getLocale("nn-NO"));

        UserData userData = userDataServiceEjb.createUserDataAndCheckOperator(uid, securityLevel, locale, inetAddress);
        
        assertThat(userData.getUid()).isEqualTo(uid);
        assertThat(userData.getSecurityLevel()).isEqualTo(expectedSecurityLevel);
        assertThat(userData.getLocale().getId()).isEqualTo(expectedLocale);
        assertThat(userData.getClientAddress()).isEqualTo(inetAddress);
    }

    @DataProvider
    public Object[][] users() {
        return new Object[][]{
                {"12345678901", "Level3", "nb", 3, "nb-NO"},
                {"12345678901", "Level4", "nn", 4, "nn-NO"},

                {"", "Level3", "nb", 3, "nb-NO"},
                {"foo", "Level3", "nb", 3, "nb-NO"},
                {null, "Level3", "nb", 3, "nb-NO"},
                
                {"12345678901", "Level3", "", 3, "nb-NO"},
                {"12345678901", "Level3", "foo", 3, "nb-NO"},
                {"12345678901", "Level4", null, 4, "nb-NO"},
        };
    }

    private Locale getLocale(String id) {
        Locale locale = new Locale();
        locale.setId(id);
        return locale;
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void createUserDataAndCheckOperator_withoutSecurityLevel_throwsException() {
        userDataServiceEjb.createUserDataAndCheckOperator("12345678901", null, "nb", inetAddress);
    }

    @Test(expectedExceptions = EvoteSecurityException.class)
    public void createUserDataAndCheckOperator_withoutMatchingOperator_auditLogsAndThrowsException() {
        when(operatorRepository.hasOperator(any())).thenReturn(false);
        
        userDataServiceEjb.createUserDataAndCheckOperator("12345678901", "3", "nb", inetAddress);
        
        verify(auditLogServiceBean, times(1)).addToAuditTrail(any(OperatorLoginAuditEvent.class));
    }

    @Test
    public void setAccessCacheOnUserData_withValidSignatureAndValidRole_shouldAddAccessCacheToUserData() {
        UserData userData = new UserData();
        userData.setUid(UID);
        OperatorRole operatorRole = new OperatorRole();
        Operator operator = new Operator();
        operatorRole.setOperator(operator);
        ElectionEvent electionEvent = new ElectionEvent(1L);
        operator.setElectionEvent(electionEvent);
        operator.setId(UID);
        AccessCache accessCache = new AccessCache(new HashSet<>(), null);

        when(operatorRoleRepository.findUnique(any(), any(Operator.class), any(), any())).thenReturn(operatorRole);
        when(accessServiceBean.findAccessCacheFor(userData)).thenReturn(accessCache);

        userDataServiceEjb.setAccessCacheOnUserData(userData, operatorRole);

        assertNotNull(userData.getAccessCache());
    }


    @Test
    public void findUserMenuMetadata_withPollingDistrictLevelUser_returnsCountyCategoriesWithVoOnly() {
        UserData userData = UserDataMockups.userOnPollingDistrictLevel();

        when(getInjectMock(OpptellingskategoriDomainService.class).countCategories(userData.operatorValggeografiSti())).thenReturn(singletonList(VO));

        UserMenuMetadata result = userDataServiceEjb.findUserMenuMetadata(userData);

        assertThat(result.getValidVoteCountCategories()).containsExactly(VO);
    }

    @Test
    public void findUserMenuMetadata_withPollingStationLevelUser_returnsEmptyCountyCategories() throws Exception {
        UserDataServiceEjb ejb = initializeMocks(new UserDataServiceEjb());

        UserData userData = UserDataMockups.userOnPollingStation();
        List<ContestRelArea> contestRelAreaList = new ArrayList<>();
        ContestRelAreaRepository contestRelAreaRepository = mock(ContestRelAreaRepository.class);

        when(contestRelAreaRepository.findAllAllowed(userData.getOperatorRole().getMvElection(), userData.getOperatorRole().getMvArea()))
                .thenReturn(contestRelAreaList);

        UserMenuMetadata result = ejb.findUserMenuMetadata(userData);

        assertEquals(result.getValidVoteCountCategories().size(), 0);
    }

    @Test(dataProvider = "scanningConfigTestData")
    public void findUserMenuMetadata_givenScanningConfiguration_findsIfScanningIsEnabled(boolean isElectionEventAdmin, 
                                                               boolean scanningEnabledInElectionGroup, 
                                                               AreaLevelEnum areaLevel,
                                                               boolean enabledInArea,
                                                               boolean expectedScanningEnabled) throws Exception {
        UserDataServiceEjb userDataServiceEjb = initializeMocks(new UserDataServiceEjb());
        UserData userData = mock(UserData.class, RETURNS_DEEP_STUBS);
        stubEnabledInElectionGroup(scanningEnabledInElectionGroup, userData);
        stubElectionEventAdmin(isElectionEventAdmin, userData);
        stubOperatorArea(areaLevel, enabledInArea, userData);

        UserMenuMetadata actualUserMenuMetadata = userDataServiceEjb.findUserMenuMetadata(userData);

        assertThat(actualUserMenuMetadata.isScanningEnabled()).isEqualTo(expectedScanningEnabled);
    }

    @DataProvider
    private Object[][] scanningConfigTestData() {
        return new Object[][] {
                { false, false, MUNICIPALITY, true, false },
                { false, true, MUNICIPALITY, true, true },
                { true, false, MUNICIPALITY, true, true },
                { true, true, MUNICIPALITY, true, true },
                { false, false, COUNTY, true, false },
                { false, true, COUNTY, true, true },
                { true, false, COUNTY, true, true },
                { true, true, COUNTY, true, true },
                { false, false, COUNTRY, true, false },
                { false, true, COUNTRY, true, false },
                { true, false, COUNTRY, true, true },
                { true, true, COUNTRY, true, true },
                { false, false, MUNICIPALITY, false, false },
                { false, true, MUNICIPALITY, false, false },
                { true, false, MUNICIPALITY, false, true },
                { true, true, MUNICIPALITY, false, true },
                { false, false, COUNTY, false, false },
                { false, true, COUNTY, false, false },
                { true, false, COUNTY, false, true },
                { true, true, COUNTY, false, true },
                { false, false, COUNTRY, false, false },
                { false, true, COUNTRY, false, false },
                { true, false, COUNTRY, false, true },
                { true, true, COUNTRY, false, true },
        };
    }

    private void stubEnabledInElectionGroup(boolean scanningEnabledInElectionGroup, UserData userData) {
        when(userData.getOperatorMvElection().getElectionEvent().isScanningEnabledInElectionGroup()).thenReturn(scanningEnabledInElectionGroup);
    }

    private void stubElectionEventAdmin(boolean isElectionEventAdmin, UserData userData) {
        when(userData.isElectionEventAdminUser()).thenReturn(isElectionEventAdmin);
    }

    private void stubOperatorArea(AreaLevelEnum areaLevel, boolean enabledInArea, UserData userData) {
        if (areaLevel == MUNICIPALITY) {
            Municipality municipality = Municipality.builder().scanningConfig(ScanningConfig.builder().scanning(enabledInArea).build()).build();
            when(getInjectMock(MunicipalityRepository.class).findByPkWithScanningConfig(any())).thenReturn(municipality);
        } else if (areaLevel == COUNTY) {
            County county = County.builder().scanningConfig(ScanningConfig.builder().scanning(enabledInArea).build()).build();
            when(getInjectMock(CountyRepository.class).findByPkWithScanningConfig(any())).thenReturn(county);
            when(userData.getOperatorMvArea().getMunicipality()).thenReturn(null);
        } else {
            when(userData.getOperatorMvArea().getMunicipality()).thenReturn(null);
            when(userData.getOperatorMvArea().getCounty()).thenReturn(null);
        }
    }
    
}
