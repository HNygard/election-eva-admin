package no.valg.eva.admin.configuration.domain.service;

import lombok.AllArgsConstructor;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.PersonId;
import no.valg.eva.admin.common.mockups.BallotMockups;
import no.valg.eva.admin.configuration.domain.model.Ballot;
import no.valg.eva.admin.configuration.domain.model.Candidate;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.ResponsibilityConflict;
import no.valg.eva.admin.configuration.domain.model.ResponsibilityConflictType;
import no.valg.eva.admin.configuration.repository.CandidateRepository;
import no.valg.eva.admin.rbac.domain.model.Role;
import no.valg.eva.admin.rbac.repository.RoleRepository;
import no.valg.eva.admin.test.MockUtilsTestCase;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static java.lang.String.join;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static no.valg.eva.admin.configuration.domain.OmraadehierarkiObjectMother.mvArea;
import static no.valg.eva.admin.configuration.domain.ValghierarkiObjectMother.contest;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

public class ResponsibilityValidationDomainServiceCandidateConflictTest extends MockUtilsTestCase {

    private static final PersonId PERSON_ID = new PersonId("01018112345");
    private static final String PARTY_NAME = "DEM";
    private static final int EXPECTED_CONFLICT_MESSAGE_ARGS = 4;
    
    private static final NamedAreaPath COUNTY_OSLO =            NamedAreaPath.from("790001.47.03", "Oslo");
    private static final NamedAreaPath MUNICIPALITY_OSLO =      NamedAreaPath.from("790001.47.03.0301", "Oslo");
    private static final NamedAreaPath BOROUGH_SAGENE =         NamedAreaPath.from("790001.47.03.0301.000003", "Sagene");
    private static final NamedAreaPath BOROUGH_VESTRE_AKER =    NamedAreaPath.from("790001.47.03.0301.000005", "Vestre Aker");
    private static final NamedAreaPath COUNTY_VESTFOLD =        NamedAreaPath.from("790001.47.07", "Vestfold");
    private static final NamedAreaPath MUNICIPALITY_HORTEN =    NamedAreaPath.from("790001.47.07.0701", "Horten");
    private static final String ROLE_ID = "test_role";
    private static final String FIRST_NAME = "firstName";
    private static final String MIDDLE_NAME = "middleName";
    private static final String LAST_NAME = "lastName";

    private ResponsibilityValidationDomainService domainService;
    private CandidateRepository repository;

    @BeforeMethod
    public void setup() throws Exception {
        domainService = initializeMocks(ResponsibilityValidationDomainService.class);
        repository = getInjectMock(CandidateRepository.class);
    }
    
    @Test(dataProvider = "candidateConflictTestData")
    public void candidateInAreas_whenCheckingInArea(List<Candidate> candidateList, NamedAreaPath validatingArea, List<NamedAreaPath> expectedConflicts) {

        AreaPath validatingAreaPath = AreaPath.from(validatingArea.areaPath);
        when(repository.findCandidateAtCountyOrBelow(PERSON_ID.getId(), validatingAreaPath)).thenReturn(candidateList);
        Role role = mockRole();
        when(getInjectMock(RoleRepository.class).findRolesByCheckCandidateConflict(any())).thenReturn(singletonList(role));

        List<ResponsibilityConflict> actualConflicts = domainService.checkIfPersonHasCandidateConflict(PERSON_ID, validatingAreaPath, ROLE_ID, new ElectionEvent());
        
        assertEquals(actualConflicts.size(), expectedConflicts.size());
        
        for(int i = 0; i < expectedConflicts.size(); i++) {
            NamedAreaPath namedAreaPath = expectedConflicts.get(i);
            ResponsibilityConflict conflict = actualConflicts.get(i);
            
            assertEquals(conflict.getType(), ResponsibilityConflictType.CANDIDATE_ID);
            assertEquals(conflict.getMessageArguments().size(), EXPECTED_CONFLICT_MESSAGE_ARGS);
            assertEquals(conflict.getMessageArguments().get(0), buildFullName());
            assertEquals(conflict.getMessageArguments().get(1), PARTY_NAME);
            assertEquals(conflict.getMessageArguments().get(2), namedAreaPath.name);
        }
    }

    private String buildFullName() {
        return join(" ", FIRST_NAME, MIDDLE_NAME, LAST_NAME);
    }

    private Role mockRole() {
        Role role = mock(Role.class);
        when(role.getId()).thenReturn(ROLE_ID);
        when(role.isCheckCandidateConflicts()).thenReturn(true);
        return role;
    }

    @DataProvider
    public Object[][] candidateConflictTestData() {
        return new Object[][] {
                { candidatesInArea(COUNTY_VESTFOLD), whenValidating(COUNTY_VESTFOLD), expectConflictInArea(COUNTY_VESTFOLD) },
                { candidatesInArea(COUNTY_VESTFOLD), whenValidating(COUNTY_OSLO), expectNoConflict() },

                { candidatesInArea(COUNTY_VESTFOLD), whenValidating(MUNICIPALITY_HORTEN), expectConflictInArea(COUNTY_VESTFOLD) },
                { candidatesInArea(COUNTY_VESTFOLD), whenValidating(MUNICIPALITY_OSLO), expectNoConflict() },

                { candidatesInArea(MUNICIPALITY_HORTEN), whenValidating(MUNICIPALITY_HORTEN), expectConflictInArea(MUNICIPALITY_HORTEN) },
                { candidatesInArea(MUNICIPALITY_HORTEN), whenValidating(COUNTY_VESTFOLD), expectNoConflict() },
                { candidatesInArea(MUNICIPALITY_HORTEN), whenValidating(MUNICIPALITY_OSLO), expectNoConflict() },
                { candidatesInArea(MUNICIPALITY_HORTEN), whenValidating(COUNTY_OSLO), expectNoConflict() },

                { candidatesInArea(BOROUGH_SAGENE), whenValidating(BOROUGH_SAGENE), expectConflictInArea(BOROUGH_SAGENE) },
                { candidatesInArea(BOROUGH_SAGENE), whenValidating(BOROUGH_VESTRE_AKER), expectConflictInArea(BOROUGH_SAGENE) },
                { candidatesInArea(BOROUGH_SAGENE), whenValidating(MUNICIPALITY_OSLO), expectConflictInArea(BOROUGH_SAGENE) },
                { candidatesInArea(BOROUGH_SAGENE), whenValidating(COUNTY_OSLO), expectNoConflict() },
                { candidatesInArea(BOROUGH_SAGENE), whenValidating(MUNICIPALITY_HORTEN), expectNoConflict() },
                { candidatesInArea(BOROUGH_SAGENE), whenValidating(COUNTY_VESTFOLD), expectNoConflict() },

                { candidatesInArea(COUNTY_VESTFOLD, MUNICIPALITY_HORTEN), whenValidating(COUNTY_VESTFOLD), expectConflictInArea(COUNTY_VESTFOLD) },
                { candidatesInArea(COUNTY_VESTFOLD, MUNICIPALITY_HORTEN), whenValidating(MUNICIPALITY_HORTEN), expectConflictInArea(COUNTY_VESTFOLD, MUNICIPALITY_HORTEN) },
                { candidatesInArea(COUNTY_VESTFOLD, MUNICIPALITY_HORTEN), whenValidating(MUNICIPALITY_OSLO), expectNoConflict() },

                { candidatesInArea(COUNTY_OSLO, MUNICIPALITY_OSLO, BOROUGH_SAGENE), whenValidating(COUNTY_OSLO), expectConflictInArea(COUNTY_OSLO) },
                { candidatesInArea(COUNTY_OSLO, MUNICIPALITY_OSLO, BOROUGH_SAGENE), whenValidating(MUNICIPALITY_OSLO), expectConflictInArea(COUNTY_OSLO, MUNICIPALITY_OSLO, BOROUGH_SAGENE) },
                { candidatesInArea(COUNTY_OSLO, MUNICIPALITY_OSLO, BOROUGH_SAGENE), whenValidating(BOROUGH_SAGENE), expectConflictInArea(COUNTY_OSLO, MUNICIPALITY_OSLO, BOROUGH_SAGENE) },
                { candidatesInArea(COUNTY_OSLO, MUNICIPALITY_OSLO, BOROUGH_SAGENE), whenValidating(BOROUGH_VESTRE_AKER), expectConflictInArea(COUNTY_OSLO, MUNICIPALITY_OSLO, BOROUGH_SAGENE) },
                { candidatesInArea(COUNTY_OSLO, MUNICIPALITY_OSLO, BOROUGH_SAGENE), whenValidating(MUNICIPALITY_HORTEN), expectNoConflict() },
                { candidatesInArea(COUNTY_OSLO, MUNICIPALITY_OSLO, BOROUGH_SAGENE), whenValidating(COUNTY_VESTFOLD), expectNoConflict() }
        };
    }

    private List<Candidate> candidatesInArea(NamedAreaPath... naps) {
        return Arrays.stream(naps)
                .map(this::candidateInArea)
                .collect(toList());
    }

    private Candidate candidateInArea(NamedAreaPath nap) {
        Ballot ballot = BallotMockups.demBallot();
        ballot.getAffiliation().getParty().setName(PARTY_NAME);
        ballot.setContest(contest(mvArea(nap.areaPath, nap.name)));

        Candidate candidate = Candidate.builder()
                .firstName(FIRST_NAME)
                .middleName(MIDDLE_NAME)
                .lastName(LAST_NAME)
                .id(PERSON_ID.getId())
                .ballot(ballot)
                .build();
        
        candidate.setPk(new Random().nextLong());
        
        return candidate;
    }
    
    private NamedAreaPath whenValidating(NamedAreaPath nap) {
        return nap;
    }
    
    private List<NamedAreaPath> expectConflictInArea(NamedAreaPath... naps) {
        return asList(naps);
    }
    
    private List<NamedAreaPath> expectNoConflict() {
        return emptyList();
    }
    
    @AllArgsConstructor
    private static class NamedAreaPath {
        String areaPath;
        String name;

        public static NamedAreaPath from(String areaPath, String name) {
            return new NamedAreaPath(areaPath, name);
        }
    }
}
