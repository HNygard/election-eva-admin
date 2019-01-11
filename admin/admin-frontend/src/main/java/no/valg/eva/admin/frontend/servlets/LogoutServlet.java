package no.valg.eva.admin.frontend.servlets;

import lombok.extern.log4j.Log4j;
import no.valg.eva.admin.frontend.security.TmpLoginDetector;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet(value = "/logout")
@Log4j
public class LogoutServlet extends HttpServlet {
    private static final String ADMIN_LOGOUT_LOCATION = "/welcome.xhtml?type=logout";
    private static final String OIDC_LOGOUT_LOCATION = "/oidc/logout";
    private static final TmpLoginDetector TMP_LOGIN_DETECTOR = new TmpLoginDetector();

    @Override
    public void doGet(final HttpServletRequest request, final HttpServletResponse response) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        try {
            if (TMP_LOGIN_DETECTOR.isTmpLoginEnabled()) {
                response.sendRedirect(ADMIN_LOGOUT_LOCATION);
            } else {
                response.sendRedirect(OIDC_LOGOUT_LOCATION);
            }
        } catch (IOException e) {
            log.error("Feil ved videresending til logoutside", e);
        }
    }

}
