package no.valg.eva.admin.frontend.opptelling;

import lombok.Getter;
import lombok.Setter;
import no.evote.exception.EvoteException;
import no.valg.eva.admin.frontend.util.FacesUtil;
import no.valg.eva.admin.frontend.util.MessageUtil;
import no.evote.security.UserData;
import no.evote.service.SpecialPurposeReportService;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.felles.sti.valggeografi.StemmekretsSti;
import no.valg.eva.admin.felles.sti.valggeografi.ValggeografiSti;
import no.valg.eva.admin.felles.sti.valghierarki.ValgSti;
import no.valg.eva.admin.felles.valggeografi.model.Bydel;
import no.valg.eva.admin.felles.valggeografi.model.Stemmekrets;
import no.valg.eva.admin.felles.valghierarki.model.Valg;
import no.valg.eva.admin.felles.valghierarki.service.ValghierarkiService;
import no.valg.eva.admin.frontend.common.PageTitleMetaBuilder;
import no.valg.eva.admin.frontend.common.PageTitleMetaModel;
import no.valg.eva.admin.frontend.i18n.MessageProvider;
import no.valg.eva.admin.frontend.kontekstvelger.Kontekst;
import no.valg.eva.admin.frontend.kontekstvelger.KontekstAvhengigController;
import no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerOppsett;
import no.valg.eva.admin.frontend.kontekstvelger.panel.OpptellingskategoriPanel;
import no.valg.eva.admin.frontend.kontekstvelger.panel.ValggeografiPanel;
import no.valg.eva.admin.frontend.kontekstvelger.panel.ValghierarkiPanel;
import no.valg.eva.admin.frontend.kontekstvelger.valggeografi.ValggeografiVariant;
import org.apache.log4j.Logger;

import javax.faces.application.FacesMessage;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.util.List;

import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.BYDEL;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.STEMMEKRETS;
import static no.valg.eva.admin.felles.valghierarki.model.ValghierarkiNivaa.VALG;
import static no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerElement.geografi;
import static no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerElement.hierarki;
import static no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerElement.opptellingskategori;

@Named
@ViewScoped
public class GenerateBarcodeStickersController extends KontekstAvhengigController {
    private static final Logger LOGGER = Logger.getLogger(GenerateBarcodeStickersController.class);

    @Inject
    private ValghierarkiService valghierarkiService;
    @Inject
    private SpecialPurposeReportService specialPurposeReportService;
    @Inject
    private UserData userData;
    @Inject
    private PageTitleMetaBuilder pageTitleMetaBuilder;
    @Inject
    private MessageProvider messageProvider;

    private Valg selectedElection;
    private Stemmekrets selectedPollingDistrict;
    private CountCategory selectedCountCategory;

    private AreaPath selectedAreaPath;
    private Bydel valgtBydel;

    @Getter
    @Setter
    private int numberOfStickers = 1;

    @Override
    public KontekstvelgerOppsett getKontekstVelgerOppsett() {
        KontekstvelgerOppsett contextPickerSetup = new KontekstvelgerOppsett();

        //Disse linjene under legger til de gitte elementene i kontekstvelgeren i den gitte rekkefølgen, og vil lage en kontekstvelger som lager nye kontekstvelger-
        //elementer ved siden av hverandre ettehvert som brukeren navigerer seg innover i kontekstvelgeren
        contextPickerSetup.leggTil(hierarki(VALG));
        contextPickerSetup.leggTil(opptellingskategori());
        contextPickerSetup.leggTil(geografi(BYDEL, STEMMEKRETS).medVariant(ValggeografiVariant.ALT_VELG_BYDEL));
        return contextPickerSetup;
    }

    @Override
    public void initialized(Kontekst kontekst) {
        ValgSti valgtValgSti = kontekst.getValghierarkiSti().tilValgSti();
        selectedElection = valghierarkiService.valg(valgtValgSti);

        ValggeografiSti valggeografiSti = kontekst.getValggeografiSti();

        selectedAreaPath = valggeografiSti.areaPath();

        //Dersom valggeografinivå er bydel hentes area path osv fra bydel istedet fra på stemmekrets, da stemmekrets ikke nødvendigvis finnes
        //for bydel for visse typer stemmer
        if (BYDEL == valggeografiSti.nivaa()) {
            valgtBydel = valggeografiService.bydel(valggeografiSti.tilBydelSti());
        } else {
            StemmekretsSti valgtStemmekretsSti = valggeografiSti.tilStemmekretsSti();
            selectedPollingDistrict = valggeografiService.stemmekrets(valgtStemmekretsSti);
        }

        selectedCountCategory = kontekst.getCountCategory();
    }

    public void generateSticker() {
        ElectionPath selectedElectionPath = selectedElection.sti().electionPath();
        try {
            byte[] bytes = specialPurposeReportService
                    .generateScanningBoxLabel(userData, selectedElectionPath, selectedCountCategory, selectedAreaPath, numberOfStickers);
            FacesUtil.sendFile("barcode.pdf", bytes);
        } catch (IOException | EvoteException e) {
            LOGGER.error(e.getMessage(), e);
            MessageUtil.buildDetailMessage(FacesMessage.SEVERITY_ERROR, e.getMessage());
        }
    }

    public String getSelectElectionLinkName() {
        return messageProvider.get("@common.choose") + " " + messageProvider.get(ValghierarkiPanel.navn(VALG)).toLowerCase();
    }

    public String getSelectCountCategoryLinkName() {
        return messageProvider.get("@common.choose") + " " + messageProvider.get(OpptellingskategoriPanel.NAVN).toLowerCase();
    }

    public String getSelectPollingDistrictLinkName() {
        return messageProvider.get("@common.choose") + " " + messageProvider.get(ValggeografiPanel.navn(STEMMEKRETS)).toLowerCase();
    }

    public List<PageTitleMetaModel> getElectionPageTitleMeta() {
        List<PageTitleMetaModel> result = pageTitleMetaBuilder.fra(selectedElection);
        result.addAll(pageTitleMetaBuilder.countCategory(selectedCountCategory));
        return result;
    }

    public List<PageTitleMetaModel> getAreaPageTitleMeta() {
        if (valgtBydel != null) {
            return pageTitleMetaBuilder.fra(valgtBydel);
        } else {
            return pageTitleMetaBuilder.fra(selectedPollingDistrict);
        }
    }

    public void redirectToSelectElection() {
        redirectTo(getContextPickerURL());
    }

    public void redirectToSelectCountCategory() {
        Kontekst kontekst = new Kontekst();
        kontekst.setValghierarkiSti(selectedElection.sti());
        redirectTo(kontekstvelgerURL(kontekst));
    }

    public void redirectToSelectPollingDistrict() {
        Kontekst kontekst = new Kontekst();
        kontekst.setValghierarkiSti(selectedElection.sti());
        kontekst.setCountCategory(selectedCountCategory);
        redirectTo(kontekstvelgerURL(kontekst));
    }
}
