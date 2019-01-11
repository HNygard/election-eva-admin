package no.valg.eva.admin.valgnatt.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import no.valg.eva.admin.common.counting.model.valgnatt.Valgnattrapportering;
import no.valg.eva.admin.configuration.domain.model.Election;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.counting.domain.model.report.ReportType;
import no.valg.eva.admin.valgnatt.domain.model.valgnattrapport.Valgnattrapport;
import no.valg.eva.admin.valgnatt.domain.model.valgnattrapport.ValgnattrapportStatus;

import org.testng.annotations.Test;


public class ValgnattrapportMapperTest {

	@Test
	public void toValgnattskjemaList_lagerSkjema() {
		List<Valgnattrapportering> valgnattrapporteringList = ValgnattrapportMapper.toValgnattrapporteringList(makeValgnattRapportList());
		assertThat(valgnattrapporteringList.get(0).getReportType()).isEqualTo(ReportType.GEOGRAFI_STEMMEBERETTIGEDE);
	}

	private List<Valgnattrapport> makeValgnattRapportList() {
		List<Valgnattrapport> valgnattrapportList = new ArrayList<>();
		valgnattrapportList.add(new Valgnattrapport(mock(Election.class, RETURNS_DEEP_STUBS), ReportType.GEOGRAFI_STEMMEBERETTIGEDE));
		return valgnattrapportList;
	}

	@Test
	public void toSortedValgnattrapportingList_givenUnorderedList_returnsOrderedList() {
		String areaPath1 = "150001.47.07.0701.070100.0001";
		String areaPath2 = "150001.47.07.0701.070100.0002";
		String areaPath3 = "150001.47.07.0701.070100.0003";
		String areaPath4 = "150001.47.07.0701.070100.0004";
		List<Valgnattrapport> actualValgnattrapportList = new ArrayList<>();
		actualValgnattrapportList.add(stubValgnattrapport(areaPath4, false));
		actualValgnattrapportList.add(stubValgnattrapport(areaPath3, true));
		actualValgnattrapportList.add(stubValgnattrapport(areaPath2, false));
		actualValgnattrapportList.add(stubValgnattrapport(areaPath1, true));

		List<Valgnattrapportering> actualValgnattrapporteringList = ValgnattrapportMapper.toSortedValgnattrapportingList(actualValgnattrapportList);

		assertThat(actualValgnattrapporteringList.get(0).kanRapporteres()).isTrue();
		assertThat(actualValgnattrapporteringList.get(1).kanRapporteres()).isTrue();
		assertThat(actualValgnattrapporteringList.get(2).kanRapporteres()).isFalse();
		assertThat(actualValgnattrapporteringList.get(3).kanRapporteres()).isFalse();
		assertThat(actualValgnattrapporteringList.get(0).getAreaPath().path()).isEqualTo(areaPath1);
		assertThat(actualValgnattrapporteringList.get(1).getAreaPath().path()).isEqualTo(areaPath3);
		assertThat(actualValgnattrapporteringList.get(2).getAreaPath().path()).isEqualTo(areaPath2);
		assertThat(actualValgnattrapporteringList.get(3).getAreaPath().path()).isEqualTo(areaPath4);
	}

	private Valgnattrapport stubValgnattrapport(String areaPath, boolean readyForReport) {
		Valgnattrapport valgnattrapport = mock(Valgnattrapport.class, RETURNS_DEEP_STUBS);
		MvArea mvArea = new MvArea();
		mvArea.setAreaPath(areaPath);

		when(valgnattrapport.getMvArea()).thenReturn(mvArea);
		when(valgnattrapport.getStatus()).thenReturn(ValgnattrapportStatus.NOT_SENT);
		when(valgnattrapport.isReadyForReport()).thenReturn(readyForReport);

		return valgnattrapport;
	}

}

