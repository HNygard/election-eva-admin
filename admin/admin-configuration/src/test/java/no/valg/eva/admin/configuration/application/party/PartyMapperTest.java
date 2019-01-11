package no.valg.eva.admin.configuration.application.party;

import no.evote.security.UserData;
import no.valg.eva.admin.backend.common.repository.LocaleTextRepository;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.configuration.model.party.Parti;
import no.valg.eva.admin.common.configuration.model.party.Partikategori;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.MvAreaDigest;
import no.valg.eva.admin.configuration.domain.model.Party;
import no.valg.eva.admin.configuration.domain.model.PartyCategory;
import no.valg.eva.admin.configuration.domain.model.PartyContestArea;
import no.valg.eva.admin.configuration.repository.MvAreaRepository;
import no.valg.eva.admin.configuration.repository.party.PartyCategoryRepository;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PartyMapperTest {

	private static final String ID_STORTINGSPARTI = "1";
	private static final String ID_LOKALT_PARTI = "3";
	private static final int INGEN_OMRADETILKNYTNINGER = 0;
	private static final int OMRADETILKNYTNING = 1;
	private static final int SHORT_CODE = 1234;

	@Test
	public void toParty_returnsParty() {
		PartyCategoryRepository fakePartyCategoryRepository = mock(PartyCategoryRepository.class);
		LocaleTextRepository fakeLocaleTextRepository = mock(LocaleTextRepository.class);
		MvAreaRepository fakeMvAreaRepository = mock(MvAreaRepository.class);
		PartyMapper partyMapper = new PartyMapper(fakePartyCategoryRepository, fakeLocaleTextRepository, fakeMvAreaRepository);
		
		PartyCategory partyCategory = new PartyCategory();
		when(fakePartyCategoryRepository.findById(Partikategori.STORTING.getId())).thenReturn(partyCategory);
		Parti parti = new Parti(Partikategori.STORTING, "V");
		parti.setOversattNavn("Venstre");
		ElectionEvent electionEvent = new ElectionEvent();
		
		Party party = partyMapper.toParty(parti, electionEvent);

		assertThat(party.getId()).isEqualTo(parti.getId());
		assertThat(party.getPartyCategory()).isEqualTo(partyCategory);
		assertThat(party.getElectionEvent()).isEqualTo(electionEvent);
		assertThat(party.getTranslatedPartyName()).isEqualTo("Venstre");
	}

	@Test
	public void updateParty_returnsParty() {
		PartyCategoryRepository fakePartyCategoryRepository = mock(PartyCategoryRepository.class);
		LocaleTextRepository fakeLocaleTextRepository = mock(LocaleTextRepository.class);
		MvAreaRepository fakeMvAreaRepository = mock(MvAreaRepository.class);
		PartyMapper partyMapper = new PartyMapper(fakePartyCategoryRepository, fakeLocaleTextRepository, fakeMvAreaRepository);

		PartyCategory partyCategory = new PartyCategory();
		when(fakePartyCategoryRepository.findById(Partikategori.LANDSDEKKENDE.getId())).thenReturn(partyCategory);
		Party party = new Party();
		Parti parti = new Parti(Partikategori.LANDSDEKKENDE, "KRF");
		parti.setOversattNavn("KRF");

		partyMapper.updateParty(party, parti);

		assertThat(party.getId()).isEqualTo(parti.getId());
		assertThat(party.getTranslatedPartyName()).isEqualTo(parti.getOversattNavn());
		assertThat(party.getPartyCategory()).isEqualTo(partyCategory);
	}

	@DataProvider
	public Object[][] partiData() {
		return new Object[][] {
				{ID_STORTINGSPARTI, INGEN_OMRADETILKNYTNINGER},	
				{ID_LOKALT_PARTI, OMRADETILKNYTNING}	
		};
	}
	
	@Test(dataProvider = "partiData")
	public void toParti_returnsPartiMedForventetAntallOmradetilknytninger(String id, int antallOmradetilknytninger) {
		LocaleTextRepository fakeLocaleTextRepository = mock(LocaleTextRepository.class, RETURNS_DEEP_STUBS);
		MvAreaRepository fakeMvAreaRepository = mock(MvAreaRepository.class);
		when(fakeMvAreaRepository.findSingleDigestByPath(any(AreaPath.class))).thenReturn(new MvAreaDigest());
		PartyMapper partyMapper = new PartyMapper(mock(PartyCategoryRepository.class), fakeLocaleTextRepository, fakeMvAreaRepository);

		PartyCategory partyCategory = new PartyCategory();
		partyCategory.setId(id);
		Party party = new Party("A", SHORT_CODE, partyCategory, null);
		party.setPk(1L);
		party.setForenkletBehandling(true);
		party.setApproved(true);
		party.getPartyContestAreas().add(new PartyContestArea(party, null, null, "030102"));
		UserData fakeUserData = mock(UserData.class, RETURNS_DEEP_STUBS);
		when(fakeUserData.getElectionEventId()).thenReturn("150001");
		when(fakeLocaleTextRepository.findByElectionEventLocaleAndTextId(anyLong(), anyLong(), anyString()).getLocaleText()).thenReturn("oversettelse");

		Parti parti = partyMapper.toParti(fakeUserData, party);
		assertThat(parti.getId()).isEqualTo(party.getId());
		assertThat(parti.isForenkletBehandling()).isEqualTo(party.isForenkletBehandling());
		assertThat(parti.isGodkjent()).isEqualTo(party.isApproved());
		assertThat(parti.getPartikategori().getId()).isEqualTo(party.getPartyCategory().getId());
		assertThat(parti.getPartikode()).isEqualTo(party.getShortCode());
		assertThat(parti.getPartyPk()).isEqualTo(party.getPk());
		assertThat(parti.getOversattNavn()).isEqualTo("oversettelse");
		assertThat(parti.getOmrader()).hasSize(antallOmradetilknytninger);
	}
}
