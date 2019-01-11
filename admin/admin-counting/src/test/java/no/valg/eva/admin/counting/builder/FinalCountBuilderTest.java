package no.valg.eva.admin.counting.builder;

import static java.util.Arrays.asList;
import static no.valg.eva.admin.common.mockups.AffiliationMockups.affiliation;
import static no.valg.eva.admin.common.mockups.PartyMockups.party;
import static no.valg.eva.admin.counting.mockup.ContestReportMockups.defaultContestReport;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.Set;

import no.valg.eva.admin.common.counting.constants.ReportingUnitTypeId;
import no.valg.eva.admin.common.counting.model.FinalCount;
import no.valg.eva.admin.common.mockups.BallotMockups;
import no.valg.eva.admin.configuration.domain.model.Affiliation;
import no.valg.eva.admin.configuration.domain.model.Ballot;
import no.valg.eva.admin.configuration.domain.model.BallotRejection;
import no.valg.eva.admin.configuration.domain.model.Party;
import no.valg.eva.admin.configuration.domain.model.ReportingUnit;
import no.valg.eva.admin.configuration.domain.model.ReportingUnitType;
import no.valg.eva.admin.counting.domain.model.BallotCount;
import no.valg.eva.admin.counting.domain.model.ContestReport;
import no.valg.eva.admin.counting.domain.model.VoteCount;
import no.valg.eva.admin.counting.domain.model.VoteCountStatus;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class FinalCountBuilderTest {

	private VoteCount voteCount;

	@BeforeMethod
	public void setUp() {
		voteCount = new VoteCount();
		VoteCountStatus voteCountStatus = new VoteCountStatus();
		voteCount.setVoteCountStatus(voteCountStatus);
		ContestReport contestReport = defaultContestReport();
		Set<Ballot> ballots = new HashSet<>();
		Ballot ballot = BallotMockups.ballot(0L, "A", 1, affiliation(null, party(null, "A")));
		ballot.setApproved(true);
		ballots.add(ballot);
		contestReport.getContest().setBallots(ballots);
		voteCount.setContestReport(contestReport);
	}

	@Test
	public void testRejectedBallot() {
		voteCount.setBallotCountSet(ballotCounts(true));

		BallotRejection ballotRejection = new BallotRejection();
		ballotRejection.setId("VA");
		ReportingUnit reportingUnit = makeReportingUnit();
		FinalCount finalCount = new FinalCountBuilder(null, null, null, false, reportingUnit)
				.applyBallotRejections(asList(ballotRejection))
				.applyFinalVoteCount(voteCount)
				.build();
		assertThat(finalCount.getRejectedBallotCounts()).hasSize(1);
	}

	@Test
	public void testOneBallot() {
		voteCount.setBallotCountSet(ballotCounts(false));

		ReportingUnit reportingUnit = makeReportingUnit();
		
		FinalCount finalCount = new FinalCountBuilder(null, null, null, false, reportingUnit).applyFinalVoteCount(voteCount).build();
		assertThat(finalCount.getBallotCounts()).hasSize(1);
	}

	private ReportingUnit makeReportingUnit() {
		ReportingUnit reportingUnit = new ReportingUnit();
		reportingUnit.setReportingUnitType(makeReportingUnitType());
		return reportingUnit;
	}

	private ReportingUnitType makeReportingUnitType() {
		ReportingUnitType reportingUnitType = new ReportingUnitType();
		reportingUnitType.setId(ReportingUnitTypeId.FYLKESVALGSTYRET.getId());
		return reportingUnitType;
	}

	private Set<BallotCount> ballotCounts(boolean rejected) {
		Set<BallotCount> bc = new HashSet<>();
		bc.add(ballotCount(rejected));
		return bc;
	}

	private BallotCount ballotCount(boolean rejected) {
		BallotCount bc = new BallotCount();
		bc.setPk(0L);
		if (rejected) {
			BallotRejection ballotRejection = new BallotRejection();
			ballotRejection.setId("VA");
			bc.setBallotRejection(ballotRejection);
		} else {
			Ballot ballot = new Ballot();
			ballot.setId("A");
			Affiliation affiliation = new Affiliation();
			affiliation.setParty(new Party());
			ballot.setAffiliation(affiliation);
			bc.setBallot(ballot);
		}
		return bc;
	}

}
