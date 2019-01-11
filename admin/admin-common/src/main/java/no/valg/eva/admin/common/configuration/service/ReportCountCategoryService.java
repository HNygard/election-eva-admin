package no.valg.eva.admin.common.configuration.service;

import java.io.Serializable;
import java.util.List;

import no.evote.security.UserData;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.configuration.model.local.ReportCountCategory;
import no.valg.eva.admin.common.counting.model.CountCategory;

public interface ReportCountCategoryService extends Serializable {

	List<ReportCountCategory> findCountCategoriesByArea(UserData userData, AreaPath areaPath, ElectionPath electionGroupPath);

	List<ReportCountCategory> findBoroughCountCategoriesByArea(UserData userData, AreaPath areaPath);

	List<ReportCountCategory> updateCountCategories(UserData userData, AreaPath areaPath, ElectionPath electionGroupPath,
		List<ReportCountCategory> reportCountCategories);
	
	ReportCountCategory findFirstByAreaAndCountCategory(UserData userData, AreaPath areaPath, ElectionPath electionGroupPath, CountCategory countCategory);
}
