package no.valg.eva.admin.frontend.rbac;

import java.util.List;

public class SpreadSheetValidationException extends Exception {

	private List<String> errors;

	public SpreadSheetValidationException(List<String> errors) {
		super("Spreadsheet had validation errors: " + errors);
		this.errors = errors;
	}

	public List<String> getErrors() {
		return errors;
	}
}
