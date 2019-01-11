package no.valg.eva.admin.common.configuration.model;

import java.io.Serializable;

import no.valg.eva.admin.common.validator.LuhnChecksumValidation;

import org.apache.commons.lang3.StringUtils;

public class Manntallsnummer implements Serializable {

	public static final int KORT_MANNTALLSNUMMER_LENGTH = 10;
	private static final int FULLT_MANNTALLSNUMMER_LENGTH = KORT_MANNTALLSNUMMER_LENGTH  + 2;
	private static final int VALGAARSSIFFER_INDEX = KORT_MANNTALLSNUMMER_LENGTH;
	private static final int KONTROLLSIFFER_INDEX = 11;
	private static final int VALGAARSSIFFER_MAX = 9;

	private final String manntallsnummer;
	
	public Manntallsnummer(String manntallsnummer) {
		validerManntallsnummer(manntallsnummer);
		this.manntallsnummer = manntallsnummer;
	}

	private void validerManntallsnummer(String manntallsnummer) {
		sjekkNotNull(manntallsnummer);
		sjekkLengdeManntallsnummer(manntallsnummer);
		sjekkKontrollsifferManntallsnummer(manntallsnummer);
	}

	private void sjekkNotNull(String manntallsnummer) {
		if (manntallsnummer == null) {
			throw new IllegalArgumentException();
		}
	}

	private void sjekkLengdeManntallsnummer(String manntallsnummer) {
		if (manntallsnummer.length() != FULLT_MANNTALLSNUMMER_LENGTH) {
			throw new IllegalArgumentException();
		}
	}

	private void sjekkKontrollsifferManntallsnummer(String manntallsnummer) {
		if (!LuhnChecksumValidation.isGyldigKontrollsiffer(manntallsnummer)) {
			throw new IllegalArgumentException();
		}
	}

	public Manntallsnummer(Long kortManntallsNummer, int valgaarsSiffer) {
		validerKortManntallsnummer(kortManntallsNummer);
		validerValgaarssiffer(valgaarsSiffer);
		String manntallsnummerUtenKontrollsiffer = manntallsnummerOgValgaarssiffer(kortManntallsNummer, valgaarsSiffer);
		int kontrollsiffer = beregnKontrollsiffer(manntallsnummerUtenKontrollsiffer);
		this.manntallsnummer = manntallsnummerUtenKontrollsiffer + kontrollsiffer;
	}

	private void validerKortManntallsnummer(Long kortManntallsNummer) {
		if (kortManntallsNummer == null || kortManntallsNummer < 0) {
			throw new IllegalArgumentException("Ugyldig kort manntallsnummer: " + kortManntallsNummer + ". Manntallsnummeret skal være 0 eller høyere");
		}
	}

	private void validerValgaarssiffer(int valgaarsSiffer) {
		if (valgaarsSiffer < 0 || valgaarsSiffer > VALGAARSSIFFER_MAX) {
			throw new IllegalArgumentException("Ugyldig valgårssiffer: " + valgaarsSiffer + ". Valgårssifferet skal være mellom 0 og 9");
		}
	}

	private String manntallsnummerOgValgaarssiffer(Long kortManntallsNummer, int valgaarsSiffer) {
		return StringUtils.leftPad("" + kortManntallsNummer, KORT_MANNTALLSNUMMER_LENGTH, "0") + valgaarsSiffer;
	}

	private int beregnKontrollsiffer(String manntallsnummerUtenKontrollsiffer) {
		return LuhnChecksumValidation.beregnKontrollsiffer(manntallsnummerUtenKontrollsiffer);
	}

	public String getManntallsnummer() {
		return manntallsnummer;
	}

	public String getManntallsnummerMasked() {
		return manntallsnummer.substring(0, KORT_MANNTALLSNUMMER_LENGTH) + " " + manntallsnummer.substring(KORT_MANNTALLSNUMMER_LENGTH);
	}
	
	public Long getKortManntallsnummer() {
		return Long.parseLong(manntallsnummer.substring(0, VALGAARSSIFFER_INDEX));
	}

	public String getKortManntallsnummerMedZeroPadding() {
		return manntallsnummer.substring(0, VALGAARSSIFFER_INDEX);
	}

	public Integer getValgaarssiffer() {
		return Integer.parseInt(manntallsnummer.substring(VALGAARSSIFFER_INDEX, VALGAARSSIFFER_INDEX + 1));
	}
	
	public Integer getKontrollsiffer() {
		return Integer.parseInt(manntallsnummer.substring(KONTROLLSIFFER_INDEX, KONTROLLSIFFER_INDEX + 1));
	}

	/**
	 * Sluttsifrene er definert som valgårssiffer og kontrollsiffer, dvs. de to siste sifrene i manntallsnummeret
	 */
	public String getSluttsifre() {
		return manntallsnummer.substring(VALGAARSSIFFER_INDEX, VALGAARSSIFFER_INDEX + 2);
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		Manntallsnummer that = (Manntallsnummer) o;

		return manntallsnummer.equals(that.manntallsnummer);
	}

	@Override
	public int hashCode() {
		return manntallsnummer.hashCode();
	}
}
