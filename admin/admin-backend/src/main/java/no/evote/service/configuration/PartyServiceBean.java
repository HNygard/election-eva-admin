package no.evote.service.configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

import no.evote.security.UserData;
import no.evote.service.LocaleTextServiceBean;
import no.evote.service.TranslationServiceBean;
import no.evote.validation.PartyValidationManual;
import no.valg.eva.admin.backend.reporting.jasperserver.JasperReportServiceBean;
import no.valg.eva.admin.common.configuration.model.party.Parti;
import no.valg.eva.admin.configuration.application.party.PartyMapper;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.LocaleText;
import no.valg.eva.admin.configuration.domain.model.Party;
import no.valg.eva.admin.configuration.domain.model.TextId;
import no.valg.eva.admin.configuration.repository.AffiliationRepository;
import no.valg.eva.admin.configuration.repository.party.PartyRepository;

/**
 */
public class PartyServiceBean {
	private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
	@Inject
	private AffiliationRepository affiliationRepository;
	@Inject
	private PartyRepository partyRepository;
	@Inject
	private LocaleTextServiceBean localeTextService;
	@Inject
	private TranslationServiceBean translationService;
	@Inject
	private PartyMapper partyMapper;
	@Inject
	private JasperReportServiceBean jasperReportServiceBean;

	public Party create(UserData userData, Party party) {
		party.setId(party.getId().toUpperCase());
		setPartyNameToAllLocale(userData, party);
		return partyRepository.create(userData, party);
	}

	private List<LocaleText> setPartyNameToAllLocale(final UserData userData, final Party newParty) {
		String textIdStr = newParty.getName();
		String translatedPartyName = newParty.getTranslatedPartyName();

		List<LocaleText> localeTexts = localeTextService.createLocaleTextToAllLocale(userData, textIdStr, translatedPartyName, newParty.getElectionEvent());
		jasperReportServiceBean.refreshResourceBundles();

		return localeTexts;
	}

	public Party update(UserData userData, Parti parti) {
		Party party = asParty(parti, userData.electionEvent(), false);
		party.setId(party.getId().toUpperCase());
		if (party.getPk() != null) {
			Party dbParty = partyRepository.findByPk(party.getPk());
			if (dbParty.getId().equals(party.getId())) {
				String textIdName = party.getName();
				localeTextService.updateLocaleTextToAllLocale(userData, textIdName, party.getTranslatedPartyName(), dbParty.getElectionEvent());
				jasperReportServiceBean.refreshResourceBundles();
			} else {
				TextId textId = translationService.findTextIdByElectionEvent(dbParty.getElectionEvent(), dbParty.getName());
				translationService.deleteTextId(userData, textId);
				setPartyNameToAllLocale(userData, party);
			}
			return party;
		}
		create(userData, party);
		return party;
	}

	public void delete(UserData userData, Party party) {
		TextId textId = translationService.findTextIdByElectionEvent(party.getElectionEvent(), party.getName());
		partyRepository.delete(userData, party.getPk());
		translationService.deleteTextId(userData, textId);
	}

	public List<String> validateParty(UserData userData, Parti parti) {

		List<String> validationFeedbackList = new ArrayList<>();
		if (parti.getPartyPk() == null && partyRepository.partyIdExist(parti.getId(), userData.getElectionEventPk())) {
			validationFeedbackList.add("@validation.party.id.exist");
		}

		if (!validationFeedbackList.isEmpty()) {
			return validationFeedbackList;
		}

		// parti som evt oppdateres må frikobles fra sesjonen for å forhindre at hibernate flusher endringer ved validering
		Party party = asParty(parti, userData.electionEvent(), true);

		Set<ConstraintViolation<Party>> constraintViolations = validator.validate(party, PartyValidationManual.class);
		if (!constraintViolations.isEmpty()) {
			validationFeedbackList
					.addAll(constraintViolations.stream().filter(c -> c != null).map(ConstraintViolation::getMessage).collect(Collectors.toList()));
		}
		return validationFeedbackList;
	}

	private Party asParty(Parti parti, ElectionEvent electionEvent, boolean detach) {
		Party party;
		if (parti.getPartyPk() != null) {
			party = findParty(parti, detach);
			partyMapper.updateParty(party, parti);
		} else {
			party = partyMapper.toParty(parti, electionEvent);
		}
		return party;
	}

	private Party findParty(Parti parti, boolean detach) {
		return detach ? partyRepository.findDetachedByPk(parti.getPartyPk()) : partyRepository.findByPk(parti.getPartyPk());
	}

	public List<String> validatePartyForDelete(UserData userData, Party party) {
		List<String> validationFeedbackList = new ArrayList<>();

		if (hasAffiliation(userData, party.getId())) {
			validationFeedbackList.add("@validation.party.name.constraint.affiliation");
		}

		return validationFeedbackList;
	}

	private boolean hasAffiliation(UserData userData, String partyId) {
		return affiliationRepository.hasAffiliationPartyId(partyId, userData.getElectionEventPk());
	}

}
