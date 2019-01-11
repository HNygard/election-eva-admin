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
 * Text linked to individual contests
 */
@Entity
@Table(name = "contest_text", uniqueConstraints = @UniqueConstraint(columnNames = { "contest_pk", "contest_text_name" }))
@AttributeOverride(name = "pk", column = @Column(name = "contest_text_pk"))
public class ContestText extends VersionedEntity implements java.io.Serializable {

	private Contest contest;
	private String name;
	private String contestText;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "contest_pk", nullable = false)
	public Contest getContest() {
		return this.contest;
	}

	public void setContest(final Contest contest) {
		this.contest = contest;
	}

	@Column(name = "contest_text_name", nullable = false, length = 50)
	@StringNotNullEmptyOrBlanks
	@Size(max = 50)
	public String getName() {
		return this.name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	@Column(name = "contest_text", nullable = false, length = 150)
	@StringNotNullEmptyOrBlanks
	@Size(max = 150)
	public String getContestText() {
		return this.contestText;
	}

	public void setContestText(final String contestText) {
		this.contestText = contestText;
	}

}
