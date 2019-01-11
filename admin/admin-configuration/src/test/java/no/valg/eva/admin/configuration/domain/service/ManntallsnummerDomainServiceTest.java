package no.valg.eva.admin.configuration.domain.service;

import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import no.valg.eva.admin.backend.configuration.repository.ElectionEventRepository;
import no.valg.eva.admin.common.configuration.model.Manntallsnummer;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.test.MockUtilsTestCase;

import org.joda.time.LocalDate;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class ManntallsnummerDomainServiceTest extends MockUtilsTestCase {

	private static final LocalDate DATO_I_2015 = new LocalDate(2015, 9, 22);
	private static final int TVERRSUMMEN_AV_2015 = 8;
	private static final Manntallsnummer MANNTALLSNUMMER_FOR_2015 = new Manntallsnummer("1234567890" + TVERRSUMMEN_AV_2015 + "0");
	private static final Manntallsnummer MANNTALLSNUMMER_FOR_2014 = new Manntallsnummer("1234567890" + (TVERRSUMMEN_AV_2015 - 1) + "2");

	@Test(dataProvider = "manntallsnummer")
	public void beregnFulltManntallsnummer_forEtKortManntallsnummer_beregnerValgaarssifferOgLuhnSiffer(
		Long kortManntallsNummer, LocalDate datoIValgaar, String forventetManntallsnummer) throws Exception {
		
		ManntallsnummerDomainService manntallsnummerDomainService = initializeMocks(ManntallsnummerDomainService.class);
		ElectionEvent electionEvent = new ElectionEvent();
		when(getInjectMock(ElectionEventRepository.class).findLatestElectionDay(electionEvent)).thenReturn(datoIValgaar);
		Manntallsnummer beregnetFulltManntallsnummer = manntallsnummerDomainService.beregnFulltManntallsnummer(kortManntallsNummer, electionEvent);

		assertEquals(beregnetFulltManntallsnummer.getManntallsnummer(), forventetManntallsnummer);
	}

	@DataProvider
	private Object[][] manntallsnummer() {
		return new Object[][]{
			{ 1234567890L, DATO_I_2015, "123456789080" },
			{ 123L, 	   DATO_I_2015, "000000012385" }
		};
	}

	@Test(dataProvider = "valgaarssiffer")
	public void erValgaarssifferGyldig_forEtFulltManntallsnummer_returnererTrueHvisValgaarssifferetErKorrekt(LocalDate datoIValgaar,
																   Manntallsnummer manntallsnummer, boolean forventetResultat) throws Exception {
		ManntallsnummerDomainService manntallsnummerDomainService = initializeMocks(ManntallsnummerDomainService.class);
		ElectionEvent electionEvent = new ElectionEvent();
		when(getInjectMock(ElectionEventRepository.class).findLatestElectionDay(electionEvent)).thenReturn(datoIValgaar);

		boolean valgaarssifferGyldig = manntallsnummerDomainService.erValgaarssifferGyldig(manntallsnummer, electionEvent);

		assertEquals(valgaarssifferGyldig, forventetResultat);
	}

	@DataProvider
	private Object[][] valgaarssiffer() {
		return new Object[][]{
			{ DATO_I_2015, MANNTALLSNUMMER_FOR_2015, true },
			{ DATO_I_2015, MANNTALLSNUMMER_FOR_2014, false }
		};
	}

	@Test(dataProvider = "tverrsumData")
	public void minsteTverrsum_forEtGittTall_BeregnerSummenAvAlleSifreneTilDetBareErEttSifferIgjen(int tall, int forventetTverrsum) throws Exception {
		ManntallsnummerDomainService manntallsnummerDomainService = initializeMocks(ManntallsnummerDomainService.class);
		assertEquals(manntallsnummerDomainService.minsteTverrsum(tall), forventetTverrsum);
	}

	@DataProvider
	private Object[][] tverrsumData() {
		return new Object[][]{
			{ 1, 	 1 },
			{ 12,	 3 },
			{ 123,	 6 },
			{ 1234,	 1 },
			{ 12345, 6 },
		};
	}

}

