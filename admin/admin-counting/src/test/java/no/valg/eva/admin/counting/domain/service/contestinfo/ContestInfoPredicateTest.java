package no.valg.eva.admin.counting.domain.service.contestinfo;

import static org.assertj.core.api.Assertions.assertThat;

import no.evote.constants.AreaLevelEnum;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.counting.model.ContestInfo;

import org.testng.annotations.Test;

public class ContestInfoPredicateTest {

	private static final AreaPath AREA_PATH_COUNTY = new AreaPath("150001.47.01");
	private static final AreaPath AREA_PATH_MUNICIPALITY = new AreaPath("150001.47.01.0101");
	private static final AreaPath AREA_PATH_BOROUGH = new AreaPath("150001.47.01.0101.010100");

	@Test
	public void test_areaLevelFylke_true() {
		ContestInfoPredicate contestInfoPredicate = new ContestInfoPredicate(AreaLevelEnum.COUNTY);
		ContestInfo contestInfo = new ContestInfo(null, "", "", AREA_PATH_COUNTY);
		
		assertThat(contestInfoPredicate.test(contestInfo)).isTrue();
	}

	@Test
	public void test_areaLevelFylke_false() {
		ContestInfoPredicate contestInfoPredicate = new ContestInfoPredicate(AreaLevelEnum.MUNICIPALITY);
		ContestInfo contestInfo = new ContestInfo(null, "", "", AREA_PATH_COUNTY);
		
		assertThat(contestInfoPredicate.test(contestInfo)).isFalse();
	}

	@Test
	public void test_areaLevelKommune_true() {
		ContestInfoPredicate contestInfoPredicate = new ContestInfoPredicate(AreaLevelEnum.MUNICIPALITY);
		ContestInfo contestInfo = new ContestInfo(null, "", "", AREA_PATH_MUNICIPALITY);
		
		assertThat(contestInfoPredicate.test(contestInfo)).isTrue();
	}

	@Test
	public void test_areaLevelKommune_false() {
		ContestInfoPredicate contestInfoPredicate = new ContestInfoPredicate(AreaLevelEnum.COUNTY);
		ContestInfo contestInfo = new ContestInfo(null, "", "", AREA_PATH_MUNICIPALITY);
		
		assertThat(contestInfoPredicate.test(contestInfo)).isFalse();
	}

	@Test
	public void test_areaLevelKommuneMenBydel_true() {
		ContestInfoPredicate contestInfoPredicate = new ContestInfoPredicate(AreaLevelEnum.MUNICIPALITY);
		ContestInfo contestInfo = new ContestInfo(null, "", "", AREA_PATH_BOROUGH);
		
		assertThat(contestInfoPredicate.test(contestInfo)).isTrue();
	}

	@Test
	public void test_areaLevelNull_true() {
		ContestInfoPredicate contestInfoPredicate = new ContestInfoPredicate(null);
		ContestInfo contestInfo = new ContestInfo(null, "", "", AREA_PATH_MUNICIPALITY);

		assertThat(contestInfoPredicate.test(contestInfo)).isTrue();
	}

	@Test
	public void test_areaLevelRoot_true() {
		ContestInfoPredicate contestInfoPredicate = new ContestInfoPredicate(AreaLevelEnum.ROOT);
		ContestInfo contestInfo = new ContestInfo(null, "", "", AREA_PATH_MUNICIPALITY);

		assertThat(contestInfoPredicate.test(contestInfo)).isTrue();
	}
}
