package no.valg.eva.admin.counting.domain.service;

import no.evote.exception.EvoteException;
import no.evote.security.UserData;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.counting.model.AntallStemmesedlerLagtTilSideForValg;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.ElectionGroup;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.configuration.repository.MvAreaRepository;
import no.valg.eva.admin.configuration.repository.MvElectionRepository;
import no.valg.eva.admin.counting.domain.model.AntallStemmesedlerLagtTilSide;
import no.valg.eva.admin.counting.domain.model.ContestReport;
import no.valg.eva.admin.counting.domain.model.VoteCount;
import no.valg.eva.admin.counting.repository.AntallStemmesedlerLagtTilSideRepository;
import no.valg.eva.admin.counting.repository.ContestReportRepository;
import no.valg.eva.admin.felles.sti.valggeografi.KommuneSti;
import no.valg.eva.admin.felles.sti.valghierarki.ValggruppeSti;

import javax.inject.Inject;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static no.evote.constants.AreaLevelEnum.BOROUGH;
import static no.evote.constants.ElectionLevelEnum.CONTEST;
import static no.evote.constants.ElectionLevelEnum.ELECTION_GROUP;
import static no.valg.eva.admin.common.counting.model.CountCategory.FO;
import static no.valg.eva.admin.common.counting.model.CountCategory.FS;

public class AntallStemmesedlerLagtTilSideDomainService {
    private AntallStemmesedlerLagtTilSideRepository repository;
    private ContestReportRepository contestReportRepository;
    private MvElectionRepository mvElectionRepository;
    private MvAreaRepository mvAreaRepository;

    @Inject
    public AntallStemmesedlerLagtTilSideDomainService(AntallStemmesedlerLagtTilSideRepository repository, ContestReportRepository contestReportRepository,
                                                      MvElectionRepository mvElectionRepository, MvAreaRepository mvAreaRepository) {
        this.repository = repository;
        this.contestReportRepository = contestReportRepository;
        this.mvElectionRepository = mvElectionRepository;
        this.mvAreaRepository = mvAreaRepository;
    }

    public void lagreAntallStemmesedlerLagtTilSide(
            UserData userData, Municipality municipality, no.valg.eva.admin.common.counting.model.AntallStemmesedlerLagtTilSide antallStemmesedlerLagtTilSide) {
        sjekkAtBrukerErValghendelsesadminEllerErForAngittKommune(userData, municipality);
        List<AntallStemmesedlerLagtTilSideForValg> antallStemmesedlerLagtTilSideForValgList =
                antallStemmesedlerLagtTilSide.getAntallStemmesedlerLagtTilSideForValgList();
        for (AntallStemmesedlerLagtTilSideForValg antallStemmesedlerLagtTilSideForValg : antallStemmesedlerLagtTilSideForValgList) {
            ElectionPath electionPath = antallStemmesedlerLagtTilSideForValg.getElectionPath();
            electionPath.assertLevels(ELECTION_GROUP, CONTEST);
            MvElection mvElection = mvElectionRepository.finnEnkeltMedSti(electionPath.tilValghierarkiSti());
            if (electionPath.getLevel() == CONTEST) {
                Contest contest = mvElection.getContest();
                lagreAntallStemmesedlerLagtTilSide(userData, municipality, contest, antallStemmesedlerLagtTilSideForValg.getAntallStemmesedler());
            } else {
                ElectionGroup electionGroup = mvElection.getElectionGroup();
                lagreAntallStemmesedlerLagtTilSide(userData, municipality, electionGroup, antallStemmesedlerLagtTilSideForValg.getAntallStemmesedler());
            }
        }
    }

    private void lagreAntallStemmesedlerLagtTilSide(UserData userData, Municipality municipality, ElectionGroup electionGroup, int antallStemmesedler) {
        sjekkAtDetIkkeFinnesNoenTellingerForFoEllerFs(electionGroup, municipality);
        opprettEllerOppdaterAntallStemmesedlerLagtTilSide(userData, municipality, electionGroup, null, antallStemmesedler);
    }

    private void lagreAntallStemmesedlerLagtTilSide(UserData userData, Municipality municipality, Contest contest, int antallStemmesedler) {
        sjekkAtValgdistriktErForAngittKommune(contest, municipality);
        sjekkAtValgdistriktErForBydel(contest);
        sjekkAtDetIkkeFinnesNoenTellingerForFoEllerFs(contest, municipality);
        opprettEllerOppdaterAntallStemmesedlerLagtTilSide(userData, municipality, contest.getElection().getElectionGroup(), contest, antallStemmesedler);
    }

    private void opprettEllerOppdaterAntallStemmesedlerLagtTilSide(
            UserData userData, Municipality municipality, ElectionGroup electionGroup, Contest contest, int antallStemmesedler) {
        AntallStemmesedlerLagtTilSide antallStemmesedlerLagtTilSide = hentAntallStemmesedlerLagtTilSide(municipality, electionGroup, contest);
        if (antallStemmesedlerLagtTilSide != null) {
            antallStemmesedlerLagtTilSide.setAntallStemmesedler(antallStemmesedler);
        } else {
            repository.create(userData, new AntallStemmesedlerLagtTilSide(municipality, electionGroup, contest, antallStemmesedler));
        }
    }

    private AntallStemmesedlerLagtTilSide hentAntallStemmesedlerLagtTilSide(Municipality municipality, ElectionGroup electionGroup, Contest contest) {
        if (contest == null) {
            return repository.findByMunicipalityAndElectionGroup(municipality, electionGroup);
        } else {
            return repository.findByMunicipalityAndContest(municipality, contest);
        }
    }

    public boolean isAntallStemmesedlerLagtTilSideLagret(ValggruppeSti valggruppeSti, KommuneSti kommuneSti) {
        MvElection mvElection = mvElectionRepository.finnEnkeltMedSti(valggruppeSti);
        MvArea mvArea = mvAreaRepository.finnEnkeltMedSti(kommuneSti);
        return repository.isAntallStemmerLagtTilSide(mvArea.getMunicipality(), mvElection.getElectionGroup());
    }

    public boolean isAntallStemmesedlerLagtTilSideLagret(MvElection mvElection, Municipality municipality) {
        return repository.isAntallStemmerLagtTilSide(municipality, mvElection.getElectionGroup());
    }

    private void sjekkAtBrukerErValghendelsesadminEllerErForAngittKommune(UserData userData, Municipality municipality) {
        if (!userData.isElectionEventAdminUser()) {
            userData.sjekkAtBrukerTilhørerKommune(municipality.areaPath());
        }
    }

    private void sjekkAtValgdistriktErForAngittKommune(Contest contest, Municipality municipality) {
        Municipality contestMunicipality = contest.getFirstContestArea().getMvArea().getMunicipality();
        if (!contestMunicipality.equals(municipality)) {
            throw new IllegalArgumentException(format("forventet at valgdistriktet <%s> skal tilhøre kommunen <%s>", contest.getName(), municipality.getName()));
        }
    }

    private void sjekkAtValgdistriktErForBydel(Contest contest) {
        if (!contest.isOnBoroughLevel()) {
            throw new IllegalArgumentException(format("forventet at valgdistriktet <%s> skal være for et bydelsvalg", contest.getName()));
        }
    }

    private void sjekkAtDetIkkeFinnesNoenTellingerForFoEllerFs(Contest contest, Municipality municipality) {
        List<ContestReport> contestReports = contestReportRepository.findByContestAndMunicipality(contest, municipality);
        sjekkAtDetIkkeFinnesNoenTellingerForFoEllerFs(contestReports);
    }

    private void sjekkAtDetIkkeFinnesNoenTellingerForFoEllerFs(ElectionGroup electionGroup, Municipality municipality) {
        List<ContestReport> contestReports = contestReportRepository.findByElectionGroupAndMunicipality(electionGroup, municipality);
        sjekkAtDetIkkeFinnesNoenTellingerForFoEllerFs(contestReports);
    }

    private void sjekkAtDetIkkeFinnesNoenTellingerForFoEllerFs(List<ContestReport> contestReports) {
        if (contestReports.isEmpty()) {
            return;
        }
        if (voteCounts(contestReports).noneMatch(this::isCountCategoryFoOrFs)) {
            return;
        }
        throw new EvoteException("antall stemmesedler lagt til side kan ikke lagres siden det eksisterer tellinger for forhåndsstemmer");
    }

    private Stream<VoteCount> voteCounts(List<ContestReport> contestReports) {
        return contestReports.stream().map(ContestReport::getVoteCountSet).flatMap(Collection::stream);
    }

    private boolean isCountCategoryFoOrFs(VoteCount voteCount) {
        // Sonar: Java 8-metodereferanse brukt ovenfor
        return voteCount.getCountCategory() == FO || voteCount.getCountCategory() == FS;
    }

    public no.valg.eva.admin.common.counting.model.AntallStemmesedlerLagtTilSide hentAntallStemmesedlerLagtTilSide(Municipality municipality) {
        ElectionPath electionEventPath = ElectionPath.from(municipality.electionEventId());
        ElectionGroup electionGroup = mvElectionRepository.findByPathAndLevel(electionEventPath, ELECTION_GROUP).get(0).getElectionGroup();
        List<Contest> valgdistrikterForBydelsvalg = valgdistrikterForBydelsvalg(electionEventPath);
        boolean lagringAvAntallStemmesedlerLagtTilSideMulig = isLagringAvAntallStemmesedlerLagtTilSideMulig(electionGroup, municipality);

        boolean isBoroughElectionForMunicipality = contestIsInMunicipality(municipality, valgdistrikterForBydelsvalg);
        if (isBoroughElectionForMunicipality) {
            return antallStemmesedlerLagtTilSideForBydelsvalg(
                    municipality, valgdistrikterForBydelsvalg, lagringAvAntallStemmesedlerLagtTilSideMulig);
        }

        return antallStemmesedlerLagtTilSideForAndreValg(municipality, electionGroup, lagringAvAntallStemmesedlerLagtTilSideMulig);
    }

    private List<Contest> valgdistrikterForBydelsvalg(ElectionPath electionEventPath) {
        return mvElectionRepository
                .findByPathAndLevelAndAreaLevel(electionEventPath, CONTEST, BOROUGH)
                .stream()
                .map(MvElection::getContest)
                .collect(toList());
    }

    private boolean isLagringAvAntallStemmesedlerLagtTilSideMulig(ElectionGroup electionGroup, Municipality municipality) {
        List<ContestReport> contestReports = contestReportRepository.findByElectionGroupAndMunicipality(electionGroup, municipality);
        return contestReports.isEmpty() || voteCounts(contestReports).noneMatch(this::isCountCategoryFoOrFs);
    }

    private boolean contestIsInMunicipality(Municipality municipality, List<Contest> contestList) {
        return contestList.stream()
                .anyMatch(currentContest -> currentContest.isInMunicipality(municipality));
    }

    private no.valg.eva.admin.common.counting.model.AntallStemmesedlerLagtTilSide antallStemmesedlerLagtTilSideForBydelsvalg(
            Municipality municipality, List<Contest> valgdistrikterForBydelsvalg, boolean lagringAvAntallStemmesedlerLagtTilSideMulig) {
        AreaPath municipalityPath = municipality.areaPath();
        List<AntallStemmesedlerLagtTilSideForValg> antallStemmesedlerLagtTilSideForValgList =
                valgdistrikterForBydelsvalg
                        .stream()
                        .map(contest -> antallStemmesedlerLagtTilSideForBydelsvalg(municipality, contest))
                        .collect(toList());
        return new no.valg.eva.admin.common.counting.model.AntallStemmesedlerLagtTilSide(
                municipalityPath, antallStemmesedlerLagtTilSideForValgList, lagringAvAntallStemmesedlerLagtTilSideMulig);
    }

    private AntallStemmesedlerLagtTilSideForValg antallStemmesedlerLagtTilSideForBydelsvalg(Municipality municipality, Contest contest) {
        ElectionPath contestPath = contest.electionPath();
        String navn = "Bydel " + contest.getName();
        AntallStemmesedlerLagtTilSide antallStemmesedlerLagtTilSide = repository.findByMunicipalityAndContest(municipality, contest);
        if (antallStemmesedlerLagtTilSide != null) {
            return new AntallStemmesedlerLagtTilSideForValg(contestPath, navn, antallStemmesedlerLagtTilSide.getAntallStemmesedler());
        }
        return new AntallStemmesedlerLagtTilSideForValg(contestPath, navn, 0);
    }

    private no.valg.eva.admin.common.counting.model.AntallStemmesedlerLagtTilSide antallStemmesedlerLagtTilSideForAndreValg(
            Municipality municipality, ElectionGroup electionGroup, boolean lagringAvAntallStemmesedlerLagtTilSideMulig) {
        AreaPath municipalityPath = municipality.areaPath();
        AntallStemmesedlerLagtTilSide antallStemmesedlerLagtTilSide = repository.findByMunicipalityAndElectionGroup(municipality, electionGroup);
        AntallStemmesedlerLagtTilSideForValg antallStemmesedlerLagtTilSideForValg = antallStemmesedlerLagtTilSideForValg(electionGroup, antallStemmesedlerLagtTilSide);

        return new no.valg.eva.admin.common.counting.model.AntallStemmesedlerLagtTilSide(
                municipalityPath, antallStemmesedlerLagtTilSideForValg, lagringAvAntallStemmesedlerLagtTilSideMulig);
    }

    private AntallStemmesedlerLagtTilSideForValg antallStemmesedlerLagtTilSideForValg(
            ElectionGroup electionGroup, AntallStemmesedlerLagtTilSide antallStemmesedlerLagtTilSide) {
        ElectionPath electionGroupPath = electionGroup.electionPath();
        String electionGroupName = electionGroup.getName();
        if (antallStemmesedlerLagtTilSide != null) {
            return new AntallStemmesedlerLagtTilSideForValg(electionGroupPath, electionGroupName, antallStemmesedlerLagtTilSide.getAntallStemmesedler());
        }
        return new AntallStemmesedlerLagtTilSideForValg(electionGroupPath, electionGroupName, 0);
    }
}
