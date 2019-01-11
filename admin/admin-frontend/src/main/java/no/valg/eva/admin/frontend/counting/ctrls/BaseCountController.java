package no.valg.eva.admin.frontend.counting.ctrls;

import javax.faces.application.FacesMessage;
import javax.inject.Inject;

import no.evote.exception.ValidateException;
import no.valg.eva.admin.frontend.util.MessageUtil;
import no.evote.security.UserData;
import no.valg.eva.admin.common.counting.model.CountContext;
import no.valg.eva.admin.common.counting.service.CountingService;
import no.valg.eva.admin.frontend.ConversationScopedController;
import no.valg.eva.admin.frontend.i18n.MessageProvider;
import no.valg.eva.admin.frontend.user.ctrls.UserAccess;

import org.apache.log4j.Logger;

public abstract class BaseCountController extends ConversationScopedController {

	protected static final Logger LOGGER = Logger.getLogger(BaseCountController.class);
	@Inject
	protected UserData userData;
	@Inject
	protected UserAccess userAccess;
	@Inject
	protected transient CountingService countingService;
	protected boolean error;

	protected abstract MessageProvider getMessageProvider();

	protected void logAndBuildDetailErrorMessage(Exception e) {
		error = true;
		if (e instanceof RuntimeException && !(e instanceof ValidateException)) {
			try {
				process((RuntimeException) e);
				return;
			} catch (Exception ee) {
				LOGGER.trace(ee.getMessage());
			}
		}
		if (e instanceof ValidateException) {
			LOGGER.debug(e.getMessage(), e);
		}
		MessageUtil.buildDetailMessage(FacesMessage.SEVERITY_ERROR, e.getMessage());
	}

	/**
	 * A user is on county level if the user's associated area is on level 2 (county) in the area hierarchy, or if the user is an election event admin and
	 * reportingUnitTypeId sent in from counting overview is fylkesvalgsstyret.
	 * @return
	 */
	public abstract boolean isUserOnCountyLevel();

	public boolean isError() {
		return error;
	}

	protected CountingService getCountingService() {
		return countingService;
	}

	public abstract CountContext getCountContext();
}
