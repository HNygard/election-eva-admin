package no.valg.eva.admin.frontend.contest;

import java.util.List;

import no.valg.eva.admin.common.counting.model.ContestInfo;

public interface SelectContestProvider {

	String getSelectContestHeader();

	void setContestPath(String contestPath);

	String getContestPath();

	List<ContestInfo> getContestList();

	String selectContest();
}
