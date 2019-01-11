package no.valg.eva.admin.configuration.domain.visitor;

import no.valg.eva.admin.configuration.domain.model.Affiliation;
import no.valg.eva.admin.configuration.domain.model.Ballot;
import no.valg.eva.admin.configuration.domain.model.Candidate;
import no.valg.eva.admin.configuration.domain.model.Contest;

public interface ConfigurationVisitor {
	boolean include(Contest contest);

	void visit(Contest contest);

	boolean include(Ballot ballot);

	void visit(Ballot ballot);

	boolean include(Affiliation affiliation);

	void visit(Affiliation affiliation);

	boolean include(Candidate candidate);

	void visit(Candidate candidate);
}
