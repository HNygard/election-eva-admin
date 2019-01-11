package no.valg.eva.admin.common;

import org.joda.time.LocalDate;

public final class Foedselsnummer extends PersonId {

	public static final int D_NUMBER_START_DAY = 40;
	public static final int D_NUMBER_START_MONTH = 40;
	public static final int DAY_BEGIN_INDEX = 0;
	public static final int MONTH_BEGIN_INDEX = 2;
	public static final int YEAR_BEGIN_INDEX = 4;
	public static final int INDIVIDUAL_DIGITS_START_INDEX = 6;
	public static final int INDIVIDUAL_DIGITS_END_INDEX = 9;

	public static final int RANGE_START_19TH_CENTURY = 500;
	public static final int RANGE_END_19TH_CENTURY = 749;
	public static final int RANGE1_START_20TH_CENTURY = 0;
	public static final int RANGE1_END_20TH_CENTURY = 499;
	public static final int RANGE2_START_20TH_CENTURY = 900;
	public static final int RANGE2_END_20TH_CENTURY = 999;
	public static final int RANGE_START_21ST_CENTURY = 500;
	public static final int RANGE_END_21ST_CENTURY = 999;

	public static final int CUTOFF_19TH_CENTURY = 55;
	public static final int CUTOFF_21ST_CENTURY = 39;
	public static final int CUTOFF_20TH_CENTURY = 40;

	public static final int NINETEENTH_CENTURY = 18;
	public static final int TWENTIETH_CENTURY = 19;
	public static final int TWENTYFIRST_CENTURY = 20;

	public static final int CENTURY_MULTIPLIER = 100;
	public static final int INDEX_OF_THIRD_DIGIT_IN_PERSONNUMMER = 8;

	public Foedselsnummer(String foedselsnummer) {
		super(foedselsnummer);
	}

	public LocalDate dateOfBirth() {
		int individsifre = Integer.parseInt(id.substring(INDIVIDUAL_DIGITS_START_INDEX, INDIVIDUAL_DIGITS_END_INDEX));
		int day = Integer.parseInt(id.substring(DAY_BEGIN_INDEX, MONTH_BEGIN_INDEX));
		int birthday;
		if (day > D_NUMBER_START_DAY) {
			// D-nummer
			birthday = day - D_NUMBER_START_DAY;
		} else {
			birthday = day;
		}
		int month = Integer.parseInt(id.substring(MONTH_BEGIN_INDEX, YEAR_BEGIN_INDEX));
		int birthmonth;
		if (month > D_NUMBER_START_MONTH) {
			// H-nummer
			birthmonth = month - D_NUMBER_START_MONTH;
		} else {
			birthmonth = month;
		}
		int year = Integer.parseInt(id.substring(YEAR_BEGIN_INDEX, INDIVIDUAL_DIGITS_START_INDEX));
		int century;
		if (individsifre >= RANGE1_START_20TH_CENTURY && individsifre <= RANGE1_END_20TH_CENTURY) {
			// 000-499 is 1900-1999
			century = TWENTIETH_CENTURY;
		} else if (individsifre >= RANGE_START_19TH_CENTURY && individsifre <= RANGE_END_19TH_CENTURY && year >= CUTOFF_19TH_CENTURY) {
			// 500-749 is 1855-1899
			century = NINETEENTH_CENTURY;
		} else if (individsifre >= RANGE_START_21ST_CENTURY && individsifre <= RANGE_END_21ST_CENTURY && year <= CUTOFF_21ST_CENTURY) {
			// 500-999 is 2000-2039
			century = TWENTYFIRST_CENTURY;
		} else if (individsifre >= RANGE2_START_20TH_CENTURY && individsifre <= RANGE2_END_20TH_CENTURY && year >= CUTOFF_20TH_CENTURY) {
			// 900-999 is 1940–1999
			century = TWENTIETH_CENTURY;
		} else {
			throw new IllegalStateException(
					"cannot convert the national identification number (" + id
							+ ") to a birth date");
		}
		year = century * CENTURY_MULTIPLIER + year;
		return new LocalDate(year, birthmonth, birthday);
	}

	public String gender() {
		return Integer.valueOf(getId().substring(INDEX_OF_THIRD_DIGIT_IN_PERSONNUMMER, INDEX_OF_THIRD_DIGIT_IN_PERSONNUMMER + 1)) % 2 == 0 ? "K" : "M";
	}
}
