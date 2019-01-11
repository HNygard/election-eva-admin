package no.valg.eva.admin.configuration.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import no.valg.eva.admin.configuration.domain.visitor.ConfigurationVisitor;
import no.valg.eva.admin.test.MockUtilsTestCase;

import org.testng.annotations.Test;


public class BallotTest extends MockUtilsTestCase {

	@Test
	public void compareTo_verifySorting() {
		Ballot ballotWithNoDisplayOrder = ballot(1L, null);
		Ballot ballot1 = ballot(3L, 1);
		Ballot ballot2 = ballot(4L, 2);
		Ballot ballot3 = ballot(5L, 3);

		Set<Ballot> sorted = new TreeSet<>();
		sorted.add(ballot3);
		sorted.add(ballotWithNoDisplayOrder);
		sorted.add(ballot1);
		sorted.add(ballot2);

		assertThat(sorted).hasSize(4);
		Iterator<Ballot> iter = sorted.iterator();
		assertThat(iter.next()).isSameAs(ballot1);
		assertThat(iter.next()).isSameAs(ballot2);
		assertThat(iter.next()).isSameAs(ballot3);
		assertThat(iter.next()).isSameAs(ballotWithNoDisplayOrder);
	}

	private Ballot ballot(Long pk, Integer displayOrder) {
		Ballot ballot = new Ballot();
		ballot.setPk(pk);
		Affiliation affiliation = new Affiliation();
		affiliation.setDisplayOrder(displayOrder);
		ballot.setAffiliation(affiliation);
		return ballot;
	}

	@Test
	public void getAffiliationCandidates_givenAffiliationWithCandidates_returnsAffiliationCandidates() throws Exception {
		HashSet<Candidate> candidates = new HashSet<>();
		Affiliation affiliation = mock(Affiliation.class);
		when(affiliation.getCandidates()).thenReturn(candidates);
		Ballot ballot = new Ballot();
		ballot.setAffiliation(affiliation);
		assertThat(ballot.getAffiliationCandidates()).isSameAs(candidates);
	}

	@Test
	public void accept_givenVisitorWithIncludeTrue_callsVisitOnVisitor() throws Exception {
		ConfigurationVisitor visitor = mock(ConfigurationVisitor.class);
		Ballot ballot = new Ballot();
		ballot.setPk(new Random().nextLong());
		Affiliation affiliation = mock(Affiliation.class);
		ballot.setAffiliation(affiliation);
		when(visitor.include(ballot)).thenReturn(true);
		ballot.accept(visitor);
		verify(visitor).visit(ballot);
	}

	@Test
	public void accept_givenVisitorWithIncludeFalse_doesNotCallVisitOnVisitor() throws Exception {
		ConfigurationVisitor visitor = mock(ConfigurationVisitor.class);
		Ballot ballot = new Ballot();
		ballot.setPk(new Random().nextLong());
		Affiliation affiliation = mock(Affiliation.class);
		ballot.setAffiliation(affiliation);
		when(visitor.include(ballot)).thenReturn(false);
		ballot.accept(visitor);
		verify(visitor, never()).visit(ballot);
	}

	@Test
	public void accept_givenVisitorWithIncludeTrue_callsAcceptOnAffiliation() throws Exception {
		ConfigurationVisitor visitor = mock(ConfigurationVisitor.class);
		Ballot ballot = new Ballot();
		ballot.setPk(new Random().nextLong());
		Affiliation affiliation = mock(Affiliation.class);
		ballot.setAffiliation(affiliation);
		when(visitor.include(ballot)).thenReturn(true);
		ballot.accept(visitor);
		verify(affiliation).accept(visitor);
	}

	@Test
	public void accept_givenVisitorWithIncludeFalse_doesNotCallAcceptOnAffiliation() throws Exception {
		ConfigurationVisitor visitor = mock(ConfigurationVisitor.class);
		Ballot ballot = new Ballot();
		ballot.setPk(new Random().nextLong());
		Affiliation affiliation = mock(Affiliation.class);
		ballot.setAffiliation(affiliation);
		when(visitor.include(ballot)).thenReturn(false);
		ballot.accept(visitor);
		verify(affiliation, never()).accept(visitor);
	}
}

