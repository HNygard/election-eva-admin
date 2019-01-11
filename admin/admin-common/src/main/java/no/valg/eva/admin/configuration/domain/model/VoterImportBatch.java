package no.valg.eva.admin.configuration.domain.model;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import no.evote.constants.AreaLevelEnum;
import no.evote.constants.ElectionLevelEnum;
import no.evote.constants.SQLConstants;
import no.evote.model.VersionedEntity;
import no.evote.security.ContextSecurable;

import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

/**
 * Information on the latest voter import batch
 */
@Entity
@Table(name = "voter_import_batch", uniqueConstraints = @UniqueConstraint(columnNames = SQLConstants.ELECTION_EVENT_PK))
@AttributeOverride(name = "pk", column = @Column(name = "voter_import_batch_pk"))
public class VoterImportBatch extends VersionedEntity implements java.io.Serializable, ContextSecurable {

	private ElectionEvent electionEvent;
	private int lastImportBatchNumber;
	private DateTime lastImportStart;
	private DateTime lastImportEnd;
	private int lastImportRecordsTotal;
	private int lastImportRecordsInsert;
	private int lastImportRecordsUpdate;
	private int lastImportRecordsDelete;
	private int lastImportRecordsSkip;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = SQLConstants.ELECTION_EVENT_PK, nullable = false)
	public ElectionEvent getElectionEvent() {
		return this.electionEvent;
	}

	public void setElectionEvent(final ElectionEvent electionEvent) {
		this.electionEvent = electionEvent;
	}

	@Column(name = "last_import_batch_number", nullable = false)
	public int getLastImportBatchNumber() {
		return this.lastImportBatchNumber;
	}

	public void setLastImportBatchNumber(final int lastImportBatchNumber) {
		this.lastImportBatchNumber = lastImportBatchNumber;
	}

	@Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
	@Column(name = "last_import_start", nullable = false, length = 29)
	public DateTime getLastImportStart() {
		return lastImportStart;
	}

	public void setLastImportStart(DateTime lastImportStart) {
		this.lastImportStart = lastImportStart;
	}

	@Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
	@Column(name = "last_import_end", nullable = false, length = 29)
	public DateTime getLastImportEnd() {
		return lastImportEnd;
	}

	public void setLastImportEnd(DateTime lastImportEnd) {
		this.lastImportEnd = lastImportEnd;
	}

	@Column(name = "last_import_records_total", nullable = false)
	public int getLastImportRecordsTotal() {
		return this.lastImportRecordsTotal;
	}

	public void setLastImportRecordsTotal(final int lastImportRecordsTotal) {
		this.lastImportRecordsTotal = lastImportRecordsTotal;
	}

	@Column(name = "last_import_records_insert", nullable = false)
	public int getLastImportRecordsInsert() {
		return this.lastImportRecordsInsert;
	}

	public void setLastImportRecordsInsert(final int lastImportRecordsInsert) {
		this.lastImportRecordsInsert = lastImportRecordsInsert;
	}

	@Column(name = "last_import_records_update", nullable = false)
	public int getLastImportRecordsUpdate() {
		return this.lastImportRecordsUpdate;
	}

	public void setLastImportRecordsUpdate(final int lastImportRecordsUpdate) {
		this.lastImportRecordsUpdate = lastImportRecordsUpdate;
	}

	@Column(name = "last_import_records_delete", nullable = false)
	public int getLastImportRecordsDelete() {
		return this.lastImportRecordsDelete;
	}

	public void setLastImportRecordsDelete(final int lastImportRecordsDelete) {
		this.lastImportRecordsDelete = lastImportRecordsDelete;
	}

	@Column(name = "last_import_records_skip", nullable = false)
	public int getLastImportRecordsSkip() {
		return this.lastImportRecordsSkip;
	}

	public void setLastImportRecordsSkip(final int lastImportRecordsSkip) {
		this.lastImportRecordsSkip = lastImportRecordsSkip;
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
