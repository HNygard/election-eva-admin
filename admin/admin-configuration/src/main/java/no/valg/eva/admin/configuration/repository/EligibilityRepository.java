package no.valg.eva.admin.configuration.repository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import no.evote.model.views.Eligibility;
import no.valg.eva.admin.backend.common.repository.BaseRepository;
import no.valg.eva.admin.common.MunicipalityId;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.MunicipalityAgeLimit;
import no.valg.eva.admin.configuration.domain.model.Voter;

import org.joda.time.LocalDate;

public class EligibilityRepository extends BaseRepository {
	private static final String ELECTION_GROUP_PK = "electionGroupPk";
	private static final String MV_AREA_PK = "mvAreaPk";
	private static final String ELIGIBLE = "eligible";

	public EligibilityRepository() {
	}

	public EligibilityRepository(EntityManager entityManager) {
		super(entityManager);
	}

	/**
	 * It is the same as findEligibilityForVoterInGroup but it does not check on date of birth
	 */
	public List<Eligibility> findTheoreticalEligibilityForVoterInGroup(Voter voter, Long electionGroupPk) {
		TypedQuery<Eligibility> query = getEm()
				.createNamedQuery("Eligibility.findTheoreticalEligibilityForVoterInGroup", Eligibility.class)
				.setParameter(ELECTION_GROUP_PK, electionGroupPk)
				.setParameter(ELIGIBLE, voter.isEligible());
		if (voter.getMvArea() != null) {
			query.setParameter(MV_AREA_PK, voter.getMvArea().getPk());
		} else {
			query.setParameter(MV_AREA_PK, null);
		}
		return query.getResultList();
	}

	/**
	 * @return the maximum end birthdate for each municipality in a given election event
     */
	public Map<MunicipalityId, LocalDate> findMaxEndBirthDateForEachMunicipalityInElectionEvent(ElectionEvent electionEvent) {
		TypedQuery<MunicipalityAgeLimit> query = getEm()
				.createNamedQuery("Eligibility.findMaxEndDateOfBirth", MunicipalityAgeLimit.class)
				.setParameter(0, electionEvent.getPk());
		List<MunicipalityAgeLimit> queryResultList = query.getResultList();

		return queryResultList.stream()
			.collect(Collectors.toMap(MunicipalityAgeLimit::getMunicipalityId, MunicipalityAgeLimit::getMustBeBornBefore));
	}
}
