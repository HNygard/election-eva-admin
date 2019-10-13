package no.valg.eva.admin.settlement.repository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import no.evote.dto.CandidateVoteCountDto;
import no.evote.security.UserData;
import no.valg.eva.admin.backend.common.repository.BaseRepository;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.settlement.domain.model.AffiliationVoteCount;
import no.valg.eva.admin.settlement.domain.model.CandidateSeat;
import no.valg.eva.admin.settlement.domain.model.Settlement;

@Default
@ApplicationScoped
public class SettlementRepository extends BaseRepository {
	private static final String SETTLEMENT_PK_QUERY = "select settlement_pk from admin.settlement where contest_pk = ?1";

	public SettlementRepository() {
		// brukes av Weld / CDI
	}

	public SettlementRepository(EntityManager entityManager) {
		super(entityManager);
	}

	public Settlement create(UserData userData, Settlement settlement) {
		return createEntity(userData, settlement);
	}

	public void deleteSettlements(ElectionPath electionPath, AreaPath areaPath) {
		String queryString = "/* NO LOAD BALANCE */select delete_settlements(?, ?)";
		Query query = getEm().createNativeQuery(queryString);
		query.setParameter(1, electionPath.path());
		query.setParameter(2, areaPath.path());
		query.getSingleResult();
	}

	public boolean erValgoppgjørKjørt(Contest contest) {
		Query query = getEm().createNativeQuery(SETTLEMENT_PK_QUERY);
		query.setParameter(1, contest.getPk());
		return !query.getResultList().isEmpty();
	}


	public Settlement findSettlementByContest(Contest contest) {
		return findSettlementByContest(contest.getPk());
	}

	public Settlement findSettlementByContest(Long contestPk) {
		try {
			return getEm().createNamedQuery("Settlement.findByContest", Settlement.class).setParameter("contestPk", contestPk).getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	public List<AffiliationVoteCount> findAffiliationVoteCountsBySettlement(Settlement settlement) {
		return findAffiliationVoteCountsBySettlement(settlement.getPk());
	}

	public List<AffiliationVoteCount> findAffiliationVoteCountsBySettlement(Long settlementPk) {
		return getEm()
				.createNamedQuery("AffiliationVoteCount.findBySettlement", AffiliationVoteCount.class)
				.setParameter("settlementPk", settlementPk)
				.getResultList();
	}

	public Map<Long, List<CandidateVoteCountDto>> findCandidateVoteCountsBySettlement(Settlement settlement) {
		return findCandidateVoteCountsBySettlement(settlement.getPk());
	}

	@SuppressWarnings("unchecked")
	public Map<Long, List<CandidateVoteCountDto>> findCandidateVoteCountsBySettlement(Long settlementPk) {
		String sqlQuerySumBallot = "SELECT "
				+ "  a.affiliation_pk, "
				+ "  can.candidate_pk, "
				+ "  can.first_name, "
				+ "  can.last_name, "
				+ "  can.display_order, "
				+ "  cr.rank_number, "
				+ "  vc.vote_category_id, "
				+ "  COALESCE(SUM(cvc.votes), 0.0) "
				+ "FROM settlement s "
				+ "JOIN contest c "
				+ "  ON (c.contest_pk = s.contest_pk) "
				+ "JOIN election e "
				+ "  ON (e.election_pk = c.election_pk) "
				+ "JOIN ballot b "
				+ "  ON (b.contest_pk = s.contest_pk) "
				+ "JOIN affiliation a "
				+ "  ON (a.ballot_pk = b.ballot_pk) "
				+ "JOIN candidate can "
				+ "  ON (can.affiliation_pk = a.affiliation_pk) "
				+ "JOIN candidate_rank cr "
				+ "  ON (cr.settlement_pk = s.settlement_pk "
				+ "  AND cr.candidate_pk = can.candidate_pk) "
				+ "LEFT JOIN vote_category vc " // Changed from JOIN to LEFT JOIN to fix #8825
				+ "  ON ((e.renumber OR vc.vote_category_id != 'renumber') "
				+ "  AND (e.baseline_vote_factor IS NOT null OR vc.vote_category_id != 'baseline') "
				+ "  AND (e.personal OR vc.vote_category_id != 'personal') "
				+ "  AND (e.strikeout OR vc.vote_category_id != 'strikeout') "
				+ "  AND (e.writein OR vc.vote_category_id != 'writein')) "
				+ "LEFT JOIN candidate_vote_count cvc "
				+ "  ON (cvc.settlement_pk = s.settlement_pk "
				+ "  AND cvc.candidate_pk = can.candidate_pk "
				+ "  AND cvc.vote_category_pk = vc.vote_category_pk) "
				+ "WHERE "
				+ "  s.settlement_pk = ?1 "
				+ "GROUP BY "
				+ "  a.affiliation_pk, "
				+ "  a.display_order, "
				+ "  can.candidate_pk, "
				+ "  can.first_name, "
				+ "  can.last_name, "
				+ "  can.display_order, "
				+ "  cr.rank_number, "
				+ "  vc.vote_category_id "
				+ "ORDER BY "
				+ "  a.display_order, "
				+ "  cr.rank_number, "
				+ "  vc.vote_category_id ";

		Query query = getEm().createNativeQuery(sqlQuerySumBallot);
		query.setParameter(1, settlementPk);
		List<Object[]> list = query.getResultList();

		List<CandidateVoteCountDto> candidateVoteCountDtoList = new ArrayList<>();

		
		long oldCandidatePk = -1;
		for (Object[] objects : list) {
			long candidatePk = ((Integer) objects[1]).longValue();
			BigDecimal sum = (BigDecimal) objects[7];
			String voteCategoryId = (String) objects[6];

			if (candidatePk != oldCandidatePk) {
				Long affiliationPk = ((Integer) objects[0]).longValue();
				int rankNumber = (Integer) objects[5];

				String firstName = (String) objects[2];
				String lastName = (String) objects[3];
				Integer displayOrder = (Integer) objects[4];

				candidateVoteCountDtoList.add(new CandidateVoteCountDto(candidatePk, firstName, lastName, affiliationPk, rankNumber, displayOrder, sum
						.doubleValue(), voteCategoryId));
			} else {
				candidateVoteCountDtoList.get(candidateVoteCountDtoList.size() - 1).setVotes(sum.doubleValue(), voteCategoryId);
			}
			oldCandidatePk = candidatePk;
		}
		

		Map<Long, List<CandidateVoteCountDto>> candidateVoteCountMap = new HashMap<>();
		List<CandidateVoteCountDto> localList = new ArrayList<>();

		boolean hasCandidateVotes = !candidateVoteCountDtoList.isEmpty();
		long oldAffiliationPk = (hasCandidateVotes) ? candidateVoteCountDtoList.get(0).getAffiliationPk() : -1;

		for (CandidateVoteCountDto candidateVoteCountDto : candidateVoteCountDtoList) {
			long affiliationPk = candidateVoteCountDto.getAffiliationPk();
			if (hasCandidateVotes && affiliationPk != oldAffiliationPk) {
				candidateVoteCountMap.put(oldAffiliationPk, localList);
				oldAffiliationPk = affiliationPk;
				localList = new ArrayList<>();
			}
			localList.add(candidateVoteCountDto);
		}
		candidateVoteCountMap.put(oldAffiliationPk, localList);

		return candidateVoteCountMap;
	}

	public List<CandidateSeat> findAffiliationCandidateSeatsBySettlement(Settlement settlement) {
		return findAffiliationCandidateSeatsBySettlement(settlement.getPk());
	}

	public List<CandidateSeat> findAffiliationCandidateSeatsBySettlement(Long settlementPk) {
		return getEm()
				.createNamedQuery("CandidateSeat.findBySettlement", CandidateSeat.class)
				.setParameter("settlementPk", settlementPk)
				.getResultList();
	}

	public List<Integer> findMandatesBySettlement(Settlement settlement) {
		return findMandatesBySettlement(settlement.getPk());
	}

	@SuppressWarnings("unchecked")
	public List<Integer> findMandatesBySettlement(Long settlementPk) {
		String sqlQuery = "SELECT CAST(COUNT(cs.*) as integer) FROM affiliation a "
				+ "LEFT OUTER JOIN candidate_seat cs ON cs.affiliation_pk = a.affiliation_pk AND cs.elected = true "
				+ "JOIN settlement s on s.settlement_pk = ?1 "
				+ "JOIN ballot b ON a.ballot_pk = b.ballot_pk AND b.contest_pk = s.contest_pk AND b.ballot_id != 'BLANK' AND b.approved = TRUE "
				+ "GROUP BY a.affiliation_pk, a.display_order "
				+ "ORDER BY a.display_order";
		Query query = getEm().createNativeQuery(sqlQuery);
		query.setParameter(1, settlementPk);
		return query.getResultList();
	}
}
