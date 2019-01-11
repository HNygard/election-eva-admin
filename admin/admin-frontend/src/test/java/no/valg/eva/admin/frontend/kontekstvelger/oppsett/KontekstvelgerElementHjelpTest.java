package no.valg.eva.admin.frontend.kontekstvelger.oppsett;

import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.FYLKESKOMMUNE;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.KOMMUNE;
import static no.valg.eva.admin.felles.valghierarki.model.ValghierarkiNivaa.VALGGRUPPE;
import static no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerElement.geografi;
import static no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerElement.hierarki;
import static no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerElement.opptellingskategori;
import static no.valg.eva.admin.frontend.kontekstvelger.valggeografi.ValggeografiFilter.FORHAND_ORDINAERE;
import static no.valg.eva.admin.frontend.kontekstvelger.valggeografi.ValggeografiTjeneste.LAG_NYTT_VALGKORT;
import static no.valg.eva.admin.frontend.kontekstvelger.valghierarki.ValghierarkiTjeneste.SLETT_VALGOPPGJOER;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa;
import no.valg.eva.admin.felles.valghierarki.model.ValghierarkiNivaa;
import no.valg.eva.admin.frontend.kontekstvelger.valggeografi.ValggeografiFilter;
import no.valg.eva.admin.frontend.kontekstvelger.valggeografi.ValggeografiTjeneste;
import no.valg.eva.admin.frontend.kontekstvelger.valghierarki.ValghierarkiTjeneste;
import org.testng.annotations.Test;

public class KontekstvelgerElementHjelpTest extends BaseFrontendTest {

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void hierarkiNivaer_medGeografiElement_kasterException() {
		KontekstvelgerElementHjelp.electionHierarchyLevels(geografi(KOMMUNE));
	}

	@Test
	public void hierarkiNivaer_medHierarkiElement_returnererNivaaer() {
		List<ValghierarkiNivaa> resultat = KontekstvelgerElementHjelp.electionHierarchyLevels(hierarki(VALGGRUPPE));
		assertThat(resultat).containsExactly(VALGGRUPPE);
	}

	@Test
	public void valgbareValghierarkiNivaaerFra_medOppsettMedHierarkiElement_returnererNivaaer() {
		KontekstvelgerOppsett oppsett = new KontekstvelgerOppsett();
		oppsett.leggTil(hierarki(VALGGRUPPE));
		List<ValghierarkiNivaa> resultat = KontekstvelgerElementHjelp.valgbareValghierarkiNivaaerFra(oppsett);
		assertThat(resultat).containsExactly(VALGGRUPPE);
	}

	@Test
	public void valgbareValghierarkiNivaaerFra_medOppsettMedGeografiElement_returnererNull() {
		KontekstvelgerOppsett oppsett = new KontekstvelgerOppsett();
		oppsett.leggTil(geografi(FYLKESKOMMUNE, KOMMUNE));
		List<ValghierarkiNivaa> resultat = KontekstvelgerElementHjelp.valgbareValghierarkiNivaaerFra(oppsett);
		assertThat(resultat).isNull();
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void geografiNivaer_medHierarkiElement_kasterException() {
        KontekstvelgerElementHjelp.electionGeoLevels(hierarki(VALGGRUPPE));
	}

	@Test
	public void geografiNivaer_medGeografiElement_returnererNivaaer() {
        List<ValggeografiNivaa> resultat = KontekstvelgerElementHjelp.electionGeoLevels(geografi(FYLKESKOMMUNE, KOMMUNE));
		assertThat(resultat).containsExactly(FYLKESKOMMUNE, KOMMUNE);
	}

	@Test
	public void valgbareValggeografiNivaaerFra_medOppsettMedGeografiElement_returnererNivaaer() {
		KontekstvelgerOppsett oppsett = new KontekstvelgerOppsett();
		oppsett.leggTil(geografi(FYLKESKOMMUNE, KOMMUNE));
		List<ValggeografiNivaa> resultat = KontekstvelgerElementHjelp.valgbareValggeografiNivaaerFra(oppsett);
		assertThat(resultat).containsExactly(FYLKESKOMMUNE, KOMMUNE);
	}

	@Test
	public void valgbareValggeografiNivaaerFra_medOppsettMedHierarkiElement_returnererNull() {
		KontekstvelgerOppsett oppsett = new KontekstvelgerOppsett();
		oppsett.leggTil(hierarki(VALGGRUPPE));
		List<ValggeografiNivaa> resultat = KontekstvelgerElementHjelp.valgbareValggeografiNivaaerFra(oppsett);
		assertThat(resultat).isNull();
	}

	@Test
	public void inkluderOpptellingskategori_gittOppsettMedOpptellingskategori_returnererTrue() {
		KontekstvelgerOppsett oppsett = new KontekstvelgerOppsett();
		oppsett.leggTil(opptellingskategori());
		assertThat(KontekstvelgerElementHjelp.inkluderOpptellingskategori(oppsett)).isTrue();
	}

	@Test
	public void inkluderOpptellingskategori_gittOppsettUtenOpptellingskategori_returnererFalse() {
		assertThat(KontekstvelgerElementHjelp.inkluderOpptellingskategori(new KontekstvelgerOppsett())).isFalse();
	}

	@Test
	public void valggeografiFilter_gittOppsettMedGeografiMedFilter_returnerFilter() {
		KontekstvelgerOppsett oppsett = new KontekstvelgerOppsett();
		oppsett.leggTil(geografi(FYLKESKOMMUNE, KOMMUNE).medFilter(FORHAND_ORDINAERE));
		assertThat(KontekstvelgerElementHjelp.valggeografiFilter(oppsett)).isEqualTo(FORHAND_ORDINAERE);
	}

	@Test
	public void valggeografiFilter_gittOppsettMedGeografiUtenFilter_returnerDefault() {
		KontekstvelgerOppsett oppsett = new KontekstvelgerOppsett();
		oppsett.leggTil(geografi(FYLKESKOMMUNE, KOMMUNE));
		assertThat(KontekstvelgerElementHjelp.valggeografiFilter(oppsett)).isEqualTo(ValggeografiFilter.DEFAULT);
	}

	@Test
	public void valggeografiFilter_gittOppsettUtenGeografi_returnerDefault() {
		KontekstvelgerOppsett oppsett = new KontekstvelgerOppsett();
		assertThat(KontekstvelgerElementHjelp.valggeografiFilter(oppsett)).isEqualTo(ValggeografiFilter.DEFAULT);
	}

	@Test
	public void valggeografiTjeneste_gittOppsettMedGeografiMedTjeneste_returnerTjeneste() {
		KontekstvelgerOppsett oppsett = new KontekstvelgerOppsett();
		oppsett.leggTil(geografi(FYLKESKOMMUNE, KOMMUNE).medTjeneste(LAG_NYTT_VALGKORT));
		assertThat(KontekstvelgerElementHjelp.electionGeographyAction(oppsett)).isEqualTo(LAG_NYTT_VALGKORT);
	}

	@Test
	public void valggeografiTjeneste_gittOppsettMedGeografiUtenTjeneste_returnerDefault() {
		KontekstvelgerOppsett oppsett = new KontekstvelgerOppsett();
		oppsett.leggTil(geografi(FYLKESKOMMUNE, KOMMUNE));
		assertThat(KontekstvelgerElementHjelp.electionGeographyAction(oppsett)).isEqualTo(ValggeografiTjeneste.DEFAULT);
	}

	@Test
	public void valggeografiTjeneste_gittOppsettUtenGeografi_returnerDefault() {
		KontekstvelgerOppsett oppsett = new KontekstvelgerOppsett();
		assertThat(KontekstvelgerElementHjelp.electionGeographyAction(oppsett)).isEqualTo(ValggeografiTjeneste.DEFAULT);
	}

	@Test
	public void valghierarkiTjeneste_gittOppsettMedHierarkiMedTjeneste_returnerTjeneste() {
		KontekstvelgerOppsett oppsett = new KontekstvelgerOppsett();
		oppsett.leggTil(hierarki(VALGGRUPPE).medTjeneste(SLETT_VALGOPPGJOER));
		assertThat(KontekstvelgerElementHjelp.valghierarkiTjeneste(oppsett)).isEqualTo(SLETT_VALGOPPGJOER);
	}

	@Test
	public void valghierarkiTjeneste_gittOppsettMedHierarkiUtenTjeneste_returnerDefault() {
		KontekstvelgerOppsett oppsett = new KontekstvelgerOppsett();
		oppsett.leggTil(hierarki(VALGGRUPPE));
		assertThat(KontekstvelgerElementHjelp.valghierarkiTjeneste(oppsett)).isEqualTo(ValghierarkiTjeneste.DEFAULT);
	}

	@Test
	public void valghierarkiTjeneste_gittOppsettUtenHierarki_returnerDefault() {
		KontekstvelgerOppsett oppsett = new KontekstvelgerOppsett();
		assertThat(KontekstvelgerElementHjelp.valghierarkiTjeneste(oppsett)).isEqualTo(ValghierarkiTjeneste.DEFAULT);
	}
}
