package no.valg.eva.admin.common.configuration.service;

import java.io.Serializable;
import java.util.List;

import no.evote.security.UserData;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.configuration.model.ElectoralRollSearch;
import no.valg.eva.admin.common.configuration.model.local.DisplayOrder;
import no.valg.eva.admin.common.configuration.model.local.ResponsibleOfficer;
import no.valg.eva.admin.common.counting.constants.ReportingUnitTypeId;

public interface ReportingUnitService extends Serializable {

	List<ResponsibleOfficer> findByArea(UserData userData, AreaPath areaPath);

	ResponsibleOfficer save(UserData userData, ResponsibleOfficer responsibleOfficer);
	
	List<ResponsibleOfficer> saveResponsibleOfficerDisplayOrder(UserData userData, AreaPath areaPath, List<DisplayOrder> displayOrders);

	List<ResponsibleOfficer> search(UserData userData, AreaPath areaPath, ElectoralRollSearch electoralRollSearch);

	void delete(UserData userData, ResponsibleOfficer responsibleOfficer);

	boolean hasReportingUnitTypeConfigured(UserData userData, ReportingUnitTypeId reportingUnitTypeId);

	boolean validate(UserData userData, ResponsibleOfficer selectedResponsibleOfficer);
}
