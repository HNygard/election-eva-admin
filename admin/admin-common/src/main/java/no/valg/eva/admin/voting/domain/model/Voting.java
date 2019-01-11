package no.valg.eva.admin.voting.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Setter;
import no.evote.constants.AreaLevelEnum;
import no.evote.constants.ElectionLevelEnum;
import no.evote.model.VersionedEntity;
import no.evote.security.ContextSecurable;
import no.valg.eva.admin.common.voting.VotingPhase;
import no.valg.eva.admin.configuration.domain.model.ElectionGroup;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.PollingPlace;
import no.valg.eva.admin.configuration.domain.model.Voter;
import no.valg.eva.admin.configuration.domain.model.VotingCategory;
import no.valg.eva.admin.configuration.domain.model.VotingRejection;
import no.valg.eva.admin.rbac.domain.model.Operator;
import no.valg.eva.admin.util.DateUtil;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.ColumnResult;
import javax.persistence.ConstructorResult;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedNativeQueries;
import javax.persistence.NamedNativeQuery;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.SqlResultSetMapping;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Information on all votings per voter. Only one voting may be accepted within each election_group
 */
@Entity
@Table(name = "voting", uniqueConstraints = {@UniqueConstraint(columnNames = {"voter_pk", "election_group_pk", "approved"}),
		@UniqueConstraint(columnNames = {"voter_pk", "election_group_pk", "voting_category_pk", "voting_number"})})
@AttributeOverride(name = "pk", column = @Column(name = "voting_pk"))
@NamedQueries({
		@NamedQuery(
				name = "Voting.countUnapprovedAdvanceVotings",
				query = "SELECT count(v) FROM Voting v WHERE v.mvArea.electionEvent.id = :electionEvenId "
						+ "AND v.mvArea.municipality.id = :municipalityId AND v.votingCategory.id IN ('FB', 'FE', 'FI', 'FU') AND "
						+ "v.approved = false AND v.lateValidation = false AND v.votingRejection is null"),
		@NamedQuery(
				name = "Voting.findApprovedVotesForVoterByElectionGroup",
				query = "SELECT v FROM Voting v WHERE v.voter.pk = :voterPk AND v.approved = TRUE " + "AND v.electionGroup.pk =:electionGroupPk "),
		@NamedQuery(name = "Voting.findVotingsByElectionGroupAndVoter", query = "SELECT v FROM Voting v"
				+ " LEFT JOIN FETCH v.mvArea " 
				+ " WHERE v.voter.pk = :voterPk AND v.electionGroup.pk ="
				+ ":electionGroupPk AND "
				+ " v.votingCategory.id != 'FA'"),
		@NamedQuery(
				name = "Voting.findVotingsByElectionGroupVoterAndMunicipality",
				query = "SELECT v FROM Voting v, MvArea AS a " 
						+ "WHERE a.pollingPlace.pk = v.pollingPlace.pk " 
						+ "AND v.voter.pk = :voterPk " 
						+ "AND v.electionGroup.pk = :electionGroupPk " 
						+ "AND a.municipality.pk = :municipalityPk"),
		@NamedQuery(
				name = "Voting.findReceivedVotingsByElectionGroupAndVoter",
				query = "SELECT v FROM Voting v WHERE v.voter.pk = :voterPk AND v.electionGroup.pk ="
						+ ":electionGroupPk AND v.receivedTimestamp IS NOT NULL "),
		@NamedQuery(name = "Voting.findVotingsNotApprovedByElectionAndVoter", query = "SELECT v FROM Voting v WHERE v.voter.pk = :voterPk "
				+ "AND v.electionGroup.pk = :electionGroupPk AND v.approved = FALSE AND v.votingRejection IS NULL AND v.votingNumber > 0 "
				+ "ORDER BY v.castTimestamp DESC"),
		@NamedQuery(
				name = "Voting.findRejectedVotingsByElectionAndMunicipality",
				query = "SELECT v FROM Voting v INNER JOIN FETCH v.voter vo INNER JOIN v.mvArea a" + " WHERE a.municipalityId = :municipalityId "
						+ "AND v.electionGroup.pk = :electionGroupPk AND v.votingRejection IS NOT NULL ORDER BY vo.lastName, vo.firstName, vo.middleName"),
		@NamedQuery(
				name = "Voting.findRejectedVotingsByElectionGroupAndVoter",
				query = "SELECT v FROM Voting v WHERE v.voter.pk = :voterPk "
						+ "AND v.electionGroup.pk = :electionGroupPk AND v.votingRejection IS NOT NULL ORDER BY v.voter.lastName, v.voter.firstName, v.voter.middleName"),
		@NamedQuery(name = "Voting.findApprovedVotingExcludingCategories", query = "SELECT v FROM Voting v WHERE v.voter.pk = :voterPk AND v.approved = TRUE "
				+ "AND v.electionGroup.pk =:electionGroupPk AND " + "v.votingCategory.id NOT IN (:votingCategories)"),
		@NamedQuery(
				name = "Voting.findApprovedVotingsByPollingDistrictAndCategories",
				query = "SELECT v.castTimestamp, v.approved, v.votingCategory, v.mvArea "
						+ "FROM Voting v "
						+ "WHERE v.approved = TRUE "
						+ "AND v.pollingPlace.pollingDistrict.pk = :pollingDistrictPk "
						+ "AND v.votingCategory.id IN (:votingCategoryIds)"),
		@NamedQuery(
				name = "Voting.findApprovedVotingCountByPollingDistrictAndCategoriesAndLateValidation",
				query = "SELECT count(v) "
						+ "FROM Voting v "
						+ "WHERE v.approved = TRUE "
						+ "AND v.mvArea.pollingDistrict.pk = :pollingDistrictPk "
						+ "AND v.votingCategory.id IN (:votingCategoryIds) "
						+ "AND v.lateValidation = :lateValidationFilter"),
		@NamedQuery(
				name = "Voting.findNotRejectedVotingCountByPollingDistrictAndCategoriesAndLateValidation",
				query = "SELECT count(v) "
						+ "FROM Voting v "
						+ "WHERE v.votingRejection IS NULL "
						+ "AND v.mvArea.pollingDistrict.pk = :pollingDistrictPk "
						+ "AND v.votingCategory.id IN (:votingCategoryIds) "
						+ "AND v.lateValidation = :lateValidationFilter"),
		@NamedQuery(
				name = "Voting.findApprovedVotingCountByBoroughAndCategoriesAndLateValidation",
				query = "SELECT count(v) "
						+ "FROM Voting v "
						+ "WHERE v.approved = TRUE "
						+ "AND v.mvArea.borough.pk = :boroughPk "
						+ "AND v.votingCategory.id IN (:votingCategoryIds) "
						+ "AND v.lateValidation = :lateValidationFilter"),
		@NamedQuery(
				name = "Voting.findApprovedVotingCountByMunicipalityAndCategoriesAndLateValidation",
				query = "SELECT count(v) "
						+ "FROM Voting v "
						+ "WHERE v.approved = TRUE "
						+ "AND v.mvArea.municipality.pk = :municipalityPk "
						+ "AND v.votingCategory.id IN (:votingCategoryIds) "
						+ "AND v.lateValidation = :lateValidationFilter"),
		@NamedQuery(
				name = "Voting.findNotRejectedVotingCountByMunicipalityAndCategoriesAndLateValidation",
				query = "SELECT count(v) "
						+ "FROM Voting v "
						+ "WHERE v.votingRejection IS NULL "
						+ "AND v.mvArea.municipality.pk = :municipalityPk "
						+ "AND v.votingCategory.id IN (:votingCategoryIds) "
						+ "AND v.lateValidation = :lateValidationFilter"),
		@NamedQuery(
				name = "Voting.findMarkOffInOtherBoroughs",
				query = "SELECT count(v) "
						+ "FROM Voting v "
						+ "WHERE v.votingRejection IS NULL "
						+ "AND v.pollingPlace.pollingDistrict.borough.municipality.pk = v.mvArea.municipality.pk "
						+ "AND v.pollingPlace.pollingDistrict.borough.pk != v.mvArea.borough.pk "
						+ "AND v.votingCategory.id = 'VF' "
						+ "AND v.voter.mvArea.borough.pk = :boroughPk")
})
@NamedNativeQueries({
		@NamedNativeQuery(
				name =  "Voting.findMarkOffForSamlekommuneInContest",
				query = "SELECT count(*) FROM voting v "
						+ "JOIN voting_category vc ON v.voting_category_pk = vc.voting_category_pk AND vc.voting_category_id IN ('FI', 'FU', 'FB', 'FE') "
						+ "JOIN mv_area pd_mva ON v.mv_area_pk = pd_mva.mv_area_pk "
						+ "JOIN mv_area m_mva ON m_mva.municipality_pk = pd_mva.municipality_pk AND m_mva.area_level = 3 "
						+ "JOIN contest_area ca ON ca.mv_area_pk = m_mva.mv_area_pk AND ca.child_area IS TRUE "
						+ "WHERE m_mva.election_event_pk = ?1 AND ca.contest_pk = ?2 AND v.voting_rejection_pk IS NULL "
						+ "AND v.late_validation = ?3"),
		@NamedNativeQuery(
				name = "Voting.slettStemmegivningerFraVelgerTilhoerendeI",
				query = "DELETE FROM voting"
						+ "  WHERE voting_pk IN ("
						+ "    SELECT voting_pk FROM voting"
						+ "      LEFT JOIN voter USING (voter_pk)"
						+ "      LEFT JOIN mv_area mva ON mva.mv_area_pk = voter.mv_area_pk"
						+ "    WHERE text2ltree(mva.area_path) <@ text2ltree(?1)"
						+ "  )"),
		@NamedNativeQuery(
				name = "Voting.flyttStemmegivningerForVelgereI",
				query = "UPDATE voting"
					  	+ " SET mv_area_pk = ?1"
						+ " WHERE mv_area_pk IN ("
						+ "   SELECT DISTINCT mva.mv_area_pk FROM voting"
						+ "   LEFT JOIN mv_area mva USING (mv_area_pk)"
						+ "   WHERE text2ltree(mva.area_path) <@ text2ltree(?2)"
						+ " )"),
		@NamedNativeQuery(
				name = "Voting.flyttStemmegivningerAvgittI",
				query = "UPDATE voting"
					  	+ " SET polling_place_pk = ?1"
						+ " WHERE polling_place_pk IN ("
						+ "   SELECT DISTINCT mva.polling_place_pk FROM voting"
						+ "   LEFT JOIN mv_area mva USING (polling_place_pk)"
						+ "   WHERE text2ltree(mva.area_path) <@ text2ltree(?2)"
						+ " )"),
		@NamedNativeQuery(
				// Lister stemmegivninger for et gitt område hvor velgere kommer fra (ikke der de avga stemme!)
				name = "Voting.findStemmegivninger",
				query = "SELECT "
						+ "  coalesce(sum(cast(vr.voting_rejection_pk IS NULL AND vc.early_voting AS INTEGER)), 0) AS stg_f_godkjente,"
						+ "  coalesce(sum(cast(vr.voting_rejection_pk IS NULL AND NOT vc.early_voting AS INTEGER)), 0) AS stg_v_godkjente,"
						+ "  coalesce(sum(cast(vr.voting_rejection_pk IS NOT NULL AND vc.early_voting AS INTEGER)), 0) AS stg_f_forkastede,"
						+ "  coalesce(sum(cast(vr.voting_rejection_pk IS NOT NULL AND NOT vc.early_voting AS INTEGER)), 0) AS stg_v_forkastede "
						+ "FROM voting vt "
						+ "  JOIN voter v                  ON vt.voter_pk = v.voter_pk "
						+ "  JOIN mv_area mva              ON v.mv_area_pk = mva.mv_area_pk "
						+ "  JOIN voting_category vc       ON vc.voting_category_pk = vt.voting_category_pk "
						+ "  LEFT JOIN voting_rejection vr ON vr.voting_rejection_pk = vt.voting_rejection_pk "
						+ "WHERE"
						+ "  text2ltree(mva.area_path) <@ text2ltree(:areaPath) AND "
						+ "  vc.voting_category_id != 'FA' AND "
						// vi sjekker at approved = true for XiM-kommuner, fordi det er mulig å registrere for sent innkomne forhåndsstemmegivinger
						// som man ikke eksplisitt angir forkastelsesgrunn. Disse blir da likevel ikke telt som *ikke godkjent* i XiM-kommuner. 
						// Problemstillingen er så bitte-bitteliten siden dette bare kan oppstå etter at oppgjøret er gjort, så vi bruker ikke mer energi på dette.
						// Uansett løser dette inkonsistens i data mellom KO- og FY-valg (se EVA-1456), hvor det er 2 oppgjør, men samme datagrunnlag!
						+ "  ((vr IS NULL AND (CASE WHEN (:xim) THEN vt.approved = 'true' ELSE TRUE END)) OR voting_rejection_id NOT IN ('FA', 'VA', 'F0', 'V0'))",
				resultSetMapping = "ValgnattStatistikk"),
		@NamedNativeQuery(
				name = "Voting.findRejectedVotingCount",
				query = "SELECT count(*) " +
						"FROM voting v " +
						"JOIN mv_area mva ON v.mv_area_pk = mva.mv_area_pk " +
						"JOIN voting_rejection vr ON v.voting_rejection_pk = vr.voting_rejection_pk " +
						"WHERE vr.voting_rejection_id = :votingRejectionId AND " +
						"text2ltree(mva.area_path) <@ text2ltree(:areaPath);"
		),
		@NamedNativeQuery(
				name = "Voting.findVotingsToConfirmByMvareaAndCategory",
				query = "SELECT v.* "
						+ "FROM Voting v "
						+ "JOIN mv_area mva on (v.polling_place_pk = mva.polling_place_pk and mva.area_level = 6) "
						+ "JOIN voting_category vc on v.voting_category_pk = vc.voting_category_pk "
						+ "WHERE v.validation_timestamp IS NULL "
						+ "AND v.election_group_pk = :electionGroupPk "
						+ "AND v.phase = coalesce(CAST(:phase AS TEXT), v.phase) "
						+ "AND mva.municipality_pk = :municipalityPk "
                        + "AND vc.voting_category_id = coalesce(CAST(:votingcategoryId AS TEXT), vc.voting_category_id)"
                        + "AND (v.cast_timestamp BETWEEN coalesce(:startDate, v.cast_timestamp) AND coalesce(:endDate, v.cast_timestamp)) "
                        + "AND v.late_validation = coalesce(CAST(CAST(:lateValidation AS TEXT) AS BOOLEAN), v.late_validation)",
                resultClass = Voting.class),
        @NamedNativeQuery(
                name = "Voting.findApprovedVotingsByMvareaAndCategory",
                query = "SELECT v.* "
                        + "FROM Voting v "
                        + "JOIN mv_area mva on (v.polling_place_pk = mva.polling_place_pk and mva.area_level = 6) "
                        + "JOIN voting_category vc on v.voting_category_pk = vc.voting_category_pk "
                        + "WHERE v.approved = TRUE "
						+ "AND v.election_group_pk = :electionGroupPk "
						+ "AND mva.municipality_pk = :municipalityPk "
                        + "AND vc.voting_category_id = coalesce(CAST(:votingcategoryId AS TEXT), vc.voting_category_id) "
                        + "AND v.late_validation = coalesce(CAST(CAST(:lateValidation AS TEXT) AS BOOLEAN), v.late_validation)",
                resultClass = Voting.class),
        @NamedNativeQuery(
                name = "Voting.findRejectedVotingsByMvareaAndCategory",
                query = "SELECT v.* "
                        + "FROM Voting v "
                        + "JOIN mv_area mva on (v.polling_place_pk = mva.polling_place_pk and mva.area_level = 6) "
                        + "JOIN voting_rejection vr on v.voting_rejection_pk = vr.voting_rejection_pk "
                        + "JOIN voting_category vc on v.voting_category_pk = vc.voting_category_pk "
                        + "WHERE v.voting_rejection_pk IS NOT NULL "
                        + "AND v.voting_number IS NOT NULL "
                        + "AND vr.voting_rejection_id != 'V0' "
                        + "AND vr.voting_rejection_id != 'F0' "
						+ "AND v.election_group_pk = :electionGroupPk "
						+ "AND mva.municipality_pk = :municipalityPk "
                        + "AND vc.voting_category_id = coalesce(CAST(:votingcategoryId AS TEXT), vc.voting_category_id) "
                        + "AND v.late_validation = coalesce(CAST(CAST(:lateValidation AS TEXT) AS BOOLEAN), v.late_validation)",
                resultClass = Voting.class),
        @NamedNativeQuery(
                name = "Voting.findVotingsToConfirmByMvareaAndCategoryWithDead",
                query = "SELECT cv.* "
                        + "FROM voting cv "
                        + "       JOIN mv_area a ON (a.polling_place_pk = cv.polling_place_pk AND a.area_level = 6) "
                        + "       JOIN voting_category vc ON (vc.voting_category_pk = cv.voting_category_pk) "
                        + "       JOIN voter v ON (v.voter_pk = cv.voter_pk) "
                        + "       LEFT JOIN mv_area voter_area ON (v.mv_area_pk = voter_area.mv_area_pk) "
                        + "WHERE a.municipality_pk = :municipalityPk "
                        + "  AND cv.election_group_pk = :electionGroupPk "
                        + "	 AND vc.voting_category_id = coalesce(CAST(:votingcategoryId AS TEXT), vc.voting_category_id)"
						+ "	 AND cv.phase = coalesce(CAST(:phase AS TEXT), cv.phase) "
                        + "  AND cv.voting_rejection_pk IS NULL "
                        + "  AND cv.validation_timestamp IS NULL "
                        + "  AND cv.late_validation = :lateValidation "
                        + "  AND v.statuskode = '5'"
						+ "  AND (cv.cast_timestamp BETWEEN coalesce(:startDate, cv.cast_timestamp) AND coalesce(:endDate, cv.cast_timestamp))",
                resultClass = Voting.class),
        @NamedNativeQuery(
                name = "Voting.findVotingsToConfirmByMvareaAndCategoryWithNotInElectoralRoll",
                query = "SELECT cv.* "
                        + "FROM voting cv "
                        + "       JOIN mv_area a ON (a.polling_place_pk = cv.polling_place_pk AND a.area_level = 6) "
                        + "       JOIN voting_category vc ON (vc.voting_category_pk = cv.voting_category_pk) "
                        + "       JOIN voter v ON (v.voter_pk = cv.voter_pk) "
                        + "       LEFT JOIN mv_area voter_area ON (v.mv_area_pk = voter_area.mv_area_pk) "
                        + "WHERE a.municipality_pk = :municipalityPk "
                        + "  AND cv.election_group_pk = :electionGroupPk "
                        + "	 AND vc.voting_category_id = coalesce(CAST(:votingcategoryId AS TEXT), vc.voting_category_id)"
						+ "	 AND cv.phase = coalesce(CAST(:phase AS TEXT), cv.phase) "
                        + "  AND cv.voting_rejection_pk IS NULL "
                        + "  AND cv.validation_timestamp IS NULL "
                        + "  AND cv.late_validation = :lateValidation "
                        + "  AND (NOT v.approved OR (voter_area.municipality_pk != :municipalityPk))"
						+ "  AND (cv.cast_timestamp BETWEEN coalesce(:startDate, cv.cast_timestamp) AND coalesce(:endDate, cv.cast_timestamp))",
                resultClass = Voting.class),
        @NamedNativeQuery(
                name = "Voting.findVotingsToConfirmByMvareaAndCategoryWithMultipleVotings",
                query = "SELECT cv.* "
                        + "FROM voting cv "
                        + "       JOIN mv_area a ON (a.polling_place_pk = cv.polling_place_pk AND a.area_level = 6) "
                        + "       JOIN voting_category vc ON (vc.voting_category_pk = cv.voting_category_pk) "
                        + "       JOIN voter v ON (v.voter_pk = cv.voter_pk) "
                        + "       LEFT JOIN mv_area voter_area ON (v.mv_area_pk = voter_area.mv_area_pk) "
                        + "WHERE a.municipality_pk = :municipalityPk "
                        + "  AND cv.election_group_pk = :electionGroupPk "
                        + "	 AND vc.voting_category_id = coalesce(CAST(:votingcategoryId AS TEXT), vc.voting_category_id)"
						+ "	 AND cv.phase = coalesce(CAST(:phase AS TEXT), cv.phase) "
                        + "  AND cv.voting_rejection_pk IS NULL "
                        + "  AND cv.validation_timestamp IS NULL "
                        + "  AND cv.late_validation = :lateValidation "
                        + "  AND EXISTS(SELECT 1 "
                        + "             FROM voting cv2 "
                        + "                    JOIN voting_category vc2 ON (vc2.voting_category_pk = cv2.voting_category_pk) "
                        + "             WHERE cv2.election_group_pk = cv.election_group_pk "
                        + "               AND vc2.voting_category_id != 'FA' "
                        + "               AND cv2.voter_pk = cv.voter_pk "
                        + "               AND cv2.voting_pk != cv.voting_pk "
                        + "               AND cv2.voting_rejection_pk IS NULL)"
						+ "  AND (cv.cast_timestamp BETWEEN coalesce(:startDate, cv.cast_timestamp) AND coalesce(:endDate, cv.cast_timestamp))",
                resultClass = Voting.class),
		@NamedNativeQuery(
                name = "Voting.countApprovedEnvelopeVotings",
				query = "SELECT count(v) "
						+ "FROM Voting v "
						+ "JOIN mv_area mva on (v.polling_place_pk = mva.polling_place_pk and mva.area_level = 6) "
						+ "JOIN voting_category vc on v.voting_category_pk = vc.voting_category_pk "
						+ "WHERE v.approved = TRUE "
                        + "AND v.voting_number IS NOT NULL " 
						+ "AND v.election_group_pk = :electionGroupPk "
                        + "AND v.late_validation = :lateValidation "
						+ "AND mva.municipality_pk = :municipalityPk "
						+ "AND vc.voting_category_id = :votingcategoryId "
						+ "AND (v.cast_timestamp BETWEEN coalesce(:startDate, v.cast_timestamp) AND coalesce(:endDate, v.cast_timestamp))"),
        @NamedNativeQuery(
                name = "Voting.countRejectedEnvelopeVotings",
                query = "SELECT count(v) "
                        + "FROM Voting v "
                        + "JOIN mv_area mva on (v.polling_place_pk = mva.polling_place_pk and mva.area_level = 6) "
                        + "JOIN voting_rejection vr on v.voting_rejection_pk = vr.voting_rejection_pk "
                        + "JOIN voting_category vc on v.voting_category_pk = vc.voting_category_pk "
                        + "WHERE v.voting_rejection_pk IS NOT NULL "
                        + "AND v.voting_number IS NOT NULL "
						+ "AND v.election_group_pk = :electionGroupPk "
                        + "AND v.late_validation = :lateValidation "
						+ "AND mva.municipality_pk = :municipalityPk "
                        + "AND vc.voting_category_id = :votingcategoryId "
                        + "AND (v.cast_timestamp BETWEEN coalesce(:startDate, v.cast_timestamp) AND coalesce(:endDate, v.cast_timestamp))"),
		@NamedNativeQuery(
				name = "Voting.findVotingsToConfirmForVoter",
				query = "SELECT v.* "
						+ "FROM Voting v "
						+ "WHERE v.validation_timestamp IS NULL " 
						+ "AND v.voter_pk = :voterPk",
				resultClass = Voting.class),
})

@SqlResultSetMapping(
		name = "ValgnattStatistikk",
		classes = {
				@ConstructorResult(
						targetClass = Stemmegivningsstatistikk.class,
						columns = {
								@ColumnResult(name = "stg_f_godkjente"),
								@ColumnResult(name = "stg_v_godkjente"),
								@ColumnResult(name = "stg_f_forkastede"),
								@ColumnResult(name = "stg_v_forkastede")
						})
		})
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Voting extends VersionedEntity implements java.io.Serializable, ContextSecurable {
	private static final long serialVersionUID = 2892612559016475901L;
	
	private static final Set<no.valg.eva.admin.common.voting.VotingCategory> ADVANCE_VOTING_CATEGORIES = new HashSet<>(Arrays.asList(
			no.valg.eva.admin.common.voting.VotingCategory.FA,
			no.valg.eva.admin.common.voting.VotingCategory.FB,
			no.valg.eva.admin.common.voting.VotingCategory.FE,
			no.valg.eva.admin.common.voting.VotingCategory.FI,
			no.valg.eva.admin.common.voting.VotingCategory.FU));

	@Setter private MvArea mvArea;
	@Setter private VotingCategory votingCategory;
	@Setter private VotingRejection votingRejection;
	@Setter private Voter voter;
	@Setter private PollingPlace pollingPlace;
	@Setter private ElectionGroup electionGroup;
	@Setter private Integer votingNumber;
	private DateTime castTimestamp;
	@Setter private DateTime receivedTimestamp;
	@Setter private DateTime validationTimestamp;
	@Setter private boolean approved;
	@Setter private String removalRequest;
	@Setter private boolean lateValidation;
	@Setter private String ballotBoxId;
	@Setter private VotingRejection suggestedVotingRejection;
    @Setter private Operator operator;
    @Setter private VotingPhase phase;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "mv_area_pk", nullable = false)
	public MvArea getMvArea() {
		return mvArea;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "voting_category_pk", nullable = false)
	public VotingCategory getVotingCategory() {
		return votingCategory;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "voting_rejection_pk")
	public VotingRejection getVotingRejection() {
		return votingRejection;
	}

	@ManyToOne
	@JoinColumn(name = "voter_pk", nullable = false)
	public Voter getVoter() {
		return voter;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "polling_place_pk")
	public PollingPlace getPollingPlace() {
		return pollingPlace;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "election_group_pk", nullable = false)
	public ElectionGroup getElectionGroup() {
		return electionGroup;
	}

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "suggested_voting_rejection_pk")
    public VotingRejection getSuggestedVotingRejection() {
        return suggestedVotingRejection;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operator_pk", nullable = false)
    public Operator getOperator() {
        return operator;
    }

	@Column(name = "voting_number", insertable = false, updatable = false)
	public Integer getVotingNumber() {
		return votingNumber;
	}

	@Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
	@Column(name = "cast_timestamp", nullable = false, length = 29)
	public DateTime getCastTimestamp() {
		return castTimestamp;
	}

	public void setCastTimestamp(LocalDate castTimestamp) {
		this.castTimestamp = castTimestamp.toDateTimeAtStartOfDay();
	}

	public void setCastTimestamp(DateTime castTimestamp) {
		this.castTimestamp = castTimestamp;
	}

	@Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
	@Column(name = "received_timestamp", length = 29)
	public DateTime getReceivedTimestamp() {
		return receivedTimestamp;
	}

	@Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
	@Column(name = "validation_timestamp", length = 29)
	public DateTime getValidationTimestamp() {
		return validationTimestamp;
	}

	@Column(name = "approved", nullable = false)
	public boolean isApproved() {
		return approved;
	}

	@Column(name = "removal_request", length = 150)
	@Size(max = 150)
	public String getRemovalRequest() {
		return removalRequest;
	}

	@Column(name = "late_validation", nullable = false)
	public boolean isLateValidation() {
		return lateValidation;
	}

    @Transient
    public boolean isNotLateValidation() {
        return !isLateValidation();
    }

	@Column(name = "ballot_box_id", length = 4)
	@Size(max = 4)
	public String getBallotBoxId() {
		return ballotBoxId;
	}

	@Column(name = "phase")
	@Enumerated(EnumType.STRING)
	public VotingPhase getPhase() {
		return phase;
	}

	@Override
	public Long getAreaPk(final AreaLevelEnum level) {
		if (level.equals(AreaLevelEnum.POLLING_PLACE) && pollingPlace != null) {
			return pollingPlace.getPk();
		}

		return null;
	}

	@Override
	public Long getElectionPk(final ElectionLevelEnum level) {
		if (level.equals(ElectionLevelEnum.ELECTION_GROUP)) {
			return electionGroup.getPk();
		}

		return null;
	}

	@Transient
	public boolean isAdvanceVoting() {
		no.valg.eva.admin.common.voting.VotingCategory aVotingCategory = no.valg.eva.admin.common.voting.VotingCategory.fromId(getVotingCategory().getId());
		return ADVANCE_VOTING_CATEGORIES.contains(aVotingCategory);
	}

    @Transient
    public LocalDateTime getCastTimeStampAsJavaTime() {
        return DateUtil.convertToLocalDateTime(getCastTimestamp());
    }

    @Transient
    public LocalDateTime getReceivedTimeStampAsJavaTime() {
        return DateUtil.convertToLocalDateTime(getReceivedTimestamp());
    }

    @Transient
    public LocalDateTime getValidationTimeStampAsJavaTime() {
        return DateUtil.convertToLocalDateTime(getValidationTimestamp());
    }

    @Transient
    public boolean isNotConfirmed() {
        return !isApproved() &&
                getVotingRejection() == null;
    }

}
