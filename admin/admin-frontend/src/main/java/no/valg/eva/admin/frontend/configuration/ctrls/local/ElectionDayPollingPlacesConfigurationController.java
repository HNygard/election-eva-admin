package no.valg.eva.admin.frontend.configuration.ctrls.local;

import lombok.Data;
import no.valg.eva.admin.application.MapService;
import no.valg.eva.admin.common.configuration.model.OpeningHours;
import no.valg.eva.admin.common.configuration.model.election.ElectionDay;
import no.valg.eva.admin.common.configuration.model.local.Borough;
import no.valg.eva.admin.common.configuration.model.local.ElectionDayPollingPlace;
import no.valg.eva.admin.common.configuration.model.local.Place;
import no.valg.eva.admin.common.configuration.service.ElectionEventService;
import no.valg.eva.admin.common.configuration.service.MunicipalityService;
import no.valg.eva.admin.common.configuration.service.PollingPlaceService;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.frontend.common.Button;
import no.valg.eva.admin.frontend.common.ButtonType;
import no.valg.eva.admin.frontend.common.MapModelMaker;
import no.valg.eva.admin.frontend.common.dialog.Dialog;
import no.valg.eva.admin.frontend.common.dialog.Dialogs;
import no.valg.eva.admin.frontend.configuration.ConfigurationMode;
import no.valg.eva.admin.frontend.configuration.ConfigurationView;
import no.valg.eva.admin.frontend.configuration.models.ElectionDayPollingPlaceViewModel;
import no.valg.eva.admin.frontend.util.FacesUtil;
import no.valg.eva.admin.frontend.util.MessageUtil;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.primefaces.event.NodeSelectEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import org.primefaces.model.map.DefaultMapModel;
import org.primefaces.model.map.MapModel;

import javax.faces.application.FacesMessage;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static no.evote.presentation.validation.GpsCoordinatesValidator.isValidGpsCoordinate;
import static no.valg.eva.admin.frontend.common.Button.enabled;
import static no.valg.eva.admin.frontend.common.Button.notRendered;
import static no.valg.eva.admin.frontend.configuration.ctrls.local.AddressLookupComponent.canLookupCoordinatesFor;
import static no.valg.eva.admin.frontend.configuration.ctrls.local.AddressLookupComponent.findGpsCoordinatesForPlace;
import static no.valg.eva.admin.frontend.configuration.models.ElectionDayPollingPlaceViewModel.ViewModelType.MUNICIPALITY;
import static no.valg.eva.admin.frontend.configuration.models.ElectionDayPollingPlaceViewModel.ViewModelType.POLLING_PLACE;
import static org.apache.commons.lang3.StringUtils.abbreviate;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isEmpty;

@Named
@ViewScoped
@Data
public class ElectionDayPollingPlacesConfigurationController extends ConfigurationController {

    private static final long serialVersionUID = 1904444033656315707L;
    
    @Inject
    private ElectionEventService electionEventService;

    @Inject
    private PollingPlaceService pollingPlaceService;

    @Inject
    private MunicipalityService municipalityService;

    @Inject
    private MapService mapService;

    private List<ElectionDay> electionDays;
    private List<ElectionDayPollingPlaceViewModel> viewModels = new ArrayList<>();
    private ElectionDayPollingPlaceViewModel selectedViewModel;

    private Boolean voterNumbersGenerated;
    private TreeNode nodeTree;
    private TreeNode rootTreeNode;
    private TreeNode selectedTreeNode;
    private List<ElectionDayPollingPlace> pollingPlacesWithCustomOpeningHours;
    private MapModel mapModel = new DefaultMapModel();

    @Override
    public void init() {
        showElectoralRollPrintedMessageIfAppropriate();
        initializeElectionDays();
        collectDataAndBuildTree();
    }

    private void showElectoralRollPrintedMessageIfAppropriate() {
        if (isManntallsnummerGenerert()) {
            String message = getMessageProvider().get("@config.local.manntallsnummerErGenerert",
                    getUserDataController().getElectionEvent().getName());
            MessageUtil.buildDetailMessage(message, FacesMessage.SEVERITY_ERROR);
        }
    }

    public Boolean isManntallsnummerGenerert() {
        if (voterNumbersGenerated == null) {
            voterNumbersGenerated = checkIsVoterNumbersGenerated();
        }
        return voterNumbersGenerated;
    }

    private void initializeElectionDays() {
        electionDays = electionEventService.findElectionDaysByElectionEvent(getUserData(), new ElectionEvent(getUserData().getElectionEventPk()));
    }

    private void initializeNodeTree() {
        setNodeTree(new DefaultTreeNode("Root", null));
    }

    private void collectDataAndBuildTree() {
        initializeNodeTree();
        viewModels = new ArrayList<>();
        final List<ElectionDayPollingPlace> places = getPollingPlaces();

        if (places.size() == 1) {
            setupSinglePollingPlaceTreeNode(places);
        } else {
            setupMunicipalityTreeNode();
            addPollingPlaceTreeNodes(places);

        }
        setSelectedViewModel(municipalityOrLastSelectedNode());
        updateTreeNodeFromViewModel();
        validateTree();
    }

    private List<ElectionDayPollingPlace> getPollingPlaces() {
        return getPollingPlaceService().findElectionDayPollingPlacesByArea(getUserData(), getAreaPath());
    }

    private void setupSinglePollingPlaceTreeNode(List<ElectionDayPollingPlace> places) {
        final ElectionDayPollingPlaceViewModel pollingPlaceViewModel = new ElectionDayPollingPlaceViewModel(places.get(0), electionDays);
        viewModels.add(pollingPlaceViewModel);
        rootTreeNode = new DefaultTreeNode(pollingPlaceViewModel, getNodeTree());
    }

    private void setupMunicipalityTreeNode() {
        final Municipality municipality = getMvArea().getMunicipality();
        final ElectionDayPollingPlaceViewModel municipalityViewModel = new ElectionDayPollingPlaceViewModel(municipality, getMunicipalityOpeningHours(), electionDays);
        viewModels.add(municipalityViewModel);

        rootTreeNode = new DefaultTreeNode(
                municipalityViewModel,
                getNodeTree());
        rootTreeNode.setExpanded(true);
    }

    private List<OpeningHours> getMunicipalityOpeningHours() {
        return getMunicipalityService().getOpeningHours(
                getUserData(),
                getMvArea().getMunicipality()
        );
    }

    private void addPollingPlaceTreeNodes(List<ElectionDayPollingPlace> places) {
        if (!places.isEmpty()) {
            if (isHasBoroughs()) {
                final Map<Borough, List<ElectionDayPollingPlace>> pollingPlacesInBoroughs = groupByBorough(places);
                addBoroughsTo(rootTreeNode, pollingPlacesInBoroughs);
            } else {
                addPollingPlacesTo(rootTreeNode, places);
            }
        }
    }

    private SortedMap<Borough, List<ElectionDayPollingPlace>> groupByBorough(List<ElectionDayPollingPlace> places) {
        SortedMap<Borough, List<ElectionDayPollingPlace>> result = new TreeMap<>(Comparator.comparing(borough -> borough.getPath().path()));
        for (ElectionDayPollingPlace place : places) {
            List<ElectionDayPollingPlace> grouped = result.computeIfAbsent(place.getBorough(), k -> new ArrayList<>());
            grouped.add(place);
        }
        return result;
    }

    private void addBoroughsTo(TreeNode parentNode, Map<Borough, List<ElectionDayPollingPlace>> grouped) {

        for (Map.Entry<Borough, List<ElectionDayPollingPlace>> entry : grouped.entrySet()) {

            final Borough borough = entry.getKey();

            final ElectionDayPollingPlaceViewModel boroughViewModel = new ElectionDayPollingPlaceViewModel(borough);
            viewModels.add(boroughViewModel);

            final TreeNode boroughNode = new DefaultTreeNode(
                    boroughViewModel,
                    parentNode);
            boroughNode.setSelectable(false);

            addPollingPlacesTo(boroughNode, entry.getValue());
        }
    }

    private void addPollingPlacesTo(TreeNode parentNode, List<ElectionDayPollingPlace> placeList) {
        placeList.forEach(pollingPlace -> {
            final ElectionDayPollingPlaceViewModel pollingPlaceViewModel = new ElectionDayPollingPlaceViewModel(pollingPlace, electionDays);
                    viewModels.add(pollingPlaceViewModel);
                    new DefaultTreeNode(pollingPlaceViewModel, parentNode);
                }
        );
    }

    private ElectionDayPollingPlaceViewModel municipalityOrLastSelectedNode() {
        return getSelectedTreeNode() == null ? findViewModelFromNode(rootTreeNode) : findViewModelFromNode(getSelectedTreeNode());
    }

    private ElectionDayPollingPlaceViewModel findViewModelFromNode(TreeNode node) {
        if (node != null && node.getData() != null) {
            final ElectionDayPollingPlaceViewModel nodeData = (ElectionDayPollingPlaceViewModel) node.getData();
            return viewModels.stream()
                    .filter(currentViewModel -> currentViewModel.sameTypeAndPk(nodeData))
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }

    private void updateTreeNodeFromViewModel() {
        TreeNode currentSelectedTreeNode = findNodeFromViewModel();
        setSelectedTreeNode(currentSelectedTreeNode);
        currentSelectedTreeNode.setSelected(true);
        expandHierarchy(currentSelectedTreeNode);
        updateMapMarker();
    }

    private TreeNode findNodeFromViewModel() {
        return traverseTree(getNodeTree(), getSelectedViewModel());
    }

    private static TreeNode traverseTree(TreeNode node, ElectionDayPollingPlaceViewModel viewModel) {
        if (viewModel.sameTypeAndPk(getViewModelFromNode(node))) {
            return node;
        } else if (node.isLeaf()) {
            return null;
        } else {
            for (TreeNode child : node.getChildren()) {
                TreeNode result = traverseTree(child, viewModel);
                if (result != null) {
                    return result;
                }
            }
            return null;
        }
    }

    private static ElectionDayPollingPlaceViewModel getViewModelFromNode(TreeNode node) {
        if (node.getData() instanceof ElectionDayPollingPlaceViewModel) {
            return (ElectionDayPollingPlaceViewModel) node.getData();
        }
        return null;
    }

    private static void expandHierarchy(TreeNode node) {
        final TreeNode parent = node.getParent();
        if (parent != null) {
            parent.setExpanded(true);
            parent.getChildren().forEach(childNode -> {
                boolean workingNode = childNode.equals(node);
                childNode.setExpanded(workingNode);
            });
            expandHierarchy(parent);
        }
    }

    private void updateMapMarker() {
        if (shouldShowMap()) {
            MapModelMaker.addMarkerOverlay(
                    mapModel,
                    getSelectedViewModel().getName(),
                    getSelectedViewModel().getGpsCoordinates()
            );
        }
    }

    private void validateTree() {
        viewModels.forEach(vm -> vm.setValid(vm.hasValidData()));
        viewModels.stream()
                .filter(vm -> vm.getType() == POLLING_PLACE && !vm.isValid())
                .forEach(vm -> {
                    TreeNode invalidPollingPlaceNode = traverseTree(getNodeTree(), vm);

                    if (invalidPollingPlaceNode != null) {
                        invalidateParents(invalidPollingPlaceNode);
                    }
                });
    }

    private static void invalidateParents(TreeNode node) {
        final TreeNode parent = node.getParent();
        if (parent != null && parent.getData() instanceof ElectionDayPollingPlaceViewModel) {
            ElectionDayPollingPlaceViewModel parentData = (ElectionDayPollingPlaceViewModel) parent.getData();
            parentData.setValid(false);
            invalidateParents(parent);
        }
    }


    @Override
    public ConfigurationView getView() {
        return ConfigurationView.ELECTION_DAY_POLLING_PLACES;
    }

    @Override
    public String getName() {
        return "@config.local.accordion.election_day_polling_place.name";
    }

    @Override
    public boolean hasAccess() {
        return isMunicipalityLevel();
    }

    @Override
    public void setDoneStatus(boolean value) {
        if (isMunicipalityLevel()) {
            getMunicipalityConfigStatus().setElectionPollingPlaces(value);
        }
    }

    @Override
    public boolean isDoneStatus() {
        return isMunicipalityLevel() && getMunicipalityConfigStatus().isElectionPollingPlaces();
    }

    @Override
    public boolean canBeSetToDone() {
        return viewModels.isEmpty() || viewModels.stream().allMatch(ElectionDayPollingPlaceViewModel::isValid);
    }

    @Override
    public Class<? extends ConfigurationController>[] getRequiresDoneBeforeEdit() {
        if (getMainController().getElectionGroup().isElectronicMarkoffs() && !isManntallsnummerGenerert()) {
            return new Class[]{ElectronicMarkoffsConfigurationController.class};
        }
        return new Class[0];
    }


    public void viewModel(NodeSelectEvent event) {
        TreeNode node = event.getTreeNode();
        setSelectedViewModel(findViewModelFromNode(node));
        updateTreeNodeFromViewModel();
    }

    private void displayOverwriteOpeningHoursDialog() {
        final Municipality municipality = getMvArea().getMunicipality();

        pollingPlacesWithCustomOpeningHours = getPollingPlaceService().findPollingPlacesWithCustomOpeningHours(getUserData(), municipality, getAreaPath());

        if (pollingPlacesWithCustomOpeningHours.isEmpty()) {
            onOverwriteOpeningHours();
        } else {
            FacesUtil.executeJS(getOverwriteOpeningHoursDialog().getOpenJS());
        }
    }

    public void onOverwriteOpeningHours() {
        saveAndOverwriteOpeningHours(true);
    }

    private void saveAndOverwriteOpeningHours(boolean overwrite) {
        if (saveDone(false)) {
            execute(() -> saveMunicipalityOpeningHours(overwrite));
        }
        FacesUtil.executeJS(getOverwriteOpeningHoursDialog().getCloseJS());
        onSaveFinished();
    }

    public void onKeepCustomOpeningHours() {
        saveAndOverwriteOpeningHours(false);
    }

    public void saveChanges() {
        final ElectionDayPollingPlaceViewModel viewModel = getSelectedViewModel();

        boolean isViewModelValid = execute(viewModel::validateOpeningHours);
        if (isViewModelValid) {
            if (viewModel.getType() == MUNICIPALITY) {
                displayOverwriteOpeningHoursDialog();
            } else {
                savePollingPlace();
                onSaveFinished();
            }
        }
    }

    private void savePollingPlace() {
        if (saveDone(false)) {
            ElectionDayPollingPlace pollingPlace = getSelectedViewModel().toDto();
            execute(() -> getPollingPlaceService().saveElectionDayPollingPlace(getUserData(), pollingPlace));
        }
    }

    private void onSaveFinished() {
        if (getSelectedViewModel().getType() == POLLING_PLACE) {
            MessageUtil.buildSavedMessage(getSelectedViewModel());
        }
        validateTree();
        FacesUtil.updateDom(asList("configurationPanel", "approve-form"));
        collectDataAndBuildTree();
    }

    @Override
    public void cancelWrite() {
        super.cancelWrite();
        collectDataAndBuildTree();
    }

    public Dialog getOverwriteOpeningHoursDialog() {
        return Dialogs.CONFIRM_ELECTION_DAY_OPENING_HOURS_OVERWRITE;
    }

    public List<ElectionDayPollingPlace> getPlacesWithCustomOpeningHours() {
        return pollingPlacesWithCustomOpeningHours;
    }

    private String getFormattedElectionDate(ElectionDay electionDay) {
        return formatElectionDayDate(electionDay);
    }

    private String formatElectionDayDate(ElectionDay electionDay) {
        return DateTimeFormat.forPattern("d. MMMM yyyy").print(electionDay.getDate()).toLowerCase();
    }

    public String getFormattedTime(LocalTime lt) {
        return lt != null ? DateTimeFormat.forPattern("HH:mm").print(lt) : "";
    }

    private void saveMunicipalityOpeningHours(boolean overwriteOpeningHoursForPollingPlaces) {
        getMunicipalityService().saveOpeningHours(
                getUserData(),
                getMvArea().getMunicipality(),
                getSelectedViewModel().toDto().getOpeningHours(),
                overwriteOpeningHoursForPollingPlaces,
                getAreaPath());
    }

    @Override
    public String getSelectId(Place place) {
        if (place instanceof ElectionDayPollingPlace) {
            return abbreviate(place.getId() + "-" + ((ElectionDayPollingPlace) place).getParentName(), SELECT_ID_LENGTH);
        }
        return super.getSelectId(place);
    }

    public boolean shouldShowMap() {
        final ElectionDayPollingPlaceViewModel viewModel = getSelectedViewModel();
        return viewModel != null &&
                viewModel.getType() == POLLING_PLACE &&
                !isEmpty(viewModel.getGpsCoordinates()) &&
                isValidGpsCoordinate(viewModel.getGpsCoordinates());
    }

    public int getPollingPlaceViewModelsSize() {
        return (int) viewModels.stream()
                .filter(vm -> vm.getType() == POLLING_PLACE)
                .count();
    }

    public boolean isSelectedViewModelOfTypeMunicipality() {
        return getSelectedViewModel().getType() == MUNICIPALITY;
    }

    public boolean isSelectedViewModelOfTypePollingPlace() {
        return getSelectedViewModel().getType() == POLLING_PLACE;
    }

    public boolean shouldPrintCustomOpeningHoursIcon(ElectionDayPollingPlaceViewModel node) {
        return node.hasCustomOpeningHours() && !isSinglePollingPlace();
    }

    private boolean isSinglePollingPlace() {
        return viewModels.size() == 1;
    }

    @Override
    public Button button(ButtonType type) {
        boolean editable = isEditable();
        switch (type) {
            case UPDATE:
                if (ConfigurationMode.READ.equals(getMode())) {
                    return enabled(editable);
                }
                return notRendered();
            case EXECUTE_UPDATE:
            case CANCEL:
                if (isWriteMode()) {
                    return enabled(editable);
                }
                return notRendered();
            default:
                return super.button(type);
        }
    }

    private List<OpeningHours> openingHoursForPollingPlaceAndElectionDay(ElectionDayPollingPlace pollingPlace, ElectionDay electionDay) {
        return getPlacesWithCustomOpeningHours().stream()
                .filter(currentElectionDayPollingPlace -> currentElectionDayPollingPlace.getPk().equals(pollingPlace.getPk()))
                .map(ElectionDayPollingPlace::getOpeningHours)
                .flatMap(List::stream)
                .filter(filterOpeningHoursForElectionDayAndPollingPlace(electionDay))
                .sorted(Comparator.comparing(o -> o.getElectionDay().getDate())).collect(Collectors.toList());
    }

    private Predicate<OpeningHours> filterOpeningHoursForElectionDayAndPollingPlace(ElectionDay electionDay) {
        return openingHours -> openingHours.getElectionDay().getPk().equals(electionDay.getPk());
    }

    public boolean isOptionalElectionDay(ElectionDayPollingPlace electionDayPollingPlace) {
        return openingHoursForPollingPlaceAndElectionDay(electionDayPollingPlace, electionDays.get(0)).size() > 1;
    }

    public String getOptionalElectionDateFormatted() {
        return getFormattedElectionDate(electionDays.get(0));
    }

    public String getRequiredElectionDateFormatted() {
        return getFormattedElectionDate(electionDays.get(1));
    }

    public List<OpeningHours> getOpeningHoursForPollingPlaceAndOptionalElectionDay(ElectionDayPollingPlace electionDayPollingPlace) {
        return openingHoursForPollingPlaceAndElectionDay(electionDayPollingPlace, electionDays.get(0));
    }

    public List<OpeningHours> getOpeningHoursForPollingPlaceAndRequiredElectionDay(ElectionDayPollingPlace electionDayPollingPlace) {
        return openingHoursForPollingPlaceAndElectionDay(electionDayPollingPlace, electionDays.get(1));
    }

    public void doAddressLookup(@SuppressWarnings("unused") AjaxBehaviorEvent abe) {

        final ElectionDayPollingPlace pollingPlace = getSelectedViewModel().toDto();

        if (canLookupCoordinatesFor(pollingPlace)) {
            getSelectedViewModel().setGpsCoordinates(findGpsCoordinatesForPlace(pollingPlace, getMvArea(), mapService));

            if (isBlank(getSelectedViewModel().getGpsCoordinates())) {
                MessageUtil.buildMessageForClientId(
                        getFormComponentId("gpsCoordinates"),
                        getMessageProvider().get("@count.error.gpsCoordinatesNotFound"),
                        FacesMessage.SEVERITY_ERROR
                );
            }

            FacesUtil.updateDom(asList(
                    getFormComponentId("gpsCoordinates"),
                    getFormComponentId("gpsCoordinatesError"),
                    getFormComponentId("map")
            ));
        }
    }

    private String getFormComponentId(String componentId) {
        return format("%s:%s", getFormId(), componentId);
    }

    private String getFormId() {
        return getWithBaseId("electionDayPollingPlaceView:form");
    }
}
