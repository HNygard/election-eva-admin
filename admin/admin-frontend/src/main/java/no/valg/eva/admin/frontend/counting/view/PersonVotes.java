package no.valg.eva.admin.frontend.counting.view;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import no.valg.eva.admin.common.counting.model.modifiedballots.Candidate;

public class PersonVotes {

    private final Set<Candidate> candidatesForPersonVotes;
    
	public PersonVotes(Collection<Candidate> allCandidates, Set<Candidate> candidatesWithPersonVotes) {
        candidatesForPersonVotes = new TreeSet<>();
        candidatesForPersonVotes.addAll(allCandidates);
        candidatesForPersonVotes.removeAll(candidatesWithPersonVotes); // remove er nødvendig her
        candidatesForPersonVotes.addAll(candidatesWithPersonVotes);
		
	}
	
	public List<Candidate> getCandidatesForPersonVotes() {
		return new ArrayList<>(candidatesForPersonVotes);
	}
	
	public Set<Candidate> getCandidatesVoteSet() {
		return candidatesForPersonVotes;
	}
}
