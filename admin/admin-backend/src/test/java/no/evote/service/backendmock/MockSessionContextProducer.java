package no.evote.service.backendmock;

import static org.mockito.Mockito.mock;

import javax.ejb.SessionContext;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

import no.valg.eva.admin.backend.service.impl.Pojo;

/**
 */
public class MockSessionContextProducer {
	@Produces
	@Pojo
	public SessionContext getSessionContext(final InjectionPoint ip) {
		return mock(SessionContext.class);
	}

}
