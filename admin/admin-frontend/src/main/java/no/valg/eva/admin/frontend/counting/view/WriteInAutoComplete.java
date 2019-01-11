package no.valg.eva.admin.frontend.counting.view;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.enterprise.context.ConversationScoped;
import javax.inject.Named;

import no.valg.eva.admin.util.StringUtil;
import no.evote.util.Wrapper;
import no.valg.eva.admin.common.counting.model.modifiedballots.Candidate;
import no.valg.eva.admin.common.counting.model.modifiedballots.CandidateRef;
import no.valg.eva.admin.frontend.i18n.MessageProvider;

/**
 * Class intended for supporting the auto-complete functionality needed for write-ins
 */
@Named
@ConversationScoped
public class WriteInAutoComplete implements Serializable {

	protected Collection<Candidate> allWriteInCandidates;
	protected List<Wrapper<Candidate>> wrappedWriteIns;
	protected MessageProvider messageProvider;

	public WriteInAutoComplete() {
	}

	public void fillWriteInAutoComplete(int maxWriteIns, Collection<Candidate> allWriteInCandidates, Collection<Candidate> existingWriteIns,
			MessageProvider messageProvider) {
		this.allWriteInCandidates = allWriteInCandidates;
		this.messageProvider = messageProvider;
		populateWrappedWriteInsWith(existingWriteIns, maxWriteIns);
	}

	private void populateWrappedWriteInsWith(Collection<Candidate> existingWriteIns, int maxWriteIns) {
		this.wrappedWriteIns = new ArrayList<>(maxWriteIns);
		Iterator<Candidate> existingWriteInsIterator = existingWriteIns.iterator();
		for (int i = 0; i < maxWriteIns; i++) {
			if (existingWriteInsIterator.hasNext()) {
				this.wrappedWriteIns.add(new Wrapper<>(existingWriteInsIterator.next()));
			} else {
				this.wrappedWriteIns.add(new Wrapper<Candidate>(null));
			}
		}
	}

	public List<Wrapper<Candidate>> getWrappedWriteIns() {
		return wrappedWriteIns;
	}

	public List<Candidate> filterCandidates(final String query) {
		List<Candidate> suggestions = new ArrayList<>();

		for (Candidate candidate : allWriteInCandidates) {
			String lowCaseQuery = query.toLowerCase(Locale.getDefault());
			String lowCaseCandidateName = candidate.getName().toLowerCase(Locale.getDefault());
			if ((StringUtil.isInSplittedString(lowCaseCandidateName, lowCaseQuery))) {
				suggestions.add(candidate);
			}
		}

		return suggestions;
	}

	public Set<Candidate> getWriteInsFromAutoComplete() {
		Set<Candidate> mappedCandidates = new LinkedHashSet<>();

		for (Wrapper<Candidate> candidateWrapper : wrappedWriteIns) {
			if (candidateWrapper.getValue() != null) {
				CandidateRef candidateRef = candidateWrapper.getValue().getCandidateRef();
				mappedCandidates.add(getCandidateFrom(candidateRef));
			}
		}

		return mappedCandidates;
	}

	private Candidate getCandidateFrom(CandidateRef candidateRef) {
		for (Candidate potentialCandidate : allWriteInCandidates) {
			if (potentialCandidate.getCandidateRef().equals(candidateRef)) {
				return potentialCandidate;
			}
		}
		
		throw new IllegalStateException("Write-in not found in list of candidates");
	}

	public String getCandidateDisplayName(Candidate candidate) {
		String displayName = "";

		if (candidate == null) {
			return displayName;
		}

		if (candidate.getName() != null) {
			displayName += candidate.getName();
		}

		if (candidate.getPartyName() != null) {
			displayName += " (" + messageProvider.get(candidate.getPartyName()) + ")";
		}

		return displayName;
	}

	public boolean isWriteInsEnabled() {
		return wrappedWriteIns.size() > 0;
	}
}
