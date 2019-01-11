package no.valg.eva.admin.common.auditlog.auditevents.config;

import static com.jayway.jsonassert.JsonAssert.collectionWithSize;
import static no.valg.eva.admin.common.counting.constants.CountingMode.BY_POLLING_DISTRICT;
import static no.valg.eva.admin.common.counting.constants.CountingMode.BY_TECHNICAL_POLLING_DISTRICT;
import static no.valg.eva.admin.common.counting.constants.CountingMode.CENTRAL;
import static no.valg.eva.admin.common.counting.constants.CountingMode.CENTRAL_AND_BY_POLLING_DISTRICT;
import static no.valg.eva.admin.common.counting.model.CountCategory.FO;
import static no.valg.eva.admin.common.counting.model.CountCategory.FS;
import static no.valg.eva.admin.common.counting.model.CountCategory.VF;
import static no.valg.eva.admin.common.counting.model.CountCategory.VO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import no.evote.security.UserData;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.Outcome;
import no.valg.eva.admin.common.auditlog.auditevents.AbstractAuditEventTest;
import no.valg.eva.admin.common.auditlog.auditevents.AuditEvent;
import no.valg.eva.admin.common.configuration.model.local.ReportCountCategory;
import no.valg.eva.admin.common.counting.constants.CountingMode;

import org.testng.annotations.Test;

import com.jayway.jsonassert.JsonAssert;


public class ReportCountCategoryAuditEventTest extends AbstractAuditEventTest {

	@Test
	public void objectType_returnsAdvancePollingPlace() throws Exception {
		ReportCountCategoryAuditEvent event = event(AuditEventTypes.Update, AreaPath.from("111111.22.33.4444"), new ArrayList<ReportCountCategory>());

		assertThat(event.objectType()).isSameAs(AreaPath.class);
	}

	@Test
	public void toJson() throws Exception {
		ReportCountCategoryAuditEvent event = event(AuditEventTypes.Update, AreaPath.from("111111.22.33.4444"),
				Arrays.asList(
						countCategory(FO, CENTRAL),
						countCategory(VF, CENTRAL_AND_BY_POLLING_DISTRICT),
						countCategory(FS, BY_POLLING_DISTRICT),
						countCategory(VO, BY_TECHNICAL_POLLING_DISTRICT)));
		JsonAssert
				.with(event.toJson())
				.assertThat("$", hasEntry("path", "111111.22.33.4444"))
				.assertThat("$.reportCountCategories[*]", collectionWithSize(equalTo(4)))
				.assertThat("$.reportCountCategories[*].voteCountCategoryId", containsInAnyOrder("FO", "VF", "FS", "VO"))
				.assertThat("$.reportCountCategories[*].countingMode",
						containsInAnyOrder("CENTRAL", "CENTRAL_AND_BY_POLLING_DISTRICT", "BY_POLLING_DISTRICT", "BY_TECHNICAL_POLLING_DISTRICT"));
	}

	private ReportCountCategory countCategory(no.valg.eva.admin.common.counting.model.CountCategory category, CountingMode mode) {
		ReportCountCategory result = new ReportCountCategory(category, new ArrayList<>());
		result.setCountingMode(mode);
		return result;
	}

	@Override
	protected Class<? extends AuditEvent> getAuditEventClass() {
		return ReportCountCategoryAuditEvent.class;
	}

	private ReportCountCategoryAuditEvent event(AuditEventTypes eventType, AreaPath areaPath, List<ReportCountCategory> categories) {
		return new ReportCountCategoryAuditEvent(createMock(UserData.class), areaPath, categories, eventType, Outcome.Success, "");
	}
}

