package no.valg.eva.admin.common.configuration.model;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.testng.Assert.assertEquals;

import java.util.List;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class GeografiSpesifikasjonTest {

	@Test(dataProvider = "fylker")
	public void referererFylke_dersomFylkeFinnesIKommuneEllerKretsLista_returnererTrue(GeografiSpesifikasjon geografiSpesifikasjon, String fylkesId, boolean finnes) {
		assertEquals(geografiSpesifikasjon.referererFylke(fylkesId), finnes);
	}


	@SuppressWarnings("unused")
	@DataProvider
	private Object[][] fylker() {
		return new Object[][]{
			{ geografiSpesifikasjon(asList("0101", "0201", "0301"), emptyList()), "01", true },
			{ geografiSpesifikasjon(asList("0101", "0201", "0301"), emptyList()), "02", true },
			{ geografiSpesifikasjon(asList("0101", "0201", "0301"), emptyList()), "03", true },
			{ geografiSpesifikasjon(asList("0101", "0201", "0301"), emptyList()), "04", false },
			{ geografiSpesifikasjon(asList("0101", "0201", "0301"), emptyList()), "05", false },
			
			{ geografiSpesifikasjon(emptyList(), asList("0101.0000", "0201.0000", "0201.0001")),  "01", true },
			{ geografiSpesifikasjon(emptyList(), asList("0101.0000", "0201.0000", "0201.0001")),  "02", true },
			{ geografiSpesifikasjon(emptyList(), asList("0101.0000", "0201.0000", "0201.0001")),  "03", false },
			{ geografiSpesifikasjon(emptyList(), asList("0101.0000", "0201.0000", "0201.0001")),  "04", false },
			{ geografiSpesifikasjon(emptyList(), asList("0101.0000", "0201.0000", "0201.0001")),  "05", false },
			
			{ geografiSpesifikasjon(emptyList(), emptyList(), asList("0101.0000.0000", "0201.0000.0000", "0201.0001.0000")),  "01", true },
			{ geografiSpesifikasjon(emptyList(), emptyList(), asList("0101.0000.0000", "0201.0000.0000", "0201.0001.0000")),  "02", true },
			{ geografiSpesifikasjon(emptyList(), emptyList(), asList("0101.0000.0000", "0201.0000.0000", "0201.0001.0000")),  "03", false },
			{ geografiSpesifikasjon(emptyList(), emptyList(), asList("0101.0000.0000", "0201.0000.0000", "0201.0001.0000")),  "04", false },
			{ geografiSpesifikasjon(emptyList(), emptyList(), asList("0101.0000.0000", "0201.0000.0000", "0201.0001.0000")),  "05", false }
		};
	}

	private GeografiSpesifikasjon geografiSpesifikasjon(List<String> kommuner, List<String> kretser) {
		return new GeografiSpesifikasjon(kommuner, kretser);
	}

	private GeografiSpesifikasjon geografiSpesifikasjon(List<String> kommuner, List<String> kretser, List<String> stemmesteder) {
		return new GeografiSpesifikasjon(kommuner, kretser, stemmesteder);
	}

	@Test(dataProvider = "kommuner")
	public void referererKommune_dersomKommuneFinnesIKommuneEllerKretsLista_returnererTrue(
		GeografiSpesifikasjon geografiSpesifikasjon, String kommuneId, boolean finnes) {
		assertEquals(geografiSpesifikasjon.referererKommune(kommuneId), finnes);
	}

	@SuppressWarnings("unused")
	@DataProvider
	private Object[][] kommuner() {
		return new Object[][]{
			{ geografiSpesifikasjon(asList("0101", "0201", "0301"), emptyList()), "0101", true },
			{ geografiSpesifikasjon(asList("0101", "0201", "0301"), emptyList()), "0201", true },
			{ geografiSpesifikasjon(asList("0101", "0201", "0301"), emptyList()), "0301", true },
			{ geografiSpesifikasjon(asList("0101", "0201", "0301"), emptyList()), "0401", false },
			{ geografiSpesifikasjon(asList("0101", "0201", "0301"), emptyList()), "0501", false },
			
			{ geografiSpesifikasjon(emptyList(), asList("0101.0000", "0201.0000", "0201.0001")),  "0101", true },
			{ geografiSpesifikasjon(emptyList(), asList("0101.0000", "0201.0000", "0201.0001")),  "0201", true },
			{ geografiSpesifikasjon(emptyList(), asList("0101.0000", "0201.0000", "0201.0001")),  "0202", false },
			{ geografiSpesifikasjon(emptyList(), asList("0101.0000", "0201.0000", "0201.0001")),  "0301", false },
			{ geografiSpesifikasjon(emptyList(), asList("0101.0000", "0201.0000", "0201.0001")),  "0401", false },

			{ geografiSpesifikasjon(emptyList(), emptyList(), asList("0101.0000.0000", "0201.0000.0000", "0201.0001.0000")),  "0101", true },
			{ geografiSpesifikasjon(emptyList(), emptyList(), asList("0101.0000.0000", "0201.0000.0000", "0201.0001.0000")),  "0201", true },
			{ geografiSpesifikasjon(emptyList(), emptyList(), asList("0101.0000.0000", "0201.0000.0000", "0201.0001.0000")),  "0301", false },
			{ geografiSpesifikasjon(emptyList(), emptyList(), asList("0101.0000.0000", "0201.0000.0000", "0201.0001.0000")),  "0401", false },
			{ geografiSpesifikasjon(emptyList(), emptyList(), asList("0101.0000.0000", "0201.0000.0000", "0201.0001.0000")),  "0501", false }
		};
	}

	@Test(dataProvider = "kretser")
	public void referererKrets_dersomErDelAvKommuneEllerKretsFinnesIKretsLista_returnererTrue(
		GeografiSpesifikasjon geografiSpesifikasjon, String municipalityId, String pollingDistrictId, boolean finnes) {
		assertEquals(geografiSpesifikasjon.referererKrets(municipalityId, pollingDistrictId), finnes);
	}

	@SuppressWarnings("unused")
	@DataProvider
	private Object[][] kretser() {
		return new Object[][]{
			{ geografiSpesifikasjon(asList("0101", "0201", "0301"), emptyList()), 					"0101", "0000", true },
			{ geografiSpesifikasjon(asList("0101", "0201", "0301"), emptyList()), 					"0201", "0000", true },
			{ geografiSpesifikasjon(asList("0101", "0201", "0301"), emptyList()),					"0301", "0000", true },
			{ geografiSpesifikasjon(asList("0101", "0201", "0301"), emptyList()),					"0401", "0000", false },
			{ geografiSpesifikasjon(asList("0101", "0201", "0301"), emptyList()),					"0501", "0000", false },
			
			{ geografiSpesifikasjon(emptyList(), asList("0101.0000", "0201.0000", "0201.0001")), 	"0101", "0000", true },
			{ geografiSpesifikasjon(emptyList(), asList("0101.0000", "0201.0000", "0201.0001")), 	"0201", "0000", true },
			{ geografiSpesifikasjon(emptyList(), asList("0101.0000", "0201.0000", "0201.0001")), 	"0201", "0001", true },
			{ geografiSpesifikasjon(emptyList(), asList("0101.0000", "0201.0000", "0201.0001")), 	"0301", "0000", false },
			{ geografiSpesifikasjon(emptyList(), asList("0101.0000", "0201.0000", "0201.0001")), 	"0401", "0000", false },
			
			{ geografiSpesifikasjon(emptyList(), emptyList(), asList("0101.0000.0000", "0201.0000.0000", "0201.0001.0000")), 	"0101", "0000", true },
			{ geografiSpesifikasjon(emptyList(), emptyList(), asList("0101.0000.0000", "0201.0000.0000", "0201.0001.0000")), 	"0201", "0000", true },
			{ geografiSpesifikasjon(emptyList(), emptyList(), asList("0101.0000.0000", "0201.0000.0000", "0201.0001.0000")), 	"0201", "0001", true },
			{ geografiSpesifikasjon(emptyList(), emptyList(), asList("0101.0000.0000", "0201.0000.0000", "0201.0001.0000")), 	"0301", "0000", false },
			{ geografiSpesifikasjon(emptyList(), emptyList(), asList("0101.0000.0000", "0201.0000.0000", "0201.0001.0000")), 	"0401", "0000", false }
		};
	}

	@Test(dataProvider = "stemmesteder")
	public void referererStemmested_dersomErDelAvKommuneKretsEllerFinnesIStemmestedslista_returnererTrue(
		GeografiSpesifikasjon geografiSpesifikasjon, String municipalityId, String pollingDistrictId, String pollingPlaceId, boolean finnes) {
		assertEquals(geografiSpesifikasjon.referererStemmested(municipalityId, pollingDistrictId, pollingPlaceId), finnes);
	}

	@SuppressWarnings("unused")
	@DataProvider
	private Object[][] stemmesteder() {
		return new Object[][]{
			{ geografiSpesifikasjon(asList("0101", "0201", "0301"), emptyList(), emptyList()), 					"0101", "0000", "0000", true },
			{ geografiSpesifikasjon(asList("0101", "0201", "0301"), emptyList(), emptyList()), 					"0201", "0000", "0000", true },
			{ geografiSpesifikasjon(asList("0101", "0201", "0301"), emptyList(), emptyList()),					"0301", "0000", "0000", true },
			{ geografiSpesifikasjon(asList("0101", "0201", "0301"), emptyList(), emptyList()),					"0401", "0000", "0000", false },
			{ geografiSpesifikasjon(asList("0101", "0201", "0301"), emptyList(), emptyList()),					"0501", "0000", "0000", false },

			{ geografiSpesifikasjon(emptyList(), asList("0101.0000", "0201.0000", "0201.0001"), emptyList()), 	"0101", "0000", "0000", true },
			{ geografiSpesifikasjon(emptyList(), asList("0101.0000", "0201.0000", "0201.0001"), emptyList()), 	"0201", "0000", "0000", true },
			{ geografiSpesifikasjon(emptyList(), asList("0101.0000", "0201.0000", "0201.0001"), emptyList()),	"0201", "0001", "0000", true },
			{ geografiSpesifikasjon(emptyList(), asList("0101.0000", "0201.0000", "0201.0001"), emptyList()), 	"0301", "0000", "0000", false },
			{ geografiSpesifikasjon(emptyList(), asList("0101.0000", "0201.0000", "0201.0001"), emptyList()), 	"0401", "0000", "0000", false },

			{ geografiSpesifikasjon(emptyList(), emptyList(), asList("0101.0000.0000", "0101.0000.0001", "0101.0001.0001")), 	"0101", "0000", "0000", true },
			{ geografiSpesifikasjon(emptyList(), emptyList(), asList("0101.0000.0000", "0101.0000.0001", "0101.0001.0001")), 	"0101", "0000", "0001", true },
			{ geografiSpesifikasjon(emptyList(), emptyList(), asList("0101.0000.0000", "0101.0000.0001", "0101.0001.0001")), 	"0101", "0000", "0002", false },
			{ geografiSpesifikasjon(emptyList(), emptyList(), asList("0101.0000.0000", "0101.0000.0001", "0101.0001.0001")), 	"0101", "0001", "0000", false },
			{ geografiSpesifikasjon(emptyList(), emptyList(), asList("0101.0000.0000", "0101.0000.0001", "0101.0001.0001")), 	"0101", "0001", "0001", true },
			{ geografiSpesifikasjon(emptyList(), emptyList(), asList("0101.0000.0000", "0101.0000.0001", "0101.0001.0001")), 	"0101", "0002", "0000", false },
			{ geografiSpesifikasjon(emptyList(), emptyList(), asList("0101.0000.0000", "0101.0000.0001", "0101.0001.0001")), 	"0101", "0002", "0001", false }
		};
	}

}
