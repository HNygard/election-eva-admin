package no.valg.eva.admin.frontend.configuration.ctrls.local;

import no.evote.security.UserData;
import no.evote.service.configuration.MvAreaService;
import no.evote.util.MvAreaBuilder;
import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.configuration.service.MunicipalityService;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.felles.sti.valggeografi.KommuneSti;
import no.valg.eva.admin.felles.sti.valggeografi.ValggeografiSti;
import no.valg.eva.admin.frontend.kontekstvelger.Kontekst;
import no.valg.eva.admin.frontend.security.PageAccess;
import org.testng.annotations.Test;

import static javax.faces.application.FacesMessage.SEVERITY_INFO;
import static no.valg.eva.admin.common.configuration.status.MunicipalityStatusEnum.APPROVED_CONFIGURATION;
import static no.valg.eva.admin.frontend.kontekstvelger.KontekstvelgerParam.KONTEKST;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ElectoralRollOverviewControllerTest extends BaseFrontendTest {

    @Test
    public void init_withNoAreaPath_redirectsToPicker() throws Exception {
        ElectoralRollOverviewController ctrl = ctrl(null);
        when(getUserDataMock().isElectionEventAdminUser()).thenReturn(true);
        getServletContainer().setRequestURI("/my/uri");
        when(getInjectMock(PageAccess.class).getId(anyString())).thenReturn("/my/uri");
        when(getUserDataMock().getOperatorAreaPath()).thenReturn(AREA_PATH_ROOT);

        ctrl.init();

        verify(getFacesContextMock().getExternalContext()).redirect("/secure/kontekstvelger.xhtml?oppsett=[geografi|nivaer|3][side|uri|/my/uri]");
    }

    @Test
    public void init_withCountyAreaPath_redirectsToPicker() throws Exception {
        ElectoralRollOverviewController ctrl = ctrl(mvArea(AREA_PATH_COUNTY));
        when(getUserDataMock().isElectionEventAdminUser()).thenReturn(true);
        getServletContainer().setRequestURI("/my/uri");
        when(getInjectMock(PageAccess.class).getId(anyString())).thenReturn("/my/uri");
        when(getUserDataMock().getOperatorElectionPath()).thenReturn(ELECTION_PATH_ELECTION_GROUP);
        when(getUserDataMock().getOperatorAreaPath()).thenReturn(AREA_PATH_ROOT);

        ctrl.init();

        verify(getFacesContextMock().getExternalContext()).redirect("/secure/kontekstvelger.xhtml?oppsett=[geografi|nivaer|3][side|uri|/my/uri]");
    }

    @Test
    public void init_withMunicipalityAreaPath_verifyState() throws Exception {
        ElectoralRollOverviewController ctrl = ctrl(mvArea(AREA_PATH_MUNICIPALITY));
        when(getInjectMock(MunicipalityService.class).getStatus(any(UserData.class), any(KommuneSti.class))).thenReturn(APPROVED_CONFIGURATION);
        when(getUserDataMock().getOperatorElectionPath()).thenReturn(ELECTION_PATH_ELECTION_GROUP);
        when(getUserDataMock().getOperatorAreaPath()).thenReturn(AREA_PATH_MUNICIPALITY);

        ctrl.init();

        assertThat(ctrl.getPageTitleMeta()).isNotNull();
        assertThat(ctrl.getPollingDistrictOnlyInElectoralRoll()).isNotNull();
        assertThat(ctrl.getPollingDistrictsWithoutVoters()).isNotNull();
        assertFacesMessage(SEVERITY_INFO, "@config.area.is_approved");
    }

    private ElectoralRollOverviewController ctrl(MvArea mvArea) throws Exception {
        ElectoralRollOverviewController ctrl = initializeMocks(ElectoralRollOverviewController.class);
        if (mvArea == null) {
            getServletContainer().setRequestParameter(KONTEKST.toString(), null);
            when(getInjectMock(MvAreaService.class).findSingleByPath(anyString(), any(AreaPath.class))).thenReturn(null);
        } else {
            Kontekst data = new Kontekst();
            data.setValggeografiSti(ValggeografiSti.fra(mvArea.areaPath()));
            getServletContainer().setRequestParameter(KONTEKST.toString(), data.serialize());
            when(getInjectMock(MvAreaService.class).findSingleByPath(anyString(), any(AreaPath.class))).thenReturn(mvArea);

        }
        return ctrl;
    }

    private MvArea mvArea(AreaPath areaPath) {
        return new MvAreaBuilder(areaPath).getValue();
    }

}
