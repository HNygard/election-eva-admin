package no.valg.eva.admin.frontend.kontekstvelger.panel;

import lombok.extern.log4j.Log4j;
import no.evote.exception.EvoteException;
import no.evote.security.UserData;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.felles.sti.valggeografi.ValggeografiSti;
import no.valg.eva.admin.felles.sti.valghierarki.ValghierarkiSti;
import no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa;
import no.valg.eva.admin.felles.valggeografi.service.LagNyttValgkortValggeografiService;
import no.valg.eva.admin.felles.valggeografi.service.ValggeografiService;
import no.valg.eva.admin.felles.valghierarki.model.Valghierarki;
import no.valg.eva.admin.frontend.kontekstvelger.Kontekst;
import no.valg.eva.admin.frontend.kontekstvelger.KontekstvelgerController;
import no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerOppsett;
import no.valg.eva.admin.frontend.kontekstvelger.valggeografi.BydelerTabell;
import no.valg.eva.admin.frontend.kontekstvelger.valggeografi.FylkeskommunerTabell;
import no.valg.eva.admin.frontend.kontekstvelger.valggeografi.KommunerTabell;
import no.valg.eva.admin.frontend.kontekstvelger.valggeografi.LandTabell;
import no.valg.eva.admin.frontend.kontekstvelger.valggeografi.RoderTabell;
import no.valg.eva.admin.frontend.kontekstvelger.valggeografi.StemmekretserTabell;
import no.valg.eva.admin.frontend.kontekstvelger.valggeografi.StemmestederTabell;
import no.valg.eva.admin.frontend.kontekstvelger.valggeografi.ValggeografiFilter;
import no.valg.eva.admin.frontend.kontekstvelger.valggeografi.ValggeografiTabell;
import no.valg.eva.admin.frontend.kontekstvelger.valggeografi.ValggeografiTjeneste;
import no.valg.eva.admin.frontend.kontekstvelger.valggeografi.ValggeografiVariant;
import no.valg.eva.admin.frontend.kontekstvelger.valggeografi.ValghendelseTabell;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static no.valg.eva.admin.common.counting.model.CountCategory.VO;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.BYDEL;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.FYLKESKOMMUNE;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.KOMMUNE;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.LAND;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.RODE;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.STEMMEKRETS;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.STEMMESTED;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.VALGHENDELSE;
import static no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerElementHjelp.electionGeographyAction;
import static no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerElementHjelp.valgbareValggeografiNivaaerFra;
import static no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerElementHjelp.valggeografiFilter;
import static no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerElementHjelp.valggeografiVariant;
import static no.valg.eva.admin.frontend.kontekstvelger.valggeografi.ValggeografiFilter.HAR_VALGDISTRIKT_FOR_VALGT_VALGHIERARKI;
import static no.valg.eva.admin.frontend.kontekstvelger.valggeografi.ValggeografiVariant.ALT_VELG_BYDEL;

@ViewScoped
@Log4j
public class ValggeografiPanel implements KontekstvelgerPanel<ValggeografiTabell, ValggeografiSti> {

    private static final long serialVersionUID = 3186450702107185103L;
    
    private KontekstvelgerController controller;
    private UserData userData;
    private ValggeografiService valggeografiService;
    private ValggeografiService electionGeoService;
    private ValghierarkiPanel valghierarkiPanel;
    private OpptellingskategoriPanel opptellingskategoriPanel;
    private ValggeografiTjeneste valggeografiTjeneste = ValggeografiTjeneste.DEFAULT;
    private List<ValggeografiNivaa> electionGeoLevels;
    private Set<ValggeografiNivaa> selectableElectionGeoLevels;
    private ValggeografiFilter valggeografiFilter = ValggeografiFilter.DEFAULT;
    private ValggeografiVariant valggeografiVariant = ValggeografiVariant.STANDARD;
    private ValggeografiNivaa selectedElectionGeoLevel;

    private ValghendelseTabell valghendelseTabell;
    private LandTabell landTabell;
    private FylkeskommunerTabell fylkeskommunerTabell;
    private KommunerTabell kommunerTabell;
    private BydelerTabell bydelerTabell;
    private StemmekretserTabell stemmekretserTabell;
    private StemmestederTabell stemmestederTabell;
    private RoderTabell roderTabell;

    private boolean valgbar;

    private ValggeografiSti selectedElectionPath;

    public ValggeografiPanel() {
        // CDI
    }

    @Inject
    public ValggeografiPanel(KontekstvelgerController controller,
                             UserData userData,
                             ValggeografiService valggeografiService,
                             @LagNyttValgkortValggeografiService ValggeografiService electionGeoService,
                             ValghierarkiPanel valghierarkiPanel,
                             OpptellingskategoriPanel opptellingskategoriPanel) {
        this.controller = controller;
        this.userData = userData;
        this.valggeografiService = valggeografiService;
        this.electionGeoService = electionGeoService;
        this.valghierarkiPanel = valghierarkiPanel;
        this.opptellingskategoriPanel = opptellingskategoriPanel;
    }

    public static String navn(ValggeografiNivaa valggeografiNivaa) {
        return "@area_level[" + valggeografiNivaa.nivaa() + "].name";
    }

    @Override
    public void initOppsett(KontekstvelgerOppsett oppsett) {
        valggeografiTjeneste = electionGeographyAction(oppsett);
        valggeografiFilter = valggeografiFilter(oppsett);
        valggeografiVariant = valggeografiVariant(oppsett);
        List<ValggeografiNivaa> valgbareValggeografiNivaaerFraOppsett = valgbareValggeografiNivaaerFra(oppsett);
        valgbar = valgbareValggeografiNivaaerFraOppsett != null;
        if (!valgbar) {
            return;
        }
        this.selectableElectionGeoLevels = EnumSet.copyOf(valgbareValggeografiNivaaerFraOppsett);
    }

    @Override
    public void initTabeller(Kontekst kontekst) {
        ValggeografiSti valggeografiSti = valggeografiSti(kontekst);
        if (valggeografiSti == null) {

            if (valggeografiVariant == ALT_VELG_BYDEL) {
                selectedElectionGeoLevel = valghierarkiPanel.valgtValghierarki().valggeografiNivaa();
            }

            electionGeoLevelsForNextPanel();

            initTabeller();
            selectedElectionPath = sisteValgteSti();
        } else {
            selectedElectionPath = valggeografiSti;
        }
    }

    private void electionGeoLevelsForNextPanel() {
        this.electionGeoLevels = electionGeoLevelsForOutput();
    }

    private ValggeografiSti valggeografiSti(Kontekst kontekst) {
        return kontekst != null ? kontekst.getValggeografiSti() : null;
    }

    private List<ValggeografiNivaa> electionGeoLevelsForOutput() {
        Set<ValggeografiNivaa> currentElectionGeoLevels = selectableElectionGeoLevels();

        ValggeografiNivaa lastElectionGeoLevel = lastElectionGeoLevelsInSet(currentElectionGeoLevels);

        if (lastElectionGeoLevel == null) {
            throw new EvoteException("ValggeografiNivaa er null for " + currentElectionGeoLevels);
        }

        return ValggeografiNivaa.listIncluding(lastElectionGeoLevel);
    }

    private ValggeografiNivaa lastElectionGeoLevelsInSet(Set<ValggeografiNivaa> currentElectionGeoLevels) {
        return currentElectionGeoLevels.stream()
                .skip(currentElectionGeoLevels.size() - 1L)
                .findFirst()
                .orElse(null);
    }

    private Set<ValggeografiNivaa> selectableElectionGeoLevels() {
        CountCategory countCategory = opptellingskategoriPanel.valgtVerdi();

        if (valggeografiVariant != ALT_VELG_BYDEL) {
            return selectableElectionGeoLevels;
        }
        return selectableElectionGeoLevels.stream()
                .filter(electionLevelFilter(countCategory))
                .collect(Collectors.toSet());
    }

    private Predicate<ValggeografiNivaa> electionLevelFilter(CountCategory countCategory) {
        if (selectedElectionGeoLevel != BYDEL || countCategory == VO) {
            return electionGeoLevel -> electionGeoLevel == STEMMEKRETS;
        }
        return electionGeoLevel -> electionGeoLevel == BYDEL;
    }

    private void initTabeller() {
        ValghierarkiSti valghierarkiSti = valghierarkiPanel.valgtVerdi();
        CountCategory countCategory = opptellingskategoriPanel.valgtVerdi();

        ValggeografiNivaa valggeografiNivaaForValgtValghierarkiSti = valggeografiNivaaForValgtValghierarki();
        initElectionDayTable();
        initCountryTable();
        initCountyTable(valghierarkiSti, countCategory, valggeografiNivaaForValgtValghierarkiSti);
        initMunicipalityTable(valghierarkiSti, countCategory, valggeografiNivaaForValgtValghierarkiSti);
        initBoroughTable(valghierarkiSti, countCategory, valggeografiNivaaForValgtValghierarkiSti);
        initPollingDistrictTable(valghierarkiSti, countCategory);
        initPollingPlaceTable();
        initPollingStationTable();

        getTabeller().get(0).oppdater();
    }

    private void initPollingStationTable() {
        if (electionGeoLevels.contains(RODE) && stemmestederTabell != null) {
            roderTabell = new RoderTabell(this);
            roderTabell.setFilter(valggeografiFilter);
        }
    }

    private void initPollingPlaceTable() {
        if (electionGeoLevels.contains(STEMMESTED) && stemmekretserTabell != null) {
            stemmestederTabell = new StemmestederTabell(this);
            stemmestederTabell.setFilter(valggeografiFilter);
        }
    }

    private void initPollingDistrictTable(ValghierarkiSti valghierarkiSti, CountCategory countCategory) {
        if (electionGeoLevels.contains(STEMMEKRETS) && bydelerTabell != null) {
            stemmekretserTabell = new StemmekretserTabell(this, valghierarkiSti, countCategory);
            stemmekretserTabell.setFilter(valggeografiFilter);
        }
    }

    private void initBoroughTable(ValghierarkiSti valghierarkiSti, CountCategory countCategory, ValggeografiNivaa valggeografiNivaaForValgtValghierarkiSti) {
        if (electionGeoLevels.contains(BYDEL) && (erLavereEllerLik(valggeografiNivaaForValgtValghierarkiSti, BYDEL))) {
            bydelerTabell = new BydelerTabell(this, valghierarkiSti, countCategory);
            bydelerTabell.setFilter(valggeografiFilter);
        }
    }

    private void initMunicipalityTable(ValghierarkiSti valghierarkiSti, CountCategory countCategory, ValggeografiNivaa valggeografiNivaaForValgtValghierarkiSti) {
        if (electionGeoLevels.contains(KOMMUNE) && (erLavereEllerLik(valggeografiNivaaForValgtValghierarkiSti, KOMMUNE))) {
            kommunerTabell = new KommunerTabell(this, valghierarkiSti, countCategory);
            kommunerTabell.setFilter(valggeografiFilter);
        }
    }

    private void initCountyTable(ValghierarkiSti valghierarkiSti, CountCategory countCategory, ValggeografiNivaa valggeografiNivaaForValgtValghierarkiSti) {
        if (electionGeoLevels.contains(FYLKESKOMMUNE) && erLavereEllerLik(valggeografiNivaaForValgtValghierarkiSti, FYLKESKOMMUNE)) {
            fylkeskommunerTabell = new FylkeskommunerTabell(this, valghierarkiSti, countCategory);
            fylkeskommunerTabell.setFilter(valggeografiFilter);
        }
    }

    private void initCountryTable() {
        if (electionGeoLevels.contains(LAND)) {
            landTabell = new LandTabell(this);
        }
    }

    private void initElectionDayTable() {
        if (electionGeoLevels.contains(VALGHENDELSE)) {
            valghendelseTabell = new ValghendelseTabell(this);
        }
    }

    private ValggeografiNivaa valggeografiNivaaForValgtValghierarki() {
        Valghierarki valgtValghierarki = valghierarkiPanel.valgtValghierarki();
        if (valgtValghierarki != null && valggeografiFilter == HAR_VALGDISTRIKT_FOR_VALGT_VALGHIERARKI) {
            return valgtValghierarki.valggeografiNivaa();
        }
        return null;
    }

    private ValggeografiSti sisteValgteSti() {
        List<ValggeografiTabell> tabeller = getTabeller();
        return tabeller.get(tabeller.size() - 1).valgtSti();
    }

    @Override
    public ValggeografiSti valgtVerdi() {
        return selectedElectionPath;
    }

    @Override
    public boolean erValgbar() {
        return valgbar;
    }

    public ValggeografiService getValggeografiService() {
        if (valggeografiTjeneste == ValggeografiTjeneste.LAG_NYTT_VALGKORT) {
            return electionGeoService;
        }
        return valggeografiService;
    }

    public UserData getUserData() {
        return userData;
    }

    private boolean erLavereEllerLik(ValggeografiNivaa valggeografiNivaaForValgtValghierarkiSti, ValggeografiNivaa fylkeskommune) {
        return valggeografiNivaaForValgtValghierarkiSti == null || valggeografiNivaaForValgtValghierarkiSti.isLowerOrBelow(fylkeskommune);
    }

    @Override
    public String getId() {
        return "valggeografiPanel";
    }

    @Override
    public String getNavn() {
        Set<ValggeografiNivaa> valgbareValggeografiNivaaerSet = selectableElectionGeoLevels();
        if (valgbareValggeografiNivaaerSet.size() != 1) {
            return "@area.common.area_level";
        }

        ValggeografiNivaa valgbartNivaa = valgbareValggeografiNivaaerSet.stream().findFirst().orElse(null);
        return navn(valgbartNivaa);
    }

    @Override
    public List<ValggeografiTabell> getTabeller() {
        return electionGeoLevels.stream()
                .map(this::valggeografiTabell)
                .filter(Objects::nonNull)
                .collect(toList());
    }

    @Override
    public void velg(Object tabellId) {
        selectedElectionPath = valggeografiTabell((ValggeografiNivaa) tabellId).valgtSti();
        controller.redirectTilUrlEllerInitNestePanel();
    }

    private ValggeografiTabell valggeografiTabell(ValggeografiNivaa valggeografiNivaa) {
        switch (valggeografiNivaa) {
            case VALGHENDELSE:
                return valghendelseTabell;
            case LAND:
                return landTabell;
            case FYLKESKOMMUNE:
                return fylkeskommunerTabell;
            case KOMMUNE:
                return kommunerTabell;
            case BYDEL:
                return bydelerTabell;
            case STEMMEKRETS:
                return stemmekretserTabell;
            case STEMMESTED:
                return stemmestederTabell;
            case RODE:
                return roderTabell;
            default:
                throw new IllegalArgumentException(format("Ukjent nivå: %s", valggeografiNivaa));
        }
    }

    public FylkeskommunerTabell getFylkeskommunerTabell() {
        return fylkeskommunerTabell;
    }

    public KommunerTabell getKommunerTabell() {
        return kommunerTabell;
    }

    public BydelerTabell getBydelerTabell() {
        return bydelerTabell;
    }

    public StemmekretserTabell getStemmekretserTabell() {
        return stemmekretserTabell;
    }

    public StemmestederTabell getStemmestederTabell() {
        return stemmestederTabell;
    }

    public boolean visKnapp(ValggeografiNivaa valggeografiNivaa) {
        return selectableElectionGeoLevels().contains(valggeografiNivaa);
    }

    public void oppdaterLandTabell() {
        oppdaterTabell(landTabell);
    }

    public void oppdaterFylkeskommunerTabell() {
        oppdaterTabell(fylkeskommunerTabell);
    }

    public void oppdaterKommunerTabell() {
        oppdaterTabell(kommunerTabell);
    }

    public void oppdaterBydelerTabell() {
        oppdaterTabell(bydelerTabell);
    }

    public void oppdaterStemmekretserTabell() {
        oppdaterTabell(stemmekretserTabell);
    }

    public void oppdaterStemmestederTabell() {
        oppdaterTabell(stemmestederTabell);
    }

    public void oppdaterRoderTabell() {
        oppdaterTabell(roderTabell);
    }

    private void oppdaterTabell(ValggeografiTabell tabell) {
        Optional.ofNullable(tabell).ifPresent(ValggeografiTabell::oppdater);
    }
}
