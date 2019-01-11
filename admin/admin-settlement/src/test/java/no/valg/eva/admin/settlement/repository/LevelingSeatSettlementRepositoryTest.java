package no.valg.eva.admin.settlement.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import no.valg.eva.admin.settlement.domain.model.LevelingSeat;
import no.valg.eva.admin.test.MockUtilsTestCase;

import org.testng.annotations.Test;



public class LevelingSeatSettlementRepositoryTest extends MockUtilsTestCase {

	@Test
	public void distributeLevelingSeats_withValidUser_verifySQL() throws Exception {
		LevelingSeatSettlementRepository repository = initializeMocks(LevelingSeatSettlementRepository.class);
		Query query = createMock(Query.class);
		when(getInjectMock(EntityManager.class).createNativeQuery("/* NO LOAD BALANCE */select leveling_seat_settlement(?)")).thenReturn(query);
		when(query.getSingleResult()).thenReturn(1);

		boolean result = repository.distributeLevelingSeats(65L);

		assertThat(result).isTrue();
		verify(query).setParameter(1, 65);
	}

	@Test
	public void findLevelingSeatsByElectionPk_withElection_verifySQL() throws Exception {
		LevelingSeatSettlementRepository repository = initializeMocks(LevelingSeatSettlementRepository.class);
		TypedQuery query = createMock(TypedQuery.class);
		when(getInjectMock(EntityManager.class).createNamedQuery("LevelingSeat.findByElectionPk", LevelingSeat.class)).thenReturn(query);

		repository.findLevelingSeatsByElectionPk(45L);

		verify(query).getResultList();
		verify(query).setParameter("electionPk", 45L);
	}

	@Test
	public void deleteLevelingSeatSettlement_withElection_verifySQL() throws Exception {
		LevelingSeatSettlementRepository repository = initializeMocks(LevelingSeatSettlementRepository.class);
		Query query = createMock(Query.class);
		when(getInjectMock(EntityManager.class).createNativeQuery("delete from leveling_seat_settlement where election_pk = ?")).thenReturn(query);

		repository.deleteLevelingSeatSettlement(35L);

		verify(query).executeUpdate();
		verify(query).setParameter(1, 35L);
	}

}

