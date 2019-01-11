package no.valg.eva.admin.frontend.stemmegivning.ctrls;

import no.evote.dto.VotingDto;
import no.evote.service.voting.VotingService;
import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.frontend.kontekstvelger.Kontekst;
import no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerOppsett;
import org.testng.annotations.Test;

import java.math.BigInteger;
import java.util.List;

import static java.util.Arrays.asList;
import static no.evote.constants.EvoteConstants.VOTING_CATEGORY_LATE;
import static no.valg.eva.admin.common.voting.VotingCategory.FB;
import static no.valg.eva.admin.common.voting.VotingCategory.FE;
import static no.valg.eva.admin.common.voting.VotingCategory.FI;
import static no.valg.eva.admin.common.voting.VotingCategory.FU;
import static no.valg.eva.admin.common.voting.VotingCategory.VB;
import static no.valg.eva.admin.common.voting.VotingCategory.VO;
import static no.valg.eva.admin.common.voting.VotingCategory.VS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;


public class EvoteSummaryControllerTest extends BaseFrontendTest {

	@Test
	public void getKontekstVelgerOppsett_returnererValggruppeOgKommune() throws Exception {
		EvoteSummaryController ctrl = initializeMocks(EvoteSummaryController.class);

		KontekstvelgerOppsett result = ctrl.getKontekstVelgerOppsett();

		assertThat(result.serialize()).isEqualTo("[hierarki|nivaer|1][geografi|nivaer|3]");
	}

	@Test
	public void initialized_medKontekst_verifiserResultat() throws Exception {
		EvoteSummaryController ctrl = initializeMocks(EvoteSummaryController.class);
		stub_findVotingStatistics();

		ctrl.initialized(createMock(Kontekst.class));

		List<EvoteSummaryController.Row> rows = ctrl.getSummary();
		assertThat(rows).hasSize(5);

		assertRow(rows.get(0), "@voting.evoting.early_votes", null, 14, 11);
		assertRow(rows.get(1), "@voting.evoting.late_validation_votes", null, 10, 0);
		assertRow(rows.get(2), "@voting.evoting.votes", true, 1, 1);
		assertRow(rows.get(3), "@voting.evoting.special_votes", true, 10, 10);
		assertRow(rows.get(4), "@voting.evoting.electiondayemergency_votes", null, 1, 0);
	}

	private void assertRow(EvoteSummaryController.Row row, String name, Boolean ok, long recieved, long approved) {
		assertThat(row.getName()).isEqualTo(name);
		assertThat(row.getOk()).isEqualTo(ok);
		assertThat(row.getRecieved()).isEqualTo(BigInteger.valueOf(recieved));
		assertThat(row.getApproved()).isEqualTo(BigInteger.valueOf(approved));
		assertThat(row.getRemaining()).isEqualTo(BigInteger.valueOf(recieved - approved));
	}

	private void stub_findVotingStatistics() {
		List<VotingDto> result = asList(
				votingDto(FI.getId(), BigInteger.TEN, true),
				votingDto(FI.getId(), BigInteger.ONE, false),
				votingDto(FU.getId(), BigInteger.ONE, true),
				votingDto(FB.getId(), BigInteger.ONE, false),
				votingDto(FE.getId(), BigInteger.ONE, false),
				votingDto(VS.getId(), BigInteger.TEN, true),
				votingDto(VB.getId(), BigInteger.ONE, false),
				votingDto(VO.getId(), BigInteger.ONE, true),
				votingDto(VOTING_CATEGORY_LATE, BigInteger.TEN, false));
        when(getInjectMock(VotingService.class).findVotingStatistics(eq(getUserDataMock()), eq(0L), anyLong(), anyLong(), any(),
                any(), eq(0), eq(0), eq(true), any(String[].class), eq(false))).thenReturn(result);
	}

	private VotingDto votingDto(String votingCat, BigInteger numOfVotings, boolean approved) {
		VotingDto dto = new VotingDto();
		dto.setVotingCategoryId(votingCat);
		dto.setNumberOfVotings(numOfVotings);
		dto.setApproved(approved);
		return dto;
	}

}

