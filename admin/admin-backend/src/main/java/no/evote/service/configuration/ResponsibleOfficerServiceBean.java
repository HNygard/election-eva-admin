package no.evote.service.configuration;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.inject.Inject;

import no.evote.security.UserData;
import no.valg.eva.admin.configuration.domain.model.ReportingUnit;
import no.valg.eva.admin.configuration.domain.model.ResponsibleOfficer;
import no.valg.eva.admin.configuration.repository.ResponsibleOfficerRepository;

@Default
@ApplicationScoped
public class ResponsibleOfficerServiceBean {

	// Injected
	@Inject
	private ResponsibleOfficerRepository responsibleOfficerRepository;

	public ResponsibleOfficerServiceBean() {
	}

	public ResponsibleOfficerServiceBean(ResponsibleOfficerRepository responsibleOfficerRepository) {
		this.responsibleOfficerRepository = responsibleOfficerRepository;
	}

	public ResponsibleOfficer save(UserData userData, ResponsibleOfficer responsibleOfficer) {
		responsibleOfficer.updateNameLine();
		if (responsibleOfficer.getPk() == null) {
			responsibleOfficer.setDisplayOrder(responsibleOfficerRepository.findNextDisplayOrder(responsibleOfficer.getReportingUnit().getPk()));
			return responsibleOfficerRepository.create(userData, responsibleOfficer);
		}
		return responsibleOfficerRepository.update(userData, responsibleOfficer);
	}

	public void delete(UserData userData, ResponsibleOfficer responsibleOfficer) {
		ReportingUnit reportingUnit = responsibleOfficer.getReportingUnit();
		responsibleOfficerRepository.delete(userData, responsibleOfficer.getPk());
		// Update display order after delete
		List<ResponsibleOfficer> list = responsibleOfficerRepository.findResponsibleOfficersForReportingUnit(reportingUnit.getPk());
		int displayOrder = 1;
		for (int i = 0; i < list.size(); i++) {
			ResponsibleOfficer officer = list.get(i);
			if (officer.getDisplayOrder() != displayOrder) {
				officer.setDisplayOrder(displayOrder);
				responsibleOfficerRepository.update(userData, officer);
			}
			displayOrder++;
		}
	}
}
