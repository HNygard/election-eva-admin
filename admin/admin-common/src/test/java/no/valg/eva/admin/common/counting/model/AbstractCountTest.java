package no.valg.eva.admin.common.counting.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Random;

import no.evote.exception.ValidateException;
import no.valg.eva.admin.common.AreaPath;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;


public class AbstractCountTest {
	private static final String DEFAULT_ID = "PVO1";
	private static final AreaPath DEFAULT_AREA_PATH = new AreaPath("730071.47.01.0101.010100.0001");
	private static final AreaPath ANOTHER_AREA_PATH = new AreaPath("730071.47.03.0301.030114.1404");
	private static final String DEFAULT_AREA_NAME = "Halden";
	private static final String DEFAULT_REPORTING_UNIT_AREA_NAME = DEFAULT_AREA_NAME;
	private static final String ANOTHER_AREA_NAME = "Lambertseter videreg. skole";
	private static final String ANOTHER_REPORTING_UNIT_AREA_NAME = ANOTHER_AREA_NAME;
	private static final boolean MANUAL_COUNT_TRUE = true;
	private static final boolean MANUAL_COUNT_FALSE = false;
	private static final String DEFAULT_COMMENT = "Uvisst.";
	private static final String ANOTHER_COMMENT = null;
	private static final int DEFAULT_BLANK_BALLOT_COUNT = 3;
	private static final int ANOTHER_BLANK_BALLOT_COUNT = 5;

	private AbstractCount count;

	@BeforeMethod
	public void setUp() throws Exception {
		count = defaultCount();
	}

	private AbstractCount defaultCount() {
		return count(
				DEFAULT_ID,
				DEFAULT_AREA_PATH,
				CountQualifier.PROTOCOL,
				DEFAULT_AREA_NAME,
				DEFAULT_REPORTING_UNIT_AREA_NAME,
				CountStatus.SAVED,
				MANUAL_COUNT_TRUE,
				DEFAULT_COMMENT,
				DEFAULT_BLANK_BALLOT_COUNT);
	}

	private AbstractCount count(
			String id,
			AreaPath areaPath,
			CountQualifier qualifier,
			String areaName,
			String reportingUnitAreaName,
			final CountStatus countStatus,
			boolean manualCount,
			final String newComment,
			final int newBlankBallotCount) {

		AbstractCount abstractCount = new AbstractCount(id, areaPath, qualifier, null, areaName, reportingUnitAreaName, manualCount, newBlankBallotCount) {
			@Override
			public void validateForApproval() {
			}

			{
				this.status = countStatus;
				this.comment = newComment;
			}

			@Override
			public int getOrdinaryBallotCount() {
				return 0;
			}

			@Override
			public int getTotalBallotCount() {
				return 0;
			}

			@Override
			public List<BallotCount> getBallotCounts() {
				return null;
			}

			@Override
			public Integer getQuestionableBallotCount() {
				return 0;
			}

			@Override
			public void setQuestionableBallotCount(Integer questionableBallotCount) {
			}

			@Override
			public boolean hasRejectedBallotCounts() {
				return false;
			}

			@Override
			public List<RejectedBallotCount> getRejectedBallotCounts() {
				return null;
			}

			@Override
			public int getTotalRejectedBallotCount() {
				return 0;
			}

			@Override
			public Integer getLateValidationCovers() {
				return null;
			}

		};
		abstractCount.setId("PVO1");
		return abstractCount;
	}

	@Test
	public void getId() throws Exception {
		String id = count.getId();

		assertThat(id).isEqualTo(DEFAULT_ID);
	}

	@Test
	public void getAreaPath() throws Exception {
		AreaPath areaPath = count.getAreaPath();

		assertThat(areaPath).isEqualTo(DEFAULT_AREA_PATH);
	}

	@Test
	public void getQualifier() throws Exception {
		CountQualifier qualifier = count.getQualifier();

		assertThat(qualifier).isSameAs(CountQualifier.PROTOCOL);
	}

	@Test
	public void getQualifierName() throws Exception {
		String qualifierName = count.getQualifierName();

		assertThat(qualifierName).isEqualTo(CountQualifier.PROTOCOL.getName());
	}

	@Test
	public void getStatus() throws Exception {
		CountStatus status = count.getStatus();

		assertThat(status).isSameAs(CountStatus.SAVED);
	}

	@Test
	public void getStatusName() throws Exception {
		String statusName = count.getStatusName();

		assertThat(statusName).isEqualTo(CountStatus.SAVED.getName());
	}

	@Test
	public void isApprovedShouldReturnTrueForApprovedStatus() throws Exception {
		count = count(
				DEFAULT_ID,
				DEFAULT_AREA_PATH,
				CountQualifier.PROTOCOL,
				DEFAULT_AREA_NAME,
				DEFAULT_REPORTING_UNIT_AREA_NAME,
				CountStatus.APPROVED,
				MANUAL_COUNT_TRUE,
				DEFAULT_COMMENT,
				DEFAULT_BLANK_BALLOT_COUNT);

		boolean approved = count.isApproved();

		assertThat(approved).isTrue();
	}

	@Test
	public void isApprovedShouldReturnFalserForOtherStatus() throws Exception {
		boolean approved = count.isApproved();

		assertThat(approved).isFalse();
	}

	@Test
	public void setStatus() throws Exception {
		count.setStatus(CountStatus.REVOKED);

		assertThat(count.getStatus()).isSameAs(CountStatus.REVOKED);
	}

	@Test
	public void getAreaName() throws Exception {
		String areaName = count.getAreaName();

		assertThat(areaName).isEqualTo(DEFAULT_AREA_NAME);
	}

	@Test
	public void getReportingUnitAreaName() throws Exception {
		String reportingUnitAreaName = count.getReportingUnitAreaName();

		assertThat(reportingUnitAreaName).isEqualTo(DEFAULT_REPORTING_UNIT_AREA_NAME);
	}

	@Test
	public void getComment() throws Exception {
		String comment = count.getComment();

		assertThat(comment).isEqualTo(DEFAULT_COMMENT);
	}

	@Test
	public void setComment() throws Exception {
		count.setComment(null);

		assertThat(count.getComment()).isNull();
	}

	@Test
	public void isManualCountShouldReturnTrueWhenManualCountIsTrue() throws Exception {
		boolean manualCount = count.isManualCount();

		assertThat(manualCount).isTrue();
	}

	@Test
	public void isManualCountShouldReturnFalseWhenManualCountIsFalse() throws Exception {
		count = count(
				DEFAULT_ID,
				DEFAULT_AREA_PATH,
				CountQualifier.PROTOCOL,
				DEFAULT_AREA_NAME,
				DEFAULT_REPORTING_UNIT_AREA_NAME,
				CountStatus.SAVED,
				MANUAL_COUNT_FALSE,
				DEFAULT_COMMENT,
				DEFAULT_BLANK_BALLOT_COUNT);

		boolean manualCount = count.isManualCount();

		assertThat(manualCount).isFalse();
	}

	@Test
	public void getBlankBallotCount() throws Exception {
		int blankBallotCount = count.getBlankBallotCount();

		assertThat(blankBallotCount).isEqualTo(DEFAULT_BLANK_BALLOT_COUNT);
	}

	@Test
	public void setBlankBallotCount() throws Exception {
		count.setBlankBallotCount(5);

		assertThat(count.getBlankBallotCount()).isEqualTo(5);
	}

	@Test(expectedExceptions = ValidateException.class,
		  expectedExceptionsMessageRegExp = AbstractCount.COUNT_ERROR_VALIDATION_NEGATIVE_BLANK_BALLOT_COUNT)
	public void validateThrowsExceptionWhenNegativeBlankBallotCount() throws Exception {
		count.setBlankBallotCount(-3);

		count.validate();
	}

	@Test
	public void validateShouldAcceptZeroBlankBallotCount() throws Exception {
		count.setBlankBallotCount(0);

		count.validate();
	}

	@Test
	public void validateShouldAcceptPositiveBlankBallotCount() throws Exception {
		int blankBallotCount = 1 + new Random(System.currentTimeMillis()).nextInt(Integer.MAX_VALUE);
		count.setBlankBallotCount(blankBallotCount);

		count.validate();
	}

	@Test
	public void equalsShouldReturnTrueForSameObject() throws Exception {
		Object other = count;

		boolean equals = count.equals(other);

		assertThat(equals).isTrue();
	}

	@Test
	public void equalsShouldReturnTrueForEqualObject() throws Exception {
		Object other = defaultCount();

		boolean equals = count.equals(other);

		assertThat(equals).isTrue();
	}

	@Test
	public void equalsShouldReturnFalseForCountWithDifferentAreaPath() throws Exception {
		Object other = count(
				DEFAULT_ID,
				ANOTHER_AREA_PATH,
				CountQualifier.PROTOCOL,
				DEFAULT_AREA_NAME,
				DEFAULT_REPORTING_UNIT_AREA_NAME,
				CountStatus.SAVED,
				MANUAL_COUNT_TRUE,
				DEFAULT_COMMENT,
				DEFAULT_BLANK_BALLOT_COUNT);

		boolean equals = count.equals(other);

		assertThat(equals).isFalse();
	}

	@Test
	public void equalsShouldReturnFalseForCountWithDifferentQualifier() throws Exception {
		Object other = count(
				DEFAULT_ID,
				DEFAULT_AREA_PATH,
				CountQualifier.PRELIMINARY,
				DEFAULT_AREA_NAME,
				DEFAULT_REPORTING_UNIT_AREA_NAME,
				CountStatus.SAVED,
				MANUAL_COUNT_TRUE,
				DEFAULT_COMMENT,
				DEFAULT_BLANK_BALLOT_COUNT);

		boolean equals = count.equals(other);

		assertThat(equals).isFalse();
	}

	@Test
	public void equalsShouldReturnFalseForCountWithDifferentAreaName() throws Exception {
		Object other = count(
				DEFAULT_ID,
				DEFAULT_AREA_PATH,
				CountQualifier.PROTOCOL,
				ANOTHER_AREA_NAME,
				DEFAULT_REPORTING_UNIT_AREA_NAME,
				CountStatus.SAVED,
				MANUAL_COUNT_TRUE,
				DEFAULT_COMMENT,
				DEFAULT_BLANK_BALLOT_COUNT);

		boolean equals = count.equals(other);

		assertThat(equals).isFalse();
	}

	@Test
	public void equalsShouldReturnFalseForCountWithDifferentReportingUnitAreaName() throws Exception {
		Object other = count(
				DEFAULT_ID,
				DEFAULT_AREA_PATH,
				CountQualifier.PROTOCOL,
				DEFAULT_AREA_NAME,
				ANOTHER_REPORTING_UNIT_AREA_NAME,
				CountStatus.SAVED,
				MANUAL_COUNT_TRUE,
				DEFAULT_COMMENT,
				DEFAULT_BLANK_BALLOT_COUNT);

		boolean equals = count.equals(other);

		assertThat(equals).isFalse();
	}

	@Test
	public void equalsShouldReturnFalseForCountWithDifferentStatus() throws Exception {
		Object other = count(
				DEFAULT_ID,
				DEFAULT_AREA_PATH,
				CountQualifier.PROTOCOL,
				DEFAULT_AREA_NAME,
				DEFAULT_REPORTING_UNIT_AREA_NAME,
				CountStatus.REVOKED,
				MANUAL_COUNT_TRUE,
				DEFAULT_COMMENT,
				DEFAULT_BLANK_BALLOT_COUNT);

		boolean equals = count.equals(other);

		assertThat(equals).isFalse();
	}

	@Test
	public void equalsShouldReturnFalseForCountWithDifferentManualCount() throws Exception {
		Object other = count(
				DEFAULT_ID,
				DEFAULT_AREA_PATH,
				CountQualifier.PROTOCOL,
				DEFAULT_AREA_NAME,
				DEFAULT_REPORTING_UNIT_AREA_NAME,
				CountStatus.SAVED,
				MANUAL_COUNT_FALSE,
				DEFAULT_COMMENT,
				DEFAULT_BLANK_BALLOT_COUNT);

		boolean equals = count.equals(other);

		assertThat(equals).isFalse();
	}

	@Test
	public void equalsShouldReturnFalseForCountWithDifferentComment() throws Exception {
		Object other = count(
				DEFAULT_ID,
				DEFAULT_AREA_PATH,
				CountQualifier.PROTOCOL,
				DEFAULT_AREA_NAME,
				DEFAULT_REPORTING_UNIT_AREA_NAME,
				CountStatus.SAVED,
				MANUAL_COUNT_TRUE,
				ANOTHER_COMMENT,
				DEFAULT_BLANK_BALLOT_COUNT);

		boolean equals = count.equals(other);

		assertThat(equals).isFalse();
	}

	@Test
	public void equalsShouldReturnFalseForCountWithDifferentBlankBallotCount() throws Exception {
		Object other = count(
				DEFAULT_ID,
				DEFAULT_AREA_PATH,
				CountQualifier.PROTOCOL,
				DEFAULT_AREA_NAME,
				DEFAULT_REPORTING_UNIT_AREA_NAME,
				CountStatus.SAVED,
				MANUAL_COUNT_TRUE,
				DEFAULT_COMMENT,
				ANOTHER_BLANK_BALLOT_COUNT);

		boolean equals = count.equals(other);

		assertThat(equals).isFalse();
	}

	@Test
	public void equalsShouldReturnFalseForNull() throws Exception {
		Object other = null;

		boolean equals = count.equals(other);

		assertThat(equals).isFalse();
	}

	@Test
	public void equalsShouldReturnFalseForObjectOfAnotherType() throws Exception {
		Object other = new Object();

		boolean equals = count.equals(other);

		assertThat(equals).isFalse();
	}

	@Test
	public void hashCodeShouldReturnSameForEqualObject() throws Exception {
		Object other = defaultCount();

		int countHashCode = count.hashCode();
		int otherHashCode = other.hashCode();

		assertThat(countHashCode).isEqualTo(otherHashCode);
	}

	@Test
	public void toStringShouldReturnCorrectString() throws Exception {
		String expectedToStringRegex =
				"[id=PVO1,areaPath=730071.47.01.0101.010100.0001,status=SAVED,areaName=Halden,reportingUnitAreaName=Halden,"
						+ "comment=Uvisst.,manualCount=true,blankBallotCount=3]";

		String toString = count.toString();

		assertThat(toString).contains(expectedToStringRegex);
	}

	@Test
	public void isEditable_whenNew_returnTrue() throws Exception {
		AbstractCount count = defaultCount();
		count.setStatus(CountStatus.NEW);
		assertThat(count.isEditable()).isTrue();
	}

	@Test
	public void isEditable_whenSaved_returnTrue() throws Exception {
		AbstractCount count = defaultCount();
		count.setStatus(CountStatus.SAVED);
		assertThat(count.isEditable()).isTrue();
	}

	@Test
	public void isEditable_whenRevoked_returnTrue() throws Exception {
		AbstractCount count = defaultCount();
		count.setStatus(CountStatus.REVOKED);
		assertThat(count.isEditable()).isTrue();
	}

	@Test
	public void isEditable_whenApproved_returnFalse() throws Exception {
		AbstractCount count = defaultCount();
		count.setStatus(CountStatus.APPROVED);
		assertThat(count.isEditable()).isFalse();
	}

	@Test
	public void isEditable_whenToSettlement_returnFalse() throws Exception {
		AbstractCount count = defaultCount();
		count.setStatus(CountStatus.TO_SETTLEMENT);
		assertThat(count.isEditable()).isFalse();
	}
}

