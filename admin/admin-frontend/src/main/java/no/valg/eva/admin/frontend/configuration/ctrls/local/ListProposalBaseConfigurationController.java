package no.valg.eva.admin.frontend.configuration.ctrls.local;

import javax.faces.application.FacesMessage;
import javax.inject.Inject;

import no.valg.eva.admin.frontend.util.MessageUtil;
import no.valg.eva.admin.common.configuration.model.central.ContestListProposalData;
import no.valg.eva.admin.common.configuration.model.local.ListProposalConfig;
import no.valg.eva.admin.common.configuration.service.ListProposalService;
import no.valg.eva.admin.frontend.configuration.ctrls.ContestListProposalDataSource;

public abstract class ListProposalBaseConfigurationController extends ConfigurationController implements ContestListProposalDataSource {

	@Inject
	private ListProposalService listProposalService;

	private ListProposalConfig listProposal;

	@Override
	public void init() {
		loadData();
		if (listProposal.isCountStarted()) {
			MessageUtil.buildDetailMessage(getMessageProvider().get("@listProposal.lockedBeacuaseOfCountStarted"), FacesMessage.SEVERITY_WARN);
		}
	}

	@Override
	public String getName() {
		return "@config.local.accordion.list_proposal.name";
	}

	@Override
	void setDoneStatus(boolean value) {
		if (isCountyLevel()) {
			getCountyConfigStatus().setListProposals(value);
		}
		if (isMunicipalityLevel()) {
			getMunicipalityConfigStatus().setListProposals(value);
		}
	}

	@Override
	public boolean isDoneStatus() {
		if (isCountyLevel()) {
			return getCountyConfigStatus().isListProposals();
		}
		if (isMunicipalityLevel()) {
			return getMunicipalityConfigStatus().isListProposals();
		}
		return false;
	}

	@Override
	public boolean isEditable() {
		return parentIsEditable() && listProposal != null && !listProposal.isCountStarted();
	}

	ListProposalConfig loadData() {
		listProposal = getListProposalService().findByArea(getUserData(), getAreaPath());
		return listProposal;
	}

	boolean parentIsEditable() {
		return super.isEditable();
	}

	public void setListProposal(ListProposalConfig listProposal) {
		this.listProposal = listProposal;
	}

	public ListProposalConfig getListProposal() {
		return listProposal;
	}

	@Override
	public ContestListProposalData getContestListProposalData() {
		return getListProposal().getContestListProposalData();
	}

	@Override
	public String getAjaxKeyUpUpdate() {
		return isAjaxKeyUpDisabled() ? "" : "@(.onStateChanged)";
	}

	@Override
	public boolean isAjaxKeyUpDisabled() {
		return !isDoneStatus();
	}

	@Override
	public void ajaxKeyUpListener() {
		if (isDoneStatus()) {
			unlock();
		}
	}

	@Override
	public boolean isListProposalWriteMode() {
		return true;
	}

	public ListProposalService getListProposalService() {
		return listProposalService;
	}
}
