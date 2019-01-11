package no.valg.eva.admin.util;

import org.assertj.core.api.Assertions;
import org.testng.annotations.Test;


public class LangUtilTest {

	@Test
	public void zeroIfNull_whenInputIsANullInteger_zeroIsReturned() {
		Integer integerNull = null;
		Assertions.assertThat(LangUtil.zeroIfNull(integerNull)).isEqualTo(0);
	}

	@Test
	public void zeroIfNull_whenInputIsNonNull_theSameObjectIsReturned() {
		Assertions.assertThat(LangUtil.zeroIfNull(Integer.valueOf(4))).isEqualTo(4);
	}
	
}

