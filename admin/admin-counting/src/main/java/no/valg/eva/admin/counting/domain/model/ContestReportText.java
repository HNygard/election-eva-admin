package no.valg.eva.admin.counting.domain.model;

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
 * Text for reporting unit contest protocols
 */
@Entity
@Table(name = "contest_report_text", uniqueConstraints = @UniqueConstraint(columnNames = { "contest_report_pk", "contest_report_text_name" }))
@AttributeOverride(name = "pk", column = @Column(name = "contest_report_text_pk"))
public class ContestReportText extends VersionedEntity implements java.io.Serializable {

	private ContestReport contestReport;
	private String name;
	private String contestReportText;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "contest_report_pk", nullable = false)
	public ContestReport getContestReport() {
		return this.contestReport;
	}

	public void setContestReport(final ContestReport contestReport) {
		this.contestReport = contestReport;
	}

	@Column(name = "contest_report_text_name", nullable = false, length = 50)
	@StringNotNullEmptyOrBlanks
	@Size(max = 50)
	public String getName() {
		return this.name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	@Column(name = "contest_report_text", nullable = false, length = 150)
	@StringNotNullEmptyOrBlanks
	@Size(max = 150)
	public String getContestReportText() {
		return this.contestReportText;
	}

	public void setContestReportText(final String contestReportText) {
		this.contestReportText = contestReportText;
	}

}
