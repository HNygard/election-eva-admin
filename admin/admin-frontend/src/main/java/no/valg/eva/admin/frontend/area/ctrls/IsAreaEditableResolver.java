package no.valg.eva.admin.frontend.area.ctrls;

import static no.evote.constants.AreaLevelEnum.POLLING_DISTRICT;

import no.evote.constants.AreaLevelEnum;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.frontend.user.ctrls.UserDataController;

class IsAreaEditableResolver {
	private UserDataController userDataController;

	/**
	 * @param userDataController to resolve accesses
	 */
	IsAreaEditableResolver(UserDataController userDataController) {
		this.userDataController = userDataController;
	}

	/**
	 * Checks status and access rights to find out whether this element can be edited
	 *
	 * @param areaLevel area context level for checking access rights
	 * @param mvArea area which may be editable or not
	 * @return true if editable or if the user has override privileges, else false
	 */
	public boolean isEditable(AreaLevelEnum areaLevel, MvArea mvArea) {
		if (mvArea == null) {
			return false;
		}
		if (areaLevel == POLLING_DISTRICT) {
			return mvArea.getPollingDistrict().isEditableCentrally();
		}
		if (userDataController.isOverrideAccess()) {
			return true;
		}
		return userDataController.isCentralConfigurationStatus();
	}
}
