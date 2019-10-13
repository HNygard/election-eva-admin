package no.evote.service.configuration;

import static no.valg.eva.admin.common.rbac.Accesses.Aggregert_Stemmegiving;
import static no.valg.eva.admin.common.rbac.SecurityType.READ;

import java.util.List;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.inject.Inject;

import no.evote.model.views.Eligibility;
import no.evote.security.UserData;
import no.evote.service.EligibilityService;
import no.valg.eva.admin.common.rbac.Security;
import no.valg.eva.admin.configuration.domain.model.Voter;
import no.valg.eva.admin.configuration.repository.EligibilityRepository;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;

/**
 * NOTE! This is legacy code and based on the old architecture and should not be extended and or build upon!
 */
@Stateless(name = "EligibilityService")


@Default
@Remote(EligibilityService.class)
public class EligibilityServiceEjb implements EligibilityService {
	@Inject
	private EligibilityRepository eligibilityRepository;

	/**
	 * It is the same as findEligibilityForVoterInGroup, but it does not check on date of birth.
	 */
	@Override
	@Security(accesses = Aggregert_Stemmegiving, type = READ)
	public List<Eligibility> findTheoreticalEligibilityForVoterInGroup(UserData userData, Voter voter, Long electionGroupPk) {
		return eligibilityRepository.findTheoreticalEligibilityForVoterInGroup(voter, electionGroupPk);
	}
}
