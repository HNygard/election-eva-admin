package no.valg.eva.admin.common.configuration.model.election;

import java.util.ArrayList;
import java.util.List;

import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.VersionedObject;
import no.valg.eva.admin.common.configuration.model.central.ContestListProposalData;
import no.valg.eva.admin.common.configuration.status.ContestStatus;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDate;

public class Contest extends VersionedObject {

	private Long pk;
	private String id;
	private String name;
	private ContestStatus contestStatus;
	private Boolean penultimateRecount;
	private LocalDate endDateOfBirth;
	private ContestListProposalData listProposalData;
	private List<AreaPath> contestAreas = new ArrayList<>();

	public Contest(Election parent) {
		this(null, null, null, null, null, null, new ContestListProposalData(parent), 0);
	}

	public Contest(Long pk, String id, String name, ContestStatus contestStatus, Boolean penultimateRecount, LocalDate endDateOfBirth,
			ContestListProposalData listProposalData, int version) {
		super(version);
		this.pk = pk;
		this.id = id;
		this.name = name;
		this.contestStatus = contestStatus;
		this.penultimateRecount = penultimateRecount;
		this.endDateOfBirth = endDateOfBirth;
		this.listProposalData = listProposalData;
	}

	public ElectionPath getParentElectionPath() {
		return listProposalData.getElection().getElectionPath();
	}

	public ElectionPath getElectionPath() {
		if (StringUtils.isEmpty(id)) {
			throw new RuntimeException("ElectionPath could not be determined");
		}
		return getParentElectionPath().add(id);
	}

	public Long getPk() {
		return pk;
	}

	public ContestStatus getContestStatus() {
		return contestStatus;
	}

	public ContestListProposalData getListProposalData() {
		return listProposalData;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Boolean getPenultimateRecount() {
		return penultimateRecount;
	}

	public void setPenultimateRecount(Boolean penultimateRecount) {
		this.penultimateRecount = penultimateRecount;
	}

	public LocalDate getEndDateOfBirth() {
		return endDateOfBirth;
	}

	public void setEndDateOfBirth(LocalDate endDateOfBirth) {
		this.endDateOfBirth = endDateOfBirth;
	}

	public List<AreaPath> getContestAreas() {
		return contestAreas;
	}
}
