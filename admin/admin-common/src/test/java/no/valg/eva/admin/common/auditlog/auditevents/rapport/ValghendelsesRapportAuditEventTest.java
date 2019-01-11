package no.valg.eva.admin.common.auditlog.auditevents.rapport;

import static com.jayway.jsonassert.JsonAssert.collectionWithSize;
import static no.valg.eva.admin.common.rapport.model.ReportCategory.GRUNNLAGSDATA;
import static no.valg.eva.admin.common.rapport.model.ReportCategory.MANNTALL;
import static no.valg.eva.admin.common.rapport.model.ReportCategory.MØTEBØKER;
import static no.valg.eva.admin.common.rbac.Accesses.Rapport_Grunnlagsdata_Brukere_Og_Roller_Kommune;
import static no.valg.eva.admin.common.rbac.Accesses.Rapport_Manntall_Partiservice_Kommune;
import static no.valg.eva.admin.common.rbac.Accesses.Rapport_Møtebøker_Bydelsutvalg;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import no.evote.security.UserData;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.Outcome;
import no.valg.eva.admin.common.auditlog.auditevents.AbstractAuditEventTest;
import no.valg.eva.admin.common.auditlog.auditevents.AuditEvent;
import no.valg.eva.admin.common.rapport.model.ReportCategory;
import no.valg.eva.admin.common.rapport.model.ValghendelsesRapport;
import no.valg.eva.admin.common.rbac.Access;

import org.testng.annotations.Test;

import com.jayway.jsonassert.JsonAssert;


public class ValghendelsesRapportAuditEventTest extends AbstractAuditEventTest {

	private static final ElectionPath ELECTION_EVENT_PATH = ElectionPath.from("111111");

	@Test
	public void objectType_returnsAdvancePollingPlace() throws Exception {
		ValghendelsesRapportAuditEvent event = event(AuditEventTypes.Save, ELECTION_EVENT_PATH, new ArrayList<>());

		assertThat(event.objectType()).isSameAs(ValghendelsesRapport.class);
	}

	@Test
	public void toJson() throws Exception {
		ValghendelsesRapportAuditEvent event = event(AuditEventTypes.Save, ELECTION_EVENT_PATH,
				Arrays.asList(
						rapport("report1", GRUNNLAGSDATA, Rapport_Grunnlagsdata_Brukere_Og_Roller_Kommune.getAccess(), true),
						rapport("report2", MANNTALL, Rapport_Manntall_Partiservice_Kommune.getAccess(), false),
						rapport("report3", MØTEBØKER, Rapport_Møtebøker_Bydelsutvalg.getAccess(), true)));
		JsonAssert
				.with(event.toJson())
				.assertThat("$", hasEntry("electionEventPath", ELECTION_EVENT_PATH.path()))
				.assertThat("$.reports[*]", collectionWithSize(equalTo(3)))
				.assertThat("$.reports[*].reportId", containsInAnyOrder("report1", "report2", "report3"))
				.assertThat("$.reports[*].visible", containsInAnyOrder("true", "false", "true"));
	}

	@Override
	protected Class<? extends AuditEvent> getAuditEventClass() {
		return ValghendelsesRapportAuditEvent.class;
	}

	private ValghendelsesRapportAuditEvent event(AuditEventTypes eventType, ElectionPath electionEventPath, List<ValghendelsesRapport> rapporter) {
		return new ValghendelsesRapportAuditEvent(createMock(UserData.class), electionEventPath, rapporter, eventType, Outcome.Success, "");
	}

	private ValghendelsesRapport rapport(String rapportId, ReportCategory kategori, Access access, boolean synlig) {
		ValghendelsesRapport result = new ValghendelsesRapport(rapportId, kategori, access);
		result.setSynlig(synlig);
		result.setTilgjengelig(synlig);
		return result;
	}
}

