package no.valg.eva.admin.common.counting.service;

import java.io.Serializable;
import java.util.List;

import no.evote.security.UserData;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.counting.model.ContestInfo;
import no.valg.eva.admin.common.counting.model.countingoverview.CountingOverviewRoot;

public interface CountingOverviewService extends Serializable {
	/**
	 * @return liste av tilgjengelige valg for en gitt bruker og områdesti
	 */
	List<ContestInfo> electionsFor(UserData userData, AreaPath areaPath);

	/**
	 * @return en eller flere oversikt over tellinger (én per kommune/bydel) for en gitt bruker, valghierarkisti og områdesti
	 */
	List<CountingOverviewRoot> countingOverviewsFor(UserData userData, ElectionPath electionPath, AreaPath areaPath);
}
