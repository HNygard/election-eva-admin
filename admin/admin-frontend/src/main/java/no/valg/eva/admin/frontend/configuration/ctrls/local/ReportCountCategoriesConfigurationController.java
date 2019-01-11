package no.valg.eva.admin.frontend.configuration.ctrls.local;

import no.valg.eva.admin.frontend.util.MessageUtil;
import no.valg.eva.admin.common.configuration.model.local.ReportCountCategory;
import no.valg.eva.admin.common.configuration.service.ReportCountCategoryService;
import no.valg.eva.admin.common.counting.comparators.ContestInfoOrderComparator;
import no.valg.eva.admin.common.counting.constants.CountingMode;
import no.valg.eva.admin.common.counting.model.ContestInfo;
import no.valg.eva.admin.common.counting.service.ContestInfoService;
import no.valg.eva.admin.frontend.configuration.ConfigurationView;

import javax.faces.application.FacesMessage;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Named
@ViewScoped
public class ReportCountCategoriesConfigurationController extends ConfigurationController {

    // Injected
    private ReportCountCategoryService reportCountCategoryService;
    private ContestInfoService contestInfoService;

    private List<ReportCountCategory> reportCountCategories;
    private List<ReportCountCategory> currentReportCountCategories;
    private List<ReportCountCategory> boroughReportCountCategories;
    private List<ContestInfo> contestInfoList;

    public ReportCountCategoriesConfigurationController() {
        // CDI
    }

    @Inject
    public ReportCountCategoriesConfigurationController(ReportCountCategoryService reportCountCategoryService, ContestInfoService contestInfoService) {
        this.reportCountCategoryService = reportCountCategoryService;
        this.contestInfoService = contestInfoService;
    }

    @Override
    public void init() {
        reportCountCategories = reportCountCategoryService.findCountCategoriesByArea(getUserData(), getAreaPath(),
                getMainController().getElectionGroup().getElectionGroupPath());
        currentReportCountCategories = reportCountCategories;
        if (isHasBoroughs()) {
            boroughReportCountCategories = reportCountCategoryService.findBoroughCountCategoriesByArea(getUserData(), getAreaPath());
            if (boroughReportCountCategories.isEmpty()) {
                boroughReportCountCategories = null;
                contestInfoList = null;
            } else {
                contestInfoList = contestInfoService.contestOrElectionByAreaPath(getAreaPath())
                        .stream()
                        .sorted(new ContestInfoOrderComparator())
                        .collect(Collectors.toList());
                contestInfoList = checkContestInfoList(contestInfoList);
            }
        }
    }

    @Override
    Class<? extends ConfigurationController>[] getRequiresDoneBeforeEdit() {
        if (getMainController().getElectionGroup().isElectronicMarkoffs()) {
            return new Class[]{ElectronicMarkoffsConfigurationController.class};
        }
        return new Class[0];
    }

    @Override
    public ConfigurationView getView() {
        return ConfigurationView.COUNT;
    }

    @Override
    public String getName() {
        return "@config.local.accordion.report_count_categories.name";
    }

    @Override
    boolean hasAccess() {
        return isMunicipalityLevel();
    }

    @Override
    public boolean isEditable() {
        return super.isEditable() && !isBoroughDataSelected();
    }

    @Override
    public boolean isDoneStatus() {
        return isMunicipalityLevel() && getMunicipalityConfigStatus().isCountCategories();
    }

    @Override
    void setDoneStatus(boolean value) {
        if (isMunicipalityLevel()) {
            getMunicipalityConfigStatus().setCountCategories(value);
        }
    }

    @Override
    boolean canBeSetToDone() {
        return true;
    }

    @Override
    public void saveDone() {
        saveCount();
    }

    public void selectBoroughData() {
        currentReportCountCategories = boroughReportCountCategories;
    }

    public void unselectBoroughData() {
        currentReportCountCategories = reportCountCategories;
    }

    public boolean hasBoroughData() {
        return boroughReportCountCategories != null;
    }

    public boolean isBoroughDataSelected() {
        return hasBoroughData() && currentReportCountCategories == boroughReportCountCategories;
    }

    public List<ReportCountCategory> getCurrentReportCountCategories() {
        return currentReportCountCategories;
    }

    public List<ContestInfo> getContestInfoList() {
        return contestInfoList;
    }

    boolean isValgtingsstemmerSentraltSamlet() {
        if (isDoneStatus()) {
            if (reportCountCategories == null && !execute(this::init)) {
                return false;

            }
            for (ReportCountCategory category : reportCountCategories) {
                if (category.getCategory().equals(no.valg.eva.admin.common.counting.model.CountCategory.VO)) {
                    return category.getCountingMode() == CountingMode.CENTRAL;
                }
            }
        }
        return false;
    }

    void saveCount() {
        if (isEditable() && super.saveDone(true)) {
            execute(() -> {
                reportCountCategories = reportCountCategoryService.updateCountCategories(getUserData(), getAreaPath(),
                        getMainController().getElectionGroup().getElectionGroupPath(), reportCountCategories);
                currentReportCountCategories = reportCountCategories;
                for (ReportCountCategory category : reportCountCategories) {
                    String[] summaryParams = {
                            getMessageProvider().get(category.getCategory().messageProperty()),
                            getMessageProvider().get(category.getCountingMode().getDescription())
                    };
                    MessageUtil.buildDetailMessage("@report_count_category.voting_count_category.update_message",
                            summaryParams, FacesMessage.SEVERITY_INFO);
                }
            });
        }
    }

    private List<ContestInfo> checkContestInfoList(List<ContestInfo> contestInfoList) {
        if (contestInfoList.size() != 2) {
            boroughReportCountCategories = null;
            return Collections.emptyList();
        }
        return contestInfoList;
    }
}
