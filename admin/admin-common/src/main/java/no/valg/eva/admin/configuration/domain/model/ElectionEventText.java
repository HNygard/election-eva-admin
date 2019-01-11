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

import no.evote.constants.SQLConstants;
import no.evote.model.VersionedEntity;
import no.evote.validation.StringNotNullEmptyOrBlanks;

/**
 * Text linked to individual election event
 */
@Entity
@Table(name = "election_event_text", uniqueConstraints = @UniqueConstraint(columnNames = { SQLConstants.ELECTION_EVENT_PK, "election_event_text_name" }))
@AttributeOverride(name = "pk", column = @Column(name = "election_event_text_pk"))
public class ElectionEventText extends VersionedEntity implements java.io.Serializable {

	private ElectionEvent electionEvent;
	private String name;
	private String electionEventText;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = SQLConstants.ELECTION_EVENT_PK, nullable = false)
	public ElectionEvent getElectionEvent() {
		return this.electionEvent;
	}

	public void setElectionEvent(final ElectionEvent electionEvent) {
		this.electionEvent = electionEvent;
	}

	@Column(name = "election_event_text_name", nullable = false, length = 50)
	@StringNotNullEmptyOrBlanks
	@Size(max = 50)
	public String getName() {
		return this.name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	@Column(name = "election_event_text", nullable = false, length = 150)
	@StringNotNullEmptyOrBlanks
	@Size(max = 150)
	public String getElectionEventText() {
		return this.electionEventText;
	}

	public void setElectionEventText(final String electionEventText) {
		this.electionEventText = electionEventText;
	}

}
