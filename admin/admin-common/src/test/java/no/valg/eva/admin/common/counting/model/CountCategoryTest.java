package no.valg.eva.admin.common.counting.model;

import no.valg.eva.admin.common.counting.constants.CountingMode;
import no.valg.eva.admin.configuration.domain.model.PollingDistrict;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Set;

import static no.valg.eva.admin.common.counting.model.CountCategory.FO;
import static no.valg.eva.admin.common.counting.model.CountCategory.FS;
import static no.valg.eva.admin.common.counting.model.CountCategory.VB;
import static no.valg.eva.admin.common.counting.model.CountCategory.VF;
import static no.valg.eva.admin.common.counting.model.CountCategory.VO;
import static no.valg.eva.admin.common.counting.model.CountCategory.VS;
import static org.assertj.core.api.Assertions.assertThat;

public class CountCategoryTest {

	private static final boolean FORHAND = true;
	private static final boolean FORELOPIGE = true;
	private static final boolean KOMMUNEKRETSEN = true;
	private static final boolean TEKNISK_KRETS = true;
	private static final boolean TEKNISKE_KRETSER = true;
	private static final boolean VO_SENTRALT_SAMLET = true;
	private static final boolean VF_SENTRALT_SAMLET = true;
	private static final boolean XIM = true;

	@Test(dataProvider = "countCategoryTestData")
	public void fromId_translatesIntoCorrectCountCategory(CountCategory expectedCountCategory, String id) {
		CountCategory category = CountCategory.fromId(id);
		assertThat(category).isSameAs(expectedCountCategory);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void fromIdShouldThrowExceptionOnInvalidCategoryId() {
		CountCategory.fromId("XX");
	}

	@Test(dataProvider = "countCategoryTestData")
	public void messageProperty_translatesFromCountCategoryToMessageId(CountCategory countCategory, String expectedMessageIdSubstring) {
		String messageProperty = countCategory.messageProperty();
		assertThat(messageProperty).isEqualTo("@vote_count_category[" + expectedMessageIdSubstring + "].name");
	}

	@Test(dataProvider = "countCategoryTestData")
	public void getId_translatesCategoryIntoId(CountCategory countCategory, String expectedId) {
		String id = countCategory.getId();
		assertThat(id).isEqualTo(expectedId);
	}

	@Test(dataProvider = "finnOpptellingskategorierTestData")
	public void finnOpptellingskategorier_opptellingskatagori(boolean erKommunekretsen, Boolean voSentraltSamlet, Boolean vfSentraltSamlet,
			boolean forelopige, boolean forhaand, boolean erXim, boolean erTekniskKrets,
			boolean harTekniskeKretser, CountCategory[] forventedeKategorier) {
		Set<CountCategory> countCategories = CountCategory.finnOpptellingskategorier(erKommunekretsen, voSentraltSamlet, vfSentraltSamlet, forelopige, forhaand,
				erXim, erTekniskKrets, harTekniskeKretser);
		assertThat(countCategories).containsExactlyInAnyOrder(forventedeKategorier);
	}

	@Test(dataProvider = "ulovligeKombinasjoner", expectedExceptions = IllegalArgumentException.class)
	public void finnOpptellingskategorier_tekniskeKretsIKommuneUtenTekniskeKretser_kasterIllegalArgumentException(boolean erKommunekretsen,
			Boolean voSentraltSamlet, Boolean vfSentralSamlet,
			boolean forelopige, boolean forhaand, boolean erXim,
			boolean erTekniskKrets, boolean harTekniskeKretser)
			throws IllegalArgumentException {
		CountCategory.finnOpptellingskategorier(erKommunekretsen, voSentraltSamlet, vfSentralSamlet, forelopige, forhaand, erXim, erTekniskKrets,
				harTekniskeKretser);
	}

	@DataProvider
	public Object[][] countCategoryTestData() {
		return new Object[][] {
				{ VO, "VO" },
				{ VF, "VF" },
				{ VS, "VS" },
				{ VB, "VB" },
				{ FO, "FO" },
				{ FS, "FS" }
		};
	}

	/**
	 * Se kommentar i {@link CountCategory#finnOpptellingskategorier(PollingDistrict, boolean, boolean, CountingMode, CountingMode)}
	 */
	@DataProvider
	public Object[][] finnOpptellingskategorierTestData() {
		return new Object[][] {
			// @formatter:off
			// 0000 i samlet kommune, foreløpig
			{KOMMUNEKRETSEN, VO_SENTRALT_SAMLET, VF_SENTRALT_SAMLET, FORELOPIGE, FORHAND, XIM, !TEKNISK_KRETS, !TEKNISKE_KRETSER, new CountCategory[]{FO}},
			{KOMMUNEKRETSEN, VO_SENTRALT_SAMLET, VF_SENTRALT_SAMLET, FORELOPIGE, !FORHAND, XIM, !TEKNISK_KRETS, !TEKNISKE_KRETSER, new CountCategory[]{VO}},
			{KOMMUNEKRETSEN, VO_SENTRALT_SAMLET, VF_SENTRALT_SAMLET, FORELOPIGE, !FORHAND, !XIM, !TEKNISK_KRETS, !TEKNISKE_KRETSER, new CountCategory[]{VO, VF}},
			{KOMMUNEKRETSEN, VO_SENTRALT_SAMLET, VF_SENTRALT_SAMLET, FORELOPIGE, FORHAND, XIM, !TEKNISK_KRETS, TEKNISKE_KRETSER, new CountCategory[]{}},

			// 0000 i samlet kommune, endelig
			{KOMMUNEKRETSEN, VO_SENTRALT_SAMLET, VF_SENTRALT_SAMLET, !FORELOPIGE, FORHAND, XIM, !TEKNISK_KRETS, !TEKNISKE_KRETSER, new CountCategory[]{FO, FS}},
			{KOMMUNEKRETSEN, VO_SENTRALT_SAMLET, VF_SENTRALT_SAMLET, !FORELOPIGE, !FORHAND, XIM, !TEKNISK_KRETS, !TEKNISKE_KRETSER, new CountCategory[]{VO, VS,
					VB}},
			{KOMMUNEKRETSEN, VO_SENTRALT_SAMLET, VF_SENTRALT_SAMLET, !FORELOPIGE, !FORHAND, !XIM, !TEKNISK_KRETS, !TEKNISKE_KRETSER, new CountCategory[]{VO, VS,
					VF}},
			{KOMMUNEKRETSEN, VO_SENTRALT_SAMLET, VF_SENTRALT_SAMLET, !FORELOPIGE, FORHAND, XIM, !TEKNISK_KRETS, TEKNISKE_KRETSER, new CountCategory[]{FS}},

			// 0000 i kretsfordelt kommune, foreløpig
			{KOMMUNEKRETSEN, !VO_SENTRALT_SAMLET, VF_SENTRALT_SAMLET, FORELOPIGE, FORHAND, XIM, !TEKNISK_KRETS, !TEKNISKE_KRETSER, new CountCategory[]{FO}},
			{KOMMUNEKRETSEN, !VO_SENTRALT_SAMLET, VF_SENTRALT_SAMLET, FORELOPIGE, !FORHAND, XIM, !TEKNISK_KRETS, !TEKNISKE_KRETSER, new CountCategory[]{}},
			{KOMMUNEKRETSEN, !VO_SENTRALT_SAMLET, VF_SENTRALT_SAMLET, FORELOPIGE, !FORHAND, !XIM, !TEKNISK_KRETS, !TEKNISKE_KRETSER, new CountCategory[]{}},
			{KOMMUNEKRETSEN, !VO_SENTRALT_SAMLET, VF_SENTRALT_SAMLET, FORELOPIGE, FORHAND, !XIM, !TEKNISK_KRETS, TEKNISKE_KRETSER, new CountCategory[]{}},

			// 0000 i kretsfordelt kommune, endelig
			{KOMMUNEKRETSEN, !VO_SENTRALT_SAMLET, VF_SENTRALT_SAMLET, !FORELOPIGE, FORHAND, XIM, !TEKNISK_KRETS, !TEKNISKE_KRETSER, new CountCategory[]{FO, FS}},
			{KOMMUNEKRETSEN, !VO_SENTRALT_SAMLET, VF_SENTRALT_SAMLET, !FORELOPIGE, !FORHAND, XIM, !TEKNISK_KRETS, !TEKNISKE_KRETSER, new CountCategory[]{VS, VB}},
			{KOMMUNEKRETSEN, !VO_SENTRALT_SAMLET, VF_SENTRALT_SAMLET, !FORELOPIGE, !FORHAND, !XIM, !TEKNISK_KRETS, !TEKNISKE_KRETSER, new CountCategory[]{VS, VF}},
			{KOMMUNEKRETSEN, !VO_SENTRALT_SAMLET, VF_SENTRALT_SAMLET, !FORELOPIGE, FORHAND, XIM, !TEKNISK_KRETS, TEKNISKE_KRETSER, new CountCategory[]{FS}},

			// Ikke 0000 i kretsfordelt kommuner, foreløpig
			{!KOMMUNEKRETSEN, !VO_SENTRALT_SAMLET, VF_SENTRALT_SAMLET, FORELOPIGE, FORHAND, XIM, !TEKNISK_KRETS, !TEKNISKE_KRETSER, new CountCategory[]{}},
			{!KOMMUNEKRETSEN, !VO_SENTRALT_SAMLET, VF_SENTRALT_SAMLET, FORELOPIGE, !FORHAND, XIM, !TEKNISK_KRETS, !TEKNISKE_KRETSER, new CountCategory[]{VO}},
			{!KOMMUNEKRETSEN, !VO_SENTRALT_SAMLET, VF_SENTRALT_SAMLET, FORELOPIGE, !FORHAND, !XIM, !TEKNISK_KRETS, !TEKNISKE_KRETSER, new CountCategory[]{VO}},
			{!KOMMUNEKRETSEN, !VO_SENTRALT_SAMLET, VF_SENTRALT_SAMLET, FORELOPIGE, FORHAND, XIM, TEKNISK_KRETS, TEKNISKE_KRETSER, new CountCategory[]{FO}},

			// Ikke 0000 i kretsfordelt kommuner, endelig
			{!KOMMUNEKRETSEN, !VO_SENTRALT_SAMLET, VF_SENTRALT_SAMLET, !FORELOPIGE, FORHAND, XIM, !TEKNISK_KRETS, !TEKNISKE_KRETSER, new CountCategory[]{}},
			{!KOMMUNEKRETSEN, !VO_SENTRALT_SAMLET, VF_SENTRALT_SAMLET, !FORELOPIGE, !FORHAND, XIM, !TEKNISK_KRETS, !TEKNISKE_KRETSER, new CountCategory[]{VO}},
			{!KOMMUNEKRETSEN, !VO_SENTRALT_SAMLET, VF_SENTRALT_SAMLET, !FORELOPIGE, !FORHAND, !XIM, !TEKNISK_KRETS, !TEKNISKE_KRETSER, new CountCategory[]{VO}},
			{!KOMMUNEKRETSEN, !VO_SENTRALT_SAMLET, VF_SENTRALT_SAMLET, !FORELOPIGE, FORHAND, XIM, TEKNISK_KRETS, TEKNISKE_KRETSER, new CountCategory[]{FO}},

			// Tekniske kretser får kun FO...
			{!KOMMUNEKRETSEN, VO_SENTRALT_SAMLET, VF_SENTRALT_SAMLET, FORELOPIGE, FORHAND, XIM, TEKNISK_KRETS, TEKNISKE_KRETSER, new CountCategory[]{FO}},

			// Tester VF når VF er kretsfordelt
			{!KOMMUNEKRETSEN, !VO_SENTRALT_SAMLET, !VF_SENTRALT_SAMLET, FORELOPIGE, !FORHAND, !XIM, !TEKNISK_KRETS, !TEKNISKE_KRETSER, new CountCategory[]{VO, VF}},
			{!KOMMUNEKRETSEN, !VO_SENTRALT_SAMLET, !VF_SENTRALT_SAMLET, !FORELOPIGE, !FORHAND, !XIM, !TEKNISK_KRETS, !TEKNISKE_KRETSER, new CountCategory[]{VO, VF}},
			{KOMMUNEKRETSEN, !VO_SENTRALT_SAMLET, !VF_SENTRALT_SAMLET, FORELOPIGE, !FORHAND, !XIM, !TEKNISK_KRETS, !TEKNISKE_KRETSER, new CountCategory[]{}},
			{KOMMUNEKRETSEN, !VO_SENTRALT_SAMLET, !VF_SENTRALT_SAMLET, !FORELOPIGE, !FORHAND, !XIM, !TEKNISK_KRETS, !TEKNISKE_KRETSER, new CountCategory[]{VS}},
			{KOMMUNEKRETSEN, VO_SENTRALT_SAMLET, !VF_SENTRALT_SAMLET, FORELOPIGE, !FORHAND, !XIM, !TEKNISK_KRETS, !TEKNISKE_KRETSER, new CountCategory[]{VO}},
			{KOMMUNEKRETSEN, VO_SENTRALT_SAMLET, !VF_SENTRALT_SAMLET, !FORELOPIGE, !FORHAND, !XIM, !TEKNISK_KRETS, !TEKNISKE_KRETSER, new CountCategory[]{VO, VS}},
			
			// Sameting
			{KOMMUNEKRETSEN, null, null, !FORELOPIGE, FORHAND, !XIM, !TEKNISK_KRETS, !TEKNISKE_KRETSER, new CountCategory[]{FO, FS}},
			{KOMMUNEKRETSEN, null, null, !FORELOPIGE, !FORHAND, !XIM, !TEKNISK_KRETS, !TEKNISKE_KRETSER, new CountCategory[]{}},
			{KOMMUNEKRETSEN, null, null, FORELOPIGE, FORHAND, !XIM, !TEKNISK_KRETS, !TEKNISKE_KRETSER, new CountCategory[]{FO}},
			{KOMMUNEKRETSEN, null, null, FORELOPIGE, !FORHAND, !XIM, !TEKNISK_KRETS, !TEKNISKE_KRETSER, new CountCategory[]{}}
			// @formatter:on
		};
	}

	@DataProvider
	public Object[][] ulovligeKombinasjoner() {
		return new Object[][] {
				// 0000 i samlet kommune, foreløpig
				{ KOMMUNEKRETSEN, VO_SENTRALT_SAMLET, VF_SENTRALT_SAMLET, FORELOPIGE, !FORHAND, XIM, TEKNISK_KRETS, TEKNISKE_KRETSER },
				// 0000 i kretsfordelt kommune, foreløpig
				{ KOMMUNEKRETSEN, !VO_SENTRALT_SAMLET, VF_SENTRALT_SAMLET, FORELOPIGE, FORHAND, XIM, TEKNISK_KRETS, TEKNISKE_KRETSER },
				// Det kan ikke være en teknisk krets i en kommune som ikke har tekniske kretser.
				{ KOMMUNEKRETSEN, VO_SENTRALT_SAMLET, VF_SENTRALT_SAMLET, FORELOPIGE, FORHAND, XIM, TEKNISK_KRETS, !TEKNISKE_KRETSER },
				// Tekniske kretser får aldri valgtingsstemmer.
				{ KOMMUNEKRETSEN, !VO_SENTRALT_SAMLET, VF_SENTRALT_SAMLET, FORELOPIGE, !FORHAND, XIM, TEKNISK_KRETS, TEKNISKE_KRETSER },
				// En kommunekrets (0000) kan ikke samtidig være en teknisk krets. Derfor returneres ingen CountCategory.
				{ KOMMUNEKRETSEN, VO_SENTRALT_SAMLET, VF_SENTRALT_SAMLET, FORELOPIGE, FORHAND, XIM, TEKNISK_KRETS, TEKNISKE_KRETSER }
		};
	}
}
