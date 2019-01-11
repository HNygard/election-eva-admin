package no.valg.eva.admin.configuration.domain.service;

import no.evote.security.UserData;
import no.valg.eva.admin.configuration.domain.model.LegacyPollingDistrict;
import no.valg.eva.admin.configuration.domain.model.Voter;
import no.valg.eva.admin.configuration.repository.LegacyPollingDistrictRepository;
import no.valg.eva.admin.configuration.repository.VoterRepository;
import no.valg.eva.admin.test.MockUtilsTestCase;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class VelgerDomainServiceTest extends MockUtilsTestCase {

    private VelgerDomainService velgerDomainService;
    private LegacyPollingDistrictRepository legacyPollingDistrictRepository;
    private VoterRepository voterRepository;
    private UserData userData;
    
    @BeforeMethod
    public void setup() throws Exception {
        velgerDomainService = initializeMocks(VelgerDomainService.class);
        legacyPollingDistrictRepository = getInjectMock(LegacyPollingDistrictRepository.class);
        voterRepository = getInjectMock(VoterRepository.class);
        userData = createMock(UserData.class);
    }

    @Test
    public void UpdateVoterInNewTransaction_withLegacyPollingDistricts_onlyUpdatesLegacyPollingDistrictsWithAssociatedVoter() {
        List<Voter> voters = voters(3);
        List<LegacyPollingDistrict> allPollingDistricts = legacyPollingDistricts(5, voters);
        List<LegacyPollingDistrict> expectedPollingDistricts = expectedLegacyPollingDistricts(allPollingDistricts);
        when(voterRepository.updateVoters(userData, voters)).thenReturn(voters);

        velgerDomainService.updateVoterInNewTransaction(userData, voters, allPollingDistricts);
        
        verify(legacyPollingDistrictRepository).updateLegacyPollingDistricts(userData, expectedPollingDistricts);
    }
    
    @Test
    public void updateVoter_withLegacyPollingDistrict_updatesLegacyPollingDistrict() {
        Voter voter = voter(0);
        LegacyPollingDistrict legacyPollingDistrict = legacyPollingDistrict(0);
        legacyPollingDistrict.setVoter(voter);
        
        velgerDomainService.updateVoter(userData, voter, legacyPollingDistrict);
        
        verify(legacyPollingDistrictRepository).create(userData, legacyPollingDistrict);
    }

    private List<Voter> voters(int num) {
        List<Voter> voters = new ArrayList<>();
        for (int i = 0; i < num; i++) {
            voters.add(voter(i));
        }
        return voters;
    }

    private Voter voter(int id) {
        Voter voter = new Voter();
        voter.setId(String.valueOf(id));
        return voter;
    }

    private List<LegacyPollingDistrict> legacyPollingDistricts(int num, List<Voter> voters) {
        List<LegacyPollingDistrict> legacyPollingDistricts = new ArrayList<>();
        for (int i = 0; i < num; i++) {
            LegacyPollingDistrict lpd = legacyPollingDistrict(i);
            if (i < voters.size()) {
                lpd.setVoter(voters.get(i));
            }
            legacyPollingDistricts.add(lpd);
        }
        return legacyPollingDistricts;
    }

    private LegacyPollingDistrict legacyPollingDistrict(int id) {
        LegacyPollingDistrict legacyPollingDistrict = new LegacyPollingDistrict();
        legacyPollingDistrict.setLegacyPollingDistrictId(String.valueOf(id));
        legacyPollingDistrict.setLegacyMunicipalityId(String.valueOf(id));
        return legacyPollingDistrict;
    }

    private List<LegacyPollingDistrict> expectedLegacyPollingDistricts(List<LegacyPollingDistrict> allPollingDistricts) {
        return allPollingDistricts.stream()
                .filter(e -> e.getVoter() != null)
                .collect(Collectors.toList());
    }
}
