package no.evote.service.configuration;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import no.evote.dto.ConfigurationDto;
import no.evote.exception.EvoteException;
import no.evote.security.UserData;
import no.valg.eva.admin.configuration.domain.model.Borough;
import no.valg.eva.admin.configuration.domain.model.Country;
import no.valg.eva.admin.configuration.repository.BoroughRepository;
import no.valg.eva.admin.configuration.repository.CountryRepository;

/**
 * NOTE! This is legacy code and based on the old architecture and should not be extended and or build upon!
 */
public class BoroughServiceBean {
	@Inject
	private BoroughRepository boroughRepository;
	@Inject
	private CountryRepository countryRepository;

	public Borough create(UserData userData, Borough borough) {
		// The municipality can only have one borough for the whole municipality
		if (borough.isMunicipality1() && isBoroughForWholeMunicipalityAlreadyExisting(borough.getMunicipality().getPk())) {
			throw new EvoteException("@common.message.evote_application_exception.DUPLICATE_BOROUGH_WHOLE_MUNICIPALITY");
		}

		return boroughRepository.createBorough(userData, borough);
	}

	public Borough update(UserData userData, Borough borough) {
		// The municipality can only have one borough for the whole municipality
		if (borough.isMunicipality1() && isBoroughForWholeMunicipalityAlreadyExisting(borough.getMunicipality().getPk())
				&& !boroughRepository.findBoroughByPk(borough.getPk()).isMunicipality1()) {
			throw new EvoteException("@common.message.evote_application_exception.DUPLICATE_BOROUGH_WHOLE_MUNICIPALITY");
		}

		return boroughRepository.updateBorough(userData, borough);
	}

	/**
	 * Only one borough for the whole municipality can exist for each municipality.
	 */
	private boolean isBoroughForWholeMunicipalityAlreadyExisting(Long municipalityPk) {
		return boroughRepository.isBoroughForWholeMunicipalityAlreadyExisting(municipalityPk);
	}

	public List<ConfigurationDto> getBoroughsWithoutPollingDistricts(Long electionEventPk) {
		List<Country> countries = countryRepository.getCountriesForElectionEvent(electionEventPk);
		List<Borough> boroughs = boroughRepository.findWithoutPollingDistricts(countries);

		List<ConfigurationDto> configData = new ArrayList<>();
		for (Borough borough : boroughs) {
			configData.add(new ConfigurationDto(borough.getMunicipality().getId() + "." + borough.getId(), borough.getMunicipality().getName() + ", "
					+ borough.getName()));
		}

		return configData;
	}

}
