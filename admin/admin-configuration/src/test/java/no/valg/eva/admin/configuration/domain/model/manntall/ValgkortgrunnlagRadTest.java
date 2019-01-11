package no.valg.eva.admin.configuration.domain.model.manntall;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.Test;

public class ValgkortgrunnlagRadTest {
	
	@Test
	public void getXxx_hvisVerdienErNull_returnererTomStreng() {
		ValgkortgrunnlagRad valgkortgrunnlagRad = new ValgkortgrunnlagRad();
		
		assertThat(valgkortgrunnlagRad.getAdresselinje1()).isEqualTo("");
		assertThat(valgkortgrunnlagRad.getAdresselinje2()).isEqualTo("");
		assertThat(valgkortgrunnlagRad.getAdresselinje3()).isEqualTo("");
		assertThat(valgkortgrunnlagRad.getFodselsaar()).isEqualTo("");
		assertThat(valgkortgrunnlagRad.getFulltManntallsnummer()).isEqualTo("");
		assertThat(valgkortgrunnlagRad.getInfotekst()).isEqualTo("");
		assertThat(valgkortgrunnlagRad.getKommuneId()).isEqualTo("");
		assertThat(valgkortgrunnlagRad.getKortManntallsnummer()).isEqualTo("0000000000");
		assertThat(valgkortgrunnlagRad.getMaalform()).isEqualTo("");
		assertThat(valgkortgrunnlagRad.getManntallLinje()).isEqualTo("");
		assertThat(valgkortgrunnlagRad.getManntallSide()).isEqualTo("");
		assertThat(valgkortgrunnlagRad.getNavn()).isEqualTo("");
		assertThat(valgkortgrunnlagRad.getPostnummer()).isEqualTo("");
		assertThat(valgkortgrunnlagRad.getPoststed()).isEqualTo("");
		assertThat(valgkortgrunnlagRad.getRode()).isEqualTo("");
		assertThat(valgkortgrunnlagRad.getValgkretsId()).isEqualTo("");
		assertThat(valgkortgrunnlagRad.getValglokaleAapningstider()).isEqualTo("");
		assertThat(valgkortgrunnlagRad.getValglokaleAdresselinje1()).isEqualTo("");
		assertThat(valgkortgrunnlagRad.getValglokaleNavn()).isEqualTo("");
		assertThat(valgkortgrunnlagRad.getValglokalePostnummer()).isEqualTo("");
		assertThat(valgkortgrunnlagRad.getValglokalePoststed()).isEqualTo("");
		assertThat(valgkortgrunnlagRad.getValgstyretAdresselinje1()).isEqualTo("");
		assertThat(valgkortgrunnlagRad.getValgstyretNavn()).isEqualTo("");
		assertThat(valgkortgrunnlagRad.getValgstyretPostnummer()).isEqualTo("");
		assertThat(valgkortgrunnlagRad.getValgstyretPoststed()).isEqualTo("");
	}

	@Test
	public void getXxx_erstatterSemikolonMedKomma() {
		ValgkortgrunnlagRad valgkortgrunnlagRad = new ValgkortgrunnlagRad();

		valgkortgrunnlagRad.setAdresselinje1("a;");
		assertThat(valgkortgrunnlagRad.getAdresselinje1()).isEqualTo("a,");

		valgkortgrunnlagRad.setAdresselinje2("b;");
		assertThat(valgkortgrunnlagRad.getAdresselinje2()).isEqualTo("b,");

		valgkortgrunnlagRad.setAdresselinje3("c;");
		assertThat(valgkortgrunnlagRad.getAdresselinje3()).isEqualTo("c,");

		valgkortgrunnlagRad.setInfotekst("d;");
		assertThat(valgkortgrunnlagRad.getInfotekst()).isEqualTo("d,");

		valgkortgrunnlagRad.setNavn("e;");
		assertThat(valgkortgrunnlagRad.getNavn()).isEqualTo("e,");

		valgkortgrunnlagRad.setPoststed("f;");
		assertThat(valgkortgrunnlagRad.getPoststed()).isEqualTo("f,");

		valgkortgrunnlagRad.setValglokaleAdresselinje1("g;");
		assertThat(valgkortgrunnlagRad.getValglokaleAdresselinje1()).isEqualTo("g,");

		valgkortgrunnlagRad.setValglokaleNavn("h;");
		assertThat(valgkortgrunnlagRad.getValglokaleNavn()).isEqualTo("h,");

		valgkortgrunnlagRad.setValglokalePoststed("i;");
		assertThat(valgkortgrunnlagRad.getValglokalePoststed()).isEqualTo("i,");

		valgkortgrunnlagRad.setValgstyretAdresselinje1("j;");
		assertThat(valgkortgrunnlagRad.getValgstyretAdresselinje1()).isEqualTo("j,");

		valgkortgrunnlagRad.setValgstyretNavn("k;");
		assertThat(valgkortgrunnlagRad.getValgstyretNavn()).isEqualTo("k,");

		valgkortgrunnlagRad.setValgstyretPoststed("l;");
		assertThat(valgkortgrunnlagRad.getValgstyretPoststed()).isEqualTo("l,");
	}

	@Test
	public void getXxx_fjernerWhitespaceForanOgBak() {
		ValgkortgrunnlagRad valgkortgrunnlagRad = new ValgkortgrunnlagRad();

		valgkortgrunnlagRad.setAdresselinje1(" a ");
		assertThat(valgkortgrunnlagRad.getAdresselinje1()).isEqualTo("a");

		valgkortgrunnlagRad.setAdresselinje2(" b ");
		assertThat(valgkortgrunnlagRad.getAdresselinje2()).isEqualTo("b");

		valgkortgrunnlagRad.setAdresselinje3(" c ");
		assertThat(valgkortgrunnlagRad.getAdresselinje3()).isEqualTo("c");

		valgkortgrunnlagRad.setInfotekst(" d ");
		assertThat(valgkortgrunnlagRad.getInfotekst()).isEqualTo("d");

		valgkortgrunnlagRad.setNavn(" e ");
		assertThat(valgkortgrunnlagRad.getNavn()).isEqualTo("e");

		valgkortgrunnlagRad.setPoststed(" f ");
		assertThat(valgkortgrunnlagRad.getPoststed()).isEqualTo("f");

		valgkortgrunnlagRad.setValglokaleAdresselinje1(" g ");
		assertThat(valgkortgrunnlagRad.getValglokaleAdresselinje1()).isEqualTo("g");

		valgkortgrunnlagRad.setValglokaleNavn(" h ");
		assertThat(valgkortgrunnlagRad.getValglokaleNavn()).isEqualTo("h");

		valgkortgrunnlagRad.setValglokalePoststed(" i ");
		assertThat(valgkortgrunnlagRad.getValglokalePoststed()).isEqualTo("i");

		valgkortgrunnlagRad.setValgstyretAdresselinje1(" j ");
		assertThat(valgkortgrunnlagRad.getValgstyretAdresselinje1()).isEqualTo("j");

		valgkortgrunnlagRad.setValgstyretNavn(" k ");
		assertThat(valgkortgrunnlagRad.getValgstyretNavn()).isEqualTo("k");

		valgkortgrunnlagRad.setValgstyretPoststed(" l ");
		assertThat(valgkortgrunnlagRad.getValgstyretPoststed()).isEqualTo("l");
	}

	@Test
	public void setXxx_hvisIkkeNullverdiSettes_lagrerVerdien() {
		ValgkortgrunnlagRad valgkortgrunnlagRad = new ValgkortgrunnlagRad();

		valgkortgrunnlagRad.setAdresselinje1("adr1");
		assertThat(valgkortgrunnlagRad.getAdresselinje1()).isEqualTo("adr1");

		valgkortgrunnlagRad.setAdresselinje2("adr2");
		assertThat(valgkortgrunnlagRad.getAdresselinje2()).isEqualTo("adr2");

		valgkortgrunnlagRad.setAdresselinje3("adr3");
		assertThat(valgkortgrunnlagRad.getAdresselinje3()).isEqualTo("adr3");

		valgkortgrunnlagRad.setFodselsaar("1234");
		assertThat(valgkortgrunnlagRad.getFodselsaar()).isEqualTo("1234");

		valgkortgrunnlagRad.setFulltManntallsnummer("123456789012");
		assertThat(valgkortgrunnlagRad.getFulltManntallsnummer()).isEqualTo("123456789012");

		valgkortgrunnlagRad.setInfotekst("infoTekst");
		assertThat(valgkortgrunnlagRad.getInfotekst()).isEqualTo("infoTekst");
		
		valgkortgrunnlagRad.setKommuneId("9876");
		assertThat(valgkortgrunnlagRad.getKommuneId()).isEqualTo("9876");
		
		valgkortgrunnlagRad.setKortManntallsnummer("12345678");
		assertThat(valgkortgrunnlagRad.getKortManntallsnummer()).isEqualTo("0012345678");

		valgkortgrunnlagRad.setMaalform("nn-NO");
		assertThat(valgkortgrunnlagRad.getMaalform()).isEqualTo("nn-NO");

		valgkortgrunnlagRad.setManntallLinje("30");
		assertThat(valgkortgrunnlagRad.getManntallLinje()).isEqualTo("30");

		valgkortgrunnlagRad.setManntallSide("1");
		assertThat(valgkortgrunnlagRad.getManntallSide()).isEqualTo("1");
		
		valgkortgrunnlagRad.setNavn("Etternavn Fornavn");
		assertThat(valgkortgrunnlagRad.getNavn()).isEqualTo("Etternavn Fornavn");
		
		valgkortgrunnlagRad.setPostnummer("1234");
		assertThat(valgkortgrunnlagRad.getPostnummer()).isEqualTo("1234");
		
		valgkortgrunnlagRad.setPoststed("Stedet");
		assertThat(valgkortgrunnlagRad.getPoststed()).isEqualTo("Stedet");
		
		valgkortgrunnlagRad.setRode("A-D");
		assertThat(valgkortgrunnlagRad.getRode()).isEqualTo("A-D");

		valgkortgrunnlagRad.setValgkretsId("0001");
		assertThat(valgkortgrunnlagRad.getValgkretsId()).isEqualTo("0001");
		
		valgkortgrunnlagRad.setValglokaleAapningstider("10.09.2017 kl. 10:00 - 12:00");
		assertThat(valgkortgrunnlagRad.getValglokaleAapningstider()).isEqualTo("10.09.2017 kl. 10:00 - 12:00");
		
		valgkortgrunnlagRad.setValglokaleAdresselinje1("adr1");
		assertThat(valgkortgrunnlagRad.getValglokaleAdresselinje1()).isEqualTo("adr1");

		valgkortgrunnlagRad.setValglokaleNavn("vlnavn");
		assertThat(valgkortgrunnlagRad.getValglokaleNavn()).isEqualTo("vlnavn");

		valgkortgrunnlagRad.setValglokalePostnummer("2345");
		assertThat(valgkortgrunnlagRad.getValglokalePostnummer()).isEqualTo("2345");

		valgkortgrunnlagRad.setValglokalePoststed("vlps");
		assertThat(valgkortgrunnlagRad.getValglokalePoststed()).isEqualTo("vlps");
		
		valgkortgrunnlagRad.setValgstyretAdresselinje1("adr1");
		assertThat(valgkortgrunnlagRad.getValgstyretAdresselinje1()).isEqualTo("adr1");

		valgkortgrunnlagRad.setValgstyretNavn("vsnavn");
		assertThat(valgkortgrunnlagRad.getValgstyretNavn()).isEqualTo("vsnavn");
		
		valgkortgrunnlagRad.setValgstyretPostnummer("3456");
		assertThat(valgkortgrunnlagRad.getValgstyretPostnummer()).isEqualTo("3456");
		
		valgkortgrunnlagRad.setValgstyretPoststed("vsps");
		assertThat(valgkortgrunnlagRad.getValgstyretPoststed()).isEqualTo("vsps");
	}

}