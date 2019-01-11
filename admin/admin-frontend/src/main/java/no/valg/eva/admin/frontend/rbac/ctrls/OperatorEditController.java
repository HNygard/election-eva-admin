package no.valg.eva.admin.frontend.rbac.ctrls;

import no.evote.constants.AreaLevelEnum;
import no.evote.constants.ElectionLevelEnum;
import no.valg.eva.admin.frontend.util.FacesUtil;
import no.valg.eva.admin.frontend.util.MessageUtil;
import no.evote.security.UserData;
import no.evote.service.configuration.MvAreaService;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.PollingPlaceArea;
import no.valg.eva.admin.common.configuration.model.election.Contest;
import no.valg.eva.admin.common.rbac.Operator;
import no.valg.eva.admin.common.rbac.RoleAssociation;
import no.valg.eva.admin.common.rbac.RoleItem;
import no.valg.eva.admin.common.rbac.service.AdminOperatorService;
import no.valg.eva.admin.configuration.application.ResponsibilityValidationService;
import no.valg.eva.admin.configuration.domain.model.ResponsibilityConflict;
import no.valg.eva.admin.frontend.BaseController;
import no.valg.eva.admin.frontend.common.RoleConflictHandler;
import no.valg.eva.admin.frontend.i18n.MessageProvider;
import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;

import javax.faces.application.FacesMessage;
import javax.faces.model.SelectItem;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static no.evote.constants.ElectionLevelEnum.CONTEST;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

@Named
@ViewScoped
public class OperatorEditController extends BaseController implements RoleConflictHandler {

    private static final Logger LOGGER = Logger.getLogger(OperatorEditController.class);

    @Inject
    private OperatorAdminController adminController;
    @Inject
    private OperatorListController listController;
    @Inject
    private OperatorCreatedController createdController;
    @Inject
    private MessageProvider messageProvider;
    @Inject
    private AdminOperatorService adminOperatorService;
    @Inject
    private RoleOptions roleOptions;
    @Inject
    private ElectionOptions electionOptions;
    @Inject
    private AreaOptions areaOptions;
    @Inject
    private MvAreaService mvAreaService;
    @Inject
    private UserData userData;
    @Inject
    private ResponsibilityValidationService responsibilityValidationService;

    private Operator operator;
    private RbacView fromView;
    private AreaPath areaPath;
    private List<RoleAssociation> editRoleAssociations = new ArrayList<>();
    private List<RoleAssociation> existingRoleAssociations = new ArrayList<>();
    private String selectedRoleToAdd;
    private Map<String, String> selectedRoleAreaToAdd;
    private List<ResponsibilityConflict> roleConflicts;
    private RootLevelEdit rootLevelEdit;

    public void init(Operator operator, RbacView view) {
        this.operator = operator;
        this.fromView = adminController.getView();
        this.areaPath = userData.getOperatorAreaPath();
        this.editRoleAssociations = new ArrayList<>(operator.getRoleAssociations());
        this.existingRoleAssociations = new ArrayList<>(operator.getRoleAssociations());
        setSelectedRoleAreaToAdd(new HashMap<>());
        adminController.setView(view);
        roleOptions.init(areaPath);
        if (isRootLevel()) {
            rootLevelEdit = new RootLevelEdit(this);
        } else {
            areaOptions.init(areaPath);
        }
    }

    public String getPageHeader() {
        if (isNewMode()) {
            return messageProvider.get("@rbac.add.operator");
        } else {
            return messageProvider.get("@rbac.edit.operator");
        }
    }

    public boolean isNewCandidate() {
        return adminController.getView() == RbacView.NEW;
    }

    public boolean erRedigeringAvEgenBruker() {
        String idUserData = getUserData().getOperator().getId();
        String idBrukerSomRedigeres = operator.getPersonId().getId();
        return idUserData.equals(idBrukerSomRedigeres);
    }

    public void deleteSelectedRoleAssociation(RoleAssociation roleAssociationToBeDeleted) {
        editRoleAssociations.remove(roleAssociationToBeDeleted);
    }

    public boolean isNewMode() {
        return adminController.getView().isNewMode();
    }

    public void createOperator() {
        execute(() -> {
            Operator updatedOperator = adminOperatorService.updateOperator(userData, operator, areaPath, editRoleAssociations,
                    Collections.emptyList());
            listController.updated(updatedOperator);
            createdController.init(updatedOperator);
            resetState();
        });

    }

    public void cancelEdit() {
        if (fromView == RbacView.CREATED) {
            fromView = RbacView.LIST;
        }
        adminController.setView(fromView);
        resetState();
    }

    public void roleChanged() {
        if (isRootLevel()) {
            rootLevelEdit.resetCounty();
        }
        electionOptions.init(roleOptions.getRoleMap().get(getSelectedRoleToAdd()));
    }

    public boolean isEditMode() {
        return adminController.getView() == RbacView.EDIT;
    }

    public void saveOperator() {
        execute(() -> {
            Collection<RoleAssociation> addedRoleAssociations = CollectionUtils.subtract(editRoleAssociations, existingRoleAssociations);
            Collection<RoleAssociation> deletedRoleAssociations = CollectionUtils.subtract(existingRoleAssociations, editRoleAssociations);
            Operator updatedOperator = adminOperatorService
                    .updateOperator(userData, operator, areaPath, addedRoleAssociations, deletedRoleAssociations);
            listController.updated(updatedOperator);
            adminController.setView(RbacView.LIST);
            MessageUtil.buildDetailMessage(getFacesContext(), operator.getName() + " " + messageProvider.get("@operation.updated").toLowerCase(),
                    FacesMessage.SEVERITY_INFO);
            resetState();
        });
    }

    public boolean isRenderElectionLevel(String roleId) {
        if (!roleId.equals(getSelectedRoleToAdd())) {
            return false;
        }
        return electionOptions.isRender();
    }

    public ElectionLevelEnum getElectionLevel(String roleId) {
        if (!roleId.equals(getSelectedRoleToAdd())) {
            return null;
        }
        return roleOptions.getRoleMap().get(roleId).getElectionLevel();
    }

    public boolean isRenderAreas(String roleId) {
        if (!electionOptions.isReady()) {
            return false;
        }
        return isRenderSelectArea(roleId) || (isRootLevel() && (rootLevelEdit.isRenderCountyList(roleId) || rootLevelEdit.isRenderMunicipalityList(roleId)));
    }

    public boolean isRenderSelectArea(String roleId) {
        if (!roleId.equals(getSelectedRoleToAdd())) {
            return false;
        }
        if (isRootLevel()) {
            return rootLevelEdit.isReady() && !isMaxLevelForSelectRoleRoot(getSelectedRoleToAdd());
        }
        return true;
    }

    public boolean isAddSelectedRoleButtonDisabled() {
        if (isRootLevel()) {
            return !rootLevelEdit.isReady();
        }
        return !areaOptions.hasAreasForRole(getSelectedRoleToAdd());
    }

    public boolean isRenderNoAreasAvailableText() {
        return !areaOptions.hasAreasForRole(getSelectedRoleToAdd());
    }

    public void validateAndAddRoleSelection() {

        final String selectedRoleId = getSelectedRoleToAdd();
        final String selectedAreaPath = getSelectedRoleAreaToAdd().get(selectedRoleId);
        
        if (selectedAreaPath == null) {
            addRoleAndDismissRoleWidget();
            return;
        }
        
        roleConflicts = responsibilityValidationService.checkIfRoleHasCandidateConflict(
                getUserData(),
                getOperator().getPersonId(),
                AreaPath.from(selectedAreaPath),
                selectedRoleId);

        if (roleConflicts.isEmpty()) {
            addRoleAndDismissRoleWidget();
        } else {
            showConflictDialog();
        }
    }

    private void addRoleAndDismissRoleWidget() {
        FacesUtil.executeJS("PF('createRoleAndLocationWidget').hide()");
        FacesUtil.updateDom(asList("editOperatorForm:roleList", "editOperatorForm:editButtons", "editOperatorForm:newButtons", "msg"));
        addSelectedRole();
    }

    private void showConflictDialog() {
        FacesUtil.updateDom("editOperatorForm:conflictWidget:roleConflictDialog");
        FacesUtil.executeJS("PF('roleConflictWidget').show()");
    }

    public void addSelectedRole() {
        String roleToAdd = getSelectedRoleToAdd();
        RoleItem roleItem = roleOptions.getRoleMap().get(roleToAdd);
        PollingPlaceArea area;
        if (isRootLevel() && isMaxLevelForSelectRoleRoot(roleToAdd)) {
            area = userData.getOperatorMvArea().toViewObject();
        } else {
            area = areaOptions.getAreaMap().get(getSelectedRoleAreaToAdd().get(roleToAdd));
        }
        RoleAssociation roleAssociation = new RoleAssociation(roleItem, area);
        if (electionOptions.isRender() && electionOptions.isReady()) {
            roleAssociation.setElectionPath(electionOptions.getElectionPath());
            if (CONTEST.equals(electionOptions.getElectionPath().getLevel())) {
                roleAssociation.setContest(new Contest(null, null, electionOptions.getSelectedContest().getName(), null, null, null, null, -1));
            }
        }
        if (!editRoleAssociations.contains(roleAssociation)) {
            editRoleAssociations.add(roleAssociation);
        } else {
            MessageUtil.buildFacesMessage(getFacesContext(), null, messageProvider.get("@rbac.import_operators.has_role"), null, FacesMessage.SEVERITY_INFO);
        }
        setSelectedRoleToAdd(null);
    }

    public void deleteOperator() {
        try {
            String name = operator.getName();
            adminOperatorService.deleteOperator(userData, operator);
            listController.removed(operator);
            adminController.setView(RbacView.LIST);
            MessageUtil.buildDetailMessage(getFacesContext(), name + " " + messageProvider.get("@operation.deleted").toLowerCase(),
                    FacesMessage.SEVERITY_INFO);
            resetState();
        } catch (Exception e) {
            LOGGER.warn("deleteOperator() failed", e);
            MessageUtil.buildFacesMessage(getFacesContext(), null, messageProvider.get("@common.message.delete.unsuccessful"), null,
                    FacesMessage.SEVERITY_ERROR);
        }
    }

    public Operator getOperator() {
        return operator;
    }

    public List<RoleAssociation> getEditRoleAssociations() {
        return editRoleAssociations;
    }

    public String getSelectedRoleToAdd() {
        return selectedRoleToAdd;
    }

    boolean isMaxLevelForSelectRoleRoot(String roleId) {
        return getMaxLevelForSelectRole(roleId) == AreaLevelEnum.ROOT;
    }

    AreaLevelEnum getMaxLevelForSelectRole(String roleId) {
        AreaLevelEnum result = AreaLevelEnum.NONE;
        if (roleId == null || roleId.trim().length() == 0) {
            return result;
        }
        result = AreaLevelEnum.ROOT;
        List<AreaLevelEnum> levels = roleOptions.getRoleMap().get(roleId).getPermittedAreaLevels();
        for (AreaLevelEnum areaLevelEnum : levels) {
            if (areaLevelEnum.getLevel() > result.getLevel()) {
                result = areaLevelEnum;
            }
        }
        return result;
    }

    public void setSelectedRoleToAdd(String selectedRoleToAdd) {
        this.selectedRoleToAdd = selectedRoleToAdd;
    }

    public List<SelectItem> getRoleNameOptions() {
        return roleOptions.getRoleNameOptions();
    }

    public Map<String, String> getSelectedRoleAreaToAdd() {
        return selectedRoleAreaToAdd;
    }

    public void setSelectedRoleAreaToAdd(Map<String, String> selectedRoleAreaToAdd) {
        this.selectedRoleAreaToAdd = selectedRoleAreaToAdd;
    }

    public Map<String, List<PollingPlaceArea>> getAvailableAreasForRole() {
        return areaOptions.getAvailableAreasForRole();
    }

    public RootLevelEdit getRootLevelEdit() {
        return rootLevelEdit;
    }

    public boolean isRootLevel() {
        return userData.isElectionEventAdminUser();
    }

    public ElectionOptions getElectionOptions() {
        return electionOptions;
    }

    public AreaOptions getAreaOptions() {
        return areaOptions;
    }

    public MvAreaService getMvAreaService() {
        return mvAreaService;
    }

    public UserData getUserData() {
        return userData;
    }

    private void resetState() {
        operator = null;
        fromView = null;
        areaPath = null;
        editRoleAssociations = null;
        existingRoleAssociations = null;
        selectedRoleToAdd = null;
        selectedRoleAreaToAdd = null;
    }


    @Override
    public MessageProvider getMessageProvider() {
        return messageProvider;
    }

    @Override
    public void onAcceptRoleConflict() {
        addRoleAndDismissRoleWidget();
    }

    @Override
    public String getLocalizedRoleConflictMessage() {
        final String selectedRole = getSelectedRoleToAdd();
        if (isNotEmpty(selectedRole)) {
            return getMessageProvider().get("@rbac.role.conflictMessage", getLocalizedRoleName(selectedRole));
        }
        return null;
    }

    @Override
    public String getLocalizedRoleConflictExplanation() {
        return getMessageProvider().get("@rbac.role.conflictExplanation");
    }

    private String getLocalizedRoleName(String roleId) {
        String localizedRoleName = null;
        if (isNotEmpty(roleId)) {
            final RoleItem roleItem = roleOptions.getRoleMap().get(roleId);
            localizedRoleName = "'" + getMessageProvider().get(roleItem.getRoleName()) + "'";
        }
        return localizedRoleName;
    }

    @Override
    public List<ResponsibilityConflict> getResponsibilityConflicts() {
        return roleConflicts;
    }
}
