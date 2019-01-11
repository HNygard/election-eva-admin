package no.valg.eva.admin.frontend.translation.ctrls;

import no.evote.exception.EvoteException;
import no.valg.eva.admin.frontend.util.FacesUtil;
import no.valg.eva.admin.frontend.util.MessageUtil;
import no.evote.security.UserData;
import no.evote.service.TranslationService;
import no.valg.eva.admin.util.CSVUtil;
import no.valg.eva.admin.common.configuration.service.ElectionEventService;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.Locale;
import no.valg.eva.admin.configuration.domain.model.TextId;
import no.valg.eva.admin.frontend.BaseController;
import no.valg.eva.admin.frontend.i18n.MessageProvider;
import no.valg.eva.admin.frontend.user.ctrls.UserDataController;
import org.apache.log4j.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.EJBException;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.event.ValueChangeEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller for editing translations
 */
@Named
@ViewScoped
public class TranslationsEditController extends BaseController {
	private static final Logger LOGGER = Logger.getLogger(TranslationsEditController.class);

	@Inject
	private MessageProvider mp;
	@Inject
	private TranslationService translationService;
	@Inject
	private ElectionEventService electionEventService;
	@Inject
	private UserData userData;
	@Inject
	private UserDataController userDataController;

	private ElectionEvent electionEvent;
	private List<Locale> locales;
	private Locale fromLocale;
	private Locale toLocale;
	private boolean showOnlyMissingTranslations;

	private Map<String, String> toTexts;
	private Map<String, TextId> textIdMap;
	private List<String[]> translations;
	private String saveButtonText;

	private String filter;
	private Integer filterType;
	private boolean showFilter;

	private Map<String, String> changedTexts = new HashMap<>();
	private Map<String, String> changedTextIdDescriptions = new HashMap<>();

	@PostConstruct
	public void init() {
		electionEvent = userDataController.getElectionEvent();

		// Are we editing global or local translations?
		if ("true".equals(getRequestParameter("global"))) {
			electionEvent = null;
			locales = translationService.getAllLocales();
		} else {
			locales = electionEventService.getLocalesForEvent(userData, electionEvent);
		}

		saveButtonText = mp.get("@common.button.saveChanges");
	}

	/**
	 * Populate the translations table
	 */
	public void loadTranslationsTable() {
		List<TextId> textIdList = translationService.findTextIdsByElectionEvent(userData, electionEvent);
		Map<String, String> fromTexts = translationService.getLocaleTexts(userData, electionEvent, fromLocale.getPk());
		toTexts = translationService.getLocaleTexts(userData, electionEvent, toLocale.getPk());

		textIdMap = new HashMap<>();
		translations = new ArrayList<>();
		for (TextId textId : textIdList) {
			textIdMap.put(textId.getTextId(), textId);

			String id = textId.getTextId();
			String[] translation = { id, textId.getInfoText(), fromTexts.get(id), toTexts.get(id) };

			if (showFilter && filter != null && filter.trim().length() > 0 && !applyFilter(translation)) {
				continue;
			}

			if (!(showOnlyMissingTranslations && translation[3] != null)) {

				translations.add(translation);
			}
		}

		changedTexts = new HashMap<>();
	}

	/**
	 * Perform filtering on the translation table
	 */
	public boolean applyFilter(final String[] translation) {
		String filterLc = filter.toLowerCase(userData.getJavaLocale());

		boolean textIdContains = translation[0].toLowerCase().contains(filterLc);
		boolean descriptionContains = translation[1] != null && translation[1].toLowerCase().contains(filterLc);
		boolean fromTextContains = translation[2] != null && translation[2].toLowerCase().contains(filterLc);

		boolean toTextContains = translation[3] != null && translation[3].toLowerCase().contains(filterLc);

		boolean result;

		switch (filterType) {
		case 0:
			result = textIdContains;
			break;
		case 1:
			result = descriptionContains;
			break;
		case 2:
			result = fromTextContains;
			break;
		case 3:
			result = toTextContains;
			break;
		case 4:
			result = textIdContains || fromTextContains || toTextContains || descriptionContains;
			break;
		default:
			result = false;
		}

		return result;
	}

	/**
	 * Save all edited translations
	 */
	public void saveTranslations() {
		try {
			if (electionEvent != null) {
				translationService.updateLocaleTexts(userData, electionEvent, toLocale.getPk(), changedTexts, false);
				translationService.updateTextIdDescriptions(userData, electionEvent, changedTextIdDescriptions);
			} else {
				translationService.updateGlobalLocaleTexts(userData, toLocale.getPk(), changedTexts, false);
				translationService.updateGlobalTextIdDescriptions(userData, changedTextIdDescriptions);
			}
			MessageUtil.buildFacesMessage(getFacesContext(), null, "@translation.edit.updateMessage",
					new String[] { String.valueOf(changedTexts.size() + changedTextIdDescriptions.size()) }, FacesMessage.SEVERITY_INFO);
			changedTexts = new HashMap<>();
			changedTextIdDescriptions = new HashMap<>();
			loadTranslationsTable();
			LOGGER.info("ResourceBundle updated - text update.");
		} catch (EvoteException e) {
			LOGGER.warn(e.getMessage(), e);
			MessageUtil.buildFacesMessage(getFacesContext(), null, e.getMessage(), new String[] {}, FacesMessage.SEVERITY_ERROR);
		} catch (EJBException e) {
			LOGGER.warn(e.getMessage(), e);
			MessageUtil.buildFacesMessage(getFacesContext(), null, "@translation.unknown_error", new String[] {}, FacesMessage.SEVERITY_FATAL);
		}
	}

	/**
	 * This method is invoked when a translation field is edited. Check whether any changes were made, and update change counter
	 */
	public void setTranslation(final ValueChangeEvent event) {
		// Adding text id to component id didn't work, so we need to get the text id from a hidden field in the table cell
		String textId = null;
		for (UIComponent component : event.getComponent().getParent().getChildren()) {
			if ("javax.faces.Input".equals(component.getFamily())) {
				textId = (String) component.getAttributes().get("value");
				break;
			}
		}

		String originalTranslation = toTexts.get(textId);
		String newTranslation = (String) event.getNewValue();

		if ("".equals(newTranslation)) {
			if (originalTranslation != null) {
				changedTexts.put(textId, null);
			} else {
				changedTexts.remove(textId);
			}
		} else if (!newTranslation.equals(originalTranslation)) {
			changedTexts.put(textId, newTranslation);
		} else {
			changedTexts.remove(textId);
		}
	}

	/**
	 * This method is invoked when a description field is edited. Check whether any changes were made, and update change counter
	 */
	public void setTextIdDescription(final ValueChangeEvent event) {
		// Adding text id to component id didn't work, so we need to get the text id from a hidden field in the table cell
		String textId = null;
		for (UIComponent component : event.getComponent().getParent().getChildren()) {
			if ("javax.faces.Input".equals(component.getFamily())) {
				textId = (String) component.getAttributes().get("value");
				break;
			}
		}

		String originalTextIdDescription = textIdMap.get(textId).getInfoText();
		String newTextIdDescription = (String) event.getNewValue();

		if ("".equals(newTextIdDescription)) {
			if (originalTextIdDescription != null) {
				changedTextIdDescriptions.put(textId, null);
			} else {
				changedTextIdDescriptions.remove(textId);
			}
		} else if (!newTextIdDescription.equals(originalTextIdDescription)) {
			changedTextIdDescriptions.put(textId, newTextIdDescription);
		} else {
			changedTextIdDescriptions.remove(textId);
		}
	}

	/**
	 * Export the list of translations to a csv file
	 */
	public void getTranslationFile() throws IOException {
		List<List<String>> outRows = new ArrayList<>();
		for (String[] inRow : translations) {
			List<String> outRow = new ArrayList<>();
			outRow.add(inRow[0]);
			outRow.add(inRow[1]);
			outRow.add(inRow[2]);

			outRow.add(inRow[3]);

			outRows.add(outRow);
		}

		byte[] csv = CSVUtil.createCsvFromRowData(outRows);
		FacesUtil.sendFile("Translations_from_" + getFromLocale().getId() + "_to_" + getToLocale().getId() + ".csv", csv);
	}

	public Locale getFromLocale() {
		return fromLocale;
	}

	public void setFromLocale(final Locale fromLocale) {
		this.fromLocale = fromLocale;
	}

	public Locale getToLocale() {
		return toLocale;
	}

	public void setToLocale(final Locale toLocale) {
		this.toLocale = toLocale;
	}

	public List<Locale> getLocales() {
		return locales;
	}

	public boolean isShowOnlyMissingTranslations() {
		return showOnlyMissingTranslations;
	}

	public void setShowOnlyMissingTranslations(final boolean showOnlyMissingTranslations) {
		this.showOnlyMissingTranslations = showOnlyMissingTranslations;
	}

	public List<String[]> getTranslations() {
		return translations;
	}

	public boolean isDisableSaveButton() {
		return changedTexts.isEmpty() && changedTextIdDescriptions.isEmpty();
	}

	public String getSaveButtonText() {
		if (!(changedTexts.isEmpty() && changedTextIdDescriptions.isEmpty())) {
			return saveButtonText + " (" + (changedTexts.size() + changedTextIdDescriptions.size()) + ")";
		}
		return saveButtonText;
	}

	public String getFilter() {
		return filter;
	}

	public void setFilter(final String filter) {
		this.filter = filter;
	}

	public Integer getFilterType() {
		return filterType;
	}

	public void setFilterType(final Integer filterType) {
		this.filterType = filterType;
	}

	public boolean isShowFilter() {
		return showFilter;
	}

	public void toggleFilter() {
		showFilter = !showFilter;
	}

	public ElectionEvent getElectionEvent() {
		return electionEvent;
	}

}
