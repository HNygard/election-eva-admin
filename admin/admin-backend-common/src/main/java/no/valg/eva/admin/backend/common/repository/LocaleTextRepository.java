package no.valg.eva.admin.backend.common.repository;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import no.evote.security.UserData;
import no.valg.eva.admin.configuration.domain.model.LocaleText;

import org.joda.time.DateTime;

public class LocaleTextRepository extends BaseRepository {
	private static final String ELECTION_EVENT_PK = "electionEventPk";
	private static final String LOCALE_PK = "localePk";

	public LocaleTextRepository() {
	}

	public LocaleTextRepository(EntityManager entityManager) {
		super(entityManager);
	}

	public LocaleText update(UserData userData, LocaleText localeText) {
		return super.updateEntity(userData, localeText);
	}

	public List<LocaleText> findByTextId(Long textIdPk) {
		TypedQuery<LocaleText> query = getEm().createNamedQuery("LocaleText.findByTextId", LocaleText.class);
		query.setParameter("textIdPk", textIdPk);
		return query.getResultList();
	}

	public LocaleText create(UserData userData, LocaleText localeText) {
		return super.createEntity(userData, localeText);
	}

	public void delete(UserData userData, Long pk) {
		super.deleteEntity(userData, LocaleText.class, pk);
	}

	@SuppressWarnings("unchecked")
	public List<Object[]> findByElectionEvent(Long electionEventPk, Long localePk) {
		return getEm()
				.createNamedQuery("LocaleText.findByElectionEvent")
				.setParameter(ELECTION_EVENT_PK, electionEventPk)
				.setParameter(LOCALE_PK, localePk)
				.getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<Object[]> findGlobal(Long localePk) {
		return getEm()
				.createNamedQuery("LocaleText.findGlobal")
				.setParameter(LOCALE_PK, localePk)
				.getResultList();
	}

	public LocaleText findByElectionEventLocaleAndTextId(Long electionEventPk, Long localePk, String textId) {
		try {
			return getEm()
					.createNamedQuery("LocaleText.findByElectionEventLocaleAndTextId", LocaleText.class)
					.setParameter(ELECTION_EVENT_PK, electionEventPk)
					.setParameter(LOCALE_PK, localePk)
					.setParameter("textId", textId)
					.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	public LocaleText findGlobalByLocaleAndTextId(Long localePk, String textId) {
		try {
			return getEm()
					.createNamedQuery("LocaleText.findGlobalByLocaleAndTextId", LocaleText.class)
					.setParameter(LOCALE_PK, localePk)
					.setParameter("textId", textId)
					.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	public List<LocaleText> findByElectionEventLocale(Long electionEventPk, Long localePk) {
		return getEm()
				.createNamedQuery("LocaleText.findByElectionEventLocale", LocaleText.class)
				.setParameter(ELECTION_EVENT_PK, electionEventPk)
				.setParameter(LOCALE_PK, localePk)
				.getResultList();
	}

	public List<LocaleText> findGlobalByLocale(Long localePk) {
		return getEm()
				.createNamedQuery("LocaleText.findGlobalByLocale", LocaleText.class)
				.setParameter(LOCALE_PK, localePk)
				.getResultList();
	}

	public DateTime lastUpdatedTimestamp() {
		return getEm().createNamedQuery("LocaleText.lastUpdatedTimestamp", DateTime.class).getSingleResult();
	}
}
