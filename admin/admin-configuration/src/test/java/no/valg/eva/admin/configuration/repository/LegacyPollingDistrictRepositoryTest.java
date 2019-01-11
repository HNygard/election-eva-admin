package no.valg.eva.admin.configuration.repository;

import no.evote.security.UserData;
import no.valg.eva.admin.configuration.domain.model.LegacyPollingDistrict;
import no.valg.eva.admin.test.MockUtilsTestCase;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class LegacyPollingDistrictRepositoryTest extends MockUtilsTestCase {

    private LegacyPollingDistrictRepository legacyPollingDistrictRepository;
    private EntityManager entityManager;
    private UserData userData;

    @BeforeMethod
    public void setUp() {
        userData = createMock(UserData.class);
        entityManager = createMock(EntityManager.class);
        legacyPollingDistrictRepository = new LegacyPollingDistrictRepository(entityManager);
    }

    @Test
    public void testCreate_withLegacyPollingDistrict_persistsLegacyPollingDistrict() {
        LegacyPollingDistrict legacyPollingDistrict = createMock(LegacyPollingDistrict.class);
        
        legacyPollingDistrictRepository.create(userData, legacyPollingDistrict);
        
        verify(entityManager, times(1)).persist(legacyPollingDistrict);
    }

    @Test
    public void testUpdateLegacyPollingDistricts_withLegacyPollingDistricts_mergesLegacyPollingDistricts() {
        List<LegacyPollingDistrict> legacyPollingDistricts = new ArrayList<>();
        legacyPollingDistricts.add(createMock(LegacyPollingDistrict.class));
        legacyPollingDistricts.add(createMock(LegacyPollingDistrict.class));
        
        legacyPollingDistrictRepository.updateLegacyPollingDistricts(userData, legacyPollingDistricts);
        
        verify(entityManager, times(2)).merge(any(LegacyPollingDistrict.class));
    }
}
