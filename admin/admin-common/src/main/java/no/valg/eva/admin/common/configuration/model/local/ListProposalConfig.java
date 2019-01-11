package no.valg.eva.admin.common.configuration.model.local;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.VersionedObject;
import no.valg.eva.admin.common.configuration.model.Displayable;
import no.valg.eva.admin.common.configuration.model.central.ContestListProposalData;

public class ListProposalConfig extends VersionedObject implements Displayable {
	private final AreaPath areaPath;
	private final long contestPk;
	private final String contestName;
	private final ContestListProposalData contestListProposalData;
	private final boolean countStarted;
	private final boolean singleArea;
	private List<ListProposalConfig> children = new ArrayList<>();

	public ListProposalConfig(AreaPath areaPath, long contestPk, String contestName, boolean singleArea,
			boolean countStarted, ContestListProposalData contestListProposalData, int version) {
		super(version);
		this.areaPath = areaPath;
		this.contestPk = contestPk;
		this.contestName = contestName;
		this.singleArea = singleArea;
		this.contestListProposalData = contestListProposalData;
		this.countStarted = countStarted;
	}

	@Override
	public String display() {
		return "";
	}

	public boolean isValid() {
		boolean valid = contestListProposalData.isValid();
		if (!valid || children.isEmpty()) {
			return valid;
		}
		for (ListProposalConfig child : children) {
			if (!child.isValid()) {
				return false;
			}
		}
		return true;
	}

	public AreaPath getAreaPath() {
		return areaPath;
	}

	public long getContestPk() {
		return contestPk;
	}

	public String getElectionName() {
		return contestListProposalData.getElection().getName();
	}

	public String getContestName() {
		return contestName;
	}

	public boolean isSingleArea() {
		return singleArea;
	}

	public boolean isCountStarted() {
		return countStarted;
	}

	public ContestListProposalData getContestListProposalData() {
		return contestListProposalData;
	}

	public List<ListProposalConfig> getChildren() {
		return children;
	}

	public void setChildren(List<ListProposalConfig> children) {
		this.children = children;
	}

	public static Comparator<ListProposalConfig> areaComparator() {
		return (l1, l2) -> l1.getAreaPath().path().compareTo(l2.getAreaPath().path());
	}

	@Override
	public String toString() {
		return "ListProposal{"
				+ "areaPath=" + areaPath
				+ ", contestPk=" + contestPk
				+ '}';
	}
}
