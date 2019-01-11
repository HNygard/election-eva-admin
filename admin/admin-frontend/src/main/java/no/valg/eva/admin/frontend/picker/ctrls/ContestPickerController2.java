package no.valg.eva.admin.frontend.picker.ctrls;

import static com.google.common.collect.Collections2.filter;
import static no.evote.constants.ElectionLevelEnum.ELECTION_EVENT;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import no.valg.eva.admin.frontend.util.MessageUtil;
import no.evote.security.UserData;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.counting.comparators.ContestInfoOrderComparator;
import no.valg.eva.admin.common.counting.model.ContestInfo;
import no.valg.eva.admin.common.counting.service.ContestInfoService;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.frontend.BaseController;
import no.valg.eva.admin.frontend.i18n.MessageProvider;

import org.primefaces.event.TabChangeEvent;

/**
 * Used for selecting a specific contest.
 */
@Named
@ViewScoped
public class ContestPickerController2 extends BaseController {

	@Inject
	private UserData userData;
	@Inject
	private ContestInfoService contestInfoService;
	@Inject
	private MessageProvider messageProvider;

	private TabChangeListener tabChangeListener;
	private List<ContestInfo> contests = new ArrayList<>();
	private ContestInfo contestInfo;
	private int activeContest = 0;

	public interface TabChangeListener {
		void onTabChange(ContestInfo contestInfo);
	}

	public void initWithElectionsFromElectionEvent(ElectionPath electionEventPath) {
		electionEventPath.assertLevel(ELECTION_EVENT);
		this.contests = contestInfoService.electionsInElectionEvent(userData, electionEventPath);
		if (!contests.isEmpty()) {
			this.contestInfo = getContests().get(0);
		}
		setActiveContest(0);
	}

	public void init(MvArea mvArea, ElectionPath electionPath, boolean hideElectionLevel) {
		if (mvArea != null) {
			this.contests = contestInfoService.contestOrElectionByAreaPath(AreaPath.from(mvArea.getAreaPath()));
			if (hideElectionLevel) {
				this.contests = new ArrayList<>(filter(contests, input -> input.getElectionPath().getContestId() != null));
			}
			Collections.sort(this.contests, new ContestInfoOrderComparator());
			if (getContests().isEmpty()) {
				MessageUtil.buildDetailMessage(messageProvider.get("@settlement.error.wrong_level"), FacesMessage.SEVERITY_ERROR);
			}
		}
		if (getContests() != null && !getContests().isEmpty()) {
			if (electionPath == null) {
				this.contestInfo = getContests().get(0);
				setActiveContest(0);
			} else {
				for (int i = 0; i < getContests().size(); i++) {
					ContestInfo info = getContests().get(i);
					if (electionPath.path().equals(info.getElectionPath().path())) {
						this.contestInfo = info;
						setActiveContest(i);
						return;
					}
				}
			}
		}
	}

	public void initForSamiElectionCountyUser(ElectionPath electionPath) {
		this.contestInfo = contestInfoService.findContestInfoByPath(electionPath);
		contests = new ArrayList<>();
		contests.add(contestInfo);
		setActiveContest(0);
	}

	public void setTabChangeListener(TabChangeListener tabChangeListener) {
		this.tabChangeListener = tabChangeListener;
	}

	public void onTabChange(TabChangeEvent event) {
		this.contestInfo = (ContestInfo) event.getData();
		if (tabChangeListener != null) {
			tabChangeListener.onTabChange(contestInfo);
		}
	}

	public String getElectionLevelName(ContestInfo info) {
		if (info == null) {
			return null;
		}
		if (info.getElectionPath().getContestId() == null) {
			// Not contest (typically borough elections)
			return info.getElectionName();
		}
		return info.getElectionName() + " " + info.getContestName();
	}

	public ContestInfo getContestInfo() {
		return contestInfo;
	}

	public List<ContestInfo> getContests() {
		return contests;
	}

	public int getActiveContest() {
		return activeContest;
	}

	public void setActiveContest(int activeContest) {
		this.activeContest = activeContest;
	}
}
