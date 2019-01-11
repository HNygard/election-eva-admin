package no.valg.eva.admin.common.auditlog.auditevents;

import static com.google.common.collect.ImmutableList.of;
import static com.jayway.jsonassert.JsonAssert.collectionWithSize;
import static no.valg.eva.admin.common.auditlog.AuditEventTypes.AddChildren;
import static no.valg.eva.admin.common.auditlog.AuditEventTypes.Create;
import static no.valg.eva.admin.common.auditlog.AuditEventTypes.Delete;
import static no.valg.eva.admin.common.auditlog.AuditEventTypes.RemoveChildren;
import static no.valg.eva.admin.common.auditlog.Outcome.Success;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;

import java.util.List;

import no.valg.eva.admin.common.auditlog.AuditLogTestsObjectMother;
import no.valg.eva.admin.configuration.domain.model.Borough;
import no.valg.eva.admin.configuration.domain.model.PollingDistrict;

import org.testng.annotations.Test;

import com.google.common.collect.Lists;

public class PollingDistrictParentAuditEventTest extends PollingDistrictAuditEventTest {
	public static final String POLLING_DISTRICT_2_NAME = "PollingDistrict2";
	public static final String POLLING_DISTRICT_2_ID = "000002";
	public static final String POLLING_DISTRICT_3_ID = "000003";
	public static final String POLLING_DISTRICT_3_NAME = "PollingDistrict3";
	public static final String DETAIL = "detail";
	protected final AuditLogTestsObjectMother objectMother = new AuditLogTestsObjectMother();
	private final Borough borough = objectMother.createBorough();
	private final List<PollingDistrict> childPollingDistricts = Lists.newArrayList(
			new PollingDistrict(POLLING_DISTRICT_2_ID, POLLING_DISTRICT_2_NAME, borough),
			new PollingDistrict(POLLING_DISTRICT_3_ID, POLLING_DISTRICT_3_NAME, borough));
	private List<String> childPollingDistrictIds = of(POLLING_DISTRICT_2_ID, POLLING_DISTRICT_3_ID);

	@Test
	public void toJson_forSuccessfulCreateParentPollingDistrict_isCorrect() {
		PollingDistrict pollingDistrict = createPollingDistrict();
		PollingDistrictAuditEvent auditEvent = createParentPollingDistrictAuditEvent(pollingDistrict);
		assertPollingDistrictWithoutDetails(auditEvent)
				.and()
				.assertThat("$.childPollingDistricts", collectionWithSize(equalTo(2)))
				.assertThat("$.childPollingDistricts[*].id", contains(POLLING_DISTRICT_2_ID, POLLING_DISTRICT_3_ID))
				.assertThat("$.childPollingDistricts[*].name", contains(POLLING_DISTRICT_2_NAME, POLLING_DISTRICT_3_NAME));
	}

	@Test
	public void toJson_forSuccessfulAddChildrenToParentPollingDistrict_isCorrect() {
		PollingDistrictAuditEvent auditEvent = createAddChildrenToParentPollingDistrictAuditEvent(createPollingDistrict());
		assertPollingDistrictWithoutDetails(auditEvent)
				.and()
				.assertThat("$.childPollingDistricts", collectionWithSize(equalTo(2)))
				.assertThat("$.childPollingDistricts[*].id", contains(POLLING_DISTRICT_2_ID, POLLING_DISTRICT_3_ID));
	}

	@Test
	public void toJson_forSuccessfulDeleteChildrenFromParentPollingDistrict_isCorrect() {
		PollingDistrictAuditEvent auditEvent = createDeleteChildrenFromParentPollingDistrictAuditEvent(createPollingDistrict());
		assertPollingDistrictWithoutDetails(auditEvent)
				.and()
				.assertThat("$.childPollingDistricts", collectionWithSize(equalTo(2)))
				.assertThat("$.childPollingDistricts[*].id", contains(POLLING_DISTRICT_2_ID, POLLING_DISTRICT_3_ID));
	}

	@Test
	public void toJson_forSuccessfulDeleteParentPollingDistrict_isCorrect() {
		PollingDistrictAuditEvent auditEvent = createDeleteParentPollingDistrictAuditEvent(createPollingDistrict());
		assertPollingDistrictWithoutDetails(auditEvent);
	}

	private PollingDistrictAuditEvent createDeleteParentPollingDistrictAuditEvent(PollingDistrict pollingDistrict) {
		return new PollingDistrictParentAuditEvent(objectMother.createUserData(), pollingDistrict, Delete, Success, DETAIL);
	}

	private PollingDistrictAuditEvent createParentPollingDistrictAuditEvent(PollingDistrict pollingDistrict) {
		return new PollingDistrictParentAuditEvent(objectMother.createUserData(), pollingDistrict, childPollingDistricts, Create, Success, DETAIL);
	}

	private PollingDistrictAuditEvent createAddChildrenToParentPollingDistrictAuditEvent(PollingDistrict pollingDistrict) {
		return new PollingDistrictParentAuditEvent(objectMother.createUserData(), childPollingDistrictIds, pollingDistrict, AddChildren, Success, DETAIL);
	}

	private PollingDistrictAuditEvent createDeleteChildrenFromParentPollingDistrictAuditEvent(PollingDistrict pollingDistrict) {
		return new PollingDistrictParentAuditEvent(objectMother.createUserData(), childPollingDistrictIds, pollingDistrict, RemoveChildren, Success, DETAIL);
	}
}
