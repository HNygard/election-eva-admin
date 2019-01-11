package no.evote.service.configuration;

import java.io.Serializable;
import java.util.List;

import no.evote.dto.ReportingUnitTypeDto;
import no.evote.security.UserData;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.configuration.domain.model.ReportingUnitType;

public interface ReportingUnitTypeService extends Serializable {

	List<ReportingUnitTypeDto> populateReportingUnitTypeDto(UserData userData,  String electionEventId);

	List<ReportingUnitType> findAll(final UserData userData);

	void updateMvElectionReportingUnits(UserData userData, List<MvElection> mvElectionList, long reportingUnitTypePk);

}
