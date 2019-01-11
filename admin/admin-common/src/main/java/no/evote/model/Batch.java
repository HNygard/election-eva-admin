package no.evote.model;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import no.evote.constants.AreaLevelEnum;
import no.evote.constants.ElectionLevelEnum;
import no.evote.constants.SQLConstants;
import no.evote.security.ContextSecurable;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.felles.bakgrunnsjobb.domain.model.Jobbkategori;
import no.valg.eva.admin.rbac.domain.model.OperatorRole;

/**
 * Control of and information on batch jobs
 */
@Entity
@Table(name = "batch", uniqueConstraints = @UniqueConstraint(columnNames = { SQLConstants.ELECTION_EVENT_PK, "batch_category", "batch_number" }))
@NamedQueries({
		@NamedQuery(name = "Batch.findByElectionEventIdAndCategory", query = "SELECT b FROM Batch b WHERE b.electionEvent.id = :electionEventId "
				+ "AND b.category = :category ORDER BY b.auditTimestamp DESC"),
		@NamedQuery(name = "Batch.findByOperator", query = "SELECT b FROM Batch b WHERE b.operatorRole.operator.pk = :opk AND b.category = :category "
				+ "ORDER BY b.auditTimestamp DESC"),
		@NamedQuery(name = "Batch.findBatchUnique", query = "SELECT b FROM Batch b WHERE b.number = :id AND b.electionEvent.pk = :electionEventPk "
				+ "AND b.category = :category") })
@AttributeOverride(name = "pk", column = @Column(name = "batch_pk"))
public class Batch extends VersionedEntity implements java.io.Serializable, ContextSecurable {

	private OperatorRole operatorRole;
	private BatchStatus batchStatus;
	private ElectionEvent electionEvent;
	private BinaryData binaryData;
	private Jobbkategori category;
	private int number;
	private String infoText;
	private String messageText;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "operator_role_pk")
	public OperatorRole getOperatorRole() {
		return this.operatorRole;
	}

	public void setOperatorRole(final OperatorRole operatorRole) {
		this.operatorRole = operatorRole;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "batch_status_pk", nullable = false)
	public BatchStatus getBatchStatus() {
		return this.batchStatus;
	}

	public void setBatchStatus(final BatchStatus batchStatus) {
		this.batchStatus = batchStatus;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = SQLConstants.ELECTION_EVENT_PK, nullable = false)
	public ElectionEvent getElectionEvent() {
		return this.electionEvent;
	}

	public void setElectionEvent(final ElectionEvent electionEvent) {
		this.electionEvent = electionEvent;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "batch_binary_data_pk")
	public BinaryData getBinaryData() {
		return this.binaryData;
	}

	public void setBinaryData(final BinaryData binaryData) {
		this.binaryData = binaryData;
	}

	@Column(name = "batch_category", nullable = false)
	@Enumerated(EnumType.STRING)
	public Jobbkategori getCategory() {
		return category;
	}

	public void setCategory(Jobbkategori category) {
		this.category = category;
	}

	@Column(name = "batch_number", nullable = false, insertable = false, updatable = false)
	public int getNumber() {
		return this.number;
	}

	public void setNumber(final int number) {
		this.number = number;
	}

	@Column(name = "info_text", length = 150)
	public String getInfoText() {
		return this.infoText;
	}

	public void setInfoText(final String infoText) {
		this.infoText = infoText;
	}

	@Column(name = "message_text", length = 500)
	public String getMessageText() {
		return this.messageText;
	}

	public void setMessageText(final String messageText) {
		this.messageText = messageText;
	}

	@Override
	public Long getAreaPk(final AreaLevelEnum level) {
		return null;
	}

	@Override
	public Long getElectionPk(final ElectionLevelEnum level) {
		if (level.equals(ElectionLevelEnum.ELECTION_EVENT)) {
			return electionEvent.getPk();
		}
		return null;
	}

}
