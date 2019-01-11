package no.valg.eva.admin.counting.domain.auditevents;

import static no.valg.eva.admin.common.test.data.AreaPathTestData.AREA_PATH_111111_11_11_1111;
import static no.valg.eva.admin.common.test.data.ElectionPathTestData.ELECTION_PATH_111111_11;
import static no.valg.eva.admin.common.test.data.ElectionPathTestData.ELECTION_PATH_111111_11_11_111111;
import static no.valg.eva.admin.common.test.data.ElectionPathTestData.ELECTION_PATH_111111_11_11_111112;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;

import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.auditlog.AuditEventFactory;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.AuditLogTestsObjectMother;
import no.valg.eva.admin.common.auditlog.AuditedObjectSource;
import no.valg.eva.admin.common.auditlog.Outcome;
import no.valg.eva.admin.common.counting.model.AntallStemmesedlerLagtTilSide;
import no.valg.eva.admin.common.counting.model.AntallStemmesedlerLagtTilSideForValg;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class AntallStemmesedlerLagtTilSideAuditEventTest {
	private final AuditLogTestsObjectMother objectMother = new AuditLogTestsObjectMother();

	@Test(dataProvider = "toJsonTestData")
	public void toJson_gittTestData_girKorrektJson(AntallStemmesedlerLagtTilSide antallStemmesedlerLagtTilSide, String json) throws Exception {
		AntallStemmesedlerLagtTilSideAuditEvent auditEvent = new AntallStemmesedlerLagtTilSideAuditEvent(objectMother.createUserData(),
				antallStemmesedlerLagtTilSide, AuditEventTypes.Save, Outcome.Success, null);
		assertThat(auditEvent.toJson()).isEqualTo(json);
	}

	@DataProvider
	public Object[][] toJsonTestData() {
		return new Object[][]{
				{antallStemmesedlerLagtTilSide(antallStemmesedlerLagtTilSideForValgList()),
						"{\"kommuneSti\":\"111111.11.11.1111\",\"antallStemmesedlerLagtTilSideForValg\":["
								+ "{\"valghierarkiSti\":\"111111.11.11.111111\",\"navn\":\"VALGDISTRIKT1\",\"antallStemmesedler\":20},"
								+ "{\"valghierarkiSti\":\"111111.11.11.111112\",\"navn\":\"VALGDISTRIKT2\",\"antallStemmesedler\":30}]}"},
				{antallStemmesedlerLagtTilSide(antallStemmesedlerLagtTilSideForValg(ELECTION_PATH_111111_11, "VALGGRUPPE", 10)),
						"{\"kommuneSti\":\"111111.11.11.1111\",\"antallStemmesedlerLagtTilSideForValg\":["
								+ "{\"valghierarkiSti\":\"111111.11\",\"navn\":\"VALGGRUPPE\",\"antallStemmesedler\":10}]}"},
		};
	}

	private AntallStemmesedlerLagtTilSide antallStemmesedlerLagtTilSide(List<AntallStemmesedlerLagtTilSideForValg> antallStemmesedlerLagtTilSideForValgList) {
		return new AntallStemmesedlerLagtTilSide(AREA_PATH_111111_11_11_1111, antallStemmesedlerLagtTilSideForValgList, true);
	}

	private AntallStemmesedlerLagtTilSide antallStemmesedlerLagtTilSide(AntallStemmesedlerLagtTilSideForValg antallStemmesedlerLagtTilSideForValg) {
		return new AntallStemmesedlerLagtTilSide(AREA_PATH_111111_11_11_1111, antallStemmesedlerLagtTilSideForValg, true);
	}

	private List<AntallStemmesedlerLagtTilSideForValg> antallStemmesedlerLagtTilSideForValgList() {
		return Arrays.asList(antallStemmesedlerLagtTilSideForValg(ELECTION_PATH_111111_11_11_111111, "VALGDISTRIKT1", 20),
				antallStemmesedlerLagtTilSideForValg(ELECTION_PATH_111111_11_11_111112, "VALGDISTRIKT2", 30));
	}

	private AntallStemmesedlerLagtTilSideForValg antallStemmesedlerLagtTilSideForValg(ElectionPath electionPath, String navn, int antallStemmesedler) {
		return new AntallStemmesedlerLagtTilSideForValg(electionPath, navn, antallStemmesedler);
	}

	@Test
	public void objectClasses_gittAuditEvent_returnererKlasserForConstructor() {
		assertThat(AntallStemmesedlerLagtTilSideAuditEvent.objectClasses(AuditEventTypes.Save))
				.isEqualTo(new Class[]{AntallStemmesedlerLagtTilSide.class});
	}

	@Test
	public void constructor_maaOverholdeKravFraAuditInterceptor() throws NoSuchMethodException {
		assertThat(AuditEventFactory.getAuditEventConstructor(AntallStemmesedlerLagtTilSideAuditEvent.class,
				AntallStemmesedlerLagtTilSideAuditEvent.objectClasses(AuditEventTypes.Save), AuditedObjectSource.Parameters)).isNotNull();
	}

}
