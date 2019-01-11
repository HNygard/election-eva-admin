package no.valg.eva.admin.opptelling.domain.auditevent;

import static no.valg.eva.admin.common.counting.model.CountCategory.FO;
import static no.valg.eva.admin.common.counting.model.CountCategory.VO;
import static no.valg.eva.admin.felles.konfigurasjon.model.Styretype.FYLKESVALGSTYRET;
import static no.valg.eva.admin.felles.konfigurasjon.model.Styretype.VALGSTYRET;
import static no.valg.eva.admin.felles.test.data.sti.valggeografi.KommuneStiTestData.KOMMUNE_STI;
import static no.valg.eva.admin.felles.test.data.sti.valghierarki.ValgStiTestData.VALG_STI;
import static org.assertj.core.api.Assertions.assertThat;

import no.valg.eva.admin.common.auditlog.AuditEventFactory;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.AuditLogTestsObjectMother;
import no.valg.eva.admin.common.auditlog.AuditedObjectSource;
import no.valg.eva.admin.common.auditlog.Outcome;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.felles.konfigurasjon.model.Styretype;
import no.valg.eva.admin.felles.sti.valggeografi.ValggeografiSti;
import no.valg.eva.admin.felles.sti.valghierarki.ValghierarkiSti;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class OpptellingerAuditEventTest {
	private final AuditLogTestsObjectMother objectMother = new AuditLogTestsObjectMother();

	@Test(dataProvider = "toJsonTestData")
	public void toJson_gittTestData_girKorrektJson(CountCategory[] countCategories, Styretype[] styretyper, String json) throws Exception {
		OpptellingerAuditEvent auditEvent = new OpptellingerAuditEvent(objectMother.createUserData(), VALG_STI, KOMMUNE_STI, countCategories, styretyper,
				AuditEventTypes.DeletedAllInArea, Outcome.Success, null);

		assertThat(auditEvent.toJson()).isEqualTo(json);
	}

	@DataProvider
	public Object[][] toJsonTestData() {
		CountCategory[] countCategories = {FO, VO};
		Styretype[] styretyper = {FYLKESVALGSTYRET, VALGSTYRET};
		return new Object[][]{
				{null, null, "{\"valghierarkiSti\":\"111111.11.11\",\"valggeografiSti\":\"111111.11.11.1111\"}"},
				{countCategories, null, "{\"valghierarkiSti\":\"111111.11.11\",\"valggeografiSti\":\"111111.11.11.1111\","
						+ "\"countCategories\":\"[FO, VO]\"}"},
				{null, styretyper, "{\"valghierarkiSti\":\"111111.11.11\",\"valggeografiSti\":\"111111.11.11.1111\","
						+ "\"styretyper\":\"[FYLKESVALGSTYRET, VALGSTYRET]\"}"},
				{countCategories, styretyper, "{\"valghierarkiSti\":\"111111.11.11\",\"valggeografiSti\":\"111111.11.11.1111\","
						+ "\"countCategories\":\"[FO, VO]\",\"styretyper\":\"[FYLKESVALGSTYRET, VALGSTYRET]\"}"}
		};
	}

	@Test
	public void objectClasses_gittAuditEvent_returnererKlasserForConstructor() {
		assertThat(OpptellingerAuditEvent.objectClasses(AuditEventTypes.DeletedAllInArea))
				.isEqualTo(new Class[]{ValghierarkiSti.class, ValggeografiSti.class, CountCategory[].class, Styretype[].class});
	}

	@Test
	public void constructor_maaOverholdeKravFraAuditInterceptor() throws NoSuchMethodException {
		assertThat(AuditEventFactory.getAuditEventConstructor(OpptellingerAuditEvent.class,
				OpptellingerAuditEvent.objectClasses(AuditEventTypes.DeletedAllInArea), AuditedObjectSource.Parameters)).isNotNull();
	}
}
