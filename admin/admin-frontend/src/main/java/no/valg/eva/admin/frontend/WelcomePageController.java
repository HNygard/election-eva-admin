package no.valg.eva.admin.frontend;

import lombok.NoArgsConstructor;
import no.valg.eva.admin.frontend.faces.FacesContextBroker;
import no.valg.eva.admin.frontend.security.TmpLoginDetector;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

@Named
@RequestScoped
@NoArgsConstructor
public class WelcomePageController{

    @Inject
    private FacesContextBroker facesContextBroker;

    public String getLoginUrl() {
        TmpLoginDetector tmpLoginDetector = new TmpLoginDetector();
        if (tmpLoginDetector.isTmpLoginEnabled()) {
            HttpServletRequest req = (HttpServletRequest) facesContextBroker.getContext().getExternalContext().getRequest();
            return "/tmpLogin?scanning=" + isScanning(req);
        } else {
            return "/secure/index.xhtml";
        }
    }

    private boolean isScanning(HttpServletRequest req) {
        return "true".equals(req.getParameter("scanning"));
    }

}
