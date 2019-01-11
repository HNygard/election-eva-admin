package no.valg.eva.admin.voting.domain.electoralroll;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static no.valg.eva.admin.configuration.domain.model.Voter.ENDRINGSTYPE_AVGANG;
import static no.valg.eva.admin.configuration.domain.model.Voter.ENDRINGSTYPE_ENDRING;
import static no.valg.eva.admin.configuration.domain.model.Voter.ENDRINGSTYPE_TILGANG;

/**
 * Intended for holding semi-parsed values for a voter
 * Values are fetched from the string-based SKD format
 * Values are also stored on string format (so further processing is needed to get Voter objects)
 */
public class SkdVoterRecord implements VoterRecord {
	public static final String SKD_DATE_FORMAT = "ddMMyyyy";
	private static final String SKD_TIMESTAMP_FORMAT = "yyyy-MM-dd-HH.mm.ss.SSS";
	
	private String record;
	private String kjorenr;

	SkdVoterRecord(String record, String kjorenr) {
		this.record = record;
		this.kjorenr = kjorenr;
	}

	@Override
	public String kjorenr() {
		return kjorenr;
	}
	
	@Override
	public String aarsakskode() {
		return record.substring(0, 0 + 2);
	}

	@Override
	public String endringstype() {
		return record.substring(2, 2 + 1);
	}
	
	@Override
	public Character endringstypeChar() {
		if (endringstype().length() > 0) {
			return endringstype().charAt(0);
		} else {
			return null;
		}
	}

	@Override
	public boolean isElectoralRollChange() {
		return endringstypeChar().equals(ENDRINGSTYPE_ENDRING) || endringstypeChar().equals(ENDRINGSTYPE_TILGANG) || endringstypeChar().equals(ENDRINGSTYPE_AVGANG);
	}

	@Override
	public String timestamp() {
		return record.substring(3, 3 + 26);
	}
	
	@Override
	public Timestamp timestampAsTimestamp() throws ParseException {
		if (timestamp().trim().length() == 26) {
			return toTimestampWithMicroSeconds(timestamp());
		} else {
			return null;
		}
	}

	@Override
	public String regDato() {
		return record.substring(29, 29 + 8);
	}

	@Override
	public String foedselsnr() {
		return record.substring(37, 37 + 11);
	}

	@Override
	public String statuskode() {
		return record.substring(48, 48 + 1);
	}

	@Override
	public String etternavn() {
		return record.substring(89, 89 + 50);
	}

	@Override
	public String fornavn() {
		return record.substring(139, 139 + 50);
	}

	@Override
	public String mellomnavn() {
		return record.substring(189, 189 + 50);
	}


	@Override
	public String legacyKommunenr() {
		return record.substring(239, 239 + 4);
	}

	@Override
	public String postnr() {
		return record.substring(264, 264 + 4);
	}

	@Override
	public String poststed() {
		return record.substring(268, 268 + 16);
	}

	@Override
	public String adressenavn() {
		return record.substring(284, 284 + 25);
	}

	@Override
	public String adresse() {
		return record.substring(309, 309 + 40);
	}

	@Override
	public String adressetype() {
		return record.substring(349, 349 + 1);
	}

	@Override
	public String tilleggsadresse() {
		return record.substring(350, 350 + 30);
	}

	@Override
	public String spesRegType() {
		return record.substring(380, 380 + 1);
	}

	@Override
	public String legacyValgkrets() {
		return record.substring(381, 381 + 4);
	}

	@Override
	public String postadrLinje1() {
		return record.substring(385, 385 + 40);
	}

	@Override
	public String postadrLinje2() {
		return record.substring(425, 425 + 40);
	}

	@Override
	public String postadrLinje3() {
		return record.substring(465, 465 + 40);
	}

	@Override
	public String postadrLandkode() {
		return record.substring(505, 505 + 3);
	}

	@Override
	public boolean eligibleInSamiElection() {
		return record.substring(508, 509).equals("I");
	}
	
	@Override
	public String kommunenr() {
		return record.substring(509, 509 + 4);
	}

	@Override
	public String valgkrets() {
		return record.substring(513, 513 + 4);
	}

	private Timestamp toTimestampWithMicroSeconds(String skdVoterRecordTimestamp) throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat(SKD_TIMESTAMP_FORMAT, Locale.getDefault());
		Date date = sdf.parse(skdVoterRecordTimestamp.substring(0, 23));

		Timestamp timestamp = new Timestamp(date.getTime());
		timestamp.setNanos(Integer.parseInt(skdVoterRecordTimestamp.substring(20, 26)) * 1000);

		return timestamp;
	}

	@Override
	public String toString() {
		return record;
	}
}

