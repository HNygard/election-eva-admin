package no.valg.eva.admin.frontend.user.ctrls;

import static no.valg.eva.admin.frontend.common.dialog.Dialogs.EDIT_CURRENT_USER;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import no.evote.presentation.cache.ElectionEventCache;
import no.evote.security.UserData;
import no.valg.eva.admin.common.configuration.status.ElectionEventStatusEnum;
import no.valg.eva.admin.common.rbac.service.AdminOperatorService;
import no.valg.eva.admin.common.rbac.service.ContactInfo;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.frontend.BaseController;
import no.valg.eva.admin.frontend.common.dialog.Dialog;
import no.valg.eva.admin.rbac.domain.model.OperatorRole;
import no.valg.eva.admin.rbac.domain.model.Role;

@Named
@SessionScoped
public class UserDataController extends BaseController {

	@Inject
	private UserData userData;
	@Inject
	private UserAccess userAccess;
	@Inject
	private ElectionEventCache electionEventCache;
	@Inject
	private AdminOperatorService adminOperatorService;

	private ContactInfo contactInfo;

	public boolean isCurrentElectionEventDisabled() {
		return getElectionEvent() != null && getElectionEvent().getElectionEventStatus().getId() == ElectionEventStatusEnum.CLOSED.id();
	}

	public boolean isOverrideAccess() {
		return getUserAccess().isOverrideAccess();
	}

	public MvArea getOperatorArea() {
		return getOperatorRole().getMvArea();
	}

	public UserData getUserData() {
		return userData;
	}

	public UserAccess getUserAccess() {
		return userAccess;
	}

	public ElectionEvent getElectionEvent() {
		return electionEventCache.get(userData, userData.getElectionEventPk());
	}

	public boolean isCentralConfigurationStatus() {
		return getElectionEvent().getElectionEventStatus().getId() == ElectionEventStatusEnum.CENTRAL_CONFIGURATION.id();
	}

	public boolean isLocalConfigurationStatus() {
		return getElectionEvent().getElectionEventStatus().getId() == ElectionEventStatusEnum.LOCAL_CONFIGURATION.id();
	}

	public void invalidateCachedElectionEvent() {
		electionEventCache.remove(userData.getElectionEventPk());
	}

	public String getLocale() {
		return userData.getLocale().getId();
	}

	public boolean isRenderForceContactInfo() {
		if (getOperatorRole() == null) {
			return false;
		}
		boolean isUserSupport = getOperatorRole().getRole().isUserSupport();
		return !isUserSupport && !userData.getOperator().isContactInfoConfirmed();
	}

	public void showContactInfoDialog() {
		contactInfo = adminOperatorService.contactInfoForOperator(userData);
		if (getContactInfo().getPhone() != null && !getContactInfo().getPhone().trim().isEmpty()) {
			// Contact info is valid. Store to set contactInfoConfirmed
			userData.getOperator().setContactInfoConfirmed(true);
			adminOperatorService.updateContactInfoForOperator(userData, getContactInfo());
		}
		getEditCurrentUserDialog().open();
	}

	public void saveOperator() {
		adminOperatorService.updateContactInfoForOperator(userData, getContactInfo());
		getEditCurrentUserDialog().close();
	}

	public ContactInfo getContactInfo() {
		return contactInfo;
	}

	public Dialog getEditCurrentUserDialog() {
		return EDIT_CURRENT_USER;
	}

	public OperatorRole getOperatorRole() {
		return userData.getOperatorRole();
	}

	public Role getRole() {
		return getOperatorRole() == null ? null : getOperatorRole().getRole();
	}

	public MvElection getMvElection() {
		return getOperatorRole() == null ? null : getOperatorRole().getMvElection();
	}

	public String getElectionNameLine() {
		StringBuilder result = new StringBuilder();
		result.append(getMvElection().getElectionEventName());
		if (getMvElection().getContest() != null) {
			result.append(" - ").append(getMvElection().getContest().getName());
		}
		return result.toString();
	}

}
