package no.valg.eva.admin.rapport.domain.model;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import no.evote.model.VersionedEntity;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;

/**
 * Inneholder hvilke rapporter som skal være tilgjengelige for et election event
 */
@Entity
@Table(name = "election_event_report")
@AttributeOverride(name = "pk", column = @Column(name = "election_event_report_pk"))
@NamedQueries({
		@NamedQuery(
				name = "ElectionEventReport.findByElectionEventId",
				query = "select e from ElectionEventReport e where e.electionEvent.id = :electionEventId")
})
public class ElectionEventReport extends VersionedEntity {
	private Report report;
	private ElectionEvent electionEvent;

	public ElectionEventReport() {
	}

	public ElectionEventReport(ElectionEvent electionEvent, Report report) {
		this.electionEvent = electionEvent;
		this.report = report;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "report_pk")
	public Report getReport() {
		return report;
	}

	public void setReport(Report report) {
		this.report = report;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "election_event_pk")
	public ElectionEvent getElectionEvent() {
		return electionEvent;
	}

	public void setElectionEvent(ElectionEvent electionEvent) {
		this.electionEvent = electionEvent;
	}
}
