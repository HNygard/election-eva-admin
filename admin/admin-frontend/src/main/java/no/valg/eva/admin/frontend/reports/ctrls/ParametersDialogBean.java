package no.valg.eva.admin.frontend.reports.ctrls;

import no.evote.constants.AreaLevelEnum;
import no.evote.security.UserData;
import no.valg.eva.admin.common.rapport.model.ValghendelsesRapport;
import no.valg.eva.admin.common.reporting.model.ReportParameter;
import no.valg.eva.admin.common.reporting.model.ReportTemplate;
import no.valg.eva.admin.common.reporting.model.SelectableReportParameterValue;
import no.valg.eva.admin.common.reporting.service.JasperReportService;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.frontend.i18n.MessageProvider;
import no.valg.eva.admin.frontend.reports.SelectableValuesWrapper;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringUtils.isBlank;

public class ParametersDialogBean implements Serializable {

    private static final long serialVersionUID = 3949426822305946469L;

    static final String RAPPORT_AVKRYSNINGSMANNTALL = "Report_4";

    private UserData userData;
    private JasperReportService jasperReportService;
    private MessageProvider messageProvider;
    private ValghendelsesRapport valghendelsesRapport;
    private ReportTemplate selectedReport;
    private Map<String, String> canonicalReportParameterParentIdMap;
    private List<ReportParameter> reportParameters;
    private Map<String, Object> arguments = new HashMap<>();
    private Map<ReportParameter, SelectableValuesWrapper> selectItemsMap = new HashMap<>();
    private boolean avkrysningsmanntallTest = true;
    private long countUnapprovedAdvanceVotings;

    ParametersDialogBean(ReportLinksController reportLinksController, ValghendelsesRapport valghendelsesRapport) {
        this.valghendelsesRapport = valghendelsesRapport;
        userData = reportLinksController.getUserData();
        messageProvider = reportLinksController.getMessageProvider();
        jasperReportService = reportLinksController.getJasperReportService();
        canonicalReportParameterParentIdMap = reportLinksController.getCanonicalReportParameterParentIdMap();
        selectedReport = jasperReportService.getReportTemplate(userData, valghendelsesRapport);

        getParameters().stream().filter(parameter -> parameter.isInferred() || parameter.isFixed())
                .forEach(parameter -> arguments.put(parameter.getId(), parameter.getDefaultValue().toString()));

        if (isAvkrysningsmanntall()) {
            countUnapprovedAdvanceVotings = reportLinksController.getVotingService().countUnapprovedAdvanceVotings(userData, userData.getOperatorAreaPath());
        }
    }

    public final boolean isAvkrysningsmanntall() {
        return userData.getOperatorMvArea().isMunicipalityLevel() && RAPPORT_AVKRYSNINGSMANNTALL.equals(valghendelsesRapport.getRapportId());
    }

    public boolean isAvkrysningsmanntallReady() {
        return !isAvkrysningsmanntall() || isAvkrysningsmanntallTest() || getCountUnapprovedAdvanceVotings() == 0;
    }

    public long getCountUnapprovedAdvanceVotings() {
        return countUnapprovedAdvanceVotings;
    }

    public boolean isAvkrysningsmanntallTest() {
        return avkrysningsmanntallTest;
    }

    public void setAvkrysningsmanntallTest(boolean avkrysningsmanntallTest) {
        this.avkrysningsmanntallTest = avkrysningsmanntallTest;
    }

    /**
     * Determines a reports final area level, which is either the user's level if report's level is undefined, or the highest level of the report and the user's
     * E.g. if the user's role is at municipality level, but the report is for the county, the county level name is returned
     *
     * @return final level
     */
    public String getAreaLevelForReportAndUser() {
        MvArea area = userData.getOperatorRole().getMvArea();
        int finalAreaLevel = findNearestAreaLevel(area, getSelectedReport());

        String areaName;
        switch (AreaLevelEnum.getLevel(finalAreaLevel)) {
            case ROOT:
                areaName = area.getElectionEventName();
                break;
            case COUNTRY:
                areaName = area.getCountryName();
                break;
            case COUNTY:
                areaName = area.getCountyName();
                break;
            case MUNICIPALITY:
                areaName = area.getMunicipalityName();
                break;
            case BOROUGH:
                areaName = area.getBoroughName();
                break;
            case POLLING_DISTRICT:
                areaName = area.getPollingDistrictName();
                break;
            case POLLING_PLACE:
                areaName = area.getPollingPlaceName();
                break;
            case POLLING_STATION:
                areaName = area.getPollingPlaceName();
                break;
            default:
                areaName = "";
        }
        // postfixing area with level names for country and election event (root) tends ti look silly, so skip in those cases.
        switch (AreaLevelEnum.getLevel(finalAreaLevel)) {
            case ROOT:
            case COUNTRY:
                break;
            default:
                String levelName = messageProvider.get("@area_level[" + finalAreaLevel + "].name");
                areaName += ' ' + levelName.substring(0, 1).toLowerCase() + levelName.substring(1);
        }
        return areaName;
    }

    public final List<ReportParameter> getParameters() {
        if (reportParameters == null) {
            reportParameters = getSelectedReport().getParameters();
        }
        // check to see if any report parameters have a parent whose value is present as a request parameter. If so, inform its dependent
        // parameter of the new parent value
        reportParameters.stream().filter(parameter -> arguments.containsKey(parameter.getId()) && parameter.getDependentParameters() != null)
                .forEach(parameter -> {
                    for (ReportParameter dependentParameter : parameter.getDependentParameters()) {
                        dependentParameter.setParentValue(arguments.get(parameter.getId()));
                        dependentParameter.setParent(parameter);
                    }
                });
        return reportParameters;
    }

    public SelectableValuesWrapper getSelectItemsForParameter(ReportParameter reportParameter) {
        if (reportParameter != null) {
            SelectableValuesWrapper selectItems = selectItemsMap.get(reportParameter);
            if (selectItems == null && reportParameter.getParent() != null) {
                Collection<SelectableReportParameterValue> selectableValuesForParameter = jasperReportService
                        .getSelectableValuesForParameter(
                                userData, reportParameter, getSelectedReport().getReportUri());
                selectItems = new SelectableValuesWrapper(selectableValuesForParameter, canonicalReportParameterParentIdMap,
                        reportParameter);
                if (!selectItems.getSelectItems().isEmpty()) {
                    selectItemsMap.put(reportParameter, selectItems);
                }
            }
            return selectItems;
        } else {
            return SelectableValuesWrapper.EMPTY;
        }
    }

    /**
     * When a value for a parameter has been selected, clear all selections below it
     */
    public void handleSelectedParameter(ReportParameter parameter) {
        parameter.getDescendingParameters().stream().filter(descendingParameter -> !descendingParameter.isFixed()).forEach(descendingParameter -> {
            arguments.remove(descendingParameter.getId());
            selectItemsMap.remove(descendingParameter);
            if (descendingParameter.getParent().equals(parameter)) {
                descendingParameter.setParentValue(arguments.get(parameter.getId()));
            } else {
                descendingParameter.setParentValue(null);
            }
        });
    }

    public boolean isAllParametersSupplied() {
        if (!isAvkrysningsmanntallReady()) {
            return false;
        }
        for (ReportParameter parameter : getParameters()) {
            if (parameter.isMandatory() && (argumentIsMissing(parameter) || argumentValueIsMissingOrBlank(parameter))) {
                return false;
            }
        }
        return true;
    }

    public ReportTemplate getSelectedReport() {
        return selectedReport;
    }

    public Map<String, Object> getArguments() {
        return arguments;
    }

    private boolean argumentIsMissing(ReportParameter parameter) {
        return arguments.get(parameter.getId()) == null;
    }

    private boolean argumentValueIsMissingOrBlank(ReportParameter parameter) {
        return isBlank(ofNullable(arguments.get(parameter.getId())).orElse("").toString());
    }

    private int findNearestAreaLevel(MvArea mvArea, ReportTemplate reportTemplate) {
        int userAreaLevel = mvArea.getAreaLevel();
        if (reportTemplate.getAreaLevels() == null) {
            return userAreaLevel;
        }

        int nearestUserAreaLevel = -1;
        for (int rtLevel : reportTemplate.getAreaLevels()) {
            if (rtLevel <= userAreaLevel) {
                nearestUserAreaLevel = Math.max(nearestUserAreaLevel, rtLevel);
            }
        }
        return nearestUserAreaLevel < 0 ? userAreaLevel : nearestUserAreaLevel;
    }
}
