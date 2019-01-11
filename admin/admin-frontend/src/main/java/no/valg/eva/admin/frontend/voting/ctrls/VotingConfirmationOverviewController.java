package no.valg.eva.admin.frontend.voting.ctrls;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.PagedList;
import no.valg.eva.admin.common.configuration.model.Manntallsnummer;
import no.valg.eva.admin.common.configuration.service.ManntallsnummerService;
import no.valg.eva.admin.common.voting.VotingCategory;
import no.valg.eva.admin.common.voting.VotingPhase;
import no.valg.eva.admin.common.voting.model.SuggestedProcessingDto;
import no.valg.eva.admin.common.voting.model.VoterDto;
import no.valg.eva.admin.common.voting.model.VotingConfirmationStatus;
import no.valg.eva.admin.common.voting.model.VotingDto;
import no.valg.eva.admin.common.voting.model.VotingFilters;
import no.valg.eva.admin.common.voting.model.VotingRejectionDto;
import no.valg.eva.admin.common.voting.model.VotingSorting;
import no.valg.eva.admin.common.voting.service.VotingInEnvelopeService;
import no.valg.eva.admin.configuration.domain.model.ElectionGroup;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.felles.sti.valggeografi.ValggeografiSti;
import no.valg.eva.admin.frontend.common.PageTitleMetaModel;
import no.valg.eva.admin.frontend.kontekstvelger.Kontekst;
import no.valg.eva.admin.frontend.kontekstvelger.KontekstAvhengigController;
import no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerOppsett;
import no.valg.eva.admin.frontend.util.FacesUtil;
import no.valg.eva.admin.frontend.voting.ctrls.model.VotingConfirmationOverviewViewModel;
import no.valg.eva.admin.frontend.voting.ctrls.model.VotingOverviewType;
import no.valg.eva.admin.frontend.voting.ctrls.model.VotingPeriodViewModel;
import no.valg.eva.admin.util.DateUtil;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static no.valg.eva.admin.common.voting.VotingCategory.votingCategoryList;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.KOMMUNE;
import static no.valg.eva.admin.felles.valghierarki.model.ValghierarkiNivaa.VALGGRUPPE;
import static no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerElement.geografi;
import static no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerElement.hierarki;
import static no.valg.eva.admin.frontend.voting.ctrls.VotingOverviewUrlFactory.FROM_DATE_REQUEST_PARAMETER;
import static no.valg.eva.admin.frontend.voting.ctrls.VotingOverviewUrlFactory.TO_DATE_REQUEST_PARAMETER;
import static no.valg.eva.admin.frontend.voting.ctrls.VotingOverviewUrlFactory.VALIDATED_REQUEST_PARAMETER;
import static no.valg.eva.admin.frontend.voting.ctrls.VotingOverviewUrlFactory.VOTING_CATEGORY_REQUEST_PARAMETER;
import static no.valg.eva.admin.frontend.voting.ctrls.VotingOverviewUrlFactory.VOTING_PHASE_REQUEST_PARAMETER;
import static no.valg.eva.admin.frontend.voting.ctrls.VotingOverviewUrlFactory.VOTING_STATUS_REQUEST_PARAMETER;
import static no.valg.eva.admin.frontend.voting.ctrls.VotingViewModel.votingViewModel;
import static no.valg.eva.admin.util.DateUtil.firstDayOfYear;
import static no.valg.eva.admin.util.DateUtil.lastDateOfYear;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.primefaces.model.SortOrder.ASCENDING;

@Named
@ViewScoped
@NoArgsConstructor
public class VotingConfirmationOverviewController extends KontekstAvhengigController implements VoterConfirmation.Handler {

    private static final long serialVersionUID = 8931394205560246202L;
    private static final String REJECTION_REASON_FILTER_KEY = "rejectionReason";
    private static final String SUGGESTED_PROCESSING_FILTER_KEY = "suggestedRejectionReason";
    private static final String VOTING_CATEGORY_FILTER_KEY = "votingCategory.name";
    private static final String CONFIRMATION_STATUS_FILTER_KEY = "status";
    private static final String PROCESSING_TYPE_FILTER_KEY = "processingType";
    private static final int PAGE_SIZE = 100;

    @Inject
    private VotingInEnvelopeService votingInEnvelopeService;
    @Inject
    private VoterConfirmation voterConfirmation;
    @Inject
    private ManntallsnummerService manntallsnummerService;
    
    @Getter
    @Setter
    private MvArea selectedMvArea;
    @Getter
    @Setter
    private VotingConfirmationOverviewViewModel viewModel;
    private ElectionGroup selectedElectionGroup;
    @Getter
    private LazyDataModel<VotingViewModel> votingLazyDataModel;
    @Getter
    private Component activeComponent = Component.VOTINGS;
    private boolean isInit = true;

    enum Component {
        VOTER,
        VOTINGS
    }

    @Override
    public KontekstvelgerOppsett getKontekstVelgerOppsett() {
        KontekstvelgerOppsett setup = new KontekstvelgerOppsett();
        setup.leggTil(hierarki(VALGGRUPPE));
        setup.leggTil(geografi(KOMMUNE));
        return setup;
    }

    @Override
    public void initialized(Kontekst context) {
        AreaPath selectedAreaPath = context.getValggeografiSti().areaPath().toAreaLevelPath(KOMMUNE.tilAreaLevelEnum());
        ValggeografiSti valggeografiSti = ValggeografiSti.fra(selectedAreaPath);
        fetchMvArea(valggeografiSti);
        fetchElectionGroup(context);
        buildViewModel();

        votingLazyDataModel = new VotingOverviewLazyDataModel();
    }

    private void buildViewModel() {
        VotingPeriodViewModel selectedVotingPeriod = votingPeriod(fromDateFromRequestOrDefault(), toDateFromRequestOrDefault());
        viewModel = VotingConfirmationOverviewViewModel.builder()
                .selectedVotingPeriod(selectedVotingPeriod)
                .votingOverviewType(showValidatedVotingsFromRequest())
                .allVotingCategories(votingCategoryList())
                .votingCategoriesFromRequest(votingCategoriesFromRequest())
                .rejectionReasons(loadRejectionReasons())
                .suggestedProcessingList(loadSuggestedProcessingList())
                .build();
    }

    private VotingOverviewType showValidatedVotingsFromRequest() {
        String parameter = getRequestParameter(VALIDATED_REQUEST_PARAMETER);
        if (!isBlank(parameter)) {
            return Boolean.parseBoolean(parameter) ? VotingOverviewType.CONFIRMED : VotingOverviewType.TO_BE_CONFIRMED;
        }
        return VotingOverviewType.TO_BE_CONFIRMED;
    }

    private VotingPeriodViewModel votingPeriod(LocalDateTime fromDate, LocalDateTime toDateIncluding) {
        return VotingPeriodViewModel.builder()
                .fromDate(fromDate)
                .toDateIncluding(toDateIncluding)
                .build();
    }

    private List<VotingCategory> votingCategoriesFromRequest() {
        String votingCategoryParameter = getRequestParameter(VOTING_CATEGORY_REQUEST_PARAMETER);

        List<VotingCategory> votingCategories = new ArrayList<>();
        if (!isBlank(votingCategoryParameter)) {
            VotingCategory votingCategory = VotingCategory.valueOf(votingCategoryParameter.toUpperCase());
            votingCategories.add(votingCategory);
        }

        return votingCategories;
    }

    private void fetchMvArea(ValggeografiSti valggeografiSti) {
        execute(() -> setSelectedMvArea(getMvAreaService().findSingleByPath(valggeografiSti)));
    }

    private void fetchElectionGroup(Kontekst context) {
        execute(() -> selectedElectionGroup = getMvElectionService().findSingleByPath(context.valggruppeSti()).getElectionGroup());
    }

    private List<VotingRejectionDto> loadRejectionReasons() {
        List<VotingRejectionDto> votingRejectionDtoList = new ArrayList<>();
        execute(() -> votingRejectionDtoList.addAll(votingInEnvelopeService.votingRejections(getUserData())));

        return votingRejectionDtoList;
    }

    private List<SuggestedProcessingDto> loadSuggestedProcessingList() {
        List<SuggestedProcessingDto> suggestedProcessingViewModels = new ArrayList<>();

        execute(() -> suggestedProcessingViewModels.addAll(votingInEnvelopeService.suggestedProcessingList(getUserData())));

        return suggestedProcessingViewModels;
    }

    private LocalDateTime fromDateFromRequestOrDefault() {
        String parameter = getRequestParameter(FROM_DATE_REQUEST_PARAMETER);
        LocalDate fromDate;
        if (!isBlank(parameter)) {
            fromDate = DateUtil.parseShortIdDateString(parameter);
        } else {
            fromDate = firstDayOfYear(resolveElectionYear());
        }
        return DateUtil.startOfDay(fromDate);
    }
    
    private int resolveElectionYear() {
        org.joda.time.LocalDate eavsd = selectedElectionGroup.getElectionEvent().getEarlyAdvanceVotingStartDate();
        if (eavsd != null) {
            return eavsd.getYear();
        }
        return org.joda.time.LocalDate.now().getYear();
    }

    private LocalDateTime toDateFromRequestOrDefault() {
        String parameter = getRequestParameter(TO_DATE_REQUEST_PARAMETER);
        LocalDate toDateIncluding;
        if (!isBlank(parameter)) {
            toDateIncluding = DateUtil.parseShortIdDateString(parameter);
        } else {
            toDateIncluding = lastDateOfYear(resolveElectionYear());
        }
        return DateUtil.endOfDay(toDateIncluding);
    }

    public List<PageTitleMetaModel> getPageTitleMeta() {
        return getPageTitleMetaBuilder().area(getSelectedMvArea());
    }

    public List<VotingConfirmationStatus> getVotingConfirmationStatuses() {
        return VotingConfirmationStatus.votingConfirmationStatuses(viewModel.isShowConfirmedVotings());
    }

    public boolean isStatusFilterDisabled() {
        return getVotingConfirmationStatuses().size() <= 1;
    }

    public boolean isStatusSortable() {
        return !isStatusFilterDisabled();
    }

    public boolean isRenderVoterConfirmation() {
        return Component.VOTER == activeComponent;
    }

    public boolean isRenderVotingOverview() {
        return Component.VOTINGS == activeComponent;
    }

    public void onSelectedVotingRow(SelectEvent event) {
        VotingViewModel selectedVotingViewModel = (VotingViewModel) event.getObject();
        getViewModel().setSelectedVoting(selectedVotingViewModel);

        activeComponent = Component.VOTER;

        voterConfirmation.initComponent(VoterConfirmation.VoterConfirmationContext.builder()
                .voterDto(selectedVotingViewModel.getVoter())
                .electoralRollNumber(generateElectoralRollNumber(selectedVotingViewModel))
                .votingCategory(selectedVotingViewModel.getVotingCategory().votingCategoryById())
                .handler(this)
                .userData(getUserData())
                .mvArea(selectedMvArea)
                .handler(this)
                .electionGroup(selectedElectionGroup)
                .build());

        FacesUtil.updateDom("form");
    }

    private Manntallsnummer generateElectoralRollNumber(VotingViewModel viewModel) {
        VoterDto voter = viewModel.getVoter();
        if (voter == null || voter.getNumber() == null) {
            return null;
        }
        return manntallsnummerService.beregnFulltManntallsnummer(getUserData(), voter.getNumber());
    }

    @Override
    public void onVoterConfirmationDismiss() {
        activeComponent = Component.VOTINGS;
        FacesUtil.updateDom("form");
    }

    public void onVotingPeriodUpdated() {
        getVotingLazyDataModel().load(0, PAGE_SIZE, "", ASCENDING, Collections.emptyMap());
        FacesUtil.updateDom("form");
    }

    private class VotingOverviewLazyDataModel extends LazyDataModel<VotingViewModel> {

        private static final long serialVersionUID = 6954924259973278764L;

        private PagedList<VotingDto> pagedVotingList;

        @Override
        public List<VotingViewModel> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, Object> filters) {
            return lazyLoadVotingViewModels(first, pageSize, sortField, sortOrder, filters);
        }

        private List<VotingViewModel> lazyLoadVotingViewModels(int offset, int limit, String sortField, SortOrder sortOrder, Map<String, Object> filters) {
            VotingFilters votingFilters = votingFilters(filters);

            VotingSorting votingSorting = VotingSorting.builder().sortField(sortField).sortOrder(sortOrder.name()).build();
            pagedVotingList = votingInEnvelopeService.votings(getUserData(), selectedMvArea, selectedElectionGroup, votingFilters, votingSorting, offset, limit);

            votingLazyDataModel.setRowCount(pagedVotingList.getTotalNumberOfObjects());

            return votingViewModels(pagedVotingList.getObjects());
        }

        private VotingFilters votingFilters(Map<String, Object> filters) {
            VotingConfirmationStatus votingConfirmationStatus;
            List<VotingCategory> votingCategories;
            LocalDateTime fromDate;
            LocalDateTime toDate;
            if (!isInit) {
                votingConfirmationStatus = votingConfirmationStatusFromFilter(filters);
                votingCategories = votingCategoryFromFilter(filters);
                fromDate = viewModel.getSelectedVotingPeriod().getFromDate();
                toDate = viewModel.getSelectedVotingPeriod().getToDateIncluding();
            } else {
                votingConfirmationStatus = votingConfirmationStatusFromRequest();
                votingCategories = viewModel.getVotingCategoriesFromRequest();
                viewModel.setSelectedVotingCategories(votingCategories.stream().map(VotingCategory::getId).collect(Collectors.toList()));
                fromDate = fromDateFromRequestOrDefault();
                toDate = toDateFromRequestOrDefault();
                isInit = false;
            }

            return VotingFilters.builder()
                    .validatedVotings(viewModel.isShowConfirmedVotings())
                    .votingCategories(votingCategories)
                    .votingConfirmationStatus(votingConfirmationStatus)
                    .votingRejections(votingRejectionsFromFilter(filters))
                    .suggestedProcessingList(suggestedProcessingListFromFilter(filters))
                    .fromDate(fromDate)
                    .toDateIncluding(toDate)
                    .processingType(processingTypeFromFilter(filters))
                    .votingPhase(votingPhaseFromRequest())
                    .build();
        }

        private List<VotingCategory> votingCategoryFromFilter(Map<String, Object> filters) {
            if (!isInit && filters != null && filters.get(VOTING_CATEGORY_FILTER_KEY) != null) {
                List<String> strings = (List<String>) filters.get(VOTING_CATEGORY_FILTER_KEY);
                return strings.stream()
                        .map(VotingCategory::valueOf)
                        .collect(Collectors.toList());
            }
            return emptyList();
        }

        private VotingConfirmationStatus votingConfirmationStatusFromFilter(Map<String, Object> filters) {
            if (filters != null && filters.get(CONFIRMATION_STATUS_FILTER_KEY) != null) {
                return (VotingConfirmationStatus) filters.get(CONFIRMATION_STATUS_FILTER_KEY);
            }
            return null;
        }

        private VotingConfirmationStatus votingConfirmationStatusFromRequest() {
            String parameter = getRequestParameter(VOTING_STATUS_REQUEST_PARAMETER);

            VotingConfirmationStatus votingConfirmationStatus = null;
            if (!isBlank(parameter)) {
                votingConfirmationStatus = VotingConfirmationStatus.valueOf(parameter.toUpperCase());
            }

            return votingConfirmationStatus;
        }

        private VotingPhase votingPhaseFromRequest() {
            String votingPhaseParameter = getRequestParameter(VOTING_PHASE_REQUEST_PARAMETER);

            VotingPhase votingPhase = null;
            if (!isBlank(votingPhaseParameter)) {
                votingPhase = VotingPhase.valueOf(votingPhaseParameter.toUpperCase());
            }

            return votingPhase;
        }

        private List<VotingRejectionDto> votingRejectionsFromFilter(Map<String, Object> filters) {
            if (filters != null && filters.get(REJECTION_REASON_FILTER_KEY) != null) {
                List<String> votingRejectionIds = (List<String>) filters.get(REJECTION_REASON_FILTER_KEY);
                return votingRejectionIds.stream()
                        .map(votingRejectionId -> viewModel.votingRejectionFromId(votingRejectionId))
                        .collect(Collectors.toList());
            }
            return emptyList();
        }

        private List<SuggestedProcessingDto> suggestedProcessingListFromFilter(Map<String, Object> filters) {
            if (filters != null && filters.get(SUGGESTED_PROCESSING_FILTER_KEY) != null) {
                List<String> suggestedRejectionIds = (List<String>) filters.get(SUGGESTED_PROCESSING_FILTER_KEY);
                return suggestedRejectionIds.stream()
                        .map(suggestedProcessingId -> viewModel.suggestedProcessingFromId(suggestedProcessingId))
                        .collect(Collectors.toList());
            }
            return emptyList();
        }

        private String processingTypeFromFilter(Map<String, Object> filters) {
            if (filters != null && filters.get(PROCESSING_TYPE_FILTER_KEY) != null) {
                return (String) filters.get(PROCESSING_TYPE_FILTER_KEY);
            }
            return null;
        }

        private List<VotingViewModel> votingViewModels(List<VotingDto> votingDtoList) {
            List<VotingViewModel> list = new ArrayList<>();

            if (votingDtoList == null) {
                return list;
            }

            votingDtoList.forEach(voting -> {
                VotingViewModel votingViewModel = votingViewModel(voting);
                list.add(votingViewModel);
            });

            return list;
        }

        @Override
        public void setRowIndex(int rowIndex) {
            super.setRowIndex(rowIndex % getPageSize());
        }

        @Override
        public Object getRowKey(VotingViewModel votingViewModel) {
            return votingViewModel.getVotingNumber();
        }

        @Override
        public VotingViewModel getRowData(String votingNumber) {
            return votingViewModelFromVotingNumber(votingNumber);
        }

        private VotingViewModel votingViewModelFromVotingNumber(String votingNumber) {
            Integer id = Integer.valueOf(votingNumber);

            for (VotingDto voting : pagedVotingList.getObjects()) {
                if (id.equals(voting.getVotingNumber())) {
                    return votingViewModel(voting);
                }
            }
            return null;
        }
    }
}   
