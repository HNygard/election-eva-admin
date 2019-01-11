package no.evote.service.configuration;

import java.io.Serializable;
import java.util.List;

import no.evote.security.UserData;
import no.valg.eva.admin.common.configuration.model.party.Parti;
import no.valg.eva.admin.configuration.domain.model.Affiliation;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.Locale;

/**
 * NOTE! This is legacy code and based on the old architecture and should not be extended and or build upon!
 */
public interface AffiliationService extends Serializable {
	Affiliation createNewAffiliation(UserData userData, Contest contest, Parti parti, Locale locale, int ballotStatus);
	
	List<Affiliation> findByContest(UserData userData, Long pk);

	Affiliation findByBallot(UserData userData, Long ballotPk);

	Affiliation createNewPartyAndAffiliation(UserData userData, Contest currentContest, Parti parti, Locale locale);

	Affiliation findByPk(UserData userData, Long pk);

	Affiliation saveColumns(UserData userData, Affiliation currentAffiliation);
	
	List<Affiliation> changeDisplayOrder(UserData userData, Affiliation affiliation, int fromPosition, int toPosition);
}
