package no.valg.eva.admin.common;

import static org.assertj.core.api.Assertions.assertThat;

import org.joda.time.IllegalFieldValueException;
import org.joda.time.LocalDate;
import org.testng.annotations.Test;


public class FoedselsnummerTest {

	@Test
	public void dateOfBirth_forFoedselsnummerStartingWith170335_is17thMarch1935() {
		assertThat(new Foedselsnummer("17033539061").dateOfBirth()).isEqualTo(new LocalDate(1935, 3, 17));
	}

	@Test
	public void dateOfBirth_forFoedselsnummerStartingWith130100HavingIndividSifferLessThan500_is13thJan1900() {
		assertThat(new Foedselsnummer("13010049900").dateOfBirth()).isEqualTo(new LocalDate(1900, 1, 13));
	}

	@Test
	public void dateOfBirth_forFoedselsnummerStartingWith220796HavingIndividSifferLessThan500_is22thJul1996() {
		assertThat(new Foedselsnummer("22079649900").dateOfBirth()).isEqualTo(new LocalDate(1996, 7, 22));
	}

	@Test
	public void dateOfBirth_forFoedselsnummerStartingWith130100HavingIndividSiffer500_is13thJan2000() {
		assertThat(new Foedselsnummer("13010050000").dateOfBirth()).isEqualTo(new LocalDate(2000, 1, 13));
	}

	@Test
	public void dateOfBirth_forFoedselsnummerStartingWith130100HavingIndividSiffer999_is13thJan2000() {
		assertThat(new Foedselsnummer("13010099900").dateOfBirth()).isEqualTo(new LocalDate(2000, 1, 13));
	}

	@Test
	public void dateOfBirth_forFoedselsnummerStartingWith170739HavingIndividSiffer999_is17thJul2039() {
		assertThat(new Foedselsnummer("17073999900").dateOfBirth()).isEqualTo(new LocalDate(2039, 7, 17));
	}

	@Test
	public void dateOfBirth_forFoedselsnummerStartingWith170740HavingIndividSiffer999_is17thJul1940() {
		assertThat(new Foedselsnummer("17074099900").dateOfBirth()).isEqualTo(new LocalDate(1940, 7, 17));
	}

	@Test
	public void dateOfBirth_forFoedselsnummerStartingWith170740HavingIndividSiffer900_is17thJul1940() {
		assertThat(new Foedselsnummer("17074090000").dateOfBirth()).isEqualTo(new LocalDate(1940, 7, 17));
	}

	@Test(expectedExceptions = IllegalStateException.class)
	public void dateOfBirth_forFoedselsnummerStartingWith170740HavingIndividSiffer899_givesIllegalStateException() {
		new Foedselsnummer("17074089900").dateOfBirth();
	}

	@Test
	public void dateOfBirth_forFoedselsnummerStartingWith170780HavingIndividSiffer500orGreater_is17thJuly1880() {
		assertThat(new Foedselsnummer("17078050000").dateOfBirth()).isEqualTo(new LocalDate(1880, 7, 17));
	}

	@Test
	public void dateOfBirth_forFoedselsnummerStartingWith170780HavingIndividSiffer749orGreater_is17thJuly1880() {
		assertThat(new Foedselsnummer("17078074900").dateOfBirth()).isEqualTo(new LocalDate(1880, 7, 17));
	}

	@Test(expectedExceptions = IllegalFieldValueException.class)
	public void dateOfBirth_forFoedselsnummerStartingWith320780ThusHavingInvalidDay_givesIllegalFieldValueException() {
		new Foedselsnummer("32078074900").dateOfBirth();
	}

	@Test(expectedExceptions = IllegalFieldValueException.class)
	public void dateOfBirth_forFoedselsnummerStartingWith171380ThusHavingInvalidMonth_givesIllegalFieldValueException() {
		new Foedselsnummer("17138074900").dateOfBirth();
	}

    @Test
    public void dateOfBirth_forFoedselsnummerStartingWith570780WhichIsDnummer_is17thJuly1880() {
        assertThat(new Foedselsnummer("57078010000").dateOfBirth()).isEqualTo(new LocalDate(1980, 7, 17));
    }

    @Test(expectedExceptions = IllegalFieldValueException.class)
    public void dateOfBirth_forFoedselsnummerStartingWith730780ThusBeingIllegalDnummer_givesIllegalFieldValueException() {
        new Foedselsnummer("73078074900").dateOfBirth();
    }
	
	@Test
	public void gender_ninthDigitIsOdd_returnsM() {
		assertThat(new Foedselsnummer("17078074900").gender()).isEqualTo("M");
	}

	@Test
	public void gender_ninthDigitIsEven_returnsK() {
		assertThat(new Foedselsnummer("13010050000").gender()).isEqualTo("K");
	}
}

