package no.valg.eva.admin.configuration.domain.model.manntall;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import no.valg.eva.admin.configuration.domain.model.ElectionDay;
import no.valg.eva.admin.configuration.domain.model.OpeningHours;

import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class AapningstidFormatterTest {
	
	@Test(dataProvider = "aapningstider")
	public void format_forEnAapningstid_konvertererDenTilLesbartFormatMedDato(List<OpeningHours> aapningstider, String forventetTekst) {
		assertThat(AapningstidFormatter.format(aapningstider)).isEqualTo(forventetTekst);
	}
	
	@DataProvider
	private Object[][] aapningstider() {
		return new Object[][] {
			{singletonList(aapningstid(new LocalTime(10, 0), new LocalTime(11, 30), new LocalDate(2017, 9, 10))),
				"10.09.2017 kl. 10:00 - 11:30" },
			{ asList(aapningstid(new LocalTime(10, 0), new LocalTime (11, 30), new LocalDate(2017, 9, 10)),
  					 aapningstid(new LocalTime(12, 0), new LocalTime (19, 00), new LocalDate(2017, 9, 10))), 
				"10.09.2017 kl. 10:00 - 11:30|10.09.2017 kl. 12:00 - 19:00" },
			{ asList(aapningstid(new LocalTime(10, 0), new LocalTime (11, 30), new LocalDate(2017, 9, 10)),
  					 aapningstid(new LocalTime(12, 0), new LocalTime (19, 00), new LocalDate(2017, 9, 10)),
					 aapningstid(new LocalTime(9, 0),  new LocalTime (11, 30), new LocalDate(2017, 9, 11)),
  					 aapningstid(new LocalTime(12, 15), new LocalTime (20, 30), new LocalDate(2017, 9, 11))), 
				"10.09.2017 kl. 10:00 - 11:30|10.09.2017 kl. 12:00 - 19:00|11.09.2017 kl. 09:00 - 11:30|11.09.2017 kl. 12:15 - 20:30" },
		};
	}

	private OpeningHours aapningstid(LocalTime startTid, LocalTime sluttTid, LocalDate valgdagDato) {
		OpeningHours aapningstid = new OpeningHours();
		aapningstid.setStartTime(startTid);
		aapningstid.setEndTime(sluttTid);
		ElectionDay valgdag = new ElectionDay();
		valgdag.setDate(valgdagDato);
		aapningstid.setElectionDay(valgdag);
		return aapningstid;
	}

}