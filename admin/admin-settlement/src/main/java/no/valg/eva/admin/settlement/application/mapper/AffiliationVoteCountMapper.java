package no.valg.eva.admin.settlement.application.mapper;

import java.util.List;

import no.valg.eva.admin.common.mapper.Mapper;
import no.valg.eva.admin.configuration.domain.model.Affiliation;
import no.valg.eva.admin.settlement.domain.model.AffiliationVoteCount;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;

@Default
@ApplicationScoped
public class AffiliationVoteCountMapper extends Mapper {
	public AffiliationVoteCountMapper() {

	}

	public List<no.valg.eva.admin.common.settlement.model.AffiliationVoteCount> affiliationVoteCounts(List<AffiliationVoteCount> affiliationVoteCountEntities) {
		return map(affiliationVoteCountEntities, this::affiliationVoteCount);
	}

	private no.valg.eva.admin.common.settlement.model.AffiliationVoteCount affiliationVoteCount(AffiliationVoteCount affiliationVoteCountEntity) {
		Affiliation affiliation = affiliationVoteCountEntity.getAffiliation();
		int earlyVotingBallots = affiliationVoteCountEntity.getEarlyVotingBallots();
		int earlyVotingModifiedBallots = affiliationVoteCountEntity.getEarlyVotingModifiedBallots();
		int electionDayBallots = affiliationVoteCountEntity.getElectionDayBallots();
		int electionDayModifiedBallots = affiliationVoteCountEntity.getElectionDayModifiedBallots();
		int baselineVotes = affiliationVoteCountEntity.getBaselineVotes();
		int addedVotes = affiliationVoteCountEntity.getAddedVotes();
		int subtractedVotes = affiliationVoteCountEntity.getSubtractedVotes();
		return new no.valg.eva.admin.common.settlement.model.AffiliationVoteCount(affiliation, earlyVotingBallots, earlyVotingModifiedBallots,
				electionDayBallots, electionDayModifiedBallots, baselineVotes, addedVotes, subtractedVotes);
	}
}
