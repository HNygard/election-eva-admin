package no.evote.exception;

import static java.lang.String.format;

import no.valg.eva.admin.common.MessageTranslator;

public enum ErrorCode {

	// @formatter:off
	
	// LegacyUserDataServiceBean
	ERROR_CODE_0101_NO_OPERATOR("0101", "No operator found with ID %s for event %s"),
	ERROR_CODE_0102_NO_ROLE("0102", "No role found with ID %s for event %s"),
	ERROR_CODE_0103_NO_OPERATOR_ROLE("0103", "No role found with operator/role %s/%s for election path %s and area path %s"),
	ERROR_CODE_0104_UNEXPECTED_TOKEN_VALIDATION("0104", "Unexpected token validation error"),
	ERROR_CODE_0105_TOKEN_XML_PARSING_ERROR("0105", "Token xml parsing error: %s"),
	ERROR_CODE_0106_SIGNATURE_VERIFICATION_ERROR("0106", "@count.error.sign.signing_fails"),
	ERROR_CODE_0107_NO_PEM_FILE("0107", "@count.error.sign.no_pem"),
	ERROR_CODE_0108_TOKEN_EXPIRED("0108", "Token has expired"),
	ERROR_CODE_0109_INVALID_USER("0109", "Wrong user ID"),

	// BatchServiceBean
	ERROR_CODE_0201_NO_DATA("0201", "No data, unable to continue."),
	ERROR_CODE_0202_UNABLE_TO_FIND_ACCESS_WITH_PATH("0202", "Unable to find access with path: %s"),
	ERROR_CODE_0203_UNEXPECTED_BATCH_ERROR("0203", "Unexpected batch error"),
	ERROR_CODE_0204_UNABLE_TO_FIND_BATCH_WITH_PK("0204", "Unable to find batch with pk: %s"),
	ERROR_CODE_0205_GENERERING_MANNTALLSNUMRE_ALLEREDE_STARTET_ELLER_FULLFORT("0205", "Generering av manntallsnummere er allerede pågående eller fullført"),
	ERROR_CODE_0206_GENERERING_AV_MANNTALLSNUMRE_IKKE_FULLFORT("0206", "Generering av manntallsnumre er ikke fullført"),

	// CountingImportServiceBean
	ERROR_CODE_0301_NO_PEM("0301", "@count.error.sign.no_pem"),
	ERROR_CODE_0302_SIGNATURE_VALIDATION_FAILED("0302", "Signature validation failed"),
	ERROR_CODE_0303_XML_VALIDATE_FAILED("0303", "Failed to validate xml file: %s"),
	ERROR_CODE_0304_UNEXPECTED_COUNTING_IMPORT_ERROR("0304", "Unexpected counting import error"),
	ERROR_CODE_0305_FAILED_TO_PARSE_COUNT_FILE("0305", "Failed to parse count file: %s"),
	ERROR_CODE_0306_WRONG_ELECTION_EVENT("0306", "Trying to import data for election event %s, but current election event is %s"),
	ERROR_CODE_0307_UNKNOWN_ELECTION("0307", "Unable to find election with ID %s for election event %s and election group %s"),
	ERROR_CODE_0308_UNKNOWN_ELECTION_GROUP("0308", "Unable to find election group with ID %s for election event %s"),
	ERROR_CODE_0309_UNKNOWN_POLLING_DISTRICT(
			"0309",
			"Unable to find polling district or borough for area path %s. Please verify that it has correct IDs and all levels down to and including polling district."),
	ERROR_CODE_0310_XML_CHILD_ELEMENT_NOT_FOUND("0310", "Expected to find element with name %s as a child of %s"),
	ERROR_CODE_0311_XML_ATTRIBUTE_NOT_FOUND("0311", "Expected to find attribute %s on element %s"),
	ERROR_CODE_0312_UNABLE_TO_FIND_REPORT_COUNT_CATEGORY("0312",
			"Unable to find report count category, most likely mismatch between configuration and import data."),
	ERROR_CODE_0313_UNKNOWN_CONTEST("0313", "Unable to find contest with id %s for election %s"),
	ERROR_CODE_0314_UNKNOWN_REPORTING_UNIT("0314", "Unable to find reporting unit with id %s for election event %s"),
	ERROR_CODE_0315_FOUND_MORE_THAN_ONE_REPORTING_UNIT("0315", "Found more then one reporting unit"),
	ERROR_CODE_0316_UNKNOWN_VOTE_COUNT_CATEGORY("0316", "Unable to find vote count category with id %s"),
	ERROR_CODE_0317_MISMATCH_BETWEEN_REPORTING_UNITS("0317", "@count.error.import.mismatch_between_reporting_units"),
	ERROR_CODE_0318_CANT_BE_FINAL("0318", "@count.error.import.cant_be_final"),
	ERROR_CODE_0319_WRONG_ROLE("0319", "@count.error.import.wrongRole"),
	ERROR_CODE_0320_SHOULD_BE_FINAL("0320", "@count.error.import.should_be_final"),
	ERROR_CODE_0321_FINAL_ALREAD_APPROVED("0321", "@count.error.final_already_approved"),
	ERROR_CODE_0322_WRONG_ELECTION_EVENT("0322", "@count.error.import.wrong_election_event"),
	ERROR_CODE_0323_NO_ID_ATTRIBUTE_ON_ELEMENT("0323", "No Id attribute found on %s element."),
	ERROR_CODE_0324_NO_TIFF_FILE_FOUND("0324", "No TIFF file found for %s"),
	ERROR_CODE_0325_UNABLE_TO_FIND_CANDIDATE("0325", "Unable to find candidate"),
	ERROR_CODE_0326_INVALID_REASON_CODE_FOR_REJECTED_VOTE_FOR_EARLY_VOTING("0326", "Invalid reason code for rejected vote for early voting: %s"),
	ERROR_CODE_0327_INVALID_REASON_CODE_FOR_REJECTED_VOTE_FOR_ELECTION_DAY_VOTING("0327", "Invalid reason code for rejected vote for election day voting: %s"),
	ERROR_CODE_0328_OPERATOR_MISSING_SUBJECT_SERIAL_NUMBER("0328", "@count.error.operator_missing_subject_serial_number"),
	ERROR_CODE_0329_CERTIFICATE_SERIAL_NUMBER_MISMATCH("0329",
			"Certificate serial number mismatch! Operator's registered certificate serial number is %s, but count file was signed with serial number %s"),
	ERROR_CODE_0330_CERTIFICATE_REVOKED("0330", "Signing certificate has been revoked"),
	ERROR_CODE_0331_ANTALL_LAGT_TIL_SIDE_IKKE_LAGRET("0331", "Antall stemmesedler lagt til side er ikke lagret"),

	// LegacyCountingServiceBean
	ERROR_CODE_0401_TO_LOW_AREA_LEVEL("0401", "@count.error.to_low_area_level"),
	ERROR_CODE_0403_NOT_CENTRAL_PRELIMINARY_COUNT_AND_NOT_PENULTIMATE_COUNT("0403", "@count.error.not_central_preliminary_count_and_not_penultimate_recount"),
	ERROR_CODE_0404_NO_LOWER_IS_READY("0404", "@count.error.no_lower_is_ready"),

	// ContestAreaServiceEjb
	ERROR_CODE_0450_CONTEST_AREA_SKIPPING_EXISTING_IN_SAME_AREA("0450", "@election.contest.contest_area.skipping_existing_in_same_area"),
	ERROR_CODE_0451_CONTEST_AREA_SKIPPING_EXISTING_IN_SAME_ELECTION("0451", "@election.contest.contest_area.skipping_existing_in_same_election"),
	
	// TranslationServiceBean
	ERROR_CODE_0460_WRONG_NUMBER_OF_COLUMNS("0460", "@translation.import.wrongNumberOfColumns"),
	
	// Database errors
	ERROR_CODE_0500_UNEXPECTED("0500", "@database.error.unexpected"),
	ERROR_CODE_0501_OPTIMISTIC_LOCK("0501", "@database.error.optimistic_lock"),
	ERROR_CODE_0502_ENTITY_NOT_FOUND("0502", "@database.error.entity_not_found"),
	ERROR_CODE_0503_CONSTRAINT_VIOLATION("0503", "@database.error.constraint_violation"),
	ERROR_CODE_0504_STALE_OBJECT("0504", "@database.error.stale_object"),
	ERROR_CODE_0505_UNIQUE_CONSTRAINT_VIOLATION("0505", "@database.error.unique_constraint_violation"),

	// Config errors
	ERROR_CODE_0550_UNIQUE_CONSTRAINT_VIOLATION("0550", "@config.error.expect_one_contest"),

	// Report server error
	ERROR_CODE_0590_REPORT_SERVER_ERROR("0590", "@rapport.server.error"),
	ERROR_CODE_0591_REPORT_AREA_LEVEL_ACCESS_ERROR("0591", "@rapport.error.areaLevelAccess"),

	// GenericFileUploadWS
	ERROR_CODE_9101_UNABLE_TO_GET_USER_DATA("9101", "Unable to get user data, please check web service parameters."),
	ERROR_CODE_9102_UNEXPECTED_FILE_UPLOAD_ERROR("9102", "Unexpected file upload error"),
	ERROR_CODE_9103_TOKEN_IS_INVALID("9103", "Token is invalid"),
	ERROR_CODE_9104_ROLE_IS_INVALID("9104", "Only users with role valgansvarlig_kommune or valgansvarlig_fylke can upload ballot counts"),
	ERROR_CODE_9105_TOKEN_SIZE_TOO_BIG("9105", "Token size is too big"),
	ERROR_CODE_9106_ACCESS_IS_INVALID("9106", "Access is invalid");

	// @formatter:on

	private final String code;
	private final String messageFormat;

	ErrorCode(String code, String messageFormat) {
		this.code = code;
		this.messageFormat = messageFormat;
	}

	public String getCode() {
		return code;
	}

	public String formatMessage(Object... parameters) {
		return format(messageFormat, parameters);
	}

	public String formatMessage(MessageTranslator messageTranslator, Object... parameters) {
		String message = format(messageFormat, parameters);
		return messageTranslator.translate(message);
	}
}
