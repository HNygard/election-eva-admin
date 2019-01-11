package no.valg.eva.admin.configuration.test.data;

import static no.valg.eva.admin.common.test.data.AreaPathTestData.AREA_PATH_111111_11_11_1111;
import static no.valg.eva.admin.common.test.data.AreaPathTestData.AREA_PATH_111111_11_11_1111_111111;
import static no.valg.eva.admin.common.test.data.AreaPathTestData.AREA_PATH_111111_11_11_1111_111111_1111;
import static no.valg.eva.admin.common.test.data.AreaPathTestData.AREA_PATH_111111_11_11_1111_111111_1112;
import static no.valg.eva.admin.common.test.data.AreaPathTestData.AREA_PATH_111111_11_11_1111_111111_1113;
import static no.valg.eva.admin.common.test.data.AreaPathTestData.AREA_PATH_111111_11_11_1111_111112;
import static no.valg.eva.admin.common.test.data.AreaPathTestData.AREA_PATH_111111_11_11_1111_111113;
import static no.valg.eva.admin.common.test.data.AreaPathTestData.AREA_PATH_111111_11_11_1112;
import static no.valg.eva.admin.common.test.data.AreaPathTestData.AREA_PATH_111111_11_11_1113;
import static no.valg.eva.admin.common.test.data.AreaPathTestData.AREA_PATH_111111_47_11;
import static no.valg.eva.admin.common.test.data.AreaPathTestData.AREA_PATH_111111_47_12;
import static no.valg.eva.admin.common.test.data.AreaPathTestData.AREA_PATH_111111_47_13;
import static no.valg.eva.admin.felles.test.data.valggeografi.BydelTestData.BYDEL_NAVN_111111_11_11_1111_111111;
import static no.valg.eva.admin.felles.test.data.valggeografi.BydelTestData.BYDEL_NAVN_111111_11_11_1111_111112;
import static no.valg.eva.admin.felles.test.data.valggeografi.BydelTestData.BYDEL_NAVN_111111_11_11_1111_111113;
import static no.valg.eva.admin.felles.test.data.valggeografi.FylkeskommuneTestData.FYLKESKOMMUNE_NAVN_111111_47_11;
import static no.valg.eva.admin.felles.test.data.valggeografi.FylkeskommuneTestData.FYLKESKOMMUNE_NAVN_111111_47_12;
import static no.valg.eva.admin.felles.test.data.valggeografi.FylkeskommuneTestData.FYLKESKOMMUNE_NAVN_111111_47_13;
import static no.valg.eva.admin.felles.test.data.valggeografi.KommuneTestData.KOMMUNE_NAVN_111111_11_11_1111;
import static no.valg.eva.admin.felles.test.data.valggeografi.KommuneTestData.KOMMUNE_NAVN_111111_11_11_1112;
import static no.valg.eva.admin.felles.test.data.valggeografi.KommuneTestData.KOMMUNE_NAVN_111111_11_11_1113;
import static no.valg.eva.admin.felles.test.data.valggeografi.StemmekretsTestData.STEMMEKRETS_NAVN_111111_11_11_1111_111111_1111;
import static no.valg.eva.admin.felles.test.data.valggeografi.StemmekretsTestData.STEMMEKRETS_NAVN_111111_11_11_1111_111111_1112;
import static no.valg.eva.admin.felles.test.data.valggeografi.StemmekretsTestData.STEMMEKRETS_NAVN_111111_11_11_1111_111111_1113;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.configuration.domain.model.MvArea;

public final class MvAreaTestData {
	public static final MvArea MV_AREA_111111_47_11 = mvArea(AREA_PATH_111111_47_11, FYLKESKOMMUNE_NAVN_111111_47_11);
	public static final MvArea MV_AREA_111111_47_12 = mvArea(AREA_PATH_111111_47_12, FYLKESKOMMUNE_NAVN_111111_47_12);
	public static final MvArea MV_AREA_111111_47_13 = mvArea(AREA_PATH_111111_47_13, FYLKESKOMMUNE_NAVN_111111_47_13);

	public static final MvArea MV_AREA_111111_11_11_1111 = mvArea(AREA_PATH_111111_11_11_1111, KOMMUNE_NAVN_111111_11_11_1111);
	public static final MvArea MV_AREA_111111_11_11_1112 = mvArea(AREA_PATH_111111_11_11_1112, KOMMUNE_NAVN_111111_11_11_1112);
	public static final MvArea MV_AREA_111111_11_11_1113 = mvArea(AREA_PATH_111111_11_11_1113, KOMMUNE_NAVN_111111_11_11_1113);

	public static final MvArea MV_AREA_111111_11_11_1111_111111 = mvArea(AREA_PATH_111111_11_11_1111_111111, BYDEL_NAVN_111111_11_11_1111_111111);
	public static final MvArea MV_AREA_111111_11_11_1111_111112 = mvArea(AREA_PATH_111111_11_11_1111_111112, BYDEL_NAVN_111111_11_11_1111_111112);
	public static final MvArea MV_AREA_111111_11_11_1111_111113 = mvArea(AREA_PATH_111111_11_11_1111_111113, BYDEL_NAVN_111111_11_11_1111_111113);

	public static final MvArea MV_AREA_111111_11_11_1111_111111_1111 =
			mvArea(AREA_PATH_111111_11_11_1111_111111_1111, STEMMEKRETS_NAVN_111111_11_11_1111_111111_1111);
	public static final MvArea MV_AREA_111111_11_11_1111_111111_1112 =
			mvArea(AREA_PATH_111111_11_11_1111_111111_1112, STEMMEKRETS_NAVN_111111_11_11_1111_111111_1112);
	public static final MvArea MV_AREA_111111_11_11_1111_111111_1113 =
			mvArea(AREA_PATH_111111_11_11_1111_111111_1113, STEMMEKRETS_NAVN_111111_11_11_1111_111111_1113);

	private MvAreaTestData() {
	}

	private static MvArea mvArea(AreaPath areaPath, String navn) {
		MvArea mvArea = mock(MvArea.class, RETURNS_DEEP_STUBS);
		when(mvArea.areaPath()).thenReturn(areaPath);
		when(mvArea.getAreaName()).thenReturn(navn);
		return mvArea;
	}
}
