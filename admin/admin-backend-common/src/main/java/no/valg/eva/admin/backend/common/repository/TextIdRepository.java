package no.valg.eva.admin.backend.common.repository;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.persistence.NoResultException;

import no.evote.security.UserData;
import no.valg.eva.admin.configuration.domain.model.TextId;

@Default
@ApplicationScoped
public class TextIdRepository extends BaseRepository {
	private static final String ELECTION_EVENT_PK = "electionEventPk";
	private static final String TEXT_ID = "textId";

	public TextIdRepository() {
	}

	public TextId create(UserData userData, TextId textId) {
		return super.createEntity(userData, textId);
	}

	public List<TextId> findByElectionEvent(Long electionEventPk) {
		return getEm()
				.createNamedQuery("TextId.findByElectionEvent", TextId.class)
				.setParameter(ELECTION_EVENT_PK, electionEventPk)
				.getResultList();
	}

	public List<TextId> findGlobal() {
		return getEm().createNamedQuery("TextId.findGlobal", TextId.class).getResultList();
	}

	public TextId findByElectionEventAndId(Long electionEventPk, String textIdStr) {
		try {
			return getEm()
					.createNamedQuery("TextId.findByElectionEventAndId", TextId.class)
					.setParameter(ELECTION_EVENT_PK, electionEventPk)
					.setParameter(TEXT_ID, textIdStr)
					.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	public TextId findGlobalById(String textIdStr) {
		try {
			return getEm()
					.createNamedQuery("TextId.findGlobalById", TextId.class)
					.setParameter(TEXT_ID, textIdStr)
					.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	public TextId update(UserData userData, TextId textId) {
		return super.updateEntity(userData, textId);
	}

	public void delete(UserData userData, Long pk) {
		super.deleteEntity(userData, TextId.class, pk);
	}
}
