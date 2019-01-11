package no.evote.model;

import javax.persistence.AttributeOverride;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.Size;

import no.evote.constants.SQLConstants;
import no.evote.validation.StringNotNullEmptyOrBlanks;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;

/**
 * Binary data storage
 */
@Entity
@Table(name = "binary_data", uniqueConstraints = { @UniqueConstraint(columnNames = { SQLConstants.ELECTION_EVENT_PK, "binary_data_number" }) })
@AttributeOverride(name = "pk", column = @Column(name = "binary_data_pk"))
public class BinaryData extends VersionedEntity implements java.io.Serializable {
	private ElectionEvent electionEvent;
	private int number;
	private String tableName;
	private String columnName;
	private String fileName;
	private String mimeType;
	private byte[] binaryData;

	public BinaryData() {
		super();
	}

	public BinaryData(final BinaryData binaryData) {
		super();
		this.number = binaryData.getNumber();
		this.electionEvent = binaryData.getElectionEvent();
		this.binaryData = binaryData.getBinaryData();
		this.tableName = binaryData.getTableName();
		this.columnName = binaryData.getColumnName();
		this.fileName = binaryData.getFileName();
		this.mimeType = binaryData.getMimeType();
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = SQLConstants.ELECTION_EVENT_PK, nullable = false)
	public ElectionEvent getElectionEvent() {
		return this.electionEvent;
	}

	public void setElectionEvent(final ElectionEvent electionEvent) {
		this.electionEvent = electionEvent;
	}

	@Column(name = "binary_data_number", nullable = false, insertable = false, updatable = false)
	public int getNumber() {
		return this.number;
	}

	public void setNumber(final int number) {
		this.number = number;
	}

	@Column(name = "table_name", nullable = false, length = 50)
	@StringNotNullEmptyOrBlanks
	@Size(max = 50)
	public String getTableName() {
		return this.tableName;
	}

	public void setTableName(final String tableName) {
		this.tableName = tableName;
	}

	@Column(name = "column_name", nullable = false, length = 50)
	@StringNotNullEmptyOrBlanks
	@Size(max = 50)
	public String getColumnName() {
		return this.columnName;
	}

	public void setColumnName(final String columnName) {
		this.columnName = columnName;
	}

	@Column(name = "file_name", nullable = false, length = 226)
	public String getFileName() {
		return this.fileName;
	}

	public void setFileName(final String fileName) {
		this.fileName = fileName;
	}

	@Column(name = "mime_type", nullable = false)
	public String getMimeType() {
		return this.mimeType;
	}

	public void setMimeType(final String mimeType) {
		this.mimeType = mimeType;
	}

	@Basic(fetch = FetchType.LAZY)
	@Column(name = "binary_data", nullable = false, columnDefinition = "bytea")
	public byte[] getBinaryData() {
		return this.binaryData;
	}

	public void setBinaryData(final byte[] binaryData) {
		if (binaryData != null) {
			this.binaryData = new byte[binaryData.length];
			System.arraycopy(binaryData, 0, this.binaryData, 0, binaryData.length);
		}
	}

}
