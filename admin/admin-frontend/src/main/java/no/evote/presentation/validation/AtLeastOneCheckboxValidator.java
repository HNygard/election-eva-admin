package no.evote.presentation.validation;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.ValidatorException;

import no.valg.eva.admin.frontend.i18n.MessageProvider;

/**
 * Validates that at least one checkbox is checked, within a given grouping. Requires two parameters: grouping and count - see
 * <code>createPollingPlace.xhtml</code> for an example.
 */
@FacesValidator("atLeastOneValidator")
public class AtLeastOneCheckboxValidator implements javax.faces.validator.Validator, Serializable {
	private final Map<String, Boolean> checked = new HashMap<String, Boolean>();
	private final Map<String, Integer> counter = new HashMap<String, Integer>();

	@Override
	public void validate(final FacesContext context, final UIComponent component, final Object value) {
		String grouping = (String) component.getAttributes().get("grouping");
		Integer groupingCounter = this.counter.get(grouping);
		if (groupingCounter == null) {
			groupingCounter = Integer.valueOf(0);
		}
		this.counter.put(grouping, groupingCounter + 1);

		Boolean groupingChecked = this.checked.get(grouping);
		if (groupingChecked == null) {
			groupingChecked = Boolean.FALSE;
			this.checked.put(grouping, groupingChecked);
		}

		if ((Boolean) value) {
			groupingChecked = true;
			this.checked.put(grouping, groupingChecked);
		}

		if (groupingCounter.equals(component.getAttributes().get("count")) && !groupingChecked) {
			MessageProvider messageProvider = context.getApplication().evaluateExpressionGet(context, "#{messageProvider}", MessageProvider.class);

			FacesMessage message = new FacesMessage();
			message.setSeverity(FacesMessage.SEVERITY_FATAL);
			message.setSummary(messageProvider.get("@area.polling_place.opening_hours.required"));
			throw new ValidatorException(message);
		}
	}
}
