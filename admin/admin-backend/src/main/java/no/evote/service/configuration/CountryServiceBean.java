package no.evote.service.configuration;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.inject.Inject;

import no.evote.security.UserData;
import no.valg.eva.admin.configuration.domain.model.Country;
import no.valg.eva.admin.configuration.repository.CountryRepository;
import no.valg.eva.admin.configuration.repository.CountyRepository;

/**
 */
@Default
@ApplicationScoped
public class CountryServiceBean {
	@Inject
	private CountryRepository countryRepository;
	@Inject
	private CountyRepository countyRepository;

	public CountryServiceBean() {

	}

	public Country create(UserData userData, Country country) {
		return countryRepository.create(userData, country);
	}

	public Country update(UserData userData, Country country) {
		return countryRepository.update(userData, country);
	}

	public void deleteByPk(UserData userData, Long countryPk) {
		countryRepository.deleteByPk(userData, countryPk);
	}

	public Country findByPk(Long pk) {
		return countryRepository.findByPk(pk);
	}

	public Country findCountryById(Long electionEventPk, String id) {
		return countryRepository.findCountryById(electionEventPk, id);
	}

	public List<Country> getCountriesWithoutCounties(Long electionEventPk) {
		List<Country> countriesMissingCounties = new ArrayList<>();
		List<Country> countries = countryRepository.getCountriesForElectionEvent(electionEventPk);
		for (Country country : countries) {
			if (!countyRepository.hasCounties(country)) {
				countriesMissingCounties.add(country);
			}
		}
		return countriesMissingCounties;
	}
}
