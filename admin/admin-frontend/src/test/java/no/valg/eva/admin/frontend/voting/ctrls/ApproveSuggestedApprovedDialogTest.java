package no.valg.eva.admin.frontend.voting.ctrls;

import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.frontend.voting.ctrls.ApproveSuggestedApprovedDialog.Context;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

public class ApproveSuggestedApprovedDialogTest extends BaseFrontendTest {

    private ApproveSuggestedApprovedDialog dialog;

    @BeforeMethod
    public void setUp() throws InvocationTargetException, NoSuchFieldException, InstantiationException, IllegalAccessException {
        dialog = initializeMocks(ApproveSuggestedApprovedDialog.class);
        when(getFacesContextMock().getApplication().evaluateExpressionGet(eq(getFacesContextMock()), eq("#{userDataProducer.userData}"), any(Class.class)))
                .thenReturn(getUserDataMock());
    }

    @Test(dataProvider = "contextValidationTestData", expectedExceptions = IllegalStateException.class)
    public void testContextValidation(Context contextViewModel) {
        dialog.initComponent(contextViewModel);
    }

    @DataProvider
    private Object[][] contextValidationTestData() {
        return new Object[][]{
                {null},
                {Context.builder()
                        .handler(null)
                        .selectedVotings(null)
                        .build()},
                {Context.builder()
                        .handler(votings -> {
                        })
                        .selectedVotings(null)
                        .build()},
                {Context.builder()
                        .handler(votingRejection -> {
                        })
                        .selectedVotings(emptyList())
                        .build()},
                {Context.builder()
                        .handler(null)
                        .selectedVotings(singletonList(votingViewModel()))
                        .build()}
        };
    }

    @Test(dataProvider = "resolveMessageTestData")
    public void testResolveMessage(Context context, String expectedMessage) {
        dialog.initComponent(context);
        assertEquals(dialog.getMessage(), expectedMessage);
    }

    private VotingViewModel votingViewModel() {
        return VotingViewModel.builder().build();
    }

    @DataProvider
    public Object[][] resolveMessageTestData() {
        List<VotingViewModel> votingListWithMoreThanOneVoting = new ArrayList<>();
        votingListWithMoreThanOneVoting.add(votingViewModel());
        votingListWithMoreThanOneVoting.add(votingViewModel());
        return new Object[][]{
                {context(votings -> {
                }, singletonList(votingViewModel())), "@voting.confirmation.confirmApproveMessageSingle"},
                {context(votings -> {
                }, votingListWithMoreThanOneVoting), "[@voting.confirmation.confirmApproveMessage, 2]"},
        };
    }

    private Context context(ApproveSuggestedApprovedDialog.Handler handler, List<VotingViewModel> votings) {
        return Context.builder()
                .handler(handler)
                .selectedVotings(votings)
                .build();
    }

    @Test(dataProvider = "show")
    public void testShow_verifyingCorrectJavaScriptExecution(String expectedJavaScriptCommand) {
        dialog.show();
        verify(getRequestContextMock()).execute(expectedJavaScriptCommand);
    }

    @DataProvider
    public Object[][] show() {
        return new Object[][]{
                {"PF('approveSuggestedApprovedDialogWidget').show()"}
        };
    }

    @Test(dataProvider = "hide")
    public void testHide_verifyingCorrectJavaScriptExecution(String expectedJavaScriptCommand) {
        dialog.hide();
        verify(getRequestContextMock()).execute(expectedJavaScriptCommand);
    }

    @DataProvider
    public Object[][] hide() {
        return new Object[][]{
                {"PF('approveSuggestedApprovedDialogWidget').hide()"}
        };
    }

    @Test
    public void testOnApprove_callsHandlerWithSelectedVotings() {
        List<VotingViewModel> selected = asList(votingViewModel(), votingViewModel());

        ApproveSuggestedApprovedDialog.Handler handler = mock(ApproveSuggestedApprovedDialog.Handler.class);

        dialog.initComponent(Context.builder()
                .selectedVotings(selected)
                .handler(handler)
                .build());

        dialog.onApprove();

        verify(handler).onApproveSuggestedApprovedVotings(selected);
    }
}