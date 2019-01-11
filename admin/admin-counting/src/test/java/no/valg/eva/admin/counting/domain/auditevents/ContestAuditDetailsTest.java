package no.valg.eva.admin.counting.domain.auditevents;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.json.JsonObject;

import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.auditlog.JsonBuilder;
import no.valg.eva.admin.configuration.domain.model.Contest;

import org.testng.annotations.Test;

public class ContestAuditDetailsTest {
	
	private static final no.valg.eva.admin.common.ElectionPath CONTEST_PATH = ElectionPath.from("150001.01.01.111111");

	@Test
	public void toJsonObject_givenContest_returnsCorrectJsonObject() throws Exception {
		Contest contest = contest();
		JsonObject jsonObject = new ContestAuditDetails(contest).toJsonObject();
		assertThat(jsonObject).isEqualTo(contestJsonObject());
	}

	public static JsonObject contestJsonObject() {
		return new JsonBuilder()
				.add("electionPath", CONTEST_PATH.path())
				.add("name", "contestName")
				.asJsonObject();
	}

	public static Contest contest() {
		Contest contest = mock(Contest.class);
		return contest(contest);
	}

	private static Contest contest(Contest contest) {
		when(contest.electionPath()).thenReturn(CONTEST_PATH);
		when(contest.getName()).thenReturn("contestName");
		return contest;
	}
	
}
