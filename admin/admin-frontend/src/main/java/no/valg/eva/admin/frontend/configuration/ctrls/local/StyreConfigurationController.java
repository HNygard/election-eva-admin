package no.valg.eva.admin.frontend.configuration.ctrls.local;

import no.valg.eva.admin.frontend.util.MessageUtil;
import no.evote.validation.FoedselsNummerValidator;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.configuration.model.ElectoralRollSearch;
import no.valg.eva.admin.common.configuration.model.local.DisplayOrder;
import no.valg.eva.admin.common.configuration.model.local.ResponsibleOfficer;
import no.valg.eva.admin.common.configuration.service.ReportingUnitService;
import no.valg.eva.admin.common.counting.constants.ReportingUnitTypeId;
import no.valg.eva.admin.common.counting.constants.ResponsibilityId;
import no.valg.eva.admin.frontend.common.Button;
import no.valg.eva.admin.frontend.common.ButtonType;
import no.valg.eva.admin.frontend.common.DeleteAction;
import no.valg.eva.admin.frontend.configuration.ConfigurationMode;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.event.ReorderEvent;
import org.primefaces.event.SelectEvent;

import javax.faces.application.FacesMessage;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import static javax.faces.application.FacesMessage.SEVERITY_ERROR;
import static javax.faces.application.FacesMessage.SEVERITY_INFO;
import static no.valg.eva.admin.common.counting.constants.ResponsibilityId.isUniqueResponsibility;
import static no.valg.eva.admin.frontend.common.Button.enabled;
import static no.valg.eva.admin.frontend.configuration.ConfigurationMode.READ;
import static no.valg.eva.admin.frontend.configuration.ConfigurationMode.UPDATE;

public abstract class StyreConfigurationController extends ConfigurationController implements DeleteAction {

    @Inject
    private ReportingUnitService reportingUnitService;

    private List<ResponsibleOfficer> responsibleOfficers = new ArrayList<>();
    private ResponsibleOfficer selectedResponsibleOfficer;
    private ElectoralRollSearch electoralRollSearch;
    private List<ResponsibleOfficer> searchResult;
    private Boolean hasReportingUnitTypeConfigured;

    @Override
    public void init() {
        setMode(ConfigurationMode.READ);
        selectedResponsibleOfficer = null;
        electoralRollSearch = null;
        searchResult = null;
        loadResponsibleOfficers();
    }

    @Override
    public boolean isEditable() {
        return true;
    }

    @Override
    boolean hasAccess() {
        return hasReportingUnitTypeConfigured();
    }

    private boolean hasReportingUnitTypeConfigured() {
        if (hasReportingUnitTypeConfigured == null) {
            hasReportingUnitTypeConfigured = reportingUnitService.hasReportingUnitTypeConfigured(getUserData(), getReportingUnitTypeId());
        }
        return hasReportingUnitTypeConfigured;
    }

    @Override
    boolean canBeSetToDone() {
        if (responsibleOfficers == null || responsibleOfficers.isEmpty()) {
            return false;
        }
        for (ResponsibleOfficer officer : responsibleOfficers) {
            if (!officer.isValid()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isRequiresDone() {
        return false;
    }

    @Override
    public Button button(ButtonType type) {
        switch (type) {
            case CREATE:
            case DELETE:
            case EXECUTE_DELETE:
                return enabled(isReadMode() && isEditable());
            case SAVE:
                return enabled(isWriteMode() && isEditable());
            default:
                return super.button(type);
        }
    }

    @Override
    public void confirmDelete() {
        execute(() -> {
            unlock();
            reportingUnitService.delete(getUserData(), selectedResponsibleOfficer);
            MessageUtil.buildDeletedMessage(selectedResponsibleOfficer);
        });
        loadResponsibleOfficers();
    }

    public void onCreateBoardMemberClick() {
        setSelectedResponsibleOfficer(null);
        searchResult = null;
        electoralRollSearch = new ElectoralRollSearch();
        setMode(ConfigurationMode.SEARCH);
    }

    public void searchResponsibleOfficer() {
        if (!electoralRollSearch.isValid()) {
            return;
        }

        if (electoralRollSearch.hasValidSsn()) {
            electoralRollSearch.setName(null);
            electoralRollSearch.setBirthDate(null);
        } else {
            electoralRollSearch.setSsn(null);
        }
        searchResult = reportingUnitService.search(getUserData(), getAreaPath(), getElectoralRollSearch());
        if (searchResult.size() == 1) {
            setSelectedResponsibleOfficer(searchResult.get(0));
            setMode(ConfigurationMode.CREATE);
        } else if (searchResult.isEmpty()) {
            setSelectedResponsibleOfficer(prepareSelectedOnEmptySearchResult());
            setMode(ConfigurationMode.CREATE);
            MessageUtil.buildDetailMessage("@config.local.electoral_roll_search.emptyResult", FacesMessage.SEVERITY_INFO);
        }
    }

    private ResponsibleOfficer prepareSelectedOnEmptySearchResult() {
        ResponsibleOfficer selected = new ResponsibleOfficer();
        selected.setSkalValideres(true);
        if (StringUtils.isEmpty(electoralRollSearch.getName())) {
            return selected;
        }
        StringTokenizer tokens = new StringTokenizer(electoralRollSearch.getName(), " ");
        String token = tokens.nextToken();
        if (!tokens.hasMoreTokens()) {
            selected.setLastName(token);
            return selected;
        }
        selected.setFirstName(token);
        token = tokens.nextToken();
        if (!tokens.hasMoreTokens()) {
            selected.setLastName(token);
            return selected;
        }
        selected.setMiddleName(token);
        while (tokens.hasMoreTokens()) {
            token = tokens.nextToken();
            if (tokens.hasMoreTokens()) {
                selected.setMiddleName((selected.getMiddleName() + " " + token).trim());
            } else {
                selected.setLastName(token);
            }
        }
        return selected;
    }

    public void cancelSearchResponsibleOfficer() {
        setSelectedResponsibleOfficer(null);
        searchResult = null;
        setMode(ConfigurationMode.READ);
    }

    public void onRowReorder(ReorderEvent event) {
        // At this point, the model is already updated. Renumber based on list index.
        List<DisplayOrder> displayOrders = new ArrayList<>();
        int index = 1;
        for (ResponsibleOfficer ro : getResponsibleOfficers()) {
            ro.setDisplayOrder(index++);
            displayOrders.add(ro.displayOrder());
        }
        execute(() -> {
            unlock();
            responsibleOfficers = reportingUnitService.saveResponsibleOfficerDisplayOrder(getUserData(), getStyreAreaPath(), displayOrders);
            String officer = MessageUtil.getDisplay(getMessageProvider(), responsibleOfficers.get(event.getToIndex()), false);
            MessageUtil.buildDetailMessage(officer + " flyttet fra nr. " + (event.getFromIndex() + 1) + " til nr. " + (event.getToIndex() + 1), SEVERITY_INFO);
        });
    }

    public void saveResponsibleOfficer() {
        selectedResponsibleOfficer.setAreaPath(getStyreAreaPath());
        if (reportingUnitService.validate(getUserData(), selectedResponsibleOfficer)) {
            if (execute(() -> {
                unlock();
                ResponsibleOfficer persisted = reportingUnitService.save(getUserData(), selectedResponsibleOfficer);
                setSelectedResponsibleOfficer(persisted);
                MessageUtil.buildSavedMessage(selectedResponsibleOfficer);
            })) {
                loadResponsibleOfficers();
                setMode(READ);
            }
        } else {
            MessageUtil.buildDetailMessage("@config.local.responsible.officer.ugyldige.data", SEVERITY_ERROR);
        }
    }

    public void cancelSaveResponsibleOfficer() {
        setMode(ConfigurationMode.READ);
        setSelectedResponsibleOfficer(null);
        if (searchResult != null && searchResult.size() == 1) {
            searchResult = null;
        }
    }

    public void onResponsibleOfficerSelected(SelectEvent event) {
        setSelectedResponsibleOfficer((ResponsibleOfficer) event.getObject());
        setMode(ConfigurationMode.CREATE);
    }

    String getAddHeaderBorn() {
        if (getSelectedResponsibleOfficer() != null) {
            return FoedselsNummerValidator.asFormattedLocalDate(getSelectedResponsibleOfficer().getId(), getUserData().getJavaLocale());
        }
        return null;
    }

    String getAddHeaderAddress() {
        if (getSelectedResponsibleOfficer() != null) {
            StringBuilder sb = new StringBuilder();
            ResponsibleOfficer ro = getSelectedResponsibleOfficer();
            sb.append(ro.getAddress());
            sb.append(", ").append(ro.getPostalCode());
            sb.append(" ").append(ro.getPostalTown());
            return sb.toString();
        }
        return null;
    }

    void loadResponsibleOfficers() {
        if (getStyreAreaPath() != null) {
            responsibleOfficers = reportingUnitService.findByArea(getUserData(), getStyreAreaPath());
        }
    }

    public List<ResponsibilityId> getResponsibilities() {
        return getSelectedResponsibleOfficer() != null
                && getSelectedResponsibleOfficer().isPersisted() ? getResponsibilitiesForUpdateUser() : getResponsibilitiesForCreateUser();
    }

    private List<ResponsibilityId> getResponsibilitiesForCreateUser() {
        List<ResponsibilityId> responsibilityIds = ResponsibilityId.list();

        //Filtering away unique responsibilities used by other responsible officers
        for (ResponsibleOfficer responsibleOfficer : getResponsibleOfficers()) {
            if (isUniqueResponsibility(responsibleOfficer.getResponsibilityId())) {
                responsibilityIds.remove(responsibleOfficer.getResponsibilityId());
            }
        }

        return responsibilityIds;
    }

    List<ResponsibilityId> getResponsibilitiesForUpdateUser() {
        List<ResponsibilityId> responsibilityIds = ResponsibilityId.list();

        //Filtering away unique responsibilities except it belongs to selected responsible officer
        for (ResponsibleOfficer responsibleOfficer : getResponsibleOfficers()) {
            if (!isSameAsSelectedOfficer(responsibleOfficer)
                    && isUniqueResponsibility(responsibleOfficer.getResponsibilityId())) {
                responsibilityIds.remove(responsibleOfficer.getResponsibilityId());
            }
        }

        return responsibilityIds;
    }

    private boolean isSameAsSelectedOfficer(ResponsibleOfficer responsibleOfficer) {
        return responsibleOfficer.equals(getSelectedResponsibleOfficer());
    }

    public boolean isSearchMode() {
        return ConfigurationMode.SEARCH == getMode();
    }

    public boolean isCreateFromElectoralRollMode() {
        return isValidCreateMode() && getSelectedResponsibleOfficer() != null && getSelectedResponsibleOfficer().getId() != null;
    }

    public boolean isCreateMode() {
        return isValidCreateMode() && !isCreateFromElectoralRollMode();
    }

    public boolean isCreateOrUpdateMode() {
        return isCreateMode() || isUpdateMode();
    }

    private boolean isValidCreateMode() {
        return getMode() == ConfigurationMode.CREATE && getSelectedResponsibleOfficer() != null;
    }

    AreaPath getStyreAreaPath() {
        return getAreaPath();
    }

    public ElectoralRollSearch getElectoralRollSearch() {
        return electoralRollSearch;
    }

    public List<ResponsibleOfficer> getSearchResult() {
        return searchResult;
    }

    public List<ResponsibleOfficer> getResponsibleOfficers() {
        return responsibleOfficers;
    }

    public ResponsibleOfficer getSelectedResponsibleOfficer() {
        return selectedResponsibleOfficer;
    }

    public void setSelectedResponsibleOfficer(ResponsibleOfficer selectedResponsibleOfficer) {
        this.selectedResponsibleOfficer = selectedResponsibleOfficer;
    }

    abstract ReportingUnitTypeId getReportingUnitTypeId();

    public void onEditBoardMemberClick(ResponsibleOfficer responsibleOfficer) {
        setSelectedResponsibleOfficer(responsibleOfficer);
        searchResult = null;
        setMode(UPDATE);
    }

    public boolean isEditUser() {
        return isCreateFromElectoralRollMode() || isSearchMode() || isUpdateMode() || isCreateMode();
    }

    public boolean isUpdateMode() {
        return getMode() != null && getMode() == UPDATE;
    }
}
