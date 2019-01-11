package no.evote.service.configuration;

import java.io.Serializable;

import no.evote.security.UserData;
import no.valg.eva.admin.configuration.domain.model.Country;

public interface CountryService extends Serializable {
	Country findCountryById(UserData userData, Long electionEventPk, String id);

	Country create(UserData userData, Country country);

	Country update(UserData userData, Country country);

	void delete(UserData userData, Country country);

	Country findByPk(UserData userData, Long pk);
}
