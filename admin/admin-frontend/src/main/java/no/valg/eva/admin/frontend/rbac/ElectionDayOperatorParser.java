package no.valg.eva.admin.frontend.rbac;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import no.valg.eva.admin.common.rbac.ImportOperatorRoleInfo;
import no.valg.eva.admin.common.rbac.PollingPlaceResponsibleOperator;
import no.valg.eva.admin.common.rbac.VoteReceiver;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

/**
 */
public class ElectionDayOperatorParser extends OperatorSpreadSheetParser {
	public static final int POLLING_PLACE = 5;
	private static final int POLLING_PLACE_SUPERVISOR = 6;
	private static final Pattern POLLING_DISTRICT_PATTERN = Pattern.compile("\\d{4}");

	@Override
	public List<ImportOperatorRoleInfo> toOperatorList(List<List<Pair<String, String>>> rows) throws SpreadSheetValidationException {
		List<ImportOperatorRoleInfo> result = new ArrayList<>();
		List<String> errors = new ArrayList<>();
		for (List<Pair<String, String>> row : rows) {
			List<String> rowErrors = validateRow(row);
			if (rowErrors.isEmpty()) {
				boolean isPollingPlaceSupervisor = getIsPollingPlaceSupervisor(row);
				if (isPollingPlaceSupervisor) {
					result.add(new PollingPlaceResponsibleOperator(getOperatorId(row), getOperatorFirstName(row), getOperatorLastName(row),
							getOperatorEmail(row),
							getOperatorPhone(row), getPollingDistrict(row)));
				} else {
					result.add(new VoteReceiver(getOperatorId(row), getOperatorFirstName(row), getOperatorLastName(row), getOperatorEmail(row),
							getOperatorPhone(row), getPollingDistrict(row)));
				}
			} else {
				errors.addAll(rowErrors);
			}
		}
		if (errors.isEmpty()) {
			return result;
		}
		throw new SpreadSheetValidationException(errors);
	}

	/**
	 * Determines whether supervisor/responsible columns of supplied spreadsheet row contains anything, e.g. "X", which is consider a positive. Only value
	 * considered negative even if present is "nei" (no)
	 * 
	 * @param row
	 *            Spreadsheet row to examine
	 * @return whether the row representing the operator is responsible/supervisor or not
	 */
	private boolean getIsPollingPlaceSupervisor(List<Pair<String, String>> row) {
		if (row.size() > POLLING_PLACE_SUPERVISOR) {
			Pair<String, String> cellData = getPairFromRow(row, POLLING_PLACE_SUPERVISOR);
			String pollingPlaceSupervisorValue = cellData.getValue();
			return (isNotBlank(pollingPlaceSupervisorValue) && pollingPlaceSupervisorValue != null && !pollingPlaceSupervisorValue.toLowerCase(
					Locale.getDefault()).contains("nei"));
		} else {
			return false;
		}
	}

	private String getPollingDistrict(List<Pair<String, String>> row) {
		Pair<String, String> cellData = getPairFromRow(row, POLLING_PLACE);
		String pollingDistrictId = cellData.getValue();
		if (StringUtils.isBlank(pollingDistrictId) || !POLLING_DISTRICT_PATTERN.matcher(pollingDistrictId).matches()) {
			throw new IllegalArgumentException(messageProvider.get("@rbac.import_operators.validation_failed.invalid.polling_district_format",
					cellData.getKey(),
					pollingDistrictId));
		}
		return pollingDistrictId;
	}

	@Override
	protected List<String> validateRow(List<Pair<String, String>> row) {
		List<String> errors = super.validateRow(row);
		try {
			getPollingDistrict(row);
		} catch (IllegalArgumentException e) {
			errors.add(e.getMessage());
		}
		return errors;
	}

}
