package no.valg.eva.admin.frontend.configuration.ctrls.local;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.faces.view.ViewScoped;
import javax.inject.Named;

import no.valg.eva.admin.common.configuration.model.local.Borough;
import no.valg.eva.admin.common.configuration.model.local.ElectionDayPollingPlace;
import no.valg.eva.admin.frontend.configuration.ConfigurationView;
import no.valg.eva.admin.frontend.configuration.models.BoroughsElectionCardModel;
import no.valg.eva.admin.frontend.configuration.models.ElectionCardModel;

import org.primefaces.model.DefaultTreeNode;

/**
 * Denne controlleren er for grunnlangsdata for kommuner som har bydeler (typisk Oslo).
 */
@Named
@ViewScoped
public class BoroughsElectionCardConfigurationController extends ElectionCardBaseConfigurationController {

	@Override
	public ConfigurationView getView() {
		return ConfigurationView.BOROUGHS_ELECTION_CARD;
	}

	@Override
	boolean hasAccess() {
		return isMunicipalityLevel() && isHasBoroughs();
	}

	@Override
	void buildTree() {

		ElectionCardModel parent = new ElectionCardModel(getElectionCard());

		setRootTreeNode(new DefaultTreeNode("Root", null));
		DefaultTreeNode topNode = new DefaultTreeNode(parent, getRootTreeNode());
		topNode.setSelected(true);
		topNode.setExpanded(true);
		setSelectedTreeNode(topNode);

		Map<Borough, List<ElectionDayPollingPlace>> grouped = groupByBorough();

		addChildren(topNode, grouped);
	}

	@Override
	String getWidgetId() {
		return "electionCardTree";
	}

	private void addChildren(DefaultTreeNode parentNode, Map<Borough, List<ElectionDayPollingPlace>> grouped) {
		for (Map.Entry<Borough, List<ElectionDayPollingPlace>> entry : grouped.entrySet()) {
			DefaultTreeNode topNode = new DefaultTreeNode(new BoroughsElectionCardModel(getElectionCard(), entry.getKey()), parentNode);
			topNode.setSelectable(false);
			for (ElectionDayPollingPlace child : entry.getValue()) {
				new DefaultTreeNode(new ElectionCardModel(getElectionCard(), child), topNode);
			}
		}
	}

	private SortedMap<Borough, List<ElectionDayPollingPlace>> groupByBorough() {
		SortedMap<Borough, List<ElectionDayPollingPlace>> result = new TreeMap<>(new Comparator<Borough>() {
			@Override
			public int compare(Borough borough1, Borough borough2) {
				return borough1.getPath().path().compareTo(borough2.getPath().path());
			}
		});
		for (ElectionDayPollingPlace place : getElectionCard().getPlaces()) {
			List<ElectionDayPollingPlace> grouped = result.get(place.getBorough());
			if (grouped == null) {
				grouped = new ArrayList<>();
				result.put(place.getBorough(), grouped);
			}
			grouped.add(place);
		}
		return result;
	}
}
