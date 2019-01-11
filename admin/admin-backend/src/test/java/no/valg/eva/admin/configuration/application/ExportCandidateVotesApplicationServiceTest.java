package no.valg.eva.admin.configuration.application;

import no.evote.security.UserData;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.configuration.domain.service.ExportCandidateVotesDomainService;
import no.valg.eva.admin.test.MockUtilsTestCase;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.lang.reflect.InvocationTargetException;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class ExportCandidateVotesApplicationServiceTest extends MockUtilsTestCase {
    
    private UserData userData;
    private ExportCandidateVotesApplicationService exportCandidateVotesApplicationService;
    
    private static final AreaPath AREA_PATH = AreaPath.from("111111.11.11.1111");
    private static final ElectionPath ELECTION_PATH = ElectionPath.from("111111.11.11.111111");
    
    @BeforeMethod
    public void setUp() throws InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchFieldException {
        userData = mock(UserData.class, RETURNS_DEEP_STUBS);
        exportCandidateVotesApplicationService = initializeMocks(ExportCandidateVotesApplicationService.class);
    }
    
    @Test
    public void exportCandidateVotes_givenUserDataAndPaths_callsDomainService() {
        ExportCandidateVotesDomainService domainService = getInjectMock(ExportCandidateVotesDomainService.class);
        exportCandidateVotesApplicationService.exportCandidateVotes(userData, AREA_PATH, ELECTION_PATH);
        verify(domainService).exportCandidateVotes(eq(AREA_PATH), eq(ELECTION_PATH));
    }
}
