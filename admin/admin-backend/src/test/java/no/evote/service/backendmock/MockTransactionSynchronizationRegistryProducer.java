package no.evote.service.backendmock;

import static org.mockito.Mockito.mock;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.transaction.TransactionSynchronizationRegistry;

import no.valg.eva.admin.backend.service.impl.Pojo;

public class MockTransactionSynchronizationRegistryProducer {
	@Produces
	@Pojo
	public TransactionSynchronizationRegistry getTransactionSynchronizationRegistry(final InjectionPoint ip) {
		return mock(TransactionSynchronizationRegistry.class);
	}
}
