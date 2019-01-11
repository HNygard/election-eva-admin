package no.valg.eva.admin.settlement.domain.model.factory;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.Party;
import no.valg.eva.admin.settlement.domain.consumer.LevelingSeatQuotientConsumer;
import no.valg.eva.admin.settlement.domain.event.LevelingSeatQuotientEvent;
import no.valg.eva.admin.settlement.domain.event.listener.LevelingSeatQuotientEventListener;
import no.valg.eva.admin.settlement.domain.model.LevelingSeatQuotient;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class LevelingSeatQuotientFactory extends EntityFactory<LevelingSeatQuotientFactory, LevelingSeatQuotientConsumer>
		implements LevelingSeatQuotientEventListener {
	private final Map<LevelingSeatQuotientKey, LevelingSeatQuotient> levelingSeatQuotientMap = new LinkedHashMap<>();
	private final Map<Contest, Integer> contestVotesMap = new HashMap<>();
	private final Map<Contest, Integer> contestSeatsMap = new HashMap<>();

	@Override
	public void levelingSeatQuotientDelta(LevelingSeatQuotientEvent event) {
		Contest contest = event.getContest();
		Party party = event.getParty();
		LevelingSeatQuotientKey levelingSeatQuotientKey = new LevelingSeatQuotientKey(contest, party);
		if (levelingSeatQuotientMap.containsKey(levelingSeatQuotientKey)) {
			LevelingSeatQuotient levelingSeatQuotient = levelingSeatQuotientMap.get(levelingSeatQuotientKey);
			levelingSeatQuotient.incrementPartyVotes(event.getPartyVotes());
			levelingSeatQuotient.incrementPartySeats(event.getPartySeats());
		} else {
			LevelingSeatQuotient levelingSeatQuotient = new LevelingSeatQuotient();
			levelingSeatQuotient.setContest(contest);
			levelingSeatQuotient.setParty(party);
			levelingSeatQuotient.setPartyVotes(event.getPartyVotes());
			levelingSeatQuotient.setPartySeats(event.getPartySeats());
			levelingSeatQuotientMap.put(levelingSeatQuotientKey, levelingSeatQuotient);
		}
		incrementValue(contest, contestVotesMap, event.getPartyVotes());
		incrementValue(contest, contestSeatsMap, event.getPartySeats());
	}

	private void incrementValue(Contest contest, Map<Contest, Integer> valueMap, int increment) {
		if (valueMap.containsKey(contest)) {
			valueMap.put(contest, valueMap.get(contest) + increment);
		} else {
			valueMap.put(contest, increment);
		}
	}

	@Override
	protected void updateConsumer(LevelingSeatQuotientConsumer consumer) {
		levelingSeatQuotientMap.values().forEach(consumer::consume);
	}

	@Override
	protected LevelingSeatQuotientFactory self() {
		return this;
	}

	public void buildLevelingSeatQuotients() {
		levelingSeatQuotientMap.values().forEach(this::updateLevelingSeatQuotient);
		updateConsumers();
	}

	private void updateLevelingSeatQuotient(LevelingSeatQuotient levelingSeatQuotient) {
		levelingSeatQuotient.setContestVotes(contestVotesMap.get(levelingSeatQuotient.getContest()));
		levelingSeatQuotient.setContestSeats(contestSeatsMap.get(levelingSeatQuotient.getContest()));
	}

	private static class LevelingSeatQuotientKey {
		private final Contest contest;
		private final Party party;

		LevelingSeatQuotientKey(Contest contest, Party party) {
			this.contest = contest;
			this.party = party;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (!(o instanceof LevelingSeatQuotientKey)) {
				return false;
			}
			LevelingSeatQuotientKey that = (LevelingSeatQuotientKey) o;
			return new EqualsBuilder()
					.append(contest, that.contest)
					.append(party, that.party)
					.isEquals();
		}

		@Override
		public int hashCode() {
			return new HashCodeBuilder(17, 37)
					.append(contest)
					.append(party)
					.toHashCode();
		}
	}
}
