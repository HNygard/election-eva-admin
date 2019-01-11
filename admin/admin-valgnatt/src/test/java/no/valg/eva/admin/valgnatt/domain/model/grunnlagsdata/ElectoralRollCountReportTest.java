package no.valg.eva.admin.valgnatt.domain.model.grunnlagsdata;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigInteger;

import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.configuration.domain.model.Valgtype;
import no.valg.eva.admin.configuration.domain.model.valgnatt.ElectoralRollCount;

import org.testng.annotations.Test;

public class ElectoralRollCountReportTest {

	private static final String AN_ELECTION_EVENT_ID = "150001";
	private static final int A_PK = 100;
	private static final int ANOTHER_PK = 200;
	private static final String OSLO = "Oslo";
	private static final String ELECTION_YEAR = "2017";
	private static final String ELECTION_NAME = "valg";
	private static final String EMPTY = "";

	@Test
	public void add_numberOfVotersIsAccumulated() throws Exception {
		ElectoralRollCountReport electoralRollCountReport = makeElectoralRoll();

		assertThat(electoralRollCountReport.getVoterTotal()).isEqualTo(2);
	}

	private ElectoralRollCountReport makeElectoralRoll() {
		ElectoralRollCountReport electoralRollCountReport = new ElectoralRollCountReport(AN_ELECTION_EVENT_ID, ELECTION_NAME, Valgtype.STORTINGSVALG,
				ELECTION_YEAR);
		ElectoralRollCount anElectoralRoll = new ElectoralRollCount(AreaPath.OSLO_MUNICIPALITY_ID, OSLO, OSLO, "03", "0101", "Et stemmested", BigInteger.ONE,
				false, A_PK, EMPTY, EMPTY, EMPTY, EMPTY, null);
		ElectoralRollCount anotherElectoralRoll = new ElectoralRollCount(AreaPath.OSLO_MUNICIPALITY_ID, OSLO, OSLO, "03", "0201", "Et annet stemmested",
				BigInteger.ONE, false, ANOTHER_PK, EMPTY, EMPTY, EMPTY, EMPTY, null);

		electoralRollCountReport.add(anElectoralRoll);
		electoralRollCountReport.add(anotherElectoralRoll);
		return electoralRollCountReport;
	}

	@Test
	public void toJson() {
		ElectoralRollCountReport electoralRollCountReport = makeElectoralRoll();

		assertThat(electoralRollCountReport.toJson()).contains(AN_ELECTION_EVENT_ID);
	}
}
