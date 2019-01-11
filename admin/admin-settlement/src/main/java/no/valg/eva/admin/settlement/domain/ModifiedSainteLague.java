package no.valg.eva.admin.settlement.domain;

import static java.math.BigDecimal.ONE;

import java.math.BigDecimal;

public class ModifiedSainteLague {
	private final BigDecimal firstDivisor;

	public ModifiedSainteLague(BigDecimal firstDivisor) {
		this.firstDivisor = firstDivisor;
	}

	public BigDecimal saintLagueDivisor(int rankNumber) {
		if (rankNumber == 1) {
			return firstDivisor;
		}
		return BigDecimal.valueOf(rankNumber - 1).multiply(BigDecimal.valueOf(2)).add(ONE);
	}
}
