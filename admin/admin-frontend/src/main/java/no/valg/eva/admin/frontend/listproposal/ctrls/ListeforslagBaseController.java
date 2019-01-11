package no.valg.eva.admin.frontend.listproposal.ctrls;

import static no.valg.eva.admin.common.configuration.status.ElectionEventStatusEnum.CENTRAL_CONFIGURATION;
import static no.valg.eva.admin.common.configuration.status.ElectionEventStatusEnum.LOCAL_CONFIGURATION;
import static no.valg.eva.admin.felles.valghierarki.model.ValghierarkiNivaa.VALGDISTRIKT;
import static no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerElement.hierarki;
import static no.valg.eva.admin.frontend.kontekstvelger.valghierarki.ValghierarkiTjeneste.LISTEFORSLAG;

import java.util.List;

import javax.faces.application.FacesMessage;
import javax.inject.Inject;

import no.evote.constants.EvoteConstants;
import no.valg.eva.admin.frontend.util.MessageUtil;
import no.evote.service.configuration.AffiliationService;
import no.evote.service.configuration.LegacyContestService;
import no.evote.service.counting.ContestReportService;
import no.valg.eva.admin.common.configuration.status.ElectionEventStatusEnum;
import no.valg.eva.admin.configuration.domain.model.Affiliation;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.ElectionEventStatus;
import no.valg.eva.admin.configuration.domain.model.ElectionType;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.frontend.common.PageTitleMetaModel;
import no.valg.eva.admin.frontend.common.ctrls.RedirectInfo;
import no.valg.eva.admin.frontend.kontekstvelger.Kontekst;
import no.valg.eva.admin.frontend.kontekstvelger.KontekstAvhengigController;
import no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerOppsett;

public class ListeforslagBaseController extends KontekstAvhengigController {

    @Inject
    private LegacyContestService contestService;
    @Inject
    private ContestReportService contestReportService;
    @Inject
    private AffiliationService affiliationService;

    private MvElection mvElectionContest;
    private Contest contest;
    private boolean contestLocked;
    private String denneSideURL;

    @Override
    public KontekstvelgerOppsett getKontekstVelgerOppsett() {
        KontekstvelgerOppsett oppsett = new KontekstvelgerOppsett();
        oppsett.leggTil(hierarki(VALGDISTRIKT).medTjeneste(LISTEFORSLAG));
        return oppsett;
    }

    @Override
    public void initialized(Kontekst kontekst) {
        denneSideURL = getPageURL();
        contest = contestService.findByElectionPath(getUserData(), kontekst.getValghierarkiSti().electionPath());
        mvElectionContest = getMvElectionService().findSingleByPath(kontekst.getValghierarkiSti());
        updateContestLocked();
    }

    void redirectTilRediger(Affiliation affiliation, String page) {
        leggRedirectInfoPaSession(new RedirectInfo(affiliation, null, null));
        String redirectTil = getDenneSideURL().replace(page, "editListProposal.xhtml");
        redirectTo(redirectTil);
    }

    private void updateContestLocked() {
        ElectionEventStatus electionEventStatus = contest.getElection().getElectionGroup().getElectionEvent().getElectionEventStatus();
        boolean electionEventLocked = isElectionEventLocked(electionEventStatus);

        boolean contestCountBegun = contestReportService.hasContestReport(getUserData(), contest.getPk());

        boolean contestNotConfigured = isContestNotConfigured(contest);

        boolean isNotCalculatedElectionType = !isElectionTypeCalculated(contest.getElection().getElectionType());

        if (electionEventLocked) {
            MessageUtil.buildDetailMessage(getMessageProvider().get("@listProposal.lockedBeacuaseOfStatus"), FacesMessage.SEVERITY_WARN);
        }
        if (contestCountBegun) {
            MessageUtil.buildDetailMessage(getMessageProvider().get("@listProposal.lockedBeacuaseOfCountStarted"), FacesMessage.SEVERITY_WARN);
        }
        if (contestNotConfigured) {
            MessageUtil.buildDetailMessage(getMessageProvider().get("@listProposal.contestNotConfigured"), FacesMessage.SEVERITY_WARN);
        }
        if (isNotCalculatedElectionType) {
            MessageUtil.buildDetailMessage(getMessageProvider().get("@listProposal.lockedBeacuaseNotCorrectElectionType"), FacesMessage.SEVERITY_WARN);
        }

        contestLocked = electionEventLocked || contestCountBegun || contestNotConfigured || isNotCalculatedElectionType;
    }

    boolean isElectionEventLocked(ElectionEventStatus electionEventStatus) {
        return !(isCentralConfiguration(electionEventStatus) || isLocalConfiguration(electionEventStatus));
    }

    boolean isElectionTypeCalculated(ElectionType electionType) {
        return EvoteConstants.ELECTION_TYPE_CALCULATED.equals(electionType.getId());
    }

    boolean isContestNotConfigured(Contest contest) {
        return contest.getMaxCandidates() == null
                || contest.getMinCandidates() == null
                || contest.getMinProposersNewParty() == null
                || contest.getMinProposersOldParty() == null;
    }

    private boolean isLocalConfiguration(ElectionEventStatus electionEventStatus) {
        return hasConfigurationStatus(electionEventStatus, LOCAL_CONFIGURATION);
    }

    private boolean isCentralConfiguration(ElectionEventStatus electionEventStatus) {
        return hasConfigurationStatus(electionEventStatus, CENTRAL_CONFIGURATION);
    }

    private boolean hasConfigurationStatus(ElectionEventStatus electionEventStatus, ElectionEventStatusEnum centralConfiguration) {
        return electionEventStatus.getId() == centralConfiguration.id();
    }

    public List<PageTitleMetaModel> getPageTitleMeta() {
        return getPageTitleMetaBuilder().election(mvElectionContest);
    }

    AffiliationService getAffiliationService() {
        return affiliationService;
    }

    String getDenneSideURL() {
        return denneSideURL;
    }

    public Contest getContest() {
        return contest;
    }

    public boolean isContestLocked() {
        return contestLocked;
    }
}
