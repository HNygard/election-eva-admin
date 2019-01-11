package no.valg.eva.admin.valgnatt.application.service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import no.valg.eva.admin.common.counting.model.valgnatt.Valgnattrapportering;
import no.valg.eva.admin.common.counting.model.valgnatt.Valgnattrapportering.ValgnattrapportPk;
import no.valg.eva.admin.valgnatt.domain.model.valgnattrapport.Valgnattrapport;

import org.apache.commons.lang3.builder.CompareToBuilder;

final class ValgnattrapportMapper {

	private ValgnattrapportMapper() {
		// Ikke mulig å instansiere denne utenfra
	}

	static List<Valgnattrapportering> toSortedValgnattrapportingList(List<Valgnattrapport> valgnattrapportList) {
		List<Valgnattrapportering> valgnattrapporteringList = toValgnattrapporteringList(valgnattrapportList);
		valgnattrapporteringList.sort(getValgnattrapporteringComparator());
		return valgnattrapporteringList;
	}

	static List<Valgnattrapportering> toValgnattrapporteringList(List<Valgnattrapport> valgnattrapportList) {
		List<Valgnattrapportering> valgnattSkjemaList = valgnattrapportList.stream().map(valgnattrapport -> new Valgnattrapportering(
				valgnattrapport.getMvArea() != null ? valgnattrapport.getMvArea().areaPath() : null,
				valgnattrapport.getMvArea() != null ? valgnattrapport.getMvArea().getAreaName() : null,
				valgnattrapport.getContest() != null ? valgnattrapport.getContest().electionPath() : null,
				valgnattrapport.getElection() != null ? valgnattrapport.getElection().electionPath() : null,
				valgnattrapport.getReportType(),
				new ValgnattrapportPk(valgnattrapport.getPk()),
				valgnattrapport.isReadyForReport(),
				valgnattrapport.getAuditTimestamp(),
				valgnattrapport.getStatus().name())).collect(Collectors.toList());

		return valgnattSkjemaList;
	}

	private static Comparator<Valgnattrapportering> getValgnattrapporteringComparator() {
		return (o1, o2) -> new CompareToBuilder()
				.append(o2.kanRapporteres(), o1.kanRapporteres())
				.append(o1.getAreaPath().path(), o2.getAreaPath().path())
				.toComparison();
	}
}
