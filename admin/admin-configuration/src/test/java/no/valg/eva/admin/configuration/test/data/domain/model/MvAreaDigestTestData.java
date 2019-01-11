package no.valg.eva.admin.configuration.test.data.domain.model;

import static java.util.Arrays.asList;
import static no.valg.eva.admin.common.test.data.AreaPathTestData.AREA_PATH_111111;
import static no.valg.eva.admin.common.test.data.AreaPathTestData.AREA_PATH_111111_11;
import static no.valg.eva.admin.common.test.data.AreaPathTestData.AREA_PATH_111111_11_11;
import static no.valg.eva.admin.common.test.data.AreaPathTestData.AREA_PATH_111111_11_11_1111;
import static no.valg.eva.admin.common.test.data.AreaPathTestData.AREA_PATH_111111_11_11_1111_111111;
import static no.valg.eva.admin.common.test.data.AreaPathTestData.AREA_PATH_111111_11_11_1111_111111_1111;
import static no.valg.eva.admin.common.test.data.AreaPathTestData.AREA_PATH_111111_11_11_1111_111111_1111_1111;
import static no.valg.eva.admin.common.test.data.AreaPathTestData.AREA_PATH_111111_11_11_1111_111111_1111_1111_11;
import static no.valg.eva.admin.common.test.data.AreaPathTestData.AREA_PATH_111111_11_11_1111_111111_1111_1111_12;
import static no.valg.eva.admin.common.test.data.AreaPathTestData.AREA_PATH_111111_11_11_1111_111111_1111_1111_13;
import static no.valg.eva.admin.common.test.data.AreaPathTestData.AREA_PATH_111111_11_11_1111_111111_1111_1112;
import static no.valg.eva.admin.common.test.data.AreaPathTestData.AREA_PATH_111111_11_11_1111_111111_1111_1113;
import static no.valg.eva.admin.common.test.data.AreaPathTestData.AREA_PATH_111111_11_11_1111_111111_1112;
import static no.valg.eva.admin.common.test.data.AreaPathTestData.AREA_PATH_111111_11_11_1111_111111_1113;
import static no.valg.eva.admin.common.test.data.AreaPathTestData.AREA_PATH_111111_11_11_1111_111112;
import static no.valg.eva.admin.common.test.data.AreaPathTestData.AREA_PATH_111111_11_11_1111_111113;
import static no.valg.eva.admin.common.test.data.AreaPathTestData.AREA_PATH_111111_11_11_1112;
import static no.valg.eva.admin.common.test.data.AreaPathTestData.AREA_PATH_111111_11_11_1113;
import static no.valg.eva.admin.common.test.data.AreaPathTestData.AREA_PATH_111111_11_12;
import static no.valg.eva.admin.common.test.data.AreaPathTestData.AREA_PATH_111111_11_13;
import static no.valg.eva.admin.felles.test.data.valggeografi.BydelTestData.BYDEL_NAVN_111111_11_11_1111_111111;
import static no.valg.eva.admin.felles.test.data.valggeografi.BydelTestData.BYDEL_NAVN_111111_11_11_1111_111112;
import static no.valg.eva.admin.felles.test.data.valggeografi.BydelTestData.BYDEL_NAVN_111111_11_11_1111_111113;
import static no.valg.eva.admin.felles.test.data.valggeografi.FylkeskommuneTestData.FYLKESKOMMUNE_NAVN_111111_11_11;
import static no.valg.eva.admin.felles.test.data.valggeografi.FylkeskommuneTestData.FYLKESKOMMUNE_NAVN_111111_11_12;
import static no.valg.eva.admin.felles.test.data.valggeografi.FylkeskommuneTestData.FYLKESKOMMUNE_NAVN_111111_11_13;
import static no.valg.eva.admin.felles.test.data.valggeografi.KommuneTestData.KOMMUNE_NAVN_111111_11_11_1111;
import static no.valg.eva.admin.felles.test.data.valggeografi.KommuneTestData.KOMMUNE_NAVN_111111_11_11_1112;
import static no.valg.eva.admin.felles.test.data.valggeografi.KommuneTestData.KOMMUNE_NAVN_111111_11_11_1113;
import static no.valg.eva.admin.felles.test.data.valggeografi.LandTestData.LAND_NAVN_111111_11;
import static no.valg.eva.admin.felles.test.data.valggeografi.RodeTestData.RODE_NAVN_111111_11_11_1111_111111_1111_1111_11;
import static no.valg.eva.admin.felles.test.data.valggeografi.RodeTestData.RODE_NAVN_111111_11_11_1111_111111_1111_1111_12;
import static no.valg.eva.admin.felles.test.data.valggeografi.RodeTestData.RODE_NAVN_111111_11_11_1111_111111_1111_1111_13;
import static no.valg.eva.admin.felles.test.data.valggeografi.StemmekretsTestData.STEMMEKRETS_NAVN_111111_11_11_1111_111111_1111;
import static no.valg.eva.admin.felles.test.data.valggeografi.StemmekretsTestData.STEMMEKRETS_NAVN_111111_11_11_1111_111111_1112;
import static no.valg.eva.admin.felles.test.data.valggeografi.StemmekretsTestData.STEMMEKRETS_NAVN_111111_11_11_1111_111111_1113;
import static no.valg.eva.admin.felles.test.data.valggeografi.StemmestedTestData.STEMMESTED_NAVN_111111_11_11_1111_111111_1111_1111;
import static no.valg.eva.admin.felles.test.data.valggeografi.StemmestedTestData.STEMMESTED_NAVN_111111_11_11_1111_111111_1111_1112;
import static no.valg.eva.admin.felles.test.data.valggeografi.StemmestedTestData.STEMMESTED_NAVN_111111_11_11_1111_111111_1111_1113;
import static no.valg.eva.admin.felles.test.data.valggeografi.ValghendelseTestData.VALGHENDELSE_NAVN_111111;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.configuration.domain.model.MvAreaDigest;
import no.valg.eva.admin.configuration.domain.model.PollingDistrictType;
import no.valg.eva.admin.felles.sti.valggeografi.ValggeografiSti;
import no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa;

public final class MvAreaDigestTestData {
	public static final MvAreaDigest MV_AREA_DIGEST_111111 = mvAreaDigest(AREA_PATH_111111, VALGHENDELSE_NAVN_111111);

	public static final MvAreaDigest MV_AREA_DIGEST_111111_11 = mvAreaDigest(AREA_PATH_111111_11, LAND_NAVN_111111_11);

	public static final MvAreaDigest MV_AREA_DIGEST_111111_11_11 = mvAreaDigest(AREA_PATH_111111_11_11, FYLKESKOMMUNE_NAVN_111111_11_11);
	public static final MvAreaDigest MV_AREA_DIGEST_111111_11_12 = mvAreaDigest(AREA_PATH_111111_11_12, FYLKESKOMMUNE_NAVN_111111_11_12);
	public static final MvAreaDigest MV_AREA_DIGEST_111111_11_13 = mvAreaDigest(AREA_PATH_111111_11_13, FYLKESKOMMUNE_NAVN_111111_11_13);

	public static final MvAreaDigest MV_AREA_DIGEST_111111_11_11_1111 = mvAreaDigest(AREA_PATH_111111_11_11_1111, KOMMUNE_NAVN_111111_11_11_1111);
	public static final MvAreaDigest MV_AREA_DIGEST_111111_11_11_1112 = mvAreaDigest(AREA_PATH_111111_11_11_1112, KOMMUNE_NAVN_111111_11_11_1112);
	public static final MvAreaDigest MV_AREA_DIGEST_111111_11_11_1113 = mvAreaDigest(AREA_PATH_111111_11_11_1113, KOMMUNE_NAVN_111111_11_11_1113);

	public static final MvAreaDigest MV_AREA_DIGEST_111111_11_11_1111_111111 = mvAreaDigest(AREA_PATH_111111_11_11_1111_111111, BYDEL_NAVN_111111_11_11_1111_111111);
	public static final MvAreaDigest MV_AREA_DIGEST_111111_11_11_1111_111112 = mvAreaDigest(AREA_PATH_111111_11_11_1111_111112, BYDEL_NAVN_111111_11_11_1111_111112);
	public static final MvAreaDigest MV_AREA_DIGEST_111111_11_11_1111_111113 = mvAreaDigest(AREA_PATH_111111_11_11_1111_111113, BYDEL_NAVN_111111_11_11_1111_111113);

	public static final MvAreaDigest MV_AREA_DIGEST_111111_11_11_1111_111111_1111 =
			mvAreaDigest(AREA_PATH_111111_11_11_1111_111111_1111, STEMMEKRETS_NAVN_111111_11_11_1111_111111_1111);
	public static final MvAreaDigest MV_AREA_DIGEST_111111_11_11_1111_111111_1112 =
			mvAreaDigest(AREA_PATH_111111_11_11_1111_111111_1112, STEMMEKRETS_NAVN_111111_11_11_1111_111111_1112);
	public static final MvAreaDigest MV_AREA_DIGEST_111111_11_11_1111_111111_1113 =
			mvAreaDigest(AREA_PATH_111111_11_11_1111_111111_1113, STEMMEKRETS_NAVN_111111_11_11_1111_111111_1113);

	public static final MvAreaDigest MV_AREA_DIGEST_111111_11_11_1111_111111_1111_1111 =
			mvAreaDigest(AREA_PATH_111111_11_11_1111_111111_1111_1111, STEMMESTED_NAVN_111111_11_11_1111_111111_1111_1111);
	public static final MvAreaDigest MV_AREA_DIGEST_111111_11_11_1111_111111_1111_1112 =
			mvAreaDigest(AREA_PATH_111111_11_11_1111_111111_1111_1112, STEMMESTED_NAVN_111111_11_11_1111_111111_1111_1112);
	public static final MvAreaDigest MV_AREA_DIGEST_111111_11_11_1111_111111_1111_1113 =
			mvAreaDigest(AREA_PATH_111111_11_11_1111_111111_1111_1113, STEMMESTED_NAVN_111111_11_11_1111_111111_1111_1113);
	
	public static final MvAreaDigest MV_AREA_DIGEST_111111_11_11_1111_111111_1111_1111_11 =
			mvAreaDigest(AREA_PATH_111111_11_11_1111_111111_1111_1111_11, RODE_NAVN_111111_11_11_1111_111111_1111_1111_11);
	public static final MvAreaDigest MV_AREA_DIGEST_111111_11_11_1111_111111_1111_1111_12 =
			mvAreaDigest(AREA_PATH_111111_11_11_1111_111111_1111_1111_12, RODE_NAVN_111111_11_11_1111_111111_1111_1111_12);
	public static final MvAreaDigest MV_AREA_DIGEST_111111_11_11_1111_111111_1111_1111_13 =
			mvAreaDigest(AREA_PATH_111111_11_11_1111_111111_1111_1111_13, RODE_NAVN_111111_11_11_1111_111111_1111_1111_13);
	
	public static final List<MvAreaDigest> MV_AREA_DIGESTS_111111_11_1X =
			asList(MV_AREA_DIGEST_111111_11_11, MV_AREA_DIGEST_111111_11_12, MV_AREA_DIGEST_111111_11_13);
	public static final List<MvAreaDigest> MV_AREA_DIGESTS_111111_11_11_111X =
			asList(MV_AREA_DIGEST_111111_11_11_1111, MV_AREA_DIGEST_111111_11_11_1112, MV_AREA_DIGEST_111111_11_11_1113);
	public static final List<MvAreaDigest> MV_AREA_DIGESTS_111111_11_11_1111_11111X =
			asList(MV_AREA_DIGEST_111111_11_11_1111_111111, MV_AREA_DIGEST_111111_11_11_1111_111112, MV_AREA_DIGEST_111111_11_11_1111_111113);
	public static final List<MvAreaDigest> MV_AREA_DIGESTS_111111_11_11_1111_111111_111X =
			asList(MV_AREA_DIGEST_111111_11_11_1111_111111_1111, MV_AREA_DIGEST_111111_11_11_1111_111111_1112, MV_AREA_DIGEST_111111_11_11_1111_111111_1113);
	public static final List<MvAreaDigest> MV_AREA_DIGESTS_111111_11_11_1111_111111_1111_111X =
			asList(MV_AREA_DIGEST_111111_11_11_1111_111111_1111_1111, MV_AREA_DIGEST_111111_11_11_1111_111111_1111_1112,
					MV_AREA_DIGEST_111111_11_11_1111_111111_1111_1113);
	public static final List<MvAreaDigest> MV_AREA_DIGESTS_111111_11_11_1111_111111_1111_1111_1X =
			asList(MV_AREA_DIGEST_111111_11_11_1111_111111_1111_1111_11, MV_AREA_DIGEST_111111_11_11_1111_111111_1111_1111_12,
					MV_AREA_DIGEST_111111_11_11_1111_111111_1111_1111_13);

	private MvAreaDigestTestData() {
	}

	public static MvAreaDigest mvAreaDigest(AreaPath areaPath, String areaName) {
		return mvAreaDigest(areaPath, areaName, null);
	}

	public static MvAreaDigest mvAreaDigest(AreaPath areaPath, String areaName, PollingDistrictType pollingDistrictType) {
		MvAreaDigest mvAreaDigest = mock(MvAreaDigest.class, RETURNS_DEEP_STUBS);
		when(mvAreaDigest.areaPath()).thenReturn(areaPath);
		when(mvAreaDigest.areaName()).thenReturn(areaName);
		ValggeografiSti valggeografiSti = ValggeografiSti.fra(areaPath);
		ValggeografiNivaa valggeografiNivaa = valggeografiSti.nivaa();
		when(mvAreaDigest.valggeografiNivaa()).thenReturn(valggeografiNivaa);
		when(mvAreaDigest.valggeografiSti()).thenReturn(valggeografiSti);
		if (pollingDistrictType != null) {
			when(mvAreaDigest.getPollingDistrictDigest().type()).thenReturn(pollingDistrictType);
		}
		return mvAreaDigest;
	}
}
