package no.valg.eva.admin.configuration.domain.model.manntall;

import com.google.common.base.Joiner;

public class ValgkortgrunnlagRadCsvMapper {
	
	private ValgkortgrunnlagRadCsvMapper() {
		// Ikke instansierbar
	}

	public static String tilCsv(ValgkortgrunnlagRad valgkortgrunnlagRad) {
		String skilletegn = ";";
		Joiner joiner = Joiner.on(skilletegn);
		return joiner.join(
			valgkortgrunnlagRad.getValgstyretNavn(), 
			valgkortgrunnlagRad.getValgstyretAdresselinje1(), 
			valgkortgrunnlagRad.getValgstyretPostnummer(), 
			valgkortgrunnlagRad.getValgstyretPoststed(), 
			valgkortgrunnlagRad.getValgkretsId(), 
			valgkortgrunnlagRad.getRode(), 
			valgkortgrunnlagRad.getManntallSide(), 
			valgkortgrunnlagRad.getManntallLinje(), 
			valgkortgrunnlagRad.getFodselsaar(), 
			valgkortgrunnlagRad.getKommuneId(), 
			valgkortgrunnlagRad.getKortManntallsnummer(), 
			valgkortgrunnlagRad.getNavn(), 
			valgkortgrunnlagRad.getFulltManntallsnummer(), 
			valgkortgrunnlagRad.getAdresselinje1(), 
			valgkortgrunnlagRad.getAdresselinje2(), 
			valgkortgrunnlagRad.getAdresselinje3(), 
			valgkortgrunnlagRad.getPostnummer(), 
			valgkortgrunnlagRad.getPoststed(), 
			valgkortgrunnlagRad.getInfotekst(), 
			valgkortgrunnlagRad.getValglokaleAapningstider(), 
			valgkortgrunnlagRad.getValglokaleNavn(), 
			valgkortgrunnlagRad.getValglokaleAdresselinje1(), 
			valgkortgrunnlagRad.getValglokalePostnummer(), 
			valgkortgrunnlagRad.getValglokalePoststed(), 
			valgkortgrunnlagRad.getMaalform()
		);
	}
}
