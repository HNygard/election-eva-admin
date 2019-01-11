package no.valg.eva.admin.frontend.stemmegivning.ctrls.registrering;

import no.evote.service.configuration.VoterService;
import no.evote.service.voting.VotingService;
import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.configuration.domain.model.Voter;
import no.valg.eva.admin.configuration.domain.model.VotingCategory;
import no.valg.eva.admin.felles.melding.Melding;
import no.valg.eva.admin.felles.sti.valggeografi.ValggeografiSti;
import no.valg.eva.admin.felles.sti.valghierarki.ValghierarkiSti;
import no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa;
import no.valg.eva.admin.frontend.kontekstvelger.Kontekst;
import no.valg.eva.admin.frontend.manntall.models.ManntallsSokType;
import no.valg.eva.admin.frontend.manntall.widgets.ManntallsSokWidget;
import no.valg.eva.admin.frontend.stemmegivning.ctrls.StemmegivningController;
import no.valg.eva.admin.voting.domain.model.StemmegivningsType;
import no.valg.eva.admin.voting.domain.model.VelgerMelding;
import no.valg.eva.admin.voting.domain.model.VelgerSomSkalStemme;
import no.valg.eva.admin.voting.domain.model.Voting;
import org.joda.time.DateTime;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.singletonList;
import static no.valg.eva.admin.common.counting.model.CountCategory.VO;
import static no.valg.eva.admin.common.voting.VotingCategory.VS;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.STEMMESTED;
import static no.valg.eva.admin.voting.domain.model.StemmegivningsType.FORHANDSSTEMME_ORDINAER;
import static no.valg.eva.admin.voting.domain.model.StemmegivningsType.VALGTINGSTEMME_ORDINAER;
import static no.valg.eva.admin.voting.domain.model.VelgerMeldingType.INGEN_VALGKRETS_FOR_VELGER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RegistreringControllerTest extends BaseFrontendTest {

    private static final ManntallsTomtResultatMelding FODSELSNUMMER = new ManntallsTomtResultatMelding(ManntallsSokType.FODSELSNUMMER);
    private static final ManntallsTomtResultatMelding MANNTALLSNUMMER = new ManntallsTomtResultatMelding(ManntallsSokType.MANNTALLSNUMMER);
    private static final ManntallsTomtResultatMelding AVANSERT_FORHAND = new ManntallsTomtResultatMelding(ManntallsSokType.AVANSERT, FORHANDSSTEMME_ORDINAER,
            false);
    private static final ManntallsTomtResultatMelding AVANSERT_FORHAND_RETTT_I_URNE = new ManntallsTomtResultatMelding(ManntallsSokType.AVANSERT,
            FORHANDSSTEMME_ORDINAER, true);
    private static final ManntallsTomtResultatMelding AVANSERT_VALGTING_ORDINAER = new ManntallsTomtResultatMelding(ManntallsSokType.AVANSERT,
            VALGTINGSTEMME_ORDINAER, false);

    @Test
    public void manntallsSokInit_medTidligereSokeTilstand_nullstillerTilstand() throws Exception {
        RegistreringController ctrl = ctrl();
        mockField("velger", Voter.class);
        mockFieldValue("ingenVelgerFunnet", true);
        ctrl.setStemmegivning(createMock(Voting.class));
        ctrl.setStemmetype(VO.getId());

        ctrl.manntallsSokInit();

        assertThat(ctrl.getVelger()).isNull();
        assertThat(ctrl.isIngenVelgerFunnet()).isFalse();
        assertThat(ctrl.getStemmegivning()).isNull();
        assertThat(ctrl.getStemmetype()).isNull();
    }

    @Test
    public void manntallsSokVelger_medNull_resetterMeldinger() throws Exception {
        RegistreringController ctrl = ctrl();
        ctrl.getStatiskeMeldinger().add(createMock(Melding.class));

        ctrl.manntallsSokVelger(null);

        assertThat(ctrl.getVelger()).isNull();
        assertThat(ctrl.isIngenVelgerFunnet()).isFalse();
        assertThat(ctrl.getStatiskeMeldinger()).isEmpty();
    }

    @Test
    public void manntallsSokVelger_medVelger_henterStemmegivningsForberedelser() throws Exception {
        RegistreringController ctrl = ctrl();
        ctrl.initialized(kontekst());
        stub_hentStemmegivningsForberedelser(true, singletonList(new VelgerMelding(INGEN_VALGKRETS_FOR_VELGER)));

        ctrl.manntallsSokVelger(createMock(Voter.class));

        assertThat(ctrl.getVelger()).isNotNull();
        assertThat(ctrl.isKanRegistrereStemmegivning()).isTrue();
        assertThat(ctrl.getStatiskeMeldinger()).hasSize(1);
    }

    @Test
    public void manntallsSokTomtResultat_resetterVelgerData() throws Exception {
        RegistreringController ctrl = ctrl();
        medVelger(ctrl, createMock(Voter.class));

        ctrl.manntallsSokTomtResultat();

        assertThat(ctrl.getVelger()).isNull();
        assertThat(ctrl.isIngenVelgerFunnet()).isTrue();
    }

    @Test
    public void opprettFiktivVelger_velgerOpprettes() throws Exception {
        RegistreringController ctrl = ctrl();
        medVelger(ctrl, createMock(Voter.class));

        ctrl.opprettFiktivVelger();

        verify(getInjectMock(VoterService.class)).createFictitiousVoter(eq(getUserDataMock()), any(AreaPath.class));
        assertThat(ctrl.getVelger()).isNotNull();
        assertThat(ctrl.isIngenVelgerFunnet()).isFalse();
        assertThat(ctrl.isKanRegistrereStemmegivning()).isTrue();
        assertThat(ctrl.getStatiskeMeldinger()).hasSize(1);
    }

    @Test
    public void hentStemmegivningsForberedelser_medVelger_verifiserMeldinger() throws Exception {
        RegistreringController ctrl = ctrl();
        ctrl.initialized(kontekst());
        stub_hentStemmegivningsForberedelser(true, singletonList(new VelgerMelding(INGEN_VALGKRETS_FOR_VELGER)));
        ctrl.manntallsSokVelger(createMock(Voter.class));

        ctrl.hentVelgerSomSkalStemme();

        assertThat(ctrl.getStatiskeMeldinger()).hasSize(1);
        assertThat(ctrl.getStatiskeMeldinger().get(0).getText()).isEqualTo("@voting.search.noContestsForVoter");
    }

    @Test(dataProvider = "manntallsTomtResultatMelding")
    public void manntallsTomtResultatMelding_medDataProvider_verifiserForventet(ManntallsTomtResultatMelding melding, String forventet) throws Exception {
        RegistreringController ctrl = ctrl(melding);
        when(getInjectMock(ManntallsSokWidget.class).getFodselsnummer()).thenReturn("12345678909");
        when(getInjectMock(ManntallsSokWidget.class).getManntallsnummer()).thenReturn("123456789098");

        assertThat(ctrl.manntallsTomtResultatMelding(melding.manntallsSokType)).isEqualTo(forventet);
    }

    @DataProvider
    public Object[][] manntallsTomtResultatMelding() {
        return new Object[][]{
                {FODSELSNUMMER, "[@electoralRoll.ssnNotInElectoralRoll, 12345678909]"},
                {MANNTALLSNUMMER, "[@electoralRoll.numberNotInElectoralRoll, 123456789098]"},
                {AVANSERT_FORHAND, "@electoralRoll.personNotInElectoralRoll.special"},
                {AVANSERT_FORHAND_RETTT_I_URNE, "@electoralRoll.personNotInElectoralRoll"},
                {AVANSERT_VALGTING_ORDINAER, "@electoralRoll.personNotInElectoralRoll @voting.mustUseSpecialCover"}
        };
    }

    private Voting stemmegivning() {
        Voting stemmegivning = new Voting();
        stemmegivning.setCastTimestamp(DateTime.parse("2017-01-01T12:12:00"));
        stemmegivning.setVotingNumber(1);
        VotingCategory vc = new VotingCategory();
        vc.setId(VS.getId());
        stemmegivning.setVotingCategory(vc);
        return stemmegivning;
    }

    private Voter velger() {
        Voter velger = new Voter();
        velger.setFictitious(false);
        velger.setNameLine("Test Testesen");
        return velger;
    }

    @Test
    public void byggStemmegivningsMelding_medVelgerOgStemme_verifiserMelding() throws Exception {
        RegistreringController ctrl = ctrl();

        Voter velger = velger();
        Voting stemegivning = stemmegivning();

        assertThat(ctrl.byggStemmegivningsMelding(velger, stemegivning, "@test"))
                .isEqualTo("[@test, Test Testesen, @common.date.weekday[1].name, 01.01.2017, 12:12, VS, 1]");
    }

    private Kontekst kontekst() {
        Kontekst kontekst = new Kontekst();
        kontekst.setValghierarkiSti(ValghierarkiSti.fra(ELECTION_PATH_ELECTION_GROUP));
        kontekst.setValggeografiSti(ValggeografiSti.fra(AREA_PATH_POLLING_PLACE));
        return kontekst;
    }

    private Voter medVelger(StemmegivningController ctrl, Voter velger) {
        ctrl.initialized(kontekst());
        stub_hentStemmegivningsForberedelser(true, singletonList(new VelgerMelding(INGEN_VALGKRETS_FOR_VELGER)));
        ctrl.manntallsSokVelger(velger);
        return velger;
    }

    private Object stub_hentStemmegivningsForberedelser(boolean kanAvgiStemme, List<VelgerMelding> meldinger) {
        VelgerSomSkalStemme result = new VelgerSomSkalStemme(new ArrayList<>());
        result.setKanRegistrereStemmegivning(kanAvgiStemme);

        result.getStemmetypeListe().add(createMock(VotingCategory.class));
        result.getVelgerMeldinger().addAll(meldinger);
        when(getInjectMock(VotingService.class).hentVelgerSomSkalStemme(eq(getUserDataMock()), any(StemmegivningsType.class), any(ElectionPath.class),
                any(AreaPath.class), any(Voter.class))).thenReturn(result);
        return result;
    }

    private ThisRegistreringController ctrl() throws Exception {
        return initializeMocks(new ThisRegistreringController());
    }

    private ThisRegistreringController ctrl(ManntallsTomtResultatMelding melding) throws Exception {
        return initializeMocks(new ThisRegistreringController(melding));
    }

    private static class ThisRegistreringController extends RegistreringController {

        private static final long serialVersionUID = 9020242960005104472L;

        private ValggeografiNivaa valggeografiNivaa;
        private StemmegivningsType stemmegivningsType;
        private Boolean forhandsstemmeRettIUrne;

        ThisRegistreringController() {
            this(STEMMESTED, VALGTINGSTEMME_ORDINAER);
        }

        ThisRegistreringController(ManntallsTomtResultatMelding melding) {
            this(STEMMESTED, melding.stemmegivningsType);
            forhandsstemmeRettIUrne = melding.forhandRettIUrne;
        }

        ThisRegistreringController(ValggeografiNivaa valggeografiNivaa, StemmegivningsType stemmegivningsType) {
            this.valggeografiNivaa = valggeografiNivaa;
            this.stemmegivningsType = stemmegivningsType;
        }

        @Override
        public ValggeografiNivaa getStemmestedNiva() {
            return valggeografiNivaa;
        }

        @Override
        public void kontekstKlar() {
        }

        @Override
        public StemmegivningsType getStemmegivningsType() {
            return stemmegivningsType;
        }

        @Override
        public void registrerStemmegivning() {
        }

        @Override
        String timeString(DateTime dateTime) {
            return "12:12";
        }

        @Override
        public boolean isForhandsstemmeRettIUrne() {
            return forhandsstemmeRettIUrne == null ? super.isForhandsstemmeRettIUrne() : forhandsstemmeRettIUrne;
        }
    }

    private static class ManntallsTomtResultatMelding {
        private ManntallsSokType manntallsSokType;
        private StemmegivningsType stemmegivningsType;
        private boolean forhandRettIUrne;

        ManntallsTomtResultatMelding(ManntallsSokType manntallsSokType) {
            this.manntallsSokType = manntallsSokType;
            this.stemmegivningsType = FORHANDSSTEMME_ORDINAER;
        }

        ManntallsTomtResultatMelding(ManntallsSokType manntallsSokType, StemmegivningsType stemmegivningsType, boolean forhandRettIUrne) {
            this.manntallsSokType = manntallsSokType;
            this.stemmegivningsType = stemmegivningsType;
            this.forhandRettIUrne = forhandRettIUrne;
        }
    }
}
