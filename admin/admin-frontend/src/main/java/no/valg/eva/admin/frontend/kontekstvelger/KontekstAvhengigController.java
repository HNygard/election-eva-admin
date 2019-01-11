package no.valg.eva.admin.frontend.kontekstvelger;

import lombok.Getter;
import no.evote.security.UserData;
import no.evote.service.configuration.MvAreaService;
import no.evote.service.configuration.MvElectionService;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.PollingDistrictType;
import no.valg.eva.admin.felles.sti.valggeografi.KommuneSti;
import no.valg.eva.admin.felles.sti.valggeografi.ValggeografiSti;
import no.valg.eva.admin.felles.sti.valghierarki.ValghierarkiSti;
import no.valg.eva.admin.felles.valggeografi.model.Kommune;
import no.valg.eva.admin.felles.valggeografi.model.Stemmekrets;
import no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa;
import no.valg.eva.admin.felles.valggeografi.service.ValggeografiService;
import no.valg.eva.admin.felles.valghierarki.model.ValghierarkiNivaa;
import no.valg.eva.admin.frontend.BaseController;
import no.valg.eva.admin.frontend.common.PageTitleMetaBuilder;
import no.valg.eva.admin.frontend.i18n.MessageProvider;
import no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerElement;
import no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerOppsett;
import no.valg.eva.admin.frontend.kontekstvelger.valggeografi.ValggeografiTjeneste;
import no.valg.eva.admin.frontend.user.ctrls.UserAccess;
import no.valg.eva.admin.frontend.user.ctrls.UserDataController;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.List;

import static no.valg.eva.admin.felles.sti.valggeografi.ValggeografiSti.kommuneSti;
import static no.valg.eva.admin.felles.valghierarki.model.ValghierarkiNivaa.VALGDISTRIKT;
import static no.valg.eva.admin.felles.valghierarki.model.ValghierarkiNivaa.VALGGRUPPE;
import static no.valg.eva.admin.frontend.kontekstvelger.KontekstvelgerHjelp.requestParameterValue;
import static no.valg.eva.admin.frontend.kontekstvelger.KontekstvelgerParam.AUTO;
import static no.valg.eva.admin.frontend.kontekstvelger.KontekstvelgerParam.KONTEKST;
import static no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerElement.Type.GEOGRAFI;
import static no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerElement.Type.HIERARKI;
import static no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerElement.Type.SIDE;
import static no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerElementHjelp.electionGeoLevels;
import static no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerElementHjelp.electionGeographyAction;
import static no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerElementHjelp.electionHierarchyLevels;
import static no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerElementHjelp.pickerElementFromType;
import static no.valg.eva.admin.frontend.kontekstvelger.valggeografi.ValggeografiTjeneste.DEFAULT;

/**
 * Superklasse til controllere som krever en kontekst som redirecter til kontekstVelger.xhtml dersom kontekst ikke er riktig.
 */
public abstract class KontekstAvhengigController extends BaseController {

    private static final long serialVersionUID = -7669445129856282848L;

    @Inject
    protected ValggeografiService valggeografiService;
    @Inject
    private UserDataController userDataController;
    @Inject
    private MvElectionService mvElectionService;
    @Inject
    private MvAreaService mvAreaService;
    @Inject
    private PageTitleMetaBuilder pageTitleMetaBuilder;
    @Inject
    private MessageProvider messageProvider;

    private KontekstvelgerOppsett contextPickerSetup;

    private boolean isAutomaticContextPicker;

    @Getter
    private Kontekst context;
    private String kontekstvelgerBrodstiTekst;
    private String pageURL;

    public abstract KontekstvelgerOppsett getKontekstVelgerOppsett();

    @PostConstruct
    public void init() {
        calculatePageURL();
        contextPickerSetup = contextPickerSetupBasedOnOperatorGeo(getKontekstVelgerOppsett());
        calculateContextPickerPathText();
        isAutomaticContextPicker = Boolean.valueOf(requestParameterValue(this, AUTO));
        KontekstvelgerElement pageContextPickerElement = contextPickerSetup == null ? null : pickerElementFromType(contextPickerSetup, SIDE);

        if (contextPickerSetup == null || contextPickerSetup.getElementer().isEmpty()) {
            isAutomaticContextPicker = true;
            buildPickerContextFromUserData();
            calculatePageURL();
            initialized();
            return;
        }

        if (pageContextPickerElement == null) {
            contextPickerSetup.getElementer().add(KontekstvelgerElement.side(getPageAccess().getId(getRequestURI())));
        }

        buildContextFromRequestAndConfig();
        if (doesNotNeedContextPicker()) {
            calculatePageURL();
            initialized();
        } else {
            goToContextPicker();
        }
    }

    private void initialized() {
        initialized(context);
    }

    public abstract void initialized(Kontekst context);

    private KontekstvelgerOppsett contextPickerSetupBasedOnOperatorGeo(KontekstvelgerOppsett contextPickerSetup) {
        if (contextPickerSetup == null) {
            return null;
        }
        // Sørg for at vi ikke konf picker med område bruker allerede er innenfor
        KontekstvelgerElement geoContextPickerElement = pickerElementFromType(contextPickerSetup, GEOGRAFI);
        if (geoContextPickerElement == null) {
            return contextPickerSetup;
        }
        AreaPath operatorAreaPath = userDataController.getUserData().getOperatorAreaPath();
        List<ValggeografiNivaa> electionGeographyLevels = electionGeoLevels(geoContextPickerElement);
        if (electionGeographyLevels.size() == 1 && operatorAreaPathGreaterThanElectionLevelZero(operatorAreaPath, electionGeographyLevels.get(0).nivaa())) {
            ValggeografiTjeneste electionGeographyAction = electionGeographyAction(contextPickerSetup);
            if (electionGeographyAction == DEFAULT) {
                contextPickerSetup.getElementer().remove(geoContextPickerElement);
            }
        }
        return contextPickerSetup;
    }

    private boolean operatorAreaPathGreaterThanElectionLevelZero(AreaPath operatorAreaPath, int electionLevel) {
        return operatorAreaPath.getLevel().getLevel() >= electionLevel;
    }

    private void calculateContextPickerPathText() {
        if (contextPickerSetup == null) {
            return;
        }
        KontekstvelgerElement contextPickerElement = pickerElementFromType(contextPickerSetup, HIERARKI);
        if (contextPickerElement != null) {
            List<ValghierarkiNivaa> nivaaer = electionHierarchyLevels(contextPickerElement);
            if (nivaaer.size() == 1 && nivaaer.get(0) == VALGGRUPPE) {
                contextPickerElement = null;
            }
        }
        if (contextPickerElement != null) {
            List<ValghierarkiNivaa> electionHierarchyLevels = electionHierarchyLevels(contextPickerElement);
            if (electionHierarchyLevels.size() != 1) {
                kontekstvelgerBrodstiTekst = "@election.common.election_level";
            } else {
                kontekstvelgerBrodstiTekst = "@election_level[" + electionHierarchyLevels.get(0).nivaa() + "].name";
            }
        } else {
            contextPickerElement = pickerElementFromType(contextPickerSetup, GEOGRAFI);
            if (contextPickerElement != null) {
                List<ValggeografiNivaa> nivaaer = electionGeoLevels(contextPickerElement);
                if (nivaaer.size() != 1) {
                    kontekstvelgerBrodstiTekst = "@area.common.area_level";
                } else {
                    kontekstvelgerBrodstiTekst = "@area_level[" + nivaaer.get(0).nivaa() + "].name";
                }
            }
        }
    }

    private void buildPickerContextFromUserData() {
        context = new Kontekst();
        context.setValghierarkiSti(ValghierarkiSti.fra(getUserData().getOperatorElectionPath()));
        context.setValggeografiSti(ValggeografiSti.fra(getUserData().getOperatorAreaPath()));
    }

    private boolean doesNotNeedContextPicker() {
        return context != null && contextPickerSetup
                .typerForKontekst()
                .stream()
                .allMatch(context::harVerdiFor);
    }

    public String getKontekstvelgerBrodstiTekst() {
        return kontekstvelgerBrodstiTekst;
    }

    protected List<Kommune> kommunerForValghendelse() {
        return getValggeografiService().kommunerForValghendelse(getUserData());
    }

    protected Kommune kommune(MvArea kommune) {
        return new Kommune(kommuneSti(kommune.areaPath()), kommune.getMunicipalityName(), kommune.getMunicipality().isElectronicMarkoffs());
    }

    protected List<Stemmekrets> stemmekretser(KommuneSti kommuneSti, PollingDistrictType... types) {
        return getValggeografiService().stemmekretser(kommuneSti, types);
    }

    public String getInitiellKontekstvelgerURL() {
        return KontekstvelgerHjelp.kontekstvelgerURL(contextPickerSetup, null);
    }

    protected String getContextPickerURL() {
        return KontekstvelgerHjelp.kontekstvelgerURL(contextPickerSetup, context);
    }

    protected String kontekstvelgerURL(Kontekst kontekst) {
        return KontekstvelgerHjelp.kontekstvelgerURL(contextPickerSetup, kontekst);
    }

    public String getPageURL() {
        return pageURL;
    }

    private void calculatePageURL() {
        pageURL = getRequestURI() + (StringUtils.isEmpty(getQueryString()) ? "" : "?" + getQueryString());
    }

    public boolean isContext() {
        return !isAutomaticContextPicker && context != null;
    }

    protected void goToContextPicker() {
        redirectTo(getContextPickerURL() + addOriginalQueryString());
    }

    private String addOriginalQueryString() {
        return StringUtils.isEmpty(getQueryString()) ? "" : ("&" + getRequest().getQueryString());
    }

    protected UserDataController getUserDataController() {
        return userDataController;
    }

    protected UserData getUserData() {
        return getUserDataController().getUserData();
    }

    protected UserAccess getUserAccess() {
        return userDataController.getUserAccess();
    }

    protected MvElectionService getMvElectionService() {
        return mvElectionService;
    }

    protected MvAreaService getMvAreaService() {
        return mvAreaService;
    }

    protected ValggeografiService getValggeografiService() {
        return valggeografiService;
    }

    protected PageTitleMetaBuilder getPageTitleMetaBuilder() {
        return pageTitleMetaBuilder;
    }

    protected MessageProvider getMessageProvider() {
        return messageProvider;
    }

    private void buildContextFromRequestAndConfig() {
        Kontekst newContext = Kontekst.deserialize(requestParameterValue(this, KONTEKST));
        if (newContext == null) {
            // No data from request. Check user data.
            newContext = new Kontekst();
            newContext = handleOperatorElection(contextPickerSetup, newContext);
            newContext = handleOperatorArea(contextPickerSetup, newContext);
        } else {
            newContext = cleanContextAgainstConfigSequence(newContext);
            if (newContext != null && !isRequestedElectionValid(contextPickerSetup, newContext)) {
                newContext = newContext.getCountCategory() != null ? newContext : null;
            }
            if (newContext != null && !isRequestedAreaValid(contextPickerSetup, newContext)) {
                newContext = newContext.getCountCategory() != null ? newContext : null;
            }
            newContext = checkContextAgainstUserData(newContext, getUserData());
        }
        this.context = newContext;
    }

    private Kontekst handleOperatorElection(KontekstvelgerOppsett setup, Kontekst data) {
        KontekstvelgerElement electionCfg = pickerElementFromType(setup, HIERARKI);

        if (electionCfg == null || data == null) {
            return data;
        }

        ElectionPath electionPath = userDataController.getUserData().getOperatorElectionPath();
        for (ValghierarkiNivaa valghierarkiNivaa : electionHierarchyLevels(electionCfg)) {
            if (electionPath.getLevel() == valghierarkiNivaa.tilElectionLevelEnum()) {
                data.setValghierarkiSti(ValghierarkiSti.fra(electionPath));
                break;
            }
        }
        return data.getValghierarkiSti() == null ? null : data;
    }

    private Kontekst handleOperatorArea(KontekstvelgerOppsett setup, Kontekst data) {
        KontekstvelgerElement areaCfg = pickerElementFromType(setup, GEOGRAFI);
        if (areaCfg == null || data == null) {
            return data;
        }
        AreaPath areaPath = userDataController.getUserData().getOperatorAreaPath();
        for (ValggeografiNivaa valggeografiNivaa : electionGeoLevels(areaCfg)) {
            if (areaPath.getLevel() == valggeografiNivaa.tilAreaLevelEnum()) {
                data.setValggeografiSti(ValggeografiSti.fra(areaPath));
                break;
            }
        }
        return data.getValggeografiSti() == null ? null : data;
    }

    private Kontekst cleanContextAgainstConfigSequence(Kontekst kontekst) {
        return contextPickerSetup.typerForKontekst().stream()
                .reduce(new Kontekst(), (k, t) -> vaskKontekstForType(kontekst, k, t), this::flettKontekster);
    }

    private Kontekst vaskKontekstForType(Kontekst originalKontekst, Kontekst kontekstTilNaa, KontekstvelgerElement.Type type) {
        return kontekstTilNaa != null && originalKontekst.harVerdiFor(type) ? kontekstTilNaa.settVerdi(type, originalKontekst.verdiFor(type)) : kontekstTilNaa;
    }

    private Kontekst flettKontekster(Kontekst k1, Kontekst k2) {
        return k1 == null || k2 == null ? null : new Kontekst(k1, k2);
    }

    private boolean isRequestedElectionValid(KontekstvelgerOppsett setup, Kontekst kontekst) {
        KontekstvelgerElement electionCfg = pickerElementFromType(setup, HIERARKI);
        if (electionCfg == null) {
            kontekst.setValghierarkiSti(ValghierarkiSti.fra(getUserData().getOperatorElectionPath()));
            return true;
        }
        if (kontekst.getValghierarkiSti() == null) {
            return false;
        }
        for (ValghierarkiNivaa valghierarkiNivaa : electionHierarchyLevels(electionCfg)) {
            if (kontekst.getValghierarkiSti().electionPath().getLevel() == valghierarkiNivaa.tilElectionLevelEnum()) {
                return true;
            }
        }
        return false;
    }

    private boolean isRequestedAreaValid(KontekstvelgerOppsett setup, Kontekst kontekst) {
        KontekstvelgerElement areaCfg = pickerElementFromType(setup, GEOGRAFI);
        if (areaCfg == null) {
            kontekst.setValggeografiSti(ValggeografiSti.fra(getUserData().getOperatorAreaPath()));
            return true;
        }
        if (kontekst.getValggeografiSti() == null) {
            return false;
        }
        for (ValggeografiNivaa valggeografiNivaa : electionGeoLevels(areaCfg)) {
            if (kontekst.getValggeografiSti().nivaa() == valggeografiNivaa) {
                return true;
            }
        }
        return false;
    }

    private Kontekst checkContextAgainstUserData(Kontekst kontekst, UserData userData) {
        if (kontekst == null) {
            return null;
        }
        Kontekst newKontekst = sjekkKontekstValghierarkiSti(kontekst, userData);
        if (newKontekst != null) {
            return sjekkKontekstValggeografiSti(newKontekst, userData);
        }
        return null;
    }

    private Kontekst sjekkKontekstValghierarkiSti(Kontekst kontekst, UserData userData) {
        ValghierarkiSti kontekstValghierarkiSti = kontekst.getValghierarkiSti();
        if (kontekstValghierarkiSti != null) {
            ValghierarkiSti operatorValghierarkiSti = userData.operatorValghierarkiSti();
            if (operatorValghierarkiSti.nivaa() == VALGDISTRIKT && !operatorValghierarkiSti.likEllerUnder(kontekstValghierarkiSti)) {
                return null;
            }
            if (operatorValghierarkiSti.nivaa() != VALGDISTRIKT && !kontekstValghierarkiSti.likEllerUnder(operatorValghierarkiSti)) {
                return null;
            }

        }
        return kontekst;
    }

    protected Kontekst sjekkKontekstValggeografiSti(Kontekst kontekst, UserData userData) {
        ValggeografiSti kontekstValggeografiSti = kontekst.getValggeografiSti();
        if (kontekstValggeografiSti != null) {
            ValggeografiSti operatorValggeografiSti = userData.operatorValggeografiSti();
            if (!kontekstValggeografiSti.likEllerUnder(operatorValggeografiSti)) {
                return null;
            }
        }
        return kontekst;
    }
}
