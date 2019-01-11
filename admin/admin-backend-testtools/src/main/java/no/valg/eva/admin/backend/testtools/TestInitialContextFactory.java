package no.valg.eva.admin.backend.testtools;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;

import org.mockito.Mockito;

public class TestInitialContextFactory implements InitialContextFactory {

	private static Context context;

	static {
		context = Mockito.mock(Context.class);
	}

	public static Context getContextMock() {
		return context;
	}

	@Override
	public Context getInitialContext(Hashtable<?, ?> environment) throws NamingException {
		return context;
	}
}
