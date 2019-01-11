package no.valg.eva.admin.voting.domain.electoralroll;

import org.testng.annotations.Test;

import static no.valg.eva.admin.configuration.domain.model.Voter.ENDRINGSTYPE_AVGANG;
import static no.valg.eva.admin.configuration.domain.model.Voter.ENDRINGSTYPE_ENDRING;
import static no.valg.eva.admin.configuration.domain.model.Voter.ENDRINGSTYPE_INITIELL;
import static no.valg.eva.admin.configuration.domain.model.Voter.ENDRINGSTYPE_TILGANG;
import static org.assertj.core.api.Assertions.assertThat;

public class SkdVoterRecordTest {

    private static final String VOTER_RECORD_STRING_WITHOUT_FIRST_THREE_CHARS = "2010-12-02-17.20.54.00000002122010000000000011Bakkevik Petter                         "
            + "Bakkevik                                          Petter                                                                                              "
            + "01010460000009900000000001776HALDEN          Major Forbus gate        Major Forbus gate 10                    O                              00001"
            + "                                                                                                                           I30011234";

    @Test
    public void isElectoralRollChange_whenTheEntryIsAnInitialEntry_returnsFalse() {
        assertThat(getVoterRecord(ENDRINGSTYPE_INITIELL).isElectoralRollChange()).isFalse();
    }

    @Test
    public void isElectoralRollChange_whenTheEntryIsAnEndringTilgangOrAvgang_returnsTrue() {
        assertThat(getVoterRecord(ENDRINGSTYPE_ENDRING).isElectoralRollChange()).isTrue();
        assertThat(getVoterRecord(ENDRINGSTYPE_TILGANG).isElectoralRollChange()).isTrue();
        assertThat(getVoterRecord(ENDRINGSTYPE_AVGANG).isElectoralRollChange()).isTrue();
    }

    private SkdVoterRecord getVoterRecord(char endringstype) {
        String recordString = "  " + endringstype + VOTER_RECORD_STRING_WITHOUT_FIRST_THREE_CHARS;
        return new SkdVoterRecord(recordString, "123");
    }

    @Test
    public void kommunenummer() {
        assertThat(getVoterRecord(ENDRINGSTYPE_ENDRING).kommunenr()).isEqualTo("3001");
    }
    
    @Test
    public void legacyKommunenummer() { assertThat(getVoterRecord(ENDRINGSTYPE_ENDRING).legacyKommunenr()).isEqualTo("0101");}

    @Test
    public void valgkrets() {
        assertThat(getVoterRecord(ENDRINGSTYPE_ENDRING).valgkrets()).isEqualTo("1234");
    }

    @Test
    public void legacyValgkrets() { assertThat(getVoterRecord(ENDRINGSTYPE_ENDRING).legacyValgkrets()).isEqualTo("0001");
    }
}

