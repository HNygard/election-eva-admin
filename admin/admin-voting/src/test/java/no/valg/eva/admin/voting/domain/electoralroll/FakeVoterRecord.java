package no.valg.eva.admin.voting.domain.electoralroll;

import java.sql.Timestamp;

/**
 * Useful for test purposes
 */
public abstract class FakeVoterRecord implements VoterRecord {

    public static final String EMPTY_STRING = "";

	@Override
	public String kjorenr() {
		return "0";
	}
    
    @Override
    public String aarsakskode() {
        return null;
    }

    @Override
    public String endringstype() {
        return EMPTY_STRING;
    }

    @Override
    public String timestamp() {
        return EMPTY_STRING;
    }

    @Override
    public Timestamp timestampAsTimestamp() {
        return null;
    }

    @Override
    public String regDato() {
        return EMPTY_STRING;
    }

    @Override
    public String foedselsnr() {
        return null;
    }

    @Override
    public String statuskode() {
        return EMPTY_STRING;
    }

    @Override
    public String etternavn() {
        return EMPTY_STRING;
    }

    @Override
    public String fornavn() {
        return EMPTY_STRING;
    }

    @Override
    public String mellomnavn() {
        return EMPTY_STRING;
    }

    @Override
    public String kommunenr() {
        return "0001";
    }

    @Override
    public String postnr() {
        return EMPTY_STRING;
    }

    @Override
    public String poststed() {
        return EMPTY_STRING;
    }

    @Override
    public String adressenavn() {
        return null;
    }

    @Override
    public String adresse() {
        return EMPTY_STRING;
    }

    @Override
    public String adressetype() {
        return null;
    }

    @Override
    public String tilleggsadresse() {
        return EMPTY_STRING;
    }

    @Override
    public String spesRegType() {
        return EMPTY_STRING;
    }

    @Override
    public String valgkrets() {
        return null;
    }

    @Override
    public String postadrLinje1() {
        return EMPTY_STRING;
    }

    @Override
    public String postadrLinje2() {
        return EMPTY_STRING;
    }

    @Override
    public String postadrLinje3() {
        return EMPTY_STRING;
    }

    @Override
    public String postadrLandkode() {
        return EMPTY_STRING;
    }

    @Override
    public boolean eligibleInSamiElection() {
        return false;
    }
}
