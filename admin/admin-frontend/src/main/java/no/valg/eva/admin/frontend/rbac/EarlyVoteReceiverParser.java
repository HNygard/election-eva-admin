package no.valg.eva.admin.frontend.rbac;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import no.valg.eva.admin.common.rbac.EarlyVoteReceiver;
import no.valg.eva.admin.common.rbac.ImportOperatorRoleInfo;

import org.apache.commons.lang3.tuple.Pair;

/**
 */
public class EarlyVoteReceiverParser extends OperatorSpreadSheetParser {
	public static final int POLLING_PLACE = 5;
	private static final Pattern POLLING_DISTRICT_PATTERN = Pattern.compile("\\d{4}");

	public List<ImportOperatorRoleInfo> toOperatorList(List<List<Pair<String, String>>> rows) throws SpreadSheetValidationException {
		List<ImportOperatorRoleInfo> result = new ArrayList<>();
		List<String> errors = new ArrayList<>();
		for (List<Pair<String, String>> row : rows) {
			List<String> rowErrors = validateRow(row);
			if (rowErrors.isEmpty()) {
				result.add(new EarlyVoteReceiver(getOperatorId(row), getOperatorFirstName(row), getOperatorLastName(row), getOperatorEmail(row),
						getOperatorPhone(row), getPollingPlace(row)));
			} else {
				errors.addAll(rowErrors);
			}
		}
		if (errors.isEmpty()) {
			return result;
		}
		throw new SpreadSheetValidationException(errors);
	}

	private String getPollingPlace(List<Pair<String, String>> row) {
		Pair<String, String> cellData = getPairFromRow(row, POLLING_PLACE);
		String pollingPlaceId = cellData.getValue();
		if (isNotBlank(pollingPlaceId) && !POLLING_DISTRICT_PATTERN.matcher(pollingPlaceId).matches()) {
			throw new IllegalArgumentException(messageProvider.get("@rbac.import_operators.validation_failed.invalid.polling_place_format", cellData.getKey(),
					pollingPlaceId));
		}
		return pollingPlaceId;
	}

	@Override
	protected List<String> validateRow(List<Pair<String, String>> row) {
		List<String> errors = super.validateRow(row);
		try {
			getPollingPlace(row);
		} catch (IllegalArgumentException e) {
			errors.add(e.getMessage());
		}
		return errors;
	}
}
