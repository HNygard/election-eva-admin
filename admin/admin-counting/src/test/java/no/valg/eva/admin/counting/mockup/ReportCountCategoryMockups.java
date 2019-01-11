package no.valg.eva.admin.counting.mockup;

import static no.valg.eva.admin.common.mockups.PrimaryKeySeries.REPORT_COUNT_CATEGORY_PK_SERIES;
import static no.valg.eva.admin.counting.mockup.VoteCountMockups.voteCountCategoryFo;
import static no.valg.eva.admin.counting.mockup.VoteCountMockups.voteCountCategoryVf;
import static no.valg.eva.admin.counting.mockup.VoteCountMockups.voteCountCategoryVo;

import no.valg.eva.admin.configuration.domain.model.ReportCountCategory;


public final class ReportCountCategoryMockups {

	public static final long REPORT_COUNT_CATEGORY_PK_VO = REPORT_COUNT_CATEGORY_PK_SERIES + 1;
	public static final long REPORT_COUNT_CATEGORY_PK_VF = REPORT_COUNT_CATEGORY_PK_SERIES + 2;
	public static final long REPORT_COUNT_CATEGORY_PK_FO = REPORT_COUNT_CATEGORY_PK_SERIES + 5;

	public static final boolean CENTRAL_PRELIMINARY_COUNT_TRUE = true;
	public static final boolean CENTRAL_PRELIMINARY_COUNT_FALSE = false;
	public static final boolean POLLING_DISTRICT_COUNT_TRUE = true;
	public static final boolean POLLING_DISTRICT_COUNT_FALSE = false;
	public static final boolean TECHNICAL_POLLING_DISTRICT_COUNT_FALSE = false;

	private ReportCountCategoryMockups() {
		// no instances allowed
	}

	public static ReportCountCategory reportCountCategoryVo() {
		ReportCountCategory reportCountCategory = new ReportCountCategory();
		reportCountCategory.setPk(REPORT_COUNT_CATEGORY_PK_VO);
		reportCountCategory.setVoteCountCategory(voteCountCategoryVo());

		// default: by polling district count mode
		reportCountCategory.setCentralPreliminaryCount(CENTRAL_PRELIMINARY_COUNT_FALSE);
		reportCountCategory.setPollingDistrictCount(POLLING_DISTRICT_COUNT_TRUE);
		reportCountCategory.setTechnicalPollingDistrictCount(TECHNICAL_POLLING_DISTRICT_COUNT_FALSE);

		return reportCountCategory;
	}

	public static ReportCountCategory reportCountCategoryVoCentral() {
		ReportCountCategory reportCountCategory = new ReportCountCategory();
		reportCountCategory.setPk(REPORT_COUNT_CATEGORY_PK_VO);
		reportCountCategory.setVoteCountCategory(voteCountCategoryVo());

		reportCountCategory.setCentralPreliminaryCount(CENTRAL_PRELIMINARY_COUNT_TRUE);
		reportCountCategory.setPollingDistrictCount(POLLING_DISTRICT_COUNT_FALSE);
		reportCountCategory.setTechnicalPollingDistrictCount(TECHNICAL_POLLING_DISTRICT_COUNT_FALSE);

		return reportCountCategory;
	}

	public static ReportCountCategory reportCountCategoryFoCentral() {
		ReportCountCategory reportCountCategory = new ReportCountCategory();
		reportCountCategory.setPk(REPORT_COUNT_CATEGORY_PK_FO);
		reportCountCategory.setVoteCountCategory(voteCountCategoryFo());

		reportCountCategory.setCentralPreliminaryCount(CENTRAL_PRELIMINARY_COUNT_TRUE);
		reportCountCategory.setPollingDistrictCount(POLLING_DISTRICT_COUNT_FALSE);
		reportCountCategory.setTechnicalPollingDistrictCount(TECHNICAL_POLLING_DISTRICT_COUNT_FALSE);

		return reportCountCategory;
	}

	public static ReportCountCategory reportCountCategoryVoByPollingDistrict() {
		ReportCountCategory reportCountCategory = new ReportCountCategory();
		reportCountCategory.setPk(REPORT_COUNT_CATEGORY_PK_VO);
		reportCountCategory.setVoteCountCategory(voteCountCategoryVo());

		reportCountCategory.setCentralPreliminaryCount(CENTRAL_PRELIMINARY_COUNT_FALSE);
		reportCountCategory.setPollingDistrictCount(POLLING_DISTRICT_COUNT_TRUE);
		reportCountCategory.setTechnicalPollingDistrictCount(TECHNICAL_POLLING_DISTRICT_COUNT_FALSE);

		return reportCountCategory;
	}

	public static ReportCountCategory reportCountCategoryVf(final boolean foreignSpecialCoversEnabled) {
		ReportCountCategory reportCountCategory = new ReportCountCategory();
		reportCountCategory.setPk(REPORT_COUNT_CATEGORY_PK_VF);
		reportCountCategory.setVoteCountCategory(voteCountCategoryVf());
		reportCountCategory.setSpecialCover(foreignSpecialCoversEnabled);

		reportCountCategory.setPollingDistrictCount(POLLING_DISTRICT_COUNT_TRUE);
		reportCountCategory.setCentralPreliminaryCount(CENTRAL_PRELIMINARY_COUNT_TRUE);
		return reportCountCategory;
	}
}

