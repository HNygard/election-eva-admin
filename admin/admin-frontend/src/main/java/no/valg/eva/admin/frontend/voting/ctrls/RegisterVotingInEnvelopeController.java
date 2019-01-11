package no.valg.eva.admin.frontend.voting.ctrls;

import lombok.Getter;
import lombok.Setter;
import no.evote.service.configuration.MvElectionService;
import no.evote.service.configuration.VoterService;
import no.evote.service.producer.EjbProxy;
import no.evote.service.voting.VotingService;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.voting.service.VotingInEnvelopeService;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.configuration.domain.model.Voter;
import no.valg.eva.admin.felles.melding.Melding;
import no.valg.eva.admin.felles.sti.valggeografi.ValggeografiSti;
import no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa;
import no.valg.eva.admin.frontend.common.MeldingerWidget;
import no.valg.eva.admin.frontend.common.PageTitleMetaModel;
import no.valg.eva.admin.frontend.kontekstvelger.Kontekst;
import no.valg.eva.admin.frontend.kontekstvelger.KontekstAvhengigController;
import no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerOppsett;
import no.valg.eva.admin.frontend.manntall.widgets.ManntallsSokWidget;
import no.valg.eva.admin.frontend.util.FacesUtilHelper;
import no.valg.eva.admin.frontend.util.MessageUtil;
import no.valg.eva.admin.util.DateUtil;
import no.valg.eva.admin.voting.domain.model.AvgittStemme;
import no.valg.eva.admin.voting.domain.model.VelgerMelding;
import no.valg.eva.admin.voting.domain.model.VelgerMeldingType;
import no.valg.eva.admin.voting.domain.model.VelgerSomSkalStemme;
import no.valg.eva.admin.voting.domain.model.VotingCategoryStatus;
import org.joda.time.DateTime;
import org.primefaces.event.TabChangeEvent;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;
import static no.valg.eva.admin.common.voting.VotingCategory.isElectionDayVotingCategory;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.KOMMUNE;
import static no.valg.eva.admin.felles.valghierarki.model.ValghierarkiNivaa.VALGGRUPPE;
import static no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerElement.geografi;
import static no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerElement.hierarki;

@Named
@ViewScoped
public class RegisterVotingInEnvelopeController extends KontekstAvhengigController {

    private static final long serialVersionUID = 2004478729890647912L;

    @Inject
    private RegisterVotingInEnvelopeMenuFactory registerVotingInEnvelopeMenuFactory;

    @Inject
    private RegisterVotingInEnvelopeSearchListener registerVotingInEnvelopeSearchListener;

    @Inject
    private ManntallsSokWidget electoralSearchWidget;

    @Inject
    private FacesUtilHelper facesUtilHelper;

    @Inject
    @EjbProxy
    protected VotingService votingService;

    @Inject
    protected VoterService voterService;

    @Getter
    private transient List<RegisterVotingInEnvelopeMenuItem> menuItems = new ArrayList<>();

    @Inject
    private MvElectionService mvElectionService;

    @Getter
    @Setter
    private int activeTabIndex = -1;

    @Getter
    @Setter
    private RegisterVotingInEnvelopeMenuItem selectedMenuItem;

    @Getter
    @Setter
    private MvArea selectedMvArea;

    @Inject
    private VotingInEnvelopeService votingInEnvelopeService;

    @Getter
    @Setter
    private MvElection electionGroupAsMvElection;

    @Getter
    @Setter
    private boolean showRegisterVotingView = false;

    @Getter
    @Setter
    private boolean showElectoralRollSearchView = true;

    @Getter
    @Setter
    private Voter voter;

    @Getter
    @Setter
    private boolean voteToOtherMunicipalityConfirmDialog;

    @Getter
    private MeldingerWidget messagesWidget;

    @Getter
    @Setter
    private boolean emptySearchResult;

    @Getter
    @Setter
    private boolean canRegisterVoting = false;

    /**
     * This method configures the context picker setup
     */
    @Override
    public KontekstvelgerOppsett getKontekstVelgerOppsett() {
        KontekstvelgerOppsett setup = new KontekstvelgerOppsett();
        setup.leggTil(hierarki(VALGGRUPPE));
        setup.leggTil(geografi(KOMMUNE));
        return setup;
    }

    /**
     * This method kicks in when the context picker is done
     */
    @Override
    public void initialized(Kontekst context) {
        AreaPath selectedAreaPath = context.getValggeografiSti()
                .areaPath()
                .toAreaLevelPath(getElectionGeoLevel()
                        .tilAreaLevelEnum());
        ValggeografiSti valggeografiSti = ValggeografiSti.fra(selectedAreaPath);
        setSelectedMvArea(getMvAreaService().findSingleByPath(valggeografiSti));

        electionGroupAsMvElection = mvElectionService.findSingleByPath(context.valggruppeSti());
        messagesWidget = new MeldingerWidget();

        execute(() -> {
            List<VotingCategoryStatus> votingCategoryStatuses = votingInEnvelopeService.votingCategoryStatuses(getUserData(), selectedMvArea);
            
            this.menuItems = registerVotingInEnvelopeMenuFactory.buildMenuItems(votingCategoryStatuses, getSelectedMvArea().getMunicipality());
        });

        registerVotingInEnvelopeSearchListener.addListener(this);
    }

    public void onTabChange(TabChangeEvent tabChangeEvent) {
        resetTabContent();

        setActiveTabIndex(activeTabIndex(tabChangeEvent));
        RegisterVotingInEnvelopeMenuItem menuItem = (RegisterVotingInEnvelopeMenuItem) tabChangeEvent.getData();
        setSelectedMenuItem(menuItem);

        VotingController votingController = menuItem.getVotingController();
        votingController.onSelectedVotingCategory(
                getSelectedMvArea(),
                menuItem.getVotingCategory(),
                menuItem.getVotingPhase());
    }

    public String getView() {
        return selectedMenuItem != null ? selectedMenuItem.getView() : "";
    }

    private void resetElectoralSearchWidget(boolean resetSearchTabs) {

        if (resetSearchTabs) {
            electoralSearchWidget.setActiveTabIndex(0);
        }
        electoralSearchWidget.reset();
    }

    private void clearMessages() {
        getMessagesWidget().clear();
    }

    private int activeTabIndex(TabChangeEvent tabChangeEvent) {
        return Integer.parseInt(tabChangeEvent.getTab()
                .getClientId()
                .split(":")[1]);
    }

    ValggeografiNivaa getElectionGeoLevel() {
        return KOMMUNE;
    }

    public boolean renderCurrentMenuItem(RegisterVotingInEnvelopeMenuItem registerVotingInEnvelopeMenuItem) {
        return getSelectedMenuItem().getVotingCategory() == registerVotingInEnvelopeMenuItem.getVotingCategory() &&
                getSelectedMenuItem().getVotingPhase() == registerVotingInEnvelopeMenuItem.getVotingPhase() &&
                getSelectedMenuItem().isOpenForRegistration();
    }

    public List<PageTitleMetaModel> getPageTitleMeta() {
        return getPageTitleMetaBuilder().area(getSelectedMvArea());
    }


    /**
     * Er stemmestedet som er valgt et "Forhånd rett i urne"-stemmested?
     */
    public boolean isForhandsstemmeRettIUrne() {
        return getSelectedMvArea() != null &&
                getSelectedMvArea().getPollingPlace() != null &&
                getSelectedMvArea().getPollingPlace().isAdvanceVoteInBallotBox();
    }

    public void registerVoting(Voter voter) {
        getSelectedMenuItem()
                .getVotingController()
                .registerVoting(voter, getElectionGroupAsMvElection(), selectedMvArea.getMunicipality(), getSelectedMenuItem().getVotingPhase());

        resetTabContent();
    }

    public boolean isRegisterVotingButtonDisabled() {
        return !isCanRegisterVoting();
    }

    public void onCancelRegistrationClick() {
        resetTabContent();
    }

    private void resetTabContent() {
        setVoter(null);
        setEmptySearchResult(false);
        setShowElectoralRollSearchView(true);
        setShowRegisterVotingView(false);
        resetElectoralSearchWidget(true);
        clearMessages();
    }

    void buildVoterMessages(VelgerSomSkalStemme velgerSomSkalStemme) {
        for (VelgerMelding velgerMelding : velgerSomSkalStemme.getVelgerMeldinger()) {
            VelgerMeldingType type = velgerMelding.getVelgerMeldingType();
            switch (type) {
                case FORHANDSTEMME_ANNEN_KOMMUNE:
                    setVoteToOtherMunicipalityConfirmDialog(true);
                    addMessage(new Melding(velgerMelding.getAlvorlighetsgrad(), type.getKey()));
                    break;
                case ALLEREDE_IKKE_GODKJENT_STEMME_FORKASTET:
                case ALLEREDE_IKKE_GODKJENT_STEMME:
                case ALLEREDE_GODKJENT_STEMME:
                    addMessage(new Melding(velgerMelding.getAlvorlighetsgrad(), buildVotingAlreadyRegisteredMessage(velgerMelding)));
                    break;
                case VELGER_IKKE_MANNTALLSFORT_DENNE_KOMMUNEN:
                case VELGER_IKKE_MANNTALLSFORT_DENNE_KRETSEN:
                    buildVoterMessageForVoterOutsidePollingDistrict(velgerMelding);
                    break;
                default:
                    buildDefaultVoterMessage(velgerMelding, type);
            }
        }
    }

    private void buildVoterMessageForVoterOutsidePollingDistrict(VelgerMelding velgerMelding) {
        String voterName = getNameLine(getVoter());
        String melding1 = getMessageProvider().get(velgerMelding.getVelgerMeldingType().getKey(), voterName);
        String melding2 = getMessageProvider().get(velgerMelding.getTilleggsMelding().getVelgerMeldingType().getKey());
        addMessage(new Melding(velgerMelding.getAlvorlighetsgrad(), melding1 + " " + melding2));
    }

    private void buildDefaultVoterMessage(VelgerMelding velgerMelding, VelgerMeldingType type) {
        if (velgerMelding.getTilleggsMelding() == null) {
            getMessagesWidget().add(new Melding(velgerMelding.getAlvorlighetsgrad(), type.getKey()));
        } else {
            String m1 = getMessageProvider().get(velgerMelding.getVelgerMeldingType().getKey());
            String m2 = getMessageProvider().get(velgerMelding.getTilleggsMelding().getVelgerMeldingType().getKey());
            addMessage(new Melding(velgerMelding.getAlvorlighetsgrad(), m1 + " " + m2));
        }
    }

    public void addMessage(Melding melding) {
        if (messagesWidget == null) {
            messagesWidget = new MeldingerWidget();
        }

        getMessagesWidget().add(melding);
    }

    private String buildVotingAlreadyRegisteredMessage(VelgerMelding melding) {
        AvgittStemme avgittStemme = (AvgittStemme) melding.getData();
        String weekDay = getNameOfDay(avgittStemme.getVotingTimeStamp());
        return getMessageProvider().get(melding.getVelgerMeldingType().getKey(),
                getNameLine(getVoter()),
                avgittStemme.getVotingCategory().getId(),
                getMessageProvider().get(avgittStemme.getVotingCategory().getName()),
                weekDay,
                DateUtil.getFormattedShortDate(avgittStemme.getStemmegivningsTidspunkt()),
                timeString(avgittStemme.getStemmegivningsTidspunkt()));
    }

    private String getNameLine(Voter voter) {
        if (voter.isFictitious()) {
            return getMessageProvider().get("@person.fictitiousVoterNameLine");
        } else {
            return voter.getNameLine();
        }
    }

    private String getNameOfDay(LocalDateTime localDateTime) {
        return getMessageProvider().get(format("@common.date.weekday[%s].name", DateUtil.dayOfWeek(localDateTime.toLocalDate()))).toLowerCase();
    }

    private String timeString(DateTime dateTime) {
        return MessageUtil.timeString(dateTime, getUserData().getJavaLocale());
    }

    public void registerFictitiousVoterAndPrepareRegisterVotingView() {
        if (execute(() -> {
            Voter fictitiousVoter = voterService.createFictitiousVoter(getUserData(), getSelectedMvArea().getMunicipality().areaPath());
            setVoter(fictitiousVoter);
        })) {
            setCanRegisterVoting(true);
            setShowElectoralRollSearchView(false);
            setShowRegisterVotingView(true);
            setEmptySearchResult(false);
        }
        
        refreshRegisterVotingPanel();
    }

    public boolean isShowRegisterFictitiousVoterLink() {
        if (isElectionDayVotingCategory(selectedMenuItem.getVotingCategory())) {
            return emptySearchResult;
        }

        return emptySearchResult && !isForhandsstemmeRettIUrne();
    }

    public VotingController getVotingController() {
        return getSelectedMenuItem() != null ? getSelectedMenuItem().getVotingController() : null;
    }

    void onEmptyElectoralRollSearchResult() {
        setEmptySearchResult(true);
        VotingController registerVotingController = getSelectedMenuItem().getVotingController();
        registerVotingController.setVoterFoundInSearch(false);
        registerVotingController.resetData();
        refreshRegisterVotingPanel();
    }

    private void refreshRegisterVotingPanel() {
        facesUtilHelper.updateDom("registerVotesInEnvelopePanel");
    }
}
