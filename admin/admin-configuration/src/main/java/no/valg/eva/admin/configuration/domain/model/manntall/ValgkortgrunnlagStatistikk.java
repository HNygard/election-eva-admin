package no.valg.eva.admin.configuration.domain.model.manntall;

import lombok.Data;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import java.util.HashSet;
import java.util.Set;

@Data
public class ValgkortgrunnlagStatistikk {

    private static final Logger LOG = Logger.getLogger(ValgkortgrunnlagStatistikk.class);

    private DateTime startJobb;
    private long antallVelgereTotalt;
    private long antallVelgereUtenManntallsnummer;
    private Set<String> velgereFiktive = new HashSet<>();
    private Set<String> velgereSomErAvgattFraManntallet = new HashSet<>();
    private Set<String> velgereIkkeGodkjente = new HashSet<>();
    private Set<String> velgereUtenStemmerett = new HashSet<>();
    private Set<String> velgereMedPostnummer0000 = new HashSet<>();
    private Set<String> velgereBosattIUtlandet = new HashSet<>();
    private Set<String> velgereIkkeTilknyttetStemmekrets = new HashSet<>();
    private Set<String> velgereTilknyttetHeleKommunenkretsen = new HashSet<>();
    private Set<String> velgereIkkeTilknyttetValgdistrikt = new HashSet<>();
    private long antallVelgereEksportertTilValgkort;
    private Set<String> velgereMedSpesRegtype3 = new HashSet<>();
    private Set<String> velgereUtenManntallsnummerMenEllersAltIOrden = new HashSet<>();
    
    public ValgkortgrunnlagStatistikk initializeStatistics() {
        startJobb = DateTime.now();
        antallVelgereTotalt = 0;
        antallVelgereUtenManntallsnummer = 0;
        velgereFiktive.clear();
        velgereSomErAvgattFraManntallet.clear();
        velgereIkkeGodkjente.clear();
        velgereUtenStemmerett.clear();
        velgereMedPostnummer0000.clear();
        velgereBosattIUtlandet.clear();
        velgereIkkeTilknyttetStemmekrets.clear();
        velgereTilknyttetHeleKommunenkretsen.clear();
        velgereIkkeTilknyttetValgdistrikt.clear();
        antallVelgereEksportertTilValgkort = 0;
        velgereMedSpesRegtype3.clear();
        velgereUtenManntallsnummerMenEllersAltIOrden.clear();
        return this;
    }

    public void loggStatistikkForGenerering() {
        LOG.info("Generering av valgkortgrunnlag ferdig:");
        LOG.info("- Antall velgere hentet ut fra databasen: " + antallVelgereTotalt);
        LOG.info("- Antall velgere eksportert til valgkort: " + antallVelgereEksportertTilValgkort);
        LOG.info("- Antall velgere uten manntallsnummer: " + antallVelgereUtenManntallsnummer);
        LOG.info("- Utfiltreringer:");
        LOG.info("   - Antall fiktive velgere: " + velgereFiktive.size());
        LOG.info("   - Antall velgere som er avgått fra manntallet: " + velgereSomErAvgattFraManntallet.size());
        LOG.info("   - Antall ikke godkjente velgere : " + velgereIkkeGodkjente.size());
        LOG.info("   - Antall velgere uten stemmerett: " + velgereUtenStemmerett.size());
        LOG.info("   - Antall velgere med postnummer 0000: " + velgereMedPostnummer0000.size());
        LOG.info("   - Antall velgere bosatt i utlandet: " + velgereBosattIUtlandet.size());
        LOG.info("   - Antall velgere ikke tilknyttet stemmekrets: " + velgereIkkeTilknyttetStemmekrets.size());
        LOG.info("   - Antall velgere tilknyttet kretsen for hele kommunen: " + velgereTilknyttetHeleKommunenkretsen.size());
        LOG.info("   - Antall velgere ikke tilknyttet valgdistrikt: " + velgereIkkeTilknyttetValgdistrikt.size());
        LOG.info("   - Antall velgere med spes.reg.type 3: " + velgereMedSpesRegtype3.size());
        LOG.info("   - Antall velgere uten manntallsnummer, men som ellers er helt i orden: " + velgereUtenManntallsnummerMenEllersAltIOrden.size());
    }
}
