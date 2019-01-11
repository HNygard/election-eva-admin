package no.valg.eva.admin.configuration.domain.service;

import no.evote.constants.CountingHierarchy;
import no.evote.constants.VotingHierarchy;
import no.evote.exception.EvoteException;
import no.evote.security.UserData;
import no.evote.service.LocaleTextServiceBean;
import no.evote.service.TranslationServiceBean;
import no.evote.service.configuration.CountyServiceBean;
import no.evote.service.rbac.RoleServiceBean;
import no.valg.eva.admin.backend.application.schedule.RefreshResourceBundlesEvent;
import no.valg.eva.admin.backend.common.repository.LocaleTextRepository;
import no.valg.eva.admin.backend.configuration.repository.ElectionEventRepository;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.configuration.model.election.ElectionDay;
import no.valg.eva.admin.common.configuration.status.ElectionEventStatusEnum;
import no.valg.eva.admin.configuration.application.ElectionMapper;
import no.valg.eva.admin.configuration.domain.model.Affiliation;
import no.valg.eva.admin.configuration.domain.model.Ballot;
import no.valg.eva.admin.configuration.domain.model.Candidate;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.Election;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.ElectionEventLocale;
import no.valg.eva.admin.configuration.domain.model.ElectionGroup;
import no.valg.eva.admin.configuration.domain.model.ElectionVoteCountCategory;
import no.valg.eva.admin.configuration.domain.model.Locale;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.OpeningHours;
import no.valg.eva.admin.configuration.domain.model.Party;
import no.valg.eva.admin.configuration.domain.model.Proposer;
import no.valg.eva.admin.configuration.domain.model.ReportCountCategory;
import no.valg.eva.admin.configuration.repository.AffiliationRepository;
import no.valg.eva.admin.configuration.repository.BallotRepository;
import no.valg.eva.admin.configuration.repository.CandidateRepository;
import no.valg.eva.admin.configuration.repository.ContestRepository;
import no.valg.eva.admin.configuration.repository.ElectionGroupRepository;
import no.valg.eva.admin.configuration.repository.ElectionRepository;
import no.valg.eva.admin.configuration.repository.ElectionVoteCountCategoryRepository;
import no.valg.eva.admin.configuration.repository.MunicipalityRepository;
import no.valg.eva.admin.configuration.repository.MvAreaRepository;
import no.valg.eva.admin.configuration.repository.MvElectionRepository;
import no.valg.eva.admin.configuration.repository.ProposerRepository;
import no.valg.eva.admin.configuration.repository.ReportCountCategoryRepository;
import no.valg.eva.admin.configuration.repository.party.PartyRepository;
import no.valg.eva.admin.felles.sti.valghierarki.ValghendelseSti;
import no.valg.eva.admin.rbac.domain.model.Access;
import no.valg.eva.admin.rbac.domain.model.Operator;
import no.valg.eva.admin.rbac.domain.model.OperatorRole;
import no.valg.eva.admin.rbac.domain.model.Role;
import no.valg.eva.admin.rbac.repository.AccessRepository;
import no.valg.eva.admin.rbac.repository.OperatorRepository;
import no.valg.eva.admin.rbac.repository.OperatorRoleRepository;
import no.valg.eva.admin.rbac.repository.RoleRepository;
import org.apache.log4j.Logger;

import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.FlushModeType;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toList;
import static no.evote.constants.EvoteConstants.ELECTION_EVENT_ADMIN;
import static no.evote.constants.EvoteConstants.SCHEDULED_IMPORT_OPERATOR_ID;
import static no.evote.constants.EvoteConstants.SCHEDULED_IMPORT_ROLE;
import static no.evote.security.SecurityLevel.TWO_FACTOR_OF_WHICH_ONE_DYNAMIC;
import static no.valg.eva.admin.common.ElectionPath.ROOT_ELECTION_EVENT_ID;
import static no.valg.eva.admin.common.rbac.Accesses.Manntall_Import;
import static no.valg.eva.admin.configuration.application.ElectionDayMapper.toDomainModel;
import static no.valg.eva.admin.util.TidtakingUtil.taTiden;

/**
 */
public class ElectionEventDomainService {

    private static final Logger LOGGER = Logger.getLogger(ElectionEventDomainService.class);

    @Inject
    private ElectionEventRepository electionEventRepository;
    @Inject
    private OperatorRepository operatorRepository;
    @Inject
    private MvAreaRepository mvAreaRepository;
    @Inject
    private MvElectionRepository mvElectionRepository;
    @Inject
    private OperatorRoleRepository operatorRoleRepository;
    @Inject
    private ElectionGroupRepository electionGroupRepository;
    @Inject
    private ReportCountCategoryRepository reportCountCategoryRepository;
    @Inject
    private MunicipalityRepository municipalityRepository;
    @Inject
    private ElectionRepository electionRepository;
    @Inject
    private ContestRepository contestRepository;
    @Inject
    private BallotRepository ballotRepository;
    @Inject
    private AffiliationRepository affiliationRepository;
    @Inject
    private CandidateRepository candidateRepository;
    @Inject
    private ProposerRepository proposerRepository;
    @Inject
    private ElectionVoteCountCategoryRepository electionVoteCountCategoryRepository;
    @Inject
    private LocaleTextServiceBean localeTextService;
    @Inject
    private TranslationServiceBean translationService;
    @Inject
    private LocaleTextRepository localeTextRepository;
    @Inject
    private PartyRepository partyRepository;
    @Inject
    private Event<RefreshResourceBundlesEvent> refreshResourceBundlesEvent;
    @Inject
    private CountyServiceBean countyService;
    @Inject
    private ElectionMapper electionMapper;
    @Inject
    private RoleRepository roleRepository;
    @Inject
    private AccessRepository accessRepository;
    @Inject
    private RoleServiceBean roleService;
    @PersistenceContext(unitName = "evotePU")
    private EntityManager em;

    public ElectionEvent create(UserData userData, ElectionEvent electionEventTo, boolean copyRoles, VotingHierarchy votingHierarchy,
                                CountingHierarchy countingHierarchy, ElectionEvent fromElectionEvent, Set<Locale> localeSet) {

        // Ytelsesfiks
        FlushModeType oldFlushMode = em.getFlushMode();
        em.setFlushMode(FlushModeType.COMMIT);

        try {
            ElectionEvent toElectionEvent = electionEventRepository.create(userData, electionEventTo);
            createElectionEventLocale(userData, toElectionEvent, localeSet);
            createRoles(toElectionEvent, fromElectionEvent, copyRoles);
            Role adminRole = getRole(toElectionEvent, ELECTION_EVENT_ADMIN);

            // The following users are required for basic functionality
            createAdminOperator(userData, toElectionEvent, adminRole);
            createScheduledImportOperator(userData, toElectionEvent);

            LOGGER.info("creates new election event votingHierarchy-nivaa: " + votingHierarchy.getLevel() + " countingHierarchy-nivaa: "
                    + countingHierarchy.getLevel());

            if (fromElectionEvent != null) {
                taTiden(LOGGER, "Kopiering av valghendelse",
                        () -> kopierDataTilNyValghendelse(userData, votingHierarchy, countingHierarchy, fromElectionEvent, toElectionEvent));
            }

            refreshResourceBundlesEvent.fire(new RefreshResourceBundlesEvent());
            return electionEventTo;
        } catch (RuntimeException e) {
            LOGGER.error("Kunne ikke kopiere valghendelse", e);
            throw e;
        } finally {
            em.setFlushMode(oldFlushMode);
        }
    }

    private void kopierDataTilNyValghendelse(UserData userData, VotingHierarchy votingHierarchy, CountingHierarchy countingHierarchy,
                                             ElectionEvent fromElectionEvent, ElectionEvent toElectionEvent) {
        kopierPartinumre(fromElectionEvent, toElectionEvent);
        kopierOmraader(votingHierarchy, countingHierarchy, fromElectionEvent, toElectionEvent);
        kopierValghierarkiet(votingHierarchy, countingHierarchy, fromElectionEvent, toElectionEvent);
        kopierManntall(votingHierarchy, fromElectionEvent, toElectionEvent);
        kopierStemmegivninger(votingHierarchy, fromElectionEvent, toElectionEvent);
        kopierOpptellingskategorier(userData, countingHierarchy, fromElectionEvent, toElectionEvent);
        kopierListeforslag(userData, countingHierarchy, fromElectionEvent, toElectionEvent);
        kopierStyrer(countingHierarchy, fromElectionEvent, toElectionEvent);
        kopierTellinger(countingHierarchy, fromElectionEvent, toElectionEvent);
    }

    private void kopierPartinumre(ElectionEvent fromElectionEvent, ElectionEvent toElectionEvent) {
        electionEventRepository.copyPartyNumberConfiguration(fromElectionEvent, toElectionEvent);
    }

    private void kopierOmraader(VotingHierarchy votingHierarchy, CountingHierarchy countingHierarchy, ElectionEvent fromElectionEvent, ElectionEvent toElectionEvent) {
        if (votingHierarchy.getLevel() > VotingHierarchy.NONE.getLevel() || countingHierarchy.getLevel() > CountingHierarchy.NONE.getLevel()) {
            LOGGER.debug("copy areas");
            electionEventRepository.copyAreas(fromElectionEvent, toElectionEvent);
        }
    }

    private void kopierValghierarkiet(VotingHierarchy votingHierarchy,
                                      CountingHierarchy countingHierarchy,
                                      ElectionEvent fromElectionEvent,
                                      ElectionEvent toElectionEvent) {
        if (votingHierarchy.getLevel() > VotingHierarchy.AREA_HIERARCHY.getLevel()
                || countingHierarchy.getLevel() > CountingHierarchy.AREA_HIERARCHY.getLevel()) {
            LOGGER.debug("copy elections");
            electionEventRepository.copyElections(fromElectionEvent, toElectionEvent);
        }
    }

    private void kopierManntall(VotingHierarchy votingHierarchy, ElectionEvent fromElectionEvent, ElectionEvent toElectionEvent) {
        if (votingHierarchy.getLevel() > VotingHierarchy.ELECTION_HIERARCHY.getLevel()) {
            LOGGER.debug("copy electoralRoll");
            electionEventRepository.copyElectoralRoll(fromElectionEvent, toElectionEvent);
        }
    }

    private void kopierStemmegivninger(VotingHierarchy votingHierarchy, ElectionEvent fromElectionEvent, ElectionEvent toElectionEvent) {
        if (votingHierarchy.getLevel() > VotingHierarchy.ELECTORAL_ROLL.getLevel()) {
            LOGGER.debug("copy votings");
            copyVoting(fromElectionEvent, toElectionEvent);
        }
    }

    private void kopierOpptellingskategorier(UserData userData, CountingHierarchy countingHierarchy, ElectionEvent fromElectionEvent, ElectionEvent toElectionEvent) {
        if (countingHierarchy.getLevel() > CountingHierarchy.ELECTION_HIERARCHY.getLevel() && countingHierarchy != CountingHierarchy.LIST_PROPOSAL) {
            LOGGER.debug("copy electionReportCountCategory");
            copyElectionReportCountCategory(userData, fromElectionEvent, toElectionEvent);
        }

        if (countingHierarchy.getLevel() > CountingHierarchy.LIST_PROPOSAL.getLevel()) {
            LOGGER.debug("copy reportCountCategories");
            copyReportCountCategories(userData, fromElectionEvent, toElectionEvent);
        }
    }

    private void kopierListeforslag(UserData userData, CountingHierarchy countingHierarchy, ElectionEvent fromElectionEvent, ElectionEvent toElectionEvent) {
        if (countingHierarchy.getLevel() > CountingHierarchy.ELECTION_HIERARCHY.getLevel()
                && countingHierarchy != CountingHierarchy.CENTRAL_CONFIGURATION) {
            LOGGER.debug("copy proposerLists");
            copyProposerLists(userData, fromElectionEvent, toElectionEvent);
        }
    }

    private void kopierStyrer(CountingHierarchy countingHierarchy, ElectionEvent fromElectionEvent, ElectionEvent toElectionEvent) {
        if (countingHierarchy.getLevel() > CountingHierarchy.LOCAL_CONFIGURATION.getLevel()) {
            LOGGER.debug("copy reportingUnits");
            electionEventRepository.copyReportingUnits(fromElectionEvent, toElectionEvent);
        }
    }

    private void kopierTellinger(CountingHierarchy countingHierarchy, ElectionEvent fromElectionEvent, ElectionEvent toElectionEvent) {
        if (countingHierarchy.getLevel() > CountingHierarchy.REPORTING_UNITS.getLevel()) {
            LOGGER.debug("copy manual votings");
            electionEventRepository.copyManualVoting(fromElectionEvent, toElectionEvent);
            LOGGER.debug("copy counts");
            electionEventRepository.copyContestReports(fromElectionEvent, toElectionEvent);
            electionEventRepository.copyVoteCounts(fromElectionEvent, toElectionEvent);
            electionEventRepository.copyBallotCounts(fromElectionEvent, toElectionEvent);
            electionEventRepository.copyModifiedBallots(fromElectionEvent, toElectionEvent);
            electionEventRepository.copyCandidateVotes(fromElectionEvent, toElectionEvent);
        }
    }

    private void createElectionEventLocale(final UserData userData, final ElectionEvent electionEvent, final Set<Locale> localeSet) {
        if (localeSet != null && !localeSet.isEmpty()) {
            for (Locale locale : localeSet) {
                ElectionEventLocale electionEventLocale = new ElectionEventLocale();
                electionEventLocale.setElectionEvent(electionEvent);
                electionEventLocale.setLocale(locale);
                electionEventRepository.createElectionEventLocale(userData, electionEventLocale);
            }
        }
    }

    private void createRoles(ElectionEvent toElectionEvent, ElectionEvent fromElectionEvent, boolean copyRoles) {
        if (fromElectionEvent == null) {
            LOGGER.debug("copy valghendelse_admin role");
            ElectionEvent rootEvent = electionEventRepository.findById(ROOT_ELECTION_EVENT_ID);
            electionEventRepository.copyRoles(rootEvent, toElectionEvent, ELECTION_EVENT_ADMIN);
        } else {
            if (copyRoles) {
                LOGGER.debug("copy roles");
                electionEventRepository.copyRoles(fromElectionEvent, toElectionEvent);
            } else {
                LOGGER.debug("copy valghendelse_admin role");
                electionEventRepository.copyRoles(fromElectionEvent, toElectionEvent, ELECTION_EVENT_ADMIN);
            }
        }
    }

    private Role getRole(ElectionEvent electionEvent, String roleId) {
        return roleRepository.findByElectionEventAndId(electionEvent, roleId);
    }

    private void createAdminOperator(UserData userData, ElectionEvent currentElectionEvent, Role adminRole) {

        Operator currentOperator = userData.getOperatorRole().getOperator();
        Operator adminOperator = new Operator();

        adminOperator.setPk(null);
        adminOperator.setElectionEvent(currentElectionEvent);
        adminOperator.setAddressLine1(currentOperator.getAddressLine1());
        adminOperator.setAddressLine2(currentOperator.getAddressLine2());
        adminOperator.setAddressLine3(currentOperator.getAddressLine3());
        adminOperator.setEmail(currentOperator.getEmail());
        adminOperator.setFirstName(currentOperator.getFirstName());
        adminOperator.setId(currentOperator.getId());
        adminOperator.setInfoText(currentOperator.getInfoText());
        adminOperator.setLastName(currentOperator.getLastName());
        adminOperator.setMiddleName(currentOperator.getMiddleName());
        adminOperator.setNameLine(currentOperator.getNameLine());
        adminOperator.setPostalCode(currentOperator.getPostalCode());
        adminOperator.setPostTown(currentOperator.getPostTown());
        adminOperator.setTelephoneNumber(currentOperator.getTelephoneNumber());
        adminOperator.setActive(true);
        adminOperator = operatorRepository.create(userData, adminOperator);

        OperatorRole or = new OperatorRole();
        or.setOperator(adminOperator);
        or.setRole(adminRole);
        or.setMvArea(mvAreaRepository.findSingleByPath(AreaPath.from(currentElectionEvent.getId())));
        or.setMvElection(mvElectionRepository.finnEnkeltMedSti(new ValghendelseSti(currentElectionEvent.getId())));
        operatorRoleRepository.create(userData, or);
    }

    private void createScheduledImportOperator(UserData userData, ElectionEvent currentElectionEvent) {
        Set<Access> accesses = new HashSet<>();
        accesses.add(accessRepository.findAccessByPath(Manntall_Import.paths()[0]));

        Role electoralRollRole = getRole(currentElectionEvent, SCHEDULED_IMPORT_ROLE);
        if (electoralRollRole == null) {
            electoralRollRole = new Role();
            electoralRollRole.setName("Scheduled Electoral Roll Import");
            electoralRollRole.setId(SCHEDULED_IMPORT_ROLE);
            electoralRollRole.setSecurityLevel(TWO_FACTOR_OF_WHICH_ONE_DYNAMIC.getLevel());
            electoralRollRole.setMutuallyExclusive(false);
            electoralRollRole.setAccesses(accesses);
            electoralRollRole.setElectionEvent(currentElectionEvent);
            electoralRollRole.setActive(true);
            electoralRollRole = roleService.create(userData, electoralRollRole, true);
        }

        Operator electoralRollOperator = new Operator();
        electoralRollOperator.setPk(null);
        electoralRollOperator.setElectionEvent(currentElectionEvent);
        electoralRollOperator.setFirstName("Scheduled");
        electoralRollOperator.setId(SCHEDULED_IMPORT_OPERATOR_ID);
        electoralRollOperator.setLastName("Electoral Roll Import");
        electoralRollOperator.setNameLine("Scheduled Electoral Roll Import");
        electoralRollOperator.setActive(true);
        electoralRollOperator = operatorRepository.create(userData, electoralRollOperator);

        OperatorRole or = new OperatorRole();
        or.setOperator(electoralRollOperator);
        or.setRole(electoralRollRole);
        or.setMvArea(mvAreaRepository.findSingleByPath(AreaPath.from(currentElectionEvent.getId())));
        or.setMvElection(mvElectionRepository.finnEnkeltMedSti(new ValghendelseSti(currentElectionEvent.getId())));
        operatorRoleRepository.create(userData, or);
    }

    private void copyVoting(ElectionEvent fromElectionEvent, ElectionEvent toElectionEvent) {
        List<ElectionGroup> electionGroupFromList = electionGroupRepository.getElectionGroupsSorted(fromElectionEvent.getPk());
        List<ElectionGroup> electionGroupToList = electionGroupRepository.getElectionGroupsSorted(toElectionEvent.getPk());

        for (ElectionGroup electionGroupFrom : electionGroupFromList) {
            ElectionGroup electionGroupTo = findCorrespondingElectionGroup(electionGroupFrom.getId(), electionGroupToList);
            electionEventRepository.copyVotings(electionGroupFrom, electionGroupTo, toElectionEvent);
        }
    }

    void copyElectionReportCountCategory(final UserData userData, final ElectionEvent fromElectionEvent, final ElectionEvent toElectionEvent) {

        List<ElectionGroup> electionGroups = electionGroupRepository.getElectionGroupsSorted(fromElectionEvent.getPk());

        for (ElectionGroup fromElectionGroup : electionGroups) {
            List<ElectionVoteCountCategory> detachedElectionVoteCountCategoryList = electionVoteCountCategoryRepository
                    .findElectionVoteCountCategories(fromElectionGroup, true);
            ElectionGroup toElectionGroup = electionGroupRepository.findElectionGroupById(toElectionEvent.getPk(), fromElectionGroup.getId());

            for (ElectionVoteCountCategory detachedElectionVoteCountCategory : detachedElectionVoteCountCategoryList) {
                detachedElectionVoteCountCategory.setPk(null);
                detachedElectionVoteCountCategory.setElectionGroup(toElectionGroup);
            }

            electionVoteCountCategoryRepository.update(userData, detachedElectionVoteCountCategoryList);
        }
    }

    private ElectionGroup findCorrespondingElectionGroup(final String electionGroupFromId, final List<ElectionGroup> electionGroupToList) {
        for (ElectionGroup electionGroup : electionGroupToList) {
            if (electionGroup.getId().equals(electionGroupFromId)) {
                return electionGroup;
            }
        }
        throw new EvoteException("No electionGroup having id: " + electionGroupFromId + " + found for new election event");
    }

    void copyReportCountCategories(final UserData userData, final ElectionEvent fromElectionEvent, final ElectionEvent toElectionEvent) {
        List<ReportCountCategory> detachedAllReportCountCategoriesForElectionEvent = reportCountCategoryRepository
                .findAllReportCountCategoriesForElectionEvent(fromElectionEvent.getPk(), true);

        for (ReportCountCategory detachedReportCountCategory : detachedAllReportCountCategoriesForElectionEvent) {
            detachedReportCountCategory.setPk(null);
            ElectionGroup toElectionGroup = electionGroupRepository.findElectionGroupById(toElectionEvent.getPk(), detachedReportCountCategory
                    .getElectionGroup().getId());
            detachedReportCountCategory.setElectionGroup(toElectionGroup);

            String countryId = detachedReportCountCategory.getMunicipality().getCounty().getCountry().getId();
            String countyId = detachedReportCountCategory.getMunicipality().getCounty().getId();
            String municipalityId = detachedReportCountCategory.getMunicipality().getId();
            Municipality municipalityTo = municipalityRepository.findUniqueMunicipalityByElectionEvent(toElectionEvent.getPk(), countryId, countyId,
                    municipalityId);
            detachedReportCountCategory.setMunicipality(municipalityTo);

            reportCountCategoryRepository.create(userData, detachedReportCountCategory);
        }
    }

    void copyProposerLists(final UserData userData, final ElectionEvent fromElectionEvent, final ElectionEvent toElectionEvent) {
        List<ElectionGroup> fromElectionGroups = electionGroupRepository.getElectionGroupsSorted(fromElectionEvent.getPk());
        for (ElectionGroup fromElectionGroup : fromElectionGroups) {
            kopierListeforslagForValggruppe(userData, toElectionEvent, fromElectionGroup);
        }
    }

    private void kopierListeforslagForValggruppe(UserData userData, ElectionEvent toElectionEvent, ElectionGroup fromElectionGroup) {
        ElectionGroup toElectionGroup = electionGroupRepository.findElectionGroupById(toElectionEvent.getPk(), fromElectionGroup.getId());
        List<Election> toElections = electionRepository.findElectionsByElectionGroup(toElectionGroup.getPk());
        for (Election toElection : toElections) {
            kopierListeforslagForValg(userData, toElectionEvent, fromElectionGroup, toElection);
        }
    }

    private void kopierListeforslagForValg(UserData userData, ElectionEvent toElectionEvent, ElectionGroup fromElectionGroup, Election toElection) {
        LOGGER.debug("kopierer listeforslag for valg " + toElection.toString());
        List<Contest> toContests = contestRepository.findByElectionPk(toElection.getPk());
        Election fromElection = electionRepository.findElectionByElectionGroupAndId(fromElectionGroup.getPk(), toElection.getId());
        if (toContests != null) {
            for (Contest toContest : toContests) {
                kopierListeforslagForValgdistrikt(userData, toElectionEvent, fromElection, toContest);
            }
        }
    }

    private void kopierListeforslagForValgdistrikt(UserData userData, ElectionEvent toElectionEvent, Election fromElection, Contest toContest) {
        LOGGER.debug("kopierer listeforslag for valgdistrikt: " + toContest.toString());
        Contest fromContest = contestRepository.findContestById(fromElection.getPk(), toContest.getId());
        List<Ballot> fromBallots = ballotRepository.findByContest(fromContest.getPk());
        if (fromBallots != null) {
            for (Ballot fromBallot : fromBallots) {
                kopierListeforslagIValgdistrikt(userData, toElectionEvent, toContest, fromBallot);
            }
        }
    }

    private void kopierListeforslagIValgdistrikt(UserData userData, ElectionEvent toElectionEvent, Contest toContest, Ballot fromBallot) {
        LOGGER.debug("kopierer listeforslag:" + fromBallot.toString());
        // ballots with ID blank are already copied
        if (!"blank".equalsIgnoreCase(fromBallot.getId().trim())) {
            Ballot toBallot = new Ballot(fromBallot);
            toBallot.setContest(toContest);
            ballotRepository.createBallot(userData, toBallot);
            LOGGER.debug("skal kopiere kandidater");
            createCandidates(userData, toElectionEvent, fromBallot, toBallot);
            LOGGER.debug("skal kopiere proposerers");
            createProposers(userData, fromBallot, toBallot);
        }
    }

    private void createCandidates(final UserData userData, final ElectionEvent toElectionEvent, final Ballot fromBallot, final Ballot toBallot) {
        Affiliation fromAffiliation = affiliationRepository.findByBallot(fromBallot.getPk());
        if (fromAffiliation != null) {
            Affiliation toAffiliation = new Affiliation(fromAffiliation);
            toAffiliation.setBallot(toBallot);
            Party party = partyRepository.findPartyByIdAndEvent(fromAffiliation.getParty().getId(), toElectionEvent.getPk());
            toAffiliation.setParty(party);
            affiliationRepository.createAffiliation(userData, toAffiliation);
            List<Candidate> fromCandidates = candidateRepository.findByAffiliation(fromAffiliation.getPk());
            if (fromCandidates != null) {
                for (Candidate fromCandidate : fromCandidates) {
                    LOGGER.debug("kopierer candidate " + fromCandidate.toString());
                    Candidate toCandidate = new Candidate(fromCandidate);
                    toCandidate.setBallot(toBallot);
                    toCandidate.setAffiliation(toAffiliation);
                    candidateRepository.createCandidate(userData, toCandidate);
                }
            }
        }
    }

    private void createProposers(final UserData userData, final Ballot fromBallot, final Ballot toBallot) {
        List<Proposer> fromProposers = proposerRepository.findByBallot(fromBallot.getPk());
        if (fromProposers != null) {
            for (Proposer fromProposer : fromProposers) {
                LOGGER.debug("kopierer proposer " + fromProposer.toString());
                Proposer toProposer = new Proposer(fromProposer);
                toProposer.setBallot(toBallot);
                proposerRepository.createProposer(userData, toProposer);
            }
        }
    }

    public ElectionEvent update(final UserData userData, final ElectionEvent electionEvent, final Set<Locale> localeSet) {
        ElectionEventStatusEnum dbStatus = electionEventRepository.findByPk(electionEvent.getPk()).getElectionEventStatusEnum();
        ElectionEvent updatedElectionEvent = electionEventRepository.update(userData, electionEvent);
        if (localeSet != null && !localeSet.isEmpty()) {
            updateLocalesForElectionEvent(userData, localeSet, updatedElectionEvent);
        }
        if (electionEvent.getElectionEventStatusEnum() != dbStatus) {
            countyService.electionEventStatusChanged(updatedElectionEvent);
        }
        return updatedElectionEvent;
    }

    private void updateLocalesForElectionEvent(final UserData userData, final Set<Locale> localeSet, final ElectionEvent updatedElectionEvent) {
        List<Locale> localeListOld = electionEventRepository.getLocalesForEvent(updatedElectionEvent);

        // Functionality for deciding if any localeTexts of electionEventLocales needs to be added
        for (Locale locale : localeSet) {
            ElectionEventLocale electionEventLocale = new ElectionEventLocale();
            if (!localeListOld.contains(locale)) {
                // Add ElectionEventLocale
                electionEventLocale.setElectionEvent(updatedElectionEvent);
                electionEventLocale.setLocale(locale);
                electionEventRepository.createElectionEventLocale(userData, electionEventLocale);

                // Make locale text for hardcoded roles
                localeTextService.createLocaleTextForElectionEvent(userData, "@role[valghendelse_admin].name", "Valghendelsesadministrator", locale,
                        updatedElectionEvent);
            }
        }
        List<Locale> localeListNew = electionEventRepository.getLocalesForEvent(updatedElectionEvent);

        // Functionality for deciding if any localeTexts of electionEventLocales needs to be removed
        for (Locale locale : localeListNew) {
            if (!localeSet.contains(locale)) {
                // IMPLEMENTATION NOTE: Deletes via service method instead of a direct invocation to the repository, to enforce security check:
                deleteElectionEventLocaleForElectionEvent(updatedElectionEvent, locale);
                // Delete locale text for hardcoded roles
                localeTextRepository.delete(userData,
                        translationService.getLocaleTextByElectionEvent(updatedElectionEvent.getPk(), locale.getPk(), "@role[valghendelse_admin].name")
                                .getPk());
            }
        }
        refreshResourceBundlesEvent.fire(new RefreshResourceBundlesEvent());
    }

    private void deleteElectionEventLocaleForElectionEvent(ElectionEvent electionEvent, Locale locale) {
        electionEventRepository.deleteElectionEventLocaleForElectionEvent(electionEvent, locale);
    }

    public void approveConfiguration(UserData userData, Long pk) {
        ElectionEvent electionEvent = electionEventRepository.findByPk(pk);
        electionEvent.setElectionEventStatus(electionEventRepository.findElectionEventStatusById(ElectionEventStatusEnum.APPROVED_CONFIGURATION.id()));
        electionEventRepository.update(userData, electionEvent);
    }

    public boolean hasGroups(Long electionEventPk) {
        return !electionGroupRepository.getElectionGroupsSorted(electionEventPk).isEmpty();
    }

    public ElectionEvent findByPk(Long pk) {
        return electionEventRepository.findByPk(pk);
    }

    List<ElectionDay> findElectionDaysByElectionEvent(ElectionEvent electionEvent) {
        return electionEventRepository.findElectionDaysByElectionEvent(electionEvent).stream()
                .map(electionMapper::toElectionDay).sorted(Comparator.comparing(ElectionDay::getDate))
                .collect(toList());
    }

    List<OpeningHours> getDefaultOpeningHoursFromElectionEvent(UserData userData) {
        List<ElectionDay> electionDays = findElectionDaysByElectionEvent(userData.electionEvent());
        return electionDayOpeningHourList(electionDays);
    }

    private List<OpeningHours> electionDayOpeningHourList(List<ElectionDay> electionDays) {
        List<OpeningHours> openingHoursList = new ArrayList<>();
        for (ElectionDay electionDay : electionDays) {
            OpeningHours openingHours =
                    new OpeningHours();
            openingHours.setStartTime(electionDay.getStartTime());
            openingHours.setEndTime(electionDay.getEndTime());
            openingHours.setElectionDay(toDomainModel(electionDay));
            openingHoursList.add(openingHours);
        }
        return openingHoursList;
    }
}
