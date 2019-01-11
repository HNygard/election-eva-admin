package no.valg.eva.admin.voting.domain.electoralroll;

import no.valg.eva.admin.configuration.domain.model.Voter;

import java.io.Serializable;
import java.sql.Timestamp;
import java.text.ParseException;

/**
 * Voter record for electoral roll import.
 */
public interface VoterRecord extends Serializable {
    
	String kjorenr();
	
	String aarsakskode();

	String endringstype();
	
	Character endringstypeChar();

    default boolean isInitialEntry() {
        return Voter.ENDRINGSTYPE_INITIELL == endringstypeChar();
    }

    default boolean isEndringstypeTilgang() {
        return Voter.ENDRINGSTYPE_TILGANG == endringstypeChar();        
    }

    default boolean isEndringstypeEndring() {
        return Voter.ENDRINGSTYPE_ENDRING == endringstypeChar();        
    }

    default boolean isEndringstypeAvgang() {
        return Voter.ENDRINGSTYPE_AVGANG == endringstypeChar();        
    }

    boolean isElectoralRollChange();

	String timestamp();

	Timestamp timestampAsTimestamp() throws ParseException;

	String regDato();

	String foedselsnr();

	String statuskode();

	String etternavn();

	String fornavn();

	String mellomnavn();

	String kommunenr();

	String legacyKommunenr();
	
	String postnr();

	String poststed();

	String adressenavn();

	String adresse();

	String adressetype();

	String tilleggsadresse();

	String spesRegType();
	
	String legacyValgkrets();

	String valgkrets();

	String postadrLinje1();

	String postadrLinje2();

	String postadrLinje3();

	String postadrLandkode();

	boolean eligibleInSamiElection();
}
