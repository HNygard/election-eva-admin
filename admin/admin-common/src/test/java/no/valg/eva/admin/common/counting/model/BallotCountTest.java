package no.valg.eva.admin.common.counting.model;

import static org.assertj.core.api.Assertions.assertThat;

import no.evote.exception.ValidateException;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;



public class BallotCountTest {
	private BallotCount ballotCount;

	@BeforeMethod
	public void setUp() throws Exception {
		ballotCount = new BallotCount();
	}

	@Test
	public void getIdShouldReturnCorrectId() throws Exception {
		ballotCount = new BallotCount("AP", "Arbeiderpartiet", 7, 3);

		String id = ballotCount.getId();

		assertThat(id).isEqualTo("AP");
	}

	@Test
	public void setIdShouldSetCorrectId() throws Exception {
		ballotCount.setId("AP");

		assertThat(ballotCount.getId()).isEqualTo("AP");
	}

	@Test
	public void getNameShouldReturnCorrectName() throws Exception {
		ballotCount = new BallotCount("AP", "Arbeiderpartiet", 7, 3);

		String name = ballotCount.getName();

		assertThat(name).isEqualTo("Arbeiderpartiet");
	}

	@Test
	public void setNameShouldSetCorrectName() throws Exception {
		ballotCount.setName("Arbeiderpartiet");

		assertThat(ballotCount.getName()).isEqualTo("Arbeiderpartiet");
	}

	@Test
	public void getUnmodifiedCountShouldReturnCorrectCount() throws Exception {
		ballotCount = new BallotCount("AP", "Arbeiderpartiet", 7, 3);

		int count = ballotCount.getUnmodifiedCount();

		assertThat(count).isEqualTo(7);
	}

	@Test
	public void setUnmodifiedCountShouldSetCorrectCount() throws Exception {
		ballotCount.setUnmodifiedCount(7);

		assertThat(ballotCount.getUnmodifiedCount()).isEqualTo(7);
	}

	@Test
	public void getModifiedCountShouldReturnCorrectCount() throws Exception {
		ballotCount = new BallotCount("AP", "Arbeiderpartiet", 7, 3);

		int count = ballotCount.getModifiedCount();

		assertThat(count).isEqualTo(3);
	}

	@Test
	public void setModifiedCountShouldSetCorrectCount() throws Exception {
		ballotCount.setModifiedCount(3);

		assertThat(ballotCount.getModifiedCount()).isEqualTo(3);
	}

	@Test
	public void getCountShouldReturnSumOfUnmodifiedAndModifiedCount() throws Exception {
		ballotCount.setUnmodifiedCount(7);
		ballotCount.setModifiedCount(3);

		int count = ballotCount.getCount();

		assertThat(count).isEqualTo(10);
	}

	@Test(expectedExceptions = ValidateException.class, expectedExceptionsMessageRegExp = "@count.error.validation.negative.modified_ballot_count")
	public void validateShouldThrowValidateExceptionOnNegativeModifiedCount() throws Exception {
		ballotCount.setModifiedCount(-1);

		ballotCount.validate();
	}

	@Test(expectedExceptions = ValidateException.class, expectedExceptionsMessageRegExp = "@count.error.validation.negative.unmodified_ballot_count")
	public void validateShouldThrowValidateExceptionOnNegativeUnmodifiedCount() throws Exception {
		ballotCount.setUnmodifiedCount(-1);

		ballotCount.validate();
	}

	@Test
	public void validateShouldReturnOkOnBlankBallotCount() throws Exception {
		ballotCount.validate();
	}

	@Test
	public void validateShouldReturnOkOnValidBallotCount() throws Exception {
		ballotCount.setUnmodifiedCount(7);
		ballotCount.setModifiedCount(3);

		ballotCount.validate();
	}

	@Test
	public void equalsShouldReturnTrueOnSameObject() throws Exception {
		boolean equals = ballotCount.equals(ballotCount);

		assertThat(equals).isTrue();
	}

	@Test
	public void equalsShouldReturnTrueOnEqualObjects() throws Exception {
		String id = "AP";
		String name = "Arbeiderpartiet";
		int unmodifiedCount = 7;
		int modifiedCount = 3;
		ballotCount.setId(id);
		ballotCount.setName(name);
		ballotCount.setUnmodifiedCount(unmodifiedCount);
		ballotCount.setModifiedCount(modifiedCount);
		BallotCount anotherBallotCount = new BallotCount();
		anotherBallotCount.setId(id);
		anotherBallotCount.setName(name);
		anotherBallotCount.setUnmodifiedCount(unmodifiedCount);
		anotherBallotCount.setModifiedCount(modifiedCount);

		boolean equals = ballotCount.equals(anotherBallotCount);

		assertThat(equals).isTrue();
	}

	@Test
	public void equalsShouldReturnFalseOnObjectsWithDifferentId() throws Exception {
		String id = "AP";
		String name = "Arbeiderpartiet";
		int unmodifiedCount = 7;
		int modifiedCount = 3;
		ballotCount.setId(id);
		ballotCount.setName(name);
		ballotCount.setUnmodifiedCount(unmodifiedCount);
		ballotCount.setModifiedCount(modifiedCount);
		BallotCount anotherBallotCount = new BallotCount();
		anotherBallotCount.setId("SP");
		anotherBallotCount.setName(name);
		anotherBallotCount.setUnmodifiedCount(unmodifiedCount);
		anotherBallotCount.setModifiedCount(modifiedCount);

		boolean equals = ballotCount.equals(anotherBallotCount);

		assertThat(equals).isFalse();
	}

	@Test
	public void equalsShouldReturnFalseOnObjectsWithDifferentName() throws Exception {
		String id = "AP";
		String name = "Arbeiderpartiet";
		int unmodifiedCount = 7;
		int modifiedCount = 3;
		ballotCount.setId(id);
		ballotCount.setName(name);
		ballotCount.setUnmodifiedCount(unmodifiedCount);
		ballotCount.setModifiedCount(modifiedCount);
		BallotCount anotherBallotCount = new BallotCount();
		anotherBallotCount.setId(id);
		anotherBallotCount.setName("Senterpartiet");
		anotherBallotCount.setUnmodifiedCount(unmodifiedCount);
		anotherBallotCount.setModifiedCount(modifiedCount);

		boolean equals = ballotCount.equals(anotherBallotCount);

		assertThat(equals).isFalse();
	}

	@Test
	public void equalsShouldReturnFalseOnObjectsWithDifferentUnmodifiedCount() throws Exception {
		String id = "AP";
		String name = "Arbeiderpartiet";
		int unmodifiedCount = 7;
		int modifiedCount = 3;
		ballotCount.setId(id);
		ballotCount.setName(name);
		ballotCount.setUnmodifiedCount(unmodifiedCount);
		ballotCount.setModifiedCount(modifiedCount);
		BallotCount anotherBallotCount = new BallotCount();
		anotherBallotCount.setId(id);
		anotherBallotCount.setName(name);
		anotherBallotCount.setUnmodifiedCount(11);
		anotherBallotCount.setModifiedCount(modifiedCount);

		boolean equals = ballotCount.equals(anotherBallotCount);

		assertThat(equals).isFalse();
	}

	@Test
	public void equalsShouldReturnFalseOnObjectsWithDifferentModifiedCount() throws Exception {
		String id = "AP";
		String name = "Arbeiderpartiet";
		int unmodifiedCount = 7;
		int modifiedCount = 3;
		ballotCount.setId(id);
		ballotCount.setName(name);
		ballotCount.setUnmodifiedCount(unmodifiedCount);
		ballotCount.setModifiedCount(modifiedCount);
		BallotCount anotherBallotCount = new BallotCount();
		anotherBallotCount.setId(id);
		anotherBallotCount.setName(name);
		anotherBallotCount.setUnmodifiedCount(unmodifiedCount);
		anotherBallotCount.setModifiedCount(5);

		boolean equals = ballotCount.equals(anotherBallotCount);

		assertThat(equals).isFalse();
	}

	@Test
	public void equalsShouldReturnFalseOnNull() throws Exception {
		Object obj = null;

		boolean equals = ballotCount.equals(obj);

		assertThat(equals).isFalse();
	}

	@Test
	public void equalsShouldReturnFalseOnObjectWithDifferentClass() throws Exception {
		boolean equals = ballotCount.equals(new Object());

		assertThat(equals).isFalse();
	}

	@Test
	public void hashCodeIsNonZero() throws Exception {
		ballotCount.setId("AP");
		ballotCount.setName("Arbeiderpartiet");
		ballotCount.setUnmodifiedCount(7);
		ballotCount.setModifiedCount(3);

		int hashCode = ballotCount.hashCode();

		assertThat(hashCode).isNotZero();
	}

	@Test
	public void hashCodeShouldReturnSameValueOnEqualObjects() throws Exception {
		String id = "AP";
		String name = "Arbeiderpartiet";
		int unmodifiedCount = 7;
		int modifiedCount = 3;
		ballotCount.setId(id);
		ballotCount.setName(name);
		ballotCount.setUnmodifiedCount(unmodifiedCount);
		ballotCount.setModifiedCount(modifiedCount);
		BallotCount anotherBallotCount = new BallotCount();
		anotherBallotCount.setId(id);
		anotherBallotCount.setName(name);
		anotherBallotCount.setUnmodifiedCount(unmodifiedCount);
		anotherBallotCount.setModifiedCount(modifiedCount);

		int hashCode = ballotCount.hashCode();
		int anotherHashCode = anotherBallotCount.hashCode();

		assertThat(hashCode).isEqualTo(anotherHashCode);
	}

	@Test
	public void toStringReturnCorrectStringRepresentation() throws Exception {
		String id = "AP";
		String name = "Arbeiderpartiet";
		int unmodifiedCount = 7;
		int modifiedCount = 3;
		ballotCount.setId(id);
		ballotCount.setName(name);
		ballotCount.setUnmodifiedCount(unmodifiedCount);
		ballotCount.setModifiedCount(modifiedCount);

		String ballotCountString = ballotCount.toString();

		assertThat(ballotCountString).isEqualTo("BallotCount[id=AP,name=Arbeiderpartiet,unmodifiedCount=7,modifiedCount=3]");
	}
}

