package no.valg.eva.admin.common.configuration.model.local;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ParentPollingDistricts implements Serializable {
	private List<RegularPollingDistrict> selectableDistricts = new ArrayList<>();
	private List<ParentPollingDistrict> parentPollingDistricts = new ArrayList<>();

	public List<RegularPollingDistrict> getSelectableDistricts() {
		return selectableDistricts;
	}

	public List<ParentPollingDistrict> getParentPollingDistricts() {
		return parentPollingDistricts;
	}

	public boolean isValid() {
		if (parentPollingDistricts.isEmpty()) {
			return false;
		}
		for (ParentPollingDistrict ppd : parentPollingDistricts) {
			if (!ppd.isValid()) {
				return false;
			}
		}
		return true;
	}
}
