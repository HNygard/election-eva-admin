package no.valg.eva.admin.frontend.validators;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Serializable;
import java.io.StringReader;

import javax.faces.application.FacesMessage;
import javax.faces.bean.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;
import javax.inject.Inject;
import javax.inject.Named;

import no.valg.eva.admin.frontend.i18n.MessageProvider;

@Named
@SessionScoped
public class VotingCardTextValidator implements Serializable, Validator {

	private static final int MAX_LINES = 5;
	private static final int MAX_TEXT_LENGTH = 150;

	@Inject
	private MessageProvider messageProvider;

	@Override
	public void validate(FacesContext facesContext, UIComponent uiComponent, Object o) {
		if (o != null && !isValidVotingCardText(o)) {
			generateErrorMessage();
		}
	}

	private boolean isValidVotingCardText(Object o) {
		if (o == null || o.toString().isEmpty()) {
			return true;
		}

		int numLines = 0;
		int numChars = 0;
		try (LineNumberReader reader = new LineNumberReader(new StringReader(o.toString()))) {
			String line = reader.readLine();
			while (line != null) {
				numLines++;
				numChars += line.length();
				if (numChars > MAX_TEXT_LENGTH || numLines > MAX_LINES) {
					return false;
				}
				line = reader.readLine();
			}
		} catch (IOException ioe) {
			return false;
		}
		return true;
	}

	private void generateErrorMessage() {
		FacesMessage msg = new FacesMessage(messageProvider.get("@config.local.election_card.infoText_invalidSize"));
		msg.setSeverity(FacesMessage.SEVERITY_ERROR);
		throw new ValidatorException(msg);
	}
}
