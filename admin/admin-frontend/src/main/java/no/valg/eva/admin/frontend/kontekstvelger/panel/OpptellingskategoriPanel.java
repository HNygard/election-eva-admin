package no.valg.eva.admin.frontend.kontekstvelger.panel;

import static java.util.Collections.singletonList;
import static no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerElementHjelp.inkluderOpptellingskategori;

import java.util.List;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;

import no.evote.security.UserData;
import no.valg.eva.admin.common.configuration.service.OpptellingskategoriService;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.felles.sti.valghierarki.ValgSti;
import no.valg.eva.admin.felles.sti.valghierarki.ValghierarkiSti;
import no.valg.eva.admin.frontend.kontekstvelger.Kontekst;
import no.valg.eva.admin.frontend.kontekstvelger.KontekstvelgerController;
import no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerOppsett;
import no.valg.eva.admin.frontend.kontekstvelger.opptellingskategori.OpptellingskategoriTabell;

@ViewScoped
public class OpptellingskategoriPanel implements KontekstvelgerPanel<OpptellingskategoriTabell, CountCategory> {
    public static final String NAVN = "@count.ballot.approve.rejected.category";

    private KontekstvelgerController controller;
    private UserData userData;
    private OpptellingskategoriService opptellingskategoriService;
    private ValghierarkiPanel valghierarkiPanel;
    private OpptellingskategoriTabell opptellingskategoriTabell;

    private boolean valgbar;
    private CountCategory valgtCountCategory;

    @SuppressWarnings("unused")
    public OpptellingskategoriPanel() {
        // CDI
    }

    @Inject
    public OpptellingskategoriPanel(KontekstvelgerController controller, UserData userData,
                                    OpptellingskategoriService opptellingskategoriService, ValghierarkiPanel valghierarkiPanel) {
        this.controller = controller;
        this.userData = userData;
        this.opptellingskategoriService = opptellingskategoriService;
        this.valghierarkiPanel = valghierarkiPanel;
    }

    @Override
    public void initOppsett(KontekstvelgerOppsett oppsett) {
        valgbar = inkluderOpptellingskategori(oppsett);
    }

    @Override
    public void initTabeller(Kontekst kontekst) {
        if (countCategoryFromContext(kontekst) == null) {
            opptellingskategoriTabell = new OpptellingskategoriTabell(this, valgSti());
            opptellingskategoriTabell.oppdater();
            valgtCountCategory = opptellingskategoriTabell.valgtCountCategory();
        } else {
            valgtCountCategory = countCategoryFromContext(kontekst);
        }
    }

    private CountCategory countCategoryFromContext(Kontekst kontekst) {
        return kontekst != null ? kontekst.getCountCategory() : null;
    }

    private ValgSti valgSti() {
        ValghierarkiSti valgtValghierarkiSti = valghierarkiPanel.valgtVerdi();
        return valgtValghierarkiSti != null ? valgtValghierarkiSti.tilValgSti() : null;
    }

    @Override
    public CountCategory valgtVerdi() {
        return valgtCountCategory;
    }

    @Override
    public boolean erValgbar() {
        return valgbar;
    }

    public OpptellingskategoriService getOpptellingskategoriService() {
        return opptellingskategoriService;
    }

    public UserData getUserData() {
        return userData;
    }

    @Override
    public String getId() {
        return "opptellingskategoriPanel";
    }

    @Override
    public String getNavn() {
        return NAVN;
    }

    @Override
    public List<OpptellingskategoriTabell> getTabeller() {
        return singletonList(opptellingskategoriTabell);
    }

    @Override
    public void velg(Object tabellId) {
        valgtCountCategory = opptellingskategoriTabell.valgtCountCategory();
        controller.redirectTilUrlEllerInitNestePanel();
    }
}
