package no.valg.eva.admin.configuration.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.Test;

public class PartyTest {
	private static final String ID_STORTINGSPARTI = "1";

	private static final String ID_LOKALT_PARTI = "3";
	@Test
	public void setId_updatesNameAlso() {
		Party party = new Party();
		party.setId("AP");
		assertThat(party.getName()).isEqualTo("@party[AP].name");
	}

	@Test
	public void partyContestAreasForPartyCategory_lokaltParti_partyContestAreasBlirReturnert() {
		assertThat(makeParty(ID_LOKALT_PARTI).partyContestAreasForPartyCategory()).hasSize(1);
	}

	@Test
	public void partyContestAreasForPartyCategory_ikkeLokaltParti_tomtSettMedPartyContestAreasBlirReturnert() {
		assertThat(makeParty(ID_STORTINGSPARTI).partyContestAreasForPartyCategory()).isEmpty();
	}

	private Party makeParty(String id) {
		Party party = new Party();
		party.setPartyCategory(makePartyCategory(id));
		party.getPartyContestAreas().add(new PartyContestArea(party, null, "0301", null));
		return party;
	}

	private PartyCategory makePartyCategory(String kategoriId) {
		PartyCategory partyCategory = new PartyCategory();
		partyCategory.setId(kategoriId);
		return partyCategory;
	}

	@Test
	public void isLokaltParti_gittPartyCategoryForLokaltParti_returnererTrue() throws Exception {
		Party party = makeParty("3");
		assertThat(party.isLokaltParti()).isTrue();
	}

	@Test
	public void isLokaltParti_gittPartyCategoryForStortingsparti_returnererFalse() throws Exception {
		Party party = makeParty("1");
		assertThat(party.isLokaltParti()).isFalse();
	}

	@Test
	public void isLokaltParti_gittPartyCategoryForLandsdekkendeParti_returnererFalse() throws Exception {
		Party party = makeParty("2");
		assertThat(party.isLokaltParti()).isFalse();
	}
}
