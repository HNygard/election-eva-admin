package no.valg.eva.admin.frontend.configuration.ctrls.local;

import no.valg.eva.admin.common.configuration.model.local.ElectionDayPollingPlace;
import no.valg.eva.admin.frontend.configuration.ConfigurationView;
import no.valg.eva.admin.frontend.configuration.models.ElectionCardModel;
import org.primefaces.model.DefaultTreeNode;

import javax.faces.view.ViewScoped;
import javax.inject.Named;

@Named
@ViewScoped
public class ElectionCardConfigurationController extends ElectionCardBaseConfigurationController {

    @Override
    public ConfigurationView getView() {
        return ConfigurationView.ELECTION_CARD;
    }

    @Override
    boolean hasAccess() {
        return (getUserData().isOpptellingsvalgstyret() || isMunicipalityLevel()) && !isHasBoroughs();
    }

    @Override
    void buildTree() {

        ElectionCardModel parent = new ElectionCardModel(getElectionCard());

        setRootTreeNode(new DefaultTreeNode("Root", null));
        DefaultTreeNode topNode = new DefaultTreeNode(parent, getRootTreeNode());
        topNode.setSelected(true);
        topNode.setExpanded(true);
        setSelectedTreeNode(topNode);

        for (ElectionDayPollingPlace place : getElectionCard().getPlaces()) {
            ElectionCardModel child = new ElectionCardModel(getElectionCard(), place);
            new DefaultTreeNode(child, topNode);
        }
    }

    @Override
    String getWidgetId() {
        return "electionCard";
    }
}
