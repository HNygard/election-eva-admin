package no.valg.eva.admin.frontend.counting.view.mapper;

import java.io.Serializable;

import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.counting.model.ContestInfo;
import no.valg.eva.admin.frontend.counting.view.CountingOverviewTabModel;

public class CountingOverviewTabModelMapper implements Serializable {
	public CountingOverviewTabModel countingOverviewTabModel(ContestInfo contestInfo) {
		ElectionPath electionPath = contestInfo.getElectionPath();
		String title;
		if (electionPath.getContestId() != null) {
			title = contestInfo.getElectionName() + " " + contestInfo.getContestName();
		} else {
			title = contestInfo.getElectionName();
		}
		return new CountingOverviewTabModel(electionPath, title);
	}
}
