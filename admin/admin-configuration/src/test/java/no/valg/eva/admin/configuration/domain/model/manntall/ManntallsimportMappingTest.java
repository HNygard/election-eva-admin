package no.valg.eva.admin.configuration.domain.model.manntall;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.InputStream;

import no.valg.eva.admin.configuration.repository.ManntallsimportMappingRepository;

import org.apache.commons.io.IOUtils;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class ManntallsimportMappingTest {

	@Test
	public void klassen_vedSerialisering_girFinJson() throws Exception {
		ManntallsimportMapping manntallsimportMapping = getManntallsimportMapping();
		String serialisertVerdi = new GsonBuilder().setPrettyPrinting().create().toJson(manntallsimportMapping);
		String forventetVerdi = getManntallsimportMappingJson();
		assertThat(serialisertVerdi).isEqualTo(forventetVerdi);
	}

	private ManntallsimportMapping getManntallsimportMapping() {
		ManntallsimportMapping manntallsimportMapping = new ManntallsimportMapping();
		manntallsimportMapping.setMappedeOmraader(asList(
				getOmraadeMapping("123456.47.07.0722.072200.0000", "123456.47.07.0729.072900.0000"),
				getOmraadeMapping("123456.47.07.0722.072200.0001", "123456.47.07.0729.072900.0002"),
				getOmraadeMapping("123456.47.07.0722.072200.0003", "123456.47.07.0729.072900.0003"),
				getOmraadeMapping("123456.47.07.0722.072200.0004", "123456.47.07.0729.072900.0004"),
				getOmraadeMapping("123456.47.07.0722.072200.0005", "123456.47.07.0729.072900.0005"),
				getOmraadeMapping("123456.47.07.0722.072200.0007", "123456.47.07.0729.072900.0007"),
				getOmraadeMapping("123456.47.07.0722.072200.0008", "123456.47.07.0729.072900.0008"),
				getOmraadeMapping("123456.47.07.0723.072300.0000", "123456.47.07.0729.072900.0000"),
				getOmraadeMapping("123456.47.07.0723.072300.0001", "123456.47.07.0729.072900.0001")));
		return manntallsimportMapping;
	}

	private OmraadeMapping getOmraadeMapping(String from, String to) {
		return new OmraadeMapping(from, to);
	}

	private String getManntallsimportMappingJson() throws Exception {
		InputStream inputStream = this.getClass().getClassLoader()
				.getResourceAsStream(ManntallsimportMappingRepository.STANDARD_MANNTALLSIMPORT_MAPPING_FILNAVN);
		return IOUtils.toString(inputStream);
	}

	@Test
	public void klassen_vedGyldigJsonFil_leserInnData() throws Exception {
		String manntallsimportMappingJson = getManntallsimportMappingJson();
		ManntallsimportMapping manntallsimportMapping = new Gson().fromJson(manntallsimportMappingJson, ManntallsimportMapping.class);
		ManntallsimportMapping forventetManntallsimportMapping = getManntallsimportMapping();
		assertThat(manntallsimportMapping).isEqualTo(forventetManntallsimportMapping);
	}

	@Test
	public void equals_gittLikeObject_returnererTrue() {

		OmraadeMapping omraadeMapping = new OmraadeMapping("123", "456");
		ManntallsimportMapping manntallsimportMapping1 = manntallsimportMapping(omraadeMapping);
		ManntallsimportMapping manntallsimportMapping2 = manntallsimportMapping(omraadeMapping);
		assertThat(manntallsimportMapping1.equals(manntallsimportMapping2)).isTrue();
	}

	private ManntallsimportMapping manntallsimportMapping(OmraadeMapping... omraadeMappinger) {
		ManntallsimportMapping manntallsimportMapping = new ManntallsimportMapping();
		if (omraadeMappinger != null) {
			for (OmraadeMapping omraadeMapping : omraadeMappinger) {
				manntallsimportMapping.getMappedeOmraader().add(omraadeMapping);
			}
		}
		return manntallsimportMapping;
	}

	@Test
	public void equals_gittUlikeObject_returnererFalse() {
		ManntallsimportMapping manntallsimportMapping1 = manntallsimportMapping((OmraadeMapping) null);
		ManntallsimportMapping manntallsimportMapping2 = manntallsimportMapping(new OmraadeMapping("456", "123"));
		assertThat(manntallsimportMapping1.equals(manntallsimportMapping2)).isFalse();
	}

	@Test(dataProvider = "ikkeManntallsimportMapping")
	public void equals_gittNoeAnnetEnnManntallsnumre_returnererFalse(Object ikkeManntallsimportMapping) {
		ManntallsimportMapping manntallsimportMapping = new ManntallsimportMapping();
		assertThat(manntallsimportMapping.equals(ikkeManntallsimportMapping)).isFalse();
	}

	@DataProvider
	public Object[][] ikkeManntallsimportMapping() {
		return new Object[][] {
				{ null },
				{ "tullOgTøys" }
		};
	}

	@Test
	public void equals_gittSammeObjekt_returnererTrue() {
		ManntallsimportMapping manntallsimportMapping = new ManntallsimportMapping();
		assertThat(manntallsimportMapping.equals(manntallsimportMapping)).isTrue();
	}

	@Test
	public void hashCode_gittLikeObjeckter_returnererTrue() {
		ManntallsimportMapping manntallsimportMapping1 = manntallsimportMapping(new OmraadeMapping("123", "456"));
		ManntallsimportMapping manntallsimportMapping2 = manntallsimportMapping(new OmraadeMapping("123", "456"));
		assertThat(manntallsimportMapping1.hashCode()).isEqualTo(manntallsimportMapping2.hashCode());
	}
}
