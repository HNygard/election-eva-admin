package no.valg.eva.admin.frontend.area.ctrls;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ConversationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.frontend.common.PageTitleMetaModel;
import no.valg.eva.admin.frontend.i18n.MessageProvider;

/**
 * Used for selecting areas in the area hierarchy.
 */
@Named("mvAreaController")
@ConversationScoped
public class MvAreaController extends BaseMvAreaController {
	// Intentionally empty

	@Inject
	private MessageProvider messageProvider;
	
	public List<PageTitleMetaModel> getPageTitleMeta() {

		List<PageTitleMetaModel> models = new ArrayList<>();

		ElectionEvent electionEvent = getElectionEvent();
		if (electionEvent == null) {
			return models;
		}

		models.add(new PageTitleMetaModel(messageProvider.get("@election_level[1].name"), electionEvent.getName()));
		
		return models;
	}
}
