package no.evote.service;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import no.evote.security.UserData;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.Locale;
import no.valg.eva.admin.configuration.domain.model.LocaleText;
import no.valg.eva.admin.configuration.domain.model.TextId;

public interface TranslationService extends Serializable {

	void updateLocaleTexts(final UserData userData, final ElectionEvent electionEvent, final Long localePk, final Map<String, String> localeTexts,
			final boolean addNewTextIds);

	void updateGlobalLocaleTexts(final UserData userData, final Long localePk, final Map<String, String> localeTexts, final boolean addNewTextIds);

	void updateTextIdDescriptions(final UserData userData, final ElectionEvent electionEvent, final Map<String, String> textIdDescriptions);

	void updateGlobalTextIdDescriptions(final UserData userData, final Map<String, String> textIdDescriptions);

	List<Locale> getAllLocales();

	List<TextId> findTextIdsByElectionEvent(UserData userData, ElectionEvent electionEvent);

	Map<String, String> getLocaleTexts(UserData userData, ElectionEvent electionEvent, Long localePk);

	Locale findLocaleById(String id);

	LocaleText getLocaleTextByElectionEvent(final Long electionEventPk, final Long localePk, final String textId);
}
