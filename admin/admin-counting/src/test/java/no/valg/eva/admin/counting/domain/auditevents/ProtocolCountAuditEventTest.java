package no.valg.eva.admin.counting.domain.auditevents;

import static no.valg.eva.admin.common.counting.model.CountQualifier.PROTOCOL;
import static no.valg.eva.admin.counting.domain.auditevents.CountingAuditEventTestObjectMother.VoteCountConfig.protocolVoteCountConfig;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.auditlog.AuditEventFactory;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.AuditLogTestsObjectMother;
import no.valg.eva.admin.common.auditlog.AuditedObjectSource;
import no.valg.eva.admin.common.auditlog.Outcome;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.common.counting.model.CountContext;
import no.valg.eva.admin.common.counting.model.DailyMarkOffCount;
import no.valg.eva.admin.common.counting.model.DailyMarkOffCounts;
import no.valg.eva.admin.common.counting.model.ProtocolCount;
import no.valg.eva.admin.counting.domain.model.VoteCount;

import org.joda.time.LocalDate;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;


public class ProtocolCountAuditEventTest {

	private static final String ID = "id123";
	private static final String AREA_NAME = "Lunner";
	private static final String AREA_PATH = "750401.47.05.0533";
	private static final String REPORTING_UNIT_NAME = "Valgstyret i Lunner";
	private static final String ELECTION_PATH = "750401.01.02.053301";
	private static final String COMMENT = "Uklart";
	private final AuditLogTestsObjectMother objectMother = new AuditLogTestsObjectMother();
	protected ProtocolCount protocolCount;
	protected CountContext context;
	protected AreaPath areaPath;

	@BeforeMethod
	public void setUp() {
		areaPath = new AreaPath(AREA_PATH);
		context = new CountContext(new ElectionPath(ELECTION_PATH), CountCategory.VO);
		protocolCount = new ProtocolCount(ID, AreaPath.from(AREA_PATH), AREA_NAME, REPORTING_UNIT_NAME, true);
		protocolCount.setOrdinaryBallotCount(22);
		protocolCount.setQuestionableBallotCount(3);
		protocolCount.setSpecialCovers(1);
		protocolCount.setForeignSpecialCovers(1);
		protocolCount.setEmergencySpecialCovers(4);
		List<DailyMarkOffCount> dailyMarkOffCountList = new ArrayList<>();
		dailyMarkOffCountList.add(new DailyMarkOffCount(new LocalDate(2015, 9, 12), 12));
		dailyMarkOffCountList.add(new DailyMarkOffCount(new LocalDate(2015, 9, 13), 13));
		protocolCount.setDailyMarkOffCounts(new DailyMarkOffCounts(dailyMarkOffCountList));
		protocolCount.setComment(COMMENT);
	}

	@Test
	public void toJson_withNormalValues_isCorrect() throws Exception {
		VoteCount voteCount = CountingAuditEventTestObjectMother.voteCount(protocolVoteCountConfig(1, null, null));
		ThreadLocalVoteCountAuditDetailsMap.INSTANCE.put(PROTOCOL, new VoteCountAuditDetails(voteCount, false, false));
		ProtocolCountAuditEvent auditEvent = new ProtocolCountAuditEvent(objectMother.createUserData(), context, protocolCount, AuditEventTypes.ApproveCount,
				Outcome.Success, "details, details");

		assertThat(auditEvent.objectType()).isEqualTo(VoteCount.class);
		assertThat(auditEvent.toJson()).contains("{\"contestReport\":{");
	}

	@Test
	public void toJson_forBoroughContests_isCorrect() throws Exception {
		VoteCount voteCount = CountingAuditEventTestObjectMother.voteCount(protocolVoteCountConfig(null, 1, 1));
		ThreadLocalVoteCountAuditDetailsMap.INSTANCE.put(PROTOCOL, new VoteCountAuditDetails(voteCount, false, false));
		List<DailyMarkOffCount> dailyMarkOffCountForOtherContestsList = new ArrayList<>();
		dailyMarkOffCountForOtherContestsList.add(new DailyMarkOffCount(new LocalDate(2015, 9, 12), 22));
		dailyMarkOffCountForOtherContestsList.add(new DailyMarkOffCount(new LocalDate(2015, 9, 13), 23));
		protocolCount.setDailyMarkOffCountsForOtherContests(new DailyMarkOffCounts(dailyMarkOffCountForOtherContestsList));
		protocolCount.setBallotCountForOtherContests(45);
		ProtocolCountAuditEvent auditEvent = new ProtocolCountAuditEvent(objectMother.createUserData(), context, protocolCount, AuditEventTypes.ApproveCount,
				Outcome.Success, "details, details");

		assertThat(auditEvent.objectType()).isEqualTo(VoteCount.class);
		assertThat(auditEvent.toJson()).contains("{\"contestReport\":{");
	}

	@Test
	public void objectClasses_mustReturnClassesOfObjectsDestinedForAuditEventConstructor() {
		assertThat(ProtocolCountAuditEvent.objectClasses(AuditEventTypes.ApproveCount)).isEqualTo(new Class[] { CountContext.class, ProtocolCount.class });
	}

	@Test
	public void constructor_mustMeetAuditInterceptorRequirements() throws NoSuchMethodException {
		assertThat(AuditEventFactory.getAuditEventConstructor(ProtocolCountAuditEvent.class,
				ProtocolCountAuditEvent.objectClasses(AuditEventTypes.SaveCount), AuditedObjectSource.Parameters)).isNotNull();
	}
}

