package no.valg.eva.admin.frontend.voting.ctrls.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.Singular;
import lombok.ToString;
import no.valg.eva.admin.common.voting.VotingCategory;
import no.valg.eva.admin.common.voting.model.SuggestedProcessingDto;
import no.valg.eva.admin.common.voting.model.VotingConfirmationStatus;
import no.valg.eva.admin.common.voting.model.VotingRejectionDto;
import no.valg.eva.admin.frontend.voting.ctrls.VotingViewModel;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Getter
@Builder
@AllArgsConstructor
@EqualsAndHashCode
@ToString(of = {"id"})
public class VotingConfirmationOverviewViewModel implements Serializable {

    private static final long serialVersionUID = -1588042259509879116L;
    private static final int MAX_NUMBER_OF_FILTERED_ITEMS_IN_HEADER = 4;
    private static final String COMMON_ALL_MESSAGE_PROPERTY = "@common.all";

    @EqualsAndHashCode.Include
    private String id;
    private List<VotingCategory> allVotingCategories;
    private List<VotingCategory> votingCategoriesFromRequest;
    private List<VotingRejectionDto> rejectionReasons;
    private LocalDateTime fromDate;
    private LocalDateTime toDateIncluding;
    @Setter
    private VotingPeriodViewModel selectedVotingPeriod;
    @Setter
    private List<VotingViewModel> selectedVotingList;
    @Setter
    private List<VotingViewModel> votings;
    @Setter
    private VotingViewModel selectedVoting;
    @Setter
    private List<String> selectedVotingCategories;
    @Setter
    private VotingConfirmationStatus selectedConfirmationStatus;
    @Setter
    private String selectedProcessingType;
    @Setter
    @Singular("suggestedProcessing")
    private List<SuggestedProcessingDto> suggestedProcessingList;
    @Setter
    private List<String> selectedSuggestedProcessingList;
    @Setter
    private List<String> selectedRejectionReasons;
    @Setter
    private VotingOverviewType votingOverviewType;

    public VotingRejectionDto votingRejectionFromId(String rejectionId) {
        Optional<VotingRejectionDto> votingRejectionDtoOptional = getRejectionReasons().stream()
                .filter(votingRejectionDto -> votingRejectionDto.getId().equals(rejectionId))
                .findFirst();

        return votingRejectionDtoOptional.orElse(null);
    }
    
    public SuggestedProcessingDto suggestedProcessingFromId(String suggestedProcessingId){
        return getSuggestedProcessingList().stream()
                .filter(suggestedProcessingViewModel -> suggestedProcessingViewModel.getId().equals(suggestedProcessingId))
                .findFirst().orElse(null);
    }

    public List<VotingRejectionDto> getRejectionReasons() {
        if (rejectionReasons == null) {
            return Collections.emptyList();
        }

        return rejectionReasons.stream()
                .sorted(Comparator.comparing(VotingRejectionDto::getId))
                .collect(Collectors.toList());
    }

    public String getVotingStatusColumnHeader() {
        return isShowConfirmedVotings() ? "@voting.envelope.overview.heading.processedStatus" : "@voting.envelope.overview.heading.suggestedProcessing";
    }

    public boolean isShowConfirmedVotings() {
        return VotingOverviewType.CONFIRMED == votingOverviewType;
    }

    public boolean isRenderProcessingTypeColumn() {
        return !isShowConfirmedVotings();
    }

    public boolean isRenderSuggestedRejectionReason() {
        return !isShowConfirmedVotings();
    }

    public boolean isRenderVotingStatusColumn() {
        return isShowConfirmedVotings();
    }

    public String getVotingOverviewHeading() {
        return isShowConfirmedVotings() ? "@voting.confirmation.heading.processedVotingList" : "@voting.confirmation.heading.votingListToProcess";
    }

    public String getFilteredVotingCategoriesHeader() {
        String filterHeader = selectedVotingCategories.stream()
                .map(Object::toString)
                .limit(MAX_NUMBER_OF_FILTERED_ITEMS_IN_HEADER)
                .collect(Collectors.joining(", "));

        if (selectedVotingCategories.size() > MAX_NUMBER_OF_FILTERED_ITEMS_IN_HEADER) {
            filterHeader = filterHeader.concat(" ...");
        }

        if (selectedVotingCategories.isEmpty() || selectedVotingCategories.size() == allVotingCategories.size()) {
            filterHeader = COMMON_ALL_MESSAGE_PROPERTY;
        }

        return filterHeader;
    }

    public String getSuggestedProcessingColumnFilterHeader() {
        return filteredRejectionReasonHeader(selectedSuggestedProcessingList, suggestedProcessingList.size());
    }

    public String getRejectionReasonsColumnFilterHeader() {
       return filteredRejectionReasonHeader(selectedRejectionReasons, rejectionReasons.size());
    }
    
    private String filteredRejectionReasonHeader(List<String> selectedItems, int allItemsListSize){
        String filterHeader = COMMON_ALL_MESSAGE_PROPERTY;
        if (selectedItems != null && !selectedItems.isEmpty()) {
            filterHeader = selectedItems.stream()
                    .map(Object::toString)
                    .limit(MAX_NUMBER_OF_FILTERED_ITEMS_IN_HEADER)
                    .collect(Collectors.joining(", "));

            if (selectedItems.size() > MAX_NUMBER_OF_FILTERED_ITEMS_IN_HEADER) {
                filterHeader = filterHeader.concat(" ...");
            }
        }

        if (selectedItems != null && selectedItems.size() == allItemsListSize) {
            filterHeader = COMMON_ALL_MESSAGE_PROPERTY;
        }

        return filterHeader;
    }
}
