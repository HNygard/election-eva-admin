package no.valg.eva.admin.configuration.domain.service;

import no.valg.eva.admin.common.configuration.model.GeografiSpesifikasjon;
import no.valg.eva.admin.configuration.domain.model.MvAreaDigest;
import no.valg.eva.admin.configuration.repository.MvAreaRepository;
import no.valg.eva.admin.test.MockUtilsTestCase;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class GeografiSpesifikasjonDomainServiceTest extends MockUtilsTestCase {

	private GeografiSpesifikasjonDomainService geografiSpesifikasjonDomainService;

	@BeforeTest
	public void setup() throws Exception {
		this.geografiSpesifikasjonDomainService = initializeMocks(GeografiSpesifikasjonDomainService.class);
	}
	
	@Test
    public void lagGeografiSpeisifkasjonForKommuner_returnererListeOverAlleKommuner() {
		when(getInjectMock(MvAreaRepository.class).findDigestsByPathAndLevel(any(), any())).thenReturn(lagKommuneListe());

		GeografiSpesifikasjon geografiSpesifikasjon = geografiSpesifikasjonDomainService.lagGeografiSpesifikasjonForKommuner("111111");

		assertThat(geografiSpesifikasjon).isEqualTo(lagGeografiSpesifikasjonKommuner());
	}
	
	private List<MvAreaDigest> lagKommuneListe() {
		List<MvAreaDigest> kommuneListe = new ArrayList<>();
		kommuneListe.add(new MvAreaDigest("111111.22.02.0201"));
		kommuneListe.add(new MvAreaDigest("111111.22.03.0301"));
		return kommuneListe;
	}
	
	private GeografiSpesifikasjon lagGeografiSpesifikasjonKommuner() {
		return new GeografiSpesifikasjon(asList("0201", "0301"), emptyList());
	}

	@Test
    public void lagGeografiSpeisifkasjonForKretser_returnererListeOverAlleKretser() {
		when(getInjectMock(MvAreaRepository.class).findDigestsByPathAndLevel(any(), any())).thenReturn(lagKretsListe());

		GeografiSpesifikasjon geografiSpesifikasjon = geografiSpesifikasjonDomainService.lagGeografiSpesifikasjonForKretser("111111");

		assertThat(geografiSpesifikasjon).isEqualTo(lagGeografiSpesifikasjonKretser());
	}

	private List<MvAreaDigest> lagKretsListe() {
		List<MvAreaDigest> kretsListe = new ArrayList<>();
		kretsListe.add(new MvAreaDigest("111111.22.02.0201.020100.0000"));
		kretsListe.add(new MvAreaDigest("111111.22.02.0201.020100.0001"));
		kretsListe.add(new MvAreaDigest("111111.22.02.0201.020100.0002"));
		kretsListe.add(new MvAreaDigest("111111.22.02.0201.020100.0003"));
		kretsListe.add(new MvAreaDigest("111111.22.02.0201.020100.0004"));
		kretsListe.add(new MvAreaDigest("111111.22.02.0201.020100.0005"));
		kretsListe.add(new MvAreaDigest("111111.22.03.0301.030100.0000"));
		kretsListe.add(new MvAreaDigest("111111.22.03.0301.030100.0001"));
		kretsListe.add(new MvAreaDigest("111111.22.03.0301.030100.0002"));
		kretsListe.add(new MvAreaDigest("111111.22.03.0301.030100.0003"));
		kretsListe.add(new MvAreaDigest("111111.22.04.0401.040100.0000"));
		kretsListe.add(new MvAreaDigest("111111.22.04.0401.040100.0001"));
		return kretsListe;
	}
	
	private GeografiSpesifikasjon lagGeografiSpesifikasjonKretser() {
		return new GeografiSpesifikasjon(emptyList(), asList(
			"0201.0000", "0201.0001", "0201.0002", "0201.0003", "0201.0004", "0201.0005",
			"0301.0000", "0301.0001", "0301.0002", "0301.0003",
			"0401.0000", "0401.0001"));
	}

	@Test
    public void lagGeografiSpeisifkasjonForBegrensetAntallKretser_gittMaksAntallKretserPerKommune_returnererListeMedRedusertAntallKretser() {
		when(getInjectMock(MvAreaRepository.class).findDigestsByPathAndLevel(any(), any())).thenReturn(lagKretsListe());

		GeografiSpesifikasjon geografiSpesifikasjon = geografiSpesifikasjonDomainService.lagGeografiSpesifikasjonForBegrensetAntallKretser("111111", 2);

		assertThat(geografiSpesifikasjon).isEqualTo(lagGeografiSpesifikasjonBegrensetAntallKretser());
	}

	private GeografiSpesifikasjon lagGeografiSpesifikasjonBegrensetAntallKretser() {
		return new GeografiSpesifikasjon(emptyList(), asList(
			"0201.0000", "0201.0001", "0201.0002",
			"0301.0000", "0301.0001", "0301.0002",
			"0401.0000", "0401.0001"));
	}

	@Test
    public void lagGeografiSpeisifkasjonForStemmesteder_returnererListeMedAlleStemmesteder() {
		when(getInjectMock(MvAreaRepository.class).findDigestsByPathAndLevel(any(), any())).thenReturn(lagStemmestedListe());

		GeografiSpesifikasjon geografiSpesifikasjon = geografiSpesifikasjonDomainService.lagGeografiSpesifikasjonForStemmesteder("111111");

		assertThat(geografiSpesifikasjon).isEqualTo(lagGeografiSpesifikasjonStemmesteder());
	}

	private List<MvAreaDigest> lagStemmestedListe() {
		List<MvAreaDigest> stemmestedListe = new ArrayList<>();
		stemmestedListe.add(new MvAreaDigest("111111.22.02.0201.020100.0004.0000"));
		stemmestedListe.add(new MvAreaDigest("111111.22.02.0201.020100.0004.0001"));
		stemmestedListe.add(new MvAreaDigest("111111.22.02.0201.020100.0004.0002"));
		stemmestedListe.add(new MvAreaDigest("111111.22.02.0201.020100.0004.0003"));
		return stemmestedListe;
	}

	private GeografiSpesifikasjon lagGeografiSpesifikasjonStemmesteder() {
		return new GeografiSpesifikasjon(emptyList(), emptyList(), asList(
			"0201.0004.0000", "0201.0004.0001", "0201.0004.0002", "0201.0004.0003"));
	}

}
