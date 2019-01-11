package no.valg.eva.admin.common.rbac;

import static java.util.Objects.requireNonNull;

import java.io.Serializable;

import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.PollingPlaceArea;
import no.valg.eva.admin.common.configuration.model.election.Contest;

public class RoleAssociation implements Serializable {
	private RoleItem role;
	private PollingPlaceArea area;
	private ElectionPath electionPath;
	private Contest contest;

	public RoleAssociation(RoleItem role, PollingPlaceArea area) {
		requireNonNull(role);
		requireNonNull(area);

		this.role = role;
		this.area = area;
	}

	public RoleItem getRole() {
		return role;
	}

	public PollingPlaceArea getArea() {
		return area;
	}

	public ElectionPath getElectionPath() {
		return electionPath;
	}

	public void setElectionPath(ElectionPath electionPath) {
		this.electionPath = electionPath;
	}

	public Contest getContest() {
		return contest;
	}

	public void setContest(Contest contest) {
		this.contest = contest;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof RoleAssociation)) {
			return false;
		}

		RoleAssociation that = (RoleAssociation) o;

		if (!area.equals(that.area)) {
			return false;
		}
		if (!role.equals(that.role)) {
			return false;
		}
		if (electionPath == null) {
			if (that.electionPath != null) {
				return false;
			}
		} else {
			if (that.electionPath == null) {
				return false;
			} else if (!electionPath.equals(that.electionPath)) {
				return false;
			}
		}

		return true;
	}

	@Override
	public int hashCode() {
		int result = role.hashCode();
		result = 31 * result + area.hashCode();
		return result;
	}
}
