package no.valg.eva.admin.frontend.stemmegivning.ctrls;

import no.evote.service.voting.VotingService;
import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.felles.sti.valghierarki.ValggruppeSti;
import org.testng.annotations.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

public class FaVotingsSentFromMunicipalityControllerTest extends BaseFrontendTest {

	@Test
	public void doInit_verifyState() throws Exception {
		FaVotingsSentFromMunicipalityController ctrl = initializeMocks(FaVotingsSentFromMunicipalityController.class);
		setUserDataElection(ELECTION_PATH_ELECTION_GROUP);
		setUserDataArea(AREA_PATH_MUNICIPALITY);

		ctrl.init();

		verify(getInjectMock(VotingService.class)).findForeignEarlyVotingsSentFromMunicipality(
                eq(getUserDataMock()), any(ValggruppeSti.class), any());
	}
}
