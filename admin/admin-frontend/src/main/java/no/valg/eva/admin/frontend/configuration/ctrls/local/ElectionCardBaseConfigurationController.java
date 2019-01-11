package no.valg.eva.admin.frontend.configuration.ctrls.local;

import no.valg.eva.admin.application.MapService;
import no.valg.eva.admin.application.map.Address;
import no.valg.eva.admin.application.map.AddressSearch;
import no.valg.eva.admin.common.configuration.model.local.ElectionCardConfig;
import no.valg.eva.admin.common.configuration.service.ElectionCardConfigService;
import no.valg.eva.admin.frontend.common.Button;
import no.valg.eva.admin.frontend.common.ButtonType;
import no.valg.eva.admin.frontend.common.dialog.Dialog;
import no.valg.eva.admin.frontend.common.dialog.Dialogs;
import no.valg.eva.admin.frontend.configuration.ConfigurationMode;
import no.valg.eva.admin.frontend.configuration.models.ElectionCardModel;
import no.valg.eva.admin.frontend.configuration.widgets.ElectionCard;
import no.valg.eva.admin.frontend.util.FacesUtil;
import no.valg.eva.admin.frontend.util.MessageUtil;
import org.primefaces.event.NodeSelectEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

import javax.faces.application.FacesMessage;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static java.util.Arrays.asList;
import static no.valg.eva.admin.frontend.common.Button.enabled;
import static no.valg.eva.admin.frontend.common.Button.notRendered;
import static no.valg.eva.admin.frontend.configuration.ConfigurationMode.READ;
import static no.valg.eva.admin.frontend.configuration.ConfigurationMode.UPDATE;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

public abstract class ElectionCardBaseConfigurationController extends ConfigurationController implements ElectionCard {

    @Inject
    private MapService mapService;

    @Inject
    private ElectionCardConfigService electionCardService;

    private ElectionCardConfig electionCard;

    private final Pattern electionCardInfoTextPattern = Pattern.compile(ElectionCardConfig.REGEX_INFO_TEXT);

    private TreeNode rootTreeNode;
    private TreeNode selectedTreeNode;
    private boolean overwriteCustomInfoText;
    private Boolean voterNumbersGenerated;

    abstract void buildTree();

    abstract String getWidgetId();

    @Override
    public void init() {
        if (getVoterNumbersGenerated()) {
            String message = getMessageProvider().get("@config.local.manntallsnummerErGenerert",
                    getUserDataController().getElectionEvent().getName());
            MessageUtil.buildDetailMessage(message, FacesMessage.SEVERITY_ERROR);
        }
        setMode(READ);
        electionCard = electionCardService.findElectionCardByArea(getUserData(), getAreaPath());
        buildTree();
    }

    @Override
    public boolean isEditable() {
        if (getVoterNumbersGenerated()) {
            return false;
        }
        if (isOpptellingsvalgstyret()) {
            return true;
        }
        return super.isEditable();
    }

    @Override
    public String getName() {
        return "@config.local.accordion.election_card.name";
    }

    @Override
    Class<? extends ConfigurationController>[] getRequiresDoneBeforeDone() {
        if (getVoterNumbersGenerated()) {
            return new Class[0];
        }
        return new Class[]{ElectionDayPollingPlacesConfigurationController.class};
    }

    @Override
    void setDoneStatus(boolean value) {
        if (isMunicipalityLevel()) {
            getMunicipalityConfigStatus().setElectionCard(value);
        }
    }

    @Override
    public boolean isDoneStatus() {
        return isMunicipalityLevel() && getMunicipalityConfigStatus().isElectionCard();
    }

    @Override
    boolean canBeSetToDone() {
        if (getVoterNumbersGenerated()) {
            return true;
        }
        if (rootTreeNode == null) {
            return false;
        }
        return !isDirty() && isValid();
    }

    @Override
    public boolean isAddressEditable() {
        return getSelected() != null && getSelected().isRoot() && isWriteMode();
    }

    @Override
    public boolean isInfoTextEditable() {
        return isWriteMode();
    }

    @Override
    public boolean isRenderInfoText() {
        return !getUserData().isOpptellingsvalgstyret();
    }

    @Override
    public Dialog getEditAddressDialog() {
        return Dialogs.ELECTION_CARD_EDIT_ADDRESS;
    }

    @Override
    public Dialog getConfirmElectionCardInfoTextOverwriteDialog() {
        return Dialogs.CONFIRM_ELECTION_CARD_INFO_TEXT_OVERWRITE;
    }

    @Override
    public ElectionCardModel getSelected() {
        return getElectionCardModel(getSelectedTreeNode());
    }

    @Override
    public Button button(ButtonType type) {
        switch (type) {
            case UPDATE:
                if (ConfigurationMode.READ.equals(getMode())) {
                    return enabled(isEditable());
                }
                return notRendered();
            case EXECUTE_UPDATE:
            case CANCEL:
                if (isWriteMode()) {
                    return enabled(isEditable());
                }
                return notRendered();
            case DONE:
                if (getVoterNumbersGenerated()) {
                    return enabled(true);
                }
                return super.button(type);
            default:
                return super.button(type);
        }
    }

    public void saveAddress() {
        getEditAddressDialog().closeAndUpdate(getAreaTableContainerId());
    }

    private String getAreaTableContainerId() {
        return getFormId() + ":electionCardWidget:areaTableContainer";
    }

    public void cancelSaveAddress() {
        getEditAddressDialog().closeAndUpdate(getFormId());
    }

    @Override
    public void saveDone() {
        if (getVoterNumbersGenerated()) {
            super.saveDone();
        } else {
            saveChanges(true);
        }
    }

    public void prepareUpdate() {
        if (isDoneStatus()) {
            unlockAndUpdateDOM();
        } else {
            updateForm();
        }
        setMode(UPDATE);
        setOverwriteCustomInfoText(false);
    }

    public void saveChanges() {
        if (electionTextIsInvalid()) {
            MessageUtil.buildDetailMessage(FacesMessage.SEVERITY_ERROR, getMessageProvider().get("@config.local.election_card.infoText_invalid"));
        } else if (electionAddressInvalid()) {
            MessageUtil.buildDetailMessage(FacesMessage.SEVERITY_ERROR, getMessageProvider().get("@config.local.election_card.address_invalid"));
        } else if (isRootNodeAndChildrenHasCustomElectionText()) {
            FacesUtil.executeJS(getConfirmElectionCardInfoTextOverwriteDialog().getOpenJS());
            return;
        } else {
            saveChanges(false);
        }

        updateOnStateChanged();
    }

    public Boolean getVoterNumbersGenerated() {
        if (voterNumbersGenerated == null) {
            voterNumbersGenerated = checkIsVoterNumbersGenerated();
        }
        return voterNumbersGenerated;
    }

    boolean electionTextIsInvalid() {
        final ElectionCardModel ecm = getElectionCardModel(selectedTreeNode);
        return ecm != null
                && isNotEmpty(ecm.getElectionCardInfoText())
                && !electionCardInfoTextPattern.matcher(ecm.getElectionCardInfoText()).matches();
    }

    boolean electionAddressInvalid() {
        final ElectionCardModel ecm = getElectionCardModel(selectedTreeNode);
        return ecm != null
                && isNotEmpty(ecm.getAddress())
                && !electionCardInfoTextPattern.matcher(ecm.getAddress()).matches();
    }

    private boolean isRootNodeAndChildrenHasCustomElectionText() {
        return !getCustomInfoTextModels().isEmpty()
                && selectedTreeNode != null
                && getElectionCardModel(selectedTreeNode).isRoot();
    }

    public List<ElectionCardModel> getCustomInfoTextModels() {
        if (rootTreeNode == null) {
            return new ArrayList<>();
        }
        return getCustomInfoTextModels(new ArrayList<>(), rootTreeNode.getChildren().get(0));
    }

    private List<ElectionCardModel> getCustomInfoTextModels(List<ElectionCardModel> list, TreeNode node) {
        if (node.isSelectable()
                && !getElectionCardModel(node).isRoot()
                && getElectionCardModel(node).isCustomInfoText()) {
            list.add(getElectionCardModel(node));
        }

        for (TreeNode child : node.getChildren()) {
            getCustomInfoTextModels(list, child);
        }
        return list;

    }

    public void keepCustom() {
        overwriteCustomInfoText = false;
        confirmSave();
    }

    public void overwriteCustom() {
        overwriteCustomInfoText = true;
        confirmSave();
    }

    private void confirmSave() {
        saveChanges(false);
        FacesUtil.executeJS(getConfirmElectionCardInfoTextOverwriteDialog().getCloseJS());
        updateOnStateChanged();
    }

    private void updateOnStateChanged() {
        FacesUtil.updateDom(asList("configurationPanel", "approve-form"));
    }

    private void updateForm() {
        FacesUtil.updateDom(getFormId());
    }

    private boolean saveChanges(boolean done) {
        if (selectedTreeNode == null || !isEditable() || (done && !isValid())) {
            return false;
        }

        ElectionCardModel model = getElectionCardModel(selectedTreeNode);
        if (model.isRoot() && !done) {
            updateChildrenWithInfoText(model, selectedTreeNode);
        }

        return execute(() -> {
            if (getUserData().isOpptellingsvalgstyret() || super.saveDone(done)) {
                electionCardService.save(getUserData(), getElectionCard());
                MessageUtil.buildSavedMessage(getElectionCard());
            }
            init();
        });
    }

    @Override
    public void cancelWrite() {
        super.cancelWrite();
        init();
    }

    private void updateChildrenWithInfoText(ElectionCardModel root, TreeNode node) {
        for (TreeNode childNode : node.getChildren()) {
            if (childNode.isSelectable()) {
                ElectionCardModel childElectionCard = getElectionCardModel(childNode);
                if (isOverwriteCustomInfoText()
                        || isEmpty(childElectionCard.getElectionCardInfoText())
                        || !childElectionCard.isCustomInfoText()) {
                    childElectionCard.getPollingPlace().setInfoText(root.getElectionCardInfoText());
                }
            } else {
                updateChildrenWithInfoText(root, childNode);
            }
        }
    }

    @Override
    public String getId() {
        return "valgkort"; // AFT fungerte ikke med standardverdien 'electioncard'
    }

    public void viewModel(NodeSelectEvent event) {
        TreeNode node = event.getTreeNode();
        if (node.isLeaf()) {
            node.getParent().setExpanded(true);
        }
    }

    public String getHeaderText() {
        if (isOpptellingsvalgstyret()) {
            return "@config.local.election_card.infoText_opptellingsvalgstyre";
        } else if (getSelected().isRoot()) {
            return "@config.local.election_card.infoText_municipality";
        } else {
            return "@config.local.election_card.infoText_pollingPlace";
        }
    }

    public boolean isOpptellingsvalgstyret() {
        return getUserData().isOpptellingsvalgstyret();
    }

    boolean isDirty() {
        if (rootTreeNode == null || selectedTreeNode == null) {
            return false;
        }
        ElectionCardModel model = getElectionCardModel(selectedTreeNode);
        return model.isInfoTextChanged() || model.isAddressChanged();
    }

    boolean isValid() {
        if (rootTreeNode == null) {
            return false;
        }

        return isValid(rootTreeNode.getChildren().get(0));
    }

    private boolean isValid(TreeNode node) {
        if (node.isSelectable() && !getElectionCardModel(node).isValid()) {
            return false;
        }
        for (TreeNode child : node.getChildren()) {
            if (!isValid(child)) {
                return false;
            }
        }
        return true;
    }

    private String getFormId() {
        return getWithBaseId(getWidgetId() + ":form");
    }

    ElectionCardModel getElectionCardModel(TreeNode node) {
        return node == null ? null : (ElectionCardModel) node.getData();
    }

    public TreeNode getRootTreeNode() {
        return rootTreeNode;
    }

    public void setRootTreeNode(DefaultTreeNode municipalityTree) {
        this.rootTreeNode = municipalityTree;
    }

    public TreeNode getSelectedTreeNode() {
        return selectedTreeNode;
    }

    public void setSelectedTreeNode(TreeNode selectedTreeNode) {
        this.selectedTreeNode = selectedTreeNode;
    }

    public ElectionCardConfig getElectionCard() {
        return electionCard;
    }

    public boolean isOverwriteCustomInfoText() {
        return overwriteCustomInfoText;
    }

    public void setOverwriteCustomInfoText(boolean overwriteCustomInfoText) {
        this.overwriteCustomInfoText = overwriteCustomInfoText;
    }

    public List autoCompleteTest(String input) {
        AddressSearch addressSearch = AddressSearch.builder()
                .streetName(input)
                .build();
        return addressesToStrings(mapService.addressSearch(addressSearch).getAddresses());
    }

    private List<String> addressesToStrings(List<Address> addresses) {
        List<String> strings = new ArrayList<>();
        for (Address address : addresses) {
            strings.add(address.getStreetName());
        }

        return strings;
    }
}
