package no.evote.exception;

import static org.assertj.core.api.Assertions.assertThat;

import no.valg.eva.admin.common.MessageTranslator;

import org.testng.annotations.Test;

public class ErrorCodeTest {
    private static final String OPERATOR_ID = "operatorId";
    private static final String EVENT_ID = "eventId";
    private static final String ERROR_MESSAGE_0101 = "No operator found with ID operatorId for event eventId";

    @Test
	public void formatMessage_givenParameters_returnsFormattedMessage() throws Exception {
		ErrorCode errorCode = ErrorCode.ERROR_CODE_0101_NO_OPERATOR;
		
		String message = errorCode.formatMessage(OPERATOR_ID, EVENT_ID);
		
		assertThat(message).isEqualTo(ERROR_MESSAGE_0101);
	}

	@Test
	public void formatMessage_givenTranslator_returnsTranslatedMessage() throws Exception {
		ErrorCode errorCode = ErrorCode.ERROR_CODE_0106_SIGNATURE_VERIFICATION_ERROR;

		String translatedMessage = errorCode.formatMessage(new MessageTranslator() {
			@Override
			public String translate(String message) {
				return "translatedMessage";
			}
		});

		assertThat(translatedMessage).isEqualTo("translatedMessage");
	}
}
