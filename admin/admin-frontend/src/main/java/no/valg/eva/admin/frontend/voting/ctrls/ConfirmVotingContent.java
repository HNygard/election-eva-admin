package no.valg.eva.admin.frontend.voting.ctrls;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import no.valg.eva.admin.common.voting.model.VoterDto;
import no.valg.eva.admin.common.voting.model.VotingConfirmationReportDto;
import no.valg.eva.admin.common.voting.model.VotingConfirmationStatus;
import no.valg.eva.admin.common.voting.model.VotingDto;
import no.valg.eva.admin.common.voting.service.VotingInEnvelopeService;
import no.valg.eva.admin.frontend.BaseController;
import no.valg.eva.admin.frontend.common.UpdatableComponent;
import no.valg.eva.admin.frontend.common.UpdatableComponentHandler;
import no.valg.eva.admin.frontend.common.search.SearchHandler;
import no.valg.eva.admin.frontend.i18n.MessageProvider;
import no.valg.eva.admin.frontend.voting.ctrls.model.ConfirmVotingContentViewModel;
import no.valg.eva.admin.frontend.voting.ctrls.model.ConfirmVotingViewModel;
import no.valg.eva.admin.frontend.voting.ctrls.model.VotingPeriodViewModel;
import no.valg.eva.admin.util.DateUtil;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static no.valg.eva.admin.common.voting.model.SuggestedProcessing.APPROVE;
import static no.valg.eva.admin.common.voting.model.VotingConfirmationStatus.APPROVED;
import static no.valg.eva.admin.common.voting.model.VotingConfirmationStatus.REJECTED;
import static no.valg.eva.admin.common.voting.model.VotingConfirmationStatus.TO_BE_CONFIRMED;
import static no.valg.eva.admin.configuration.application.MunicipalityMapper.toDto;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Named
@ViewScoped
@NoArgsConstructor

// slettes og denne navngis korrekt
public class ConfirmVotingContent extends BaseController
        implements UpdatableComponent<ConfirmVotingViewModel>, SearchHandler<VotingViewModel>, UpdatableComponentHandler {

    private static final long serialVersionUID = 8931394205560246202L;

    private static final String VOTING_CONFIRMATION_OVERVIEW_XHTML = "votingConfirmationOverview.xhtml";

    @Inject
    private VotingInEnvelopeService votingInEnvelopeService;

    @Inject
    private MessageProvider messageProvider;

    @Inject
    private ConfirmVotingTabs confirmVotingTabs;

    @Getter
    @Setter
    private ConfirmVotingViewModel contextViewModel;

    @Getter
    @Setter
    private ConfirmVotingContentViewModel viewModel;

    private UpdatableComponentHandler componentHandler;

    private VotingConfirmationReportDto votingConfirmationReportDto;

    private String votingConfirmationOverviewBaseURL;

    @Override
    public void initComponent(ConfirmVotingViewModel context, UpdatableComponentHandler componentHandler) {
        this.contextViewModel = context;
        this.componentHandler = componentHandler;

        fetchData();
        initTabsComponent(createTabsComponentContext());

        votingConfirmationOverviewBaseURL = VOTING_CONFIRMATION_OVERVIEW_XHTML + resolveRequestQueryString(context);
    }

    private void fetchData() {
        fetchData(null);
    }

    private void fetchData(VotingPeriodViewModel selectedVotingPeriod) {

        if (selectedVotingPeriod == null) {
            selectedVotingPeriod = new VotingPeriodViewModel(contextViewModel.getStartDate(), contextViewModel.getEndDateIncluding());
        }

        fetchDataAndUpdateViewModel(selectedVotingPeriod);

        getViewModel().setPhasePeriod(new VotingPeriodViewModel(contextViewModel.getStartDate(), contextViewModel.getEndDateIncluding()));
    }

    private void fetchDataAndUpdateViewModel(VotingPeriodViewModel selectedVotingPeriod) {
        execute(() -> votingConfirmationReportDto = votingInEnvelopeService.votingConfirmationReport(contextViewModel.getUserData(),
                contextViewModel.getMvArea(),
                contextViewModel.getElectionGroup(),
                contextViewModel.getVotingCategory(),
                contextViewModel.getVotingPhase(),
                selectedVotingPeriod.getFromDate(),
                selectedVotingPeriod.getToDateIncluding()
        ));

        setViewModel(createViewModel(votingConfirmationReportDto, selectedVotingPeriod));
    }

    private ConfirmVotingContentViewModel createViewModel(VotingConfirmationReportDto votingConfirmationReportDto, VotingPeriodViewModel selectedVotingPeriod) {

        return ConfirmVotingContentViewModel.builder()
                .numberOfApprovedVotings(votingConfirmationReportDto.getNumberOfApprovedVotings())
                .numberOfRejectedVotings(votingConfirmationReportDto.getNumberOfRejectedVotings())
                .numberOfVotingsToConfirm(votingConfirmationReportDto.getNumberOfVotingsToConfirm())
                .votingList(votingViewModels(votingConfirmationReportDto.getVotingDtoListToConfirm()))
                .selectedVotingPeriod(selectedVotingPeriod)
                .handler(getContextViewModel().getConfirmVotingContentHandler())
                .build();
    }

    private List<VotingViewModel> votingViewModels(List<VotingDto> votingDtoList) {
        final List<VotingViewModel> list = new ArrayList<>();
        votingDtoList.forEach(voting -> {
            VotingViewModel votingViewModel = votingViewModel(voting);
            list.add(votingViewModel);
        });

        return list;
    }

    private VotingViewModel votingViewModel(VotingDto votingDto) {
        VoterDto voterDto = votingDto.getVoterDto();

        return VotingViewModel.builder()
                .voter(voterDto)
                .nameLine(voterDto.getNameLine())
                .firstName(voterDto.getFirstName())
                .middleName(voterDto.getMiddleName())
                .lastName(voterDto.getLastName())
                .personId(voterDto.getId())
                .votingDate(DateUtil.formatToShortDate(votingDto.getCastTimestamp()))
                .votingTime(DateUtil.formatToShortTime(votingDto.getCastTimestamp()))
                .votingCategory(votingDto.getVotingCategory())
                .votingNumber(votingDto.getVotingNumber())
                .suggestedRejectionReason(messageProvider.get(votingDto.getSuggestedProcessing()))
                .votingRegisteredBy(votingDto.getVoteReceiverName())
                .voterListedIn(voterDto.getMvArea().getMunicipalityName())
                .suggestedRejected(!APPROVE.getName().equals(votingDto.getSuggestedProcessing()))
                .electionGroup(votingDto.getElectionGroup())
                .build();
    }

    private void initTabsComponent(ConfirmVotingTabs.ContextViewModel contextViewModel) {
        if (isRenderContent()) {
            confirmVotingTabs.initComponent(contextViewModel, this);
        }
    }

    private ConfirmVotingTabs.ContextViewModel createTabsComponentContext() {
        return createTabsComponentContext(getViewModel().getVotingList());
    }

    private ConfirmVotingTabs.ContextViewModel createTabsComponentContext(List<VotingViewModel> votings) {
        return ConfirmVotingTabs.ContextViewModel.builder()
                .votingCategory(getContextViewModel().getVotingCategory())
                .electionGroup(getContextViewModel().getElectionGroup())
                .municipality(toDto(getContextViewModel().getMvArea().getMunicipality()))
                .votingList(votings)
                .confirmVotingContentHandler(getContextViewModel().getConfirmVotingContentHandler())
                .build();
    }

    private String resolveRequestQueryString(ConfirmVotingViewModel context) {
        return !isBlank(context.getRequestUrlQueryString()) ? "?" + context.getRequestUrlQueryString() : "";
    }
    
    public boolean isDemoElection() {
        return contextViewModel.getMvArea().getElectionEvent().isDemoElection();
    }

    @Override
    public void componentDidUpdate(ConfirmVotingViewModel context) {
        this.contextViewModel = context;

        fetchDataKeepingVotingPeriod();

        confirmVotingTabs.componentDidUpdate(createTabsComponentContext());
    }

    private void fetchDataKeepingVotingPeriod() {
        fetchData(votingPeriodNullSafe());
    }

    private VotingPeriodViewModel votingPeriodNullSafe() {
        return viewModel != null ? viewModel.getSelectedVotingPeriod() : null;
    }

    public String getVotingsToBeConfirmedOverviewURL() {
        return requestUrl(TO_BE_CONFIRMED);
    }

    private LocalDate toDate() {
        return viewModel != null && viewModel.getSelectedVotingPeriod() != null ? viewModel.getSelectedVotingPeriod().getToDateIncluding().toLocalDate() : null;
    }

    private LocalDate fromDate() {
        return viewModel != null && viewModel.getSelectedVotingPeriod() != null ? viewModel.getSelectedVotingPeriod().getFromDate().toLocalDate() : null;
    }

    public String getApprovedVotingsOverviewURL() {
        return requestUrl(APPROVED);
    }

    private String requestUrl(VotingConfirmationStatus votingConfirmationStatus) {
        return VotingOverviewUrlFactory.requestUrl(contextViewModel.getVotingCategory(),
                contextViewModel.getVotingPhase(),
                fromDate(),
                toDate(),
                votingConfirmationOverviewBaseURL,
                votingConfirmationStatus);
    }

    public String getRejectedVotingsOverviewURL() {
        return requestUrl(REJECTED);
    }

    public boolean isRenderContent() {
        return contextViewModel.isCategoryOpen();
    }

    public void onVotingPeriodUpdated() {
        fetchDataKeepingVotingPeriod();
        initTabsComponent(createTabsComponentContext());
    }

    @Override
    public void onSearchCallback(List<VotingViewModel> filteredVotings) {
        initTabsComponent(createTabsComponentContext(filteredVotings));
    }

    @Override
    public void forceUpdate(UpdatableComponent component) {
        componentHandler.forceUpdate(this);
    }
}   
