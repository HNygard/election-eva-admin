package no.evote.service.configuration;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;

import no.evote.dto.ReportingUnitTypeDto;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.configuration.domain.model.ReportingUnitType;
import no.valg.eva.admin.configuration.repository.MvElectionReportingUnitsRepository;
import no.valg.eva.admin.configuration.repository.MvElectionRepository;
import no.valg.eva.admin.configuration.repository.ReportingUnitRepository;

/**
 * 
 */
public class ReportingUnitTypeServiceBean {

	@Inject
	private MvElectionRepository mvElectionRepository;
	@Inject
	private ReportingUnitRepository reportingUnitRepository;
	@Inject
	private MvElectionReportingUnitsRepository mvElectionReportingUnitsRepository;

	public List<ReportingUnitTypeDto> populateReportingUnitTypeDto(String electionEventId) {
		List<ReportingUnitTypeDto> reportingUnitTypeDtoList = new ArrayList<>();
		for (ReportingUnitType rut : reportingUnitRepository.findAllReportingUnitTypes()) {
			ReportingUnitTypeDto rutDto = new ReportingUnitTypeDto();
			List<MvElection> selectedElections = new ArrayList<>();

			List<MvElection> mvElectionList = mvElectionRepository.findByPathAndLevel(electionEventId, rut.getElectionLevel());
			for (MvElection mvElection : mvElectionList) {
				if (!mvElectionReportingUnitsRepository.findMvElectionReportingUnitByElectionAndType(rut.getPk(), mvElection.getPk()).isEmpty()) {
					selectedElections.add(mvElectionReportingUnitsRepository.findMvElectionReportingUnitByElectionAndType(rut.getPk(),
							mvElection.getPk()).get(0).getMvElection());
				}
			}
			rutDto.setElections(mvElectionList);
			rutDto.setSelectedElections(selectedElections);
			rutDto.setId(rut.getId());
			rutDto.setName(rut.getName());
			rutDto.setReportingUnitTypePk(rut.getPk());
			rutDto.setElectionLevel(rut.getElectionLevel());
			reportingUnitTypeDtoList.add(rutDto);
		}
		Collections.sort(reportingUnitTypeDtoList, new SortById());
		return reportingUnitTypeDtoList;
	}

	private static class SortById implements Comparator<ReportingUnitTypeDto>, Serializable {
		@Override
		public int compare(ReportingUnitTypeDto o1, ReportingUnitTypeDto o2) {
			return Integer.valueOf(o1.getId()).compareTo(o2.getId());
		}
	}

}
