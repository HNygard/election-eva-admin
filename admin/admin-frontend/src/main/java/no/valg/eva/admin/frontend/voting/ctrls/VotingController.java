package no.valg.eva.admin.frontend.voting.ctrls;

import lombok.Getter;
import lombok.Setter;
import no.evote.security.UserData;
import no.evote.service.configuration.MvAreaService;
import no.evote.service.configuration.VoterService;
import no.evote.service.producer.EjbProxy;
import no.evote.service.voting.VotingService;
import no.valg.eva.admin.common.configuration.model.Manntallsnummer;
import no.valg.eva.admin.common.voting.VotingCategory;
import no.valg.eva.admin.common.voting.VotingPhase;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.configuration.domain.model.Voter;
import no.valg.eva.admin.felles.valggeografi.model.Kommune;
import no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa;
import no.valg.eva.admin.felles.valggeografi.service.ValggeografiService;
import no.valg.eva.admin.frontend.BaseController;
import no.valg.eva.admin.frontend.common.PageTitleMetaBuilder;
import no.valg.eva.admin.frontend.common.PageTitleMetaModel;
import no.valg.eva.admin.frontend.i18n.MessageProvider;
import no.valg.eva.admin.frontend.manntall.widgets.ManntallsSokWidget;
import no.valg.eva.admin.frontend.user.ctrls.UserDataController;
import no.valg.eva.admin.frontend.util.MessageUtil;
import no.valg.eva.admin.util.DateUtil;
import no.valg.eva.admin.voting.domain.model.StemmegivningsType;
import no.valg.eva.admin.voting.domain.model.Voting;
import org.joda.time.DateTime;

import javax.inject.Inject;
import java.util.List;

public abstract class VotingController extends BaseController {

    private static final long serialVersionUID = 4334663988494247003L;

    @Inject
    protected ManntallsSokWidget manntallsSokWidget;
    @Inject
    @EjbProxy
    protected VotingService votingService;
    @Inject
    protected VoterService voterService;
    @Setter
    protected transient List<Kommune> countyList;
    @Inject
    @Getter
    ValggeografiService valggeografiService;
    @Inject
    @Getter
    private MvAreaService mvAreaService;
    @Inject
    @Getter
    private MessageProvider messageProvider;
    @Inject
    @Getter
    private UserDataController userDataController;
    @Inject
    private PageTitleMetaBuilder pageTitleMetaBuilder;
    @Setter
    @Getter
    private MvArea pollingPlaceMvArea;

    @Getter
    private boolean voteToOtherMunicipalityConfirmDialog;

    @Getter
    @Setter
    private MvArea county;

    @Getter
    @Setter
    private no.valg.eva.admin.common.voting.VotingCategory votingCategory;

    @Getter
    @Setter
    private boolean voterFoundInSearch;

    @Getter
    @Setter
    private MvElection electionGroupAsMvElection;

    @Getter
    @Setter
    private VotingPhase votingPhase;

    public String getVotingHeader() {
        String categoryName = messageProvider.get(getVotingCategory().getName(votingPhase));
        return messageProvider.get("@voting.envelope.searchVoter.header", categoryName);
    }

    public abstract boolean isShowDeleteAdvanceVotingLink();

    public abstract void deleteAdvanceVoting();

    public abstract StemmegivningsType votingType();

    public abstract void registerVoting(Voter voter, MvElection mvElection, Municipality municipality, VotingPhase votingPhase);

    /**
     * Hvilken område kontekst skal det registreres stemmer på?
     */
    public abstract ValggeografiNivaa getPollingPlaceElectionGeoLevel();

    public abstract void resetData();

    protected UserData getUserData() {
        return getUserDataController().getUserData();
    }

    public void onSelectedVotingCategory(MvArea selectedMvArea, VotingCategory votingCategory, VotingPhase votingPhase) {
        setVotingCategory(votingCategory);
        setPollingPlaceMvArea(selectedMvArea);
        setCounty(selectedMvArea);
        setVotingPhase(votingPhase);
        resetData();
    }

    String buildVotingMessage(Voter voter, Voting voting, String message) {
        String weekDay = getNameOfDay(voting.getCastTimeStampAsJavaTime());
        return getMessageProvider().get(
                message,
                getVoterName(voter),
                weekDay,
                DateUtil.getFormattedShortDate(voting.getCastTimestamp()),
                timeString(voting.getCastTimestamp()),
                voting.getVotingCategory().getId(),
                "" + voting.getVotingNumber());
    }

    private String getNameOfDay(java.time.LocalDateTime datetime) {
        String key = String.format("@common.date.weekday[%s].name", DateUtil.dayOfWeek(datetime.toLocalDate()));
        return getMessageProvider().get(key).toLowerCase();
    }

    String timeString(DateTime dateTime) {
        return MessageUtil.timeString(dateTime, getUserData().getJavaLocale());
    }

    /**
     * Returnerer navn på velger. I utgangspunktet [for-, mellom- og etternavn] eller "Fiktiv velger".
     */
    String getVoterName(Voter voter) {
        if (voter.isFictitious()) {
            return getMessageProvider().get("@person.fictitiousVoterNameLine");
        } else {
            return voter.getNameLine();
        }
    }

    /**
     * Liste med alle tilgjengelige kommuner.
     */
    public List<Kommune> getCountyList() {
        if (countyList == null) {
            countyList = kommunerForValghendelse();
        }
        return countyList;
    }

    protected List<Kommune> kommunerForValghendelse() {
        return getValggeografiService().kommunerForValghendelse(getUserData());
    }

    /**
     * Er velger registrert i samme kommune som stemmen skal avgis?
     */
    public boolean isVelgerEgenKommune(Voter voter) {
        return voter != null && voter.getMunicipalityId().equals(getPollingPlaceMvArea().getMunicipalityId());
    }

    /**
     * Returnerer formatert manntallsnummer på velger eller null dersom ingen velger.
     */
    public String getManntallsnummer() {
        Manntallsnummer manntallsnummer = manntallsSokWidget.getManntallsnummerObject();
        if (manntallsnummer == null) {
            return null;
        }
        return manntallsnummer.getKortManntallsnummerMedZeroPadding() + " " + manntallsnummer.getSluttsifre();
    }

    /**
     * Side tittel info.
     */
    public List<PageTitleMetaModel> getPageTitleMeta() {
        return getPageTitleMetaBuilder().area(getPollingPlaceMvArea());
    }

    /**
     * Er stemmestedet som er valgt et "Forhånd rett i urne"-stemmested?
     */
    public boolean isForhandsstemmeRettIUrne() {
        return getPollingPlaceMvArea() != null && getPollingPlaceMvArea().getPollingPlace().isAdvanceVoteInBallotBox();
    }

    protected PageTitleMetaBuilder getPageTitleMetaBuilder() {
        return pageTitleMetaBuilder;
    }

    boolean emptyVoterSearchResult() {
        return !voterFoundInSearch;
    }

    void onSelectedVoterClick(MvElection electionGroupAsMvElection) {
        setElectionGroupAsMvElection(electionGroupAsMvElection);
    }

    String getSelectedVotingCategoryId() {
        return getVotingCategory() != null
                ? getVotingCategory().getId()
                : "";
    }
}
