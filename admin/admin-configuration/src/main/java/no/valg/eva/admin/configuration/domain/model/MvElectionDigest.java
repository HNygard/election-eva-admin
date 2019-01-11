package no.valg.eva.admin.configuration.domain.model;

import static org.hibernate.annotations.CacheConcurrencyStrategy.READ_ONLY;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedNativeQueries;
import javax.persistence.NamedNativeQuery;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.QueryHint;
import javax.persistence.Table;
import javax.validation.constraints.Size;

import no.evote.constants.ElectionLevelEnum;
import no.evote.model.BaseEntity;
import no.evote.validation.StringNotNullEmptyOrBlanks;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.felles.sti.valghierarki.ValghierarkiSti;
import no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.Immutable;

/**
 * Inneholder basis info for et område.
 */
@Entity
@Immutable
@Cache(usage = READ_ONLY, region = "no.valg.eva.admin.configuration.domain.model.MvElectionDigest")
@Table(name = "mv_election")
@AttributeOverride(name = "pk", column = @Column(name = "mv_election_pk"))
@NamedQueries({
		@NamedQuery(
				name = "MvElectionDigest.findSingleDigestByPath",
				query = "SELECT mve "
						+ "FROM MvElectionDigest mve "
						+ "WHERE mve.electionPath = :path",
				hints = { @QueryHint(name = "org.hibernate.cacheable", value = "true") }) })
@NamedNativeQueries(value = {
		@NamedNativeQuery(
				name = "MvElectionDigest.findDigestsByPathAndLevel",
				query = "SELECT * "
						+ "FROM mv_election mve "
						+ "WHERE text2ltree(mve.election_path) <@ text2ltree(?1) AND mve.election_level = ?2 "
						+ "ORDER BY mve.election_path",
				resultClass = MvElectionDigest.class),
		@NamedNativeQuery(
				name = "MvElectionDigest.findFirstDigestByPathAndLevel",
				query = "SELECT * "
						+ "FROM mv_election mve "
						+ "WHERE text2ltree(mve.election_path) <@ text2ltree(?1) AND mve.election_level = ?2 "
						+ "ORDER BY mve.election_path limit 1",
				resultClass = MvElectionDigest.class),
		@NamedNativeQuery(
				name = "MvElectionDigest.findDigestByElectionPathAndAreaPath",
				query = "SELECT DISTINCT mve.*  "
						+ "FROM mv_election mve "
						+ "JOIN contest_area USING (contest_pk) "
						+ "JOIN mv_area mva_ca USING (mv_area_pk) "
						+ "WHERE mve.election_path LIKE ?1 AND mve.election_level = 3 "
						+ "      AND EXISTS (SELECT mv_area_pk "
						+ "                  FROM mv_area mva "
						+ "                  WHERE mva.area_path = ?2 "
						+ "                        AND (mva.area_level >= mva_ca.area_level AND mva.area_path LIKE concat(mva_ca.area_path, '%') "
						+ "                             OR mva.area_level < mva_ca.area_level AND mva_ca.area_path LIKE concat(mva.area_path, '%')))",
				resultClass = MvElectionDigest.class)
})
public class MvElectionDigest extends BaseEntity {
	private String electionPath;
	private int electionLevel;
	private String electionEventName;
	private String electionGroupName;
	private String electionName;
	private String contestName;
	private Integer areaLevel;
	private ElectionDigest electionDigest;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "election_pk")
	public ElectionDigest getElectionDigest() {
		return this.electionDigest;
	}

	public void setElectionDigest(ElectionDigest electionDigest) {
		this.electionDigest = electionDigest;
	}

	@Column(name = "election_path", nullable = false, length = 19)
	@StringNotNullEmptyOrBlanks
	@Size(max = 19)
	public String getElectionPath() {
		return this.electionPath;
	}

	private void setElectionPath(final String electionPath) {
		this.electionPath = electionPath;
	}

	@Column(name = "election_level", nullable = false)
	public int getElectionLevel() {
		return this.electionLevel;
	}

	private void setElectionLevel(final int electionLevel) {
		this.electionLevel = electionLevel;
	}

	@Column(name = "election_event_name", nullable = false, length = 100)
	@StringNotNullEmptyOrBlanks
	@Size(max = 100)
	public String getElectionEventName() {
		return this.electionEventName;
	}

	private void setElectionEventName(final String electionEventName) {
		this.electionEventName = electionEventName;
	}

	@Column(name = "election_group_name", length = 100)
	@Size(max = 100)
	public String getElectionGroupName() {
		return this.electionGroupName;
	}

	private void setElectionGroupName(final String electionGroupName) {
		this.electionGroupName = electionGroupName;
	}

	@Column(name = "election_name", length = 100)
	@Size(max = 100)
	public String getElectionName() {
		return this.electionName;
	}

	private void setElectionName(final String electionName) {
		this.electionName = electionName;
	}

	@Column(name = "contest_name", length = 100)
	@Size(max = 100)
	public String getContestName() {
		return this.contestName;
	}

	private void setContestName(final String contestName) {
		this.contestName = contestName;
	}

	@Column(name = "area_level")
	public Integer getAreaLevel() {
		return this.areaLevel;
	}

	private void setAreaLevel(final Integer areaLevel) {
		this.areaLevel = areaLevel;
	}

	public ElectionPath electionPath() {
		return ElectionPath.from(getElectionPath());
	}

	public String electionHierarchyName() {
		switch (ElectionLevelEnum.getLevel(electionLevel)) {
		case ELECTION_EVENT:
			return electionEventName;
		case ELECTION_GROUP:
			return electionGroupName;
		case ELECTION:
			return electionName;
		default:
			return contestName;
		}
	}

	public ValghierarkiSti valghierarkiSti() {
		return ValghierarkiSti.fra(electionPath());
	}

	public ValggeografiNivaa valggeografiNivaa() {
		return ValggeografiNivaa.fra(areaLevel);
	}
}
