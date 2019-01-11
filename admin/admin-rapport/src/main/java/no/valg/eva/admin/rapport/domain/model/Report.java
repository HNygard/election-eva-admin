package no.valg.eva.admin.rapport.domain.model;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import no.evote.model.VersionedEntity;
import no.evote.validation.ID;
import no.valg.eva.admin.common.rapport.model.ReportCategory;
import no.valg.eva.admin.rbac.domain.model.Access;

/**
 * Inneholder alle tilgjengelige rapporter i systemet
 */
@Entity
@Table(name = "report")
@AttributeOverride(name = "pk", column = @Column(name = "report_pk"))
public class Report extends VersionedEntity {
	private String id;
	private ReportCategory category;
	private Access access;

	@Column(name = "report_id", nullable = false, length = 64)
	@ID(size = 64)
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Column(name = "category", nullable = false)
	@Enumerated(EnumType.STRING)
	public ReportCategory getCategory() {
		return category;
	}

	public void setCategory(ReportCategory category) {
		this.category = category;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "access_pk")
	public Access getAccess() {
		return access;
	}

	public void setAccess(Access access) {
		this.access = access;
	}
}
