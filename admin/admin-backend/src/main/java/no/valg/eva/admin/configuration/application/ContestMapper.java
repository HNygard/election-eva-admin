package no.valg.eva.admin.configuration.application;

import javax.inject.Inject;

import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.configuration.model.central.ContestListProposalData;
import no.valg.eva.admin.common.configuration.model.election.Election;
import no.valg.eva.admin.common.configuration.model.local.ListProposalConfig;
import no.valg.eva.admin.common.configuration.status.ContestStatus;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.counting.repository.ContestReportRepository;

public class ContestMapper {

	private ElectionMapper electionMapper;
	private ContestReportRepository contestReportRepository;

	@Inject
	public ContestMapper(ContestReportRepository contestReportRepository, ElectionMapper electionMapper) {
		this.contestReportRepository = contestReportRepository;
		this.electionMapper = electionMapper;
	}

	public no.valg.eva.admin.common.configuration.model.election.Contest toCommon(Contest dbContest) {
		return new no.valg.eva.admin.common.configuration.model.election.Contest(
				dbContest.getPk(),
				dbContest.getId(),
				dbContest.getName(),
				ContestStatus.fromId(dbContest.getContestStatus().getId()),
				dbContest.getPenultimateRecount(),
				dbContest.getEndDateOfBirth(),
				toListProposalData(dbContest),
				dbContest.getAuditOplock());
	}

	public void updateEntity(Contest dbContest, no.valg.eva.admin.common.configuration.model.election.Contest common) {
		dbContest.setId(common.getId());
		dbContest.setName(common.getName());
		dbContest.setEndDateOfBirth(common.getEndDateOfBirth());
		dbContest.setPenultimateRecount(common.getPenultimateRecount());
		updateEntityFromListProposalData(dbContest, common.getListProposalData());
	}

	public ListProposalConfig toListProposalConfig(AreaPath areaPath, Contest dbContest) {
		ContestListProposalData data = toListProposalData(dbContest);
		return new ListProposalConfig(areaPath, dbContest.getPk(), dbContest.getName(), dbContest.isSingleArea(),
				contestReportRepository.hasContestReport(dbContest.getPk()), data, dbContest.getAuditOplock());
	}

	public void updateEntityFromListProposalData(Contest dbContest, ContestListProposalData listProposalData) {
		dbContest.setMaxCandidates(listProposalData.getMaxCandidates());
		dbContest.setMaxRenumber(listProposalData.getMaxRenumber());
		dbContest.setMaxWriteIn(listProposalData.getMaxWriteIn());
		dbContest.setMinCandidates(listProposalData.getMinCandidates());
		dbContest.setMinProposersNewParty(listProposalData.getMinProposersNewParty());
		dbContest.setMinProposersOldParty(listProposalData.getMinProposersOldParty());
		dbContest.setNumberOfPositions(listProposalData.getNumberOfPositions());
	}

	private ContestListProposalData toListProposalData(Contest dbContest) {
		Election election = electionMapper.toCommonObject(dbContest.getElection());
		return new ContestListProposalData(
				election,
				dbContest.getMinProposersNewParty(),
				dbContest.getMinProposersOldParty(),
				dbContest.getMinCandidates(),
				dbContest.getMaxCandidates(),
				dbContest.getNumberOfPositions(),
				dbContest.getMaxWriteIn(),
				dbContest.getMaxRenumber());
	}
}
