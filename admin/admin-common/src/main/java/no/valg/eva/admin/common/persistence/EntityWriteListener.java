package no.valg.eva.admin.common.persistence;

import java.io.Serializable;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.PrePersist;
import javax.persistence.PreRemove;
import javax.persistence.PreUpdate;
import javax.transaction.TransactionSynchronizationRegistry;

import no.evote.exception.EvoteException;
import no.evote.model.BaseEntity;
import no.evote.security.UserData;

/**
 * Entity listener for acting on persistence write operations. Currently checks that Role.isUserSupport is not allowed to perform write operations.
 */
public class EntityWriteListener implements Serializable {

	private static final String USER_DATA_KEY = "userData";

	@PrePersist
	public void doPrePersist(final BaseEntity entity) throws NamingException, IllegalAccessException {
		checkSupportUser();
	}

	@PreRemove
	public void doPreRemove(final BaseEntity entity) throws NamingException, IllegalAccessException {
		checkSupportUser();
	}

	@PreUpdate
	public void doPreUpdate(final BaseEntity entity) throws NamingException, IllegalAccessException {
		checkSupportUser();
	}

	private void checkSupportUser() throws IllegalAccessException {
		try {
			TransactionSynchronizationRegistry registry = (TransactionSynchronizationRegistry) getInitialContext()
					.lookup("java:comp/TransactionSynchronizationRegistry");
			if (registry.getTransactionKey() != null) {
				UserData userData = (UserData) registry.getResource(USER_DATA_KEY);
				if (userData != null && userData.getRole().isUserSupport()) {
					throw new IllegalAccessException("Support user does not have write access");
				}
			}
		} catch (NamingException ne) {
			throw new EvoteException("lookup of TransactionSynchronizationRegistry failed", ne);
		}
	}

	InitialContext getInitialContext() throws NamingException {
		return new InitialContext();
	}

}
