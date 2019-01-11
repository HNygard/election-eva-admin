package no.valg.eva.admin.frontend.faces;

import java.io.ObjectStreamException;
import java.io.Serializable;

import javax.faces.bean.ApplicationScoped;
import javax.faces.context.FacesContext;
import javax.inject.Named;
/**
 * This class represents the JSF Facescontext. To centralize and make controllers that use
 * this more testable, this was necessary to introduce.
 */
@Named
@ApplicationScoped
public class FacesContextBroker implements Serializable {
	private static final long serialVersionUID = 1L;
	private static final FacesContextBroker INSTANCE = new FacesContextBroker();

	public FacesContext getContext() {
		return FacesContext.getCurrentInstance();
	}

	private Object readResolve() throws ObjectStreamException {
		return INSTANCE;
	}
}
