package no.evote.model;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import no.evote.constants.SQLConstants;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;

/**
 * PKCS#12 key pair / PKCS#7 certificate bundle
 */
@Entity
@NamedQueries({
		@NamedQuery(name = "SigningKey.getSigningKeyForElectionEventSigning", query = "SELECT k FROM SigningKey k WHERE"
				+ " k.electionEvent.pk = :electionEventPk AND k.keyDomain.id = 'ADMIN_SIGNING'"),
		@NamedQuery(name = "SigningKey.getAllSigningKeys", query = "SELECT k FROM SigningKey k WHERE"
				+ " k.keyDomain.id = 'ADMIN_SIGNING'"),
		@NamedQuery(name = "SigningKey.getScanningCountVerificationSigningKey", query = "SELECT k FROM SigningKey k WHERE k.keyDomain.id = 'SCANNING_COUNT'") })
@Table(name = "signing_key", uniqueConstraints = @UniqueConstraint(columnNames = { SQLConstants.ELECTION_EVENT_PK, "key_domain_pk" }))
@AttributeOverride(name = "pk", column = @Column(name = "signing_key_pk"))
public class SigningKey extends VersionedEntity implements java.io.Serializable {

	private ElectionEvent electionEvent;
	private KeyDomain keyDomain;
	private BinaryData binaryData;
	private String keyEncryptedPassphrase;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = SQLConstants.ELECTION_EVENT_PK, nullable = false)
	public ElectionEvent getElectionEvent() {
		return this.electionEvent;
	}

	public void setElectionEvent(final ElectionEvent electionEvent) {
		this.electionEvent = electionEvent;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "key_domain_pk", nullable = false)
	public KeyDomain getKeyDomain() {
		return this.keyDomain;
	}

	public void setKeyDomain(final KeyDomain keyDomain) {
		this.keyDomain = keyDomain;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "key_binary_data_pk")
	public BinaryData getBinaryData() {
		return this.binaryData;
	}

	public void setBinaryData(final BinaryData binaryData) {
		this.binaryData = binaryData;
	}

	@Column(name = "key_encrypted_passphrase", length = 100)
	public String getKeyEncryptedPassphrase() {
		return this.keyEncryptedPassphrase;
	}

	public void setKeyEncryptedPassphrase(final String keyEncryptedPassphrase) {
		this.keyEncryptedPassphrase = keyEncryptedPassphrase;
	}

}
