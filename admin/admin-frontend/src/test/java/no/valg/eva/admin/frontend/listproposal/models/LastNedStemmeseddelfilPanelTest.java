package no.valg.eva.admin.frontend.listproposal.models;

import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.felles.sti.valggeografi.ValggeografiSti;
import no.valg.eva.admin.felles.sti.valghierarki.ValgSti;
import no.valg.eva.admin.felles.sti.valghierarki.ValggruppeSti;
import no.valg.eva.admin.felles.sti.valghierarki.ValghierarkiSti;
import no.valg.eva.admin.felles.valggeografi.model.Fylkeskommune;
import no.valg.eva.admin.felles.valghierarki.model.Valg;
import no.valg.eva.admin.felles.valghierarki.model.Valgdistrikt;
import no.valg.eva.admin.felles.valghierarki.model.Valggruppe;
import no.valg.eva.admin.frontend.listproposal.ctrls.LastNedStemmeseddelfilController;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

import static java.util.Collections.singletonList;
import static no.valg.eva.admin.felles.test.data.valghierarki.ValggruppeTestData.VALGGRUPPE_NAVN_111111_11;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.KOMMUNE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;


public class LastNedStemmeseddelfilPanelTest extends BaseFrontendTest {

	private LastNedStemmeseddelfilController ctrl;

	@BeforeMethod
	public void init() {
		ctrl = createMock(LastNedStemmeseddelfilController.class);
		when(ctrl.getValghierarkiService().valggrupper(ctrl.getUserData())).thenReturn(valggruppeListe());
		when(ctrl.getValghierarkiService().valg(eq(ctrl.getUserData()), any(ValggruppeSti.class), any())).thenReturn(valgListe());
		when(ctrl.getValggeografiService().fylkeskommuner(eq(ctrl.getUserData()), any(ValghierarkiSti.class), any()))
				.thenReturn(fylkeskommuneListe());
		when(ctrl.getValghierarkiService().valgdistrikter(eq(ctrl.getUserData()), any(ValgSti.class))).thenReturn(valgdistriktListe());
		when(ctrl.getValghierarkiService().valgdistrikter(eq(ctrl.getUserData()), any(ValgSti.class), any(ValggeografiSti.class)))
				.thenReturn(valgdistriktListe());
		when(ctrl.getMvAreaService().findValgdistriktStierByValgStiWhereAllListProposalsAreApproved(eq(ctrl.getUserData()), any(ValgSti.class))).thenReturn(
				singletonList(ValghierarkiSti.valgdistriktSti(ELECTION_PATH_CONTEST)));
	}

	@Test
	public void create_medEnkleTabeller_veifiserState() {
		LastNedStemmeseddelfilPanel panel = new LastNedStemmeseddelfilPanel(ctrl);

		assertThat(panel.isValgPaaKommuneNiva()).isTrue();
		assertThat(panel.getTabeller()).hasSize(4);
		assertThat(panel.getValg().sti().electionPath()).isEqualTo(ELECTION_PATH_ELECTION);
		assertThat(panel.getValgdistrikt().sti().electionPath()).isEqualTo(ELECTION_PATH_CONTEST);
	}

	@Test
	public void isIndikatorOK_medNull_returnererFalse() {
		LastNedStemmeseddelfilPanel panel = new LastNedStemmeseddelfilPanel(ctrl);

		assertThat(panel.isIndikatorOK(null)).isFalse();
	}

	@Test
	public void isIndikatorOK_medValgdistrikt_returnererTrue() {
		LastNedStemmeseddelfilPanel panel = new LastNedStemmeseddelfilPanel(ctrl);

		assertThat(panel.isIndikatorOK(valgdistrikt())).isTrue();
		assertThat(panel.isIndikatorOK(valgdistrikt())).isTrue();
	}

	private List<Valggruppe> valggruppeListe() {
		return singletonList(valggruppe());
	}

	private Valggruppe valggruppe() {
		return new Valggruppe(ValghierarkiSti.valggruppeSti(ELECTION_PATH_ELECTION_GROUP), "Gruppe");
	}

	private List<Valg> valgListe() {
		return singletonList(valg());
	}

	private Valg valg() {
		return new Valg(ValghierarkiSti.valgSti(ELECTION_PATH_ELECTION), "Valg", KOMMUNE, true, VALGGRUPPE_NAVN_111111_11);
	}

	private List<Fylkeskommune> fylkeskommuneListe() {
		return singletonList(fylkeskommune());
	}

	private Fylkeskommune fylkeskommune() {
		return new Fylkeskommune(ValggeografiSti.fylkeskommuneSti(AREA_PATH_COUNTY), "Fylke");
	}

	private List<Valgdistrikt> valgdistriktListe() {
		return singletonList(valgdistrikt());
	}

	private Valgdistrikt valgdistrikt() {
		return new Valgdistrikt(ValghierarkiSti.valgdistriktSti(ELECTION_PATH_CONTEST), "Valg", KOMMUNE);
	}

}

