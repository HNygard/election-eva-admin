package no.valg.eva.admin.frontend.listproposal.models;

import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.felles.sti.valggeografi.ValggeografiSti;
import no.valg.eva.admin.felles.sti.valghierarki.ValghierarkiSti;
import no.valg.eva.admin.felles.valggeografi.model.Fylkeskommune;
import no.valg.eva.admin.felles.valghierarki.model.Valg;
import no.valg.eva.admin.felles.valghierarki.model.Valgdistrikt;
import no.valg.eva.admin.felles.valghierarki.model.Valggruppe;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static java.util.Arrays.asList;
import static no.valg.eva.admin.felles.test.data.valghierarki.ValggruppeTestData.VALGGRUPPE_NAVN_111111_11;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.FYLKESKOMMUNE;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.KOMMUNE;
import static no.valg.eva.admin.felles.valghierarki.model.ValghierarkiNivaa.VALG;
import static no.valg.eva.admin.felles.valghierarki.model.ValghierarkiNivaa.VALGDISTRIKT;
import static no.valg.eva.admin.felles.valghierarki.model.ValghierarkiNivaa.VALGGRUPPE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class LastNedStemmeseddelfilTabellTest extends BaseFrontendTest {

	private LastNedStemmeseddelfilPanel panelMock;

	@BeforeMethod
	public void init() {
		panelMock = createMock(LastNedStemmeseddelfilPanel.class);
	}

	@Test
    public void setValgtRad_medValgGruppeNivaa_klargjorValg() {
		LastNedStemmeseddelfilTabell tabell = new LastNedStemmeseddelfilTabell(panelMock, VALGGRUPPE);

		Object valgt = valggruppe();
		tabell.setValgtRad(valgt);

		verify(panelMock).initValg();
		assertThat(tabell.getValgtRad()).isSameAs(valgt);
		assertThat(tabell.isVisTabell()).isFalse();
		assertThat(tabell.isVisKnapp()).isFalse();
		assertThat(tabell.isKnappDeaktivert()).isTrue();
		assertThat(tabell.getId()).isSameAs(VALGGRUPPE);
	}

	@Test
    public void setValgtRad_medValgNivaaOgKommuneNivaa_klargjorFylker() {
		LastNedStemmeseddelfilTabell tabell = new LastNedStemmeseddelfilTabell(panelMock, VALG);
		when(panelMock.isValgPaaKommuneNiva()).thenReturn(true);

		tabell.setValgtRad(valg());

		verify(panelMock).initFylker();
		assertThat(tabell.isVisTabell()).isFalse();
		assertThat(tabell.isVisKnapp()).isFalse();
		assertThat(tabell.isKnappDeaktivert()).isTrue();
	}

	@Test
    public void setValgtRad_medValgNivaaOgIkkeKommuneNivaa_klargjorValgdistrikt() {
		LastNedStemmeseddelfilTabell tabell = new LastNedStemmeseddelfilTabell(panelMock, VALG);
		when(panelMock.isValgPaaKommuneNiva()).thenReturn(false);

		tabell.setValgtRad(valg());

		verify(panelMock).initValgdistrikt();
		assertThat(tabell.isVisTabell()).isFalse();
		assertThat(tabell.isVisKnapp()).isFalse();
		assertThat(tabell.isKnappDeaktivert()).isTrue();
	}

	@Test
    public void setValgtRad_medFylkesNivaa_klargjorValgdistrikt() {
		LastNedStemmeseddelfilTabell tabell = new LastNedStemmeseddelfilTabell(panelMock, FYLKESKOMMUNE);
		tabell.setRader(asList(createMock(Fylkeskommune.class), createMock(Fylkeskommune.class)));

		tabell.setValgtRad(fylkeskommune());

		verify(panelMock).initValgdistrikt();
		assertThat(tabell.isVisTabell()).isTrue();
		assertThat(tabell.isVisKnapp()).isFalse();
		assertThat(tabell.isKnappDeaktivert()).isTrue();
	}

	@Test(dataProvider = "getNavn")
    public void getNavn_medDataProvider_verifiserForventet(Object nivaa, String forventet) {
		LastNedStemmeseddelfilTabell tabell = new LastNedStemmeseddelfilTabell(panelMock, nivaa);

		assertThat(tabell.getNavn()).isEqualTo(forventet);
	}

	@DataProvider
	public Object[][] getNavn() {
		return new Object[][] {
				{ VALGGRUPPE, "@election_level[1].name" },
				{ FYLKESKOMMUNE, "@area_level[2].name" }
		};
	}

	@Test
	public void isVisTabell_medValgdistriktNivaa_returnererTrue() {
		LastNedStemmeseddelfilTabell tabell = new LastNedStemmeseddelfilTabell(panelMock, VALGDISTRIKT);

		assertThat(tabell.isVisTabell()).isTrue();
		assertThat(tabell.isVisKnapp()).isTrue();
	}

	@Test
	public void isKnappDeaktivert_medValgdistriktNivaaOgIkkeOKIndikator_returnererTrue() {
		LastNedStemmeseddelfilTabell tabell = new LastNedStemmeseddelfilTabell(panelMock, VALGDISTRIKT);
		when(panelMock.isIndikatorOK(any(Valgdistrikt.class))).thenReturn(false);
		tabell.setValgtRad(valgdistrikt());

		assertThat(tabell.isKnappDeaktivert()).isTrue();
	}

	private Valggruppe valggruppe() {
		return new Valggruppe(ValghierarkiSti.valggruppeSti(ELECTION_PATH_ELECTION_GROUP), "Gruppe");
	}

	private Valg valg() {
		return new Valg(ValghierarkiSti.valgSti(ELECTION_PATH_ELECTION), "Valg", KOMMUNE, true, VALGGRUPPE_NAVN_111111_11);
	}

	private Fylkeskommune fylkeskommune() {
		return new Fylkeskommune(ValggeografiSti.fylkeskommuneSti(AREA_PATH_COUNTY), "Fylke");
	}

	private Valgdistrikt valgdistrikt() {
		return new Valgdistrikt(ValghierarkiSti.valgdistriktSti(ELECTION_PATH_CONTEST), "Valg", KOMMUNE);
	}

}
