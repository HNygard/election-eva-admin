package no.valg.eva.admin.frontend.configuration.ctrls.local;

import no.evote.dto.ConfigurationDto;
import no.valg.eva.admin.frontend.util.MessageUtil;
import no.evote.service.configuration.LegacyPollingDistrictService;
import no.valg.eva.admin.common.configuration.service.MunicipalityService;
import no.valg.eva.admin.common.configuration.status.MunicipalityStatusEnum;
import no.valg.eva.admin.felles.sti.valggeografi.KommuneSti;
import no.valg.eva.admin.frontend.common.PageTitleMetaModel;
import no.valg.eva.admin.frontend.kontekstvelger.Kontekst;
import no.valg.eva.admin.frontend.kontekstvelger.KontekstAvhengigController;
import no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerOppsett;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

import static javax.faces.application.FacesMessage.SEVERITY_INFO;
import static no.valg.eva.admin.common.configuration.status.MunicipalityStatusEnum.APPROVED_CONFIGURATION;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.KOMMUNE;
import static no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerElement.geografi;

@Named
@ViewScoped
public class ElectoralRollOverviewController extends KontekstAvhengigController {

    // Injected
    private MunicipalityService municipalityService;
    private LegacyPollingDistrictService pollingDistrictService;

    private List<PageTitleMetaModel> pageTitleMetaModels;
    private List<ConfigurationDto> pollingDistrictsWithoutVoters;
    private List<ConfigurationDto> votersWithoutPollingDistricts;

    @SuppressWarnings("unused")
    public ElectoralRollOverviewController() {
        // CDI
    }

    @Inject
    public ElectoralRollOverviewController(MunicipalityService municipalityService, LegacyPollingDistrictService pollingDistrictService) {
        this.municipalityService = municipalityService;
        this.pollingDistrictService = pollingDistrictService;
    }

    @Override
    public KontekstvelgerOppsett getKontekstVelgerOppsett() {
        KontekstvelgerOppsett setup = new KontekstvelgerOppsett();
        if (getUserData().isElectionEventAdminUser()) {
            setup.leggTil(geografi(KOMMUNE));
        }
        return setup;
    }

    @Override
    public void initialized(Kontekst kontekst) {
        KommuneSti kommuneSti = kontekst.kommuneSti();

        pageTitleMetaModels = getPageTitleMetaBuilder().area(getMvAreaService().findSingleByPath(kommuneSti));
        pollingDistrictsWithoutVoters = pollingDistrictService.getPollingDistrictsMissingVoters(getUserData(), kommuneSti);
        votersWithoutPollingDistricts = municipalityService.findVotersWithoutPollingDistricts(getUserData(), kommuneSti);

        MunicipalityStatusEnum municipalityStatus = municipalityService.getStatus(getUserData(), kommuneSti);
        if (municipalityStatus == APPROVED_CONFIGURATION) {
            MessageUtil.buildDetailMessage("@config.area.is_approved", SEVERITY_INFO);
        }
    }

    public List<ConfigurationDto> getPollingDistrictsWithoutVoters() {
        return pollingDistrictsWithoutVoters;
    }

    public List<ConfigurationDto> getPollingDistrictOnlyInElectoralRoll() {
        return votersWithoutPollingDistricts;
    }

    public List<PageTitleMetaModel> getPageTitleMeta() {
        return pageTitleMetaModels;
    }
}
