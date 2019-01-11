package no.valg.eva.admin.frontend.stemmegivning.ctrls;

import no.evote.service.configuration.VoterService;
import no.evote.service.producer.EjbProxy;
import no.evote.service.voting.VotingService;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.configuration.model.Manntallsnummer;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.configuration.domain.model.Voter;
import no.valg.eva.admin.felles.sti.valggeografi.KommuneSti;
import no.valg.eva.admin.felles.sti.valggeografi.ValggeografiSti;
import no.valg.eva.admin.felles.sti.valghierarki.ValggruppeSti;
import no.valg.eva.admin.felles.sti.valghierarki.ValghierarkiSti;
import no.valg.eva.admin.felles.valggeografi.model.Kommune;
import no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa;
import no.valg.eva.admin.frontend.common.PageTitleMetaModel;
import no.valg.eva.admin.frontend.kontekstvelger.Kontekst;
import no.valg.eva.admin.frontend.kontekstvelger.KontekstAvhengigController;
import no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerOppsett;
import no.valg.eva.admin.frontend.manntall.widgets.ManntallsSokListener;
import no.valg.eva.admin.frontend.manntall.widgets.ManntallsSokWidget;

import javax.inject.Inject;
import java.util.List;

import static no.valg.eva.admin.felles.valghierarki.model.ValghierarkiNivaa.VALGGRUPPE;
import static no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerElement.geografi;
import static no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerElement.hierarki;

/**
 * Superklasse for alle stemmegivning controllere med felles funksjonalitet.
 */
public abstract class StemmegivningController extends KontekstAvhengigController implements ManntallsSokListener {

    @Inject
    protected ManntallsSokWidget manntallsSokWidget;

    @Inject
    @EjbProxy
    protected VotingService votingService;
    @Inject
    protected VoterService voterService;
    protected List<Kommune> kommuneListe;
    private MvElection valgGruppe;
    private MvArea stemmested;
    private Voter velger;
    private boolean ingenVelgerFunnet;

    /**
     * Hvilken område kontekst skal det registreres stemmer på?
     */
    public abstract ValggeografiNivaa getStemmestedNiva();

    /**
     * Kalles når riktig kontekst er klar (gitt konfigurasjon av KontekstAvhengig).
     */
    public abstract void kontekstKlar();

    /**
     * Kalles fra KontekstAvhengig for å avgjøre kontekst det skal jobbes i.
     */
    @Override
    public KontekstvelgerOppsett getKontekstVelgerOppsett() {
        KontekstvelgerOppsett setup = new KontekstvelgerOppsett();
        setup.leggTil(hierarki(VALGGRUPPE));
        setup.leggTil(geografi(getStemmestedNiva()));
        return setup;
    }

    /**
     * Kalles fra KontekstAvhengig når kontext er kjent.
     */
    @Override
    public void initialized(Kontekst kontekst) {
        valgGruppe = getMvElectionService().findSingleByPath(kontekst.valggruppeSti());
        AreaPath riktigNiva = kontekst.getValggeografiSti().areaPath().toAreaLevelPath(getStemmestedNiva().tilAreaLevelEnum());
        ValggeografiSti valggeografiSti = ValggeografiSti.fra(riktigNiva);
        setStemmested(getMvAreaService().findSingleByPath(valggeografiSti));
        kontekstKlar();
        manntallsSokWidget.addListener(this);
    }

    /**
     * Kalles fra ManntallsSokController ved oppsett av søk med kommune id.
     */
    @Override
    public KommuneSti getKommuneSti() {
        return getStemmested() == null ? null : ValggeografiSti.kommuneSti(getStemmested().areaPath());
    }

    /**
     * Kalles fra ManntallsSokController ved oppsett av søk med løpenummer.
     */
    @Override
    public ValggruppeSti getValggruppeSti() {
        return getValgGruppe() == null ? null : ValghierarkiSti.valggruppeSti(getValgGruppe().electionPath());
    }

    /**
     * Kalles fra ManntallsSokController rett før et søk.
     */
    @Override
    public void manntallsSokInit() {
        velger = null;
        ingenVelgerFunnet = false;
    }

    /**
     * Kalles fra ManntallsSokController etter et søk og man finner et enkelt treff.
     */
    @Override
    public void manntallsSokVelger(Voter velger) {
        this.velger = velger;
        ingenVelgerFunnet = false;
    }

    /**
     * Kalles fra ManntallsSokController etter et søk medFilter tomt resultat.
     */
    @Override
    public void manntallsSokTomtResultat() {
        velger = null;
        ingenVelgerFunnet = true;
    }

    /**
     * Liste med alle tilgjengelige kommuner.
     */
    public List<Kommune> getKommuneListe() {
        if (kommuneListe == null) {
            kommuneListe = kommunerForValghendelse();
        }
        return kommuneListe;
    }

    public void setKommuneListe(List<Kommune> kommuneListe) {
        this.kommuneListe = kommuneListe;
    }

    /**
     * Er velger registrert i samme kommune som stemmen skal avgis?
     */
    public boolean isVelgerEgenKommune() {
        return isHarVelger() && getVelger().getMunicipalityId().equals(getStemmested().getMunicipalityId());
    }

    /**
     * Valgte velger eller null.
     */
    public Voter getVelger() {
        return velger;
    }

    public void setVelger(Voter velger) {
        this.velger = velger;
    }

    /**
     * Returnerer formatert manntallsnummer på velger eller null dersom ingen velger.
     */
    public String getManntallsnummer() {
        Manntallsnummer manntallsnummer = manntallsSokWidget.getManntallsnummerObject();
        if (manntallsnummer == null) {
            return null;
        }
        return manntallsnummer.getKortManntallsnummerMedZeroPadding() + " " + manntallsnummer.getSluttsifre();
    }

    /**
     * Returnerer om èn velger ble funnet i manntall eller ikke.
     */
    public boolean isIngenVelgerFunnet() {
        return ingenVelgerFunnet;
    }

    /**
     * Er det søkt frem en velger som kan stemme?
     */
    public boolean isHarVelger() {
        return getVelger() != null;
    }

    /**
     * Side tittel info.
     */
    public List<PageTitleMetaModel> getPageTitleMeta() {
        return getPageTitleMetaBuilder().area(getStemmested());
    }

    /**
     * Er stemmestedet som er valgt et "Forhånd rett i urne"-stemmested?
     */
    public boolean isForhandsstemmeRettIUrne() {
        return getStemmested() != null && getStemmested().getPollingPlace().isAdvanceVoteInBallotBox();
    }

    public MvElection getValgGruppe() {
        return valgGruppe;
    }

    public MvArea getStemmested() {
        return stemmested;
    }

    public void setStemmested(MvArea stemmested) {
        this.stemmested = stemmested;
    }

    public void resetVoteRegistration() {
        manntallsSokWidget.reset();
        velger = null;
    }
}
