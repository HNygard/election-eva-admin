package no.evote.service.counting;

import java.io.Serializable;

import no.evote.security.UserData;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;

public interface ContestReportService extends Serializable {
	
    boolean hasContestReport(UserData userData, Long pk);

	boolean hasContestReport(UserData userData, ElectionPath electionPath, AreaPath areaPath);
}
