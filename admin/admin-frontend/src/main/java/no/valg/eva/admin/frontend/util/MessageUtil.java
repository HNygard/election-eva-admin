package no.valg.eva.admin.frontend.util;

import static java.util.stream.Collectors.joining;

import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.ejb.EJBException;
import javax.faces.application.FacesMessage;
import javax.faces.application.FacesMessage.Severity;
import javax.faces.context.FacesContext;
import javax.persistence.PersistenceException;

import no.evote.exception.EvoteException;
import no.valg.eva.admin.util.DateUtil;
import no.valg.eva.admin.common.UserMessage;
import no.valg.eva.admin.common.configuration.model.Displayable;
import no.valg.eva.admin.frontend.i18n.MessageProvider;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.exception.ConstraintViolationException;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;

public final class MessageUtil {

	public static final String CREATE_SUCCESSFUL_KEY = "@common.message.create.successful";
	public static final String CREATE_SUBLEVEL_SUCCESSFUL_KEY = "@common.message.sub_create.successful";
	public static final String DUPLICATE_ID = "@common.message.evote_application_exception.DUPLICATE_ID";
	public static final String CHOOSE_UNIQUE_ID = "@common.message.create.CHOOSE_UNIQUE_ID";
	public static final String UPDATE_SUCCESSFUL_KEY = "@common.message.update.successful";
	public static final String DELETE_SUCCESSFUL_KEY = "@common.message.delete.successful";
	public static final String DELETE_FROM_LEVEL_SUCCESSFUL_KEY = "@common.message.sub_delete.successful";
	public static final String DELETE_SUCCESSFUL_WITH_COUNT_KEY = "@common.message.delete.successful_with_count";
	public static final String DELETE_NOT_SUCCESSFUL_WITH_COUNT_KEY = "@common.message.delete.not_successful_with_count";
	public static final String DELETE_NOT_PERFORMED_AS_NONE_SELECTED_KEY = "@common.message.delete.not_performed_as_none_selected";
	public static final String REMOVE_NOT_ALLOWED_KEY = "@common.message.remove.not_allowed";
	public static final String FIELD_IS_REQUIRED = "@common.message.required";
	public static final String EDIT_ID_NOT_ALLOWED = "@common.message.edit.id.not.allowed";

	public static final String AREA_ID_MUST_CONFORM = "@area.list_areas.message.id_must_conform";

	public static final String EXCEPTION_GENERAL = "@common.message.exception.general";
	public static final String EXCEPTION_END_DATE_BEFORE_START_DATE_AT_ELECTION_DAY = "@area.polling_place.opening_hours.validate.time_interval.starttime_after_endtime";

	public static final String VOTING_VOTER_MUST_SPECIAL_COVER = "@voting.search.voterMustCastSpecialCover";
	public static final String VOTING_NUMBER_ENVELOPE = "@voting.markOff.votingNumberEnvelope";
	public static final String VOTER_NOT_APPROVED = "@voting.search.voterNotApproved";
	public static final String INVALID_TIME_INTERVAL = "@area.polling_place.opening_hours.validate.time_interval.illegal";
	public static final String EXCEEDING_TIME_INTERVAL = "@area.polling_place.opening_hours.validate.time_interval.exceed";
	public static final String OVERLAP_TIME_INTERVAL = "@area.polling_place.opening_hours.validate.time_interval.overlap";
	public static final String COMMON_TIME_PATTERN = "@common.date.time_pattern";
	public static final String COMMON_DATE_PATTERN = "@common.date.date_pattern";
	public static final String INCOMPLETE_TIME_INTERVAL = "@area.polling_place.opening_hours.validate.time_interval.incomplete";

	private MessageUtil() {
	}

	public static void buildDetailMessage(FacesContext context, String message, Severity severity) {
		String summary = buildTranslatedMessage(context, message, null);

		FacesMessage facesMessage = new FacesMessage();
		facesMessage.setSeverity(severity);
		facesMessage.setSummary(summary);
		addMessage(context, null, facesMessage);
		context.getExternalContext().getFlash().setKeepMessages(true); // Keep messages. Should be controlled by a parameter?
	}

	public static void buildDetailMessageFromValidationResults(List<UserMessage> validationResults) {

		String summary = validationResults.stream().map(vr -> buildTranslatedMessage(vr.getMessage(), vr.getArgs())).collect(joining(", "));

		FacesMessage facesMessage = new FacesMessage();
		FacesContext context = getContext();
		facesMessage.setSeverity(FacesMessage.SEVERITY_ERROR);
		facesMessage.setSummary(summary);
		addMessage(context, null, facesMessage);
		context.getExternalContext().getFlash().setKeepMessages(true); // Keep messages. Should be controlled by a parameter?
	}

	public static void buildDetailMessage(String message, Severity severity) {

		String summary = buildTranslatedMessage(message, null);

		FacesMessage facesMessage = new FacesMessage();
		FacesContext context = getContext();
		facesMessage.setSeverity(severity);
		facesMessage.setSummary(summary);
		addMessage(context, null, facesMessage);
		context.getExternalContext().getFlash().setKeepMessages(true); // Keep messages. Should be controlled by a parameter?
	}

	public static void buildDetailMessage(String message, String[] summaryParams, Severity severity) {
		if (summaryParams != null) {
			for (int i = 0; i < summaryParams.length; i++) {
				summaryParams[i] = buildTranslatedMessage(getContext(), summaryParams[i], null);
			}
		}
		String summary = buildTranslatedMessage(message, summaryParams);

		FacesMessage facesMessage = new FacesMessage();
		FacesContext context = getContext();
		facesMessage.setSeverity(severity);
		facesMessage.setSummary(summary);
		addMessage(context, null, facesMessage);
		context.getExternalContext().getFlash().setKeepMessages(true); // Keep messages. Should be controlled by a parameter?
	}

	public static void buildDetailMessage(Severity serverity, String... messages) {
		buildDetailMessage(buildString(messages), serverity);
	}

	public static void buildMessageForClientId(String clientId, String message, Severity severity) {
		FacesMessage facesMessage = new FacesMessage();
		FacesContext context = getContext();
		facesMessage.setSeverity(severity);
		facesMessage.setSummary(message);
		addMessage(context, clientId, facesMessage);
		context.getExternalContext().getFlash().setKeepMessages(true); // Keep messages. Should be controlled by a parameter?
	}

	public static void buildMessageForClientId(String clientId, Severity serverity, String... messages) {
		buildMessageForClientId(clientId, buildString(messages), serverity);
	}

	private static String buildString(String... messages) {
		StringBuilder builder = new StringBuilder();
		for (String msg : messages) {
			builder.append(msg);
		}
		return builder.toString();
	}

	/**
	 * Deprecated, use buildFacesMessage(javax.faces.context.FacesContext, java.lang.String, java.lang.String, java.lang.String[],
	 * javax.faces.application.FacesMessage.Severity)() Inject FacesBroker, use facesBroker.getContext() for finding FacesContext.
	 */
	@Deprecated
	public static void buildFacesMessage(String clientId, String summary, String[] summaryParams, Severity serverity) {
		if (summaryParams != null) {
			for (int i = 0; i < summaryParams.length; i++) {
				summaryParams[i] = buildTranslatedMessage(summaryParams[i], null);
			}
		}

		String summaryMessage = buildTranslatedMessage(summary, summaryParams);

		FacesMessage facesMessage = new FacesMessage();
		FacesContext context = getContext();
		facesMessage.setSeverity(serverity);
		facesMessage.setSummary(summaryMessage);
		addMessage(context, clientId, facesMessage);
	}

	public static void buildFacesMessage(FacesContext context, String clientId, String summary, String[] summaryParams,
			Severity serverity) {
		if (summaryParams != null) {
			for (int i = 0; i < summaryParams.length; i++) {
				summaryParams[i] = buildTranslatedMessage(context, summaryParams[i], null);
			}
		}
		String summaryMessage = buildTranslatedMessage(context, summary, summaryParams);
		FacesMessage facesMessage = new FacesMessage();
		facesMessage.setSeverity(serverity);
		facesMessage.setSummary(summaryMessage);
		addMessage(context, clientId, facesMessage);
	}

	private static String buildTranslatedMessage(String messageKey, Object[] params) {
		if (!StringUtils.isEmpty(messageKey) && messageKey.charAt(0) == '@') {
			String translatedMessage = getMessageProvider().get(messageKey, params);
			if (translatedMessage != null && translatedMessage.trim().length() > 0) {
				return translatedMessage;
			} else {
				return messageKey;
			}
		} else {
			return messageKey;
		}
	}

	private static String buildTranslatedMessage(FacesContext context, String messageKey, Object[] params) {
		if (!StringUtils.isEmpty(messageKey) && messageKey.charAt(0) == '@') {
			String translatedMessage = getMessageProvider(context).get(messageKey, params);
			if (translatedMessage != null && translatedMessage.trim().length() > 0) {
				return translatedMessage;
			} else {
				return messageKey;
			}
		} else {
			return messageKey;
		}
	}

	private static MessageProvider getMessageProvider() {
		FacesContext context = getContext();
		return context.getApplication().evaluateExpressionGet(context, "#{messageProvider}", MessageProvider.class);
	}

	private static MessageProvider getMessageProvider(FacesContext context) {
		return context.getApplication().evaluateExpressionGet(context, "#{messageProvider}", MessageProvider.class);
	}

	public static void buildDeleteRemoveSelectedDetailMessage(Object[] selectedArray, int deleteRemoveCount) {
		String message;
		Severity severity;
		String[] params;
		if (selectedArray != null) {
			if (selectedArray.length == deleteRemoveCount) {
				switch (selectedArray.length) {
				case 0:
					message = buildTranslatedMessage(DELETE_NOT_PERFORMED_AS_NONE_SELECTED_KEY, null);
					break;
				case 1:
					message = buildTranslatedMessage(DELETE_SUCCESSFUL_KEY, null);
					break;
				default:
					params = new String[1];
					params[0] = String.valueOf(selectedArray.length);
					message = buildTranslatedMessage(DELETE_SUCCESSFUL_WITH_COUNT_KEY, params);
					break;
				}
				severity = FacesMessage.SEVERITY_INFO;
			} else {
				params = new String[2];
				params[0] = String.valueOf(deleteRemoveCount);
				params[1] = String.valueOf(selectedArray.length);
				message = buildTranslatedMessage(DELETE_NOT_SUCCESSFUL_WITH_COUNT_KEY, params);
				severity = FacesMessage.SEVERITY_WARN;
			}
		} else {
			message = buildTranslatedMessage(DELETE_NOT_PERFORMED_AS_NONE_SELECTED_KEY, null);
			severity = FacesMessage.SEVERITY_INFO;
		}

		buildDetailMessage(message, severity);
	}

	public static void buildMessageFromException(Exception e) {
		Exception exception = e;

		// If we have an EJB exception, get the exception that caused it
		if (exception instanceof EJBException) {
			exception = ((EJBException) exception).getCausedByException();
		}

		Severity severity;
		String messageKey;
		if (exception instanceof EvoteException) {
			severity = FacesMessage.SEVERITY_ERROR;
			messageKey = exception.getMessage();
		} else if (exception instanceof PersistenceException && exception.getCause() instanceof ConstraintViolationException) {
			buildDetailMessage(DUPLICATE_ID, FacesMessage.SEVERITY_ERROR);
			return;
		} else {
			severity = FacesMessage.SEVERITY_FATAL;
			messageKey = EXCEPTION_GENERAL;
		}

		buildDetailMessage(messageKey, severity);
	}

	public static String timeString(DateTime dateTime, Locale locale) {
		return timeString(dateTime.toLocalTime(), locale);
	}

	public static String timeString(LocalTime localTime, Locale locale) {
		MessageProvider mms = getMessageProvider();
		return DateUtil.getFormattedTime(localTime, DateTimeFormat.forPattern(mms.get(COMMON_TIME_PATTERN)).withLocale(locale));
	}

	public static String dateString(LocalDate localDate, Locale locale) {
		MessageProvider mms = getMessageProvider();
		return DateUtil.getFormattedDate(localDate, DateTimeFormat.forPattern(mms.get(COMMON_DATE_PATTERN)).withLocale(locale));
	}

	public static void clearMessages() {
		Iterator<FacesMessage> messages = getContext().getMessages();
		while (messages.hasNext()) {
			messages.next();
			messages.remove();
		}
	}

	public static void buildSavedMessage(Displayable displayable) {
		buildDetailMessage(getSavedMessage(displayable), FacesMessage.SEVERITY_INFO);
	}

	public static void buildDeletedMessage(Displayable displayable) {
		buildDetailMessage(getDeletedMessage(displayable), FacesMessage.SEVERITY_INFO);
	}

	public static String getSavedMessage(Displayable displayable) {
		MessageProvider mp = getMessageProvider();
		return mp.get("@common.displayable.saved", new String[] { getDisplay(mp, displayable, true) });
	}

	public static String getDeleteConfirmMessage(Displayable displayable) {
		MessageProvider mp = getMessageProvider();
		return mp.get("@common.displayable.deleteConfirm", new String[] { getDisplay(mp, displayable, true) });
	}

	public static String getDeletedMessage(Displayable displayable) {
		MessageProvider mp = getMessageProvider();
		return mp.get("@common.displayable.deleted", new String[] { getDisplay(mp, displayable, true) });
	}

	public static String getDisplay(MessageProvider mp, Displayable displayable, boolean toLower) {
		String simpleName = displayable.getClass().getSimpleName();
		int index = simpleName.indexOf('$');
		if (index != -1) {
			simpleName = simpleName.substring(0, index);
		}
		String type = mp.get("@common.displayable." + simpleName);
		if (toLower) {
			type = type.toLowerCase(Locale.ENGLISH);
		}
		return type + " " + displayable.display();
	}

	/** FOR TESTING */
	private static FacesContext context;

	private static FacesContext getContext() {
		if (context != null) {
			return context;
		}
		return FacesContext.getCurrentInstance();
	}

	public static void setContext(FacesContext context) {
		MessageUtil.context = context;
	}

	private static void addMessage(FacesContext ctx, String clientId, FacesMessage msg) {
		if (!exists(ctx, msg)) {
			ctx.addMessage(clientId, msg);
		}
	}

	private static boolean exists(FacesContext ctx, FacesMessage msg) {
		for (Iterator<FacesMessage> iter = ctx.getMessages(null); iter.hasNext();) {
			FacesMessage existing = iter.next();
			if (existing.getSeverity() == msg.getSeverity() && StringUtils.equals(existing.getSummary(), msg.getSummary())) {
				return true;
			}
		}
		return false;
	}
}
