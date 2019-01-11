package no.valg.eva.admin.configuration.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Setter;
import no.evote.constants.EvoteConstants;
import no.evote.constants.SQLConstants;
import no.evote.model.VersionedEntity;
import no.evote.validation.PartyValidationManual;
import no.valg.eva.admin.util.EqualsHashCodeUtil;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;

import javax.persistence.AttributeOverride;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedNativeQueries;
import javax.persistence.NamedNativeQuery;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Standard party list. Codes from Statistisk sentralbyrå
 */
@Entity
@Table(name = "party", uniqueConstraints = { @UniqueConstraint(columnNames = { SQLConstants.ELECTION_EVENT_PK, "short_code" }),
		@UniqueConstraint(columnNames = { SQLConstants.ELECTION_EVENT_PK, "party_id" }) })
@AttributeOverride(name = "pk", column = @Column(name = "party_pk"))
@NamedQueries({
		@NamedQuery(name = "Party.findWithNoAffiliationByContest", query = "SELECT distinct p1 FROM Party p1 left join p1.partyContestAreas as pca "
				+ "WHERE p1.electionEvent.pk = (SELECT c.electionEvent.pk FROM MvElection c "
				+ "WHERE c.contest.pk = :contestPk AND c.electionLevel = 3) AND p1.pk NOT IN "
				+ "(SELECT p2.pk FROM Party p2, Affiliation a WHERE p2.pk = a.party.pk AND a.ballot.contest.pk = :contestPk) AND "
				+ "(p1.partyCategory.id < '3' OR pca.countyId = :areaId OR pca.municipalityId = :areaId OR pca.boroughId = :areaId)"),
		@NamedQuery(name = "Party.findById", query = "SELECT p from Party p WHERE p.electionEvent.pk = :electionEventPk AND UPPER(p.id) = UPPER(:id)"),
		@NamedQuery(
				name = "Party.countPartiesWithId",
				query = "select count(p) from Party p where p.electionEvent.pk = :electionEventPk and UPPER(p.id) = UPPER(:id)"),
		@NamedQuery(name = "Party.findPartiesInEvent", query = "SELECT p from Party p WHERE p.electionEvent.pk = :electionEventPk"),
		@NamedQuery(
				name = "Party.findByShortCodeEvent",
				query = "SELECT p from Party p WHERE p.electionEvent.pk = :electionEventPk AND p.shortCode = :shortCode") })
// @formatter:off
@NamedNativeQueries({
		@NamedNativeQuery(
				name = "Party.findAllForAreaPathAndElectionPath",
				query = "SELECT p.* " +
						"FROM party p " +
						"JOIN affiliation a USING (party_pk) " +
						"JOIN ballot b using (ballot_pk) " +
						"JOIN mv_election mve using (contest_pk) " +
						"JOIN ballot_count bc using (ballot_pk) " +
						"JOIN vote_count vc using (vote_count_pk) " +
						"JOIN mv_area mva using (mv_area_pk) " +
						"WHERE text2ltree(mva.area_path) <@ text2ltree(:areaPath) " +
                        "   AND text2ltree(mve.election_path) <@ text2ltree(:electionPath) " +
                        "   AND p.party_id not like 'BLANK' " +
						"order by p.party_id", resultClass = Party.class) })
// @formatter:on
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Party extends VersionedEntity implements java.io.Serializable {

	private static final String KATEGORI_ID_FOR_LOKALT_PARTI = "3";

	@Setter private ElectionEvent electionEvent;
	@Setter private PartyCategory partyCategory;
	private String id;
	@Setter private Integer shortCode;
	@Setter private String name;
	@Setter private boolean approved;
	@Setter private boolean blank;
	@Setter private boolean forenkletBehandling;
	@Setter private Set<PartyContestArea> partyContestAreas = new HashSet<>();
	@Setter @Transient private String translatedPartyName;

	public Party(final String id, final Integer shortCode, final PartyCategory partyCategory,
				 final ElectionEvent electionEvent) {
		this.electionEvent = electionEvent;
		setId(id); 
		this.shortCode = shortCode;
		this.partyCategory = partyCategory;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = SQLConstants.ELECTION_EVENT_PK, nullable = false)
	@NotNull(groups = { PartyValidationManual.class })
	public ElectionEvent getElectionEvent() {
		return electionEvent;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "party_category_pk", nullable = false)
	public PartyCategory getPartyCategory() {
		return partyCategory;
	}

	@Length(min = 0, max = 8, message = "@validation.party.id.max", groups = { PartyValidationManual.class })
	@NotEmpty(message = "@validation.party.id.notEmpty", groups = { PartyValidationManual.class })
	@Column(name = "party_id", nullable = false, length = 8)
	@Pattern(regexp = EvoteConstants.REGEXP_PARTY_ID, message = "@validation.party.id.regex", groups = { PartyValidationManual.class })
	public String getId() {
		return id;
	}

	public void setId(final String id) {
		this.id = id;
		setName("@party[" + id + "].name");
	}

	@Column(name = "short_code", nullable = true)
	public Integer getShortCode() {
		return shortCode;
	}

	@Column(name = "party_name", nullable = false, length = 50)
	public String getName() {
		return name;
	}

	@Column(name = "approved", nullable = false)
	public boolean isApproved() {
		return approved;
	}

	@Column(name = "blank", nullable = false)
	public boolean isBlank() {
		return blank;
	}

	@Column(name = "forenklet_behandling", nullable = false)
	public boolean isForenkletBehandling() {
		return forenkletBehandling;
	}

	@OneToMany(mappedBy = "party", cascade = CascadeType.ALL, orphanRemoval = true)
	@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
	public Set<PartyContestArea> getPartyContestAreas() {
		return partyContestAreas;
	}

	@Transient
	public String getTranslatedPartyName() {
		return translatedPartyName;
	}

	public Set<PartyContestArea> partyContestAreasForPartyCategory() {
		if (this.skalHaOmradetilknytning()) {
			return getPartyContestAreas();
		}
		return Collections.emptySet();
	}

	@Transient
	public boolean isLokaltParti() {
		return KATEGORI_ID_FOR_LOKALT_PARTI.equals(getPartyCategory().getId());
	}

	public boolean skalHaOmradetilknytning() {
		return isLokaltParti();
	}
	
	
	@Override
	public boolean equals(final Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj.getClass() == this.getClass()) {
			return EqualsHashCodeUtil.genericEquals(obj, this);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return EqualsHashCodeUtil.genericHashCode(this);
	}
}
