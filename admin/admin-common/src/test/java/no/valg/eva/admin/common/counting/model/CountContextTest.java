package no.valg.eva.admin.common.counting.model;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;

import no.valg.eva.admin.common.ElectionPath;

import org.testng.annotations.Test;

public class CountContextTest {

	public static final ElectionPath DEFAULT_CONTEST_PATH = new ElectionPath("730001.01.01.000001");
	public static final ElectionPath ANOTHER_CONTEST_PATH = new ElectionPath("730001.01.01.000002");
	public static final ElectionPath ELECTION_PATH = new ElectionPath("730001.01.01");

	public static final CountCategory DEFAULT_COUNT_CATEGORY = CountCategory.VO;
	public static final CountCategory ANOTHER_COUNT_CATEGORY = CountCategory.VF;

	public static final String TO_STRING_FORMAT = "CountContext[contestPath=%s,category=%s]";

	@Test(expectedExceptions = NullPointerException.class, expectedExceptionsMessageRegExp = "expected non-null contest path")
	public void constructorThrowsExceptionWhenContestPathIsNull() {
		new CountContext(null, DEFAULT_COUNT_CATEGORY);
	}

	@Test(
			expectedExceptions = IllegalArgumentException.class,
			expectedExceptionsMessageRegExp = "expected <CONTEST> election path, but got <ELECTION>")
	public void constructorThrowsExceptionOnElectionPath() {
		new CountContext(ELECTION_PATH, DEFAULT_COUNT_CATEGORY);
	}

	@Test
	public void constructorAcceptsContestPath() {
		new CountContext(DEFAULT_CONTEST_PATH, DEFAULT_COUNT_CATEGORY);
	}

	@Test(expectedExceptions = NullPointerException.class, expectedExceptionsMessageRegExp = "expected non-null category")
	public void constructorThrowsExceptionWhenCategoryIsNull() {
		new CountContext(DEFAULT_CONTEST_PATH, null);
	}

	@Test
	public void getContestPathReturnsDefaultContestPath() {
		CountContext context = defaultCountContext();

		ElectionPath contestPath = context.getContestPath();

		assertThat(contestPath).isEqualTo(DEFAULT_CONTEST_PATH);
	}

	@Test
	public void getContestPathReturnsAnotherContestPath() {
		CountContext context = anotherContestCountContext();

		ElectionPath contestPath = context.getContestPath();

		assertThat(contestPath).isEqualTo(ANOTHER_CONTEST_PATH);
	}

	@Test
	public void getCategoryReturnDefaultCategory() {
		CountContext context = defaultCountContext();

		CountCategory category = context.getCategory();

		assertThat(category).isEqualTo(DEFAULT_COUNT_CATEGORY);
	}

	@Test
	public void getCategoryReturnsAnotherCategory() {
		CountContext context = anotherCategoryCountContext();

		CountCategory category = context.getCategory();

		assertThat(category).isEqualTo(ANOTHER_COUNT_CATEGORY);
	}

	@Test
	public void equalsIsFalseForNullObject() {
		CountContext context = defaultCountContext();
		Object obj = null;

		boolean equals = context.equals(obj);

		assertThat(equals).isFalse();
	}

	@Test
	public void equalsIsFalseForObjectOfAnotherType() {
		CountContext context = defaultCountContext();

		boolean equals = context.equals(new Object());

		assertThat(equals).isFalse();
	}

	@Test
	public void equalsIsTrueForSameObject() {
		CountContext context = defaultCountContext();

		boolean equals = context.equals(context);

		assertThat(equals).isTrue();
	}

	@Test
	public void equalsIsTrueWhenAreaContestAndCategoryIsEqual() {
		CountContext context = defaultCountContext();
		CountContext anotherContext = defaultCountContext();

		boolean equals = context.equals(anotherContext);

		assertThat(equals).isTrue();
	}

	@Test
	public void equalsIsFalseWhenAnotherContest() {
		CountContext context = defaultCountContext();
		CountContext anotherContext = anotherContestCountContext();

		boolean equals = context.equals(anotherContext);

		assertThat(equals).isFalse();
	}

	@Test
	public void equalsIsFalseWhenAnotherCategory() {
		CountContext context = defaultCountContext();
		CountContext anotherContext = anotherCategoryCountContext();

		boolean equals = context.equals(anotherContext);

		assertThat(equals).isFalse();
	}

	@Test
	public void hashCodeIsNotZero() {
		CountContext context = defaultCountContext();

		int hashCode = context.hashCode();

		assertThat(hashCode).isNotZero();
	}

	@Test
	public void hashCodeIsEqualWhenEqualsIsTrue() {
		CountContext context = defaultCountContext();
		CountContext anotherContext = defaultCountContext();

		int hashCode = context.hashCode();
		int anotherHashCode = anotherContext.hashCode();

		assertThat(hashCode).isEqualTo(anotherHashCode);
	}

	@Test
	public void toStringReturnStringWithAreaPathContestPathAndCategory() {
		CountContext context = defaultCountContext();

		String contextString = context.toString();

		String expected = format(TO_STRING_FORMAT, DEFAULT_CONTEST_PATH, CountCategory.VO);
		assertThat(contextString).isEqualTo(expected);
	}

	private CountContext defaultCountContext() {
		return new CountContext(DEFAULT_CONTEST_PATH, DEFAULT_COUNT_CATEGORY);
	}

	private CountContext anotherContestCountContext() {
		return new CountContext(ANOTHER_CONTEST_PATH, DEFAULT_COUNT_CATEGORY);
	}

	private CountContext anotherCategoryCountContext() {
		return new CountContext(DEFAULT_CONTEST_PATH, ANOTHER_COUNT_CATEGORY);
	}
}
