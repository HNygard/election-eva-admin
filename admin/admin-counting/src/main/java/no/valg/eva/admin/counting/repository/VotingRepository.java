package no.valg.eva.admin.counting.repository;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.persistence.EntityManager;

import no.valg.eva.admin.backend.common.repository.BaseRepository;
import no.valg.eva.admin.configuration.domain.model.Borough;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.configuration.domain.model.PollingDistrict;
import no.valg.eva.admin.configuration.domain.model.VotingCategory;
import no.valg.eva.admin.voting.domain.model.Voting;

import org.joda.time.DateTime;

/**
 * Note: This class is currently located in the counting module. We should consider moving it to the voting module in the future
 */
public class VotingRepository extends BaseRepository {

	private static final String VOTING_CATEGORY_IDS = "votingCategoryIds";
	private static final String LATE_VALIDATION_FILTER = "lateValidationFilter";
	private static final int PARAM_1_ELECTION_EVENT_PK = 1;
	private static final int PARAM_2_CONTEST_PK = 2;
	private static final int PARAM_3_LATE_VALIDATION = 3;

	public VotingRepository() {
	}

	VotingRepository(final EntityManager entityManager) {
		super(entityManager);
	}

	/**
	 * Note this method returns a VotingCategory object. In the future - following the pattern given by CountingCodeValueRepository, we should consider moving
	 * this method to a VotingCodeValueRepository in the voting module
	 */
	public VotingCategory findVotingCategoryById(final String id) {
		return super.findEntityById(VotingCategory.class, id);
	}

	public long findApprovedVotingCountByPollingDistrictAndCategoriesAndLateValidation(
			PollingDistrict pollingDistrict,
			no.valg.eva.admin.common.voting.VotingCategory[] categories,
			boolean lateValidation) {
		return getEm()
				.createNamedQuery("Voting.findApprovedVotingCountByPollingDistrictAndCategoriesAndLateValidation", Long.class)
				.setParameter("pollingDistrictPk", pollingDistrict.getPk())
				.setParameter(VOTING_CATEGORY_IDS, toStringList(categories))
				.setParameter(LATE_VALIDATION_FILTER, lateValidation)
				.getSingleResult();
	}

	public long findNotRejectedVotingCountByPollingDistrictAndCategoriesAndLateValidation(
			PollingDistrict pollingDistrict,
			no.valg.eva.admin.common.voting.VotingCategory[] categories,
			boolean lateValidation) {
		return getEm()
				.createNamedQuery("Voting.findNotRejectedVotingCountByPollingDistrictAndCategoriesAndLateValidation", Long.class)
				.setParameter("pollingDistrictPk", pollingDistrict.getPk())
				.setParameter(VOTING_CATEGORY_IDS, toStringList(categories))
				.setParameter(LATE_VALIDATION_FILTER, lateValidation)
				.getSingleResult();
	}

	public long findApprovedVotingCountByMunicipalityAndCategoriesAndLateValidation(
			Municipality municipality,
			no.valg.eva.admin.common.voting.VotingCategory[] categories,
			boolean lateValidation) {
		return getEm()
				.createNamedQuery("Voting.findApprovedVotingCountByMunicipalityAndCategoriesAndLateValidation", Long.class)
				.setParameter("municipalityPk", municipality.getPk())
				.setParameter(VOTING_CATEGORY_IDS, toStringList(categories))
				.setParameter(LATE_VALIDATION_FILTER, lateValidation)
				.getSingleResult();
	}

	public long findApprovedVotingCountByBoroughAndCategoriesAndLateValidation(
			Borough borough,
			no.valg.eva.admin.common.voting.VotingCategory[] categories,
			boolean lateValidation) {
		return getEm()
				.createNamedQuery("Voting.findApprovedVotingCountByBoroughAndCategoriesAndLateValidation", Long.class)
				.setParameter("boroughPk", borough.getPk())
				.setParameter(VOTING_CATEGORY_IDS, toStringList(categories))
				.setParameter(LATE_VALIDATION_FILTER, lateValidation)
				.getSingleResult();
	}

	public long findNotRejectedVotingCountByMunicipalityAndCategoriesAndLateValidation(
			Municipality municipality,
			no.valg.eva.admin.common.voting.VotingCategory[] categories,
			boolean lateValidation) {
		return getEm()
				.createNamedQuery("Voting.findNotRejectedVotingCountByMunicipalityAndCategoriesAndLateValidation", Long.class)
				.setParameter("municipalityPk", municipality.getPk())
				.setParameter(VOTING_CATEGORY_IDS, toStringList(categories))
				.setParameter(LATE_VALIDATION_FILTER, lateValidation)
				.getSingleResult();
	}

	public Collection<Voting> findApprovedVotingsByPollingDistrictAndCategories(
			PollingDistrict pollingDistrict,
			no.valg.eva.admin.common.voting.VotingCategory[] categories) {
		List<Object[]> rows = getEm()
			.createNamedQuery("Voting.findApprovedVotingsByPollingDistrictAndCategories", Object[].class)
			.setParameter("pollingDistrictPk", pollingDistrict.getPk())
			.setParameter(VOTING_CATEGORY_IDS, toStringList(categories))
			.getResultList();
		List<Voting> result = new ArrayList<>(rows.size());
		for (Object[] row : rows) {
			Voting voting = new Voting();
			voting.setCastTimestamp((DateTime) row[0]);
			voting.setApproved((Boolean) row[1]);
			voting.setVotingCategory((VotingCategory) row[2]);
			
			voting.setMvArea((MvArea) row[3]);
			
			result.add(voting);
		}
		return result;
	}

	public long findMarkOffInOtherBoroughs(Long boroughPk) {
		return getEm()
				.createNamedQuery("Voting.findMarkOffInOtherBoroughs", Long.class)
				.setParameter("boroughPk", boroughPk)
				.getSingleResult();
	}

	/**
	 * Find mark off count for samlekommune municipality in the sami election
	 * @param mvElection samlekommune
	 * @param lateValidationCovers true if FS, false if FO
	 * @return mark off count for all child municipalities with less that 30 voters in electoral roll
	 */
	public long findMarkOffForSamlekommuneInContest(MvElection mvElection, boolean lateValidationCovers) {
        return ((BigInteger) getEm()
                .createNamedQuery("Voting.findMarkOffForSamlekommuneInContest")
                .setParameter(PARAM_1_ELECTION_EVENT_PK, mvElection.getElectionEvent().getPk())
                .setParameter(PARAM_2_CONTEST_PK, mvElection.getContest().getPk())
                .setParameter(PARAM_3_LATE_VALIDATION, lateValidationCovers)
                .getSingleResult()).longValue();
	}

	private List<String> toStringList(no.valg.eva.admin.common.voting.VotingCategory[] categories) {
		List<String> stringList = new ArrayList<>();
		for (no.valg.eva.admin.common.voting.VotingCategory category : categories) {
			stringList.add(category.getId());
		}
		return stringList;
	}
}
