package no.valg.eva.admin.configuration.domain.model.manntall;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import no.valg.eva.admin.configuration.domain.model.OpeningHours;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * Ment brukt ifbm generering av valgkortgrunnlag
 * Forutsetninger:
 * - OpeningHours-objektene må ha med ElectionDay-objektene for at dette skal kunne fungere
 * - Åpningstidene trenger _ikke_ å være sorterte
 */
public class AapningstidFormatter {

	private AapningstidFormatter() {
		// Instansiering ikke mulig	
	}
	
	/**
	 * @return Formattert streng med sorterte åpningstider ihht. eksportformat for valgkort
	 */
	public static String format(Collection<OpeningHours> aapningstider) {
		Comparator<OpeningHours> aapningstidComparator = (aapningstid1, aapningstid2) -> {
			LocalDate valgdag1 = aapningstid1.getElectionDay().getDate();
			LocalDate valgdag2 = aapningstid2.getElectionDay().getDate();
			if (valgdag1.compareTo(valgdag2) != 0) {
				return valgdag1.compareTo(valgdag2);	
			} else {
				return aapningstid1.getStartTime().compareTo(aapningstid2.getStartTime());
			}
		};

		List<OpeningHours> sorterteAapningstider = aapningstider.stream()
			.sorted(aapningstidComparator).collect(Collectors.toList());

		DateTimeFormatter valgdatoFormatter = DateTimeFormat.forPattern("dd.MM.yyyy");
		DateTimeFormatter klokkeslettFormatter = DateTimeFormat.forPattern("HH:mm");

		List<String> formatterteAapningstider = sorterteAapningstider.stream().map(aapningstid -> 
			valgdatoFormatter.print(aapningstid.getElectionDay().getDate())
			+ " kl. " 
			+ klokkeslettFormatter.print(aapningstid.getStartTime()) 
			+ " - "
			+ klokkeslettFormatter.print(aapningstid.getEndTime())
		).collect(Collectors.toList());
	
		return StringUtils.join(formatterteAapningstider, "|");
	}

}
