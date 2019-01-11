package no.valg.eva.admin.frontend.categories.ctrls;

import no.evote.constants.ElectionLevelEnum;
import no.evote.presentation.config.counting.ElectionVoteCountCategoryElement;
import no.valg.eva.admin.frontend.util.MessageUtil;
import no.evote.security.UserData;
import no.evote.service.configuration.ElectionVoteCountCategoryService;
import no.evote.service.configuration.MvElectionService;
import no.evote.service.configuration.VoteCountCategoryService;
import no.valg.eva.admin.common.configuration.service.MunicipalityService;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.configuration.domain.model.ElectionGroup;
import no.valg.eva.admin.configuration.domain.model.ElectionVoteCountCategory;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.configuration.domain.model.VoteCountCategory;
import no.valg.eva.admin.frontend.BaseController;
import no.valg.eva.admin.frontend.common.PageTitleMetaModel;
import no.valg.eva.admin.frontend.i18n.MessageProvider;
import no.valg.eva.admin.frontend.user.ctrls.UserDataController;
import org.apache.log4j.Logger;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * For central configuring of count elements.
 */
@Named
@ViewScoped
public class ElectionVoteCountCategoryController extends BaseController {
    private static final Logger LOGGER = Logger.getLogger(ElectionVoteCountCategoryController.class);

    @Inject
    private MessageProvider messageProvider;
    @Inject
    private MvElectionService mvElectionService;
    @Inject
    private UserData userData;
    @Inject
    private UserDataController userDataController;
    @Inject
    private ElectionVoteCountCategoryService electionVoteCountCategoryService;
    @Inject
    private VoteCountCategoryService voteCountCategoryService;
    @Inject
    private MunicipalityService municipalityService;

    private List<ElectionVoteCountCategoryElement> elements;
    private ElectionGroup selectedElectionGroup;
    private boolean editable;
    private boolean requiredProtocolCount;

    @PostConstruct
    public void init() {
        if (isOperatorAtElectionEventLevel()) {
            // Get group level MvElection
            List<MvElection> mvElections = mvElectionService.findByPathAndChildLevel(userData, userData.getOperatorMvElection());
            if (mvElections.size() > 1) {
                throw new RuntimeException("MvElection list size expected to be 1");
            } else if (mvElections.size() == 1) {
                selectedElectionGroup = mvElections.get(0).getElectionGroup();
                editable = resolveEditable();
                setRequiredProtocolCount(municipalityService.getRequiredProtocolCountForElectionEvent(userData, selectedElectionGroup.getElectionEvent()
                        .getPk()));
                elements = electionVoteCountCategoryElements(userData, selectedElectionGroup);
                if (!isEditable()) {
                    MessageUtil.buildDetailMessage(getFacesContext(), "@election_vote_count_category.status_not_central", FacesMessage.SEVERITY_ERROR);
                }
            }
        }
    }

    public List<PageTitleMetaModel> getPageTitleMeta() {
        List<PageTitleMetaModel> result = new ArrayList<>();
        if (selectedElectionGroup != null) {
            result.add(new PageTitleMetaModel(messageProvider.get("@election_level[1].name"), selectedElectionGroup.getName()));
        }
        return result;
    }

    /**
     * Saves elements to database. Deletes existing elements for municipality and election group and inserts new ones. This makes it easier to get rid of
     * existing elements that are made no longer enabled by central admin user.
     */
    public void save() {
        if (isEditable()) {
            doSave();
        }
    }

    /**
     * @param userData      userData
     * @param electionGroup selected election group
     * @return list of ElectionVoteCountCategoryElement
     */
    List<ElectionVoteCountCategoryElement> electionVoteCountCategoryElements(UserData userData, ElectionGroup electionGroup) {
        if (electionGroup == null) {
            return new ArrayList<>();
        }

        List<ElectionVoteCountCategory> centralCats = electionVoteCountCategoryService.findElectionVoteCountCategories(userData, electionGroup,
                CountCategory.BF);

        List<VoteCountCategory> voteCountCategories = voteCountCategoryService.findAll(userData, CountCategory.BF);

        Collection<ElectionVoteCountCategory> electionVoteCountCategories = mergeExisting(centralCats, map(voteCountCategories));

        List<ElectionVoteCountCategoryElement> result = electionVoteCountCategories.stream().map(ElectionVoteCountCategoryElement::new)
                .collect(Collectors.toList());
        sortToKeepFuncTestsOk(result);
        return result;
    }

    // After upgrade to Java 8, default sort order in collections is changed.
    @SuppressWarnings("unchecked")
    private void sortToKeepFuncTestsOk(List<ElectionVoteCountCategoryElement> elements) {
        elements.sort(new Comparator<ElectionVoteCountCategoryElement>() {

            private Map<String, Integer> sortOrder = new HashMap();

            {
                sortOrder.put(CountCategory.FS.getId(), 1);
                sortOrder.put(CountCategory.VB.getId(), 2);
                sortOrder.put(CountCategory.VS.getId(), 3);
                sortOrder.put(CountCategory.FO.getId(), 4);
                sortOrder.put(CountCategory.VO.getId(), 5);
                sortOrder.put(CountCategory.VF.getId(), 6);
            }


            @Override
            public int compare(ElectionVoteCountCategoryElement e1, ElectionVoteCountCategoryElement e2) {
                if (order(e1) > order(e2)) {
                    return 1;
                }
                if (order(e1) < order(e2)) {
                    return -1;
                }
                return 0;
            }

            private Integer order(ElectionVoteCountCategoryElement e1) {
                return sortOrder.get(e1.getElectionVoteCountCategory().getVoteCountCategory().getId());
            }
        });
    }

    public boolean isRequiredProtocolCount() {
        return requiredProtocolCount;
    }

    public void setRequiredProtocolCount(boolean requiredProtocolCount) {
        this.requiredProtocolCount = requiredProtocolCount;
    }

    public List<ElectionVoteCountCategoryElement> getElements() {
        return elements;
    }

    public boolean isEditable() {
        return editable;
    }

    public ElectionGroup getSelectedElectionGroup() {
        return selectedElectionGroup;
    }

    private void doSave() {
        try {
            electionVoteCountCategoryService.update(userData, cats(elements));
            municipalityService.setRequiredProtocolCountForElectionEvent(userData, selectedElectionGroup.getElectionEvent().getPk(), isRequiredProtocolCount());
            MessageUtil.buildDetailMessage(getFacesContext(), MessageUtil.UPDATE_SUCCESSFUL_KEY, FacesMessage.SEVERITY_INFO);
        } catch (RuntimeException e) {
            String message = createErrorMessage(elements, e);
            LOGGER.error(message, e);
        }
        elements = electionVoteCountCategoryElements(userData, selectedElectionGroup);
    }

    private List<ElectionVoteCountCategory> cats(List<ElectionVoteCountCategoryElement> elements) {
        return elements.stream().map(ElectionVoteCountCategoryElement::getElectionVoteCountCategory).collect(Collectors.toList());
    }

    private String createErrorMessage(List<ElectionVoteCountCategoryElement> elements, RuntimeException e) {
        if (getFacesContext() != null) {
            process(e);
        }
        StringBuilder catIds = new StringBuilder();
        for (ElectionVoteCountCategoryElement element : elements) {
            catIds.append(element.getElectionVoteCountCategory().getVoteCountCategory().getId()).append(" ");
        }
        return "Error during replace (delete/create) of election vote count elements " + catIds + "- electionGroupPk = " + selectedElectionGroup.getPk();
    }

    private boolean resolveEditable() {
        return userDataController.isCentralConfigurationStatus()
                || userDataController.isOverrideAccess();
    }

    private Collection<ElectionVoteCountCategory> mergeExisting(List<ElectionVoteCountCategory> centralCats,
                                                                Map<String, ElectionVoteCountCategory> categoryMap) {
        for (ElectionVoteCountCategory electionVoteCountCategory : centralCats) {
            categoryMap.put(electionVoteCountCategory.key(), electionVoteCountCategory);
        }
        return categoryMap.values();
    }

    private Map<String, ElectionVoteCountCategory> map(List<VoteCountCategory> voteCountCategories) {
        Map<String, ElectionVoteCountCategory> map = new HashMap<>();
        for (VoteCountCategory voteCountCategory : voteCountCategories) {
            map.put(voteCountCategory.getId(), createElectionVoteCountCategory(voteCountCategory));
        }
        return map;
    }

    private ElectionVoteCountCategory createElectionVoteCountCategory(VoteCountCategory voteCountCategory) {
        ElectionVoteCountCategory electionVoteCountCategory = new ElectionVoteCountCategory();
        electionVoteCountCategory.setVoteCountCategory(voteCountCategory);
        electionVoteCountCategory.setElectionGroup(selectedElectionGroup);
        if (CountCategory.fromId(voteCountCategory.getId()) == CountCategory.VF) {
            electionVoteCountCategory.setSpecialCover(true);
        }
        return electionVoteCountCategory;
    }

    private boolean isOperatorAtElectionEventLevel() {
        return userData.getOperatorMvElection().getElectionLevel() == ElectionLevelEnum.ELECTION_EVENT.getLevel();
    }
}
