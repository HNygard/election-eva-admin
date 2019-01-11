package no.valg.eva.admin.frontend.election.ctrls;

import no.valg.eva.admin.frontend.util.FacesUtil;
import no.evote.service.TranslationService;
import no.valg.eva.admin.common.configuration.service.ElectionEventService;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.ElectionEventStatus;
import no.valg.eva.admin.configuration.domain.model.Locale;
import no.valg.eva.admin.frontend.BaseController;
import no.valg.eva.admin.frontend.i18n.MessageProvider;
import no.valg.eva.admin.frontend.user.ctrls.UserDataController;

import javax.faces.event.ValueChangeEvent;
import javax.inject.Inject;
import javax.servlet.ServletContext;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public abstract class EditElectionEventController extends BaseController {

	@Inject
	private TranslationService translationService;
	@Inject
	private MessageProvider messageProvider;
	@Inject
	private ElectionEventService electionEventService;
	@Inject
	private UserDataController userDataController;

	private List<Locale> locales;
	private List<String> themes = new ArrayList<>();
	private Set<Locale> availableLocalesSet = new HashSet<>();
	private List<ElectionEvent> electionEventList;
	private List<ElectionEventStatus> electionEventStatusList;

	public void valueChangeSelectManyCheckbox(ValueChangeEvent evt) {
		availableLocalesSet.clear();
		List<String> pks = (List<String>) evt.getNewValue();
		for (String pk : pks) {
			Locale changeLocale = getLocale(Long.parseLong(pk));
			if (changeLocale != null) {
				availableLocalesSet.add(changeLocale);
			}
		}
	}

	public List<String> getThemes() {
		return themes;
	}

	public List<Locale> getLocales() {
		return locales;
	}

	public Set<Locale> getAvailableLocalesSet() {
		return availableLocalesSet;
	}

	public List<ElectionEvent> getElectionEventList() {
		return electionEventList;
	}

	public List<ElectionEventStatus> getElectionEventStatusList() {
		return electionEventStatusList;
	}

	public ElectionEventService getElectionEventService() {
		return electionEventService;
	}

	public UserDataController getUserDataController() {
		return userDataController;
	}

	public MessageProvider getMessageProvider() {
		return messageProvider;
	}

	protected void loadLocaleList() {
		availableLocalesSet = new TreeSet<>(Comparator.comparing(locale -> messageProvider.get(locale.getName())));
		locales = translationService.getAllLocales();
	}

	protected void findThemes() {
		themes.clear();
		ServletContext servletContext = FacesUtil.getServletContext();
		String absoluteDiskPath = servletContext.getRealPath("resources/css/themes");
		File file = new File(absoluteDiskPath);
		if (file != null) {
			File[] files = file.listFiles(pathname -> {
				return pathname.getName().endsWith(".css");
			});

			if (files != null) {
				for (File cssFile : files) {
					themes.add(cssFile.getName());
				}
			}
		}
	}

	protected void reloadElectionEventList() {
		electionEventList = electionEventService.findAll(getUserDataController().getUserData());
		Collections.sort(electionEventList, new Comparator<ElectionEvent>() {
			@Override
			public int compare(ElectionEvent ee1, ElectionEvent ee2) {
				return ee1.getId().compareTo(ee2.getId());
			}
		});
	}

	protected void loadElectionEventStatusList() {
		electionEventStatusList = electionEventService.findAllElectionEventStatuses(userDataController.getUserData());
	}

	private Locale getLocale(long pk) {
		for (Locale locale : locales) {
			if (locale.getPk() == pk) {
				return locale;
			}
		}
		return null;
	}

	protected ElectionEventStatus getElectionEventStatus(int id) {
		for (ElectionEventStatus status : electionEventStatusList) {
			if (status.getId() == id) {
				return status;
			}
		}
		return null;
	}

}
