package no.valg.eva.admin.common.persistence;

import no.evote.exception.EvoteException;
import no.evote.model.BaseEntity;
import no.evote.security.UserData;
import no.valg.eva.admin.test.MockUtilsTestCase;
import org.testng.annotations.Test;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.TransactionSynchronizationRegistry;

import static org.assertj.core.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

public class EntityWriteListenerTest extends MockUtilsTestCase {

	@Test(expectedExceptions = EvoteException.class, expectedExceptionsMessageRegExp = "lookup of TransactionSynchronizationRegistry failed")
	public void doPrePersist_withNamingException_throwsEvoteException() throws Exception {
		EntityWriteListener listener = getEntityWriteListener();
		doThrow(new NamingException("NamingException")).when(listener.getInitialContext()).lookup(anyString());

		listener.doPrePersist(createMock(BaseEntity.class));
	}

	@Test(expectedExceptions = IllegalAccessException.class, expectedExceptionsMessageRegExp = "Support user does not have write access")
	public void doPreRemove_withUserSupport_throwsIllegalAccessException() throws Exception {
		EntityWriteListener listener = getEntityWriteListener();
		setupRegistry(listener, true);

		listener.doPreRemove(createMock(BaseEntity.class));
	}

	@Test
	public void doPreUpdate_withNoUserSupport_doesNotThrowException() throws Exception {
		EntityWriteListener listener = getEntityWriteListener();
		setupRegistry(listener, false);

		try {
			listener.doPreUpdate(createMock(BaseEntity.class));
		} catch (Exception e) {
			fail("Unexpected exception " + e);
		}
	}

	private void setupRegistry(EntityWriteListener listener, boolean isUserSupport) throws NamingException {
		TransactionSynchronizationRegistry registry = createMock(TransactionSynchronizationRegistry.class);
		when(registry.getTransactionKey()).thenReturn("key");
		when(listener.getInitialContext().lookup(anyString())).thenReturn(registry);
		UserData userData = createMock(UserData.class);
		when(registry.getResource(anyString())).thenReturn(userData);
		when(userData.getRole().isUserSupport()).thenReturn(isUserSupport);
	}

	private EntityWriteListener getEntityWriteListener() {
		final InitialContext ctx = createMock(InitialContext.class);
		return new EntityWriteListener() {
			@Override
            InitialContext getInitialContext() {
				return ctx;
			}
		};
	}
}
