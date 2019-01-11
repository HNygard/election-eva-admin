package no.valg.eva.admin.counting.domain.event;

import static no.valg.eva.admin.common.counting.constants.ReportingUnitTypeId.VALGSTYRET;
import static no.valg.eva.admin.common.counting.model.CountQualifier.PRELIMINARY;
import static no.valg.eva.admin.common.counting.model.CountQualifier.PROTOCOL;

import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.counting.model.CountCategory;

import org.testng.annotations.Test;

public class TellingEndrerStatusTest {

	private static final AreaPath AN_AREA_PATH = AreaPath.from("150001.47.03.0301");
	private static final ElectionPath A_CONTEST_PATH = ElectionPath.from("150001.01.01.000001");
	private static final CountCategory A_CATEGORY = CountCategory.FO;

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void countQualifier_protocol_constructorKasterException() {
		new TellingEndrerStatus(AN_AREA_PATH, PROTOCOL, A_CONTEST_PATH, A_CATEGORY, VALGSTYRET);
	}
	
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void countQualifier_manglerStyretype_constructorKasterException() {
		new TellingEndrerStatus(AN_AREA_PATH, PROTOCOL, A_CONTEST_PATH, A_CATEGORY, null);
	}

	@Test
	public void countQualifier_preliminaryOgContestPath_constructorKasterIkkeException() {
		new TellingEndrerStatus(AN_AREA_PATH, PRELIMINARY, A_CONTEST_PATH, A_CATEGORY, VALGSTYRET);
	}
	
}
