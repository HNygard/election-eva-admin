package no.valg.eva.admin.settlement.domain.model.factory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import no.valg.eva.admin.common.Randomizer;
import no.valg.eva.admin.settlement.domain.model.CandidateSeat;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class CandidateSeatComparatorTest {
	@Test
	public void compare_gittIdentiskeCandidateSeat_returnerZero() throws Exception {
		CandidateSeat candidateSeat = mock(CandidateSeat.class);
		assertThat(new CandidateSeatComparator(Randomizer.INSTANCE).compare(candidateSeat, candidateSeat)).isZero();
	}

	@Test(dataProvider = "quotientDataStorsteVerdiForst")
	public void compare_gittToCandidateSeatMedForskjelligQuotient_returnererIkkeZero(BigDecimal quotient1, BigDecimal quotient2, int compareResultat) throws Exception {
		CandidateSeat candidateSeat1 = mock(CandidateSeat.class);
		CandidateSeat candidateSeat2 = mock(CandidateSeat.class);
		when(candidateSeat1.getQuotient()).thenReturn(quotient1);
		when(candidateSeat2.getQuotient()).thenReturn(quotient2);
		assertThat(new CandidateSeatComparator(Randomizer.INSTANCE).compare(candidateSeat1, candidateSeat2)).isEqualTo(compareResultat);
	}

	@Test(dataProvider = "dividendDataStorsteVerdiForst")
	public void compare_gittToCandidateSeatMedLikQuotientOgForskjelligDividend_returnererIkkeZero(int dividend1, int dividend2, int compareResultat) throws Exception {
		CandidateSeat candidateSeat1 = mock(CandidateSeat.class);
		CandidateSeat candidateSeat2 = mock(CandidateSeat.class);
		when(candidateSeat1.getQuotient()).thenReturn(new BigDecimal("1.0"));
		when(candidateSeat2.getQuotient()).thenReturn(new BigDecimal("1.0"));
		when(candidateSeat1.getDividend()).thenReturn(dividend1);
		when(candidateSeat2.getDividend()).thenReturn(dividend2);
		assertThat(new CandidateSeatComparator(Randomizer.INSTANCE).compare(candidateSeat1, candidateSeat2)).isEqualTo(compareResultat);
	}

	@Test(dataProvider = "tilfeldigRekkefolgeDataTrueAngirForsteElement")
	public void compare_gittToCandidateSeatMedLikQuotientOgLikDividend_returnererTilfeldigRekkefolge(
			int firstInt, int secondInt, int compareResultat) throws Exception {
		CandidateSeat candidateSeat1 = mock(CandidateSeat.class);
		CandidateSeat candidateSeat2 = mock(CandidateSeat.class);
		Randomizer randomizer = mock(Randomizer.class);
		when(candidateSeat1.getQuotient()).thenReturn(new BigDecimal("1.0"));
		when(candidateSeat2.getQuotient()).thenReturn(new BigDecimal("1.0"));
		when(candidateSeat1.getDividend()).thenReturn(1);
		when(candidateSeat2.getDividend()).thenReturn(1);
		when(randomizer.nextInt()).thenReturn(firstInt, secondInt);
		assertThat(new CandidateSeatComparator(randomizer).compare(candidateSeat1, candidateSeat2)).isEqualTo(compareResultat);
	}

	@Test
	public void compare_gittToCandidateSeatMedLikQuotientOgLikDividend_huskerTilfeldigRekkefolgeAngittVedForsteKall() throws Exception {
		CandidateSeat candidateSeat1 = new CandidateSeat();
		candidateSeat1.setPk(1L);
		candidateSeat1.setDividend(1);
		candidateSeat1.setDivisor(BigDecimal.ONE);
		CandidateSeat candidateSeat2 = new CandidateSeat();
		candidateSeat2.setPk(2L);
		candidateSeat2.setDividend(1);
		candidateSeat2.setDivisor(BigDecimal.ONE);
		Randomizer randomizer = mock(Randomizer.class);
		when(randomizer.nextInt()).thenReturn(1, 2);
		CandidateSeatComparator comparator = new CandidateSeatComparator(randomizer);
		assertThat(comparator.compare(candidateSeat1, candidateSeat2)).isEqualTo(-1);
		assertThat(comparator.compare(candidateSeat1, candidateSeat2)).isEqualTo(-1);
		assertThat(comparator.compare(candidateSeat2, candidateSeat1)).isEqualTo(1);
	}

	@Test
	public void compare_gittTreCandidateSeatMedLikQuotientOgLikDividend_transitivRekkefolgeBlirRiktig() throws Exception {
		CandidateSeat candidateSeat1 = new CandidateSeat();
		candidateSeat1.setPk(1L);
		candidateSeat1.setDividend(1);
		candidateSeat1.setDivisor(BigDecimal.ONE);
		CandidateSeat candidateSeat2 = new CandidateSeat();
		candidateSeat2.setPk(2L);
		candidateSeat2.setDividend(1);
		candidateSeat2.setDivisor(BigDecimal.ONE);
		CandidateSeat candidateSeat3 = new CandidateSeat();
		
		candidateSeat3.setPk(3L);
		
		candidateSeat3.setDividend(1);
		candidateSeat3.setDivisor(BigDecimal.ONE);
		Randomizer randomizer = mock(Randomizer.class);
		when(randomizer.nextInt()).thenReturn(1, 2, 3);
		CandidateSeatComparator comparator = new CandidateSeatComparator(randomizer);
		assertThat(comparator.compare(candidateSeat1, candidateSeat2)).isNegative();
		assertThat(comparator.compare(candidateSeat2, candidateSeat3)).isNegative();
		assertThat(comparator.compare(candidateSeat3, candidateSeat1)).isPositive();
	}

	@DataProvider
	public Object[][] quotientDataStorsteVerdiForst() {
		BigDecimal en = new BigDecimal("1.0");
		BigDecimal to = new BigDecimal("2.0");
		return new Object[][]{
				new Object[]{en, to, to.compareTo(en)},
				new Object[]{to, en, en.compareTo(to)}
		};
	}

	@DataProvider
	public Object[][] dividendDataStorsteVerdiForst() {
		return new Object[][]{
				new Object[]{1, 2, 2 - 1},
				new Object[]{2, 1, 1 - 2}
		};
	}

	@DataProvider
	public Object[][] tilfeldigRekkefolgeDataTrueAngirForsteElement() {
		return new Object[][]{
				new Object[]{1, 2, -1},
				new Object[]{2, 1, 1}
		};
	}
}
