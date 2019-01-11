package no.valg.eva.admin.voting.application;

import no.evote.security.UserData;
import no.valg.eva.admin.common.voting.VotingCategory;
import no.valg.eva.admin.common.voting.VotingPhase;
import no.valg.eva.admin.configuration.domain.model.ElectionGroup;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.PollingPlace;
import no.valg.eva.admin.configuration.domain.model.Voter;
import no.valg.eva.admin.configuration.repository.MunicipalityRepository;
import no.valg.eva.admin.test.MockUtilsTestCase;
import no.valg.eva.admin.voting.domain.service.VotingRegistrationDomainService;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.lang.reflect.InvocationTargetException;

import static no.valg.eva.admin.common.voting.VotingCategory.FI;
import static no.valg.eva.admin.common.voting.VotingPhase.ADVANCE;
import static no.valg.eva.admin.configuration.application.MunicipalityMapper.toDto;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class VotingRegistrationApplicationServiceTest extends MockUtilsTestCase {

    private VotingRegistrationApplicationService applicationService;
    private VotingRegistrationDomainService votingRegistrationDomainService;
    private ElectionGroup electionGroup;
    private PollingPlace pollingPlace;
    private Municipality municipality;

    @BeforeMethod
    public void setUp() throws InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchFieldException {
        applicationService = initializeMocks(VotingRegistrationApplicationService.class);
        votingRegistrationDomainService = getInjectMock(VotingRegistrationDomainService.class);
        electionGroup = createMock(ElectionGroup.class);
        pollingPlace = createMock(PollingPlace.class);
        municipality = createMock(Municipality.class);
        
        MvArea mvArea = createMock(MvArea.class);
        when(mvArea.getMunicipality()).thenReturn(municipality);
    }

    @Test
    public void testRegisterAdvanceVotingInEnvelope__verifiesDomainServiceCall() {
        Voter voter = createMock(Voter.class);
        no.valg.eva.admin.common.configuration.model.Municipality municipalityDto = toDto(municipality);
        UserData userData = new UserData();

        when(getInjectMock(MunicipalityRepository.class).findByPk(anyLong())).thenReturn(municipality);
        when(votingRegistrationDomainService.registerAdvanceVotingInEnvelope(
                any(UserData.class),
                any(PollingPlace.class),
                any(ElectionGroup.class),
                any(Municipality.class),
                any(Voter.class),
                any(VotingCategory.class),
                anyBoolean(),
                any(VotingPhase.class)))
                .thenReturn(null);

        applicationService.registerAdvanceVotingInEnvelope(userData, pollingPlace, electionGroup, municipalityDto, voter, FI, false, ADVANCE);

        verify(votingRegistrationDomainService)
                .registerAdvanceVotingInEnvelope(
                        userData,
                        pollingPlace,
                        electionGroup,
                        municipality,
                        voter,
                        FI,
                        false,
                        ADVANCE);
    }

    @Test
    public void testRegisterElectionDayVotingInEnvelopeCentrally_verifiesDomainServiceCall() {
        Voter voter = createMock(Voter.class);
        UserData userData = new UserData();

        when(getInjectMock(MunicipalityRepository.class).findByPk(anyLong())).thenReturn(municipality);
        when(votingRegistrationDomainService.registerElectionDayVotingInEnvelopeCentrally(
                any(UserData.class),
                any(ElectionGroup.class),
                any(Municipality.class),
                any(Voter.class),
                any(VotingCategory.class),
                any(VotingPhase.class)))
                .thenReturn(null);

        applicationService.registerElectionDayVotingInEnvelopeCentrally(userData, electionGroup, municipality, voter, FI, ADVANCE);

        verify(votingRegistrationDomainService)
                .registerElectionDayVotingInEnvelopeCentrally(
                        userData,
                        electionGroup,
                        municipality,
                        voter,
                        FI,
                        ADVANCE);
    }

}
