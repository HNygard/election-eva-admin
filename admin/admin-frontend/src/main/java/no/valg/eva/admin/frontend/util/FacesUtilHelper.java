package no.valg.eva.admin.frontend.util;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import java.io.Serializable;

@Named
@ApplicationScoped
public class FacesUtilHelper implements Serializable {

    private static final long serialVersionUID = -5768124229354908777L;

    public void updateDom(String id) {
        FacesUtil.updateDom(id);
    }
}

