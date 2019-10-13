package no.evote.service.configuration;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.inject.Inject;

import no.evote.security.UserData;
import no.valg.eva.admin.common.configuration.status.CountyStatusEnum;
import no.valg.eva.admin.configuration.domain.model.Country;
import no.valg.eva.admin.configuration.domain.model.County;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.repository.CountryRepository;
import no.valg.eva.admin.configuration.repository.CountyRepository;
import no.valg.eva.admin.configuration.repository.MunicipalityRepository;

/**
 */
@Default
@ApplicationScoped
public class CountyServiceBean {
	@Inject
	private CountyRepository countyRepository;
	@Inject
	private CountryRepository countryRepository;
	@Inject
	private MunicipalityRepository municipalityRepository;

	public CountyServiceBean() {

	}

	public County create(UserData userData, County county) {
		// Set correct countyStatus on new county. Either central or local, based on electionEvent.
		Country country = countryRepository.findByPk(county.getCountry().getPk());
		CountyStatusEnum status = CountyStatusEnum.CENTRAL_CONFIGURATION;
		if (country.getElectionEvent().isLocalConfiguration()) {
			status = CountyStatusEnum.LOCAL_CONFIGURATION;
		}
		if (county.getLocale() == null) {
			county.setLocale(userData.getLocale());
		}
		county.setCountyStatus(countyRepository.findCountyStatusById(status.id()));
		return countyRepository.create(userData, county);
	}

	public County findCountyById(Long countryPk, String id) {
		return countyRepository.findCountyById(countryPk, id);
	}

	public List<County> getCountiesWithoutMunicipalities(Long electionEventPk) {
		List<Country> countries = countryRepository.getCountriesForElectionEvent(electionEventPk);
		List<County> counties = new ArrayList<>();
		for (Country country : countries) {
			for (County county : countyRepository.getCountiesByCountry(country.getPk())) {
				if (!municipalityRepository.hasMunicipalities(county)) {
					counties.add(county);
				}
			}
		}
		return counties;
	}

	public void electionEventStatusChanged(ElectionEvent electionEvent) {
		if (electionEvent.isCentralConfiguration()) {
			countyRepository.updateStatusOnCounties(electionEvent.getPk(), CountyStatusEnum.LOCAL_CONFIGURATION, CountyStatusEnum.CENTRAL_CONFIGURATION);
		} else if (electionEvent.isLocalConfiguration()) {
			countyRepository.updateStatusOnCounties(electionEvent.getPk(), CountyStatusEnum.CENTRAL_CONFIGURATION, CountyStatusEnum.LOCAL_CONFIGURATION);
		}
	}

	public List<County> getCountiesByStatus(Long electionEventPk, Integer status) {
		return countryRepository.getCountiesByStatus(electionEventPk, status);
	}
}
