package no.valg.eva.admin.frontend.rbac.ctrls;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.model.SelectItem;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import no.evote.security.UserData;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.rbac.RoleItem;
import no.valg.eva.admin.common.rbac.service.AdminOperatorService;
import no.valg.eva.admin.frontend.BaseController;
import no.valg.eva.admin.frontend.i18n.MessageProvider;

@Named
@ViewScoped
public class RoleOptions extends BaseController {
	@Inject
	private MessageProvider messageProvider;
	@Inject
	private AdminOperatorService adminOperatorService;
	@Inject
	private UserData userData;

	private AreaPath areaPath;
	private List<SelectItem> roleNameOptions;
	private Map<String, RoleItem> roleMap;

	public void init(AreaPath areaPath) {
		if (this.areaPath == null || !this.areaPath.equals(areaPath)) {
			this.areaPath = areaPath;
			initOptions();
		}
	}

	public List<SelectItem> getRoleNameOptions() {
		return roleNameOptions;
	}

	public Map<String, RoleItem> getRoleMap() {
		return roleMap;
	}

	private void initOptions() {
		boolean includeUserSupport = areaPath.isRootLevel();
		Collection<RoleItem> roleItems = adminOperatorService.assignableRolesForArea(userData, areaPath);
		roleNameOptions = new ArrayList<>();
		roleMap = new HashMap<>();
		for (RoleItem roleItem : roleItems) {
			if (includeUserSupport || !roleItem.isUserSupport()) {
				roleNameOptions.add(new SelectItem(roleItem.getRoleId(), messageProvider.getByElectionEvent(roleItem.getRoleName(),
						userData.getElectionEventPk())));
				roleMap.put(roleItem.getRoleId(), roleItem);
			}
		}
		Collections.sort(roleNameOptions, new RoleOptionsComparator());
	}
}
