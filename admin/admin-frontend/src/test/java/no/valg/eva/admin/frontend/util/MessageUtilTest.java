package no.valg.eva.admin.frontend.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Locale;

import javax.ejb.EJBException;
import javax.faces.application.FacesMessage;
import javax.persistence.PersistenceException;

import no.evote.exception.EvoteException;
import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.common.UserMessage;
import no.valg.eva.admin.common.configuration.model.Displayable;
import no.valg.eva.admin.common.configuration.model.local.ResponsibleOfficer;

import org.hibernate.exception.ConstraintViolationException;
import org.joda.time.format.DateTimeFormat;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;


public class MessageUtilTest extends BaseFrontendTest {

	@BeforeMethod
	public void setup() throws Exception {
		initializeMocks(); // Just to get default mocks;

		when(getMessageProviderMock().get(MessageUtil.COMMON_TIME_PATTERN)).thenReturn("HH:mm");
		when(getMessageProviderMock().get(MessageUtil.COMMON_DATE_PATTERN)).thenReturn("dd.MM.yyyy");
	}

	@Test
	public void buildDetailMessage_withContextMessageSeverity_assertFacesMessage() throws Exception {
		MessageUtil.buildDetailMessage(getFacesContextMock(), "@message", FacesMessage.SEVERITY_ERROR);

		assertFacesMessage(FacesMessage.SEVERITY_ERROR, "@message");
	}

	@Test
	public void buildDetailMessageFromValidationResults_withListOfMessages_appendsMessage() throws Exception {
		MessageUtil.buildDetailMessageFromValidationResults(Arrays.asList(new UserMessage("@message1"), new UserMessage("@message2")));

		assertFacesMessage(FacesMessage.SEVERITY_ERROR, "@message1, @message2");
	}

	@Test
	public void buildDetailMessage_withMessageSeverity_assertFacesMessage() throws Exception {
		MessageUtil.buildDetailMessage("@message", FacesMessage.SEVERITY_ERROR);

		assertFacesMessage(FacesMessage.SEVERITY_ERROR, "@message");
	}

	@Test
	public void buildDetailMessage_withSeverityAndStrings_assertFacesMessage() throws Exception {
		MessageUtil.buildDetailMessage(FacesMessage.SEVERITY_ERROR, "@message1", "@message2");

		assertFacesMessage(FacesMessage.SEVERITY_ERROR, "@message1@message2");
	}

	@Test
	public void buildMessageForClientId_withClientIdMessageAndSeverity_assertFacesMessage() throws Exception {
		MessageUtil.buildMessageForClientId("clientId", "@message", FacesMessage.SEVERITY_INFO);

		assertFacesMessage("clientId", FacesMessage.SEVERITY_INFO, "@message");
	}

	@Test
	public void buildMessageForClientId_withClientIdSeverityAndMessages_assertFacesMessage() throws Exception {
		MessageUtil.buildMessageForClientId("clientId", FacesMessage.SEVERITY_INFO, "@message1", "@message2");

		assertFacesMessage("clientId", FacesMessage.SEVERITY_INFO, "@message1@message2");
	}

	@Test
	public void buildFacesMessage_withClientIdSummaryParamsAndSeverity_assertFacesMessage() throws Exception {
		MessageUtil.buildFacesMessage("clientId", "@summary", new String[] { "param1", "param2" }, FacesMessage.SEVERITY_INFO);

		assertFacesMessage("clientId", FacesMessage.SEVERITY_INFO, "[@summary, param1, param2]");
	}

	@Test
	public void buildFacesMessage_withContextClientIdSummaryParamsAndSeverity_assertFacesMessage() throws Exception {
		MessageUtil.buildFacesMessage(getFacesContextMock(), "clientId", "@summary", new String[] { "param1", "param2" }, FacesMessage.SEVERITY_INFO);

		assertFacesMessage("clientId", FacesMessage.SEVERITY_INFO, "[@summary, param1, param2]");
	}

	@Test
	public void buildDeleteRemoveSelectedDetailMessage_withNoSelectedArray_assertFacesMessage() throws Exception {
		// public static void buildDeleteRemoveSelectedDetailMessage(final Object[] selectedArray, final int deleteRemoveCount, final boolean isDelete) {
		MessageUtil.buildDeleteRemoveSelectedDetailMessage(null, 1);

		assertFacesMessage(FacesMessage.SEVERITY_INFO, "@common.message.delete.not_performed_as_none_selected");
	}

	@Test
	public void buildDeleteRemoveSelectedDetailMessage_withSelectedArrayNotEqualToDeleteRemoveCount_assertFacesMessage() throws Exception {
		MessageUtil.buildDeleteRemoveSelectedDetailMessage(new String[0], 1);

		assertFacesMessage(FacesMessage.SEVERITY_WARN, "[@common.message.delete.not_successful_with_count, 1, 0]");
	}

	@Test
	public void buildDeleteRemoveSelectedDetailMessage_withSelectedArray0_assertFacesMessage() throws Exception {
		MessageUtil.buildDeleteRemoveSelectedDetailMessage(new String[0], 0);

		assertFacesMessage(FacesMessage.SEVERITY_INFO, "@common.message.delete.not_performed_as_none_selected");
	}

	@Test
	public void buildDeleteRemoveSelectedDetailMessage_withSelectedArray1_assertFacesMessage() throws Exception {
		MessageUtil.buildDeleteRemoveSelectedDetailMessage(new String[1], 1);

		assertFacesMessage(FacesMessage.SEVERITY_INFO, "@common.message.delete.successful");
	}

	@Test
	public void buildDeleteRemoveSelectedDetailMessage_withSelectedArray2_assertFacesMessage() throws Exception {
		MessageUtil.buildDeleteRemoveSelectedDetailMessage(new String[2], 2);

		assertFacesMessage(FacesMessage.SEVERITY_INFO, "[@common.message.delete.successful_with_count, 2]");
	}

	@Test
	public void buildMessageFromException_withEvoteException_assertFacesMessage() throws Exception {
		MessageUtil.buildMessageFromException(new EJBException(new EvoteException("ERROR")));

		assertFacesMessage(FacesMessage.SEVERITY_ERROR, "ERROR");
	}

	@Test
	public void buildMessageFromException_withConstraintViolationException_assertFacesMessage() throws Exception {
		MessageUtil.buildMessageFromException(getConstraintViolationException());

		assertFacesMessage(FacesMessage.SEVERITY_ERROR, "@common.message.evote_application_exception.DUPLICATE_ID");
	}

	@Test
	public void buildMessageFromException_withNullpointerException_assertFacesMessage() throws Exception {
		MessageUtil.buildMessageFromException(new NullPointerException("ERROR"));

		assertFacesMessage(FacesMessage.SEVERITY_FATAL, "@common.message.exception.general");
	}

	@Test
	public void timeString_withNullpointerException_assertFacesMessage() throws Exception {
		assertThat(MessageUtil.timeString(DateTimeFormat.forPattern("dd.MM.yyyy HH:mm").parseLocalTime("01.01.2014 12:00"), Locale.ENGLISH)).isEqualTo("12:00");
	}

	@Test
	public void dateString_withNullpointerException_assertFacesMessage() throws Exception {
		assertThat(MessageUtil.dateString(DateTimeFormat.forPattern("dd.MM.yyyy HH:mm").parseLocalDate("01.01.2014 12:00"), Locale.ENGLISH)).isEqualTo("01.01.2014");
	}

	@Test
	public void clearMessages_withFacesMessageIterator_returns2() throws Exception {
		FacesMessageIterator iteratorMock = new FacesMessageIterator();
		when(getFacesContextMock().getMessages()).thenReturn(iteratorMock);

		MessageUtil.clearMessages();

		assertThat(iteratorMock.getRemoved()).isEqualTo(2);
	}

	@Test
	public void buildSavedMessage_withDisplayable_returnsSavedMessage() throws Exception {
		MessageUtil.buildSavedMessage(getDisplayable());

		assertFacesMessage(FacesMessage.SEVERITY_INFO, "[@common.displayable.saved, @common.displayable.responsibleofficer Test Testesen]");
	}

	@Test
	public void buildDeletedMessage_withDisplayable_returnsDeletedMessage() throws Exception {
		MessageUtil.buildDeletedMessage(getDisplayable());

		assertFacesMessage(FacesMessage.SEVERITY_INFO, "[@common.displayable.deleted, @common.displayable.responsibleofficer Test Testesen]");
	}

	@Test
	public void getDeleteConfirmMessage_withDisplayable_returnsgetDeleteConfirmMessage() throws Exception {
		String result = MessageUtil.getDeleteConfirmMessage(getDisplayable());

		assertThat(result).isEqualTo("[@common.displayable.deleteConfirm, @common.displayable.responsibleofficer Test Testesen]");
	}

	private Displayable getDisplayable() {
		ResponsibleOfficer result = new ResponsibleOfficer();
		result.setFirstName("Test");
		result.setLastName("Testesen");
		return result;
	}

	private EJBException getConstraintViolationException() {
		return new EJBException(new PersistenceException(new ConstraintViolationException("ERROR", new SQLException(), "constraint")));
	}

	static class FacesMessageIterator implements Iterator<FacesMessage> {

		private int messages = 2;
		private int removed = 0;

		@Override
		public boolean hasNext() {
			return messages > 0;
		}

		@Override
		public FacesMessage next() {
			messages--;
			return null;
		}

		@Override
		public void remove() {
			removed++;
		}

		public int getRemoved() {
			return removed;
		}
	}
}
