package no.evote.persistence;

import java.io.Serializable;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.transaction.TransactionSynchronizationRegistry;

import no.evote.exception.EvoteException;
import no.evote.model.BaseEntity;
import no.evote.model.VersionedEntity;
import no.evote.security.UserData;
import no.evote.service.security.SecurityInterceptor;

import org.joda.time.DateTime;

public class VersionedEntityListener implements Serializable {

	@PrePersist
	public void doPrePersist(final BaseEntity entity) throws NamingException {
		if (entity instanceof VersionedEntity) {
			setAuditFields((VersionedEntity) entity, "I");
		}
	}

	@PreUpdate
	public void doPreUpdate(final BaseEntity entity) {
		if (entity instanceof VersionedEntity) {
			setAuditFields((VersionedEntity) entity, "U");
		}
	}

	public void setAuditFields(VersionedEntity entity, String operation) {
		// audit_timestamp
		entity.setAuditTimestamp(DateTime.now());

		// audit_user (user is set in SecurityInterceptor)
		try {
			TransactionSynchronizationRegistry registry = (TransactionSynchronizationRegistry) new InitialContext()
					.lookup("java:comp/TransactionSynchronizationRegistry");
			// Audit operator is already set for non-container managed transactions (because TSR doesn't work)
			if (registry.getTransactionKey() != null) {
				UserData userData = (UserData) registry.getResource(SecurityInterceptor.USER_DATA_KEY);
				if (userData != null) {
					entity.setAuditOperator(userData.getUid());
				} else {
					if (entity.getAuditOperator() == null) {
						entity.setAuditOperator("System");
					}
				}
			}
		} catch (NamingException ne) {
			throw new EvoteException("lookup of TransactionSynchronizationRegistry failed", ne);
		}

		// audit_operation
		entity.setAuditOperation(operation);
	}

}
