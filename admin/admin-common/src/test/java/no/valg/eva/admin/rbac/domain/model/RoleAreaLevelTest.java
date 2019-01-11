package no.valg.eva.admin.rbac.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import no.evote.constants.AreaLevelEnum;
import no.evote.constants.PollingPlaceType;
import no.valg.eva.admin.configuration.domain.model.AreaLevel;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;

import org.testng.annotations.Test;

public class RoleAreaLevelTest {

	public static final long AN_ELECTION_EVENT_PK = 1L;
	private static final String AN_ID = "1";
	public static final long SAME_PK = 2L;
	public static final long DIFFERENT_PK = 3L;
	
	@Test
	public void equals_roleAreaLevelWithSamePk_areEqual() {
		Role role = new Role();
		role.setElectionEvent(new ElectionEvent(AN_ELECTION_EVENT_PK));
		role.setId(AN_ID);
		RoleAreaLevel roleAreaLevel1 = new RoleAreaLevel(role, new AreaLevel(AreaLevelEnum.POLLING_PLACE), PollingPlaceType.ADVANCE_VOTING);
		roleAreaLevel1.setPk(SAME_PK);
		RoleAreaLevel roleAreaLevel2 = new RoleAreaLevel(role, new AreaLevel(AreaLevelEnum.POLLING_PLACE), PollingPlaceType.ELECTION_DAY_VOTING);
		roleAreaLevel2.setPk(SAME_PK);
		
		assertThat(roleAreaLevel1.equals(roleAreaLevel2)).isTrue();
		assertThat(roleAreaLevel1.hashCode()).isEqualTo(roleAreaLevel2.hashCode());
	}

	@Test
	public void equals_roleAreaLevelWithDifferentPk_areNotEqual() {
		Role role = new Role();
		role.setElectionEvent(new ElectionEvent(AN_ELECTION_EVENT_PK));
		role.setId(AN_ID);
		RoleAreaLevel roleAreaLevel1 = new RoleAreaLevel(role, new AreaLevel(AreaLevelEnum.POLLING_PLACE), PollingPlaceType.ADVANCE_VOTING);
		roleAreaLevel1.setPk(SAME_PK);
		RoleAreaLevel roleAreaLevel2 = new RoleAreaLevel(role, new AreaLevel(AreaLevelEnum.POLLING_PLACE), PollingPlaceType.ELECTION_DAY_VOTING);
		roleAreaLevel2.setPk(DIFFERENT_PK);
		
		assertThat(roleAreaLevel1.equals(roleAreaLevel2)).isFalse();
	}

	@Test
	public void equals_roleAreaLevelWithPkNullButDifferentFields_areNotEqual() {
		Role role = new Role();
		role.setElectionEvent(new ElectionEvent(AN_ELECTION_EVENT_PK));
		role.setId(AN_ID);
		RoleAreaLevel roleAreaLevel1 = new RoleAreaLevel(role, new AreaLevel(AreaLevelEnum.POLLING_PLACE), PollingPlaceType.ADVANCE_VOTING);
		RoleAreaLevel roleAreaLevel2 = new RoleAreaLevel(role, new AreaLevel(AreaLevelEnum.POLLING_DISTRICT), PollingPlaceType.ADVANCE_VOTING);
		
		assertThat(roleAreaLevel1.equals(roleAreaLevel2)).isFalse();
	}
}
