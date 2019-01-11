package no.evote.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.Test;

public class SecurityLevelTest {
	@Test
	public void getLevel_returnsLevelValue() {
		
		assertThat(SecurityLevel.NO_REQUIREMENTS.getLevel()).isEqualTo(1);
		assertThat(SecurityLevel.ONE_FACTOR.getLevel()).isEqualTo(2);
		assertThat(SecurityLevel.TWO_FACTOR_OF_WHICH_ONE_DYNAMIC.getLevel()).isEqualTo(3);
		assertThat(SecurityLevel.TWO_FACTOR_OF_WHICH_ONE_DYNAMIC_WITH_PKI.getLevel()).isEqualTo(4);
		assertThat(SecurityLevel.NO_VALUE.getLevel()).isNull();
		
	}

	@Test
	public void fromLevel_returnsEnumValue() {
		
		assertThat(SecurityLevel.fromLevel(1)).isEqualTo(SecurityLevel.NO_REQUIREMENTS);
		assertThat(SecurityLevel.fromLevel(2)).isEqualTo(SecurityLevel.ONE_FACTOR);
		assertThat(SecurityLevel.fromLevel(3)).isEqualTo(SecurityLevel.TWO_FACTOR_OF_WHICH_ONE_DYNAMIC);
		assertThat(SecurityLevel.fromLevel(4)).isEqualTo(SecurityLevel.TWO_FACTOR_OF_WHICH_ONE_DYNAMIC_WITH_PKI);
		assertThat(SecurityLevel.fromLevel(null)).isEqualTo(SecurityLevel.NO_VALUE);
		
	}

	@Test
	public void toString_returnsNameOfEnumAndLevelValue() {
		assertThat(SecurityLevel.TWO_FACTOR_OF_WHICH_ONE_DYNAMIC.toString()).isEqualTo("TWO_FACTOR_OF_WHICH_ONE_DYNAMIC(3)");
		assertThat(SecurityLevel.NO_VALUE.toString()).isEqualTo("NO_VALUE");
	}
}
