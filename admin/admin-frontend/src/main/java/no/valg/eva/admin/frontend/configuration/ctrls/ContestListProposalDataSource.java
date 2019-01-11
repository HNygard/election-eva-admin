package no.valg.eva.admin.frontend.configuration.ctrls;

import no.valg.eva.admin.common.configuration.model.central.ContestListProposalData;

public interface ContestListProposalDataSource {

	ContestListProposalData getContestListProposalData();

	String getAjaxKeyUpUpdate();

	boolean isAjaxKeyUpDisabled();

	void ajaxKeyUpListener();

	boolean isListProposalWriteMode();

	void saveListProposal();
	
	void prepareForSave();
}
