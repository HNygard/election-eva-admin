package no.valg.eva.admin.voting.domain.service;

import no.evote.exception.EvoteException;
import no.valg.eva.admin.common.voting.LockType;
import no.valg.eva.admin.common.voting.Tense;
import no.valg.eva.admin.common.voting.VotingCategory;
import no.valg.eva.admin.common.voting.VotingPhase;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.service.BoroughElectionDomainService;
import no.valg.eva.admin.configuration.domain.service.MunicipalityDomainService;
import no.valg.eva.admin.configuration.repository.MunicipalityRepository;
import no.valg.eva.admin.util.DateUtil;
import no.valg.eva.admin.voting.domain.model.VotingCategoryStatus;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;
import static no.valg.eva.admin.common.voting.LockType.LOCKED;
import static no.valg.eva.admin.common.voting.LockType.NOT_APPLICABLE;
import static no.valg.eva.admin.common.voting.LockType.UNLOCKED;
import static no.valg.eva.admin.common.voting.Tense.FUTURE;
import static no.valg.eva.admin.common.voting.Tense.PAST;
import static no.valg.eva.admin.common.voting.Tense.PRESENT;
import static no.valg.eva.admin.common.voting.VotingCategory.VF;
import static no.valg.eva.admin.common.voting.VotingPhase.ADVANCE;
import static no.valg.eva.admin.common.voting.VotingPhase.EARLY;
import static no.valg.eva.admin.util.DateUtil.toLocalDateTime;

@Default
@ApplicationScoped
public class VotingCategoryStatusDomainService {

    private static final String CONFIG_ELECTION_EVENT_MISSING_VALUE = "@config.election_event.missing_value";
    private static final String CONFIG_LOCAL_ELECTION_DAY_MISSING_VALUE = "@config.local.election_day.missing_value";
    
    @Inject
    private BoroughElectionDomainService boroughElectionDomainService;
    @Inject
    private MunicipalityRepository municipalityRepository;
    @Inject
    private MunicipalityDomainService municipalityDomainService;

    public List<VotingCategoryStatus> votingCategoryStatuses(MvArea mvArea, boolean considerEndOfElection) {
        boolean hasAccessToBoroughs = boroughElectionDomainService.electionPathAndMvAreaHasAccessToBoroughs(mvArea);
        Municipality municipality = municipalityRepository.getReference(mvArea.getMunicipality());
        boolean xim = municipality.isElectronicMarkoffs();
        ElectionEvent electionEvent = mvArea.getElectionEvent();
        boolean demoElection = electionEvent.isDemoElection();
        ArrayList<VotingCategoryStatus> statuses = new ArrayList<>();

        validateDates(electionEvent, municipality);

        for (VotingPhase votingPhase : VotingPhase.values()) {
            LocalDate startingDate = startingDate(votingPhase, electionEvent, municipality);
            LocalDate endingDate = endingDate(votingPhase, electionEvent, municipality);
            for (VotingCategory votingCategory : VotingCategory.from(votingPhase, xim, hasAccessToBoroughs)) {

                String messageProperty = name(votingPhase, votingCategory, hasAccessToBoroughs);
                Tense tense = tense(electionEvent, municipality, votingPhase, demoElection, considerEndOfElection);
                LockType locked = locked(electionEvent, municipality, votingPhase, demoElection);

                if (startingDate != null) {
                    statuses.add(new VotingCategoryStatus(messageProperty, votingCategory, votingPhase, tense, locked, startingDate, endingDate));
                }
            }
        }

        return statuses;
    }

    private void validateDates(ElectionEvent electionEvent, Municipality municipality) {
        if (electionEvent.getEarlyAdvanceVotingStartDate() == null) {
            throw new EvoteException(CONFIG_ELECTION_EVENT_MISSING_VALUE, "startdato for tidligstemmegivning");
        }
        if (electionEvent.getAdvanceVotingStartDate() == null) {
            throw new EvoteException(CONFIG_ELECTION_EVENT_MISSING_VALUE, "startdato for forhåndsstemmegivning");
        }

        if (!municipalityDomainService.isMinusThirtyMunicipality(electionEvent, municipality)
                && new LocalDate(Long.MAX_VALUE, DateTimeZone.UTC).equals(municipality.dateForFirstElectionDay())) {
            throw new EvoteException(CONFIG_LOCAL_ELECTION_DAY_MISSING_VALUE, "åpningstider");
        }
    }

    private LocalDate startingDate(VotingPhase votingPhase, ElectionEvent electionEvent, Municipality municipality) {
        switch (votingPhase) {
            case EARLY:
                return electionEvent.getEarlyAdvanceVotingStartDate();
            case ADVANCE:
                return electionEvent.getAdvanceVotingStartDate();
            case ELECTION_DAY:
            case LATE:
                return municipality.dateForFirstElectionDay();
            default:
                throw new IllegalStateException(String.format("Fasen '%s' er ikke håndtert. Kan dermed ikke hente startdato for fasen.", votingPhase));
        }
    }

    private String name(VotingPhase votingPhase, VotingCategory votingCategory, boolean hasAccessToBoroughs) {
        String boroughSuffix = voteForOtherBorough(votingCategory, hasAccessToBoroughs) ? "_BOROUGH" : "";
        return format("@voting_category_status[%s_%s%s].name", votingPhase, votingCategory, boroughSuffix);
    }

    private boolean voteForOtherBorough(VotingCategory votingCategory, boolean hasAccessToBoroughs) {
        return votingCategory == VF && hasAccessToBoroughs;
    }

    private Tense tense(ElectionEvent electionEvent, Municipality municipality, VotingPhase votingPhase, boolean demoElection, boolean considerEndOfElection) {
        if (demoElection) {
            return PRESENT;
        }

        switch (votingPhase) {
            case EARLY:
                return getTenseForEarly(electionEvent);
            case ADVANCE:
                return getTenseForAdvance(electionEvent, municipality);
            case ELECTION_DAY:
            case LATE:
                return getTenseForElectionDayOrLate(electionEvent, municipality, considerEndOfElection);
        }
        throw new IllegalStateException(String.format("Fasen '%s' er ikke håndtert. Kan dermed ikke hente status for fasen.", votingPhase));
    }

    private Tense getTenseForEarly(ElectionEvent electionEvent) {
        LocalDate now = LocalDate.now();
        if (now.isBefore(electionEvent.getEarlyAdvanceVotingStartDate())) {
            return FUTURE;
        }
        if (now.isBefore(electionEvent.getAdvanceVotingStartDate())) {
            return PRESENT;
        }
        return PAST;
    }

    private Tense getTenseForAdvance(ElectionEvent electionEvent, Municipality municipality) {
        
        LocalDate now = LocalDate.now();
        LocalDate advanceVotingStartDate = electionEvent.getAdvanceVotingStartDate();
        
        if (now.isBefore(advanceVotingStartDate)) {
            return FUTURE;
        }
        
        LocalDate dateForFirstElectionDay = municipality.dateForFirstElectionDay();
        if (dateForFirstElectionDay == null || (now.isBefore(dateForFirstElectionDay) && electronicMarkoffsOrElectoralRollNotPrintedFor(municipality))) {
            return PRESENT;
        }

        return PAST;
    }
    
    private boolean electronicMarkoffsOrElectoralRollNotPrintedFor(Municipality municipality) {
        return municipality.isElectronicMarkoffs() || !municipality.isAvkrysningsmanntallKjort();
    }

    private Tense getTenseForElectionDayOrLate(ElectionEvent electionEvent, Municipality municipality, boolean considerEndOfElection) {

        LocalDate dateForFirstElectionDay = municipality.dateForFirstElectionDay();
        
        if (dateForFirstElectionDay == null || LocalDate.now().isBefore(dateForFirstElectionDay) || electoralRollNotPrinted(municipality)) {
            return FUTURE;
        }
        if (considerEndOfElection && LocalDateTime.now().isAfter(endOfElection(electionEvent))) {
            return PAST;
        }

        return PRESENT;
    }
    
    private boolean electoralRollNotPrinted(Municipality municipality) {
        return !municipality.isElectronicMarkoffs() && !municipality.isAvkrysningsmanntallKjort();
    }

    private LocalDateTime endOfElection(ElectionEvent electionEvent) {
        return toLocalDateTime(electionEvent.getElectionEndDate(), electionEvent.getElectionEndTime());
    }

    private LockType locked(ElectionEvent electionEvent, Municipality municipality, VotingPhase votingPhase, boolean demoElection) {
        switch (votingPhase) {
            case ELECTION_DAY:
            case LATE:
                return NOT_APPLICABLE;
            default:
                break;
        }

        if (demoElection) {
            return UNLOCKED;
        }

        if (votingPhase.equals(EARLY) && getTenseForEarly(electionEvent).equals(PAST)) {
            return LOCKED;
        }

        if (votingPhase.equals(ADVANCE) && getTenseForAdvance(electionEvent, municipality).equals(PAST)) {
            return LOCKED;
        }

        return UNLOCKED;
    }
    
    private LocalDate endingDate(VotingPhase votingPhase, ElectionEvent electionEvent, Municipality municipality) {
        switch (votingPhase) {
            case EARLY:
                return electionEvent.getAdvanceVotingStartDate();
            case ADVANCE:
                return municipality.dateForFirstElectionDay();
            case ELECTION_DAY:
            case LATE:
                return firstDayOfYearAfterElectionYearIfApplicable(municipality);
            default:
                throw new IllegalStateException(String.format("Fasen '%s' er ikke håndtert. Kan dermed ikke hente startdato for fasen.", votingPhase));
        }
    }
    
    private LocalDate firstDayOfYearAfterElectionYearIfApplicable(Municipality municipality) {
        LocalDate firstElectionDay;
        if ((firstElectionDay = municipality.dateForFirstElectionDay()) != null) {
            int electionYear = firstElectionDay.getYear();
            return DateUtil.firstDayOfYearJT(electionYear + 1); 
        }
        return null;
    }
}
