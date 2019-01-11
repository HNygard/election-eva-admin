package no.valg.eva.admin.frontend.rbac.ctrls;

import static no.evote.constants.ElectionLevelEnum.CONTEST;
import static no.evote.constants.ElectionLevelEnum.ELECTION;
import static no.evote.constants.ElectionLevelEnum.ELECTION_GROUP;
import static org.apache.commons.lang3.StringUtils.isEmpty;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import no.evote.constants.ElectionLevelEnum;
import no.evote.dto.MvElectionMinimal;
import no.evote.security.UserData;
import no.evote.service.configuration.MvElectionService;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.rbac.RoleItem;

@Named
@ViewScoped
public class ElectionOptions implements Serializable, Converter {
	@Inject
	private UserData userData;
	@Inject
	private MvElectionService mvElectionService;

	private ElectionLevelEnum electionLevel;

	private MvElectionMinimal selectedElectionGroup;
	private List<MvElectionMinimal> electionGroups;

	private MvElectionMinimal selectedElection;
	private List<MvElectionMinimal> elections;

	private MvElectionMinimal selectedContest;
	private List<MvElectionMinimal> contests;

	public void init(RoleItem roleItem) {
		selectedElectionGroup = null;
		selectedElection = null;
		selectedContest = null;
		electionLevel = roleItem.getElectionLevel();
		if (electionLevel != null) {
			// Start collecting from ElectionGroup
			electionGroups = sort(
					mvElectionService.findByPathAndLevel(userData, userData.getOperatorMvElection().getElectionEvent().electionPath(), ELECTION_GROUP));
			if (electionGroups.size() == 1) {
				setSelectedElectionGroup(electionGroups.get(0));
			}
		}
	}

	public ElectionPath getElectionPath() {
		if (isRender() && isReady()) {
			switch (electionLevel) {
			case ELECTION_GROUP:
				return selectedElectionGroup.toElectionPath();
			case ELECTION:
				return selectedElection.toElectionPath();
			case CONTEST:
				return selectedContest.toElectionPath();
			default:
				return null;
			}
		}
		return null;
	}

	public boolean isReady() {
		if (!isRender()) {
			return true;
		}
		switch (electionLevel) {
		case ELECTION_GROUP:
			return selectedElectionGroup != null;
		case ELECTION:
			return selectedElection != null;
		case CONTEST:
			return selectedContest != null;
		default:
			return false;
		}
	}

	public boolean isRender() {
		return electionLevel != null;
	}

	public boolean isRenderSelect(ElectionLevelEnum level) {
		if (!isRender() || electionLevel.isLowerThan(level)) {
			return false;
		}
		switch (level) {
		case ELECTION_GROUP:
			return electionGroups != null && electionGroups.size() > 1;
		case ELECTION:
			return elections != null && elections.size() > 1;
		case CONTEST:
			return contests != null && contests.size() > 1;
		default:
			return false;
		}
	}

	@Override
	public Object getAsObject(FacesContext facesContext, UIComponent uiComponent, String s) {
		if (isEmpty(s)) {
			return null;
		}
		try {
			ElectionPath electionPath = ElectionPath.from(s);
			if (electionPath.getLevel() == ELECTION_GROUP) {
				return findByPath(electionPath, electionGroups);
			} else if (electionPath.getLevel() == ELECTION) {
				return findByPath(electionPath, elections);
			} else if (electionPath.getLevel() == CONTEST) {
				return findByPath(electionPath, contests);
			}
		} catch (Exception e) {
			return null;
		}
		return null;
	}

	@Override
	public String getAsString(FacesContext facesContext, UIComponent uiComponent, Object o) {
		if (o == null || !MvElectionMinimal.class.isAssignableFrom(o.getClass())) {
			return null;
		}
		return ((MvElectionMinimal) o).getPath();
	}

	private MvElectionMinimal findByPath(ElectionPath electionPath, List<MvElectionMinimal> list) {
		for (MvElectionMinimal e : list) {
			if (electionPath.equals(e.toElectionPath())) {
				return e;
			}
		}
		return null;
	}

	private void collectElections() {
		if (!electionLevel.isLowerThan(ELECTION) && getSelectedElectionGroup() != null) {
			elections = sort(mvElectionService.findByPathAndLevel(userData, getSelectedElectionGroup().toElectionPath(), ELECTION));
			if (elections.size() == 1) {
				setSelectedElection(elections.get(0));
			}
		}
	}

	private void collectContests() {
		if (!electionLevel.isLowerThan(CONTEST) && getSelectedElection() != null) {
			contests = sort(mvElectionService.findByPathAndLevel(userData, getSelectedElection().toElectionPath(), CONTEST));
			if (contests.size() == 1) {
				setSelectedContest(contests.get(0));
			}
		}
	}

	private List<MvElectionMinimal> sort(List<MvElectionMinimal> list) {
		return list.stream().sorted((o1, o2) -> o1.getName().compareTo(o2.getName())).collect(Collectors.toList());
	}

	public List<MvElectionMinimal> getElectionGroups() {
		return electionGroups;
	}

	public List<MvElectionMinimal> getElections() {
		return elections;
	}

	public List<MvElectionMinimal> getContests() {
		return contests;
	}

	public MvElectionMinimal getSelectedElectionGroup() {
		return selectedElectionGroup;
	}

	public void setSelectedElectionGroup(MvElectionMinimal selectedElectionGroup) {
		this.selectedElectionGroup = selectedElectionGroup;
		if (selectedElectionGroup == null) {
			elections = new ArrayList<>();
			contests = new ArrayList<>();
		} else {
			collectElections();
		}
	}

	public MvElectionMinimal getSelectedElection() {
		return selectedElection;
	}

	public void setSelectedElection(MvElectionMinimal selectedElection) {
		this.selectedElection = selectedElection;
		if (selectedElection == null) {
			contests = new ArrayList<>();
		} else {
			collectContests();
		}
	}

	public MvElectionMinimal getSelectedContest() {
		return selectedContest;
	}

	public void setSelectedContest(MvElectionMinimal selectedContest) {
		this.selectedContest = selectedContest;
	}
}
