package no.valg.eva.admin.frontend.voting.ctrls;

import no.evote.model.views.VoterAudit;
import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.common.voter.service.VoterAuditService;
import no.valg.eva.admin.configuration.domain.model.ElectionGroup;
import no.valg.eva.admin.frontend.voting.ctrls.VoterConfirmation.VoterConfirmationContext;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

public class VoterElectoralRollHistoryTest extends BaseFrontendTest {

    private VoterElectoralRollHistory voterElectoralRollHistory;
    private ElectionGroup electionGroup;

    @BeforeMethod
    public void setUp() throws InvocationTargetException, NoSuchFieldException, InstantiationException, IllegalAccessException {
        voterElectoralRollHistory = initializeMocks(VoterElectoralRollHistory.class);
        electionGroup = createMock(ElectionGroup.class);
    }

    @Test
    public void testInitComponent_verifiesLoadingOfElectoralRollHistory() {
        VoterConfirmationContext context = VotingConfirmationTestData.voterConfirmationViewModel(getUserDataMock(), electionGroup);

        List<VoterAudit> electoralRollHistory = new ArrayList<>();
        electoralRollHistory.add(new VoterAudit());

        when(getInjectMock(VoterAuditService.class).getHistoryForVoter(getUserDataMock(), context.getVoterDto().getId()))
                .thenReturn(electoralRollHistory);
        voterElectoralRollHistory.initComponent(VoterElectoralRollHistory.VoterElectoralRollHistoryContext.builder()
                .userData(getUserDataMock())
                .voterDto(context.getVoterDto())
                .build());

        assertEquals(voterElectoralRollHistory.getElectoralRollHistory(), electoralRollHistory);
        verify(getInjectMock(VoterAuditService.class), times(1)).getHistoryForVoter(getUserDataMock(),
                context.getVoterDto().getId());
    }

    @Test
    public void testShow() {
        voterElectoralRollHistory.show();
        assertFacesUtilExecutedJavaScript("PF('electoralRollHistoryDialog').show()");
    }
}