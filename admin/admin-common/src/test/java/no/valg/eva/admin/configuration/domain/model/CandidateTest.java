package no.valg.eva.admin.configuration.domain.model;

import static no.valg.eva.admin.configuration.domain.model.Candidate.Gender.FEMALE;
import static no.valg.eva.admin.configuration.domain.model.Candidate.Gender.MALE;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.Random;

import no.valg.eva.admin.configuration.domain.visitor.ConfigurationVisitor;
import no.valg.eva.admin.test.BaseTakeTimeTest;

import org.testng.annotations.Test;

public class CandidateTest extends BaseTakeTimeTest {

	@Test
	public void forCandidateWithoutValidIdGenderIsBlank() {
		Candidate candidate = new Candidate();
		candidate.setId("123456789"); // invalid fnr
		assertEquals("", candidate.getGender());
	}

	@Test
	public void forCandidateWithValidIdWhereDigitNo9IsEvenGenderIsFemale() {
		Candidate candidate = new Candidate();
		candidate.setId("16106336834");
		assertEquals(FEMALE.getValue(), candidate.getGender());
		assertTrue(candidate.isFemale());
	}

	@Test
	public void forCandidateWithValidIdWhereDigitNo9IsOddGenderIsMale() {
		Candidate candidate = new Candidate();
		candidate.setId("14048846549");
		assertEquals(MALE.getValue(), candidate.getGender());
		assertTrue(candidate.isMale());
	}
	
	@Test
	public void accept_givenVisitorWithIncludeTrue_callsVisitOnVisitor() {
		ConfigurationVisitor visitor = mock(ConfigurationVisitor.class);
		Candidate candidate = new Candidate();
		candidate.setPk(new Random().nextLong());
		when(visitor.include(candidate)).thenReturn(true);
		candidate.accept(visitor);
		verify(visitor).visit(candidate);
	}

	@Test
	public void accept_givenVisitorWithIncludeFalse_doesNotCallVisitOnVisitor() {
		ConfigurationVisitor visitor = mock(ConfigurationVisitor.class);
		Candidate candidate = new Candidate();
		candidate.setPk(new Random().nextLong());
		when(visitor.include(candidate)).thenReturn(false);
		candidate.accept(visitor);
		verify(visitor, never()).visit(candidate);
	}
}
