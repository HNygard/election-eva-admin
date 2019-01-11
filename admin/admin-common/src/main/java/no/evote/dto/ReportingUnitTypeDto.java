package no.evote.dto;

import java.io.Serializable;
import java.util.List;

import no.valg.eva.admin.configuration.domain.model.MvElection;

public class ReportingUnitTypeDto implements Serializable {

	private long reportingUnitTypePk;
	private int id;
	private int electionLevel;
	private String name;
	private List<MvElection> elections;
	private List<MvElection> selectedElections;

	public String getMvElectionNameString() {
		StringBuilder mvElectionNameString = new StringBuilder("");
		List<MvElection> mveru = getSelectedElections();
		if (mveru != null && !mveru.isEmpty()) {
			for (MvElection mvElection : mveru) {
				if (mvElectionNameString.length() > 0) {
					mvElectionNameString.append(", ");
				}
				mvElectionNameString.append(mvElection.getNamedPath());
			}
		}
		return mvElectionNameString.toString();
	}

	public int getElectionLevel() {
		return electionLevel;
	}

	public void setElectionLevel(final int electionLevel) {
		this.electionLevel = electionLevel;
	}

	public String getElectionLevelName() {
		return "@election_level[" + electionLevel + "].name";
	}

	public int getId() {
		return id;
	}

	public void setId(final int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public List<MvElection> getElections() {
		return elections;
	}

	public void setElections(final List<MvElection> elections) {
		this.elections = elections;
	}

	public List<MvElection> getSelectedElections() {
		return selectedElections;
	}

	public void setSelectedElections(final List<MvElection> selectedElections) {
		this.selectedElections = selectedElections;
	}

	public long getReportingUnitTypePk() {
		return reportingUnitTypePk;
	}

	public void setReportingUnitTypePk(final long reportingUnitTypePk) {
		this.reportingUnitTypePk = reportingUnitTypePk;
	}

}
