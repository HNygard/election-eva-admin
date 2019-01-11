package no.valg.eva.admin.frontend.opptelling;

import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.BYDEL;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.STEMMEKRETS;
import static no.valg.eva.admin.felles.valghierarki.model.ValghierarkiNivaa.VALG;
import static no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerElement.geografi;
import static no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerElement.hierarki;
import static no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerElement.opptellingskategori;
import static org.assertj.core.api.Assertions.assertThat;

import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerOppsett;
import no.valg.eva.admin.frontend.kontekstvelger.valggeografi.ValggeografiVariant;

import org.testng.annotations.Test;

public class ForkastedeOpptellingerControllerTest extends BaseFrontendTest {
	@Test
	public void getKontekstVelgerOppsett_gittController_returnererKorrektOppsett() throws Exception {
		ForkastedeOpptellingerController controller = new ForkastedeOpptellingerController() {
			@Override
			protected String url() {
				return "url";
			}
		};
		KontekstvelgerOppsett oppsett = new KontekstvelgerOppsett();
		oppsett.leggTil(hierarki(VALG));
		oppsett.leggTil(opptellingskategori());
		oppsett.leggTil(geografi(BYDEL, STEMMEKRETS).medVariant(ValggeografiVariant.ALT_VELG_BYDEL));
		assertThat(controller.getKontekstVelgerOppsett()).isEqualTo(oppsett);
	}
}
