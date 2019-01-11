package no.evote.service.configuration;

import no.evote.security.UserData;
import no.valg.eva.admin.common.configuration.status.CountyStatusEnum;
import no.valg.eva.admin.configuration.domain.model.County;
import no.valg.eva.admin.configuration.domain.model.CountyStatus;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.repository.CountryRepository;
import no.valg.eva.admin.configuration.repository.CountyRepository;
import no.valg.eva.admin.test.MockUtilsTestCase;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CountyServiceBeanTest extends MockUtilsTestCase {

    @Test(dataProvider = "createOpeningHours")
    public void create_withDataProvider_verifyExpected(boolean localConf, int expected) throws Exception {
        CountyServiceBean bean = initializeMocks(CountyServiceBean.class);
        UserData userData = createMock(UserData.class);
        County county = createMock(County.class);
        when(getInjectMock(CountryRepository.class).findByPk(anyLong()).getElectionEvent().isLocalConfiguration()).thenReturn(localConf);

        assertThat(bean.create(userData, county)).isNotNull();
        verify(getInjectMock(CountyRepository.class)).findCountyStatusById(expected);
        verify(county).setCountyStatus(any(CountyStatus.class));
    }

    @DataProvider(name = "createOpeningHours")
    public Object[][] createOpeningHours() {
        return new Object[][]{
                {true, 1},
                {false, 0}
        };
    }

    @Test(dataProvider = "electionEventStatusChanged")
    public void electionEventStatusChanged_withDataProvider_verifyExpected(boolean central, CountyStatusEnum expectedFrom, CountyStatusEnum expectedTo) throws
            Exception {
        CountyServiceBean bean = initializeMocks(CountyServiceBean.class);
        ElectionEvent electionEvent = createMock(ElectionEvent.class);
        when(electionEvent.isCentralConfiguration()).thenReturn(central);
        when(electionEvent.isLocalConfiguration()).thenReturn(!central);

        bean.electionEventStatusChanged(electionEvent);

        verify(getInjectMock(CountyRepository.class)).updateStatusOnCounties(anyLong(), eq(expectedFrom), eq(expectedTo));
    }

    @DataProvider(name = "electionEventStatusChanged")
    public Object[][] electionEventStatusChanged() {
        return new Object[][]{
                {true, CountyStatusEnum.LOCAL_CONFIGURATION, CountyStatusEnum.CENTRAL_CONFIGURATION},
                {false, CountyStatusEnum.CENTRAL_CONFIGURATION, CountyStatusEnum.LOCAL_CONFIGURATION}
        };
    }
}
