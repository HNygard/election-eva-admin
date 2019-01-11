package no.valg.eva.admin.frontend.stemmegivning.ctrls;

import static no.valg.eva.admin.common.voting.VotingCategory.FB;
import static no.valg.eva.admin.common.voting.VotingCategory.FI;
import static no.valg.eva.admin.common.voting.VotingCategory.FU;
import static no.valg.eva.admin.common.voting.VotingCategory.FE;
import static no.valg.eva.admin.common.voting.VotingCategory.VB;
import static no.valg.eva.admin.common.voting.VotingCategory.VF;
import static no.valg.eva.admin.common.voting.VotingCategory.VO;
import static no.valg.eva.admin.common.voting.VotingCategory.VS;
import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.KOMMUNE;
import static no.valg.eva.admin.felles.valghierarki.model.ValghierarkiNivaa.VALGGRUPPE;
import static no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerElement.geografi;
import static no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerElement.hierarki;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import no.evote.constants.EvoteConstants;
import no.evote.dto.VotingDto;
import no.evote.security.UserData;
import no.evote.service.configuration.MvAreaService;
import no.evote.service.configuration.MvElectionService;
import no.evote.service.producer.EjbProxy;
import no.evote.service.voting.VotingService;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.frontend.common.PageTitleMetaBuilder;
import no.valg.eva.admin.frontend.common.PageTitleMetaModel;
import no.valg.eva.admin.frontend.kontekstvelger.Kontekst;
import no.valg.eva.admin.frontend.kontekstvelger.KontekstAvhengigController;
import no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerOppsett;

/**
 * Controller used for marking municipalities as ready for e-votes.
 */
@Named
@ViewScoped
public class EvoteSummaryController extends KontekstAvhengigController {

	@Inject
	private UserData userData;
	@Inject
	private PageTitleMetaBuilder pageTitleMetaBuilder;
	@Inject
	@EjbProxy
	private VotingService votingService;
	@Inject
	private MvElectionService mvElectionService;
	@Inject
	private MvAreaService mvAreaService;

	private MvArea kommune;
	private List<Row> data = null;

	@Override
	public KontekstvelgerOppsett getKontekstVelgerOppsett() {
		KontekstvelgerOppsett setup = new KontekstvelgerOppsett();
		setup.leggTil(hierarki(VALGGRUPPE));
		setup.leggTil(geografi(KOMMUNE));
		return setup;
	}

	@Override
	public void initialized(Kontekst kontekst) {
		kommune = mvAreaService.findSingleByPath(kontekst.kommuneSti());
		populateData(mvElectionService.findSingleByPath(kontekst.valggruppeSti()));
	}

	/**
	 * Returns a summary of the status of the votes.
	 */
	public List<Row> getSummary() {
		return data;
	}

	private void populateData(MvElection valgGruppe) {
		List<VotingDto> votingStatistics = votingService.findVotingStatistics(
				userData, 0L, kommune.getMunicipality().getPk(), valgGruppe.getElectionGroup().getPk(), null, null, 0, 0, true,
				new String[] { FI.getId(), FU.getId(), FB.getId(), FE.getId(), VB.getId(), VS.getId(), VO.getId(), VF.getId() }, false);

		Row earlyVotes = new Row();
		Row specialVotes = new Row();
		Row beredskapVotes = new Row();
		Row lateVotes = new Row();
		Row valgtingOrdinaere = new Row();

		earlyVotes.setName("@voting.evoting.early_votes");
		specialVotes.setName("@voting.evoting.special_votes");
		beredskapVotes.setName("@voting.evoting.electiondayemergency_votes");
		lateVotes.setName("@voting.evoting.late_validation_votes");
		valgtingOrdinaere.setName("@voting.evoting.votes");

		for (VotingDto dto : votingStatistics) {
			if (dto.getVotingCategoryId().equals(FI.getId()) || 
				dto.getVotingCategoryId().equals(FU.getId()) || 
				dto.getVotingCategoryId().equals(FB.getId()) || 
				dto.getVotingCategoryId().equals(FE.getId())) {
				addVotes(earlyVotes, dto);
			} else if (dto.getVotingCategoryId().equals(VS.getId())) {
				addVotes(specialVotes, dto);
			} else if (dto.getVotingCategoryId().equals(VB.getId())) {
				addVotes(beredskapVotes, dto);
				// check if late_validation
			} else if (dto.getVotingCategoryId().equals(VO.getId()) || dto.getVotingCategoryId().equals(VF.getId())) {
				addVotes(valgtingOrdinaere, dto);
				// check if late_validation
			} else if (dto.getVotingCategoryId().equals(EvoteConstants.VOTING_CATEGORY_LATE)) {
				addVotes(lateVotes, dto);
			}
		}

		earlyVotes.setRemaining(earlyVotes.getRecieved().subtract(earlyVotes.getApproved()));
		specialVotes.setRemaining(specialVotes.getRecieved().subtract(specialVotes.getApproved()));
		beredskapVotes.setRemaining(beredskapVotes.getRecieved().subtract(beredskapVotes.getApproved()));
		lateVotes.setRemaining(lateVotes.getRecieved().subtract(lateVotes.getApproved()));
		valgtingOrdinaere.setRemaining(valgtingOrdinaere.getRecieved().subtract(valgtingOrdinaere.getApproved()));

		data = new ArrayList<>();
		data.add(earlyVotes);
		data.add(lateVotes);
		data.add(valgtingOrdinaere);
		data.add(specialVotes);
		data.add(beredskapVotes);

		for (Row row : data) {
			if (row.getRemaining().intValue() == 0) {
				row.setOk(true);
			}
		}
	}

	private void addVotes(final Row votes, final VotingDto dto) {
		votes.addRecieved(dto.getNumberOfVotings());

		if (dto.isApproved()) {
			votes.addApproved(dto.getNumberOfVotings());
		}
	}

	public List<PageTitleMetaModel> getPageTitleMeta() {
		return pageTitleMetaBuilder.area(kommune);
	}

	/**
	 * DTO used in the view.
	 */
	public static class Row {
		private Boolean ok;
		private String name;
		private BigInteger recieved = BigInteger.ZERO;
		private BigInteger approved = BigInteger.ZERO;
		private BigInteger remaining = BigInteger.ZERO;

		public Boolean getOk() {
			return ok;
		}

		public void setOk(final Boolean ok) {
			this.ok = ok;
		}

		public String getName() {
			return name;
		}

		public void setName(final String name) {
			this.name = name;
		}

		public BigInteger getRecieved() {
			return recieved;
		}

		public void addRecieved(final BigInteger bigInteger) {
			recieved = recieved.add(bigInteger);
		}

		public BigInteger getApproved() {
			return approved;
		}

		public void addApproved(final BigInteger handled) {
			approved = approved.add(handled);
		}

		public BigInteger getRemaining() {
			return remaining;
		}

		public void setRemaining(final BigInteger remaining) {
			this.remaining = remaining;
		}

	}
}
