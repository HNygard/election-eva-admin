package no.valg.eva.admin.backend.common.domain;

import java.security.cert.CRLException;
import java.security.cert.X509CRL;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.Size;

import no.evote.exception.EvoteException;
import no.evote.model.VersionedEntity;

import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

@Entity
@Table(name = "certificate_revocation_list", uniqueConstraints = { @UniqueConstraint(columnNames = { "issuer_dn" }) })
@AttributeOverride(name = "pk", column = @Column(name = "certificate_revocation_list_pk"))
/**
 * Certificate revocation list.  Note that setters are kept private (used by hibernate) according to good DDD practice.
 */
public class CertificateRevocationList extends VersionedEntity {

	private String id;
	private DateTime updated;
	private DateTime nextUpdate;
	private byte[] encodedCrl;

	private CertificateRevocationList() {
	}

	public CertificateRevocationList(X509CRL x509Crl) {
		setId(x509Crl.getIssuerX500Principal().toString());
		setUpdated(new DateTime(x509Crl.getThisUpdate()));
		setNextUpdate(new DateTime(x509Crl.getNextUpdate()));
		try {
			setEncodedCrl(x509Crl.getEncoded());
		} catch (CRLException e) {
			throw new EvoteException("Error when getting encoded CRL", e);
		}
	}

	@Column(name = "issuer_dn", nullable = false, length = 100)
	@Size(max = 100)
	public String getId() {
		return id;
	}

	private void setId(String id) {
		this.id = id;
	}

	@Column(name = "updated", nullable = false)
	@Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
	public DateTime getUpdated() {
		return updated;
	}

	private void setUpdated(DateTime updated) {
		this.updated = updated;
	}

	@Column(name = "next_update", nullable = false)
	@Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
	public DateTime getNextUpdate() {
		return nextUpdate;
	}

	private void setNextUpdate(DateTime nextUpdate) {
		this.nextUpdate = nextUpdate;
	}

	@Column(name = "encoded_crl", nullable = false, columnDefinition = "bytea")
	public byte[] getEncodedCrl() {
		return encodedCrl;
	}
	
	private void setEncodedCrl(byte[] encodedCrl) {
		this.encodedCrl = encodedCrl;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof CertificateRevocationList)) {
			return false;
		}
		if (!super.equals(o)) {
			return false;
		}

		CertificateRevocationList that = (CertificateRevocationList) o;

		if (!id.equals(that.id)) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + id.hashCode();
		return result;
	}
}
