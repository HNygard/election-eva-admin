package no.valg.eva.admin.voting.domain.electoralroll;


import no.valg.eva.admin.common.MunicipalityId;

import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.repository.EligibilityRepository;
import org.joda.time.LocalDate;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.inject.Inject;
import java.util.Map;

/**
 * The electoral roll files from SKD contains persons of age 16 and 17 who are normally not allowed to vote. In some elections or contests, 16 and 17 year old
 * persons can vote. EligibleVoterDomainService can be used for checking whether a person is eligible to vote or not.
 */
@Default
@ApplicationScoped
public class EligibleVoterDomainService {

	@Inject
	private EligibilityRepository eligibilityRepository;

	public StemmerettIKommune buildEligibilityMap(ElectionEvent electionEvent) {
		StemmerettIKommune stemmerettIKommune = new StemmerettIKommune(electionEvent);
		Map<MunicipalityId, LocalDate> eligibilityMap = eligibilityRepository.findMaxEndBirthDateForEachMunicipalityInElectionEvent(electionEvent);
		stemmerettIKommune.putAll(eligibilityMap);
		return stemmerettIKommune;
	}
}
