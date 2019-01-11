package no.valg.eva.admin.voting.service;

import no.evote.constants.EvoteConstants;
import no.evote.dto.ElectionDayPickListItem;
import no.evote.dto.PickListItem;
import no.evote.dto.VotingDto;
import no.evote.exception.EvoteException;
import no.evote.model.views.Eligibility;
import no.evote.security.UserData;
import no.evote.service.configuration.MvAreaServiceBean;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.voting.VotingPhase;
import no.valg.eva.admin.configuration.domain.model.ElectionGroup;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.configuration.domain.model.PollingDistrict;
import no.valg.eva.admin.configuration.domain.model.PollingPlace;
import no.valg.eva.admin.configuration.domain.model.Voter;
import no.valg.eva.admin.configuration.domain.model.VotingCategory;
import no.valg.eva.admin.configuration.repository.EligibilityRepository;
import no.valg.eva.admin.configuration.repository.MvElectionRepository;
import no.valg.eva.admin.configuration.repository.PollingDistrictRepository;
import no.valg.eva.admin.configuration.repository.PollingPlaceRepository;
import no.valg.eva.admin.configuration.repository.VoterRepository;
import no.valg.eva.admin.felles.melding.Alvorlighetsgrad;
import no.valg.eva.admin.voting.domain.model.AvgittStemme;
import no.valg.eva.admin.voting.domain.model.StemmegivningsType;
import no.valg.eva.admin.voting.domain.model.VelgerMelding;
import no.valg.eva.admin.voting.domain.model.VelgerMeldingType;
import no.valg.eva.admin.voting.domain.model.VelgerSomSkalStemme;
import no.valg.eva.admin.voting.domain.model.Voting;
import no.valg.eva.admin.voting.repository.VotingRepository;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import javax.inject.Inject;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static no.valg.eva.admin.common.voting.VotingCategory.FA;
import static no.valg.eva.admin.common.voting.VotingCategory.FI;
import static no.valg.eva.admin.common.voting.VotingCategory.VB;
import static no.valg.eva.admin.common.voting.VotingCategory.VF;
import static no.valg.eva.admin.common.voting.VotingCategory.VO;
import static no.valg.eva.admin.common.voting.VotingCategory.VS;
import static no.valg.eva.admin.felles.melding.Alvorlighetsgrad.ERROR;
import static no.valg.eva.admin.felles.melding.Alvorlighetsgrad.WARN;
import static no.valg.eva.admin.voting.domain.model.VelgerMeldingType.ALLEREDE_GODKJENT_STEMME;
import static no.valg.eva.admin.voting.domain.model.VelgerMeldingType.ALLEREDE_IKKE_GODKJENT_STEMME;
import static no.valg.eva.admin.voting.domain.model.VelgerMeldingType.ALLEREDE_IKKE_GODKJENT_STEMME_FORKASTET;
import static no.valg.eva.admin.voting.domain.model.VelgerMeldingType.FORHANDSTEMME_ANNEN_KOMMUNE;
import static no.valg.eva.admin.voting.domain.model.VelgerMeldingType.FORHANDSTEMME_STENGT_PGA_AVKRYSSNINGSMANNTALL_KJORT;
import static no.valg.eva.admin.voting.domain.model.VelgerMeldingType.INGEN_VALGKRETS_FOR_VELGER;
import static no.valg.eva.admin.voting.domain.model.VelgerMeldingType.STEMMERETT_KUN_VED_KOMMUNEVALG;
import static no.valg.eva.admin.voting.domain.model.VelgerMeldingType.STEMMERETT_VED_SAMETINGSVALG;
import static no.valg.eva.admin.voting.domain.model.VelgerMeldingType.VELGER_AVGANG_I_MANNTALL;
import static no.valg.eva.admin.voting.domain.model.VelgerMeldingType.VELGER_IKKE_AVLAGT_STEMME;
import static no.valg.eva.admin.voting.domain.model.VelgerMeldingType.VELGER_IKKE_MANNTALLSFORT_DENNE_KOMMUNEN;
import static no.valg.eva.admin.voting.domain.model.VelgerMeldingType.VELGER_IKKE_MANNTALLSFORT_DENNE_KRETSEN;
import static no.valg.eva.admin.voting.domain.model.VelgerMeldingType.VELGER_IKKE_STEMMEBERETTIGET;
import static no.valg.eva.admin.voting.domain.model.VelgerMeldingType.VELGER_IKKE_STEMMEBERETTIGET_GRUNNET_ALDER;
import static no.valg.eva.admin.voting.domain.model.VelgerMeldingType.VELGER_KAN_STEMME_I_KONVOLUTT;
import static no.valg.eva.admin.voting.domain.model.VelgerMeldingType.VELGER_KAN_STEMME_I_KONVOLUTT_FORHAND;
import static no.valg.eva.admin.voting.domain.model.VelgerMeldingType.VELGER_KAN_STEMME_I_KONVOLUTT_FORHAND_IKKE_URNE;
import static no.valg.eva.admin.voting.domain.model.VelgerMeldingType.VELGER_KAN_STEMME_I_KONVOLUTT_FORHAND_URNE;
import static org.joda.time.DateTime.now;

public class VotingServiceBean {
    private static final String F0_INCORRECTLY_REGISTERED_VOTE = "F0";
    private static final String V0_INCORRECTLY_REGISTERED_VOTE = "V0";
    private static final int INDEKS_FOR_SENT_INNKOMMENDE = 3;

    private MvAreaServiceBean mvAreaService;
    private PollingDistrictRepository pollingDistrictRepository;
    private VotingRepository votingRepository;
    private PollingPlaceRepository pollingPlaceRepository;
    private VoterRepository voterRepository;
    private EligibilityRepository eligibilityRepository;
    private MvElectionRepository mvElectionRepository;

    @Inject
    public VotingServiceBean(MvAreaServiceBean mvAreaService,
            PollingDistrictRepository pollingDistrictRepository,
            PollingPlaceRepository pollingPlaceRepository,
            VotingRepository votingRepository,
            VoterRepository voterRepository,
            EligibilityRepository eligibilityRepository,
            MvElectionRepository mvElectionRepository) {
        this.mvAreaService = mvAreaService;
        this.pollingDistrictRepository = pollingDistrictRepository;
        this.pollingPlaceRepository = pollingPlaceRepository;
        this.votingRepository = votingRepository;
        this.voterRepository = voterRepository;
        this.eligibilityRepository = eligibilityRepository;
        this.mvElectionRepository = mvElectionRepository;
    }

    public List<VotingDto> findVotingStatistics(
            Long pollingPlacePk, long municipalityPk, Long electionGroupPk, LocalDate startDate, LocalDate endDate, int votingNumberStart, int votingNumberEnd,
            boolean includeLateValidation, String[] votingCategories, boolean includeLateAdvanceVotings) {
        List<Object[]> votingStatisticsResponse = votingRepository.findVotingStatistics(pollingPlacePk, municipalityPk, electionGroupPk, startDate, endDate,
                votingNumberStart, votingNumberEnd, includeLateValidation, votingCategories, includeLateAdvanceVotings);
        List<VotingDto> votingStatisticsDTOList = new ArrayList<>();
        for (Object object : votingStatisticsResponse) {
            votingStatisticsDTOList.add(getVotingStatisticsDTO(object));
        }
        return votingStatisticsDTOList;
    }

    public List<PickListItem> findAdvanceVotingPickList(
            Long pollingPlacePk, long municipalityPk, Long electionGroupPk, LocalDate startDate, LocalDate endDate, int votingNumberStart,
            int votingNumberEnd) {
        List<Object[]> advanceVotingPickListResponse = votingRepository.findAdvanceVotingPickList(pollingPlacePk, municipalityPk, electionGroupPk, startDate,
                endDate, votingNumberStart, votingNumberEnd);
        List<PickListItem> pickListDTOList = new ArrayList<>();
        for (Object object : advanceVotingPickListResponse) {
            pickListDTOList.add(new PickListItem(object));
        }
        return pickListDTOList;
    }

    public List<PickListItem> findElectionDayVotingPickList(
            long municipalityPk, Long electionGroupPk, int votingNumberStart, int votingNumberEnd, String... votingCats) {
        List<Object[]> electionDayVotingPickListResponse = votingRepository.findElectionDayVotingPickList(municipalityPk, electionGroupPk, votingNumberStart,
                votingNumberEnd, votingCats);
        List<PickListItem> pickListDTOList = new ArrayList<>();
        for (Object object : electionDayVotingPickListResponse) {
            pickListDTOList.add(new ElectionDayPickListItem(object));
        }
        return pickListDTOList;
    }

    public int updateAdvanceVotingsApproved(
            Long pollingPlacePk, long municipalityPk, Long electionGroupPk, LocalDate startDate, LocalDate endDate,
            int votingNumberStart, int votingNumberEnd) {
        return votingRepository.updateAdvanceVotingsApproved(pollingPlacePk, municipalityPk, electionGroupPk, startDate, endDate, votingNumberStart,
                votingNumberEnd);
    }

    public int updateElectionDayVotingsApproved(
            long municipalityPk, Long electionGroupPk, int votingNumberStart, int votingNumberEnd, String... votingCats) {
        return votingRepository.updateElectionDayVotingsApproved(municipalityPk, electionGroupPk, votingNumberStart, votingNumberEnd, votingCats);
    }

    private VotingDto getVotingStatisticsDTO(Object object) {
        Object[] objectList = (Object[]) object;
        VotingDto votingStatisticsDTO = new VotingDto();
        // if late validation
        if ((Boolean) objectList[INDEKS_FOR_SENT_INNKOMMENDE]) {
            votingStatisticsDTO.setVotingCategoryId(EvoteConstants.VOTING_CATEGORY_LATE);
            votingStatisticsDTO.setApproved((Boolean) objectList[1]);
            votingStatisticsDTO.setNumberOfVotings((BigInteger) objectList[2]);
        } else {
            votingStatisticsDTO.setVotingCategoryId((String) objectList[0]);
            votingStatisticsDTO.setApproved((Boolean) objectList[1]);
            votingStatisticsDTO.setNumberOfVotings((BigInteger) objectList[2]);
        }

        return votingStatisticsDTO;
    }

    public Voting markOffVoterAdvance(UserData userData,
                                      PollingPlace pollingPlace,
                                      ElectionGroup electionGroup,
                                      Voter voter,
                                      boolean isVoterInLoggedInMunicipality,
                                      String selectedVotingCategoryId,
                                      String ballotBoxId,
                                      VotingPhase votingPhase) {

        validateVoterMvArea(voter);

        Voting voting = Voting.builder()
                .pollingPlace(pollingPlace)
                .electionGroup(electionGroup)
                .castTimestamp(now())
                .voter(voter)
                .phase(votingPhase)
                .mvArea(voter.getMvArea())
                .build();

        if (isVoterInLoggedInMunicipality || voter.isFictitious()) {
            voting.setVotingCategory(votingRepository.findVotingCategoryById(selectedVotingCategoryId));
            voting.setReceivedTimestamp(now());
            voting.setBallotBoxId(ballotBoxId);
        } else {
            voting.setVotingCategory(votingRepository.findVotingCategoryById(FA.getId()));
            voting.setReceivedTimestamp(null);
        }
        if (VotingPhase.LATE == votingPhase) {
            voting.setLateValidation(true);
        }
        voting.setApproved(false);
        voting = votingRepository.create(userData, voting);

        return votingRepository.findByPk(userData, voting.getPk()); // Fetch object again because of function set_voting_number() in database
    }

    public Voting registerVoteCentrally(UserData userData,
                                        ElectionGroup electionGroup,
                                        Voter voter,
                                        String selectedVotingCategoryId,
                                        MvArea currentMvArea, VotingPhase votingPhase) {

        PollingDistrict municipalityDistrict = pollingDistrictRepository.findMunicipalityProxy(currentMvArea.getMunicipality().getPk());
        PollingPlace envelopePollingPlace = pollingPlaceRepository.findFirstByPollingDistrictPkAndAdvanceVoteInBallotBox(municipalityDistrict.getPk(), false);

        if (envelopePollingPlace == null) {
            throw new EvoteException("No envelope polling place in municipality district");
        }

        // Hvis fremmedstemme, sett område til der velger hører til, ellers bruk stemmekrets for første stemmested i kommunen
        MvArea mvArea = (selectedVotingCategoryId.equals(VF.getId())) ? voter.getMvArea() : mvAreaService
                .findByPollingDistrict(municipalityDistrict.getPk());

        assertMvAreaWhenCreatingVoting(mvArea);

        DateTime now = now();
        Voting voting = Voting.builder()
                .electionGroup(electionGroup)
                .castTimestamp(now)
                .receivedTimestamp(now)
                .voter(voter)
                .votingCategory(votingRepository.findVotingCategoryById(selectedVotingCategoryId))
                .approved(false)
                .pollingPlace(envelopePollingPlace)
                .mvArea(mvArea)
                .phase(votingPhase)
                .build();
        
        voting = votingRepository.create(userData, voting);
        return votingRepository.findByPk(userData, voting.getPk()); // Fetch object again because of function set_voting_number() in database
    }

    public Voting markOffVoter(UserData userData, PollingPlace pollingPlace, ElectionGroup electionGroup, Voter voter, boolean fremmedstemme, VotingPhase votingPhase) {
        validateVoterMvArea(voter);

        DateTime now = now();
        Voting voting = Voting.builder()
                .pollingPlace(pollingPlace)
                .electionGroup(electionGroup)
                .castTimestamp(now)
                .receivedTimestamp(now)
                .voter(voter)
                .approved(true)
                .validationTimestamp(now)
                .votingCategory(votingRepository.findVotingCategoryById(fremmedstemme ? VF.getId() : VO.getId()))
                .mvArea(voter.getMvArea())
                .phase(votingPhase)
                .build();

        voting = votingRepository.create(userData, voting);
        return votingRepository.findByPk(userData, voting.getPk());
    }

    public Voting markOffVoterAdvanceVoteInBallotBox(UserData userData, PollingPlace pollingPlace, ElectionGroup electionGroup, Voter voter, boolean isVoterInLoggedInMunicipality, VotingPhase votingPhase) {

        validateVoterMvArea(voter);

        DateTime now = now();
        Voting voting = Voting.builder()
                .pollingPlace(pollingPlace)
                .electionGroup(electionGroup)
                .castTimestamp(now)
                .receivedTimestamp(now)
                .voter(voter)
                .mvArea(voter.getMvArea())
                .phase(votingPhase)
                .build();

        if (isVoterInLoggedInMunicipality || voter.isFictitious()) {
            voting.setVotingCategory(votingRepository.findVotingCategoryById(FI.getId()));
            voting.setApproved(true);
            voting.setValidationTimestamp(now);
        } else {
            voting.setReceivedTimestamp(null);
            voting.setVotingCategory(votingRepository.findVotingCategoryById(FA.getId()));
            voting.setApproved(false);
        }

        voting = votingRepository.create(userData, voting);

        return votingRepository.findByPk(userData, voting.getPk());
    }

    public Voting updateAdvanceVotingApproved(UserData userData, Voting votingForApproval) {
        votingForApproval.setApproved(true);
        validateVoterMvArea(votingForApproval.getVoter());
        votingForApproval.setValidationTimestamp(now());
        return votingRepository.update(userData, votingForApproval);
    }

    private void validateVoterMvArea(Voter voter) {
        assertMvAreaWhenCreatingVoting(voter.getMvArea());
    }

    private void assertMvAreaWhenCreatingVoting(MvArea mvArea) {
        if (mvArea == null) {
            throw new EvoteException("Missing MvArea when creating Voting");
        }
    }

    public VelgerSomSkalStemme hentVelgerSomSkalStemme(StemmegivningsType stemmegivningsType, ElectionPath valggruppeSti, AreaPath stemmestedSti,
                                                       Voter velger) {

        valggruppeSti.assertElectionGroupLevel();

        velger = voterRepository.findByPk(velger.getPk());
        MvArea stemmested = mvAreaService.findSingleByPath(stemmestedSti);
        VelgerSomSkalStemme resultat = initVelgerMeldinger(stemmegivningsType, stemmested, velger);

        if (sjekkForhåndsstemmerStengtPgaAvkryssingsmanntallKjort(stemmegivningsType, stemmested.getMunicipality(), velger, resultat)) {
            return resultat;
        }

        if (velger.isFictitious()) {
            resultat.setKanRegistrereStemmegivning(true);
            return resultat;
        }

        sjekkStemmerettSameting(velger, resultat);

        MvElection valggruppe = mvElectionRepository.finnEnkeltMedSti(valggruppeSti.tilValghierarkiSti());
        List<Eligibility> teoretiskKvalifiserteValgdistrikt = getTeoretiskKvalifiserteValgdistrikt(valggruppe, velger);

        if (stemmegivningsType.isForhand() && !isVelgerSammeKommune(stemmested.getMunicipality(), velger)) {
            handterVelgerAnnenKommuneForForhand(velger, resultat);

            // The two lines below were added to solve EVAADMIN-1571, but is not a fully good solution. A better solution should be made.
            sjekkStemmerettKunVedKommunevalg(teoretiskKvalifiserteValgdistrikt, velger, resultat);
            return resultat;
        }

        if (sjekkAvgangFraManntall(velger, resultat)) {
            return resultat;
        }

        if (sjekkVelgerIkkeStemmerettGrunnetAlder(teoretiskKvalifiserteValgdistrikt, stemmegivningsType, stemmested, velger, resultat)) {
            resultat.setKanRegistrereStemmegivning(resultat.isKanRegistrereStemmegivning() || stemmegivningsType.isValgtingSentralt());
            return resultat;
        }

        sjekkStemmerettKunVedKommunevalg(teoretiskKvalifiserteValgdistrikt, velger, resultat);

        if (sjekkTidligereStemmegivninger(stemmegivningsType, stemmested, valggruppe, velger, resultat)) {
            resultat.setKanRegistrereStemmegivning(resultat.isKanRegistrereStemmegivning() || stemmegivningsType.isValgtingSentralt());
            return resultat;
        }

        if (sjekkStemmeberettiget(stemmegivningsType, stemmested, velger, resultat)) {
            return resultat;
        }

        if (stemmegivningsType.isValgtingOrdinaere() && !isVelgerSammeKommune(stemmested.getMunicipality(), velger)) {
            leggTilMeldingMedTillegg(VELGER_IKKE_MANNTALLSFORT_DENNE_KOMMUNEN, VELGER_KAN_STEMME_I_KONVOLUTT, ERROR, resultat);
            resultat.setKanRegistrereStemmegivning(resultat.isKanRegistrereStemmegivning() || stemmegivningsType.isValgtingSentralt());
            return resultat;
        }

        resultat.setKanRegistrereStemmegivning(true);
        resultat.getVelgerMeldinger().add(new VelgerMelding(VELGER_IKKE_AVLAGT_STEMME));

        return resultat;
    }

    private void leggTilMeldingMedTillegg(VelgerMeldingType melding, VelgerMeldingType tillegg, Alvorlighetsgrad alvorlighetsgrad,
                                          VelgerSomSkalStemme velgerSomSkalStemme) {
        VelgerMelding m = new VelgerMelding(melding, alvorlighetsgrad);
        m.setTilleggsMelding(new VelgerMelding(tillegg, alvorlighetsgrad));
        velgerSomSkalStemme.getVelgerMeldinger().add(m);
    }

    private boolean sjekkForhåndsstemmerStengtPgaAvkryssingsmanntallKjort(
            StemmegivningsType stemmegivningsType, Municipality kommune, Voter velger, VelgerSomSkalStemme velgerSomSkalStemme) {
        if (stemmegivningsType.isForhandIkkeSentInnkomne() && isVelgerSammeKommune(kommune, velger) && kommune.isAvkrysningsmanntallKjort()) {
            velgerSomSkalStemme.getVelgerMeldinger().add(new VelgerMelding(FORHANDSTEMME_STENGT_PGA_AVKRYSSNINGSMANNTALL_KJORT, WARN));
            return true;
        }
        return false;
    }

    private void sjekkStemmerettSameting(Voter velger, VelgerSomSkalStemme velgerSomSkalStemme) {
        if (velger.isStemmerettOgsaVedSametingsvalg()) {
            velgerSomSkalStemme.getVelgerMeldinger().add(new VelgerMelding(STEMMERETT_VED_SAMETINGSVALG));
        }
    }

    private void handterVelgerAnnenKommuneForForhand(Voter velger, VelgerSomSkalStemme velgerSomSkalStemme) {
        velgerSomSkalStemme.getVelgerMeldinger().add(new VelgerMelding(FORHANDSTEMME_ANNEN_KOMMUNE, ERROR));
        if (sjekkVelgerOmrade(velger, velgerSomSkalStemme)) {
            velgerSomSkalStemme.setKanRegistrereStemmegivning(true);
        }
    }

    private boolean sjekkVelgerOmrade(Voter velger, VelgerSomSkalStemme velgerSomSkalStemme) {
        if (velger.getMvArea() == null) {
            // There should not exist any voters with mvArea == null
            velgerSomSkalStemme.getVelgerMeldinger().add(new VelgerMelding(INGEN_VALGKRETS_FOR_VELGER, ERROR));
            velgerSomSkalStemme.setKanRegistrereStemmegivning(false);
            return false;
        }
        return true;
    }

    private void sjekkStemmerettKunVedKommunevalg(List<Eligibility> teoretiskKvalifiserteValgdistrikt, Voter velger, VelgerSomSkalStemme velgerSomSkalStemme) {
        for (Eligibility valgdistrikt : teoretiskKvalifiserteValgdistrikt) {
            if (valgdistrikt.getEndDateOfBirth().isBefore(velger.getDateOfBirth())) {
                velgerSomSkalStemme.getVelgerMeldinger().add(new VelgerMelding(STEMMERETT_KUN_VED_KOMMUNEVALG, WARN));
                return;
            }
        }
    }

    private boolean sjekkStemmeberettiget(StemmegivningsType stemmegivningsType, MvArea stemmested, Voter velger, VelgerSomSkalStemme velgerSomSkalStemme) {
        if (velger.isApproved()) {
            return false;
        }

        velgerSomSkalStemme.setKanRegistrereStemmegivning(false);

        if (stemmegivningsType.isValgting()) {
            leggTilMeldingMedTillegg(VELGER_IKKE_STEMMEBERETTIGET, VELGER_KAN_STEMME_I_KONVOLUTT, ERROR, velgerSomSkalStemme);
        } else if (stemmegivningsType.isForhand()) {
            if (isForhandsstemmerRettIUrne(stemmested)) {
                velgerSomSkalStemme.getVelgerMeldinger().add(new VelgerMelding(VELGER_IKKE_STEMMEBERETTIGET, WARN));
            } else {
                leggTilMeldingMedTillegg(VELGER_IKKE_STEMMEBERETTIGET, VELGER_KAN_STEMME_I_KONVOLUTT_FORHAND, ERROR, velgerSomSkalStemme);
            }
        }
        return true;
    }

    private boolean sjekkTidligereStemmegivninger(
            StemmegivningsType stemmegivningsType, MvArea stemmested, MvElection valggruppe, Voter velger, VelgerSomSkalStemme velgerSomSkalStemme) {

        List<Voting> tidligereStemmegivninger = votingRepository.getReceivedVotingsByElectionGroupAndVoter(velger.getPk(),
                valggruppe.getElectionGroup().getPk());

        // Filtrer vekk forkastede stemmer til annen kommune
        tidligereStemmegivninger = tidligereStemmegivninger
                .stream()
                .filter(stemmegivning -> !isForkastetAnnenKommune(velger, stemmegivning))
                .collect(Collectors.toList());

        if (tidligereStemmegivninger.isEmpty()) {
            return false;
        }

        List<Voting> ikkeGodkjente = tidligereStemmegivninger.stream().filter(voting -> !voting.isApproved())
                .collect(Collectors.toList());

        if (!ikkeGodkjente.isEmpty()) {
            List<VelgerMelding> meldinger = new ArrayList<>();
            ikkeGodkjente.stream().filter(voting -> !isFeilregistrert(voting)).forEach(voting -> {
                if (voting.getVotingRejection() != null) {
                    meldinger.add(new VelgerMelding(ALLEREDE_IKKE_GODKJENT_STEMME_FORKASTET, ERROR, avgittStemme(voting)));
                } else {
                    meldinger.add(new VelgerMelding(ALLEREDE_IKKE_GODKJENT_STEMME, ERROR, avgittStemme(voting)));
                }
            });
            if (meldinger.isEmpty()) {
                // Kun feilregistrerte
                velgerSomSkalStemme.setKanRegistrereStemmegivning(true);
            } else {
                velgerSomSkalStemme.getVelgerMeldinger().addAll(meldinger);
                if (isRettIUrneStemmegivning(stemmegivningsType, stemmested)) {
                    velgerSomSkalStemme.getVelgerMeldinger().add(new VelgerMelding(VELGER_KAN_STEMME_I_KONVOLUTT, ERROR));
                    velgerSomSkalStemme.setKanRegistrereStemmegivning(false);
                } else {
                    velgerSomSkalStemme.setKanRegistrereStemmegivning(true);
                }
            }
        }

        List<Voting> godkjente = tidligereStemmegivninger.stream().filter(Voting::isApproved).collect(Collectors.toList());

        if (!godkjente.isEmpty()) {
            for (Voting voting : godkjente) {
                velgerSomSkalStemme.getVelgerMeldinger().add(new VelgerMelding(ALLEREDE_GODKJENT_STEMME, ERROR, avgittStemme(voting)));
            }
            if (stemmegivningsType.isValgtingOrdinaere()) {
                velgerSomSkalStemme.getVelgerMeldinger().add(new VelgerMelding(VELGER_KAN_STEMME_I_KONVOLUTT, ERROR));
                velgerSomSkalStemme.setKanRegistrereStemmegivning(false);
            } else if (stemmegivningsType.isForhand()) {
                if (isForhandsstemmerRettIUrne(stemmested)) {
                    velgerSomSkalStemme.getVelgerMeldinger().add(new VelgerMelding(VELGER_KAN_STEMME_I_KONVOLUTT_FORHAND_URNE, ERROR));
                    velgerSomSkalStemme.setKanRegistrereStemmegivning(false);
                } else {
                    velgerSomSkalStemme.getVelgerMeldinger().add(new VelgerMelding(VELGER_KAN_STEMME_I_KONVOLUTT_FORHAND_IKKE_URNE, ERROR));
                    velgerSomSkalStemme.setKanRegistrereStemmegivning(true);
                }
            }
        }

        boolean kunIkkeGodkjente = !ikkeGodkjente.isEmpty() && godkjente.isEmpty();
        if (stemmegivningsType.isValgtingOrdinaere()) {
            if (!isVelgerSammeKommune(stemmested.getMunicipality(), velger)) {
                Alvorlighetsgrad alvorlighetsgrad;
                if (kunIkkeGodkjente) {
                    velgerSomSkalStemme.setKanRegistrereStemmegivning(false);
                    alvorlighetsgrad = ERROR;
                } else {
                    alvorlighetsgrad = WARN;
                }
                leggTilMeldingMedTillegg(VELGER_IKKE_MANNTALLSFORT_DENNE_KOMMUNEN, VELGER_KAN_STEMME_I_KONVOLUTT, alvorlighetsgrad, velgerSomSkalStemme);
                return true;
            }
            if (isVelgerSammeKommuneMenIkkeSammeStemmekrets(stemmested, velger) && !stemmested.getMunicipality().isElectronicMarkoffs()) {
                Alvorlighetsgrad alvorlighetsgrad;
                if (kunIkkeGodkjente) {
                    velgerSomSkalStemme.setKanRegistrereStemmegivning(false);
                    alvorlighetsgrad = ERROR;
                } else {
                    alvorlighetsgrad = WARN;
                }
                leggTilMeldingMedTillegg(VELGER_IKKE_MANNTALLSFORT_DENNE_KRETSEN, VELGER_KAN_STEMME_I_KONVOLUTT, alvorlighetsgrad, velgerSomSkalStemme);
                return true;
            }
        }
        if (stemmegivningsType.isForhand() && !isVelgerSammeKommune(stemmested.getMunicipality(), velger)) {
            velgerSomSkalStemme.getVelgerMeldinger().add(new VelgerMelding(FORHANDSTEMME_ANNEN_KOMMUNE, WARN));
        }
        return true;
    }

    private boolean isForkastetAnnenKommune(Voter velger, Voting stemmegivning) {
        if (velger.getMvArea() == null || stemmegivning.getMvArea() == null || stemmegivning.getVotingRejection() == null) {
            return false;
        }
        return !velger.getMvArea().areaPath().equals(stemmegivning.getMvArea().areaPath());
    }

    private boolean isRettIUrneStemmegivning(StemmegivningsType stemmegivningsType, MvArea stemmested) {
        return stemmegivningsType.isValgtingOrdinaere() || (stemmegivningsType.isForhand() && isForhandsstemmerRettIUrne(stemmested));
    }

    private boolean isVelgerSammeKommuneMenIkkeSammeStemmekrets(MvArea stemmested, Voter velger) {
        return isVelgerSammeKommune(stemmested.getMunicipality(), velger) && !isVelgerISammeStemmekrets(stemmested, velger);
    }

    private boolean sjekkAvgangFraManntall(Voter velVoter, VelgerSomSkalStemme velgerSomSkalStemme) {
        if (velVoter.getEndringstype() != null && velVoter.getEndringstype().equals('A')) {
            velgerSomSkalStemme.getVelgerMeldinger().add(new VelgerMelding(VELGER_AVGANG_I_MANNTALL, ERROR));
            return true;
        }
        return false;
    }

    private boolean sjekkVelgerIkkeStemmerettGrunnetAlder(
            List<Eligibility> teoretiskKvalifiserteValgdistrikt,
            StemmegivningsType stemmegivningsType,
            MvArea stemmested,
            Voter velger,
            VelgerSomSkalStemme velgerSomSkalStemme) {

        for (Eligibility eligibility : teoretiskKvalifiserteValgdistrikt) {
            if (velger.getDateOfBirth() != null && eligibility.getEndDateOfBirth().isAfter(velger.getDateOfBirth())) {
                // Gyldig
                return false;
            }
        }
        if (sjekkVelgerOmrade(velger, velgerSomSkalStemme)) {
            velgerSomSkalStemme.getVelgerMeldinger().add(new VelgerMelding(VELGER_IKKE_STEMMEBERETTIGET_GRUNNET_ALDER, ERROR));
            boolean kanRegStemme = stemmegivningsType.isForhand()
                    && !isVelgerSammeKommune(stemmested.getMunicipality(), velger)
                    && !isForhandsstemmerRettIUrne(stemmested);
            velgerSomSkalStemme.setKanRegistrereStemmegivning(kanRegStemme);
        }
        return true;
    }

    private VelgerSomSkalStemme initVelgerMeldinger(StemmegivningsType stemmegivningsType, MvArea stemmested, Voter velger) {
        List<VotingCategory> liste = new ArrayList<>();
        if (stemmegivningsType.isForhand() && isVelgerSammeKommune(stemmested.getMunicipality(), velger)) {
            liste.addAll(votingRepository.findAdvanceVotingCategories());
        } else if (stemmegivningsType.isValgtingSentralt()) {
            if (stemmested.getMunicipality().isElectronicMarkoffs()) {
                liste.add(votingRepository.findVotingCategoryById(VB.getId()));
                liste.add(votingRepository.findVotingCategoryById(VS.getId()));
            } else {
                if (isVelgerISammeKommuneMenIkkeSammeDistrikt(stemmested, velger)) {
                    liste.add(votingRepository.findVotingCategoryById(VF.getId()));
                }
                liste.add(votingRepository.findVotingCategoryById(VS.getId()));
            }
        }
        return new VelgerSomSkalStemme(liste);
    }

    private List<Eligibility> getTeoretiskKvalifiserteValgdistrikt(MvElection valggruppe, Voter velger) {
        return eligibilityRepository.findTheoreticalEligibilityForVoterInGroup(velger, valggruppe.getElectionGroup().getPk());
    }

    private AvgittStemme avgittStemme(Voting stemmegivning) {
        return new AvgittStemme(no.valg.eva.admin.common.voting.VotingCategory.fromId(stemmegivning.getVotingCategory().getId()),
                stemmegivning.getCastTimestamp());
    }

    private boolean isFeilregistrert(Voting stemmegivning) {
        return isStemmegivningForkastet(stemmegivning) && (stemmegivning.getVotingRejection().getId().equals(F0_INCORRECTLY_REGISTERED_VOTE)
                || stemmegivning.getVotingRejection().getId().equals(V0_INCORRECTLY_REGISTERED_VOTE));
    }

    private boolean isStemmegivningForkastet(Voting stemmegivning) {
        return stemmegivning.getVotingRejection() != null;
    }

    private boolean isVelgerSammeKommune(Municipality kommune, Voter velger) {
        return kommune.getId().equals(velger.getMunicipalityId());
    }

    private boolean isVelgerISammeStemmekrets(MvArea stemmested, Voter velger) {
        return velger.getPollingDistrictId().equals(stemmested.getPollingDistrictId());
    }

    private boolean isVelgerISammeKommuneMenIkkeSammeDistrikt(MvArea stemmested, Voter velger) {
        return isVelgerSammeKommune(stemmested.getMunicipality(), velger) && !isVelgerISammeStemmekrets(stemmested, velger);
    }

    private boolean isForhandsstemmerRettIUrne(MvArea stemmested) {
        return stemmested.getPollingPlace() != null && stemmested.getPollingPlace().isAdvanceVoteInBallotBox();
    }
}
