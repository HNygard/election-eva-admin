package no.valg.eva.admin.configuration.domain.model.manntall;

import lombok.Setter;
import no.valg.eva.admin.common.configuration.model.Manntallsnummer;

import org.apache.commons.lang3.StringUtils;

@Setter
public class ValgkortgrunnlagRad {
	
	private String valgstyretNavn;
	private String valgstyretAdresselinje1;
	private String valgstyretPostnummer;
	private String valgstyretPoststed;

	private String valgkretsId;
	private String rode;
	private String manntallSide;
	private String manntallLinje;
	
	private String fodselsaar;
	private String kommuneId;
	private String kortManntallsnummer;
	private String navn;
	private String fulltManntallsnummer;
	private String adresselinje1;
	private String adresselinje2;
	private String adresselinje3;
	private String postnummer; 
	private String poststed; 

	private String infotekst;
	
	private String valglokaleAapningstider;
	private String valglokaleNavn;
	private String valglokaleAdresselinje1;
	private String valglokalePostnummer;
	private String valglokalePoststed;
	
	private String maalform;

	public String getValgstyretNavn() {
		return erstattSemikolonMedKomma(trimAndEmptyIfNull(valgstyretNavn));
	}

	public String getValgstyretAdresselinje1() {
		return erstattSemikolonMedKomma(trimAndEmptyIfNull(valgstyretAdresselinje1));
	}

	public String getValgstyretPostnummer() {
		return erstattSemikolonMedKomma(trimAndEmptyIfNull(valgstyretPostnummer));
	}

	public String getValgstyretPoststed() {
		return erstattSemikolonMedKomma(trimAndEmptyIfNull(valgstyretPoststed));
	}

	public String getValgkretsId() {
		return erstattSemikolonMedKomma(trimAndEmptyIfNull(valgkretsId));
	}

	public String getRode() {
		return erstattSemikolonMedKomma(trimAndEmptyIfNull(rode));
	}

	public String getManntallSide() {
		return erstattSemikolonMedKomma(trimAndEmptyIfNull(manntallSide));
	}

	public String getManntallLinje() {
		return erstattSemikolonMedKomma(trimAndEmptyIfNull(manntallLinje));
	}

	public String getFodselsaar() {
		return erstattSemikolonMedKomma(trimAndEmptyIfNull(fodselsaar));
	}

	public String getKommuneId() {
		return erstattSemikolonMedKomma(trimAndEmptyIfNull(kommuneId));
	}

	public String getKortManntallsnummer() {
		return StringUtils.leftPad(trimAndEmptyIfNull(kortManntallsnummer), Manntallsnummer.KORT_MANNTALLSNUMMER_LENGTH, "0");
	}

	public String getNavn() {
		return erstattSemikolonMedKomma(trimAndEmptyIfNull(navn));
	}

	public String getFulltManntallsnummer() {
		return erstattSemikolonMedKomma(trimAndEmptyIfNull(fulltManntallsnummer));
	}

	public String getAdresselinje1() {
		return erstattSemikolonMedKomma(trimAndEmptyIfNull(adresselinje1));
	}

	public String getAdresselinje2() {
		return erstattSemikolonMedKomma(trimAndEmptyIfNull(adresselinje2));
	}

	public String getAdresselinje3() {
		return erstattSemikolonMedKomma(trimAndEmptyIfNull(adresselinje3));
	}

	public String getPostnummer() {
		return erstattSemikolonMedKomma(trimAndEmptyIfNull(postnummer));
	}

	public String getPoststed() {
		return erstattSemikolonMedKomma(trimAndEmptyIfNull(poststed));
	}

	public String getInfotekst() {
		return erstattSemikolonMedKomma(trimAndEmptyIfNull(infotekst));
	}

	public String getValglokaleAapningstider() {
		return erstattSemikolonMedKomma(trimAndEmptyIfNull(valglokaleAapningstider));
	}

	public String getValglokaleNavn() {
		return erstattSemikolonMedKomma(trimAndEmptyIfNull(valglokaleNavn));
	}

	public String getValglokaleAdresselinje1() {
		return erstattSemikolonMedKomma(trimAndEmptyIfNull(valglokaleAdresselinje1));
	}

	public String getValglokalePostnummer() {
		return erstattSemikolonMedKomma(trimAndEmptyIfNull(valglokalePostnummer));
	}

	public String getValglokalePoststed() {
		return erstattSemikolonMedKomma(trimAndEmptyIfNull(valglokalePoststed));
	}

	public String getMaalform() {
		return erstattSemikolonMedKomma(trimAndEmptyIfNull(maalform));
	}

	private String trimAndEmptyIfNull(String s) {
		return s == null ? "" : s.trim();
	}
	
	private String erstattSemikolonMedKomma(String s) { 
		return s.replace(";", ","); 
	}
}
