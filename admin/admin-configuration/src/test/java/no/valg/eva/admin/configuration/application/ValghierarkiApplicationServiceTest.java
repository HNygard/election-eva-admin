package no.valg.eva.admin.configuration.application;

import static no.valg.eva.admin.felles.test.data.sti.valggeografi.ValggeografiStiTestData.valggeografiSti;
import static no.valg.eva.admin.felles.test.data.sti.valghierarki.ValgStiTestData.VALG_STI;
import static no.valg.eva.admin.felles.test.data.sti.valghierarki.ValgdistriktStiTestData.VALGDISTRIKT_STI;
import static no.valg.eva.admin.felles.test.data.sti.valghierarki.ValggruppeStiTestData.VALGGRUPPE_STI;
import static no.valg.eva.admin.felles.test.data.sti.valghierarki.ValghendelseStiTestData.VALGHENDELSE_STI;
import static no.valg.eva.admin.felles.test.data.valghierarki.ValgTestData.VALG_111111_11_11;
import static no.valg.eva.admin.felles.test.data.valghierarki.ValgTestData.flereValg;
import static no.valg.eva.admin.felles.test.data.valghierarki.ValgTestData.valg;
import static no.valg.eva.admin.felles.test.data.valghierarki.ValgdistriktTestData.VALGDISTRIKT_111111_11_11_111111;
import static no.valg.eva.admin.felles.test.data.valghierarki.ValgdistriktTestData.valgdistrikter;
import static no.valg.eva.admin.felles.test.data.valghierarki.ValggruppeTestData.VALGGRUPPE_111111_11;
import static no.valg.eva.admin.felles.test.data.valghierarki.ValggruppeTestData.valggrupper;
import static no.valg.eva.admin.felles.test.data.valghierarki.ValghendelseTestData.VALGHENDELSE_111111;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import no.evote.security.UserData;
import no.valg.eva.admin.configuration.domain.service.ValghierarkiDomainService;
import no.valg.eva.admin.felles.sti.valggeografi.ValggeografiSti;
import no.valg.eva.admin.felles.sti.valghierarki.ValghendelseSti;
import no.valg.eva.admin.felles.sti.valghierarki.ValghierarkiSti;
import no.valg.eva.admin.felles.valghierarki.model.Valg;
import no.valg.eva.admin.felles.valghierarki.model.Valgdistrikt;
import no.valg.eva.admin.felles.valghierarki.model.Valggruppe;
import no.valg.eva.admin.felles.valghierarki.model.Valghendelse;
import no.valg.eva.admin.felles.valghierarki.model.Valghierarki;
import no.valg.eva.admin.test.MockUtilsTestCase;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ValghierarkiApplicationServiceTest extends MockUtilsTestCase {
	private ValghierarkiApplicationService service;

	private ValghierarkiDomainService domainService;
	private UserData userData;
	@BeforeMethod
	public void setUp() throws Exception {
		service = initializeMocks(ValghierarkiApplicationService.class);
		domainService = getInjectMock(ValghierarkiDomainService.class);
		userData = createMock(UserData.class);
	}

	@Test
	public void valghendelse_gittUserData_returnererValghendelse() throws Exception {
		when(domainService.valghendelse(operatorValghendelseSti())).thenReturn(VALGHENDELSE_111111);
		Valghendelse resultat = service.valghendelse(userData);
		assertThat(resultat).isSameAs(VALGHENDELSE_111111);
	}

	@Test
	public void valggrupper_gittUserData_returnerValggrupperForValghendelse() throws Exception {
		List<Valggruppe> valggrupper = valggrupper();
		when(domainService.valggrupper(operatorValghendelseSti())).thenReturn(valggrupper);
		List<Valggruppe> resultat = service.valggrupper(userData);
		assertThat(resultat).isSameAs(valggrupper);
	}

	@Test
	public void valg_gittValgSti_returnererValg() throws Exception {
		Valg valg = valg();
		when(domainService.valg(VALG_STI)).thenReturn(valg);
		Valg resultat = service.valg(VALG_STI);
		assertThat(resultat).isSameAs(valg);
	}

	@Test
	public void valg_gittUserDataOgValggruppeSti_returnererValgForValggruppe() throws Exception {
		List<Valg> valg = flereValg();
		when(domainService.valg(VALGGRUPPE_STI, operatorValggeografiSti(), false, null)).thenReturn(valg);
		List<Valg> resultat = service.valg(userData, VALGGRUPPE_STI, null);
		assertThat(resultat).isSameAs(valg);
	}

	@Test
	public void valgdistrikter_gittUserDataOgValgSti_returnererValgdistriktForValg() throws Exception {
		List<Valgdistrikt> valgdistrikter = valgdistrikter();
		when(domainService.valgdistrikter(VALG_STI, operatorValggeografiSti())).thenReturn(valgdistrikter);
		List<Valgdistrikt> resultat = service.valgdistrikter(userData, VALG_STI);
		assertThat(resultat).isSameAs(valgdistrikter);
	}

	@Test
	public void valghierarki_gittValghendelseSti_returnererValghendelse() throws Exception {
		when(domainService.valghendelse(VALGHENDELSE_STI)).thenReturn(VALGHENDELSE_111111);
		Valghierarki resultat = service.valghierarki(VALGHENDELSE_STI);
		assertThat(resultat).isSameAs(VALGHENDELSE_111111);
	}

	@Test
	public void valghierarki_gittValggruppeSti_returnererValggruppe() throws Exception {
		when(domainService.valggruppe(VALGGRUPPE_STI)).thenReturn(VALGGRUPPE_111111_11);
		Valghierarki resultat = service.valghierarki(VALGGRUPPE_STI);
		assertThat(resultat).isSameAs(VALGGRUPPE_111111_11);
	}

	@Test
	public void valghierarki_gittValgSti_returnererValg() throws Exception {
		when(domainService.valg(VALG_STI)).thenReturn(VALG_111111_11_11);
		Valghierarki resultat = service.valghierarki(VALG_STI);
		assertThat(resultat).isSameAs(VALG_111111_11_11);
	}

	@Test
	public void valghierarki_gittValgdistriktSti_returnererValgdistrikt() throws Exception {
		when(domainService.valgdistrikt(VALGDISTRIKT_STI)).thenReturn(VALGDISTRIKT_111111_11_11_111111);
		Valghierarki resultat = service.valghierarki(VALGDISTRIKT_STI);
		assertThat(resultat).isSameAs(VALGDISTRIKT_111111_11_11_111111);
	}

	@Test
	public void valgdistrikter_gittX_returnererY() throws Exception {
		ValggeografiSti valggeografiSti = valggeografiSti();
		List<Valgdistrikt> valgdistrikter = valgdistrikter();
		when(domainService.valgdistrikterFiltrertPaaGeografi(VALG_STI, valggeografiSti)).thenReturn(valgdistrikter);
		List<Valgdistrikt> resultat = service.valgdistrikter(userData, VALG_STI, valggeografiSti);
		assertThat(resultat).isEqualTo(valgdistrikter);
	}

	private ValghendelseSti operatorValghendelseSti() {
		return operatorValghierarkiSti().valghendelseSti();
	}

	private ValghierarkiSti operatorValghierarkiSti() {
		return userData.operatorValghierarkiSti();
	}

	private ValggeografiSti operatorValggeografiSti() {
		return userData.operatorValggeografiSti();
	}
}
