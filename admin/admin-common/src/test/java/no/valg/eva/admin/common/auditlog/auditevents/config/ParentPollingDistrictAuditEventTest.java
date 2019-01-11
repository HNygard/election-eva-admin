package no.valg.eva.admin.common.auditlog.auditevents.config;

import static com.jayway.jsonassert.JsonAssert.collectionWithSize;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;

import no.evote.security.UserData;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.Outcome;
import no.valg.eva.admin.common.auditlog.auditevents.AbstractAuditEventTest;
import no.valg.eva.admin.common.auditlog.auditevents.AuditEvent;
import no.valg.eva.admin.common.configuration.model.local.ParentPollingDistrict;
import no.valg.eva.admin.common.configuration.model.local.RegularPollingDistrict;
import no.valg.eva.admin.configuration.domain.model.PollingDistrictType;

import org.testng.annotations.Test;

import com.jayway.jsonassert.JsonAssert;


public class ParentPollingDistrictAuditEventTest extends AbstractAuditEventTest {

	@Test
	public void objectType_returnsAdvancePollingPlace() throws Exception {
		ParentPollingDistrictAuditEvent event = event(AuditEventTypes.Save, new ParentPollingDistrict(AreaPath.from("111111.22.33.4444")));

		assertThat(event.objectType()).isSameAs(ParentPollingDistrict.class);
	}

	@Test
	public void toJson() throws Exception {
		ParentPollingDistrict district = new ParentPollingDistrict(AreaPath.from("111111.22.33.4444"));
		district.setId("010100");
		district.setName("My Polling place");
		RegularPollingDistrict child = new RegularPollingDistrict(district.getPath(), PollingDistrictType.REGULAR);
		child.setPk(10L);
		child.setId("0001");
		child.setName("Child 1");
		district.getChildren().add(child);
		ParentPollingDistrictAuditEvent event = event(AuditEventTypes.Save, district);

		JsonAssert.with(event.toJson())
				.assertThat("$", hasEntry("path", district.getPath().path()))
				.assertThat("$", hasEntry("id", district.getId()))
				.assertThat("$", hasEntry("name", district.getName()))
				.assertThat("$.children[*]", collectionWithSize(equalTo(1)))
				.assertThat("$.children[*].path", containsInAnyOrder(child.getPath().path()))
				.assertThat("$.children[*].pk", containsInAnyOrder(new Long(10).intValue()))
				.assertThat("$.children[*].id", containsInAnyOrder(child.getId()))
				.assertThat("$.children[*].name", containsInAnyOrder(child.getName()));
	}

	@Override
	protected Class<? extends AuditEvent> getAuditEventClass() {
		return ParentPollingDistrictAuditEvent.class;
	}

	private ParentPollingDistrictAuditEvent event(AuditEventTypes eventType, ParentPollingDistrict district) {
		return new ParentPollingDistrictAuditEvent(createMock(UserData.class), district, eventType, Outcome.Success, "");
	}

}

