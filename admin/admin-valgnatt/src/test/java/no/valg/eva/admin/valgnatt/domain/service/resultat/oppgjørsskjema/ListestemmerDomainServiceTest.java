package no.valg.eva.admin.valgnatt.domain.service.resultat.oppgjørsskjema;

import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.settlement.repository.SettlementRepository;
import org.testng.annotations.Test;

import static no.evote.constants.AreaLevelEnum.COUNTY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ListestemmerDomainServiceTest {

	@Test
	public void finnListestemmer_ikkeValgPåKommunenivå_returnererTomtSet() {

		ListestemmerDomainService listestemmerDomainService = new ListestemmerDomainService(null);

		MvElection mvElectionContest = mock(MvElection.class, RETURNS_DEEP_STUBS);
		when(mvElectionContest.getElection().getAreaLevel()).thenReturn(COUNTY.getLevel());

		assertThat(listestemmerDomainService.finnListestemmer(mvElectionContest)).isEmpty();
	}

	@Test
	public void finnListestemmer_valgPåKommunenivå_returnererSetMedListestemmer() {
		SettlementRepository fakeSettlementRepo = mock(SettlementRepository.class, RETURNS_DEEP_STUBS);
		ListestemmerDomainService listestemmerDomainService = new ListestemmerDomainService(fakeSettlementRepo);
		MvElection mvElection = mock(MvElection.class, RETURNS_DEEP_STUBS);
		when(mvElection.getElection().isWritein()).thenReturn(true);

		listestemmerDomainService.finnListestemmer(mvElection);

		verify(fakeSettlementRepo, times(1)).findSettlementByContest(anyLong());
	}
}
