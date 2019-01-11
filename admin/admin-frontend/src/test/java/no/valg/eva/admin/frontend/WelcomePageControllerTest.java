package no.valg.eva.admin.frontend;

import no.valg.eva.admin.frontend.faces.FacesContextBroker;
import no.valg.eva.admin.test.MockUtilsTestCase;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.InvocationTargetException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class WelcomePageControllerTest extends MockUtilsTestCase {

    private WelcomePageController welcomePageController;
    private HttpServletRequest httpServletRequest;

    @BeforeMethod
    public void setUp() throws InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchFieldException {
        welcomePageController = initializeMocks(WelcomePageController.class);
        httpServletRequest = createMock(HttpServletRequest.class);
        FacesContextBroker facesContextBroker = getInjectMock(FacesContextBroker.class);
        when(facesContextBroker.getContext().getExternalContext().getRequest()).thenReturn(httpServletRequest);
    }

    @Test
    public void getLoginUrl_tmpLoginwithoutScanning_returnsURLwithScanningFalse() {
        String loginUrl = welcomePageController.getLoginUrl();
        
        assertThat(loginUrl).isEqualTo("/tmpLogin?scanning=false");
    }

    @Test
    public void getLoginUrl_tmpLoginwithScanning_returnsURLwithScanningTrue() {
        when(httpServletRequest.getParameter("scanning")).thenReturn("true");
        
        String loginUrl = welcomePageController.getLoginUrl();

        assertThat(loginUrl).isEqualTo("/tmpLogin?scanning=true");
    }
    
}
