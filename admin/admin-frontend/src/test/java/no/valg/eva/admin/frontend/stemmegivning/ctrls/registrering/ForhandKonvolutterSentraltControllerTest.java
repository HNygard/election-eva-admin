package no.valg.eva.admin.frontend.stemmegivning.ctrls.registrering;

import no.evote.service.configuration.MvAreaService;
import no.evote.util.MvAreaBuilder;
import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.Voter;
import no.valg.eva.admin.voting.domain.model.Voting;
import org.joda.time.DateTime;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static no.valg.eva.admin.common.AreaPath.CENTRAL_ENVELOPE_REGISTRATION_POLLING_PLACE_ID;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.KOMMUNE;
import static no.valg.eva.admin.voting.domain.model.StemmegivningsType.FORHANDSSTEMME_KONVOLUTTER_SENTRALT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;


public class ForhandKonvolutterSentraltControllerTest extends BaseFrontendTest {

    @Test
    public void getStemmestedNiva_returnererKommune() throws Exception {
        ForhandKonvolutterSentraltController ctrl = initializeMocks(ForhandKonvolutterSentraltController.class);

        assertThat(ctrl.getStemmestedNiva()).isSameAs(KOMMUNE);
    }

    @Test
    public void getStemmegivningsType_returnererForhandsstemmerKonvolutterSentralt() throws Exception {
        ForhandKonvolutterSentraltController ctrl = initializeMocks(ForhandKonvolutterSentraltController.class);

        assertThat(ctrl.getStemmegivningsType()).isSameAs(FORHANDSSTEMME_KONVOLUTTER_SENTRALT);
    }

    @Test
    public void kontekstKlar_medStemmestedOgAvkryssningsmanntallKjort_verifiserState() throws Exception {
        ForhandKonvolutterSentraltController ctrl = initializeMocks(ForhandKonvolutterSentraltController.class);
        ctrl.setStemmested(stemmested());
        MvArea stemmested = stub_findByMunicipalityAndPollingPlaceId();
        when(stemmested.getMunicipality().isAvkrysningsmanntallKjort()).thenReturn(true);

        ctrl.kontekstKlar();

        assertThat(ctrl.getStemmested()).isSameAs(stemmested);
        assertThat(ctrl.getStatiskeMeldinger()).hasSize(1);
        assertThat(ctrl.getStatiskeMeldinger().get(0).getText()).isEqualTo("@voting.search.forhåndsstemmerStengtPgaAvkrysningsmanntallKjort");
    }

    @Test
    public void manntallsSokVelger_medNullOgAvkryssningsmanntallKjort_sjekkMelding() throws Exception {
        ForhandKonvolutterSentraltController ctrl = initializeMocks(ForhandKonvolutterSentraltController.class);
        MvArea stemmested = new MvAreaBuilder(AREA_PATH_POLLING_PLACE_ENVELOPE).getValue();
        when(stemmested.getMunicipality().isAvkrysningsmanntallKjort()).thenReturn(true);
        ctrl.setStemmested(stemmested);

        ctrl.manntallsSokVelger(null);

        assertThat(ctrl.getStatiskeMeldinger()).hasSize(1);
        assertThat(ctrl.getStatiskeMeldinger().get(0).getText()).isEqualTo("@voting.search.forhåndsstemmerStengtPgaAvkrysningsmanntallKjort");
    }

    @Test(dataProvider = "isStemmetypeDisabled")
    public void isStemmetypeDisabled_medDataProvider_verifiserForventet(boolean kanAvgiStemme, boolean velgerEgenKommune, boolean forventet) throws Exception {
        MyController ctrl = ctrl(null);
        ctrl.setKanRegistrereStemmegivning(kanAvgiStemme);
        ctrl.setVelgerEgenKommune(velgerEgenKommune);

        assertThat(ctrl.isStemmetypeDisabled()).isEqualTo(forventet);
    }

    @DataProvider
    public Object[][] isStemmetypeDisabled() {
        return new Object[][]{
                {false, false, false},
                {true, false, false},
                {false, true, true},
                {true, true, false},
        };
    }

    @Test
    public void isVisOpprettFiktivVelgerLink_medTomtSokOgIkkeRettIUrne_returnererTrue() throws Exception {
        MyController ctrl = ctrl(null);
        ctrl.setIngenVelgerFunnet(true);
        ctrl.setForhandsstemmeRettIUrne(false);

        assertThat(ctrl.isVisOpprettFiktivVelgerLink()).isTrue();
    }

    @Test
    public void isVisSlettForhandsstemmeLink_medStemmegivningIkkeRettIUrne_returnererTrue() throws Exception {
        MyController ctrl = ctrl(null);
        ctrl.setStemmegivning(createMock(Voting.class));
        ctrl.setForhandsstemmeRettIUrne(false);

        assertThat(ctrl.isVisSlettForhandsstemmeLink()).isTrue();
    }

    private MvArea stemmested() {
        return new MvAreaBuilder(AREA_PATH_POLLING_PLACE).getValue();
    }

    private MvArea stub_findByMunicipalityAndPollingPlaceId() {
        MvArea stemmested = new MvAreaBuilder(AREA_PATH_POLLING_PLACE_ENVELOPE).getValue();
        when(getInjectMock(MvAreaService.class).findByMunicipalityAndPollingPlaceId(getUserDataMock(), 4444L, CENTRAL_ENVELOPE_REGISTRATION_POLLING_PLACE_ID))
                .thenReturn(stemmested);
        return stemmested;
    }

    private MyController ctrl(Voter velger) throws Exception {
        MyController ctrl = initializeMocks(new MyController());
        mockFieldValue("velger", velger);
        return ctrl;
    }

    private static class MyController extends ForhandKonvolutterSentraltController {

        private static final long serialVersionUID = -3187820139135910858L;

        private boolean velgerEgenKommune;
        private boolean forhandsstemmeRettIUrne;
        private boolean kanAvgiStemme;
        private boolean ingenVelgerFunnet;

        @Override
        String timeString(DateTime dateTime) {
            return "12:12";
        }

        @Override
        public boolean isVelgerEgenKommune() {
            return velgerEgenKommune;
        }

        void setVelgerEgenKommune(boolean velgerEgenKommune) {
            this.velgerEgenKommune = velgerEgenKommune;
        }

        @Override
        public boolean isForhandsstemmeRettIUrne() {
            return forhandsstemmeRettIUrne;
        }

        void setForhandsstemmeRettIUrne(boolean forhandsstemmeRettIUrne) {
            this.forhandsstemmeRettIUrne = forhandsstemmeRettIUrne;
        }

        @Override
        public boolean isKanRegistrereStemmegivning() {
            return kanAvgiStemme;
        }

        @Override
        public void setKanRegistrereStemmegivning(boolean kanAvgiStemme) {
            this.kanAvgiStemme = kanAvgiStemme;
        }

        @Override
        public boolean isIngenVelgerFunnet() {
            return ingenVelgerFunnet;
        }

        void setIngenVelgerFunnet(boolean ingenVelgerFunnet) {
            this.ingenVelgerFunnet = ingenVelgerFunnet;
        }
    }

}

