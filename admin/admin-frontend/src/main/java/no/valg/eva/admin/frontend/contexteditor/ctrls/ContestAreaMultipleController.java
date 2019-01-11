package no.valg.eva.admin.frontend.contexteditor.ctrls;

import static no.valg.eva.admin.util.StringUtil.isSet;

import javax.enterprise.context.ConversationScoped;
import javax.faces.application.FacesMessage;
import javax.inject.Inject;
import javax.inject.Named;

import no.valg.eva.admin.frontend.util.MessageUtil;
import no.evote.security.UserData;
import no.evote.service.configuration.ContestAreaService;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.ContestArea;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.frontend.ConversationScopedController;
import no.valg.eva.admin.frontend.area.ctrls.MvAreaMultipleController;

@Named
@ConversationScoped
public class ContestAreaMultipleController extends ConversationScopedController {

	@Inject
	private MvAreaMultipleController mvAreaController;
	@Inject
	private UserData userData;
	@Inject
	private ContestAreaService contestAreaService;

	private Contest currentContest;

	@Override
	protected void doInit() {
	}

	public void doGetCreateContestArea(final Contest contest) {
		mvAreaController.loadAndReset();
		mvAreaController.setSelectedAreaLevel(contest.getElection().getAreaLevel());
		mvAreaController.setCountryId(mvAreaController.getCountryItems().get(0).getValue().toString());
		mvAreaController.changeCountry();
		mvAreaController.setCountyId(null);

		currentContest = contest;
	}

	public void doCreateContestArea() {
		if (isSet(mvAreaController.getCountyId())) {
			Contest contest = currentContest;
			currentContest = null;
			if (isSet(mvAreaController.getMunicipalityId())) {
				createContestAreaForSingleArea(contest);
			} else {
				createContestAreaForParentArea(contest);
			}
		}
	}

	private void createContestAreaForSingleArea(Contest contest) {
		ContestArea newContestArea = new ContestArea();
		newContestArea.setContest(contest);
		newContestArea.setMvArea(mvAreaController.getSelectedMvArea());
		execute(() -> {
			contestAreaService.create(userData, newContestArea);
			MessageUtil.buildDetailMessage(MessageUtil.CREATE_SUCCESSFUL_KEY, FacesMessage.SEVERITY_INFO);
		});
	}

	/**
	 * Creates contestArea for all the areas in a list. Used in sami election where all municipalities for a given county is a part of the contest.
	 */
	private void createContestAreaForParentArea(Contest contest) {
		for (MvArea mvArea : mvAreaController.getMunicipalityList()) {
			ContestArea newContestArea = new ContestArea();
			newContestArea.setContest(contest);
			newContestArea.setMvArea(mvArea);
			execute(() -> contestAreaService.create(userData, newContestArea));
		}
		MessageUtil.buildDetailMessage(MessageUtil.CREATE_SUCCESSFUL_KEY, FacesMessage.SEVERITY_INFO);

	}

	public Contest getCurrentContest() {
		return currentContest;
	}

	public void setCurrentContest(Contest currentContest) {
		this.currentContest = currentContest;
	}

}
