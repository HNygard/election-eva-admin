package no.valg.eva.admin.frontend;

import com.google.common.base.Function;
import no.evote.exception.CandidateValidationException;
import no.evote.exception.ErrorCode;
import no.evote.exception.EvoteException;
import no.evote.exception.EvoteSecurityException;
import no.evote.exception.ReadOnlyPrivilegeException;
import no.evote.presentation.exceptions.ErrorPageRenderer;
import no.valg.eva.admin.common.interceptor.Jamon;
import no.valg.eva.admin.frontend.common.ErrorCodeHandler;
import no.valg.eva.admin.frontend.common.ctrls.RedirectInfo;
import no.valg.eva.admin.frontend.faces.FacesContextBroker;
import no.valg.eva.admin.frontend.faces.RequestContextBroker;
import no.valg.eva.admin.frontend.i18n.MessageProvider;
import no.valg.eva.admin.frontend.security.PageAccess;
import no.valg.eva.admin.frontend.util.FacesUtil;
import no.valg.eva.admin.frontend.util.MessageUtil;
import no.valg.eva.admin.util.ExceptionUtil;
import no.valg.eva.admin.util.Service;
import org.apache.log4j.Logger;
import org.primefaces.context.RequestContext;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;

import static java.lang.String.format;
import static no.valg.eva.admin.frontend.common.ctrls.RedirectInfo.REDIRECT_INFO_SESSION_KEY;

/**
 * Common controller type. Useful for adding common annotations or functionality to all controller instances.
 */
@Jamon
public abstract class BaseController implements Serializable {

    private static final int NO_CHARS_IN_DOTXHTML = 6;

    private static final int CSS_TABLE_ROW_HEIGHT = 55;

    private static final long serialVersionUID = -542282392873399786L;

    @Inject
    private RequestContextBroker requestContextBroker;
    @Inject
    private FacesContextBroker facesContextBroker;
    @Inject
    private PageAccess pageAccess;

    protected static String addRedirect(String url) {
        if (!url.contains("faces-redirect") && !isSamePage(url)) {
            if (url.contains("?")) {
                url += '&';
            } else {
                url += '?';
            }
            return url + "faces-redirect=true";
        }
        return url;
    }

    private static boolean isSamePage(String url) {
        FacesContext ctx = FacesUtil.getContext();
        if (ctx == null || ctx.getExternalContext() == null || ctx.getExternalContext().getRequest() == null) {
            return false;
        }
        String requestURI = ((HttpServletRequest) ctx.getExternalContext().getRequest()).getRequestURI();
        if (requestURI == null) {
            return false;
        }
        return getResource(url).equals(getResource(requestURI));
    }

    public static String getResource(String url) {
        int index = url.indexOf(".xhtml");
        if (index == -1) {
            return url;
        }
        url = url.substring(0, index + NO_CHARS_IN_DOTXHTML);
        index = url.lastIndexOf('/');
        if (index == -1) {
            return url;
        }
        return url.substring(index + 1);
    }

    protected RedirectInfo getAndRemoveRedirectInfo() {
        RedirectInfo redirectInfo = (RedirectInfo) getRequest().getSession().getAttribute(REDIRECT_INFO_SESSION_KEY);
        getRequest().getSession().removeAttribute(REDIRECT_INFO_SESSION_KEY);
        return redirectInfo;
    }

    protected void leggRedirectInfoPaSession(RedirectInfo redirectInfo) {
        getRequest().getSession().setAttribute(REDIRECT_INFO_SESSION_KEY, redirectInfo);
    }

    public HttpServletRequest getRequest() {
        FacesContext ctx = FacesUtil.getContext();
        return (HttpServletRequest) ctx.getExternalContext().getRequest();
    }

    public RequestContext getRequestContext() {
        return requestContextBroker.getContext();
    }

    protected FacesContext getFacesContext() {
        return facesContextBroker.getContext();
    }

    public String getRequestParameter(String key) {
        String[] values = getFacesContext().getExternalContext().getRequestParameterValuesMap().get(key);
        return values == null || values.length == 0 ? null : values[0];
    }

    protected <T> T getRequestParameter(String key, Function<String, T> mapper) {
        String stringValue = getRequestParameter(key);
        if (stringValue == null) {
            return null;
        }
        return mapper.apply(stringValue);
    }

    protected String getQueryString() {
        return getRequest().getQueryString();
    }

    protected String getRequestURI() {
        return getRequest().getRequestURI();
    }

    public int getScrollHeight(Collection collection, int maxScrollHeight) {
        int result = collection.size() * CSS_TABLE_ROW_HEIGHT;
        return result > maxScrollHeight ? maxScrollHeight : result;
    }

    protected boolean execute(Service service) {
        try {
            service.execute();
        } catch (RuntimeException e) {
            process(e, null, null);
            return false;
        }
        return true;
    }

    protected boolean execute(Service service, String clientId) {
        try {
            service.execute();
        } catch (RuntimeException e) {
            process(e, null, clientId);
            return false;
        }
        return true;
    }

    protected boolean execute(Service service, ErrorCodeHandler handler) {
        try {
            service.execute();
        } catch (RuntimeException e) {
            process(e, handler, null);
            return false;
        }
        return true;
    }

    protected void process(RuntimeException e) {
        process(e, null, null);
    }

    void process(RuntimeException e, ErrorCodeHandler handler, String clientId) {
        EvoteException evoteException = ExceptionUtil.unwrapThrowable(e, EvoteException.class);
        if (evoteException == null) {
            handleUnexpected(e, clientId);
            return;
        }

        String message = null;
        boolean logged = false;
        if (evoteException.getErrorCode() == null && evoteException.getUserMessage() != null) {
            String[] args = Arrays.stream(evoteException.getUserMessage().getArgs()).map(Object::toString).toArray(String[]::new);
            message = translate(evoteException.getUserMessage().getMessage(), args);
        } else if (evoteException.getErrorCode() != null) {
            // For now, we only process EvoteExceptions with ErrorCode. The rest is thrown.
            ErrorCode errorCode = evoteException.getErrorCode();
            if (handler != null) {
                message = translate(handler.onError(errorCode, evoteException.getParams()));
            }
            if (message == null) {
                // Some special handling of database errors
                if (errorCode == ErrorCode.ERROR_CODE_0503_CONSTRAINT_VIOLATION) {
                    String constraintName = evoteException.getParams()[0];
                    message = translate(oldConstraintViolationCheck(constraintName));
                } else if (errorCode == ErrorCode.ERROR_CODE_0505_UNIQUE_CONSTRAINT_VIOLATION) {
                    String constraint = evoteException.getParams()[0];
                    if ("nk_polling_district".equals(constraint)) {
                        message = translate("@config.local.error.polling_district.nonUniqueId");
                    } else if ("nk_polling_place".equals(constraint)) {
                        message = translate("@config.local.error.polling_place.nonUniqueId");
                    } else {
                        String uuid = ErrorPageRenderer.md5(constraint);
                        message = translate(evoteException.getMessage(), uuid);
                        Logger.getLogger(this.getClass()).error(uuid + ": " + constraint, e);
                        logged = true;
                    }
                } else if (errorCode == ErrorCode.ERROR_CODE_0450_CONTEST_AREA_SKIPPING_EXISTING_IN_SAME_AREA) {
                    message = translate(MessageUtil.CHOOSE_UNIQUE_ID, evoteException.getParams());
                } else if (errorCode == ErrorCode.ERROR_CODE_0451_CONTEST_AREA_SKIPPING_EXISTING_IN_SAME_ELECTION) {
                    message = translate(MessageUtil.CHOOSE_UNIQUE_ID, evoteException.getParams());
                } else if (errorCode == ErrorCode.ERROR_CODE_0590_REPORT_SERVER_ERROR) {
                    message = translate(errorCode.formatMessage());
                }
            }
            if (message == null) {
                if (evoteException.getParams() == null) {
                    message = translate(e.getMessage());
                } else {
                    message = translate(e.getMessage(), evoteException.getParams());
                }
            }
        } else if (evoteException instanceof CandidateValidationException) {
            MessageUtil.buildDetailMessageFromValidationResults(((CandidateValidationException) evoteException).getValidationMessages());
            return;
        } else if (evoteException instanceof ReadOnlyPrivilegeException) {
            message = translate("@common.message.evote_application_exception.READ_ONLY_PRIVILEGE");
        } else if (evoteException instanceof EvoteSecurityException) {
            message = translate("@common.message.evote_application_exception.SECURITY");
        } else if (evoteException.getMessage().startsWith("@")) {
            message = translate(evoteException.getMessage(), evoteException.getParams());
        }

        if (message == null) {
            handleUnexpected(evoteException, clientId);
            return;
        }
        if (!logged) {
            if (erFeilkodeSomIkkeTrengerUmiddelbarOppfolging(evoteException.getErrorCode())) {
                Logger.getLogger(this.getClass()).warn(e.getMessage(), e);
            } else {
                Logger.getLogger(this.getClass()).error(e.getMessage(), e);
            }
        }
        MessageUtil.buildMessageForClientId(clientId, message, FacesMessage.SEVERITY_ERROR);
    }

    private void handleUnexpected(Exception e, String clientId) {
        String message = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
        String uuid = ErrorPageRenderer.md5(message);
        Logger.getLogger(this.getClass()).error(uuid + ": " + message, e);
        MessageUtil.buildMessageForClientId(clientId, translate("@common.error.unexpected", uuid), FacesMessage.SEVERITY_ERROR);
    }

    private boolean erFeilkodeSomIkkeTrengerUmiddelbarOppfolging(ErrorCode errorCode) {
        return errorCode == ErrorCode.ERROR_CODE_0501_OPTIMISTIC_LOCK;
    }

    private String translate(String message, String... params) {
        if (message != null && message.startsWith("@")) {
            if (params == null) {
                params = new String[0];
            }
            message = getMessageProvider().getWithTranslatedParams(message, params);
        }
        return message;
    }

    private MessageProvider getMessageProvider() {
        return getFacesContext().getApplication().evaluateExpressionGet(getFacesContext(), "#{messageProvider}",
                MessageProvider.class);
    }

    protected boolean isOptimisticLockingException(ErrorCode errorCode) {
        return errorCode == ErrorCode.ERROR_CODE_0501_OPTIMISTIC_LOCK
                || errorCode == ErrorCode.ERROR_CODE_0502_ENTITY_NOT_FOUND
                || errorCode == ErrorCode.ERROR_CODE_0504_STALE_OBJECT;
    }

    protected String oldConstraintViolationCheck(String constraintName) {
        if (constraintName.startsWith("fk_voting_x_")) {
            return "@common.message.voting_constraint_error";
        } else if (constraintName.startsWith("fk_vote_count_x_")) {
            return "@common.message.vote_count_constraint_error";
        } else if (constraintName.startsWith("fk_ballot_count_x_")) {
            return "@common.message.vote_count_constraint_error";
        } else if (constraintName.startsWith("fk_voter_x_")) {
            return "@common.message.voter_constraint_error";
        } else if (constraintName.startsWith("fk_affiliation_vote_count_x_affiliation")) {
            return "@common.message.affiliation_count_constraint_error";
        }
        return null;
    }

    protected void redirectTo(String url) {
        try {
            getFacesContext().getExternalContext().redirect(url);
        } catch (IOException e) {
            throw new IllegalStateException(format("failed to redirect to: %s", url), e);
        }
    }

    public PageAccess getPageAccess() {
        return pageAccess;
    }
}
