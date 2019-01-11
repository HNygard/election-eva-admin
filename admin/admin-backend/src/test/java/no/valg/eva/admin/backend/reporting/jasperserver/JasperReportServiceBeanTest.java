package no.valg.eva.admin.backend.reporting.jasperserver;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import no.evote.constants.AreaLevelEnum;
import no.evote.exception.EvoteException;
import no.evote.exception.EvoteSecurityException;
import no.evote.exception.ValidateException;
import no.evote.security.UserData;
import no.evote.service.configuration.CountryServiceBean;
import no.valg.eva.admin.backend.configuration.repository.ElectionEventRepository;
import no.valg.eva.admin.backend.reporting.jasperserver.api.DataType;
import no.valg.eva.admin.backend.reporting.jasperserver.api.FileReference;
import no.valg.eva.admin.backend.reporting.jasperserver.api.FileResource;
import no.valg.eva.admin.backend.reporting.jasperserver.api.FolderMetaData;
import no.valg.eva.admin.backend.reporting.jasperserver.api.InputControl;
import no.valg.eva.admin.backend.reporting.jasperserver.api.JasperExecution;
import no.valg.eva.admin.backend.reporting.jasperserver.api.JasperExecutionRequest;
import no.valg.eva.admin.backend.reporting.jasperserver.api.JasperFolder;
import no.valg.eva.admin.backend.reporting.jasperserver.api.JasperReport;
import no.valg.eva.admin.backend.reporting.jasperserver.api.JasperResources;
import no.valg.eva.admin.backend.reporting.jasperserver.api.JasperRestApi;
import no.valg.eva.admin.backend.reporting.jasperserver.api.JasperRestApiNoTimeout;
import no.valg.eva.admin.backend.reporting.jasperserver.api.JasperRestApiWithTimeout;
import no.valg.eva.admin.backend.reporting.jasperserver.api.PregeneratedContentRetriever;
import no.valg.eva.admin.backend.reporting.jasperserver.api.ReportExecutionStatus;
import no.valg.eva.admin.backend.reporting.jasperserver.api.ReportMetaData;
import no.valg.eva.admin.backend.reporting.jasperserver.api.ReportMetaData.UnselectableParameterValue;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.reporting.model.ReportExecution;
import no.valg.eva.admin.common.reporting.model.ReportParameter;
import no.valg.eva.admin.common.reporting.model.ReportTemplate;
import no.valg.eva.admin.common.reporting.model.SelectableReportParameterValue;
import no.valg.eva.admin.configuration.domain.model.Borough;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.ContestArea;
import no.valg.eva.admin.configuration.domain.model.Country;
import no.valg.eva.admin.configuration.domain.model.County;
import no.valg.eva.admin.configuration.domain.model.Election;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.ElectionGroup;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.configuration.domain.model.PollingDistrict;
import no.valg.eva.admin.configuration.domain.model.PollingPlace;
import no.valg.eva.admin.configuration.repository.BoroughRepository;
import no.valg.eva.admin.configuration.repository.ContestRepository;
import no.valg.eva.admin.configuration.repository.CountryRepository;
import no.valg.eva.admin.configuration.repository.CountyRepository;
import no.valg.eva.admin.configuration.repository.ElectionGroupRepository;
import no.valg.eva.admin.configuration.repository.ElectionRepository;
import no.valg.eva.admin.configuration.repository.MunicipalityRepository;
import no.valg.eva.admin.configuration.repository.MvAreaRepository;
import no.valg.eva.admin.configuration.repository.MvElectionRepository;
import no.valg.eva.admin.configuration.repository.PollingDistrictRepository;
import no.valg.eva.admin.configuration.repository.PollingPlaceRepository;
import no.valg.eva.admin.felles.sti.valghierarki.ValgSti;
import no.valg.eva.admin.felles.sti.valghierarki.ValghierarkiSti;
import no.valg.eva.admin.rbac.domain.model.OperatorRole;
import no.valg.eva.admin.test.MockUtilsTestCase;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.enterprise.inject.Instance;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.google.common.collect.ImmutableSet.of;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.transform;
import static java.lang.String.valueOf;
import static java.util.Arrays.asList;
import static java.util.Collections.EMPTY_LIST;
import static java.util.Collections.EMPTY_MAP;
import static java.util.Collections.singletonList;
import static java.util.Collections.sort;
import static no.evote.constants.AreaLevelEnum.BOROUGH;
import static no.evote.constants.AreaLevelEnum.COUNTY;
import static no.evote.constants.AreaLevelEnum.MUNICIPALITY;
import static no.evote.constants.AreaLevelEnum.POLLING_DISTRICT;
import static no.evote.constants.AreaLevelEnum.ROOT;
import static no.valg.eva.admin.backend.reporting.jasperserver.api.DataType.Type.NUMBER;
import static no.valg.eva.admin.backend.reporting.jasperserver.api.ReportExecutionStatus.Status.EXECUTION;
import static no.valg.eva.admin.backend.reporting.jasperserver.api.ReportExecutionStatus.Status.READY;
import static no.valg.eva.admin.common.rbac.Accesses.Aggregert_Rapport;
import static no.valg.eva.admin.common.rbac.Accesses.Opptelling_Forhånd_Se;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;



public class JasperReportServiceBeanTest extends MockUtilsTestCase {
	private static final long COUNTY_LEVEL_CONTEST_PK = 100;
	private static final long COUNTY_LEVEL_OSLO_STORTING_CONTEST_PK = 105;
	private static final long MUNICIPALITY_LEVEL_CONTEST_PK = 50;
	private static final long MUNICIPALITY_LEVEL_CONTEST_HAMMERFEST_PK = 55;
	private static final long BOROUGH_LEVEL_CONTEST_PK = 150;
	private static final long COUNTY_ELECTION_PK = 1000;
	private static final long MUNICIPALITY_ELECTION_PK = 500;
	private static final long BOROUGH_ELECTION_PK = 1500;
	private static final int SORT_ORDER_1 = 100;
	private static final int SORT_ORDER_2 = 200;
	private static final int SORT_ORDER_3 = 300;
	private static final String CATEGORY_1_ID = SORT_ORDER_1 + "." + "category_1";
	private static final String CATEGORY_2_ID = SORT_ORDER_2 + "." + "category_2";
	private static final String CATEGORY_INACCESSIBLE_ID = SORT_ORDER_3 + "." + "category_3";
	private static final String CATEGORY_LABEL_1 = "Category 1";
	private static final String CATEGORY_LABEL_2 = "Category 2";
	private static final String REQUEST_ID = "677629003_1355225037212_1";
	private static final String FOLDER_URI_1 = "/reports/" + CATEGORY_1_ID;
	private static final String FOLDER_URI_2 = "/reports/" + CATEGORY_2_ID;
	private static final String FOLDER_URI_3 = "/reports/" + CATEGORY_1_ID;
	private static final String FOLDER_URI_INACCESSIBLE = "/reports/" + CATEGORY_INACCESSIBLE_ID;
	private static final JasperFolder JASPER_FOLDER_1 = new JasperFolder(FOLDER_URI_1, CATEGORY_LABEL_1);
	private static final JasperFolder JASPER_FOLDER_2 = new JasperFolder(FOLDER_URI_2, CATEGORY_LABEL_2);
	private static final JasperFolder JASPER_FOLDER_INACCESSIBLE = new JasperFolder(FOLDER_URI_INACCESSIBLE, CATEGORY_INACCESSIBLE_ID);
	private static final String REPORT_ID_1 = "100.report_1/Report1";
	private static final String REPORT_ID_2 = "100.report_2/Report2";
	private static final String REPORT_ID_3 = "100.report_3/Report3";
	private static final String REPORT_ID_4 = "100.report_3/Report4";
	private static final String REPORT_ID_5 = "100.report_3/Report5";
	private static final String REPORT_ID_6 = "100.report_3/Report6";
	private static final String REPORT_ID_INACCESSIBLE = "100.report_3/Report5";
	private static final String REPORT_ID_HIDDEN = "100.report_3/Report6";
	private static final String REPORT_ID_PREGENERATED = "100.report_3/Report7";
	private static final String REPORT_ID_ASYNC = "100.report_3/Report8";
	private static final String REPORT_ID_9 = "100.report_3/Report9";
	private static final String REPORT_ID_10 = "100.report_3/Report10";
	private static final String REPORT_ID_11 = "100.report_3/Report11";

	private static final String REPORT_URI_MUNICIPALITY = FOLDER_URI_1 + "/" + REPORT_ID_1;
	private static final String REPORT_URI_OSLO_ONLY = FOLDER_URI_3 + "/" + REPORT_ID_3;
	private static final String REPORT_URI_COUNTY = FOLDER_URI_2 + "/" + REPORT_ID_2;
	private static final String REPORT_URI_DUAL_AREA_LEVEL = FOLDER_URI_2 + "/" + REPORT_ID_4;
	private static final String REPORT_URI_CONTEST_PK = FOLDER_URI_2 + "/" + REPORT_ID_5;
	private static final String REPORT_URI_CONTEST_COUNTY_OR_MUNICIPALITY_PK = FOLDER_URI_2 + "/" + REPORT_ID_6;
	private static final String REPORT_URI_INACCESSIBLE_CATEGORY = FOLDER_URI_INACCESSIBLE + "/" + REPORT_ID_INACCESSIBLE;
	private static final String REPORT_URI_HIDDEN = FOLDER_URI_1 + "/" + REPORT_ID_HIDDEN;
	private static final String REPORT_URI_PREGENERATED = FOLDER_URI_1 + "/" + REPORT_ID_PREGENERATED;
	private static final String REPORT_URI_ASYNC = FOLDER_URI_1 + "/" + REPORT_ID_ASYNC;
	private static final String REPORT_URI_9 = FOLDER_URI_1 + "/" + REPORT_ID_9;
	private static final String REPORT_URI_10 = FOLDER_URI_1 + "/" + REPORT_ID_10;
	private static final String REPORT_URI_11 = FOLDER_URI_1 + "/" + REPORT_ID_11;

	private static final byte[] PDF_CONTENT = "pdf content".getBytes();
	private static final String FILENAME_PATTERN = "FilenamePattern";
	private static final String REPORT_1_NAME = "Report Name";
	private static final String REPORT_3_NAME = "Report for Oslo Only";
	private static final String REPORT_4_NAME = "Report with dual area level";
	private static final String REPORT_5_NAME = "Report matched to area level";
	private static final String REPORT_HIDDEN_NAME = "Hidden report";
	private static final String REPORT_PREGENERATED_NAME = "Pregenerated report";
	private static final String REPORT_ASYNC_NAME = "Asynchronous report";
	private static final String REPORT_9_NAME = "Report 9";
	private static final String REPORT_10_NAME = "Report with polling district parameter";
	private static final String REPORT_11_NAME = "Report with integer parameter";

	private static final String ELECTION_EVENT_PARAMETER_NAME = "EE1";
	private static final String COUNTY_PARAMETER_NAME = "EE1.CO1.CNT1";
	private static final String MUNICIPALITY_PARAMETER_NAME = "EE1.CO1.CNT1.MUN1";
	private static final String BOROUGH_PARAMETER_NAME = "EE1.CO1.CNT1.MUN1.BOR1";
	private static final String POLLING_PLACE_PARAMETER_NAME = "EE1.CO1.CNT1.MUN1.BOR1.PD1.PP1";
	private static final String ELECTION_GROUP_PARAMETER_NAME = "EE1.EG1";
	private static final String ELECTION_PARAMETER_NAME = "EE1.EG1.EL1";
	private static final String CONTEST_PARAMETER_NAME = "EE1.EG1.EL1.CT1";
	private static final String CONTEST_PK_PARAMETER = "EE1.EG1.EL1.CT1_PK";
	private static final String CONTEST_PK_FOR_COUNTY_OR_MUNICIPALITY_PARAMETER = "EE1.EG1.EL1.CT1_FOR_CNT_OR_MUN_ONLY_PK";
	private static final String PROTOCOL_FROM_INT_PARAMETER_NAME = "protocol_from_int";

	private static final String INPUT_CONTROL_ID_EE1 = "EE1";
	private static final String INPUT_CONTROL_URI_EE1 = "/inputControls/" + INPUT_CONTROL_ID_EE1;
	private static final String ELECTION_EVENT_LABEL = "@reporting.report.parameter.label.EE1";
	private static final InputControl INPUT_CONTROL_EE1 = new InputControl(ELECTION_EVENT_LABEL, "description", INPUT_CONTROL_URI_EE1, "text");
	private static final String COUNTRY_PARAMETER_NAME = "EE1.CO1";
	private static final String INPUT_CONTROL_ID_EE1_CO1 = COUNTRY_PARAMETER_NAME;
	private static final String INPUT_CONTROL_URI_EE1_CO1 = "/inputControls/" + INPUT_CONTROL_ID_EE1_CO1;
	private static final String COUNTRY_LABEL = "@reporting.report.parameter.label.EE1.CO1";
	private static final InputControl INPUT_CONTROL_EE1_CO1 = new InputControl(COUNTRY_LABEL, "Country description", INPUT_CONTROL_URI_EE1_CO1, "text");
	private static final String INPUT_CONTROL_ID_EE1_CO1_CNT1 = COUNTY_PARAMETER_NAME;
	private static final String INPUT_CONTROL_URI_EE1_CO1_CNT1 = "/inputControls/" + INPUT_CONTROL_ID_EE1_CO1_CNT1;
	private static final String COUNTY_LABEL = "@reporting.report.parameter.label.EE1.CO1.CNT1";
	private static final InputControl INPUT_CONTROL_EE1_CO1_CNT1 = new InputControl(COUNTY_LABEL, "County description", INPUT_CONTROL_URI_EE1_CO1_CNT1, "text");
	private static final String INPUT_CONTROL_ID_EE1_CO1_CNT1_MUN1 = MUNICIPALITY_PARAMETER_NAME;
	private static final String INPUT_CONTROL_URI_EE1_CO1_CNT1_MUN1 = "/inputControls/" + INPUT_CONTROL_ID_EE1_CO1_CNT1_MUN1;
	private static final String MUNICIPALITY_LABEL = "@reporting.report.parameter.label.EE1.CO1.CNT1.MUN1";
	private static final InputControl INPUT_CONTROL_EE1_CO1_CNT1_MUN1 = new InputControl(MUNICIPALITY_LABEL, "Municipality description",
			INPUT_CONTROL_URI_EE1_CO1_CNT1_MUN1, "text");
	private static final String INPUT_CONTROL_ID_EE1_CO1_CNT1_MUN1_BOR1 = BOROUGH_PARAMETER_NAME;
	private static final String INPUT_CONTROL_URI_EE1_CO1_CNT1_MUN1_BOR1 = "/inputControls/" + INPUT_CONTROL_ID_EE1_CO1_CNT1_MUN1_BOR1;
	private static final InputControl INPUT_CONTROL_EE1_CO1_CNT1_MUN1_BOR1 = new InputControl("Borough", "Borough description",
			INPUT_CONTROL_URI_EE1_CO1_CNT1_MUN1_BOR1, "text");
	private static final String POLLING_DISTRICT_PARAMETER_NAME = "EE1.CO1.CNT1.MUN1.BOR1.PD1";
	private static final String INPUT_CONTROL_ID_EE1_CO1_CNT1_MUN1_BOR1_PD1 = POLLING_DISTRICT_PARAMETER_NAME;
	private static final String INPUT_CONTROL_URI_EE1_CO1_CNT1_MUN1_BOR1_PD1 = "/inputControls/" + INPUT_CONTROL_ID_EE1_CO1_CNT1_MUN1_BOR1_PD1;
	private static final InputControl INPUT_CONTROL_EE1_CO1_CNT1_MUN1_BOR1_PD1 = new InputControl("Polling_district", "Polling_district description",
			INPUT_CONTROL_URI_EE1_CO1_CNT1_MUN1_BOR1_PD1, "text");
	private static final String INPUT_CONTROL_ID_EE1_CO1_CNT1_MUN1_BOR1_PD1_PP1 = POLLING_PLACE_PARAMETER_NAME;
	private static final String INPUT_CONTROL_URI_EE1_CO1_CNT1_MUN1_BOR1_PD1_PP1 = "/inputControls/" + INPUT_CONTROL_ID_EE1_CO1_CNT1_MUN1_BOR1_PD1_PP1;
	private static final InputControl INPUT_CONTROL_EE1_CO1_CNT1_MUN1_BOR1_PD1_PP1 = new InputControl("Polling_place", "Polling_place description",
			INPUT_CONTROL_URI_EE1_CO1_CNT1_MUN1_BOR1_PD1_PP1, "text");
	private static final String INPUT_CONTROL_ID_EE1_EG1 = ELECTION_GROUP_PARAMETER_NAME;
	private static final String INPUT_CONTROL_URI_EE1_EG1 = "/inputControls/" + INPUT_CONTROL_ID_EE1_EG1;
	private static final InputControl INPUT_CONTROL_EE1_EG1 = new InputControl("label_3", "description_3", INPUT_CONTROL_URI_EE1_EG1, "text");
	private static final String INPUT_CONTROL_ID_EE1_EG1_EL1 = ELECTION_PARAMETER_NAME;
	private static final String INPUT_CONTROL_URI_EE1_EG1_EL1 = "/inputControls/" + INPUT_CONTROL_ID_EE1_EG1_EL1;
	private static final InputControl INPUT_CONTROL_EE1_EG1_EL1 = new InputControl("label_3", "description_3", INPUT_CONTROL_URI_EE1_EG1_EL1, "text");
	private static final String INPUT_CONTROL_ID_EE1_EG1_EL1_CT1 = CONTEST_PARAMETER_NAME;
	private static final String INPUT_CONTROL_ID_EE1_EG1_EL1_CT1_MATCH_TO_AREA_LEVEL = CONTEST_PK_PARAMETER;
	private static final String INPUT_CONTROL_ID_EE1_EG1_EL1_CT1_MATCH_TO_TWO_AREA_LEVELS = CONTEST_PK_FOR_COUNTY_OR_MUNICIPALITY_PARAMETER;
	private static final String INPUT_CONTROL_URI_EE1_EG1_EL1_CT1 = "/inputControls/" + INPUT_CONTROL_ID_EE1_EG1_EL1_CT1;
	private static final String INPUT_CONTROL_URI_EE1_EG1_EL1_CT1_MATCH_TO_AREA_LEVEL = "/inputControls/" + INPUT_CONTROL_ID_EE1_EG1_EL1_CT1_MATCH_TO_AREA_LEVEL;
	private static final String INPUT_CONTROL_URI_EE1_EG1_EL1_CT1_MATCH_TO_COUNTY_AND_MUN_AREA_LEVELS = "/inputControls/"
			+ INPUT_CONTROL_ID_EE1_EG1_EL1_CT1_MATCH_TO_TWO_AREA_LEVELS;
	private static final InputControl INPUT_CONTROL_EE1_EG1_EL1_CT1 = new InputControl("label_3", "description_3", INPUT_CONTROL_URI_EE1_EG1_EL1_CT1, "text");
	private static final InputControl INPUT_CONTROL_EE1_EG1_EL1_CT1_FOR_CNT_OR_MUN_ONLY_PK = new InputControl("label_5", "description_5",
			INPUT_CONTROL_URI_EE1_EG1_EL1_CT1_MATCH_TO_COUNTY_AND_MUN_AREA_LEVELS, "text");
	private static final InputControl INPUT_CONTROL_EE1_EG1_EL1_CT1_PK = new InputControl("label_4", "description_4",
			INPUT_CONTROL_URI_EE1_EG1_EL1_CT1_MATCH_TO_AREA_LEVEL, "text");
	private static final String INPUT_CONTROL_ID_PROTOCOL_FROM_INT = "protocol_from_int";
	private static final String INPUT_CONTROL_URI_PROTOCOL_FROM_INT = "/inputControls/" + INPUT_CONTROL_ID_PROTOCOL_FROM_INT;
	private static final String PROTOCOL_FROM_INT_LABEL = "@reporting.report.parameter.label.protocol_from_int";
	private static final InputControl INPUT_CONTROL_PROTOCOL_FROM_INT =
			new InputControl(PROTOCOL_FROM_INT_LABEL, "description", INPUT_CONTROL_URI_PROTOCOL_FROM_INT, "number");
	private static final List<InputControl> INPUT_CONTROLS_FOR_REPORT_1 = newArrayList(INPUT_CONTROL_EE1, INPUT_CONTROL_EE1_CO1,
			INPUT_CONTROL_EE1_CO1_CNT1, INPUT_CONTROL_EE1_CO1_CNT1_MUN1);
	private static final List<InputControl> INPUT_CONTROLS_FOR_REPORT_2 = newArrayList(INPUT_CONTROL_EE1, INPUT_CONTROL_EE1_EG1, INPUT_CONTROL_EE1_EG1_EL1,
			INPUT_CONTROL_EE1_EG1_EL1_CT1);
	private static final List<InputControl> INPUT_CONTROLS_FOR_CONTEST_PK = newArrayList(INPUT_CONTROL_EE1, INPUT_CONTROL_EE1_EG1_EL1_CT1_PK);
	private static final List<InputControl> INPUT_CONTROLS_FOR_CONTEST_PK_COUNTY_OR_MUN = newArrayList(INPUT_CONTROL_EE1,
			INPUT_CONTROL_EE1_EG1_EL1_CT1_FOR_CNT_OR_MUN_ONLY_PK);
	private static final List<InputControl> INPUT_CONTROLS_FOR_REPORT_10 = newArrayList(INPUT_CONTROL_EE1, INPUT_CONTROL_EE1_CO1,
			INPUT_CONTROL_EE1_CO1_CNT1, INPUT_CONTROL_EE1_CO1_CNT1_MUN1, INPUT_CONTROL_EE1_CO1_CNT1_MUN1_BOR1, INPUT_CONTROL_EE1_CO1_CNT1_MUN1_BOR1_PD1);
	private static final List<InputControl> INPUT_CONTROLS_FOR_REPORT_11 = newArrayList(INPUT_CONTROL_PROTOCOL_FROM_INT);
	private static final String REPORT_LABEL_1 = "Report label 1";
	private static final String REPORT_LABEL_2 = "Report label 2";
	private static final String REPORT_LABEL_3 = "Report label 3";
	private static final String REPORT_LABEL_4 = "Report label 4";
	private static final String REPORT_LABEL_6 = "Report label 6";
	private static final String REPORT_LABEL_INACCESSIBLE = "Report label for inaccessible report";
	private static final String REPORT_LABEL_HIDDEN = "Report label for hidden report";
	private static final String REPORT_LABEL_PREGENERATED = "preGenerated";
	private static final String REPORT_LABEL_ASYNC = "async";
	private static final String REPORT_LABEL_9 = "Report label 9";
	private static final String REPORT_LABEL_10 = "Report with polling district parameters";
	private static final String REPORT_LABEL_11 = "Report with integer parameters";
	private static final String ELECTION_EVENT_ID = "200701";
	private static final String COUNTRY_ID = "47";
	private static final String COUNTY_ID = "03";
	private static final String MUNICIPALITY_ID = AreaPath.OSLO_MUNICIPALITY_ID;
	private static final String PDF = "pdf";
	private static final String NN_NO = "nn_NO";
	private static final String REPORT_LOCALE = NN_NO + "_" + ELECTION_EVENT_ID;

	private static final Map<String, String> PROTOCOL_FROM_INT_INVALID_ARGUMENT = ImmutableMap.of(PROTOCOL_FROM_INT_PARAMETER_NAME, "FOO");
	private static final Map<String, String> MUNICIPALITY_PATH_ARGUMENTS = ImmutableMap.of(
			ELECTION_EVENT_PARAMETER_NAME, ELECTION_EVENT_ID,
			COUNTRY_PARAMETER_NAME, COUNTRY_ID,
			COUNTY_PARAMETER_NAME, COUNTY_ID,
			MUNICIPALITY_PARAMETER_NAME, MUNICIPALITY_ID);
	public static final ImmutableMap<String, String> PATH_ARGUMENTS_LABELS = ImmutableMap.of(
			ELECTION_EVENT_PARAMETER_NAME, ELECTION_EVENT_LABEL,
			COUNTRY_PARAMETER_NAME, COUNTRY_LABEL,
			COUNTY_PARAMETER_NAME, COUNTY_LABEL,
			MUNICIPALITY_PARAMETER_NAME, MUNICIPALITY_LABEL);
	private static final String META_DATA_URI_FOR_MUNICIPALITY_REPORT = "metaDataUri_1";
	private static final String META_DATA_URI_FOR_OSLO_ONLY_REPORT = "metaDataUri_oslo_only";
	private static final String META_DATA_URI_FOR_COUNTY_REPORT = "metaDataUri_2";
	private static final String META_DATA_URI_FOR_INACCESSIBLE_BY_CATEGORY_REPORT = "metaDataUri_3";
	private static final String META_DATA_URI_FOR_HIDDEN_REPORT = "metaDataUri_6";
	private static final String META_DATA_URI_FOR_DUAL_AREA_LEVEL = "metaDataUri_4";
	private static final String META_DATA_URI_FOR_MATCH_TO_AREA_LEVEL = "metaDataUri_5";
	private static final String META_DATA_URI_FOR_PREGENERATED_REPORT = "metaDataUri_7";
	private static final String META_DATA_URI_FOR_ASYNC_REPORT = "metaDataUri_8";
	private static final String META_DATA_URI_FOR_MATCH_CONTEST_PK_TO_TWO_AREA_LEVELS = "metaDataUri_contest_pk_two_area_levels";
	private static final String META_DATA_URI_FOR_REPORT_9 = "metaDataUri_9";
	private static final String META_DATA_URI_FOR_REPORT_10 = "metaDataUri_10";
	private static final String META_DATA_URI_FOR_REPORT_11 = "metaDataUri_11";

	private static final String DESCRIPTION = "Report description";
	private static final String REGEXP_OSLO = "\\d{6}\\.47\\.\\d{2}\\.0301"; // matches e.g. 950004.47.03.0301, which is municipality of Oslo
	private static final JasperReport JASPER_REPORT_DUAL_AREA_LEVEL = new JasperReport(REPORT_LABEL_4, REPORT_URI_DUAL_AREA_LEVEL, DESCRIPTION,
			newArrayList(new FileResource("metaData",
					new FileReference(META_DATA_URI_FOR_DUAL_AREA_LEVEL))),
			INPUT_CONTROLS_FOR_REPORT_1);
	private static final JasperReport JASPER_REPORT_MUNICIPALITY = new JasperReport(REPORT_LABEL_1, REPORT_URI_MUNICIPALITY, DESCRIPTION,
			newArrayList(new FileResource("metaData",
					new FileReference(META_DATA_URI_FOR_MUNICIPALITY_REPORT))),
			INPUT_CONTROLS_FOR_REPORT_1);
	private static final JasperReport JASPER_REPORT_FOR_OSLO_ONLY = new JasperReport(REPORT_LABEL_3, REPORT_URI_OSLO_ONLY, DESCRIPTION,
			newArrayList(new FileResource("metaData",
					new FileReference(META_DATA_URI_FOR_OSLO_ONLY_REPORT))),
			INPUT_CONTROLS_FOR_REPORT_1);
	private static final JasperReport JASPER_REPORT_COUNTY = new JasperReport(REPORT_LABEL_2, REPORT_URI_COUNTY, DESCRIPTION, newArrayList(new FileResource(
			"metaData",
			new FileReference(META_DATA_URI_FOR_COUNTY_REPORT))), INPUT_CONTROLS_FOR_REPORT_2);
	private static final JasperReport JASPER_REPORT_CONTEST_PK = new JasperReport(REPORT_LABEL_2, REPORT_URI_CONTEST_PK, DESCRIPTION,
			newArrayList(new FileResource("metaData",
					new FileReference(META_DATA_URI_FOR_MATCH_TO_AREA_LEVEL))),
			INPUT_CONTROLS_FOR_CONTEST_PK);
	private static final JasperReport JASPER_REPORT_INACCESSIBLE_CATEGORY = new JasperReport(REPORT_LABEL_INACCESSIBLE, REPORT_URI_INACCESSIBLE_CATEGORY,
			DESCRIPTION,
			newArrayList(new FileResource("metaData",
					new FileReference(META_DATA_URI_FOR_INACCESSIBLE_BY_CATEGORY_REPORT))),
			newArrayList(INPUT_CONTROLS_FOR_REPORT_1));
	private static final JasperReport JASPER_REPORT_HIDDEN = new JasperReport(REPORT_LABEL_HIDDEN, REPORT_URI_HIDDEN,
			DESCRIPTION,
			newArrayList(new FileResource("metaData",
					new FileReference(META_DATA_URI_FOR_HIDDEN_REPORT))),
			newArrayList(INPUT_CONTROLS_FOR_REPORT_1));
	private static final JasperReport JASPER_REPORT_PREGENERATED = new JasperReport(REPORT_LABEL_PREGENERATED, REPORT_URI_PREGENERATED,
			DESCRIPTION,
			newArrayList(new FileResource("metaData",
					new FileReference(META_DATA_URI_FOR_PREGENERATED_REPORT))),
			newArrayList(INPUT_CONTROLS_FOR_REPORT_1));
	private static final JasperReport JASPER_REPORT_ASYNC = new JasperReport(REPORT_LABEL_ASYNC, REPORT_URI_ASYNC,
			DESCRIPTION,
			newArrayList(new FileResource("metaData",
					new FileReference(META_DATA_URI_FOR_ASYNC_REPORT))),
			newArrayList(INPUT_CONTROLS_FOR_REPORT_1));
	private static final JasperReport JASPER_REPORT_9 = new JasperReport(REPORT_LABEL_9, REPORT_URI_9,
			DESCRIPTION,
			newArrayList(new FileResource("metaData",
					new FileReference(META_DATA_URI_FOR_REPORT_9))),
			newArrayList(INPUT_CONTROLS_FOR_REPORT_2));
	private static final JasperReport JASPER_REPORT_10 = new JasperReport(REPORT_LABEL_10, REPORT_URI_10,
			DESCRIPTION,
			newArrayList(new FileResource("metaData",
					new FileReference(META_DATA_URI_FOR_REPORT_10))),
			newArrayList(INPUT_CONTROLS_FOR_REPORT_10));
	private static final JasperReport JASPER_REPORT_11 = new JasperReport(REPORT_LABEL_11, REPORT_URI_11,
			DESCRIPTION,
			newArrayList(new FileResource("metaData",
					new FileReference(META_DATA_URI_FOR_REPORT_11))),
			newArrayList(INPUT_CONTROLS_FOR_REPORT_11));

	private static final String[] AREA_PATH_PARAMETERS = {
			ELECTION_EVENT_PARAMETER_NAME, COUNTRY_PARAMETER_NAME, COUNTY_PARAMETER_NAME, MUNICIPALITY_PARAMETER_NAME, BOROUGH_PARAMETER_NAME,
			POLLING_DISTRICT_PARAMETER_NAME, POLLING_PLACE_PARAMETER_NAME
	};
	private static final String[] ELECTION_PATH_PARAMETERS = {
			ELECTION_EVENT_PARAMETER_NAME, ELECTION_GROUP_PARAMETER_NAME, ELECTION_PARAMETER_NAME, CONTEST_PARAMETER_NAME
	};
	private static final JasperReport JASPER_REPORT_CONTEST_PK_COUNTY_OR_MUN = new JasperReport(REPORT_LABEL_6, REPORT_URI_CONTEST_COUNTY_OR_MUNICIPALITY_PK,
			DESCRIPTION,
			newArrayList(new FileResource("metaData",
					new FileReference(META_DATA_URI_FOR_MATCH_CONTEST_PK_TO_TWO_AREA_LEVELS))),
			INPUT_CONTROLS_FOR_CONTEST_PK_COUNTY_OR_MUN);

	public static final Function<ReportParameter, String> PARAMETER_TO_ID = new Function<ReportParameter, String>() {
		@Override
		public String apply(ReportParameter input) {
			return input.getId();
		}
	};
	private static final String ROLE_MV_AREA_REG_EXP_OSLO_COUNT_AND_BELOW = "\\d{6}\\.47\\.03[\\d\\.]*$";
	private static final String COUNTY_ELECTION_ID = "01";
	private static final String MUNICIPALITY_ELECTION_ID = "02";
	private static final String BOROUGH_ELECTION_ID = "03";
	private static final String STORTING_ELECTION_ID = "01";
	private static final Object OSLO_COUNTY_ID = "03";
	private static final String VESTFOLD_COUNTY_ID = "05";
	private static final String FINNMARK_COUNTY_ID = "20";

	private static final String POLLING_DISTRICT_2_ID = "0002";
	private static final String POLLING_DISTRICT_1_ID = "0001";
	private static final String POLLING_DISTRICT_2_NAME = "Technical polling district";
	private static final String POLLING_DISTRICT_1_NAME = "Ordinary polling district";

	private JasperReportServiceBean jasperReportService;

	@Mock
	private ElectionEvent mockElectionEvent;

	@Mock
	private Country mockCountry;

	@Mock
	private County mockCounty;

	@Mock
	private Municipality mockMunicipality;

	@Mock
	private Borough mockBorough;

	@Mock
	private PollingDistrict mockPollingDistrict;

	@Mock
	private PollingPlace mockPollingPlace;

	@Mock
	private ElectionGroup mockElectionGroup;

	@Mock
	private Election mockCountyElection;

	@Mock
	private Election mockMunicipalityElection;

	@Mock
	private Election mockBoroughElection;

	@Mock
	private MvElection mockMvElectionForCounty;

	@Mock
	private MvElection mockMvElectionForMunicipality;

	@Mock
	private MvElection mockMvElectionForBorough;

	@Mock
	private Contest mockMunicipalityLevelContest;

	@Mock
	private Contest mockMunicipalityLevelContestHammerfest;

	@Mock
	private Contest mockBoroughLevelContest;

	@Mock
	private Contest mockCountyLevelContest;

	@Mock
	private Contest mockCountyLevelContestStortingOslo;

	@Mock
	private ContestArea mockMunicipalityContestArea;

	@Mock
	private ContestArea mockBoroughContestArea;

	@Mock
	private ContestArea mockCountyContestArea;

	@Mock
	private MvArea mockMvArea;

	@Mock
	private Response mockAsyncReportNotReadyResponse;

	@Mock
	private Response mockAsyncReportFinishedResponse;

	private PregeneratedContentRetriever pregeneratedContentRetriever;

	@BeforeSuite
	public void init() {
		initMocks(this);
	}

	@BeforeMethod
	public void setUp() throws Exception {
		jasperReportService = initializeMocks(new JasperReportServiceBean());
		setupMockForFolders();
		setupMockForInputControls();
		setupMockForReportExecution();
		setupMockForPreGeneratedReport();
		setupMockForMetaData();
		setupMockForServices();
		setupMockForReportTemplates();
		no.valg.eva.admin.configuration.domain.model.Locale locale = new no.valg.eva.admin.configuration.domain.model.Locale();
		locale.setId("nn-NO");
		when(mockMunicipality.getLocale()).thenReturn(locale);
		when(mockMvArea.getMunicipality()).thenReturn(mockMunicipality);
		when(mockCountyElection.getId()).thenReturn("01");
		when(mockCountyElection.electionPath()).thenReturn(ElectionPath.from("111111.11.01"));
		when(mockMunicipalityElection.getId()).thenReturn("02");
		when(mockMunicipalityElection.electionPath()).thenReturn(ElectionPath.from("111111.11.02"));
		when(mockBoroughElection.getId()).thenReturn("03");
		when(mockBoroughElection.electionPath()).thenReturn(ElectionPath.from("111111.11.03"));
	}

	private void setupMockForPreGeneratedReport() throws Exception {
		pregeneratedContentRetriever = mock(PregeneratedContentRetriever.class);
		when(pregeneratedContentRetriever.getRepositoryType()).thenReturn("jasperserver");
		when(pregeneratedContentRetriever.tryPreGeneratedReport(anyString())).thenReturn(PDF_CONTENT);
		Instance mock = mock(Instance.class);
		when(mock.iterator()).thenReturn(newArrayList(pregeneratedContentRetriever).iterator());
		mockFieldValue("pregeneratedContentRetrievers", mock);
	}

	private void setupMockForReportTemplates() {
		when(getInjectMock(JasperRestApiWithTimeout.class)
				.getResources(eq(JasperRestApi.ResourceType.reportUnit), anyString()))
						.thenReturn(new JasperResources(Lists.newArrayList(
								JASPER_REPORT_MUNICIPALITY,
								JASPER_REPORT_COUNTY,
								JASPER_REPORT_CONTEST_PK,
								JASPER_REPORT_INACCESSIBLE_CATEGORY,
								JASPER_REPORT_FOR_OSLO_ONLY,
								JASPER_REPORT_DUAL_AREA_LEVEL,
								JASPER_REPORT_HIDDEN,
								JASPER_REPORT_PREGENERATED,
								JASPER_REPORT_ASYNC,
								JASPER_REPORT_CONTEST_PK_COUNTY_OR_MUN,
								JASPER_REPORT_9,
								JASPER_REPORT_10,
								JASPER_REPORT_11)));
		when(getInjectMock(JasperRestApiWithTimeout.class)
				.getJasperReportUnit(REPORT_URI_MUNICIPALITY))
						.thenReturn(JASPER_REPORT_MUNICIPALITY);
		when(getInjectMock(JasperRestApiWithTimeout.class)
				.getJasperReportUnit(REPORT_URI_COUNTY))
						.thenReturn(JASPER_REPORT_COUNTY);
		when(getInjectMock(JasperRestApiWithTimeout.class)
				.getJasperReportUnit(REPORT_URI_INACCESSIBLE_CATEGORY))
						.thenReturn(JASPER_REPORT_INACCESSIBLE_CATEGORY);
		when(getInjectMock(JasperRestApiWithTimeout.class)
				.getJasperReportUnit(REPORT_URI_OSLO_ONLY))
						.thenReturn(JASPER_REPORT_FOR_OSLO_ONLY);
		when(getInjectMock(JasperRestApiWithTimeout.class)
				.getJasperReportUnit(REPORT_URI_DUAL_AREA_LEVEL))
						.thenReturn(JASPER_REPORT_DUAL_AREA_LEVEL);
		when(getInjectMock(JasperRestApiWithTimeout.class)
				.getJasperReportUnit(REPORT_URI_CONTEST_PK))
						.thenReturn(JASPER_REPORT_CONTEST_PK);
		when(getInjectMock(JasperRestApiWithTimeout.class)
				.getJasperReportUnit(REPORT_URI_HIDDEN))
						.thenReturn(JASPER_REPORT_HIDDEN);
		when(getInjectMock(JasperRestApiWithTimeout.class)
				.getJasperReportUnit(REPORT_URI_PREGENERATED))
						.thenReturn(JASPER_REPORT_PREGENERATED);
		when(getInjectMock(JasperRestApiWithTimeout.class)
				.getJasperReportUnit(REPORT_URI_ASYNC))
						.thenReturn(JASPER_REPORT_ASYNC);
		when(getInjectMock(JasperRestApiWithTimeout.class)
				.getJasperReportUnit(REPORT_URI_CONTEST_COUNTY_OR_MUNICIPALITY_PK))
						.thenReturn(JASPER_REPORT_CONTEST_PK_COUNTY_OR_MUN);
		when(getInjectMock(JasperRestApiWithTimeout.class)
				.getJasperReportUnit(REPORT_URI_9))
						.thenReturn(JASPER_REPORT_9);
		when(getInjectMock(JasperRestApiWithTimeout.class)
				.getJasperReportUnit(REPORT_URI_10))
						.thenReturn(JASPER_REPORT_10);
		when(getInjectMock(JasperRestApiWithTimeout.class)
				.getJasperReportUnit(REPORT_URI_11))
						.thenReturn(JASPER_REPORT_11);
	}

	private void setupMockForMetaData() {
		ReportMetaData reportMetaDataForMunicipalityReport = new ReportMetaData();
		reportMetaDataForMunicipalityReport.setFilenamePattern(FILENAME_PATTERN);
		reportMetaDataForMunicipalityReport.setReportName(REPORT_1_NAME);
		reportMetaDataForMunicipalityReport.setDescription(DESCRIPTION);
		reportMetaDataForMunicipalityReport.setAreaLevels(newArrayList(ReportMetaData.AreaLevel.MUNICIPALITY));
		when(getInjectMock(JasperRestApiWithTimeout.class).getReportMetaData(META_DATA_URI_FOR_MUNICIPALITY_REPORT)).thenReturn(
				reportMetaDataForMunicipalityReport);

		ReportMetaData reportMetaDataForReportCountyReport = new ReportMetaData();
		reportMetaDataForReportCountyReport.setFilenamePattern(FILENAME_PATTERN);
		reportMetaDataForReportCountyReport.setReportName(REPORT_1_NAME);
		reportMetaDataForReportCountyReport.setDescription(DESCRIPTION);
		reportMetaDataForReportCountyReport.setAreaLevels(newArrayList(ReportMetaData.AreaLevel.COUNTY));
		when(getInjectMock(JasperRestApiWithTimeout.class).getReportMetaData(META_DATA_URI_FOR_COUNTY_REPORT)).thenReturn(reportMetaDataForReportCountyReport);

		ReportMetaData reportMetaDataForReportInaccessibleByCategoryReport = new ReportMetaData();
		reportMetaDataForReportInaccessibleByCategoryReport.setFilenamePattern(FILENAME_PATTERN);
		reportMetaDataForReportInaccessibleByCategoryReport.setReportName(REPORT_1_NAME);
		reportMetaDataForReportInaccessibleByCategoryReport.setDescription(DESCRIPTION);
		when(getInjectMock(JasperRestApiWithTimeout.class).getReportMetaData(META_DATA_URI_FOR_INACCESSIBLE_BY_CATEGORY_REPORT)).thenReturn(
				reportMetaDataForReportInaccessibleByCategoryReport);

		ReportMetaData reportMetaDataForReportOsloOnlyReport = new ReportMetaData();
		reportMetaDataForReportOsloOnlyReport.setFilenamePattern(FILENAME_PATTERN);
		reportMetaDataForReportOsloOnlyReport.setReportName(REPORT_3_NAME);
		reportMetaDataForReportOsloOnlyReport.setDescription(DESCRIPTION);
		reportMetaDataForReportOsloOnlyReport.setAreaPathMask(REGEXP_OSLO);
		when(getInjectMock(JasperRestApiWithTimeout.class).getReportMetaData(META_DATA_URI_FOR_OSLO_ONLY_REPORT)).thenReturn(
				reportMetaDataForReportOsloOnlyReport);

		ReportMetaData reportMetaDataForReportDualAreaLevelReport = new ReportMetaData();
		reportMetaDataForReportDualAreaLevelReport.setFilenamePattern(FILENAME_PATTERN);
		reportMetaDataForReportDualAreaLevelReport.setReportName(REPORT_4_NAME);
		reportMetaDataForReportDualAreaLevelReport.setDescription(DESCRIPTION);
		reportMetaDataForReportDualAreaLevelReport.setAreaLevels(newArrayList(ReportMetaData.AreaLevel.COUNTY, ReportMetaData.AreaLevel.MUNICIPALITY));
		when(getInjectMock(JasperRestApiWithTimeout.class).getReportMetaData(META_DATA_URI_FOR_DUAL_AREA_LEVEL)).thenReturn(
				reportMetaDataForReportDualAreaLevelReport);

		ReportMetaData reportMetaDataForReportMatchedToAreaLevelReport = new ReportMetaData();
		reportMetaDataForReportMatchedToAreaLevelReport.setFilenamePattern(FILENAME_PATTERN);
		reportMetaDataForReportMatchedToAreaLevelReport.setReportName(REPORT_5_NAME);
		reportMetaDataForReportMatchedToAreaLevelReport.setDescription(DESCRIPTION);
		when(getInjectMock(JasperRestApiWithTimeout.class).getReportMetaData(META_DATA_URI_FOR_MATCH_TO_AREA_LEVEL)).thenReturn(
				reportMetaDataForReportMatchedToAreaLevelReport);

		ReportMetaData reportMetaDataForHiddenReport = new ReportMetaData();
		reportMetaDataForHiddenReport.setFilenamePattern(FILENAME_PATTERN);
		reportMetaDataForHiddenReport.setReportName(REPORT_HIDDEN_NAME);
		reportMetaDataForHiddenReport.setDescription(DESCRIPTION);
		reportMetaDataForHiddenReport.setHidden(true);
		when(getInjectMock(JasperRestApiWithTimeout.class).getReportMetaData(META_DATA_URI_FOR_HIDDEN_REPORT)).thenReturn(
				reportMetaDataForHiddenReport);

		ReportMetaData reportMetaDataForPregeneratedReport = new ReportMetaData();
		reportMetaDataForPregeneratedReport.setFilenamePattern(FILENAME_PATTERN);
		reportMetaDataForPregeneratedReport.setReportName(REPORT_PREGENERATED_NAME);
		reportMetaDataForPregeneratedReport.setDescription(DESCRIPTION);
		reportMetaDataForPregeneratedReport.setRunNightly(true);
		reportMetaDataForPregeneratedReport.setAreaLevels(newArrayList(ReportMetaData.AreaLevel.MUNICIPALITY));
		when(getInjectMock(JasperRestApiWithTimeout.class).getReportMetaData(META_DATA_URI_FOR_PREGENERATED_REPORT)).thenReturn(
				reportMetaDataForPregeneratedReport);

		ReportMetaData reportMetaDataForAsyncReport = new ReportMetaData();
		reportMetaDataForAsyncReport.setFilenamePattern(FILENAME_PATTERN);
		reportMetaDataForAsyncReport.setReportName(REPORT_ASYNC_NAME);
		reportMetaDataForAsyncReport.setDescription(DESCRIPTION);
		reportMetaDataForAsyncReport.setAsync(true);
		reportMetaDataForAsyncReport.setAreaLevels(newArrayList(ReportMetaData.AreaLevel.MUNICIPALITY));
		when(getInjectMock(JasperRestApiWithTimeout.class).getReportMetaData(META_DATA_URI_FOR_ASYNC_REPORT)).thenReturn(
				reportMetaDataForAsyncReport);

		ReportMetaData reportMetaDataForReportMatchedToCountyAndMunicipalityAreaLevelReport = new ReportMetaData();
		reportMetaDataForReportMatchedToCountyAndMunicipalityAreaLevelReport.setFilenamePattern(FILENAME_PATTERN);
		reportMetaDataForReportMatchedToCountyAndMunicipalityAreaLevelReport.setReportName(REPORT_5_NAME);
		reportMetaDataForReportMatchedToCountyAndMunicipalityAreaLevelReport.setDescription(DESCRIPTION);
		reportMetaDataForReportMatchedToCountyAndMunicipalityAreaLevelReport.setUnselectableParameterValues(
				newArrayList(new UnselectableParameterValue("EE1.EG1.EL1.CT1_FOR_CNT_OR_MUN_ONLY_PK", valueOf(BOROUGH_LEVEL_CONTEST_PK))));
		when(getInjectMock(JasperRestApiWithTimeout.class).getReportMetaData(META_DATA_URI_FOR_MATCH_CONTEST_PK_TO_TWO_AREA_LEVELS))
				.thenReturn(reportMetaDataForReportMatchedToCountyAndMunicipalityAreaLevelReport);

		ReportMetaData reportMetaDataForReport9 = new ReportMetaData();
		reportMetaDataForReport9.setFilenamePattern(FILENAME_PATTERN);
		reportMetaDataForReport9.setReportName(REPORT_9_NAME);
		reportMetaDataForReport9.setDescription(DESCRIPTION);
		reportMetaDataForReport9.setUnselectableParameterValues(
				newArrayList(
						// new UnselectableParameterValue(ELECTION_PARAMETER_NAME, COUNTY_ELECTION_ID, ROLE_MV_AREA_REG_EXP_OSLO_COUNT_AND_BELOW),
						new UnselectableParameterValue(ELECTION_PARAMETER_NAME, BOROUGH_ELECTION_ID)));
		when(getInjectMock(JasperRestApiWithTimeout.class).getReportMetaData(META_DATA_URI_FOR_REPORT_9))
				.thenReturn(reportMetaDataForReport9);

		ReportMetaData reportMetaDataForReport10 = new ReportMetaData();
		reportMetaDataForReport10.setFilenamePattern(FILENAME_PATTERN);
		reportMetaDataForReport10.setReportName(REPORT_10_NAME);
		reportMetaDataForReport10.setDescription(DESCRIPTION);
		when(getInjectMock(JasperRestApiWithTimeout.class).getReportMetaData(META_DATA_URI_FOR_REPORT_10))
				.thenReturn(reportMetaDataForReport10);

		ReportMetaData reportMetaDataForReport11 = new ReportMetaData();
		reportMetaDataForReport11.setFilenamePattern(FILENAME_PATTERN);
		reportMetaDataForReport11.setReportName(REPORT_11_NAME);
		reportMetaDataForReport11.setDescription(DESCRIPTION);
		when(getInjectMock(JasperRestApiWithTimeout.class).getReportMetaData(META_DATA_URI_FOR_REPORT_11)).thenReturn(reportMetaDataForReport11);
	}

	private void setupMockForReportExecution() {
		when(mockAsyncReportNotReadyResponse.getStatus()).thenReturn(200);
		ReportExecutionStatus unfinishedReportExecutionStatus = new ReportExecutionStatus();
		unfinishedReportExecutionStatus.setValue(EXECUTION);
		when(mockAsyncReportNotReadyResponse.readEntity(ReportExecutionStatus.class)).thenReturn(unfinishedReportExecutionStatus);
		when(mockAsyncReportNotReadyResponse.getHeaderString("Content-Type")).thenReturn("application/status+xml");

		when(mockAsyncReportFinishedResponse.getStatus()).thenReturn(200);
		when(mockAsyncReportFinishedResponse.getHeaderString("Content-Type")).thenReturn("application/status+xml");
		ReportExecutionStatus finishedReportExecutionStatus = new ReportExecutionStatus();
		finishedReportExecutionStatus.setValue(READY);
		when(mockAsyncReportFinishedResponse.readEntity(ReportExecutionStatus.class)).thenReturn(finishedReportExecutionStatus);

		when(getInjectMock(JasperRestApiNoTimeout.class).getReportExecutionStatus(anyString())).thenReturn(
				mockAsyncReportNotReadyResponse,
				mockAsyncReportNotReadyResponse,
				mockAsyncReportNotReadyResponse,
				mockAsyncReportFinishedResponse);

		JasperExecution unfinishedAsyncReportOutput = new JasperExecution(1, REPORT_URI_ASYNC, REQUEST_ID, "execution",
				newArrayList(new JasperExecution.Export(
						PDF, "queued")));

		when(getInjectMock(JasperRestApiNoTimeout.class)
				.executeReport(argThat(jasperExecutionRequestMatcher(REPORT_URI_ASYNC, true)), anyString()))
						.thenReturn(unfinishedAsyncReportOutput);

		when(getInjectMock(JasperRestApiNoTimeout.class)
				.executeReport(argThat(jasperExecutionRequestMatcher(REPORT_URI_ASYNC, false)), anyString()))
						.thenReturn(
								new JasperExecution(1, REPORT_URI_MUNICIPALITY, REQUEST_ID, "ready", newArrayList(new JasperExecution.Export(PDF, "ready"))));

		Response outputResponse = mock(Response.class);
		when(outputResponse.getMediaType()).thenReturn(new MediaType("application", PDF));
		when(outputResponse.readEntity(byte[].class)).thenReturn(PDF_CONTENT);
		when(outputResponse.getStatus()).thenReturn(200);
		when(getInjectMock(JasperRestApiNoTimeout.class)
				.getReportOutput(eq(REQUEST_ID), eq(PDF), anyString()))
						.thenReturn(outputResponse);
	}

	private ArgumentMatcher<JasperExecutionRequest> jasperExecutionRequestMatcher(final String uriExpr, final boolean nonInvert) {
		return new ArgumentMatcher<JasperExecutionRequest>() {
			@Override
			public boolean matches(JasperExecutionRequest argument) {
				if (argument != null) {
					boolean matches = uriExpr.matches(argument.getReportUnitUri());
					return (nonInvert == matches);
				} else {
					return false;
				}
			}
		};
	}

	private void setupMockForServices() {
		when(getInjectMock(ElectionGroupRepository.class).getElectionGroupsSorted(anyLong()))
				.thenReturn(singletonList(mockElectionGroup));
		when(getInjectMock(ElectionGroupRepository.class).findElectionGroupById(anyLong(), anyString())).thenReturn(mockElectionGroup);

		when(getInjectMock(ElectionRepository.class).findElectionsByElectionGroup(anyLong())).thenReturn(singletonList(mockCountyElection));
		when(getInjectMock(ElectionRepository.class).findElectionByElectionGroupAndId(anyLong(), anyString())).thenReturn(mockCountyElection);

		// when(getInjectMock(ContestRepository.class).findByElectionPk(anyLong())).thenReturn(asList(mockMunicipalityLevelContest, mockCountyLevelContest));

		when(mockCountyContestArea.getActualAreaLevel()).thenReturn(COUNTY);
		when(mockCountyLevelContest.getContestAreaList()).thenReturn(singletonList(mockCountyContestArea));
		when(mockCountyLevelContest.getPk()).thenReturn(COUNTY_LEVEL_CONTEST_PK);
		when(mockCountyLevelContest.getElection()).thenReturn(mockCountyElection);
		when(mockCountyLevelContest.getFirstContestArea()).thenReturn(mockCountyContestArea);
		when(mockCountyLevelContest.getId()).thenReturn("000020");

		when(mockCountyLevelContestStortingOslo.getContestAreaList()).thenReturn(singletonList(mockCountyContestArea));
		when(mockCountyLevelContestStortingOslo.getPk()).thenReturn(COUNTY_LEVEL_OSLO_STORTING_CONTEST_PK);
		when(mockCountyLevelContestStortingOslo.getElection()).thenReturn(mockCountyElection);
		when(mockCountyLevelContestStortingOslo.getFirstContestArea()).thenReturn(mockCountyContestArea);
		when(mockCountyLevelContestStortingOslo.getId()).thenReturn("000003");

		when(mockMunicipalityContestArea.getActualAreaLevel()).thenReturn(MUNICIPALITY);
		when(mockMunicipalityLevelContest.getContestAreaList()).thenReturn(singletonList(mockMunicipalityContestArea));
		when(mockMunicipalityLevelContest.getPk()).thenReturn(MUNICIPALITY_LEVEL_CONTEST_PK);
		when(mockMunicipalityLevelContest.getElection()).thenReturn(mockCountyElection);
		when(mockMunicipalityLevelContest.getFirstContestArea()).thenReturn(mockMunicipalityContestArea);
		when(mockMunicipalityLevelContest.getId()).thenReturn("000301");

		when(mockMunicipalityLevelContestHammerfest.getContestAreaList()).thenReturn(singletonList(mockMunicipalityContestArea));
		when(mockMunicipalityLevelContestHammerfest.getPk()).thenReturn(MUNICIPALITY_LEVEL_CONTEST_HAMMERFEST_PK);
		when(mockMunicipalityLevelContestHammerfest.getElection()).thenReturn(mockCountyElection);
		when(mockMunicipalityLevelContestHammerfest.getFirstContestArea()).thenReturn(mockMunicipalityContestArea);
		when(mockMunicipalityLevelContestHammerfest.getId()).thenReturn("002004");

		when(mockBoroughContestArea.getActualAreaLevel()).thenReturn(BOROUGH);
		when(mockBoroughLevelContest.getContestAreaList()).thenReturn(singletonList(mockBoroughContestArea));
		when(mockBoroughLevelContest.getPk()).thenReturn(BOROUGH_LEVEL_CONTEST_PK);
		when(mockBoroughLevelContest.getElection()).thenReturn(mockCountyElection);
		when(mockBoroughLevelContest.getFirstContestArea()).thenReturn(mockBoroughContestArea);

		when(getInjectMock(ElectionEventRepository.class).findById(anyString())).thenReturn(mockElectionEvent);

		when(getInjectMock(CountryRepository.class).getCountriesForElectionEvent(anyLong())).thenReturn(singletonList(mockCountry));
		when(getInjectMock(CountryServiceBean.class).findCountryById(anyLong(), anyString())).thenReturn(mockCountry);

		when(getInjectMock(CountyRepository.class).getCountiesByCountry(anyLong())).thenReturn(singletonList(mockCounty));
		when(getInjectMock(CountyRepository.class).findCountyById(anyLong(), anyString())).thenReturn(mockCounty);

		when(getInjectMock(MunicipalityRepository.class).findByCounty(anyLong())).thenReturn(singletonList(mockMunicipality));
		when(getInjectMock(MunicipalityRepository.class).findMunicipalityById(anyLong(), anyString())).thenReturn(mockMunicipality);

		when(getInjectMock(BoroughRepository.class).findByMunicipality(anyLong())).thenReturn(singletonList(mockBorough));
		when(getInjectMock(BoroughRepository.class).findBoroughById(anyLong(), anyString())).thenReturn(mockBorough);

		when(getInjectMock(PollingDistrictRepository.class).findPollingDistrictById(anyLong(), anyString())).thenReturn(mockPollingDistrict);

		when(getInjectMock(PollingPlaceRepository.class).findByPollingDistrict(anyLong())).thenReturn(singletonList(mockPollingPlace));
		when(mockMvElectionForCounty.getAreaLevel()).thenReturn(COUNTY.getLevel());
		when(mockMvElectionForCounty.getContest()).thenReturn(mockCountyLevelContest);
		when(mockMvElectionForMunicipality.getAreaLevel()).thenReturn(MUNICIPALITY.getLevel());
		when(mockMvElectionForMunicipality.getContest()).thenReturn(mockMunicipalityLevelContest);
		when(mockMvElectionForBorough.getAreaLevel()).thenReturn(BOROUGH.getLevel());
		when(mockMvElectionForBorough.getContest()).thenReturn(mockBoroughLevelContest);
		when(getInjectMock(MvElectionRepository.class).findContestsForElectionAndArea(any(ElectionPath.class), any(AreaPath.class))).thenReturn(
				newArrayList(mockMvElectionForCounty, mockMvElectionForMunicipality, mockMvElectionForBorough));
		when(getInjectMock(MvAreaRepository.class).findSingleByPath(any(AreaPath.class))).thenReturn(mockMvArea);
	}

	private void setupMockForFolders() {
		when(getInjectMock(JasperRestApiWithTimeout.class)
				.getJasperFolder(FOLDER_URI_1))
						.thenReturn(JASPER_FOLDER_1);
		when(getInjectMock(JasperRestApiWithTimeout.class)
				.getJasperFolder(FOLDER_URI_1))
						.thenReturn(JASPER_FOLDER_1);
		when(getInjectMock(JasperRestApiWithTimeout.class)
				.getJasperFolder(FOLDER_URI_2))
						.thenReturn(JASPER_FOLDER_2);
	}

	private void setupMockForInputControls() {
		when(getInjectMock(JasperRestApiWithTimeout.class)
				.getJasperFolder(FOLDER_URI_INACCESSIBLE))
						.thenReturn(JASPER_FOLDER_INACCESSIBLE);
		FolderMetaData folderMetaDataAccessible = new FolderMetaData();
		folderMetaDataAccessible.setAccess(Aggregert_Rapport.name());
		when(getInjectMock(JasperRestApiWithTimeout.class).getJasperFolderMetaData(FOLDER_URI_1)).thenReturn(folderMetaDataAccessible);
		when(getInjectMock(JasperRestApiWithTimeout.class).getJasperFolderMetaData(FOLDER_URI_2)).thenReturn(folderMetaDataAccessible);
		FolderMetaData folderMetaDataNoAccess = new FolderMetaData();
		folderMetaDataNoAccess.setAccess(Opptelling_Forhånd_Se.name());
		when(getInjectMock(JasperRestApiWithTimeout.class).getJasperFolderMetaData(FOLDER_URI_INACCESSIBLE)).thenReturn(folderMetaDataNoAccess);
		when(getInjectMock(JasperRestApiWithTimeout.class).getDataType("number")).thenReturn(new DataType(NUMBER));
		when(getInjectMock(JasperRestApiWithTimeout.class).getDataType("text")).thenReturn(new DataType());
		setupMockInputControlsForReport1();
		setupMockInputControlsForReport2();
		setupMockInputControlsForReport5();
		when(getInjectMock(JasperRestApiWithTimeout.class)
				.getInputControl(INPUT_CONTROL_URI_EE1_EG1_EL1_CT1_MATCH_TO_COUNTY_AND_MUN_AREA_LEVELS)).thenReturn(
						INPUT_CONTROL_EE1_EG1_EL1_CT1_FOR_CNT_OR_MUN_ONLY_PK);
		setupMockInputControlsForReport11();
	}

	private void setupMockInputControlsForReport1() {
		when(getInjectMock(JasperRestApiNoTimeout.class)
				.getInputControls(REPORT_URI_MUNICIPALITY))
						.thenReturn(INPUT_CONTROLS_FOR_REPORT_1);
		when(getInjectMock(JasperRestApiWithTimeout.class)
				.getInputControl(INPUT_CONTROL_URI_EE1)).thenReturn(INPUT_CONTROL_EE1);
		when(getInjectMock(JasperRestApiWithTimeout.class)
				.getInputControl(INPUT_CONTROL_URI_EE1_CO1)).thenReturn(INPUT_CONTROL_EE1_CO1);
		when(getInjectMock(JasperRestApiWithTimeout.class)
				.getInputControl(INPUT_CONTROL_URI_EE1_CO1_CNT1)).thenReturn(INPUT_CONTROL_EE1_CO1_CNT1);
		when(getInjectMock(JasperRestApiWithTimeout.class)
				.getInputControl(INPUT_CONTROL_URI_EE1_CO1_CNT1_MUN1)).thenReturn(INPUT_CONTROL_EE1_CO1_CNT1_MUN1);
		when(getInjectMock(JasperRestApiWithTimeout.class)
				.getInputControl(INPUT_CONTROL_URI_EE1_CO1_CNT1_MUN1_BOR1)).thenReturn(INPUT_CONTROL_EE1_CO1_CNT1_MUN1_BOR1);
		when(getInjectMock(JasperRestApiWithTimeout.class)
				.getInputControl(INPUT_CONTROL_URI_EE1_CO1_CNT1_MUN1_BOR1_PD1)).thenReturn(INPUT_CONTROL_EE1_CO1_CNT1_MUN1_BOR1_PD1);
		when(getInjectMock(JasperRestApiWithTimeout.class)
				.getInputControl(INPUT_CONTROL_URI_EE1_CO1_CNT1_MUN1_BOR1_PD1_PP1)).thenReturn(INPUT_CONTROL_EE1_CO1_CNT1_MUN1_BOR1_PD1_PP1);
	}

	private void setupMockInputControlsForReport2() {
		when(getInjectMock(JasperRestApiWithTimeout.class)
				.getInputControls(REPORT_URI_COUNTY))
						.thenReturn(INPUT_CONTROLS_FOR_REPORT_2);
		when(getInjectMock(JasperRestApiWithTimeout.class)
				.getInputControl(INPUT_CONTROL_URI_EE1)).thenReturn(INPUT_CONTROL_EE1);
		when(getInjectMock(JasperRestApiWithTimeout.class)
				.getInputControl(INPUT_CONTROL_URI_EE1_EG1)).thenReturn(INPUT_CONTROL_EE1_EG1);
		when(getInjectMock(JasperRestApiWithTimeout.class)
				.getInputControl(INPUT_CONTROL_URI_EE1_EG1_EL1)).thenReturn(INPUT_CONTROL_EE1_EG1_EL1);
		when(getInjectMock(JasperRestApiWithTimeout.class)
				.getInputControl(INPUT_CONTROL_URI_EE1_EG1_EL1_CT1)).thenReturn(INPUT_CONTROL_EE1_EG1_EL1_CT1);
	}

	private void setupMockInputControlsForReport5() {
		when(getInjectMock(JasperRestApiWithTimeout.class)
				.getInputControls(REPORT_URI_CONTEST_PK))
						.thenReturn(INPUT_CONTROLS_FOR_CONTEST_PK);
		when(getInjectMock(JasperRestApiWithTimeout.class)
				.getInputControl(INPUT_CONTROL_URI_EE1)).thenReturn(INPUT_CONTROL_EE1);
		when(getInjectMock(JasperRestApiWithTimeout.class)
				.getInputControl(INPUT_CONTROL_URI_EE1_EG1)).thenReturn(INPUT_CONTROL_EE1_EG1);
		when(getInjectMock(JasperRestApiWithTimeout.class)
				.getInputControl(INPUT_CONTROL_URI_EE1_EG1_EL1)).thenReturn(INPUT_CONTROL_EE1_EG1_EL1);
		when(getInjectMock(JasperRestApiWithTimeout.class)
				.getInputControl(INPUT_CONTROL_URI_EE1_EG1_EL1_CT1_MATCH_TO_AREA_LEVEL)).thenReturn(INPUT_CONTROL_EE1_EG1_EL1_CT1_PK);
	}

	private void setupMockInputControlsForReport11() {
		when(getInjectMock(JasperRestApiNoTimeout.class)
				.getInputControls(REPORT_URI_11))
				.thenReturn(INPUT_CONTROLS_FOR_REPORT_11);
		when(getInjectMock(JasperRestApiWithTimeout.class)
				.getInputControl(INPUT_CONTROL_URI_PROTOCOL_FROM_INT)).thenReturn(INPUT_CONTROL_PROTOCOL_FROM_INT);
	}

	private UserData userDataForAreaLevel(final AreaLevelEnum areaLevel) {
		return userDataForAreaLevel(areaLevel, "03", "0301");
	}

	private UserData userDataForAreaLevel(final AreaLevelEnum areaLevel, final String countyId, final String municipalityId) {
		when(mockMvArea.getActualAreaLevel()).thenReturn(areaLevel);
		switch (areaLevel) {
		case COUNTY:
			when(mockMvArea.getAreaPath()).thenReturn(ELECTION_EVENT_ID + ".47." + countyId);
			break;
		case MUNICIPALITY:
			when(mockMvArea.getAreaPath()).thenReturn(ELECTION_EVENT_ID + ".47." + countyId + "." + municipalityId);
			break;
		case POLLING_DISTRICT:
			when(mockMvArea.getAreaPath()).thenReturn(ELECTION_EVENT_ID + ".47." + countyId + "." + municipalityId + ".030100.0001");
			break;
		case ROOT:
			when(mockMvArea.getAreaPath()).thenReturn(ELECTION_EVENT_ID);
			break;
		default:
			fail("Undefined area level");
		}

		UserData userDataForArea = createUserDataForArea(mockMvArea);
		when(userDataForArea.getOperatorAreaLevel()).thenReturn(areaLevel);
		when(userDataForArea.getOperatorMvArea()).thenReturn(mockMvArea);
		return userDataForArea;
	}

	private UserData createUserDataForArea(final MvArea mockMvArea) {
		UserData userData = mock(UserData.class);
		when(userData.getElectionEventId()).thenReturn(ELECTION_EVENT_ID);
		when(userData.hasAccess(Aggregert_Rapport)).thenReturn(true);
		when(userData.hasAccess(Opptelling_Forhånd_Se)).thenReturn(false);
		when(userData.getJavaLocale()).thenReturn(new Locale("nb", "NO"));
		when(userData.getLocale()).thenReturn(new no.valg.eva.admin.configuration.domain.model.Locale());
		AreaPath areaPath = AreaPath.from(mockMvArea.getAreaPath());
		when(userData.getOperatorAreaPath()).thenReturn(areaPath);
		ElectionPath electionPath = ElectionPath.from(ELECTION_EVENT_ID);
		when(userData.getOperatorElectionPath()).thenReturn(electionPath);
		OperatorRole mockOperatorRole = mock(OperatorRole.class);
		when(mockOperatorRole.getMvArea()).thenReturn(mockMvArea);
		MvElection mockMvElection = mock(MvElection.class);
		when(mockMvElection.getElectionPath()).thenReturn(ELECTION_EVENT_ID + ".01.02");
		when(mockOperatorRole.getMvElection()).thenReturn(mockMvElection);
		when(userData.getOperatorRole()).thenReturn(mockOperatorRole);
		return userData;
	}

	@Test
	public void testUserRoleMvAreaRegExp() {
		assertThat("950004.47.03.0301.030104.0401".matches(ROLE_MV_AREA_REG_EXP_OSLO_COUNT_AND_BELOW)).isTrue();
		assertThat("950004.47.03.0301.030104".matches(ROLE_MV_AREA_REG_EXP_OSLO_COUNT_AND_BELOW)).isTrue();
		assertThat("950004.47.03.0301".matches(ROLE_MV_AREA_REG_EXP_OSLO_COUNT_AND_BELOW)).isTrue();
		assertThat("950004.47.03".matches(ROLE_MV_AREA_REG_EXP_OSLO_COUNT_AND_BELOW)).isTrue();
		assertThat("950004.47.04.0401.040100".matches(ROLE_MV_AREA_REG_EXP_OSLO_COUNT_AND_BELOW)).isFalse();
		assertThat("950004.47.04.0401".matches(ROLE_MV_AREA_REG_EXP_OSLO_COUNT_AND_BELOW)).isFalse();
		assertThat("950004.47.04".matches(ROLE_MV_AREA_REG_EXP_OSLO_COUNT_AND_BELOW)).isFalse();
	}

	@Test(expectedExceptions = EvoteSecurityException.class)
	public void testGetReportTemplateWithoutAccessMunicipality() {
		jasperReportService.getReportTemplate(userDataForAreaLevel(MUNICIPALITY), REPORT_URI_COUNTY);
	}

	@Test(expectedExceptions = EvoteSecurityException.class)
	public void testGetReportTemplateWithoutAccessCounty() {
		jasperReportService.getReportTemplate(userDataForAreaLevel(COUNTY), REPORT_URI_MUNICIPALITY);
	}

	@Test
	public void testGetReportTemplate() {
		ReportTemplate reportTemplate = jasperReportService.getReportTemplate(userDataForAreaLevel(MUNICIPALITY), REPORT_URI_MUNICIPALITY);
		assertEquals(reportTemplate.getReportName(), REPORT_1_NAME);
		assertEquals(reportTemplate.getReportUri(), REPORT_URI_MUNICIPALITY);
		assertEquals(reportTemplate.getReportDescription(), JASPER_REPORT_MUNICIPALITY.getDescription());
		Collection<ReportParameter> reportTemplateParameters = reportTemplate.getParameters();
		assertEquals(reportTemplateParameters.size(), INPUT_CONTROLS_FOR_REPORT_1.size());
		int i = 0;
		String[] expectedInputControlIds = new String[] { INPUT_CONTROL_ID_EE1, INPUT_CONTROL_ID_EE1_CO1, INPUT_CONTROL_ID_EE1_CO1_CNT1,
				INPUT_CONTROL_ID_EE1_CO1_CNT1_MUN1, INPUT_CONTROL_ID_EE1_CO1_CNT1_MUN1_BOR1, INPUT_CONTROL_ID_EE1_CO1_CNT1_MUN1_BOR1_PD1,
				INPUT_CONTROL_ID_EE1_CO1_CNT1_MUN1_BOR1_PD1_PP1 };
		for (ReportParameter reportParameter : reportTemplateParameters) {
			assertEquals(reportParameter.getId(), expectedInputControlIds[i]);
			assertEquals(reportParameter.getDescription(), INPUT_CONTROLS_FOR_REPORT_1.get(i).getDescription());
			i++;
		}
		List<ReportParameter> reportParameters = new ArrayList<>(reportTemplateParameters);
		Iterator<ReportParameter> reportParameterIterator = reportParameters.get(0).getDependentParameters().iterator();
		assertTrue((INPUT_CONTROL_ID_EE1_CO1).contains(reportParameterIterator.next().getId()));
		reportParameterIterator = reportParameters.get(0).getDependentParameters().iterator();
		assertEquals(reportParameterIterator.next().getParentValue(), ELECTION_EVENT_ID);
		assertThat(transform(newArrayList(reportParameters.get(0).getDescendingParameters()), new Function<ReportParameter, String>() {
			@Override
			public String apply(ReportParameter input) {
				return input.getId();
			}
		})).containsAll(of(INPUT_CONTROL_ID_EE1_CO1, INPUT_CONTROL_ID_EE1_CO1_CNT1, INPUT_CONTROL_ID_EE1_CO1_CNT1_MUN1));
	}

	@Test
	public void testRunReport() {
		byte[] reportContent = jasperReportService.executeReport(userDataForAreaLevel(ROOT), REPORT_URI_MUNICIPALITY,
				ImmutableMap.of("parameter1", "value1"), PDF).getContent();
		assertEquals(reportContent.length, PDF_CONTENT.length);
	}

	@Test(expectedExceptions = EvoteSecurityException.class)
	public void testMunicipalityUserShouldNotBeAbleToRunCountyReport() {
		jasperReportService.executeReport(userDataForAreaLevel(MUNICIPALITY), REPORT_URI_COUNTY,
				ImmutableMap.of("parameter1", "value1"), PDF);
	}

	@Test
	public void testMunicipalityUserShouldBeAbleToRunMunicipalityReport() {
		jasperReportService.executeReport(userDataForAreaLevel(MUNICIPALITY), REPORT_URI_MUNICIPALITY,
				ImmutableMap.of("parameter1", "value1"), PDF);
	}

	@Test
	public void testCountyUserShouldBeAbleToRunCountyReport() {
		jasperReportService.executeReport(userDataForAreaLevel(COUNTY), REPORT_URI_COUNTY,
				ImmutableMap.of("parameter1", "value1"), PDF);
	}

	@Test
	public void testRootUserShouldBeAbleToRunAllReport() {
		jasperReportService.executeReport(userDataForAreaLevel(ROOT), REPORT_URI_COUNTY,
				ImmutableMap.of("parameter1", "value1"), PDF);
		jasperReportService.executeReport(userDataForAreaLevel(ROOT), REPORT_URI_MUNICIPALITY,
				ImmutableMap.of("parameter1", "value1"), PDF);
	}

	@Test
	public void testAreaPathParser() {
		Map<String, Object> params = jasperReportService.inferParameterValues(ELECTION_EVENT_ID + ".47.10.1021.102100.0002.0003", AREA_PATH_PARAMETERS, null,
				EMPTY_LIST, REPORT_URI_ASYNC);
		assertEquals(params.get(AREA_PATH_PARAMETERS[0]), ELECTION_EVENT_ID);
		assertEquals(params.get(AREA_PATH_PARAMETERS[1]), COUNTRY_ID);
		assertEquals(params.get(AREA_PATH_PARAMETERS[2]), "10");
		assertEquals(params.get(AREA_PATH_PARAMETERS[3]), "1021");
		assertEquals(params.get(AREA_PATH_PARAMETERS[4]), "102100");
		assertEquals(params.get(AREA_PATH_PARAMETERS[5]), "0002");
		assertEquals(params.get(AREA_PATH_PARAMETERS[6]), "0003");
		assertEquals(params.size(), 7);

		params = jasperReportService.inferParameterValues(ELECTION_EVENT_ID + ".12.13", ELECTION_PATH_PARAMETERS, null, EMPTY_LIST,
				REPORT_URI_ASYNC);
		assertEquals(params.get(ELECTION_PATH_PARAMETERS[0]), ELECTION_EVENT_ID);
		assertEquals(params.get(ELECTION_PATH_PARAMETERS[1]), "12");
		assertEquals(params.get(ELECTION_PATH_PARAMETERS[2]), "13");
		assertEquals(params.size(), 3);
	}

	@Test
	public void testGetSelectableValuesForParameterForReport1() {
		UserData userData = userDataForAreaLevel(MUNICIPALITY);
		ReportTemplate reportTemplate = jasperReportService.getReportTemplate(userData, REPORT_URI_MUNICIPALITY);
		Iterator<ReportParameter> iterator = reportTemplate.getParameters().iterator();
		iterator.next(); // skip Election Event
		while (iterator.hasNext()) {
			ReportParameter reportParameter = iterator.next();
			Collection<SelectableReportParameterValue> selectableValuesForParameter = jasperReportService.getSelectableValuesForParameter(
					userData,
					reportParameter, REPORT_URI_MUNICIPALITY);
			assertFalse(selectableValuesForParameter.isEmpty(), "Value list for parameter " + reportParameter.getId() + " was empty");
		}
	}

	@Test(enabled = false)
	public void testGetSelectableValuesForParameterForReport2() {
		// Ender opp med ikke å ha noen contester under seg
		//
		UserData userData = userDataForAreaLevel(COUNTY);
		ReportTemplate reportTemplate = jasperReportService.getReportTemplate(userData, REPORT_URI_COUNTY);
		Iterator<ReportParameter> iterator = reportTemplate.getParameters().iterator();
		iterator.next(); // skip Election Event
		while (iterator.hasNext()) {
			ReportParameter reportParameter = iterator.next();
			Collection<SelectableReportParameterValue> selectableValuesForParameter = jasperReportService.getSelectableValuesForParameter(
					userData,
					reportParameter, REPORT_URI_COUNTY);
			assertFalse(selectableValuesForParameter.isEmpty(), "Value list for parameter " + reportParameter.getId() + " was empty");
		}
	}

	@Test
	public void testGetSelectableValuesForParameterForReportContestPkParameter() {
		UserData userData = userDataForAreaLevel(COUNTY);
		ReportTemplate reportTemplate = jasperReportService.getReportTemplate(userData, REPORT_URI_CONTEST_PK);
		assertThat(transform(reportTemplate.getParameters(), PARAMETER_TO_ID)).containsAll(asList(
				ELECTION_EVENT_PARAMETER_NAME, CONTEST_PK_PARAMETER));
		Iterator<ReportParameter> iterator = reportTemplate.getParameters().iterator();
		iterator.next(); // skip Election Event
		boolean foundContestPk = false;
		while (iterator.hasNext()) {
			ReportParameter reportParameter = iterator.next();
			Collection<SelectableReportParameterValue> selectableValuesForParameter = jasperReportService.getSelectableValuesForParameter(
					userData,
					reportParameter, REPORT_URI_CONTEST_PK);
			if (reportParameter.getId().equals(CONTEST_PK_PARAMETER)) {
				assertThat(selectableValuesForParameter).hasSize(1);
				if (selectableValuesForParameter.iterator().next().getValueId().equals(valueOf(COUNTY_LEVEL_CONTEST_PK))) {
					foundContestPk = true;
				}
			}
			assertFalse(selectableValuesForParameter.isEmpty(), "Value list for parameter " + reportParameter.getId() + " was empty");
		}
		assertThat(foundContestPk).isTrue();
	}

	@Test
	public void testGetSelectableValuesForParameterForReportContestAtCountyOrMunicipalityLevelPkParameter() {
		UserData userData = userDataForAreaLevel(MUNICIPALITY);
		ReportTemplate reportTemplate = jasperReportService.getReportTemplate(userData, REPORT_URI_CONTEST_COUNTY_OR_MUNICIPALITY_PK);
		assertThat(transform(reportTemplate.getParameters(), PARAMETER_TO_ID)).containsAll(asList(
				ELECTION_EVENT_PARAMETER_NAME, CONTEST_PK_FOR_COUNTY_OR_MUNICIPALITY_PARAMETER));
		Iterator<ReportParameter> iterator = reportTemplate.getParameters().iterator();
		iterator.next(); // skip Election Event
		boolean foundContestPk = false;
		boolean foundMunicipalityPk = false;
		while (iterator.hasNext()) {
			ReportParameter reportParameter = iterator.next();
			Collection<SelectableReportParameterValue> selectableValuesForParameter = jasperReportService.getSelectableValuesForParameter(
					userData,
					reportParameter, REPORT_URI_CONTEST_COUNTY_OR_MUNICIPALITY_PK);
			if (reportParameter.getId().equals(CONTEST_PK_FOR_COUNTY_OR_MUNICIPALITY_PARAMETER)) {
				assertThat(selectableValuesForParameter).hasSize(2);
				Iterator<SelectableReportParameterValue> parameterValueIterator = selectableValuesForParameter.iterator();
				while (parameterValueIterator.hasNext()) {
					if (parameterValueIterator.next().getValueId().equals(valueOf(COUNTY_LEVEL_CONTEST_PK))) {
						foundContestPk = true;
					}
					if (parameterValueIterator.next().getValueId().equals(valueOf(MUNICIPALITY_LEVEL_CONTEST_PK))) {
						foundMunicipalityPk = true;
					}
				}
			}
			assertFalse(selectableValuesForParameter.isEmpty(), "Value list for parameter " + reportParameter.getId() + " was empty");
		}
		assertThat(foundContestPk).isTrue();
		assertThat(foundMunicipalityPk).isTrue();
	}

	@Test
	public void executeReport_whenRootUserRunsReport_localeIsFetchedFromMunicipality() {
		jasperReportService.executeReport(userDataForAreaLevel(ROOT), REPORT_URI_MUNICIPALITY, MUNICIPALITY_PATH_ARGUMENTS, PDF);
		ArgumentCaptor<JasperExecutionRequest> jasperExecutionRequestArgumentCaptor = ArgumentCaptor.forClass(JasperExecutionRequest.class);

		verify(getInjectMock(JasperRestApiNoTimeout.class)).executeReport(jasperExecutionRequestArgumentCaptor.capture(), eq(NN_NO));
		assertThat(jasperExecutionRequestArgumentCaptor.getValue().getParameters())
				.contains(new JasperExecutionRequest.ReportParameter("REPORT_LOCALE", newArrayList(REPORT_LOCALE)));
		assertThat(userDataForAreaLevel(ROOT).getJavaLocale().toString()).isEqualTo("nb_NO");
	}

	@Test
	public void executeReport_whenReportMayBePreGenerated_fetchesPregeneratedReport() {
		UserData userData = userDataForAreaLevel(MUNICIPALITY);
		ReportExecution reportExecution = jasperReportService.executeReport(userData, REPORT_URI_PREGENERATED, MUNICIPALITY_PATH_ARGUMENTS, PDF);
		verify(pregeneratedContentRetriever).tryPreGeneratedReport(anyString());
		assertThat(reportExecution.getFormat()).isEqualTo(PDF);
		assertThat(reportExecution.getContent()).isEqualTo(PDF_CONTENT);
	}

	@Test(expectedExceptions = ValidateException.class)
	public void executeReport_whenReportHasIntegerParameterAndNonIntegerParameterValue_throwsException() {
		UserData userData = userDataForAreaLevel(MUNICIPALITY);
		jasperReportService.executeReport(userData, REPORT_URI_11, PROTOCOL_FROM_INT_INVALID_ARGUMENT, PDF);
	}

	@Test
	public void executeReport_whenReportIsAsync_requiresSomePollsToFinish() {
		UserData userData = userDataForAreaLevel(MUNICIPALITY);
		ReportExecution reportExecution = jasperReportService.executeReport(userData, REPORT_URI_ASYNC, EMPTY_MAP, PDF);
		assertThat(reportExecution.getContent()).isNull();
		while (!reportExecution.isReady()) {
			reportExecution = jasperReportService.pollReportExecution(userData, reportExecution);
		}

		verify(getInjectMock(JasperRestApiNoTimeout.class), times(4)).getReportExecutionStatus(anyString());
		assertThat(reportExecution.getContent()).isEqualTo(PDF_CONTENT);
	}

	@DataProvider(name = "userCountyAndSelectableElectionIdValues")
	public static Object[][] userCountyAndSelectableElectionIdValues() {
		return new Object[][] {
				{ OSLO_COUNTY_ID, "0301", newArrayList(MUNICIPALITY_ELECTION_ID) },
				{ FINNMARK_COUNTY_ID, "2004", newArrayList(COUNTY_ELECTION_ID, MUNICIPALITY_ELECTION_ID) },
		};
	}

	@Test(dataProvider = "userCountyAndSelectableElectionIdValues")
	public void getSelectableValuesForParameter_certainElectionIdParameterValue_isUnselectableDependingOnUserAreaPath(
			String countyId, String municipalityId, List<String> expectedSelectableElectionIds) {
		when(getInjectMock(ElectionRepository.class).findElectionsByElectionGroup(anyLong())).thenReturn(
				asList(mockCountyElection, mockMunicipalityElection, mockBoroughElection));
		when(mockCountyElection.getPk()).thenReturn(COUNTY_ELECTION_PK);
		when(mockMunicipalityElection.getPk()).thenReturn(MUNICIPALITY_ELECTION_PK);
		when(getInjectMock(ContestRepository.class).findByElectionPk(mockCountyElection.getPk())).thenReturn(singletonList(mockCountyLevelContest));
		when(getInjectMock(ContestRepository.class).findByElectionPk(mockMunicipalityElection.getPk()))
				.thenReturn(asList(mockMunicipalityLevelContest, mockMunicipalityLevelContestHammerfest));
		stub_matcherValghierarkiStiOgValggeografiSti(
				expectedSelectableElectionIds.contains("01"), expectedSelectableElectionIds.contains("02"), expectedSelectableElectionIds.contains("02"));
		UserData userData = userDataForAreaLevel(MUNICIPALITY, countyId, municipalityId);
		ReportTemplate reportTemplate = jasperReportService.getReportTemplate(userData, REPORT_URI_9);
		assertThat(transform(reportTemplate.getParameters(), PARAMETER_TO_ID)).containsAll(asList(
				ELECTION_EVENT_PARAMETER_NAME, ELECTION_GROUP_PARAMETER_NAME, ELECTION_PARAMETER_NAME, CONTEST_PARAMETER_NAME));
		Iterator<ReportParameter> iterator = reportTemplate.getParameters().iterator();
		iterator.next(); // skip Election Event
		while (iterator.hasNext()) {
			ReportParameter reportParameter = iterator.next();
			if (reportParameter.getId().equals(ELECTION_PARAMETER_NAME)) {
				Collection<SelectableReportParameterValue> selectableValuesForParameter = jasperReportService.getSelectableValuesForParameter(
						userData,
						reportParameter, REPORT_URI_9);
				assertThat(selectableValuesForParameter).hasSize(expectedSelectableElectionIds.size());
				ArrayList<String> selectableElectionIds = new ArrayList<>(transform(new ArrayList<>(selectableValuesForParameter),
						new Function<SelectableReportParameterValue, String>() {
							@Override
							public String apply(SelectableReportParameterValue input) {
								return input.getValueId();
							}
						}));
				sort(selectableElectionIds);
				assertThat(selectableElectionIds).isEqualTo(expectedSelectableElectionIds);
			}
		}
	}

	@Test
	public void getSelectableValuesForParameter_forPollingDistricts_technicalPollingDistrictsAreFilteredOut() {
		UserData userData = userDataForAreaLevel(POLLING_DISTRICT);
		ReportTemplate reportTemplate = jasperReportService.getReportTemplate(userData, REPORT_URI_10);
		ReportParameter reportParameter = reportTemplate.getParameters().get(5); // The polling district parameter
		PollingDistrict ordinaryPollingDistrict = buildOrdinaryPollingDistrict();
		List<PollingDistrict> oneOrdinaryAndOneTechnicalPollingDistrict = Arrays.asList(ordinaryPollingDistrict, buildTechnicalPollingDistrict());
		when(getInjectMock(PollingDistrictRepository.class).findPollingDistrictsForBorough(any(Borough.class)))
				.thenReturn(oneOrdinaryAndOneTechnicalPollingDistrict);

		Collection<SelectableReportParameterValue> selectablePollingDistricts = jasperReportService.getSelectableValuesForParameter(userData, reportParameter,
				REPORT_URI_MUNICIPALITY);

		assertThat(selectablePollingDistricts.size()).isEqualTo(1);
		assertThat(selectablePollingDistricts.iterator().next().getValueId()).isEqualTo(ordinaryPollingDistrict.getId());
	}

	protected PollingDistrict buildTechnicalPollingDistrict() {
		PollingDistrict pollingDistrict = new PollingDistrict(POLLING_DISTRICT_2_ID, POLLING_DISTRICT_2_NAME, null);
		pollingDistrict.setTechnicalPollingDistrict(true);
		return pollingDistrict;
	}

	protected PollingDistrict buildOrdinaryPollingDistrict() {
		return new PollingDistrict(POLLING_DISTRICT_1_ID, POLLING_DISTRICT_1_NAME, null);
	}

	@Test
	public void getCanonicalReportParameterParentIdMap_returnsMapWithReportParameterRelationships() {
		Map<String, String> canonicalReportParameterParentIdMap = jasperReportService.getCanonicalReportParameterParentIdMap();
		assertThat(canonicalReportParameterParentIdMap).hasSize(11);
	}

	@Test(
			expectedExceptions = EvoteException.class,
			expectedExceptionsMessageRegExp = "Report execution failed, received null Response from server based on requestId 1000")
	public void pollReportExecution_withNoResponse_throwsEvoteException() throws Exception {
		JasperReportServiceBean bean = initializeMocks(new JasperReportServiceBean());
		String requestId = "1000";
		ReportExecution execution = createMock(ReportExecution.class);
		when(execution.getRequestId()).thenReturn(requestId);
		when(getInjectMock(JasperRestApiNoTimeout.class).getReportExecutionStatus(requestId)).thenReturn(null);

		bean.pollReportExecution(createMock(UserData.class), execution);
	}

	@Test(
			expectedExceptions = EvoteException.class,
			expectedExceptionsMessageRegExp = "Report execution failed, received Response from server based on requestId "
					+ "1000 with missing Content-Type")
	public void pollReportExecution_withNoContentType_throwsEvoteException() throws Exception {
		JasperReportServiceBean bean = initializeMocks(new JasperReportServiceBean());
		String requestId = "1000";
		ReportExecution execution = createMock(ReportExecution.class);
		when(execution.getRequestId()).thenReturn(requestId);
		Response response = createMock(Response.class);
		when(response.getStatus()).thenReturn(HttpServletResponse.SC_OK);
		when(response.getHeaderString("Content-Type")).thenReturn(null);
		when(getInjectMock(JasperRestApiNoTimeout.class).getReportExecutionStatus(requestId)).thenReturn(response);

		bean.pollReportExecution(createMock(UserData.class), execution);
	}

	@Test(
			expectedExceptions = EvoteException.class,
			expectedExceptionsMessageRegExp = "Report execution failed, received Response from server based on requestId "
					+ "1000 with missing ReportExecutionStatus")
	public void pollReportExecution_withNoReportExecutionStatus_throwsEvoteException() throws Exception {
		JasperReportServiceBean bean = initializeMocks(new JasperReportServiceBean());
		String requestId = "1000";
		ReportExecution execution = createMock(ReportExecution.class);
		when(execution.getRequestId()).thenReturn(requestId);
		Response response = createMock(Response.class);
		when(response.getStatus()).thenReturn(HttpServletResponse.SC_OK);
		when(response.getHeaderString("Content-Type")).thenReturn("application/status+xml");
		when(response.readEntity(ReportExecutionStatus.class)).thenReturn(null);
		when(getInjectMock(JasperRestApiNoTimeout.class).getReportExecutionStatus(requestId)).thenReturn(response);

		bean.pollReportExecution(createMock(UserData.class), execution);
	}

	@DataProvider(name = "KommuneFylkeBydelsValg")
	public static Object[][] removeElectionsBasedOnAvailableContestsForUser() {
		return new Object[][] {
				{ OSLO_COUNTY_ID, "0301", newArrayList(MUNICIPALITY_ELECTION_ID) },
				{ FINNMARK_COUNTY_ID, "2004", newArrayList(COUNTY_ELECTION_ID, MUNICIPALITY_ELECTION_ID) },
		};
	}

	@Test(dataProvider = "KommuneFylkeBydelsValg")
	public void removeFylkesValgElectionForOslokommuneBruker(String countyId, String municipalityId, List<String> expectedSelectableElectionIds) {

		when(mockCountyElection.getPk()).thenReturn(COUNTY_ELECTION_PK);
		when(mockMunicipalityElection.getPk()).thenReturn(MUNICIPALITY_ELECTION_PK);
		when(mockBoroughElection.getPk()).thenReturn(BOROUGH_ELECTION_PK);

		// Er tre valg 01, 02 og 03 (typisk fylkes, kommune og bydelsvalg)
		when(getInjectMock(ElectionRepository.class).findElectionsByElectionGroup(anyLong())).thenReturn(
				asList(mockCountyElection, mockMunicipalityElection, mockBoroughElection));
		when(getInjectMock(ContestRepository.class).findByElectionPk(mockMunicipalityElection.getPk()))
				.thenReturn(asList(mockMunicipalityLevelContest, mockMunicipalityLevelContestHammerfest));
		when(getInjectMock(ContestRepository.class).findByElectionPk(mockCountyElection.getPk())).thenReturn(singletonList(mockCountyLevelContest));
		when(getInjectMock(ContestRepository.class).findByElectionPk(mockBoroughElection.getPk())).thenReturn(singletonList(mockBoroughLevelContest));
		UserData userData = userDataForAreaLevel(MUNICIPALITY, countyId, municipalityId);
		stub_matcherValghierarkiStiOgValggeografiSti(
				expectedSelectableElectionIds.contains("01"), expectedSelectableElectionIds.contains("02"), expectedSelectableElectionIds.contains("02"));
		// her har jeg: 200701.47.03.0301 for areapath

		ReportTemplate reportTemplate = jasperReportService.getReportTemplate(userData, REPORT_URI_9);
		assertThat(transform(reportTemplate.getParameters(), PARAMETER_TO_ID)).containsAll(asList(
				ELECTION_EVENT_PARAMETER_NAME, ELECTION_GROUP_PARAMETER_NAME, ELECTION_PARAMETER_NAME, CONTEST_PARAMETER_NAME));

		Iterator<ReportParameter> iterator = reportTemplate.getParameters().iterator();
		iterator.next(); // skip Election Event
		while (iterator.hasNext()) {
			ReportParameter reportParameter = iterator.next();
			if (reportParameter.getId().equals(ELECTION_PARAMETER_NAME)) {
				Collection<SelectableReportParameterValue> selectableValuesForParameter = jasperReportService.getSelectableValuesForParameter(
						userData,
						reportParameter, REPORT_URI_9);
				assertThat(selectableValuesForParameter).hasSize(expectedSelectableElectionIds.size());
				ArrayList<String> selectableElectionIds = new ArrayList<>(transform(new ArrayList<>(selectableValuesForParameter),
						new Function<SelectableReportParameterValue, String>() {
							@Override
							public String apply(SelectableReportParameterValue input) {
								return input.getValueId();
							}
						}));
				sort(selectableElectionIds);
				assertThat(selectableElectionIds).isEqualTo(expectedSelectableElectionIds);
			}
		}

	}

	@DataProvider(name = "Stortingsvalg")
	public static Object[][] removeElectionsBasedOnAvailableContestsForUserStorting() {
		return new Object[][] {
				{ OSLO_COUNTY_ID, "0301", newArrayList(STORTING_ELECTION_ID) },
				{ FINNMARK_COUNTY_ID, "2004", newArrayList(STORTING_ELECTION_ID) },
		};
	}

	@Test(dataProvider = "Stortingsvalg")
	public void dasdfasdf(String countyId, String municipalityId, List<String> expectedSelectableElectionIds) {

		when(mockCountyElection.getPk()).thenReturn(COUNTY_ELECTION_PK);

		// Er kun ett valg - har id "01"
		when(getInjectMock(ElectionRepository.class).findElectionsByElectionGroup(anyLong())).thenReturn(singletonList(mockCountyElection));
		stub_matcherValghierarkiStiOgValggeografiSti(true, true, true);
		when(getInjectMock(ContestRepository.class).findByElectionPk(mockCountyElection.getPk()))
				.thenReturn(asList(mockCountyLevelContest, mockCountyLevelContestStortingOslo));
		UserData userData = userDataForAreaLevel(MUNICIPALITY, countyId, municipalityId);

		ReportTemplate reportTemplate = jasperReportService.getReportTemplate(userData, REPORT_URI_9);
		assertThat(transform(reportTemplate.getParameters(), PARAMETER_TO_ID)).containsAll(asList(
				ELECTION_EVENT_PARAMETER_NAME, ELECTION_GROUP_PARAMETER_NAME, ELECTION_PARAMETER_NAME, CONTEST_PARAMETER_NAME));

		Iterator<ReportParameter> iterator = reportTemplate.getParameters().iterator();
		iterator.next(); // skip Election Event
		while (iterator.hasNext()) {
			ReportParameter reportParameter = iterator.next();
			if (reportParameter.getId().equals(ELECTION_PARAMETER_NAME)) {
				Collection<SelectableReportParameterValue> selectableValuesForParameter = jasperReportService.getSelectableValuesForParameter(
						userData,
						reportParameter, REPORT_URI_9);
				assertThat(selectableValuesForParameter).hasSize(expectedSelectableElectionIds.size());
				ArrayList<String> selectableElectionIds = new ArrayList<>(transform(new ArrayList<>(selectableValuesForParameter),
						new Function<SelectableReportParameterValue, String>() {
							@Override
							public String apply(SelectableReportParameterValue input) {
								return input.getValueId();
							}
						}));
				sort(selectableElectionIds);
				assertThat(selectableElectionIds).isEqualTo(expectedSelectableElectionIds);
			}
		}

	}

	private void stub_matcherValghierarkiStiOgValggeografiSti(boolean countyElection, boolean municipalityElection, boolean boroughElection) {
		when(getInjectMock(MvElectionRepository.class).matcherValghierarkiStiOgValggeografiSti(
                eq(valgSti(mockCountyElection)), any())).thenReturn(countyElection);

		when(getInjectMock(MvElectionRepository.class).matcherValghierarkiStiOgValggeografiSti(
                eq(valgSti(mockMunicipalityElection)), any())).thenReturn(municipalityElection);

		when(getInjectMock(MvElectionRepository.class).matcherValghierarkiStiOgValggeografiSti(
                eq(valgSti(mockBoroughElection)), any())).thenReturn(boroughElection);
	}

	private ValgSti valgSti(Election election) {
		return ValghierarkiSti.valgSti(election.electionPath());
	}

}

