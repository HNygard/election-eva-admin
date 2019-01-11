package no.valg.eva.admin.configuration.application;

import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.configuration.model.local.ResponsibleOfficer;
import no.valg.eva.admin.common.counting.constants.ResponsibilityId;
import no.valg.eva.admin.configuration.domain.model.Voter;

public final class ResponsibleOfficerMapper {

	private ResponsibleOfficerMapper() {
	}

	public static ResponsibleOfficer toResponsibleOfficer(no.valg.eva.admin.configuration.domain.model.ResponsibleOfficer dbOfficer) {
		ResponsibleOfficer result = new ResponsibleOfficer(dbOfficer.getAuditOplock());
		result.setPk(dbOfficer.getPk());
		result.setAreaPath(AreaPath.from(dbOfficer.getReportingUnit().getMvArea().getAreaPath()));
		result.setResponsibilityId(ResponsibilityId.fromId(dbOfficer.getResponsibility().getId()));
		result.setDisplayOrder(dbOfficer.getDisplayOrder());
		result.setFirstName(dbOfficer.getFirstName());
		result.setMiddleName(dbOfficer.getMiddleName());
		result.setLastName(dbOfficer.getLastName());
		result.setAddress(dbOfficer.getAddressLine1());
		result.setPostalCode(dbOfficer.getPostalCode());
		result.setPostalTown(dbOfficer.getPostTown());
		result.setEmail(dbOfficer.getEmail());
		result.setTlf(dbOfficer.getTelephoneNumber());
		
		return result;
	}

	public static ResponsibleOfficer toResponsibleOfficer(Voter voter) {
		ResponsibleOfficer result = new ResponsibleOfficer();
		result.setId(voter.getId());
		result.setAge(voter.getAgeInYears());
		result.setFirstName(voter.getFirstName());
		result.setMiddleName(voter.getMiddleName());
		result.setLastName(voter.getLastName());
		result.setAddress(voter.getAddressLine1());
		result.setPostalCode(voter.getPostalCode());
		result.setPostalTown(voter.getPostTown());
		result.setEmail(voter.getEmail());
		result.setTlf(voter.getTelephoneNumber());
		return result;
	}

	public static void merge(no.valg.eva.admin.configuration.domain.model.ResponsibleOfficer dbOfficer, ResponsibleOfficer officer) {
		dbOfficer.setDisplayOrder(officer.getDisplayOrder());
		dbOfficer.setFirstName(officer.getFirstName());
		dbOfficer.setMiddleName(officer.getMiddleName());
		dbOfficer.setLastName(officer.getLastName());
		dbOfficer.setAddressLine1(officer.getAddress());
		dbOfficer.setAddressLine2(null);
		dbOfficer.setAddressLine3(null);
		dbOfficer.setPostalCode(officer.getPostalCode());
		dbOfficer.setPostTown(officer.getPostalTown());
		dbOfficer.setEmail(officer.getEmail());
		dbOfficer.setTelephoneNumber(officer.getTlf());
	}
}
