package no.valg.eva.admin.voting.domain.electoralroll;

import static no.valg.eva.admin.voting.domain.electoralroll.ElectoralRollObjectMother.BORN_BEFORE_1998;
import static no.valg.eva.admin.voting.domain.electoralroll.ElectoralRollObjectMother.BORN_BEFORE_2000;
import static no.valg.eva.admin.voting.domain.electoralroll.ElectoralRollObjectMother.MUNICIPALITY_ID_PORSGRUNN;
import static no.valg.eva.admin.voting.domain.electoralroll.ElectoralRollObjectMother.MUNICIPALITY_ID_STAVANGER;
import static no.valg.eva.admin.voting.domain.electoralroll.ElectoralRollObjectMother.MUNICIPALITY_ID_SVALBARD;
import static no.valg.eva.admin.voting.domain.electoralroll.ElectoralRollObjectMother.MUST_BE_BORN_BEFORE_PORSGRUNN;
import static no.valg.eva.admin.voting.domain.electoralroll.ElectoralRollObjectMother.MUST_BE_BORN_BEFORE_STAVANGER;
import static no.valg.eva.admin.voting.domain.electoralroll.ElectoralRollObjectMother.MUST_BE_BORN_BEFORE_SVALBARD;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import no.valg.eva.admin.common.MunicipalityId;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.repository.EligibilityRepository;
import no.valg.eva.admin.test.MockUtilsTestCase;

import org.joda.time.LocalDate;
import org.testng.annotations.Test;

public class EligibleVoterDomainServiceTest extends MockUtilsTestCase {

	@Test
	public void buildEligibilityInMunicipalityMap_returnsNewestDateSpecifiedInContestOrElection() throws Exception {
		EligibleVoterDomainService eligibleVoterDomainService = initializeMocks(EligibleVoterDomainService.class);

		ElectionEvent electionEvent = new ElectionEvent();
		Map<MunicipalityId, LocalDate> eligibilityMap = buildEligibilityMap();
		when(getInjectMock(EligibilityRepository.class).findMaxEndBirthDateForEachMunicipalityInElectionEvent(electionEvent)).thenReturn(eligibilityMap);

		StemmerettIKommune stemmerettIKommune = eligibleVoterDomainService.buildEligibilityMap(electionEvent);

		assertThat(stemmerettIKommune.get(new MunicipalityId(MUNICIPALITY_ID_PORSGRUNN))).isEqualTo(BORN_BEFORE_2000);
		assertThat(stemmerettIKommune.get(new MunicipalityId(MUNICIPALITY_ID_STAVANGER))).isEqualTo(BORN_BEFORE_2000);
		assertThat(stemmerettIKommune.get(new MunicipalityId(MUNICIPALITY_ID_SVALBARD))).isEqualTo(BORN_BEFORE_1998);
	}

	private Map<MunicipalityId, LocalDate> buildEligibilityMap() {
		Map<MunicipalityId, LocalDate> eligibilityMap = new HashMap<>();

		eligibilityMap.put(new MunicipalityId(MUNICIPALITY_ID_PORSGRUNN), MUST_BE_BORN_BEFORE_PORSGRUNN);
		eligibilityMap.put(new MunicipalityId(MUNICIPALITY_ID_STAVANGER), MUST_BE_BORN_BEFORE_STAVANGER);
		eligibilityMap.put(new MunicipalityId(MUNICIPALITY_ID_SVALBARD), MUST_BE_BORN_BEFORE_SVALBARD);

		return eligibilityMap;
	}
}
