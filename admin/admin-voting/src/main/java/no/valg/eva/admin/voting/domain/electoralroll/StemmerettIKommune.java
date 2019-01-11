package no.valg.eva.admin.voting.domain.electoralroll;

import static no.evote.util.EvoteProperties.MANNTALLSIMPORT_IGNORER_MANGLENDE_STEMMERETTSALDER_FOR;
import static no.evote.util.EvoteProperties.getProperty;

import java.util.Arrays;
import java.util.HashMap;

import no.evote.util.EvoteProperties;
import no.valg.eva.admin.common.MunicipalityId;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.Voter;

import org.joda.time.LocalDate;

/**
 * Ment for raske oppslag om stemmerettsalder for hver kommune.
 * Merk at denne klassen er Map-versjonen av MunicipalityAgeLimit.
 */
public class StemmerettIKommune extends HashMap<MunicipalityId, LocalDate> {

    private final boolean ignorerManglendeStemmerettsalder;

    public StemmerettIKommune(ElectionEvent valghendelse) {
        super();
        ignorerManglendeStemmerettsalder = sjekkKonfigurasjon(valghendelse);
    }

    private boolean sjekkKonfigurasjon(ElectionEvent valghendelse) {
        String valghendelserSomSkalIgnorereStemmerettsalder;
        valghendelserSomSkalIgnorereStemmerettsalder = getProperty(MANNTALLSIMPORT_IGNORER_MANGLENDE_STEMMERETTSALDER_FOR, true);
        return valghendelserSomSkalIgnorereStemmerettsalder != null && valghendelseFinnesIKonfigurasjon(valghendelse, valghendelserSomSkalIgnorereStemmerettsalder);
    }

    private boolean valghendelseFinnesIKonfigurasjon(ElectionEvent valghendelse, String valghendelserSomSkalIgnorereStemmerettsalder) {
        return Arrays.stream(valghendelserSomSkalIgnorereStemmerettsalder.split(","))
			.map(String::trim)
			.filter(valghendelsesId -> valghendelse.getId().equals(valghendelsesId))
			.count() > 0;
    }

    public boolean forVoterRecord(VoterRecord skdVoterRecord) {
        Voter voter = VoterConverter.fromVoterRecordWithOnlyMunicipalityIdAndBirthdate(skdVoterRecord);
        return forVelger(voter);
    }

    public boolean forVelger(Voter velger) {
        if (ignorerManglendeStemmerettsalder) {
            return true;
        } else {
            return sjekkStemmerettForVelger(velger);
        }
    }

    private boolean sjekkStemmerettForVelger(Voter velger) {
        LocalDate maaVaereFodtFoer = get(new MunicipalityId(velger.getMunicipalityId()));
        validerVelger(velger, maaVaereFodtFoer);
        velger.setDateOfBirthFromFodselsnummerIfMissing();
        return velger.getDateOfBirth().isBefore(maaVaereFodtFoer);
    }

    private void validerVelger(Voter voter, LocalDate mustBeBornBefore) {
        if (mustBeBornBefore == null) {
            throw new RuntimeException("Ingen stemmerettsalderinformasjon funnet for kommune " + voter.getMunicipalityId() 
                + ". Merk at EVA Admin kan konfigureres til å akseptere alle velgere i manntallet ved å legge inn valghendelsesid i den kommaseparerte listen til " 
                + EvoteProperties.MANNTALLSIMPORT_IGNORER_MANGLENDE_STEMMERETTSALDER_FOR + " i evote.properties");
        }

        if (voter.getDateOfBirth() == null && voter.getId() == null) {
            throw new RuntimeException("Ingen fødselsdato spesifisert for velger med id " + voter.getId());
        }
    }

}
