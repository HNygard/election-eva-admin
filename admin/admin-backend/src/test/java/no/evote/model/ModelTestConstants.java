package no.evote.model;

public final class ModelTestConstants {

	public static final String MESSAGE_NOT_NULL = "may not be null";
	public static final String MESSAGE_NOT_EMPTY = "may not be empty";
	public static final String MESSAGE_SIZE_0_2 = "size must be between 0 and 2";
	public static final String MESSAGE_SIZE_0_30 = "size must be between 0 and 30";
	public static final String MESSAGE_SIZE_0_50 = "size must be between 0 and 50";
	public static final String MESSAGE_SIZE_0_100 = "size must be between 0 and 100";
	public static final String MESSAGE_SIZE_0_152 = "size must be between 0 and 152";
	public static final String MESSAGE_MIN_0 = "must be greater than or equal to 0";
	public static final String MESSAGE_MAX_1 = "must be less than or equal to 1";
	public static final String MESSAGE_MAX_7 = "must be less than or equal to 7";
	public static final String MESSAGE_MAX_9999 = "must be less than or equal to 9999";
	public static final String MESSAGE_NOT_NULL_EMPTY_OR_BLANK = "The string must not be empty or filled with blanks";
	public static final String MESSAGE_MUST_BE_IN_THE_PAST = "must be in the past";
	public static final String MESSAGE_ID_VALIDATION_FAILED = "{@validation.id}";

	public static final String PROPERTY_ID = "id";
	public static final String PROPERTY_NAME = "name";

	private ModelTestConstants() {
		throw new AssertionError();
	}
}
