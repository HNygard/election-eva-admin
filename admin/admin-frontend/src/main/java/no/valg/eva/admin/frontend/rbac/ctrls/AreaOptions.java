package no.valg.eva.admin.frontend.rbac.ctrls;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import no.evote.security.UserData;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.PollingPlaceArea;
import no.valg.eva.admin.common.rbac.RoleItem;
import no.valg.eva.admin.common.rbac.service.AdminOperatorService;
import no.valg.eva.admin.frontend.BaseController;

@Named
@ViewScoped
public class AreaOptions extends BaseController {
	@Inject
	private AdminOperatorService adminOperatorService;
	@Inject
	private UserData userData;

	private AreaPath areaPath;
	private Map<String, List<PollingPlaceArea>> availableAreasForRole = new HashMap<>();
	private Map<String, PollingPlaceArea> areaMap = new HashMap<>();

	public void init(AreaPath areaPath) {
		if (this.areaPath == null || !this.areaPath.equals(areaPath)) {
			this.areaPath = areaPath;
			initOptions();
		}
	}

	public Map<String, List<PollingPlaceArea>> getAvailableAreasForRole() {
		return availableAreasForRole;
	}

	public Map<String, PollingPlaceArea> getAreaMap() {
		return areaMap;
	}

	public boolean hasAreasForRole(String roleId) {
		return !availableAreasForRole.isEmpty() && availableAreasForRole.containsKey(roleId) && !availableAreasForRole.get(roleId).isEmpty();
	}

	private void initOptions() {
		Map<RoleItem, List<PollingPlaceArea>> areasForRole = adminOperatorService.areasForRole(userData, areaPath);
		availableAreasForRole = new HashMap<>();
		for (Map.Entry<RoleItem, List<PollingPlaceArea>> entry : areasForRole.entrySet()) {
			List<PollingPlaceArea> areaList = entry.getValue();
			// Sort list by AreaPath
			Collections.sort(areaList, new Comparator<PollingPlaceArea>() {
				@Override
				public int compare(PollingPlaceArea area1, PollingPlaceArea area2) {
					return area1.getAreaPath().path().compareTo(area2.getAreaPath().path());
				}
			});
			availableAreasForRole.put(entry.getKey().getRoleId(), areaList);
		}
		areaMap = new HashMap<>();
		for (Map.Entry<String, List<PollingPlaceArea>> roleAreaEntry : availableAreasForRole.entrySet()) {
			for (PollingPlaceArea area : roleAreaEntry.getValue()) {
				areaMap.put(area.getAreaPath().path(), area);
			}
		}

	}
}
