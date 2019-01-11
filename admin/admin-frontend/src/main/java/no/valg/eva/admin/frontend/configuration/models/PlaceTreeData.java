package no.valg.eva.admin.frontend.configuration.models;

import java.util.List;

import no.valg.eva.admin.common.configuration.model.local.Borough;
import no.valg.eva.admin.common.configuration.model.local.Place;
import no.valg.eva.admin.common.configuration.model.local.PollingDistrict;
import no.valg.eva.admin.frontend.configuration.ctrls.local.ConfigurationController;
import no.valg.eva.admin.frontend.configuration.ctrls.local.StemmestyreBaseConfigurationController;

public class PlaceTreeData<T extends Place> {

	private ConfigurationController ctrl;
	private Borough borough;
	private List<T> boroughPlaces;
	private T place;

	public PlaceTreeData(ConfigurationController ctrl, Borough borough, List<T> boroughPlaces) {
		this.ctrl = ctrl;
		this.borough = borough;
		this.boroughPlaces = boroughPlaces;
	}

	public PlaceTreeData(ConfigurationController ctrl, T place) {
		this.ctrl = ctrl;
		this.place = place;
	}

	public boolean isValid() {
		if (borough == null) {
			return isPlaceValid(place);
		} else {
			for (T p : boroughPlaces) {
				if (!isPlaceValid(p)) {
					return false;
				}
			}
		}
		return true;
	}
	
	private boolean isPlaceValid(T place) {
		if (ctrl instanceof StemmestyreBaseConfigurationController) {
			return place.isValid() && ((PollingDistrict) place).isHasResponsibleOffiers();
		}
		return place.isValid();
	}

	public String getLabel() {
		if (borough == null) {
			return ctrl.getSelectId(place);
		} else {
			return borough.getName();
		}
	}

	public T getPlace() {
		return place;
	}
}
