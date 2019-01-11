package no.evote.constants;

import com.google.common.collect.ImmutableMap;

import java.util.Locale;
import java.util.Map;

public final class EvoteConstants {

	public static final String NOT_IN_ELECTORAL_ROLL = "1";
	public static final String DEAD_VOTER = "2";
	public static final String MULTIPLE_VOTES = "3";

	public static final String WARNING_UNCHECKED = "unchecked";

	public static final String REGEXP_PARTY_ID = "(([A-ZÆØÅÄÖÜa-zæøåäöéü_]+)([A-ZÆØÅÄÖÜa-zæøåäöéü_]{1,50})?)?";
	public static final String REGEXP_REMOVE_SEARCH = "['|\"]";

	public static final String CHARACTER_SET = "UTF-8";
	public static final String CHARACTER_SET_ISO = "ISO-8859-1";

	// 4 hours conversation timeout, needs to be longer than session timeout (30 mins) - shorter timeout gives 550-error situations (no conversation found to
	// restore..)
	public static final long CONVERSATION_TIMEOUT = (long) 4 * 60 * 60 * 1000;

	public static final String SCHEDULED_IMPORT_OPERATOR_ID = "00000000003";
	public static final String SCHEDULED_IMPORT_ROLE = "planlagt_import";

	public static final int MAX_CANDIDATES_IN_AFFILIATION = 97;

	public static final int FREEZE_LEVEL_AREA = 2;

	public static final int BALLOT_STATUS_UNDERCONSTRUCTION = 0;
	public static final int BALLOT_STATUS_WITHDRAWN = 1;
	public static final int BALLOT_STATUS_PENDING = 2;
	public static final int BALLOT_STATUS_APPROVED = 3;
	public static final int BALLOT_STATUS_REJECTED = 4;

	// used to separate late_validation votes from other early votes
	public static final String VOTING_CATEGORY_LATE = "late";

	public static final int BATCH_STATUS_IN_QUEUE_ID = 0;
	public static final int BATCH_STATUS_STARTED_ID = 1;
	public static final int BATCH_STATUS_COMPLETED_ID = 2;
	public static final int BATCH_STATUS_FAILED_ID = 3;

	public static final long MAX_FILE_SIZE = 250000000;

	public static final String BALLOT_BLANK = "BLANK";

	public static final String ELECTION_TYPE_DIRECT = "D";
	public static final String ELECTION_TYPE_CALCULATED = "F";
	public static final String ELECTION_TYPE_REFERENDUM = "R";

	public static final String PARTY_ID_BLANK = "BLANK";
	public static final String VALGNATT_PARTY_ID_BLANKE = "BLANKE";
	public static final String VALGNATT_PARTY_NAME_BLANKE = "Blanke";

	public static final String CANDIDATE_VOTE_CATEGORY_WRITEIN = "writein";
	public static final String CANDIDATE_VOTE_CATEGORY_PERSONAL = "personal";

	public static final String DEFAULT_LOCALE = "nb-NO";
	public static final Locale DEFAULT_JAVA_LOCALE = new Locale("nb", "NO");

	public static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZÆØÅ";
	public static final String ELECTION_EVENT_ADMIN = "valghendelse_admin";
	public static final String SYSTEM_ADMIN = "system_admin";

	public static final String[] DEFAULT_ROLES = new String[] { SCHEDULED_IMPORT_ROLE, ELECTION_EVENT_ADMIN, SYSTEM_ADMIN };

	public static final String USER_SUPPORT_ERROR_MSG = "User support does not have write privileges";

	public static final String VALID_EMAIL_REGEXP = "^[A-ZÆØÅæøåa-z0-9_\\-\\.]{1,}@[A-ZÆØÅæøåa-z0-9_\\-\\.]{2,}\\.[a-z]{2,}$";
	public static final String VALID_GPS_PATTERN = "[-+]?([1-8]?\\d(\\.\\d+)?|90(\\.0+)?),\\s*[-+]?(180(\\.0+)?|((1[0-7]\\d)|([1-9]?\\d))(\\.\\d+)?)|^$";
	public static final Map<String, String> MIME_TYPES = ImmutableMap.of(
			"pdf", "application/pdf",
			"csv", "text/csv",
			"xls", "application/vnd.ms-excel",
			"html", "text/html",
			"xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

	private EvoteConstants() {
		throw new AssertionError();
	}
}
