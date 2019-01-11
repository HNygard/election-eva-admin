package no.valg.eva.admin.configuration.repository;

import no.evote.security.UserData;
import no.valg.eva.admin.backend.configuration.local.domain.model.MunicipalityOpeningHour;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.rbac.domain.model.Operator;
import no.valg.eva.admin.rbac.domain.model.OperatorRole;
import no.valg.eva.admin.rbac.domain.model.Role;
import no.valg.eva.admin.test.AbstractJpaTestBase;
import no.valg.eva.admin.test.GenericTestRepository;
import org.joda.time.LocalTime;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.persistence.PersistenceUnitUtil;
import java.util.List;

import static no.valg.eva.admin.test.TestGroups.REPOSITORY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.Assert.assertEquals;

@Test(groups = REPOSITORY)
public class MunicipalityRepositoryTest extends AbstractJpaTestBase {

    private MunicipalityRepository municipalityRepository;

    private GenericTestRepository genericTestRepository;

    private static final String PARAMETER_ID = "id";
    private static final String ELECTION_EVENT_ID = "200701";
    private static final String MUNICIPALITY_ID = "0101";

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        new MunicipalityRepository();
        municipalityRepository = new MunicipalityRepository(getEntityManager());
        genericTestRepository = new GenericTestRepository(getEntityManager());
    }

    @Test
    public void testFindByPk() {
        List<Municipality> municipalities = genericTestRepository.findEntitiesByProperty(Municipality.class, PARAMETER_ID, AreaPath.OSLO_MUNICIPALITY_ID);
        assertThat(municipalities).isNotEmpty();

        Municipality municipality = municipalityRepository.municipalityByPk(municipalities.get(0).getPk());
        assertThat(municipality).isNotNull();
    }

    @Test
    public void municipalityByElectionEventAndId() {
        ElectionEvent electionEvent = genericTestRepository.findEntityByProperty(ElectionEvent.class, PARAMETER_ID, ELECTION_EVENT_ID);

        Municipality municipality = municipalityRepository.municipalityByElectionEventAndId(electionEvent.getPk(), MUNICIPALITY_ID);
        assertThat(municipality).isNotNull();
    }

    @Test
    public void findByPkWithScanningConfig_always_eagerLoadsScanningConfig() {
        PersistenceUnitUtil puUtil = getEntityManager().getEntityManagerFactory().getPersistenceUnitUtil();
        ElectionEvent electionEvent = genericTestRepository.findEntityByProperty(ElectionEvent.class, PARAMETER_ID, ELECTION_EVENT_ID);
        Municipality municipalityWithLazyScanningConfig = municipalityRepository.municipalityByElectionEventAndId(electionEvent.getPk(), MUNICIPALITY_ID);

        Municipality municipalityWithEagerScanningConfig = municipalityRepository.findByPkWithScanningConfig(municipalityWithLazyScanningConfig.getPk());

        assertThat(puUtil.isLoaded(municipalityWithEagerScanningConfig, "scanningConfig")).isTrue();
    }

    @Test
    public void findByElectionEventWithScanningConfig_always_eagerLoadsScanningConfig() {
        PersistenceUnitUtil puUtil = getEntityManager().getEntityManagerFactory().getPersistenceUnitUtil();
        ElectionEvent electionEvent = genericTestRepository.findEntityByProperty(ElectionEvent.class, PARAMETER_ID, ELECTION_EVENT_ID);
        Municipality singleMunicipality = municipalityRepository.municipalityByElectionEventAndId(electionEvent.getPk(), MUNICIPALITY_ID);
        singleMunicipality.getOrCreateScanningConfig().setScanning(true);

        List<Municipality> municipalitiesWithEagerScanningConfig = municipalityRepository.findByElectionEventWithScanningConfig(electionEvent.getPk());

        assertThat(puUtil.isLoaded(municipalitiesWithEagerScanningConfig.get(0), "scanningConfig")).isTrue();
    }

    @Test
    public void addMunicipalityOpeningHours_VerifyIsAdded() {
        GenericTestRepository genericTestRepository = new GenericTestRepository(getEntityManager());
        ElectionEvent electionEvent = genericTestRepository.findEntityByProperty(ElectionEvent.class, PARAMETER_ID, ELECTION_EVENT_ID);

        UserData userData = userData(electionEvent);

        Municipality municipality = municipalityRepository.municipalityByElectionEventAndId(electionEvent.getPk(), MUNICIPALITY_ID);

        LocalTime startTime = new LocalTime().withHourOfDay(9).withMinuteOfHour(0).withSecondOfMinute(0);
        LocalTime endTime = new LocalTime().withHourOfDay(16).withMinuteOfHour(0).withSecondOfMinute(0);

        MunicipalityOpeningHour municipalityOpeningHour = MunicipalityOpeningHour.builder()
                .startTime(startTime)
                .endTime(endTime)
                .municipality(municipality)
                .electionDay(electionEvent.getElectionDays().iterator().next())
                .build();

        municipality.addOpeningHours(municipalityOpeningHour);
        municipality = municipalityRepository.update(userData, municipality);

        Municipality updatedMunicipality = municipalityRepository.getReference(municipality);
        assertEquals(updatedMunicipality.getOpeningHours().iterator().next(), municipalityOpeningHour);
    }

    private UserData userData(ElectionEvent electionEvent) {
        Role role = new Role();
        role.setUserSupport(false);

        Operator operator = new Operator();
        operator.setElectionEvent(electionEvent);
        
        OperatorRole operatorRole = new OperatorRole();
        operatorRole.setRole(role);
        operatorRole.setOperator(operator);
        

        UserData userData = new UserData();
        userData.setOperatorRole(operatorRole);

        return userData;
    }
}
