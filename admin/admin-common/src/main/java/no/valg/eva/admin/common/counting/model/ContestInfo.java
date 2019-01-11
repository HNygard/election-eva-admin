package no.valg.eva.admin.common.counting.model;

import java.io.Serializable;

import no.evote.constants.AreaLevelEnum;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;

import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Contains key parameters for Contest
 */
public class ContestInfo implements Serializable {
	private final ElectionPath electionPath;
	private final String electionName;
	private final String contestName;
	private final AreaPath areaPath;

	public ContestInfo(String electionPath, String electionName, String contestName, String areaPath) {
		this.electionPath = ElectionPath.from(electionPath);
		this.electionName = electionName;
		this.contestName = contestName;
		this.areaPath = areaPath != null ? AreaPath.from(areaPath) : null;
	}

	public ContestInfo(ElectionPath electionPath, String electionName, String contestName, AreaPath areaPath) {
		this.electionPath = electionPath;
		this.electionName = electionName;
		this.contestName = contestName;
		this.areaPath = areaPath;
	}

	public ElectionPath getElectionPath() {
		return electionPath;
	}

	public String getElectionName() {
		return electionName;
	}

	public String getContestName() {
		return contestName;
	}

	public AreaLevelEnum getAreaLevel() {
		return areaPath.getLevel();
	}

	public AreaPath getAreaPath() {
		return areaPath;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		ContestInfo that = (ContestInfo) o;

		if (areaPath != null ? !areaPath.equals(that.areaPath) : that.areaPath != null) {
			return false;
		}
		if (contestName != null ? !contestName.equals(that.contestName) : that.contestName != null) {
			return false;
		}
		if (electionName != null ? !electionName.equals(that.electionName) : that.electionName != null) {
			return false;
		}
		if (electionPath != null ? !electionPath.equals(that.electionPath) : that.electionPath != null) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder()
				.append(electionPath)
				.append(electionName)
				.append(contestName)
				.append(areaPath)
				.toHashCode();
	}

	@Override
	public String toString() {
		return "ContestInfo{"
				+ "electionPath=" + electionPath
				+ ", electionName='" + electionName + '\''
				+ ", contestName='" + contestName + '\''
				+ ", areaPath=" + areaPath
				+ '}';
	}
}
