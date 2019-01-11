package no.valg.eva.admin.configuration.repository;

import no.valg.eva.admin.backend.configuration.repository.ElectionEventRepository;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.PollingDistrict;
import no.valg.eva.admin.configuration.domain.model.Voter;
import no.valg.eva.admin.test.AbstractJpaTestBase;
import no.valg.eva.admin.test.TestGroups;
import org.joda.time.LocalDate;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.Assert.fail;

@Test(groups = {TestGroups.REPOSITORY})
public class VoterRepositoryManntallsnumreSideOgLinjeGenereringTest extends AbstractJpaTestBase {

    private static final String VALGHENDELSE_ID = "200701";
    private static final String KRETS_STI = "200701.47.01.0101.010100.0000";

    private VoterRepository voterRepository;
    private MvAreaRepository mvAreaRepository;
    private ElectionEvent electionEvent;

    @BeforeMethod(alwaysRun = true)
    public void init() {
        ElectionEventRepository electionEventRepository = new ElectionEventRepository(getEntityManager());
        electionEvent = electionEventRepository.findById(VALGHENDELSE_ID);
        voterRepository = new VoterRepository(getEntityManager());
        mvAreaRepository = new MvAreaRepository(getEntityManager());
    }

    @Test
    public void genererManntallsnumre_godkjenteVelgereMedStemmerettFaarManntallsnummer_deAndreVelgerneFaarIkkeManntallsnummer() {
        List<Voter> velgereFoerGenerering = voterRepository.findVotersByElectionEvent(electionEvent.getPk());
        long antallVelgereFoerGenereringMedManntallsnumre = velgereFoerGenerering.stream().filter(Voter::harManntallsnummer).count();
        long antallVelgereFoerGenereringUtenManntallsnumre = velgereFoerGenerering.size() - antallVelgereFoerGenereringMedManntallsnumre;

        voterRepository.genererManntallsnumre(electionEvent.getPk());

        slettCache();
        List<Voter> velgereEtterGenerering = voterRepository.findVotersByElectionEvent(electionEvent.getPk());
        long antallVelgereEtterGenereringMedManntallsnumre = velgereEtterGenerering.stream().filter(Voter::harManntallsnummer).count();
        long antallVelgereEtterGenereringUtenManntallsnumre = velgereEtterGenerering.size() - antallVelgereEtterGenereringMedManntallsnumre;

        for (Voter velger : velgereEtterGenerering) {
            if (!velger.harManntallsnummer()) {
                assertThat(skalKunneStemmeI2007valget(velger)).isFalse();
            }
        }

        assertThat(velgereFoerGenerering.size()).isEqualTo(velgereEtterGenerering.size());
        assertThat(antallVelgereFoerGenereringMedManntallsnumre).isLessThan(antallVelgereEtterGenereringMedManntallsnumre);
        assertThat(antallVelgereFoerGenereringUtenManntallsnumre).isGreaterThan(antallVelgereEtterGenereringUtenManntallsnumre);
    }

    private void slettCache() {
        getEntityManager().clear();
    }

    private boolean skalKunneStemmeI2007valget(Voter velger) {
        return !velger.isFictitious()
                && velger.isEligible()
                && velger.isApproved()
                && velger.getDateOfBirth().isBefore(new LocalDate(1993, 12, 30));
    }

    @Test
    public void genererManntallsnumre_velgereMedManntallsnummerFaarOgsaaSideOgLinje() {
        List<Voter> velgereFoerGenerering = voterRepository.findVotersByElectionEvent(electionEvent.getPk());
        long antallVelgereFoerGenereringMedSideOgLinje = velgereFoerGenerering.stream().filter(Voter::harSideOgLinje).count();
        long antallVelgereFoerGenereringUtenSideOgLinje = velgereFoerGenerering.size() - antallVelgereFoerGenereringMedSideOgLinje;

        voterRepository.genererManntallsnumre(electionEvent.getPk());

        slettCache();
        List<Voter> velgereEtterGenerering = voterRepository.findVotersByElectionEvent(electionEvent.getPk());
        long antallVelgereEtterGenereringMedSideOgLinje = velgereEtterGenerering.stream().filter(Voter::harSideOgLinje).count();
        long antallVelgereEtterGenereringUtenSideOgLinje = velgereEtterGenerering.size() - antallVelgereEtterGenereringMedSideOgLinje;

        for (Voter velger : velgereEtterGenerering) {
            if (!velger.harSideOgLinje()) {
                assertThat(skalKunneStemmeI2007valget(velger)).isFalse();
            }
        }

        assertThat(velgereFoerGenerering.size()).isEqualTo(velgereEtterGenerering.size());
        assertThat(antallVelgereFoerGenereringMedSideOgLinje).isLessThan(antallVelgereEtterGenereringMedSideOgLinje);
        assertThat(antallVelgereFoerGenereringUtenSideOgLinje).isGreaterThan(antallVelgereEtterGenereringUtenSideOgLinje);

        sjekkSideOgLinjeForKrets(KRETS_STI);
    }

    private void sjekkSideOgLinjeForKrets(String stiTilKrets) {
        PollingDistrict krets = mvAreaRepository.findSingleByPath(new AreaPath(stiTilKrets)).getPollingDistrict();
        List<Voter> velgereIEnKrets = voterRepository.getElectoralRollForPollingDistrict(krets);
        int gjeldendeSide = 1;
        int gjeldendeLinje = 1;
        for (Voter velger : velgereIEnKrets) {
            if (velger.getElectoralRollPage() == gjeldendeSide && velger.getElectoralRollLine() == gjeldendeLinje) {
                gjeldendeLinje++;
            } else if (velger.getElectoralRollPage() == gjeldendeSide + 1) {
                assertThat(velger.getElectoralRollLine()).isEqualTo(1);
                gjeldendeSide++;
                gjeldendeLinje = 2;
            } else {
                fail("Velger " + velger.getId() + " har ikke forventet side og linje ("
                        + velger.getElectoralRollPage() + ", " + velger.getElectoralRollLine() + ")");
            }
        }
    }
}
