package no.valg.eva.admin.frontend.rbac.ctrls;

import java.util.List;

import no.evote.constants.AreaLevelEnum;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.configuration.domain.model.MvArea;

public class RootLevelEdit {

	private OperatorEditController ctrl;
	private List<MvArea> countyList;
	private String county;
	private List<MvArea> municipalityList;
	private String municipality;

	public RootLevelEdit(OperatorEditController ctrl) {
		this.ctrl = ctrl;
		countyList = ctrl.getMvAreaService().findByPathAndLevel(
				ctrl.getUserData().getOperatorAreaPath().path(), AreaLevelEnum.COUNTY.getLevel());
	}

	public boolean isReady() {
		AreaLevelEnum maxLevel = ctrl.getMaxLevelForSelectRole(ctrl.getSelectedRoleToAdd());
		if (maxLevel == AreaLevelEnum.ROOT) {
			return true;
		}
		if (maxLevel == AreaLevelEnum.COUNTY) {
			return !isEmpty(getCounty());
		}
		return !isEmpty(getCounty()) && !isEmpty(getMunicipality());
	}

	public boolean isRenderCountyList(String roleId) {
		if (!roleId.equals(ctrl.getSelectedRoleToAdd())) {
			return false;
		}
		AreaLevelEnum maxLevel = ctrl.getMaxLevelForSelectRole(roleId);
		boolean notReadyAndLevelMatch = !isReady() && maxLevel.getLevel() >= AreaLevelEnum.COUNTY.getLevel();
		return notReadyAndLevelMatch && isEmpty(getCounty());
	}

	public boolean isRenderMunicipalityList(String roleId) {
		if (!roleId.equals(ctrl.getSelectedRoleToAdd())) {
			return false;
		}
		AreaLevelEnum maxLevel = ctrl.getMaxLevelForSelectRole(roleId);
		boolean notReadyAndLevelMatch = !isReady() && maxLevel.getLevel() >= AreaLevelEnum.MUNICIPALITY.getLevel();
		return notReadyAndLevelMatch && !isEmpty(getCounty()) && isEmpty(getMunicipality());
	}

	public void selectCounty() {
		if (ctrl.getMaxLevelForSelectRole(ctrl.getSelectedRoleToAdd()) == AreaLevelEnum.COUNTY) {
			AreaPath path = AreaPath.from(getCounty());
			ctrl.getAreaOptions().init(path);
		} else {
			municipalityList = ctrl.getMvAreaService().findByPathAndLevel(getCounty(), AreaLevelEnum.MUNICIPALITY.getLevel());
		}
	}

	public void selectMunicipality() {
		if ("0".equals(getMunicipality())) {
			setMunicipality(getCounty());
		}
		AreaPath path = AreaPath.from(getMunicipality());
		ctrl.getAreaOptions().init(path);
	}

	public void resetCounty() {
		setCounty(null);
		setMunicipality(null);
	}

	public void resetMunicipality() {
		setMunicipality(null);
	}

	public List<MvArea> getCountyList() {
		return countyList;
	}

	public String getCounty() {
		return county;
	}

	public void setCounty(String county) {
		this.county = county;
	}

	public List<MvArea> getMunicipalityList() {
		return municipalityList;
	}

	public String getMunicipality() {
		return municipality;
	}

	public void setMunicipality(String municipality) {
		this.municipality = municipality;
	}

	private boolean isEmpty(String s) {
		return s == null || s.isEmpty();
	}
}
