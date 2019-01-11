package no.valg.eva.admin.configuration.domain.model;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Random;

import no.valg.eva.admin.configuration.domain.visitor.ConfigurationVisitor;

import org.testng.annotations.Test;

public class AffiliationTest {
	@Test
	public void accept_givenVisitorWithIncludeTrue_callsVisitOnVisitor() throws Exception {
		ConfigurationVisitor visitor = mock(ConfigurationVisitor.class);
		Affiliation affiliation = new Affiliation();
		affiliation.setPk(new Random().nextLong());
		affiliation.setParty(mock(Party.class));
		when(visitor.include(affiliation)).thenReturn(true);
		affiliation.accept(visitor);
		verify(visitor).visit(affiliation);
	}

	@Test
	public void accept_givenVisitorWithIncludeFalse_doesNotCallVisitOnVisitor() throws Exception {
		ConfigurationVisitor visitor = mock(ConfigurationVisitor.class);
		Affiliation affiliation = new Affiliation();
		affiliation.setPk(new Random().nextLong());
		affiliation.setParty(mock(Party.class));
		when(visitor.include(affiliation)).thenReturn(false);
		affiliation.accept(visitor);
		verify(visitor, never()).visit(affiliation);
	}

	@Test
	public void accept_givenVisitorWithIncludeTrue_callsAcceptOnAffiliations() throws Exception {
		ConfigurationVisitor visitor = mock(ConfigurationVisitor.class);
		Affiliation affiliation = new Affiliation();
		affiliation.setPk(new Random().nextLong());
		affiliation.setParty(mock(Party.class));
		Candidate candidate = mock(Candidate.class);
		affiliation.getCandidates().add(candidate);
		when(visitor.include(affiliation)).thenReturn(true);
		affiliation.accept(visitor);
		verify(candidate).accept(visitor);
	}

	@Test
	public void accept_givenVisitorWithIncludeFalse_doesNotCallAcceptOnAffiliations() throws Exception {
		ConfigurationVisitor visitor = mock(ConfigurationVisitor.class);
		Affiliation affiliation = new Affiliation();
		affiliation.setPk(new Random().nextLong());
		affiliation.setParty(mock(Party.class));
		Candidate candidate = mock(Candidate.class);
		affiliation.getCandidates().add(candidate);
		when(visitor.include(affiliation)).thenReturn(false);
		affiliation.accept(visitor);
		verify(candidate, never()).accept(visitor);
	}
}
