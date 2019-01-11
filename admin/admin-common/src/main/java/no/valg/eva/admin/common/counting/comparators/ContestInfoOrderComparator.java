package no.valg.eva.admin.common.counting.comparators;

import java.io.Serializable;
import java.util.Comparator;

import no.evote.constants.AreaLevelEnum;
import no.valg.eva.admin.common.counting.model.ContestInfo;

/**
 * Comparator for comparing ContestInfo and order by natural order.
 */
public class ContestInfoOrderComparator implements Comparator<ContestInfo>, Serializable {

	@Override
	public int compare(ContestInfo contestInfo1, ContestInfo contestInfo2) {
		contestInfo1 = boroughToMunicipality(contestInfo1);
		contestInfo2 = boroughToMunicipality(contestInfo2);
		if (contestInfo1.getAreaLevel().getLevel() == contestInfo2.getAreaLevel().getLevel()) {
			return contestInfo1.getElectionPath().path().compareTo(contestInfo2.getElectionPath().path());
		}
		return contestInfo1.getAreaLevel().getLevel() < contestInfo2.getAreaLevel().getLevel() ? 1 : -1;
	}

	private ContestInfo boroughToMunicipality(ContestInfo contestInfo) {
		if (contestInfo.getAreaLevel().equals(AreaLevelEnum.BOROUGH)) {
			contestInfo = new ContestInfo(contestInfo.getElectionPath().toElectionPath().path(), contestInfo.getElectionName(), null,
					contestInfo.getAreaPath().toMunicipalityPath().path());
		}
		return contestInfo;
	}
}
