package no.valg.eva.admin.frontend.voting.ctrls;

import no.evote.security.UserData;
import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.common.voting.VotingCategory;
import no.valg.eva.admin.common.voting.model.VotingRejectionDto;
import no.valg.eva.admin.common.voting.service.VotingInEnvelopeService;
import no.valg.eva.admin.frontend.common.UpdatableComponent;
import no.valg.eva.admin.frontend.common.UpdatableComponentHandler;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

public class VotingSuggestedRejectedDialogTest extends BaseFrontendTest {

    private VotingSuggestedRejectedDialog dialog;

    @BeforeMethod
    public void setUp() throws InvocationTargetException, NoSuchFieldException, InstantiationException, IllegalAccessException {
        dialog = initializeMocks(VotingSuggestedRejectedDialog.class);

        when(getFacesContextMock().getApplication().evaluateExpressionGet(eq(getFacesContextMock()), eq("#{userDataProducer.userData}"), any(Class.class)))
                .thenReturn(getUserDataMock());
    }

    @Test(dataProvider = "setSelectedRejectionId")
    public void testSetSelectedVotingRejectionId(String expectedRejectionId) {
        dialog.setSelectedVotingRejectionId(expectedRejectionId);
        assertEquals(dialog.getSelectedVotingRejectionId(), expectedRejectionId);
    }

    @DataProvider
    public Object[][] setSelectedRejectionId() {
        return new Object[][]{
                {"id"}
        };
    }

    @Test(dataProvider = "setVotingRejectionList")
    public void testSetVotingRejectionDtoList(List<VotingRejectionDto> votingRejectionList) {
        dialog.setVotingRejectionDtoList(votingRejectionList);
        assertEquals(dialog.getVotingRejectionDtoList(), votingRejectionList);
    }

    @DataProvider
    public Object[][] setVotingRejectionList() {

        List<VotingRejectionDto> votingRejectionDtoList = new ArrayList<>();
        String votingRejectionId = "votingRejectionId";
        VotingRejectionDto votingRejectionDto = VotingRejectionDto.builder()
                .id(votingRejectionId)
                .build();
        votingRejectionDtoList.add(votingRejectionDto);

        return new Object[][]{
                {votingRejectionDtoList}
        };
    }

    @Test(dataProvider = "show")
    public void testShow_verifyingCorrectJavaScriptExecution(String expectedJavaScriptCommand) {
        dialog.show();
        verify(getRequestContextMock()).execute(expectedJavaScriptCommand);
    }

    @DataProvider
    public Object[][] show() {
        return new Object[][]{
                {"PF('votingSuggestedRejectedDialogWidget').show()"}
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
                {"PF('votingSuggestedRejectedDialogWidget').hide()"}
        };
    }

    @Test(dataProvider = "initComponent")
    public void testInitComponent(VotingSuggestedRejectedDialog.ContextViewModel context) {
        when(getInjectMock(VotingInEnvelopeService.class).votingRejections(context.getUserData(), context.getVotingCategory())).thenReturn(Collections.emptyList());

        dialog.initComponent(context);

        verify(getInjectMock(VotingInEnvelopeService.class), times(1))
                .votingRejections(getUserDataMock(),
                        context.getVotingCategory());
        assertNull(dialog.getSelectedVotingRejectionId());
    }

    @DataProvider
    public Object[][] initComponent() {
        ComponentHandlerMock componentHandler = componentHandlerMock();
        return new Object[][]{
                {contextViewModel(componentHandler)}
        };
    }

    @Test
    public void testRejectButtonDisabled() {
        dialog.setSelectedVotingRejectionId(null);
        assertTrue(dialog.isRejectButtonDisabled());

        dialog.setSelectedVotingRejectionId("rejectionReason1");
        assertFalse(dialog.isRejectButtonDisabled());
    }

    @Test(dataProvider = "onMoveToSuggestedRejectedTestData")
    public void testOnMoveToSuggestedRejected(List<VotingRejectionDto> votingRejectionList, VotingRejectionDto expectedVotingRejection)
            throws NoSuchFieldException, IllegalAccessException {
        List<VotingRejectionDto> votingRejectionDtoList = (List<VotingRejectionDto>) mockField("votingRejectionDtoList", List.class);
        when(votingRejectionDtoList.stream())
                .thenReturn(votingRejectionList.stream());

        mockFieldValue("selectedVotingRejectionId", expectedVotingRejection.getId());

        VotingSuggestedRejectedDialog.Handler callbackHandler = mockField("callbackHandler", VotingSuggestedRejectedDialog.Handler.class);
        doNothing().when(callbackHandler).onMoveVotingsToSuggestedRejected(any(VotingRejectionDto.class));

        dialog.onSelectedRejectionReason();

        verify(callbackHandler, times(1)).onMoveVotingsToSuggestedRejected(expectedVotingRejection);
    }

    @DataProvider
    public Object[][] onMoveToSuggestedRejectedTestData() {

        List<VotingRejectionDto> votingRejectionDtoList = new ArrayList<>();
        String votingRejectionId = "votingRejectionId";
        VotingRejectionDto votingRejectionDto = VotingRejectionDto.builder()
                .id(votingRejectionId)
                .build();
        votingRejectionDtoList.add(votingRejectionDto);

        return new Object[][]{
                {votingRejectionDtoList, votingRejectionDto}
        };
    }

    private ComponentHandlerMock componentHandlerMock() {
        return new ComponentHandlerMock();
    }

    private VotingSuggestedRejectedDialog.ContextViewModel contextViewModel(ComponentHandlerMock componentHandler) {
        return VotingSuggestedRejectedDialog.ContextViewModel.builder()
                .votingCategory(VotingCategory.FI)
                .callbackHandler(componentHandler)
                .userData(userData())
                .build();
    }

    private UserData userData() {
        UserData userData = new UserData();
        userData.setSecurityLevel(1);

        return userData;
    }

    public class ComponentHandlerMock implements UpdatableComponentHandler, VotingSuggestedRejectedDialog.Handler {
        @Override
        public void forceUpdate(UpdatableComponent component) {
        }

        @Override
        public void onMoveVotingsToSuggestedRejected(VotingRejectionDto votingRejection) {
        }
    }
}