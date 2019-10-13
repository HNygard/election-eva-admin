package no.evote.service;

import static no.valg.eva.admin.common.rbac.Accesses.Konfigurasjon_Oversettelser;
import static no.valg.eva.admin.common.rbac.SecurityType.READ;
import static no.valg.eva.admin.common.rbac.SecurityType.WRITE;

import java.util.List;
import java.util.Map;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import no.evote.constants.ElectionLevelEnum;
import no.evote.security.SecureEntity;
import no.evote.security.UserData;
import no.valg.eva.admin.backend.common.repository.LocaleRepository;
import no.valg.eva.admin.backend.reporting.jasperserver.JasperReportServiceBean;
import no.valg.eva.admin.common.rbac.Security;
import no.valg.eva.admin.common.rbac.SecurityNone;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.Locale;
import no.valg.eva.admin.configuration.domain.model.LocaleText;
import no.valg.eva.admin.configuration.domain.model.TextId;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;

/**
 * Methods for fetching and maintaining translations. Entities are detached before returned. The reason for this is: We apply AntiSamy filtering on translations
 * to and from the database. If this alters translations that have been retrieved from the database, Hibernate will try to update the database (unless we detach
 * them). And in some cases (e.g. in the list proposal apps) we don't have write access to the translation tables.
 * 
 * NOTE! This is legacy code and based on the old architecture and should not be extended and or build upon!
 */
@Stateless(name = "TranslationService")

@Default
@Remote(TranslationService.class)
public class TranslationServiceEjb implements TranslationService {
	@Inject
	private TranslationServiceBean translationService;
	@Inject
	private LocaleRepository localeRepository;
	@Inject
	@java.lang.SuppressWarnings("unused")
	private JasperReportServiceBean jasperReportServiceBean;

	/**
	 * Update global translations. Is in a separate method because of securable object.
	 */
	@Override
	@Security(accesses = Konfigurasjon_Oversettelser, type = WRITE)
	public void updateGlobalLocaleTexts(UserData userData, Long localePk, Map<String, String> localeTexts, boolean addNewTextIds) {
		translationService.updateGlobalLocaleTexts(userData, localePk, localeTexts, addNewTextIds);
	}

	/**
	 * Update local translations. Is in a separate method because of securable object.
	 */
	@Override
	@Security(accesses = Konfigurasjon_Oversettelser, type = WRITE)
	public void updateLocaleTexts(UserData userData,
			@SecureEntity(electionLevel = ElectionLevelEnum.ELECTION_EVENT) ElectionEvent electionEvent,
			Long localePk,
			Map<String, String> localeTexts,
			boolean addNewTextIds) {
		translationService.updateLocaleTexts(userData, electionEvent, localePk, localeTexts, addNewTextIds);
	}

	@Override
	@SecurityNone
	public List<Locale> getAllLocales() {
		return localeRepository.findAllLocales();
	}

	@Override
	@Security(accesses = Konfigurasjon_Oversettelser, type = READ)
	public Map<String, String> getLocaleTexts(UserData userData, ElectionEvent electionEvent, Long localePk) {
		return translationService.getLocaleTexts(electionEvent, localePk);
	}

	@Override
	@Security(accesses = Konfigurasjon_Oversettelser, type = READ)
	public List<TextId> findTextIdsByElectionEvent(UserData userData, ElectionEvent electionEvent) {
		return translationService.findTextIdsByElectionEvent(electionEvent);
	}

	@Override
	@SecurityNone
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public LocaleText getLocaleTextByElectionEvent(Long electionEventPk, Long localePk, String textId) {
		return translationService.getLocaleTextByElectionEvent(electionEventPk, localePk, textId);
	}

	@Override
	@SecurityNone
	public Locale findLocaleById(String id) {
		return localeRepository.findById(id);
	}

	/**
	 * Update local text id descriptions. Is in a separate method because of securable object.
	 */
	@Override
	@Security(accesses = Konfigurasjon_Oversettelser, type = WRITE)
	public void updateTextIdDescriptions(UserData userData, ElectionEvent electionEvent, Map<String, String> textIdDescriptions) {
		translationService.updateTextIdDescriptions(userData, electionEvent, textIdDescriptions);
	}

	/**
	 * Update global text id descriptions. Is in a separate method because of securable object.
	 */
	@Override
	@Security(accesses = Konfigurasjon_Oversettelser, type = WRITE)
	public void updateGlobalTextIdDescriptions(UserData userData, Map<String, String> textIdDescriptions) {
		translationService.updateGlobalTextIdDescriptions(userData, textIdDescriptions);
	}

}
