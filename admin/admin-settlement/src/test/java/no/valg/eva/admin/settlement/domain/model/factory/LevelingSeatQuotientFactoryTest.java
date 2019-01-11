package no.valg.eva.admin.settlement.domain.model.factory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;

import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.Party;
import no.valg.eva.admin.settlement.domain.consumer.LevelingSeatQuotientConsumer;
import no.valg.eva.admin.settlement.domain.event.LevelingSeatQuotientEvent;
import no.valg.eva.admin.settlement.domain.model.LevelingSeatQuotient;
import no.valg.eva.admin.test.MockUtilsTestCase;

import org.mockito.ArgumentCaptor;
import org.testng.annotations.Test;


public class LevelingSeatQuotientFactoryTest extends MockUtilsTestCase {
	@Test
	public void buildLevelingSeatQuotients_givenEvents_buildsLevelingSeatQuotients() throws Exception {
		LevelingSeatQuotientConsumer consumer = createMock(LevelingSeatQuotientConsumer.class);
		LevelingSeatQuotientFactory levelingSeatQuotientFactory = new LevelingSeatQuotientFactory();
		levelingSeatQuotientFactory.addConsumer(consumer);
		Contest contest1 = createMock(Contest.class);
		Contest contest2 = createMock(Contest.class);
		Party party1 = createMock(Party.class);
		Party party2 = createMock(Party.class);

		levelingSeatQuotientFactory.levelingSeatQuotientDelta(levelingSeatQuotientEvent(contest1, party1, 1, 0));
		levelingSeatQuotientFactory.levelingSeatQuotientDelta(levelingSeatQuotientEvent(contest1, party1, 0, 1));
		levelingSeatQuotientFactory.levelingSeatQuotientDelta(levelingSeatQuotientEvent(contest1, party2, 1, 0));
		levelingSeatQuotientFactory.levelingSeatQuotientDelta(levelingSeatQuotientEvent(contest1, party2, 0, 1));
		levelingSeatQuotientFactory.levelingSeatQuotientDelta(levelingSeatQuotientEvent(contest2, party1, 1, 0));
		levelingSeatQuotientFactory.levelingSeatQuotientDelta(levelingSeatQuotientEvent(contest2, party1, 0, 1));
		levelingSeatQuotientFactory.levelingSeatQuotientDelta(levelingSeatQuotientEvent(contest2, party2, 1, 0));
		levelingSeatQuotientFactory.levelingSeatQuotientDelta(levelingSeatQuotientEvent(contest2, party2, 0, 1));
		levelingSeatQuotientFactory.buildLevelingSeatQuotients();

		ArgumentCaptor<LevelingSeatQuotient> argumentCaptor = ArgumentCaptor.forClass(LevelingSeatQuotient.class);
		verify(consumer, times(4)).consume(argumentCaptor.capture());
		List<LevelingSeatQuotient> levelingSeatQuotients = argumentCaptor.getAllValues();
		assertThat(levelingSeatQuotients.get(0)).isEqualToComparingFieldByField(levelingSeatQuotient(contest1, party1));
		assertThat(levelingSeatQuotients.get(1)).isEqualToComparingFieldByField(levelingSeatQuotient(contest1, party2));
		assertThat(levelingSeatQuotients.get(2)).isEqualToComparingFieldByField(levelingSeatQuotient(contest2, party1));
		assertThat(levelingSeatQuotients.get(3)).isEqualToComparingFieldByField(levelingSeatQuotient(contest2, party2));
	}

	private LevelingSeatQuotientEvent levelingSeatQuotientEvent(Contest contest, Party party, int partyVotes, int partySeats) {
		return new LevelingSeatQuotientEvent(contest, party, partyVotes, partySeats);
	}

	private LevelingSeatQuotient levelingSeatQuotient(Contest contest, Party party) {
		LevelingSeatQuotient levelingSeatQuotient = new LevelingSeatQuotient();
		levelingSeatQuotient.setContest(contest);
		levelingSeatQuotient.setParty(party);
		levelingSeatQuotient.setPartyVotes(1);
		levelingSeatQuotient.setPartySeats(1);
		levelingSeatQuotient.setContestVotes(2);
		levelingSeatQuotient.setContestSeats(2);
		return levelingSeatQuotient;
	}
}

