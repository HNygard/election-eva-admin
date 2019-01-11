package no.valg.eva.admin.configuration.domain.model;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.Size;

import no.evote.model.VersionedEntity;
import no.evote.validation.StringNotNullEmptyOrBlanks;

/**
 * Text linked to individual elections
 */
@Entity
@Table(name = "election_text", uniqueConstraints = @UniqueConstraint(columnNames = { "election_pk", "election_text_name" }))
@AttributeOverride(name = "pk", column = @Column(name = "election_text_pk"))
public class ElectionText extends VersionedEntity implements java.io.Serializable {

	private Election election;
	private String name;
	private String electionText;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "election_pk", nullable = false)
	public Election getElection() {
		return this.election;
	}

	public void setElection(final Election election) {
		this.election = election;
	}

	@Column(name = "election_text_name", nullable = false, length = 50)
	@StringNotNullEmptyOrBlanks
	@Size(max = 50)
	public String getName() {
		return this.name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	@Column(name = "election_text", nullable = false, length = 150)
	@StringNotNullEmptyOrBlanks
	@Size(max = 150)
	public String getElectionText() {
		return this.electionText;
	}

	public void setElectionText(final String electionText) {
		this.electionText = electionText;
	}

}
