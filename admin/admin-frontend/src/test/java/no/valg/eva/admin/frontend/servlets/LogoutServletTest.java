package no.valg.eva.admin.frontend.servlets;

import no.valg.eva.admin.test.MockUtilsTestCase;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class LogoutServletTest extends MockUtilsTestCase {

    private LogoutServlet logoutServlet;
    private HttpServletRequest httpServletRequest;
    private HttpServletResponse httpServletResponse;

    @BeforeMethod
    public void setUp() throws InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchFieldException {
        logoutServlet = initializeMocks(LogoutServlet.class);
        httpServletRequest = createMock(HttpServletRequest.class);
        httpServletResponse = createMock(HttpServletResponse.class);
    }

    @Test
    public void doGet_withSession_invalidatesSession() {
        HttpSession session = createMock(HttpSession.class);
        when(httpServletRequest.getSession(false)).thenReturn(session);
        
        logoutServlet.doGet(httpServletRequest, httpServletResponse);
        
        verify(session, times(1)).invalidate();
    }

    @Test
    public void testdoGet_withTmpLogin_redirectToLogoutPage() throws IOException {
        logoutServlet.doGet(httpServletRequest, httpServletResponse);

        verify(httpServletResponse, times(1)).sendRedirect(any());
    }

}
