package no.valg.eva.admin.configuration.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import no.valg.eva.admin.common.MunicipalityId;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.test.AbstractJpaTestBase;
import no.valg.eva.admin.test.TestGroups;

import org.joda.time.LocalDate;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test(groups = TestGroups.REPOSITORY)
public class EligibilityRepositoryTest extends AbstractJpaTestBase  {

    private static final int NO_OF_MUNICIPALITIES_WITH_ELIGIBILITY_IN_2007_ELECTIONS = 431;
    
    private static final LocalDate MOST_BE_BORN_BEOFRE_DATE_IN_2007_ELECTIONS = new LocalDate(1993, 12, 31);
    
    private static final String MUNICIPALITY_ID_HORTEN = "0701";
    private static final ElectionEvent ELECTION_EVENT_2007 = new ElectionEvent(2L);

    private EligibilityRepository eligibilityRepository;

    @BeforeMethod(alwaysRun = true)
    public void init() {
        eligibilityRepository = new EligibilityRepository(getEntityManager());
    }

    @Test
    public void findMaxEndBirthDateForEachMunicipalityInElectionEvent_returnsTheDateVotersMustBeBornBeforeForEachMunicipality() {

        Map<MunicipalityId, LocalDate> maxEndBirthDateForEachMunicipalityInElectionEvent =
                eligibilityRepository.findMaxEndBirthDateForEachMunicipalityInElectionEvent(ELECTION_EVENT_2007);

        assertThat(maxEndBirthDateForEachMunicipalityInElectionEvent.size()).isEqualTo(NO_OF_MUNICIPALITIES_WITH_ELIGIBILITY_IN_2007_ELECTIONS);
        assertThat(maxEndBirthDateForEachMunicipalityInElectionEvent.get(new MunicipalityId(MUNICIPALITY_ID_HORTEN)))
            .isEqualTo(MOST_BE_BORN_BEOFRE_DATE_IN_2007_ELECTIONS);
    }
}

