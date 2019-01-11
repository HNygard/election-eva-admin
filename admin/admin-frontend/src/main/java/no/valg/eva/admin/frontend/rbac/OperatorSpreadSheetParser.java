package no.valg.eva.admin.frontend.rbac;

import static no.evote.constants.EvoteConstants.VALID_EMAIL_REGEXP;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.inject.Inject;

import no.evote.validation.FoedselsNummerValidator;
import no.valg.eva.admin.common.rbac.ImportOperatorRoleInfo;
import no.valg.eva.admin.frontend.i18n.MessageProvider;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

/**
 */
public abstract class OperatorSpreadSheetParser {
	public static final int OPERATOR_ID = 0;
	public static final int FIRST_NAME = 1;
	public static final int LAST_NAME = 2;
	public static final int EMAIL = 3;
	public static final int PHONE = 4;
	private static final Pattern VALID_EMAIL_PATTERN = Pattern.compile(VALID_EMAIL_REGEXP);

	@Inject
	protected MessageProvider messageProvider;

	public abstract List<ImportOperatorRoleInfo> toOperatorList(List<List<Pair<String, String>>> dataList) throws SpreadSheetValidationException;

	protected List<String> validateRow(List<Pair<String, String>> row) {
		List<String> errors = new ArrayList<>();
		try {
			getOperatorId(row);
		} catch (IllegalArgumentException e) {
			errors.add(e.getMessage());
		}
		try {
			getOperatorFirstName(row);
		} catch (IllegalArgumentException e) {
			errors.add(e.getMessage());
		}
		try {
			getOperatorLastName(row);
		} catch (IllegalArgumentException e) {
			errors.add(e.getMessage());
		}
		try {
			getOperatorEmail(row);
		} catch (IllegalArgumentException e) {
			errors.add(e.getMessage());
		}
		try {
			getOperatorPhone(row);
		} catch (IllegalArgumentException e) {
			errors.add(e.getMessage());
		}
		return errors;
	}

	protected String getOperatorId(List<Pair<String, String>> row) {
		Pair<String, String> cellData = getPairFromRow(row, OPERATOR_ID);
		String operatorId = cellData.getValue();
		if ((operatorId == null || operatorId.matches("\\d+") && !operatorId.matches("\\d{11}")) || !FoedselsNummerValidator.isFoedselsNummerValid(operatorId)) {
			throw new IllegalArgumentException(
					messageProvider.get("@rbac.import_operators.validation_failed.invalid.fnr_format", cellData.getKey(), "" + operatorId));
		}
		return operatorId;
	}

	protected String getOperatorFirstName(List<Pair<String, String>> row) {
		Pair<String, String> cellData = getPairFromRow(row, FIRST_NAME);
		String firstName = cellData.getValue();
		if (isBlank(firstName)) {
			throw new IllegalArgumentException(messageProvider.get("@rbac.import_operators.validation_failed.empty.first_name", cellData.getKey(),
					cellData.getValue()));
		}
		return firstName;
	}

	protected String getOperatorLastName(List<Pair<String, String>> row) {
		Pair<String, String> cellData = getPairFromRow(row, LAST_NAME);
		String lastName = cellData.getValue();
		if (isBlank(lastName)) {
			throw new IllegalArgumentException(messageProvider.get("@rbac.import_operators.validation_failed.empty.last_name", cellData.getKey(),
					cellData.getValue()));
		}
		return lastName;
	}

	protected String getOperatorEmail(List<Pair<String, String>> row) {
		Pair<String, String> cellData = getPairFromRow(row, EMAIL);
		String email = cellData.getValue();
		if (isNotBlank(email) && !VALID_EMAIL_PATTERN.matcher(email).matches()) {
			throw new IllegalArgumentException(messageProvider.get("@rbac.import_operators.validation_failed.invalid.email_format", cellData.getKey(),
					cellData.getValue()));
		}
		return email;
	}

	protected String getOperatorPhone(List<Pair<String, String>> row) {
		Pair<String, String> cellData = getPairFromRow(row, PHONE);
		String telephoneNumber = cellData.getValue();
		if (!isBlank(telephoneNumber) && !telephoneNumber.matches("\\d{8}")) {
			throw new IllegalArgumentException(
					messageProvider.get("@rbac.import_operators.validation_failed.invalid.phone_format", cellData.getKey(), telephoneNumber));
		}
		return telephoneNumber;
	}

	protected Pair<String, String> getPairFromRow(List<Pair<String, String>> row, int index) {
		return row.size() > index ? row.get(index) : new ImmutablePair<String, String>(null, null);
	}
}
