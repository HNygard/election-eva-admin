package no.evote.security;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents the operator's security level, as defined in <a
 * href="http://www.regjeringen.no/nb/dep/kmd/dok/lover_regler/retningslinjer/2008/rammeverk-for-autentisering-og-uavviseli/4.html?id=505929"> Sikkerhetsnivåer
 * for autentisering og uavviselighet</a>.
 */
public enum SecurityLevel {
	NO_VALUE(null),
	NO_REQUIREMENTS(1),
	ONE_FACTOR(2),
	TWO_FACTOR_OF_WHICH_ONE_DYNAMIC(3),
	TWO_FACTOR_OF_WHICH_ONE_DYNAMIC_WITH_PKI(4);

	private static final Map<Integer, SecurityLevel> SECURITY_LEVEL_TO_INSTANCE_MAP = new HashMap<>();
	static {
		for (SecurityLevel securityLevel : SecurityLevel.values()) {
			if (!NO_VALUE.equals(securityLevel)) {
				SECURITY_LEVEL_TO_INSTANCE_MAP.put(securityLevel.getLevel(), securityLevel);
			}
		}
	}

	private Integer level;

	private SecurityLevel(Integer level) {
		this.level = level;
	}

	public Integer getLevel() {
		return level;
	}

	public static SecurityLevel fromLevel(Integer securityLevel) {
		if (securityLevel == null) {
			return NO_VALUE;
		}
		return SECURITY_LEVEL_TO_INSTANCE_MAP.get(securityLevel);
	}

	@Override
	public String toString() {
		if (NO_VALUE.equals(this)) {
			return name();
		}
		return name() + "(" + level + ")";
	}
}
