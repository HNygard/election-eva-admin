package no.valg.eva.admin.frontend.configuration.ctrls.local;

import no.valg.eva.admin.common.configuration.model.local.PollingDistrict;
import no.valg.eva.admin.frontend.configuration.ConfigurationView;

import javax.faces.event.ValueChangeEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

import static no.valg.eva.admin.frontend.configuration.ConfigurationView.STEMMESTYRE;

@Named
@ViewScoped
public class StemmestyreConfigurationController extends StemmestyreBaseConfigurationController {

    @Override
    public ConfigurationView getView() {
        return STEMMESTYRE;
    }

    @Override
    boolean hasAccess() {
        return super.hasAccess() && !isHasBoroughs();
    }

    public void districtSelected(ValueChangeEvent event) {
        districtSelected((PollingDistrict) event.getNewValue());
    }

    @Override
    String getBoardMemberFormIdPath() {
        return "configurationPanel:" + getMainController().getActiveControllerIndex() + ":stemmestyre:boardMemberForm";
    }

}
