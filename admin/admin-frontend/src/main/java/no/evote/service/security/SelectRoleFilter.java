package no.evote.service.security;

import java.io.IOException;
import java.util.List;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import no.evote.security.UserData;
import no.evote.service.rbac.OperatorService;
import no.valg.eva.admin.common.Person;
import no.valg.eva.admin.common.PersonId;
import no.valg.eva.admin.rbac.domain.model.Operator;

import org.apache.log4j.Logger;

public class SelectRoleFilter implements Filter {

	public static final String OPERATOR_SWITCHED_ROLES_SESSION_ATTR_KEY = "operatorSwitchedRoles";
	public static final String SELECT_ROLE_PAGE = "/secure/selectRole.xhtml";
	private static final Logger LOGGER = Logger.getLogger(SelectRoleFilter.class);

	@Inject
	private Instance<UserData> userDataInstance;
	@Inject
	private OperatorService operatorService;

	@Override
	/**
	 * If a role is not selected, the user should be redirected to selectRole.xhtml. One exception is for Scanning login, which has its own role selection
	 * process.
	 */
	public void doFilter(final ServletRequest req, final ServletResponse res, final FilterChain chain) throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) res;
		String servletPath = request.getServletPath();
		UserData userData = userDataInstance.get();

		boolean selectRolePageAccess = request.getRequestURI().endsWith(SELECT_ROLE_PAGE);
		if (userHasAlreadySelectedAnOperatorRole(userData)) {
			if (selectRolePageAccess) {
				LOGGER.debug("User " + userData.getUid() + " is switching roles...");
				HttpSession oldSession = existingSession(request);
				oldSession.setAttribute(OPERATOR_SWITCHED_ROLES_SESSION_ATTR_KEY, true);
				oldSession.invalidate();

				userData.invalidateRoleSelection();

				HttpSession newSession = newSession(request);
				newSession.setAttribute(OPERATOR_SWITCHED_ROLES_SESSION_ATTR_KEY, userData);

				response.sendRedirect(request.getServletContext().getContextPath() + SELECT_ROLE_PAGE);
				return;
			} else {
				chain.doFilter(req, res);
			}
		} else {
			setNameline(userData);
			if (selectRolePageAccess) {
				chain.doFilter(req, res);
			} else if (ScanningLoginUtil.isScanningLogin(request)) {
				// Scanning login has its own 'role selection process'.
				chain.doFilter(req, res);
			} else {
				LOGGER.debug("Legger " + servletPath + " på session og går til selectRole");
				request.getSession().setAttribute("goto", servletPath);
				response.sendRedirect("/secure/selectRole.xhtml");
			}
		}
	}

	private boolean userHasAlreadySelectedAnOperatorRole(UserData userData) {
		return userData != null && userData.getOperatorRole() != null;
	}

	private void setNameline(UserData userData) {
		if (userData.getNameLine() == null) {
			List<Operator> operators = operatorService.findOperatorsById(userData.getUid());
			Operator operator = operators.get(0);
			userData.setNameLine(new Person(new PersonId(operator.getId()), null, operator.getFirstName(), operator.getMiddleName(), operator
					.getLastName(), null).nameLine());
		}
	}

	@Override
	public void init(final FilterConfig arg0) throws ServletException {
		// To conform with interface
	}

	@Override
	public void destroy() {
		// To conform with interface
	}

	private HttpSession existingSession(HttpServletRequest request) {
		return request.getSession(false);
	}

	private HttpSession newSession(HttpServletRequest request) {
		return request.getSession(true);
	}

}
