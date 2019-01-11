package no.valg.eva.admin.util;

import org.testng.annotations.Test;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;


public class StringUtilTest {

	@Test
	public void convert_withObjectArray_returnsStringArray() {
		Object[] o = new Object[] { "hei", Integer.valueOf(100), new HashMap<>() };

		String[] result = StringUtil.convert(o);
		assertThat(result.length).isEqualTo(3);
		assertThat(result[0]).isEqualTo("hei");
		assertThat(result[1]).isEqualTo("100");
		assertThat(result[2]).isEqualTo("{}");
	}
	
	@Test
	public void joinOnlyNonNullAndNonEmpty() {
		assertThat(StringUtil.joinOnlyNonNullAndNonEmpty("En", "To", "Tre")).isEqualTo("En To Tre");
		assertThat(StringUtil.joinOnlyNonNullAndNonEmpty("En", null, "Tre")).isEqualTo("En Tre");
		assertThat(StringUtil.joinOnlyNonNullAndNonEmpty("En", "To", "")).isEqualTo("En To");
		assertThat(StringUtil.joinOnlyNonNullAndNonEmpty('-', "En", "To", "")).isEqualTo("En-To");
	}
	
	@Test
	public void noneOfTheArgumentsAreBlank() {
		assertThat(StringUtil.isNotBlank("per")).isTrue();
		assertThat(StringUtil.isNotBlank("per", "pål")).isTrue();
		assertThat(StringUtil.isNotBlank("per", "pål", "espen askeladden")).isTrue();
	}

	@Test
	public void someOfTheArgumentsAreBlank() {
		assertThat(StringUtil.isNotBlank("per")).isTrue();
		assertThat(StringUtil.isNotBlank("per", " ")).isFalse();
		assertThat(StringUtil.isNotBlank("per", "", "espen askeladden")).isFalse();
	}

	@Test
	public void allOfTheArgumentsAreBlank() {
		assertThat(StringUtil.isNotBlank(" ")).isFalse();
		assertThat(StringUtil.isNotBlank(null, " ", "", null)).isFalse();
	}
}

