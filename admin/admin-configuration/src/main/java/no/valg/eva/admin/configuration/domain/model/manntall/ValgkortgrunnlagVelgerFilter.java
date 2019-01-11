package no.valg.eva.admin.configuration.domain.model.manntall;

import no.valg.eva.admin.configuration.domain.model.Voter;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.format.PeriodFormat;

import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.lang.String.format;

/** 
 * Logikken for å filtrere ut velgere som ikke skal være med i valgkortgrunnlaget.
 *
 * DEV-NOTES:
 * - Tester gjøres gjennom ValgkortgrunnlagDomainService
 * - Merk at denne klassen har ganske mange metoder med sideeffekter. Det kan vurderes å finne en mer elegant løsning
 */
public class ValgkortgrunnlagVelgerFilter {

    private static final Logger LOG = Logger.getLogger(ValgkortgrunnlagVelgerFilter.class);

    private final ValgkortgrunnlagFactory valgkortgrunnlagFactory;
    private final ValgkortgrunnlagStatistikk valgkortgrunnlagStatistikk;

    public ValgkortgrunnlagVelgerFilter(ValgkortgrunnlagFactory valgkortgrunnlagFactory, ValgkortgrunnlagStatistikk valgkortgrunnlagStatistikk) {
        this.valgkortgrunnlagFactory = valgkortgrunnlagFactory;
        this.valgkortgrunnlagStatistikk = valgkortgrunnlagStatistikk;
    }

    public List<Voter> filter(List<Voter> velgere) {
        DateTime startKonvertering = DateTime.now();
        List<Voter> filtrerteVelgere = velgere.stream()
                .filter(this::erIkkeFiktivMedLogging)
                .filter(this::erIkkeAvgaattMedLogging)
                .filter(this::erGodkjentMedLogging)
                .filter(this::harStemmerettMedLogging)
                .filter(this::harIkkeBostedsAdresseOgPostnummerLik0000MedLogging)
                .filter(this::erBosattINorgeMedLogging)
                .filter(this::erTilknyttetStemmekretsMedLogging)
                .filter(this::erIkkeTilknyttetKretsenForHeleKommunenMedLogging)
                .filter(velger -> erTilknyttetValgdistriktMedLogging(valgkortgrunnlagFactory, velger))
                .filter(this::harIkkeSpesRegType3MedLogging)
                .filter(this::harManntallsnummerMedLogging)
                .collect(Collectors.toList());
        LOG.debug(format("Filtrering av velgere ferdig. Filtrering av %d velgere tok %s.", velgere.size(),
                        PeriodFormat.getDefault().print(new Duration(startKonvertering, DateTime.now()).toPeriod())));
        return filtrerteVelgere;
    }

    private boolean erIkkeFiktivMedLogging(Voter velger) {
        return filtrerVelgerMedStatistikkOgLogging(velger, this::erIkkeFiktiv, valgkortgrunnlagStatistikk.getVelgereFiktive(), null, null);
    }

    private boolean erIkkeFiktiv(Voter velger) {
        return !velger.isFictitious();
    }

    private boolean erIkkeAvgaattMedLogging(Voter velger) {
        return filtrerVelgerMedStatistikkOgLogging(velger, this::erIkkeAvgaatt, valgkortgrunnlagStatistikk.getVelgereSomErAvgattFraManntallet(), null, null);
    }

    private boolean erIkkeAvgaatt(Voter velger) {
        return !velger.isNotInElectoralRollAnymore();
    }

    private boolean erGodkjentMedLogging(Voter velger) {
        return filtrerVelgerMedStatistikkOgLogging(velger, Voter::isApproved, valgkortgrunnlagStatistikk.getVelgereIkkeGodkjente(), null, null);
    }

    private boolean harStemmerettMedLogging(Voter velger) {
        return filtrerVelgerMedStatistikkOgLogging(velger, Voter::isEligible, valgkortgrunnlagStatistikk.getVelgereUtenStemmerett(), null, null);
    }

    private boolean harIkkeBostedsAdresseOgPostnummerLik0000MedLogging(Voter velger) {
        return filtrerVelgerMedStatistikkOgLogging(velger, this::harIkkeBostedsAdresseOgPostnummerLik0000, valgkortgrunnlagStatistikk.getVelgereMedPostnummer0000(),
                Logger::info, "Velger %s har bostedsadresse med postnummer 0000. Velgeren får ikke tilsendt valgkort.");
    }

    private boolean harIkkeBostedsAdresseOgPostnummerLik0000(Voter velger) {
        return velger.isMailingAddressSpecified() || !"0000".equals(velger.getPostalCode());
    }

    private boolean erBosattINorgeMedLogging(Voter velger) {
        return filtrerVelgerMedStatistikkOgLogging(velger, Voter::erBosattINorge, valgkortgrunnlagStatistikk.getVelgereBosattIUtlandet(),
                Logger::info, "Velger %s er ikke bosatt i Norge. Velgeren får ikke tilsendt valgkort.");
    }

    private boolean erTilknyttetStemmekretsMedLogging(Voter velger) {
        return filtrerVelgerMedStatistikkOgLogging(velger, Voter::erTilknyttetStemmekrets, valgkortgrunnlagStatistikk.getVelgereIkkeTilknyttetStemmekrets(),
                Logger::error, "Velger %s er ikke tilknyttet en stemmekrets. Velgeren får ikke tilsendt valgkort.");
    }

    private boolean erIkkeTilknyttetKretsenForHeleKommunenMedLogging(Voter velger) {
        return filtrerVelgerMedStatistikkOgLogging(velger, this::erIkkeTilknyttetKretsenForHeleKommunen,
                valgkortgrunnlagStatistikk.getVelgereTilknyttetHeleKommunenkretsen(),
                Logger::info, "Velger %s er tilknyttet stemmekretsen for hele kommunen. Velgeren får ikke tilsendt valgkort.");
    }

    private boolean erIkkeTilknyttetKretsenForHeleKommunen(Voter v) {
        return !v.erTilknyttetKretsenForHeleKommunen();
    }

    private boolean erTilknyttetValgdistriktMedLogging(ValgkortgrunnlagFactory valgkortgrunnlagFactory, Voter velger) {
        return filtrerVelgerMedStatistikkOgLogging(velger, valgkortgrunnlagFactory::erTilknyttetValgdistrikt,
                valgkortgrunnlagStatistikk.getVelgereIkkeTilknyttetValgdistrikt(),
                Logger::warn, "Velger %s er ikke tilknyttet et valgdistrikt. Velgeren får ikke tilsendt valgkort.");
    }

    private boolean harIkkeSpesRegType3MedLogging(Voter velger) {
        return filtrerVelgerMedStatistikkOgLogging(velger, this::harIkkeSpesRegType3, valgkortgrunnlagStatistikk.getVelgereMedSpesRegtype3(),
                Logger::info, "Velger %s har spes.reg.type 3 (utvandret/bosatt på Svalbard/Jan Mayen). Velgeren får ikke tilsendt valgkort.");
    }

    private boolean harIkkeSpesRegType3(Voter velger) {
        return !(new Character('3').equals(velger.getSpesRegType()));
    }

    private boolean harManntallsnummerMedLogging(Voter velger) {
        return filtrerVelgerMedStatistikkOgLogging(velger, this::harManntallsnummer, valgkortgrunnlagStatistikk.getVelgereUtenManntallsnummerMenEllersAltIOrden(),
                Logger::error, "Velger %s har ikke manntallsnummer, men har data forøvrig i orden. Dette tyder på at noe galt har skjedd.");
    }

    private boolean harManntallsnummer(Voter velger) {
        return velger.getNumber() != null;
    }

    private boolean filtrerVelgerMedStatistikkOgLogging(Voter velger, Predicate<Voter> predicate, Set<String> filtrerteVelgere,
                                                        BiConsumer<Logger, Object> logMethod, String melding) {
        if (predicate.test(velger)) {
            return true;
        }
        String velgerId = velger.getId();
        if (!filtrerteVelgere.contains(velgerId)) {
            filtrerteVelgere.add(velgerId);
            if (logMethod != null && melding != null) {
                logMethod.accept(LOG, format(melding, velgerId));
            }
        }
        return false;
    }
    
}
