package no.valg.eva.admin.configuration.application.party;

import no.evote.security.UserData;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.Party;
import no.valg.eva.admin.configuration.repository.MvElectionRepository;
import no.valg.eva.admin.configuration.repository.party.PartyRepository;
import no.valg.eva.admin.felles.sti.valghierarki.ValgdistriktSti;
import no.valg.eva.admin.test.MockUtilsTestCase;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PartiApplicationServiceTest extends MockUtilsTestCase {
	
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void partierUtenListeforslag_electionPathIkkeTilContest_illegalArgumentException() throws Exception {
		PartiApplicationService service = initializeMocks(PartiApplicationService.class);

		ElectionPath electionPath = ElectionPath.from("150001.01.01");
		service.partierUtenListeforslag(mock(UserData.class), electionPath);
	}

	@Test
	public void partierUtenListeforslag_henterFraRepositories() throws Exception {
		PartiApplicationService service = initializeMocks(PartiApplicationService.class);

		ValgdistriktSti valgdistriktSti = new ValgdistriktSti("150001", "01", "01", "000001");
		when(getInjectMock(MvElectionRepository.class).finnEnkeltMedSti(valgdistriktSti).getContest()).thenReturn(mock(Contest.class));
		when(getInjectMock(PartyRepository.class).getPartyWithoutAffiliationList(any(Contest.class))).thenReturn(makePartyList());
		
		service.partierUtenListeforslag(mock(UserData.class), valgdistriktSti.electionPath());
	}

	private List<Party> makePartyList() {
		List<Party> parties = new ArrayList<>();
		parties.add(makeParty());
		return parties;
	}

	private Party makeParty() {
		return new Party();
	}

}
