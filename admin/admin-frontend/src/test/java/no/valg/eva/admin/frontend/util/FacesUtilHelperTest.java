package no.valg.eva.admin.frontend.util;

import no.valg.eva.admin.BaseFrontendTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.verify;

public class FacesUtilHelperTest extends BaseFrontendTest {

    private FacesUtilHelper facesUtilHelper;

    @BeforeMethod
    public void setUp() throws Exception {
        facesUtilHelper = initializeMocks(FacesUtilHelper.class);

        FacesUtil.setContext(getFacesContextMock());
        FacesUtil.setRequestContext(getRequestContextMock());
    }

    @Test
    public void testUpdateDom_givenDomId_verifyRequestContextInvocation() {
        facesUtilHelper.updateDom("id");

        verify(getRequestContextMock()).update("id");
    }
}