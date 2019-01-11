package no.valg.eva.admin.voting.domain.service;

import lombok.extern.log4j.Log4j;
import no.evote.security.UserData;
import no.valg.eva.admin.backend.i18n.MessageProvider;
import no.valg.eva.admin.common.PagedList;
import no.valg.eva.admin.common.configuration.model.Municipality;
import no.valg.eva.admin.common.voting.VotingCategory;
import no.valg.eva.admin.common.voting.VotingRejection;
import no.valg.eva.admin.common.voting.model.VotingApprovalState;
import no.valg.eva.admin.common.voting.model.VotingApprovalStatus;
import no.valg.eva.admin.common.voting.model.VotingDto;
import no.valg.eva.admin.common.voting.model.VotingFilters;
import no.valg.eva.admin.common.voting.model.VotingSorting;
import no.valg.eva.admin.configuration.domain.model.ElectionGroup;
import no.valg.eva.admin.configuration.domain.model.Voter;
import no.valg.eva.admin.configuration.repository.VoterRepository;
import no.valg.eva.admin.voting.application.VotingMapper;
import no.valg.eva.admin.voting.domain.model.Voting;
import no.valg.eva.admin.voting.repository.PagingVotingRepository;
import no.valg.eva.admin.voting.repository.VotingRepository;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static no.valg.eva.admin.util.StringUtil.isNotBlank;
import static no.valg.eva.admin.voting.application.VotingMapper.toDto;

@Log4j
public class VotingInEnvelopeDomainService {
    
    private static final Locale NORWEGIAN_NB = new Locale("nb", "NO");
    private static final Locale NORWEGIAN_NN = new Locale("nn", "NO");
    
    @Inject
    private VotingRepository votingRepository;
    @Inject
    private VoterRepository voterRepository;
    @Inject
    private PagingVotingRepository pagingVotingRepository;

    private Map<Locale, Map<VotingCategory, String>> languageVotingCategoryMap = new HashMap<>();
    private Map<Locale, Map<VotingRejection, String>> languageVotingRejectionMap = new HashMap<>();

    @PostConstruct
    public void postConstruct() {
        loadTexts();
    }

    private void loadTexts() {
        if (languageVotingCategoryMap.size() == 0 || languageVotingRejectionMap.size() == 0) {
            languageVotingRejectionMap.put(NORWEGIAN_NB, loadVotingRejectionMap(NORWEGIAN_NB));
            languageVotingRejectionMap.put(NORWEGIAN_NN, loadVotingRejectionMap(NORWEGIAN_NN));

            languageVotingCategoryMap.put(NORWEGIAN_NB, loadVotingCategoryMap(NORWEGIAN_NB));
            languageVotingCategoryMap.put(NORWEGIAN_NN, loadVotingCategoryMap(NORWEGIAN_NN));
            log.info("Done loading votingcategory and votingrejection text maps for NORWEGIAN_NB and NORWEGIAN_NN for the first and last time..");
        }
    }

    private Map<VotingCategory, String> loadVotingCategoryMap(Locale locale) {
        return EnumSet.allOf(VotingCategory.class).stream()
                .collect(Collectors.toMap(votingCategory -> votingCategory, votingCategory -> getText(locale, "@voting_category[%s].name", votingCategory.getId())));
    }

    private String getText(Locale locale, String textProperty, String textArgument) {
        return MessageProvider.get(locale, format(textProperty, textArgument));
    }

    private Map<VotingRejection, String> loadVotingRejectionMap(Locale locale) {
        return EnumSet.allOf(VotingRejection.class).stream()
                .collect(Collectors.toMap(votingRejection -> votingRejection, votingRejection -> getText(locale, "@voting_rejection[%s].name", votingRejection.getId())));
    }

    public List<Voting> approvedVotingsForVoter(ElectionGroup electionGroup, Voter voter) {
        return filterApprovedVotings(allVotingsForVoter(electionGroup, voter));
    }

    private List<Voting> filterApprovedVotings(List<Voting> votings) {
        return votings.stream()
                .filter(Voting::isApproved)
                .collect(Collectors.toList());
    }

    public List<Voting> unconfirmedVotingsForVoter(ElectionGroup electionGroup, Voter voter) {
        return filterUnconfirmedVotings(allVotingsForVoter(electionGroup, voter));
    }

    private List<Voting> filterUnconfirmedVotings(List<Voting> votings) {
        return votings.stream()
                .filter(Voting::isNotConfirmed)
                .collect(Collectors.toList());
    }

    public List<Voting> rejectedVotingsForVoter(ElectionGroup electionGroup, Voter voter) {
        return filterRejectedVotings(allVotingsForVoter(electionGroup, voter));
    }

    private List<Voting> filterRejectedVotings(List<Voting> votings) {
        return votings.stream()
                .filter(voting -> voting.getVotingRejection() != null)
                .collect(Collectors.toList());
    }

    private List<Voting> allVotingsForVoter(ElectionGroup electionGroup, Voter voter) {
        return votingRepository.getVotingsByElectionGroupAndVoter(voter.getPk(), electionGroup.getPk());
    }

    public VotingApprovalStatus resolveSuggestedRejectedVotingApproval(ElectionGroup electionGroup, Municipality municipality, VotingDto votingDto, Voter voter) {

        final List<Voting> approvedVotings = approvedVotingsForVoter(electionGroup, voter);
        if (isNotEmpty(approvedVotings)) {
            return VotingApprovalStatus.builder()
                    .state(VotingApprovalState.PREVIOUSLY_APPROVED_VOTING)
                    .previouslyApprovedVoting(toDto(approvedVotings.get(0)))
                    .build();
        }

        final List<Voting> unconfirmedVotings = filterNonSelectedUnconfirmedVoting(votingDto, municipality, allVotingsForVoter(electionGroup, voter));
        if (isNotEmpty(unconfirmedVotings)) {
            return VotingApprovalStatus.builder()
                    .state(VotingApprovalState.MULTIPLE_UNCONFIRMED_VOTINGS)
                    .build();
        }

        return VotingApprovalStatus.builder()
                .state(VotingApprovalState.NO_OTHER_VOTINGS)
                .build();
    }

    private List<Voting> filterNonSelectedUnconfirmedVoting(VotingDto selectedVotingDto, Municipality municipality, List<Voting> allVotingsForVoter) {
        final Voting selectedVoting = votingRepository.findVotingByVotingNumber(municipality, selectedVotingDto);
        return filterUnconfirmedVotings(allVotingsForVoter).stream()
                .filter(voting -> !voting.getPk().equals(selectedVoting.getPk()))
                .collect(Collectors.toList());
    }

    private static boolean isNotEmpty(List list) {
        return !list.isEmpty();
    }

    public List<Voter> resolveVotersThatNeedToBeHandledOneByOne(ElectionGroup electionGroup, List<VotingDto> votingsToBeRejected) {
        Map<Voter, List<Voting>> votingMap = findAllVotingsForVoterInVotingsToBeRejected(electionGroup, votingsToBeRejected);

        List<Voter> votersToBeHandled = new ArrayList<>();
        for (Map.Entry<Voter, List<Voting>> voterMapEntry : votingMap.entrySet()) {

            final List<Voting> allVotingsForVoter = voterMapEntry.getValue();
            final List<Voting> approved = filterApprovedVotings(allVotingsForVoter);
            if (isNotEmpty(approved)) {
                continue;
            }

            List<Voting> unconfirmed = filterUnconfirmedVotings(allVotingsForVoter);
            if (unconfirmed.size() > 1) {
                votersToBeHandled.add(voterMapEntry.getKey());
            }
        }

        votersToBeHandled.sort(lastThenFirstName());
        return votersToBeHandled;
    }

    private Map<Voter, List<Voting>> findAllVotingsForVoterInVotingsToBeRejected(ElectionGroup electionGroup, List<VotingDto> votingsToBeRejected) {
        final Map<Voter, List<Voting>> map = new HashMap<>();
        for (VotingDto votingDto : votingsToBeRejected) {
            final Voter voter = voterRepository.voterOfId(votingDto.getVoterDto().getId(), electionGroup.getElectionEvent().getPk());
            if (!map.containsKey(voter)) {
                map.put(voter, allVotingsForVoter(electionGroup, voter));
            }
        }
        return map;
    }

    private Comparator<Voter> lastThenFirstName() {
        return (voter1, voter2) -> {
            int compareTo = -1;
            if (isNotBlank(voter1.getLastName(), voter1.getFirstName(), voter2.getLastName(), voter2.getFirstName())) {
                compareTo = voter1.getLastName().compareTo(voter2.getLastName());
                if (compareTo == 0) {
                    compareTo = voter1.getFirstName().compareTo(voter2.getFirstName());
                }
            }
            return compareTo;
        };
    }

    public PagedList<VotingDto> votings(UserData userData, no.valg.eva.admin.configuration.domain.model.Municipality municipality, ElectionGroup electionGroup, VotingFilters votingFilters, VotingSorting votingSorting, int offset, int limit) {
        Map<VotingCategory, String> votingCategoryTextMap = languageVotingCategoryMap.get(userData.getJavaLocale());
        Map<VotingRejection, String> votingRejectionTextMap = languageVotingRejectionMap.get(userData.getJavaLocale());

        List<Voting> votings = pagingVotingRepository.findVotings(municipality, electionGroup, votingFilters, votingSorting, votingCategoryTextMap, votingRejectionTextMap, offset, limit);
        List<VotingDto> votingDtos = votings.stream()
                .map(VotingMapper::toDto)
                .collect(Collectors.toList());

        int numberOfVotings = pagingVotingRepository.countVotings(municipality, electionGroup, votingFilters);

        return new PagedList<>(offset, limit, votingDtos, numberOfVotings);
    }
}
