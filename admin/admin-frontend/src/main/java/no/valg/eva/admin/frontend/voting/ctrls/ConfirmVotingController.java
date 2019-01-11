package no.valg.eva.admin.frontend.voting.ctrls;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.configuration.model.Manntallsnummer;
import no.valg.eva.admin.common.configuration.service.ManntallsnummerService;
import no.valg.eva.admin.common.voting.model.VoterDto;
import no.valg.eva.admin.common.voting.service.VotingInEnvelopeService;
import no.valg.eva.admin.configuration.domain.model.ElectionGroup;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.felles.sti.valggeografi.ValggeografiSti;
import no.valg.eva.admin.frontend.common.PageTitleMetaModel;
import no.valg.eva.admin.frontend.common.UpdatableComponent;
import no.valg.eva.admin.frontend.common.UpdatableComponentHandler;
import no.valg.eva.admin.frontend.kontekstvelger.Kontekst;
import no.valg.eva.admin.frontend.kontekstvelger.KontekstAvhengigController;
import no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerOppsett;
import no.valg.eva.admin.frontend.util.FacesUtil;
import no.valg.eva.admin.frontend.voting.ctrls.VoterConfirmation.VoterConfirmationContext;
import no.valg.eva.admin.frontend.voting.ctrls.model.ConfirmVotingViewModel;
import no.valg.eva.admin.voting.domain.model.ConfirmationCategoryStatus;
import org.primefaces.event.TabChangeEvent;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.KOMMUNE;
import static no.valg.eva.admin.felles.valghierarki.model.ValghierarkiNivaa.VALGGRUPPE;
import static no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerElement.geografi;
import static no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerElement.hierarki;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Named
@ViewScoped
@NoArgsConstructor

// slettes og denne navngis korrekt
public class ConfirmVotingController extends KontekstAvhengigController implements UpdatableComponentHandler, ConfirmVotingTabs.Handler, VoterConfirmation.Handler {

    private static final long serialVersionUID = 8931394205560246202L;

    private static final String MENU_ITEM_REQUEST_PARAMETER = "menuItem";

    @Inject
    private VotingInEnvelopeService votingInEnvelopeService;

    @Inject
    private ConfirmVotingMenuItemFactory confirmVotingMenuItemFactory;

    @Inject
    private VoterConfirmation voterConfirmation;
    
    @Inject
    private ManntallsnummerService manntallsnummerService;

    @Getter
    private List<ConfirmVotingMenuItem> menuItems;

    @Getter
    @Setter
    private ConfirmVotingMenuItem selectedMenuItem;

    @Inject
    private ConfirmVotingContent confirmVotingContent;

    @Getter
    @Setter
    private MvArea selectedMvArea;

    private ElectionGroup selectedElectionGroup;

    @Getter
    @Setter
    private int activeMenuItemIndex = -1;

    @Getter
    private String urlQueryString;

    @Getter
    @Setter
    private ConfirmVotingViewModel votingConfirmationViewModel;

    @Getter
    private Component activeComponent = Component.VOTINGS;

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
        AreaPath selectedAreaPath = context.getValggeografiSti()
                .areaPath()
                .toAreaLevelPath(KOMMUNE.tilAreaLevelEnum());
        ValggeografiSti valggeografiSti = ValggeografiSti.fra(selectedAreaPath);

        urlQueryString = getQueryString();

        fetchMvArea(valggeografiSti);
        fetchElectionGroup(context);
        fetchMenuItems();

        selectMenuItemFromRequest();
    }

    private void selectMenuItemFromRequest() {
        String currentMenuItemIndex = getRequestParameter(MENU_ITEM_REQUEST_PARAMETER);
        if (isNotBlank(currentMenuItemIndex)) {
            int index;
            try {
                int ix = Integer.parseInt(currentMenuItemIndex);
                index = validMenuItemIndex(ix) ? ix : 0;
            } catch (NumberFormatException e) {

                index = 0;
            }
            FacesUtil.executeJS("EVA.Application.getInstance().getView().selectTab(" + index + ")");
        }
    }

    private boolean validMenuItemIndex(int index) {
        return index >= 0 && index <= menuItems.size() - 1;
    }

    public void onAccordionChange(TabChangeEvent tabChangeEvent) {
        ConfirmVotingMenuItem menuItem = (ConfirmVotingMenuItem) tabChangeEvent.getData();

        activeMenuItemIndex = (Integer.parseInt(tabChangeEvent.getTab().getClientId().split(":")[2]));
        setSelectedMenuItem(menuItem);

        votingConfirmationViewModel = viewModel(menuItem);
        votingConfirmationViewModel.setRequestUrlQueryString(urlQueryString);
        votingConfirmationViewModel.setConfirmVotingContentHandler(this);
        votingConfirmationViewModel.setVotingCategory(menuItem.getVotingCategory());

        confirmVotingContent.initComponent(votingConfirmationViewModel, this);
    }

    protected ConfirmVotingViewModel viewModel(ConfirmVotingMenuItem menuItem) {
        return ConfirmVotingViewModel.builder()
                .electionGroup(selectedElectionGroup)
                .mvArea(selectedMvArea)
                .userData(getUserData())
                .categoryOpen(menuItem.isCategoryOpen())
                .votingCategory(menuItem.getVotingCategory())
                .votingPhase(menuItem.getVotingPhase())
                .startDate(menuItem.getStartDate())
                .endDateIncluding(menuItem.getEndDateIncluding())
                .confirmVotingContentHandler(this)
                .build();
    }

    private void fetchMvArea(ValggeografiSti valggeografiSti) {
        execute(() -> setSelectedMvArea(getMvAreaService().findSingleByPath(valggeografiSti)));
    }

    private void fetchElectionGroup(Kontekst context) {
        execute(() -> selectedElectionGroup = getMvElectionService().findSingleByPath(context.valggruppeSti()).getElectionGroup());
    }

    private void fetchMenuItems() {
        execute(() -> {
            List<ConfirmationCategoryStatus> confirmationCategoryStatuses = votingInEnvelopeService.confirmationCategoryStatuses(getUserData(), selectedMvArea,
                    selectedElectionGroup);
            menuItems = confirmVotingMenuItemFactory.buildMenuItems(confirmationCategoryStatuses);
        });
    }


    public List<PageTitleMetaModel> getPageTitleMeta() {
        return getPageTitleMetaBuilder().area(getSelectedMvArea());
    }

    @Override
    public void forceUpdate(UpdatableComponent component) {
        fetchMenuItems();
        ConfirmVotingViewModel confirmVotingViewModel = viewModel(getSelectedMenuItem());
        confirmVotingContent.componentDidUpdate(confirmVotingViewModel);
    }

    @Override
    public void onSelectedVoting(VotingViewModel votingViewModel) {
        activeComponent = Component.VOTER;

        voterConfirmation.initComponent(VoterConfirmationContext.builder()
                .voterDto(votingViewModel.getVoter())
                .handler(this)
                .userData(getUserData())
                .mvArea(selectedMvArea)
                .electionGroup(selectedElectionGroup)
                .voterDto(votingViewModel.getVoter())
                .electoralRollNumber(generateElectoralRollNumber(votingViewModel))
                .votingCategory(getVotingConfirmationViewModel().getVotingCategory())
                .build());

        FacesUtil.updateDom("form");
        FacesUtil.scrollTo("voter-confirmation");
    }

    private Manntallsnummer generateElectoralRollNumber(VotingViewModel viewModel) {
        VoterDto voter = viewModel.getVoter();
        if (voter == null || voter.getNumber() == null) {
            return null;
        }
        return manntallsnummerService.beregnFulltManntallsnummer(getUserData(), voter.getNumber());
    }

    public boolean isRenderConfirmVotingAccordion() {
        return Component.VOTINGS == activeComponent;
    }

    @Override
    public void onVoterConfirmationDismiss() {
        //set selected voting = null
        activeComponent = Component.VOTINGS;
        forceUpdate(null);
        FacesUtil.updateDom("form");
    }

    public boolean isRenderVoterConfirmationComponent() {
        return Component.VOTER == activeComponent;
    }
}   
