package no.valg.eva.admin.configuration.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.manntall.OmraadeMapping;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

@SuppressWarnings("unused")

public class ManntallsimportMappingRepositoryTest {

	@Test(dataProvider = "antallMappingerPrValghendelse")
	public void finnForValghendelse_forEtRegelsettFraClasspath_returnerReglerForValghendelsen(ElectionEvent electionEvent, int forventetAntallMappinger) {
		ManntallsimportMappingRepository manntallsimportMappingRepository = new ManntallsimportMappingRepository();
		sjekkMappinger(electionEvent, forventetAntallMappinger, manntallsimportMappingRepository);
	}

	@DataProvider
	private Object[][] antallMappingerPrValghendelse() {
		return new Object[][] {
				{ valghendelse("123456"), 9 },
				{ valghendelse("111111"), 0 }
		};
	}

	private ElectionEvent valghendelse(String valghendelsesId) {
		return new ElectionEvent(valghendelsesId, null, null);
	}

	private void sjekkMappinger(ElectionEvent electionEvent, int forventetAntallMappinger, ManntallsimportMappingRepository manntallsimportMappingRepository) {
		List<OmraadeMapping> manntallsimportMappinger = manntallsimportMappingRepository.finnForValghendelse(electionEvent);
		assertThat(manntallsimportMappinger.size()).isEqualTo(forventetAntallMappinger);
	}

	@Test(dataProvider = "antallMappingerPrValghendelse")
	public void finnForValghendelse_forEtRegelsettFraFil_returnerReglerForValghendelsen(ElectionEvent electionEvent, int forventetAntallMappinger) {
		String filnavn = this.getClass().getClassLoader().getResource(ManntallsimportMappingRepository.STANDARD_MANNTALLSIMPORT_MAPPING_FILNAVN).getFile();
		ManntallsimportMappingRepository manntallsimportMappingRepository = new ManntallsimportMappingRepository(filnavn);
		sjekkMappinger(electionEvent, forventetAntallMappinger, manntallsimportMappingRepository);
	}

	@Test
	public void finnForValghendelse_hvisMappingfilIkkeFinnes_returnererTomListe() {
		ManntallsimportMappingRepository manntallsimportMappingRepository = new ManntallsimportMappingRepository("fil-som-ikke-finnes.json");
		sjekkMappinger(valghendelse("123456"), 0, manntallsimportMappingRepository);
	}
}

