package no.valg.eva.admin.configuration.domain.service;

import static no.valg.eva.admin.common.test.data.ElectionPathTestData.ELECTION_PATH_111111_11_11_111111;
import static no.valg.eva.admin.felles.test.data.sti.valghierarki.ValgdistriktStiTestData.VALGDISTRIKT_STI;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;

import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.configuration.domain.model.ContestArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.configuration.repository.MvElectionRepository;

import org.testng.annotations.Test;

public class ContestAreaDomainServiceTest {

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void contestAreasFor_givenNotContestPath_throwsException() throws Exception {
		new ContestAreaDomainService(null).contestAreasFor(ElectionPath.from("111111.11.11"));
	}

	@Test
	public void contestAreasFor_givenContestPath_returnsContestAreas() throws Exception {
		MvElection mvElection = mock(MvElection.class, RETURNS_DEEP_STUBS);
		MvElectionRepository mvElectionRepository = mock(MvElectionRepository.class);
		when(mvElectionRepository.finnEnkeltMedSti(VALGDISTRIKT_STI)).thenReturn(mvElection);
		Collection<ContestArea> contestAreas = new ContestAreaDomainService(mvElectionRepository).contestAreasFor(ELECTION_PATH_111111_11_11_111111);
		assertThat(contestAreas).isSameAs(mvElection.getContest().getContestAreaSet());
	}
}
