package no.valg.eva.admin.configuration.domain.model;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import no.evote.constants.SQLConstants;
import no.evote.model.VersionedEntity;

@Entity
@Table(name = "election_event_locale", schema = "admin", uniqueConstraints = @UniqueConstraint(columnNames = { SQLConstants.ELECTION_EVENT_PK, "locale_pk" }))
@AttributeOverride(name = "pk", column = @Column(name = "election_event_locale_pk"))
public class ElectionEventLocale extends VersionedEntity implements java.io.Serializable {

	private Locale locale;
	private ElectionEvent electionEvent;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "locale_pk", nullable = false)
	public Locale getLocale() {
		return this.locale;
	}

	public void setLocale(final Locale locale) {
		this.locale = locale;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = SQLConstants.ELECTION_EVENT_PK, nullable = false)
	public ElectionEvent getElectionEvent() {
		return this.electionEvent;
	}

	public void setElectionEvent(final ElectionEvent electionEvent) {
		this.electionEvent = electionEvent;
	}

}
