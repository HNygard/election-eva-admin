package no.valg.eva.admin.felles.test.data.valggeografi;

import static java.util.Arrays.asList;
import static no.valg.eva.admin.felles.test.data.sti.valggeografi.KommuneStiTestData.KOMMUNE_STI_111111_11_11_1111;
import static no.valg.eva.admin.felles.test.data.sti.valggeografi.KommuneStiTestData.KOMMUNE_STI_111111_11_11_1112;
import static no.valg.eva.admin.felles.test.data.sti.valggeografi.KommuneStiTestData.KOMMUNE_STI_111111_11_11_1113;

import java.util.List;

import no.valg.eva.admin.felles.sti.valggeografi.KommuneSti;
import no.valg.eva.admin.felles.valggeografi.model.Kommune;

public final class KommuneTestData {
	public static final String KOMMUNE_ID_1111 = "1111";
	public static final String KOMMUNE_ID_1112 = "1112";
	public static final String KOMMUNE_ID_1113 = "1113";
	public static final String KOMMUNE_NAVN_111111_11_11_1111 = "KOMMUNE_111111_11_11_1111";
	public static final String KOMMUNE_NAVN_111111_11_11_1112 = "KOMMUNE_111111_11_11_1112";
	public static final String KOMMUNE_NAVN_111111_11_11_1113 = "KOMMUNE_111111_11_11_1113";
	public static final Kommune KOMMUNE_111111_11_11_1111 = kommune(KOMMUNE_STI_111111_11_11_1111, KOMMUNE_NAVN_111111_11_11_1111);
	public static final Kommune KOMMUNE_111111_11_11_1112 = kommune(KOMMUNE_STI_111111_11_11_1112, KOMMUNE_NAVN_111111_11_11_1112);
	public static final Kommune KOMMUNE_111111_11_11_1113 = kommune(KOMMUNE_STI_111111_11_11_1113, KOMMUNE_NAVN_111111_11_11_1113);
	public static final List<Kommune> KOMMUNER_111111_11_11_111X = asList(KOMMUNE_111111_11_11_1111, KOMMUNE_111111_11_11_1112, KOMMUNE_111111_11_11_1113);

	private KommuneTestData() {
	}

	private static Kommune kommune(KommuneSti sti, String navn) {
		return new Kommune(sti, navn, false);
	}
}
