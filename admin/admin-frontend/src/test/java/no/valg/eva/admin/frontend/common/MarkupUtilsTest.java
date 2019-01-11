package no.valg.eva.admin.frontend.common;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.Test;


public class MarkupUtilsTest {

	@Test
	public void getClass_withPostive_returnsPos() throws Exception {
		assertThat(MarkupUtils.getClass(10)).isEqualTo("diff-pos");
	}

	@Test
	public void getClass_withNegative_returnsNeg() throws Exception {
		assertThat(MarkupUtils.getClass(-10)).isEqualTo("diff-neg");
	}

	@Test
	public void getClass_withZero_returnsZero() throws Exception {
		assertThat(MarkupUtils.getClass(0)).isEqualTo("diff-zero");
	}
}

