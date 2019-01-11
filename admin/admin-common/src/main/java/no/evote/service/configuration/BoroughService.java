package no.evote.service.configuration;

import java.io.Serializable;
import java.util.List;

import no.evote.security.UserData;
import no.valg.eva.admin.configuration.domain.model.Borough;

/**
 * NOTE! This is legacy code and based on the old architecture and should not be extended and or build upon!
 */
public interface BoroughService extends Serializable {
	Borough create(UserData userData, Borough borough);

	Borough update(UserData userData, Borough borough);

	void delete(UserData userData, Borough borough);

	Borough findBoroughById(UserData userData, final Long municipalityPk, final String id);

	Borough findByPk(UserData userData, Long boroughPk);

	List<Borough> findByMunicipality(UserData userData, Long municipalityPk);
}
