package no.valg.eva.admin.settlement.repository;

import static java.math.BigInteger.ZERO;

import java.util.List;

import javax.persistence.Query;
import javax.persistence.TypedQuery;

import no.evote.security.UserData;
import no.valg.eva.admin.backend.common.repository.BaseRepository;
import no.valg.eva.admin.configuration.domain.model.Election;
import no.valg.eva.admin.settlement.domain.model.LevelingSeat;
import no.valg.eva.admin.settlement.domain.model.LevelingSeatSettlement;

public class LevelingSeatSettlementRepository extends BaseRepository {

	public boolean distributeLevelingSeats(long electionPk) {
		Query query = getEm().createNativeQuery("/* NO LOAD BALANCE */select leveling_seat_settlement(?)");
		query.setParameter(1, (int) electionPk);
		return !(query.getSingleResult()).equals(0);
	}

	public boolean areAllSettlementsInElectionFinished(Election election) {
		return areAllSettlementsInElectionFinished(election.getPk());
	}

	public boolean areAllSettlementsInElectionFinished(long electionPk) {
		String sqlQuery = "select count(*) "
				+ "from contest c "
				+ "where c.election_pk = ?1 "
				+ "and c.contest_id != '000000' "
				+ "and not exists ( "
				+ "select 1 "
				+ "from settlement s "
				+ "where s.contest_pk = c.contest_pk); ";
		Query query = getEm().createNativeQuery(sqlQuery);
		query.setParameter(1, electionPk);
		return (query.getSingleResult()).equals(ZERO);
	}

	public List<LevelingSeat> findLevelingSeatsByElection(Election election) {
		return findLevelingSeatsByElectionPk(election.getPk());
	}

	public List<LevelingSeat> findLevelingSeatsByElectionPk(long electionPk) {
		TypedQuery<LevelingSeat> query = getEm().createNamedQuery("LevelingSeat.findByElectionPk", LevelingSeat.class);
		query.setParameter("electionPk", electionPk);
		return query.getResultList();
	}

	public void deleteLevelingSeatSettlement(Election election) {
		deleteLevelingSeatSettlement(election.getPk());
	}

	public void deleteLevelingSeatSettlement(long electionPk) {
		Query query = getEm().createNativeQuery("delete from leveling_seat_settlement where election_pk = ?");
		query.setParameter(1, electionPk);
		query.executeUpdate();
	}

	public void create(UserData userData, LevelingSeatSettlement levelingSeatSettlement) {
		createEntity(userData, levelingSeatSettlement);
	}
}
