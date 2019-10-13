package no.evote.service.configuration;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.inject.Inject;

import no.evote.exception.EvoteSecurityException;
import no.evote.security.UserData;
import no.valg.eva.admin.configuration.domain.model.PollingDistrict;
import no.valg.eva.admin.configuration.repository.PollingDistrictRepository;

@Default
@ApplicationScoped
public class PollingDistrictServiceBean {
	@Inject
	private PollingDistrictRepository pollingDistrictRepository;

	public PollingDistrictServiceBean() {

	}

	public PollingDistrict createParentPollingDistrict(UserData userData, PollingDistrict pollingDistrict, List<PollingDistrict> childrenPollingDistricts) {
		if (!pollingDistrict.isParentPollingDistrict()) {
			throw new EvoteSecurityException("Polling district is not a parent polling district");
		}
		PollingDistrict createdPollingDistrict = pollingDistrictRepository.create(userData, pollingDistrict);
		for (PollingDistrict childPollingDistrict : childrenPollingDistricts) {
			childPollingDistrict.setPollingDistrict(pollingDistrict);
			pollingDistrictRepository.update(userData, childPollingDistrict);
		}
		return createdPollingDistrict;
	}
}
