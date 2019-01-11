package no.valg.eva.admin.counting.mockup;

import static no.valg.eva.admin.common.mockups.PrimaryKeySeries.VOTING_CATEGORY_PK_SERIES;

import no.valg.eva.admin.configuration.domain.model.VotingCategory;

public final class VotingCategoryMockups {

	private static final long VOTING_CATEGORY_PK_VO = VOTING_CATEGORY_PK_SERIES + 1;
	private static final String VOTING_CATEGORY_ID_VO = "VO";

	public static VotingCategory votingCategory(final Long votingCategoryPk, final String votingCategoryId) {
		VotingCategory votingCategory = new VotingCategory();
		votingCategory.setPk(votingCategoryPk);
		votingCategory.setId(votingCategoryId);
		return votingCategory;
	}

	public static VotingCategory votingCategoryVo() {
		return votingCategory(VOTING_CATEGORY_PK_VO, VOTING_CATEGORY_ID_VO);
	}

	private VotingCategoryMockups() {
		// no instances allowed
	}
}
