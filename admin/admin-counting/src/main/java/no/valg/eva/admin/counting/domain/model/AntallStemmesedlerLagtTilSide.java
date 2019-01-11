package no.valg.eva.admin.counting.domain.model;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import no.evote.model.VersionedEntity;
import no.valg.eva.admin.common.persistence.EntityWriteListener;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.ElectionGroup;
import no.valg.eva.admin.configuration.domain.model.Municipality;

@Entity
@EntityListeners({ EntityWriteListener.class })
@Table(
		name = "antall_stemmesedler_lagt_til_side",
		uniqueConstraints = { @UniqueConstraint(columnNames = { "municipality_pk", "election_group_pk", "contest_pk" }) })
@AttributeOverride(name = "pk", column = @Column(name = "antall_stemmesedler_lagt_til_side_pk"))
@NamedQueries({
	@NamedQuery(name = "AntallStemmesedlerLagtTilSide.findByMunicipalityAndElectionGroup",
			query = "SELECT a FROM AntallStemmesedlerLagtTilSide a WHERE a.municipality.pk = :municipalityPk AND a.electionGroup.pk = :electionGroupPk"),
	@NamedQuery(name = "AntallStemmesedlerLagtTilSide.findByMunicipalityAndContest",
			query = "SELECT a FROM AntallStemmesedlerLagtTilSide a WHERE a.municipality.pk = :municipalityPk AND a.contest.pk = :contestPk")
})
public class AntallStemmesedlerLagtTilSide extends VersionedEntity {
	private Municipality municipality;
	private ElectionGroup electionGroup;
	private Contest contest;
	private int antallStemmesedler;

	public AntallStemmesedlerLagtTilSide() {
		// påkrevd av Hibernate
	}

	public AntallStemmesedlerLagtTilSide(Municipality municipality, ElectionGroup electionGroup, Contest contest, int antallStemmesedler) {
		this.municipality = municipality;
		this.electionGroup = electionGroup;
		this.contest = contest;
		this.antallStemmesedler = antallStemmesedler;
	}

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "municipality_pk", nullable = false)
	public Municipality getMunicipality() {
		return municipality;
	}

	private void setMunicipality(Municipality municipality) {
		// Compliant, påkrevd av Hibernate
		this.municipality = municipality;
	}

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "election_group_pk", nullable = false)
	public ElectionGroup getElectionGroup() {
		return electionGroup;
	}

	private void setElectionGroup(ElectionGroup electionGroup) {
		// Compliant, påkrevd av Hibernate
		this.electionGroup = electionGroup;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "contest_pk")
	public Contest getContest() {
		return contest;
	}

	private void setContest(Contest contest) {
		// Compliant, påkrevd av Hibernate
		this.contest = contest;
	}

	@Column(name = "antall_stemmesedler", nullable = false)
	public int getAntallStemmesedler() {
		return antallStemmesedler;
	}

	public void setAntallStemmesedler(int antallStemmesedler) {
		this.antallStemmesedler = antallStemmesedler;
	}
}
