package no.valg.eva.admin.configuration.domain.model;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import no.valg.eva.admin.common.AreaPath;

import org.testng.annotations.Test;

public class PartyContestAreaTest {

	public static final String ELECTION_PATH = "773400";
	
	@Test
	public void areaPath_withCounty_returnsExpectedCountyPath() throws Exception {
		PartyContestArea partyContestArea = new PartyContestArea();
		partyContestArea.setCountyId("07");
		AreaPath areaPath = new AreaPath("773400.47.07");
		assertThat(partyContestArea.areaPath(ELECTION_PATH)).isEqualTo(areaPath);
	}
	
	@Test
	public void areaPath_withMunicipality_returnsExpectedMunicipalityPath() throws Exception {
		PartyContestArea partyContestArea = new PartyContestArea();
		partyContestArea.setMunicipalityId("0701");
		AreaPath areaPath = new AreaPath("773400.47.07.0701");
		assertThat(partyContestArea.areaPath(ELECTION_PATH)).isEqualTo(areaPath);
	}
	
	@Test
	public void areaPath_withBorough_returnsExpectedBoroughPath() throws Exception {
		PartyContestArea partyContestArea = new PartyContestArea();
		partyContestArea.setBoroughId("070102");
		AreaPath areaPath = new AreaPath("773400.47.07.0701.070102");
		assertThat(partyContestArea.areaPath(ELECTION_PATH)).isEqualTo(areaPath);
	}
	
	@Test
    public void equals_medLikPartyMunicipalityIdOgCountyId_returnererTrue() {
        PartyContestArea aPartyContestArea = makePartyContestArea();
        PartyContestArea equalPartyContestArea = makePartyContestArea();
        assertThat(aPartyContestArea.equals(equalPartyContestArea)).isTrue();
    }

    private PartyContestArea makePartyContestArea() {
        PartyContestArea partyContestArea = new PartyContestArea();
        partyContestArea.setMunicipalityId("0701");
        partyContestArea.setParty(makeParty());
        return partyContestArea;
    }

    private Party makeParty() {
        Party party = new Party();
        party.setPk(1L);
        return party;
    }
}
