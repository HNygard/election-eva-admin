package no.valg.eva.admin.frontend.kontekstvelger;

import no.evote.security.UserData;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.felles.sti.valggeografi.ValggeografiSti;
import no.valg.eva.admin.felles.sti.valghierarki.ValghierarkiSti;
import no.valg.eva.admin.frontend.BaseController;
import no.valg.eva.admin.frontend.i18n.MessageProvider;
import no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerElement;
import no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerOppsett;
import no.valg.eva.admin.frontend.kontekstvelger.panel.KontekstvelgerPanel;
import no.valg.eva.admin.frontend.kontekstvelger.panel.OpptellingskategoriPanel;
import no.valg.eva.admin.frontend.kontekstvelger.panel.ValggeografiPanel;
import no.valg.eva.admin.frontend.kontekstvelger.panel.ValghierarkiPanel;
import org.apache.log4j.Logger;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.util.List;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static no.valg.eva.admin.frontend.kontekstvelger.KontekstvelgerHjelp.kontekstvelgerURL;
import static no.valg.eva.admin.frontend.kontekstvelger.KontekstvelgerHjelp.requestParameterValue;
import static no.valg.eva.admin.frontend.kontekstvelger.KontekstvelgerParam.AUTO;
import static no.valg.eva.admin.frontend.kontekstvelger.KontekstvelgerParam.KONTEKST;
import static no.valg.eva.admin.frontend.kontekstvelger.KontekstvelgerParam.OPPSETT;
import static no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerElement.Parameter.URI;
import static no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerElementHjelp.pickerElementFromType;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Named
@ViewScoped
public class KontekstvelgerController extends BaseController {

    private static final Logger LOGGER = Logger.getLogger(KontekstvelgerController.class);

    private static final long serialVersionUID = -1552513587644887006L;

    private static final String AND_SYMBOL = "&";

    private UserData userData;
    private ValggeografiPanel valggeografiPanel;
    private ValghierarkiPanel valghierarkiPanel;
    private OpptellingskategoriPanel opptellingskategoriPanel;
    private List<KontekstvelgerPanel> paneler;
    private MessageProvider messageProvider;

    private KontekstvelgerOppsett oppsett;
    private Kontekst kontekst;
    private boolean auto;
    private String redirectUrl;
    private String originalQueryString;

    @SuppressWarnings("unused")
    public KontekstvelgerController() {
        // CDI
    }

    @Inject
    public KontekstvelgerController(UserData userData,
                                    ValghierarkiPanel valghierarkiPanel,
                                    ValggeografiPanel valggeografiPanel,
                                    OpptellingskategoriPanel opptellingskategoriPanel,
                                    MessageProvider messageProvider) {
        this.userData = userData;
        this.valghierarkiPanel = valghierarkiPanel;
        this.valggeografiPanel = valggeografiPanel;
        this.opptellingskategoriPanel = opptellingskategoriPanel;
        this.messageProvider = messageProvider;
    }

    @PostConstruct
    protected void doInit() {
        originalQueryString = "";
        if (!isBlank(getQueryString()) && getQueryString().contains(AND_SYMBOL)) {
            originalQueryString = getQueryString().substring(getQueryString().indexOf(AND_SYMBOL));
        }
        
        oppsett = KontekstvelgerOppsett.deserialize(requestParameterValue(this, OPPSETT));
        kontekst = initKontekst();
        auto = true;
        redirectUrl = initRedirectUrl();
        initPaneler();
        if (allValuesSelected()) {
            redirectToURL();
        }
    }

    private Kontekst initKontekst() {
        String kontekstVerdi = requestParameterValue(this, KONTEKST);
        if (kontekstVerdi == null) {
            return null;
        }
        return Kontekst.deserialize(kontekstVerdi);
    }

    private String initRedirectUrl() {
        KontekstvelgerElement sideElement = pickerElementFromType(oppsett, KontekstvelgerElement.Type.SIDE);
        return getPageAccess().getPage(sideElement.get(URI));
    }

    private void initPaneler() {
        paneler = oppsett.typerForKontekst().stream().map(this::panelForType).collect(toList());
        KontekstvelgerPanel forrigePanel = null;
        for (KontekstvelgerPanel panel : paneler) {
            panel.initOppsett(oppsett);
            if (forrigePanel == null || forrigePanel.erVerdiValgt()) {
                panel.initTabeller(kontekst);
            }
            forrigePanel = panel;
        }
    }

    private KontekstvelgerPanel panelForType(KontekstvelgerElement.Type type) {
        switch (type) {
            case HIERARKI:
                return valghierarkiPanel;
            case GEOGRAFI:
                return valggeografiPanel;
            case OPPTELLINGSKATEGORI:
                return opptellingskategoriPanel;
            default:
                throw new IllegalArgumentException(format("finnes ikke panel for typen <%s>", type));
        }
    }

    private boolean allValuesSelected() {
        return paneler.stream().allMatch(KontekstvelgerPanel::erVerdiValgt);
    }

    public KontekstvelgerPanel getCurrentContextPickerPanel() {
        return paneler.stream()
                .filter(KontekstvelgerPanel::erVerdiIkkeValgtMenValgbar)
                .findFirst()
                .orElse(null);
    }

    public UserData getUserData() {
        return userData;
    }

    public void redirectTilUrlEllerInitNestePanel() {
        auto = false;
        if (allValuesSelected()) {
            redirectToURL();
        } else {
            initNextContextPickerPanel();
        }
    }

    private void redirectToURL() {
        Kontekst data = new Kontekst();
        ValghierarkiSti valgtValghierarkiSti = valghierarkiPanel.valgtVerdi();
        if (valgtValghierarkiSti != null) {
            data.setValghierarkiSti(valgtValghierarkiSti);
        }
        CountCategory valgtCountCategory = opptellingskategoriPanel.valgtVerdi();
        if (valgtCountCategory != null) {
            data.setCountCategory(valgtCountCategory);
        }
        ValggeografiSti valgtValggeografiSti = valggeografiPanel.valgtVerdi();
        if (valgtValggeografiSti != null) {
            data.setValggeografiSti(valgtValggeografiSti);
        }
        String serialized = data.serialize();
        if (serialized == null) {
            return;
        }

        String uri = redirectUrl;
        if (uri.contains("?")) {
            uri += "&%s=%s&%s=%s";
        } else {
            uri += "?%s=%s&%s=%s";
        }
        uri = format(uri, KONTEKST, serialized, AUTO, auto);


        uri += originalQueryString;
        
        try {
            getFacesContext().getExternalContext().redirect(uri);
        } catch (IOException e) {
            LOGGER.error(format("kunne ikke redirecte til %s", redirectUrl), e);
        }
    }

    private void initNextContextPickerPanel() {
        KontekstvelgerPanel currentContextPickerPanel = getCurrentContextPickerPanel();
        if (currentContextPickerPanel.erVerdiIkkeValgtMenValgbar()) {
            currentContextPickerPanel.initTabeller(kontekst);
            if (currentContextPickerPanel.erVerdiValgt()) {
                redirectTilUrlEllerInitNestePanel();
            }
        }
    }

    public String getLinkNavn1() {
        return linkNavn(paneler.get(0));
    }

    public String getLinkNavn2() {
        return paneler.size() > 1 ? linkNavn(paneler.get(1)) : "";
    }

    private String linkNavn(KontekstvelgerPanel panel) {
        String panelNavn;
        if (panel == valghierarkiPanel) {
            panelNavn = valghierarkiPanel.getNavn();
        } else if (panel == valggeografiPanel) {
            panelNavn = valggeografiPanel.getNavn();
        } else {
            panelNavn = opptellingskategoriPanel.getNavn();
        }
        return format("%s %s", messageProvider.get("@common.choose"), messageProvider.get(panelNavn).toLowerCase());
    }

    public boolean isVisLink1() {
        return paneler.get(0).erVerdiValgt();
    }

    public boolean isVisLink2() {
        return paneler.size() > 1 && paneler.get(1).erVerdiValgt();
    }

    public void visPanel1() {
        redirectTo(kontekstvelgerURL(oppsett));
    }

    public void visPanel2() {
        if (paneler.size() == 1) {
            return;
        }
        Kontekst newKontekst = new Kontekst();
        KontekstvelgerPanel panel1 = paneler.get(0);
        if (panel1 == valghierarkiPanel) {
            newKontekst.setValghierarkiSti(valghierarkiPanel.valgtVerdi());
        } else if (panel1 == valggeografiPanel) {
            newKontekst.setValggeografiSti(valggeografiPanel.valgtVerdi());
        } else {
            newKontekst.setCountCategory(opptellingskategoriPanel.valgtVerdi());
        }
        redirectTo(kontekstvelgerURL(oppsett, newKontekst));
    }
}
