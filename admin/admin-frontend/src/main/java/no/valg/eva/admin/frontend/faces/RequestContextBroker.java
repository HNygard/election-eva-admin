package no.valg.eva.admin.frontend.faces;

import java.io.ObjectStreamException;
import java.io.Serializable;

import javax.faces.bean.ApplicationScoped;
import javax.inject.Named;

import org.primefaces.context.RequestContext;


/**
 * This class represents the PrimeFaces request context. To centralize and make controllers that use
 * this more testable, this was necessary to introduce.
 */
@Named
@ApplicationScoped
public class RequestContextBroker implements Serializable {
	private static final long serialVersionUID = 1L;
	private static final RequestContextBroker INSTANCE = new RequestContextBroker();

	public RequestContext getContext() {
		return RequestContext.getCurrentInstance();
	}

	private Object readResolve() throws ObjectStreamException {
		return INSTANCE;
	}
}
