package no.evote.service.impl;

import static com.google.common.collect.Lists.newArrayList;
import static no.valg.eva.admin.backend.reporting.jasperserver.api.JasperRestApi.ResourceType.reportUnit;
import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.fail;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

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
import no.valg.eva.admin.backend.reporting.jasperserver.api.JasperserverRestClientProducer;
import no.valg.eva.admin.backend.reporting.jasperserver.api.ReportMetaData;
import no.valg.eva.admin.backend.reporting.jasperserver.api.VersionInfo;

import org.jboss.resteasy.plugins.server.sun.http.HttpContextBuilder;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

/**
 * This test sets up a small HTTP server and deploys a JAX-RS application, serving out the
 * {@link no.valg.eva.admin.backend.reporting.jasperserver.api.JasperRestApi} interface. It also serves test data literally, not marshalled by JAXB. The purpose
 * of this test class is to test JAX-RS binding.
 * 
 */
public class JasperRestApiImplTest {
	private static final String REQUEST_ID = "677629003_1355225037212_1";
	private static final String REPORT_URI = "/reports/Myreport";
	private static final String LITERAL_ALL_REPORTS_RESOURCES_BODY =
			"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
					+ "<resources>\n"
					+ "    <resourceLookup>\n"
					+ "        <creationDate>2014-01-16 11:10:22</creationDate>\n"
					+ "        <description>All Accounts Report</description>\n"
					+ "        <label>Accounts Report</label>\n"
					+ "        <permissionMask>1</permissionMask>\n"
					+ "        <updateDate>2013-10-21 21:26:09</updateDate>\n"
					+ "        <uri>/reports/samples/AllAccounts</uri>\n"
					+ "        <version>0</version>\n"
					+ "        <resourceType>reportUnit</resourceType>\n"
					+ "    </resourceLookup>\n"
					+ "    <resourceLookup>\n"
					+ "        <creationDate>2014-01-16 11:10:23</creationDate>\n"
					+ "        <label>adhoc_datatree_icons_sprite.png</label>\n"
					+ "        <permissionMask>2</permissionMask>\n"
					+ "        <updateDate>2013-10-22 21:41:56</updateDate>\n"
					+ "        <uri>/themes/default/images/adhoc_datatree_icons_sprite.png</uri>\n"
					+ "        <version>0</version>\n"
					+ "        <resourceType>file</resourceType>\n"
					+ "    </resourceLookup>\n"
					+ "</resources>\n";

	private static final String FILE_REF_NAME = "metadata";
	private static final String FILE_REF_URI = "/reports/EVA/100.configuration/kretsstatistikk/" + FILE_REF_NAME + "/Metadata_for_Kretsstatistikk";
	
	private static final String LITERAL_REPORT_RESOURCE_BODY =
			"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
					+ "<reportUnit>\n"
					+ "    <creationDate>2014-01-16 12:42:15</creationDate>\n"
					+ "    <description></description>\n"
					+ "    <label>Myreport</label>\n"
					+ "    <permissionMask>1</permissionMask>\n"
					+ "    <updateDate>2014-01-16 14:10:49</updateDate>\n"
					+ "    <uri>" + REPORT_URI + "</uri>\n"
					+ "    <version>3</version>\n"
					+ "    <dataSourceReference>\n"
					+ "        <uri>/datasources/evote</uri>\n"
					+ "    </dataSourceReference>\n"
					+ "    <alwaysPromptControls>false</alwaysPromptControls>\n"
					+ "    <controlsLayout>popupScreen</controlsLayout>\n"
					+ "    <inputControlRenderingView></inputControlRenderingView>\n"
					+ "    <inputControls>\n"
					+ "        <inputControlReference>\n"
					+ "            <uri>" + REPORT_URI + "_files/EE1</uri>\n"
					+ "        </inputControlReference>\n"
					+ "        <inputControlReference>\n"
					+ "            <uri>" + REPORT_URI + "_files/EE1.CO1</uri>\n"
					+ "        </inputControlReference>\n"
					+ "        <inputControlReference>\n"
					+ "            <uri>" + REPORT_URI + "_files/EE1.CO1.CNT1</uri>\n"
					+ "        </inputControlReference>\n"
					+ "        <inputControlReference>\n"
					+ "            <uri>" + REPORT_URI + "_files/EE1.CO1.CNT1.MUN1</uri>\n"
					+ "        </inputControlReference>\n"
					+ "    </inputControls>\n"
					+ "    <resources>\n"
					+ "        <resource>\n"
					+ "            <fileReference>\n"
					+ "                <uri>" + FILE_REF_URI + "</uri>\n"
					+ "            </fileReference>\n"
					+ "            <name>" + FILE_REF_NAME + "</name>\n"
					+ "        </resource>\n"
					+ "    </resources>\n"
					+ "    <jrxmlFileReference>\n"
					+ "        <uri>" + REPORT_URI + "_files/Myreport_</uri>\n"
					+ "    </jrxmlFileReference>\n"
					+ "    <reportRenderingView></reportRenderingView>\n"
					+ "</reportUnit>\n";

	private static final String LITERAL_REPORT_EXECUTION_BODY =
			"<reportExecution>\n"
					+ "    <currentPage>1</currentPage>\n"
					+ "    <exports>\n"
					+ "        <export>\n"
					+ "            <id>pdf</id>\n"
					+ "            <status>queued</status>\n"
					+ "        </export>\n"
					+ "    </exports>\n"
					+ "    <reportURI>" + REPORT_URI + "</reportURI>\n"
					+ "    <requestId>" + REQUEST_ID + "</requestId>\n"
					+ "    <status>execution</status>\n"
					+ "</reportExecution>\n";

	private static final String FOLDER_URI = "/reports";
	private static final String FOLDER_LABEL = "Folder label";
	private static final String LITERAL_FOLDER_BODY =
			"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
					+ "<folder>\n"
					+ "    <creationDate>2014-02-13 11:09:56</creationDate>\n"
					+ "    <description></description>\n"
					+ "    <label>" + FOLDER_LABEL + "</label>\n"
					+ "    <permissionMask>1</permissionMask>\n"
					+ "    <updateDate>2014-02-13 12:41:24</updateDate>\n"
					+ "    <uri>" + FOLDER_URI + "</uri>\n"
					+ "    <version>1</version>\n"
					+ "</folder>\n";

	private static final String TEST_REPORT_LABEL_1 = "Test Report";
	private static final String TEST_REPORT_URI_1 = "reports/1";
	private static final String TEST_REPORT_LABEL_2 = "Test Report 2";
	private static final String TEST_REPORT_URI_2 = "reports/2";
	private static final String JASPERSERVER_CONTEXT = "jasperserver";
	private static final String JASPERSERVER_LITERAL_CONTEXT = JASPERSERVER_CONTEXT + "_literal";
	private static final String DATATYPE_REFERENCE_URI = "/reports/EVA/dataTypes/Date";
	private static final List<InputControl> INPUT_CONTROLS = newArrayList(
			new InputControl("Election Event", "Election Event", "/EE1", DATATYPE_REFERENCE_URI),
			new InputControl("Country", "Country", "/EE1.CO1", DATATYPE_REFERENCE_URI),
			new InputControl("County", "County", "/EE1.CO1.CNT1", DATATYPE_REFERENCE_URI),
			new InputControl("Municipality", "Municipality", "/EE1.CO1.CNT1.MUN1", DATATYPE_REFERENCE_URI));
	private static final JasperReport JASPER_REPORT = new JasperReport(TEST_REPORT_LABEL_1, TEST_REPORT_URI_1);
	private static final int PORT = 41523;
	private static final String BASE_URL = "http://localhost:" + PORT;
	private static final String REPORT_OUTPUT = "pdf content";
	private static final byte[] REPORT_OUTPUT_BYTES = REPORT_OUTPUT.getBytes();
	private static final JasperResources JASPER_RESOURCES = new JasperResources(newArrayList(new JasperReport(TEST_REPORT_LABEL_1, TEST_REPORT_URI_1),
			new JasperReport(TEST_REPORT_LABEL_2, TEST_REPORT_URI_2)));

	private URI uri = UriBuilder.fromUri("http://localhost/").port(PORT).path(JASPERSERVER_CONTEXT).path("rest_v2").path("resources").build();
	private HttpServer server;
	private JasperRestApi jasperRestApiLiteral;
	private JasperRestApi jasperRestApi;

	// Testen feiler ved gjevne mellomrom og ofte på Sonar, antakelig pga at port ikke er ledig - egner seg ikke som automatisk test..
	@BeforeClass(enabled = false)
	public void setUp() throws IOException {
		server = HttpServer.create(new InetSocketAddress(uri.getPort()), 0);
		createApiBackedContext();

		server.createContext("/" + JASPERSERVER_LITERAL_CONTEXT, new HttpHandler() {
			// A simple handler that bypasses JAX-RS annotated api implementation, and replies with a fixed text
			@Override
			public void handle(final HttpExchange httpExchange) throws IOException {
				String responseBody;
				String path = httpExchange.getRequestURI().getPath();
				String contentType = MediaType.APPLICATION_XML;
				switch (path) {
				case "/" + JASPERSERVER_LITERAL_CONTEXT + "/rest_v2/resources" + REPORT_URI:
					responseBody = LITERAL_REPORT_RESOURCE_BODY;
					break;
				case "/" + JASPERSERVER_LITERAL_CONTEXT + "/rest_v2/resources" + FOLDER_URI:
					responseBody = LITERAL_FOLDER_BODY;
					break;
				case "/" + JASPERSERVER_LITERAL_CONTEXT + "/rest_v2/resources":
					responseBody = LITERAL_ALL_REPORTS_RESOURCES_BODY;
					break;
				case "/" + JASPERSERVER_LITERAL_CONTEXT + "/rest_v2/reportExecutions":
					responseBody = LITERAL_REPORT_EXECUTION_BODY;
					break;
				case "/" + JASPERSERVER_LITERAL_CONTEXT + "/rest_v2/reportExecutions/" + REQUEST_ID + "/exports/pdf/outputResource":
					responseBody = new String(REPORT_OUTPUT_BYTES);
					contentType = "application/pdf";
					break;
				default:
					fail("Unknown path: " + path);
					responseBody = "";
				}
				httpExchange.getResponseHeaders().add(HttpHeaders.CONTENT_TYPE, contentType);
				
				httpExchange.sendResponseHeaders(200, responseBody.length());
				
				OutputStream os = httpExchange.getResponseBody();
				os.write(responseBody.getBytes());
				os.close();
			}
		});
		server.start();
		jasperRestApiLiteral = new JasperserverRestClientProducer("user", "pwd", BASE_URL, JASPERSERVER_LITERAL_CONTEXT, 1L).createJasperRestApiNoTimeout();
		jasperRestApi = new JasperserverRestClientProducer("user", "pwd", BASE_URL, JASPERSERVER_CONTEXT, 1L).createJasperRestApiWithTimeOut();
	}

	private void createApiBackedContext() {
		HttpContextBuilder httpContextBuilder = new HttpContextBuilder();
		httpContextBuilder.getDeployment().getActualResourceClasses().add(JasperRestApiImpl.class);
		httpContextBuilder.setPath("/" + JASPERSERVER_CONTEXT);
		httpContextBuilder.bind(server);
	}

	@AfterClass(enabled = false)
	public void tearDown() throws IOException {
		server.stop(0);
	}

	@Test(enabled = false)
	public void testGetJasperReportUnit() throws Exception {
		javax.ws.rs.client.Client client = ClientBuilder.newClient();
		WebTarget target = client.target(uri + "/" + TEST_REPORT_URI_1);
		String s = target.request("application/repository.reportUnit+xml").get(String.class);
		Assert.assertTrue(s.contains(TEST_REPORT_LABEL_1));
		Assert.assertTrue(s.contains(TEST_REPORT_URI_1));
	}

	@Test(enabled = false)
	public void testGetLiteralResources() throws Exception {
		JasperResources resources = jasperRestApiLiteral.getResources(reportUnit, null);
		Assert.assertSame(2, resources.getResources().size());
	}

	@Test(enabled = false)
	public void testGetLiteralReport() throws Exception {
		JasperReport report = jasperRestApiLiteral.getJasperReportUnit(REPORT_URI);
		assertEquals(REPORT_URI, report.getUri());
		List<FileResource> resources = report.getResources();
		assertEquals(resources.size(), 1);
		assertEquals(resources.get(0).getFileReference(), new FileReference(FILE_REF_URI));
		assertEquals(resources.get(0).getName(), FILE_REF_NAME);
	}

	@Test(enabled = false)
	public void testGetResourcesFromApiStub() throws Exception {
		JasperResources resources = jasperRestApi.getResources(reportUnit, null);
		assertEquals(resources, JASPER_RESOURCES);
	}

	@Test(enabled = false)
	public void testExecuteReportFromApiStub() throws Exception {
		JasperExecution jasperExecution = jasperRestApi.executeReport(new JasperExecutionRequest(REPORT_URI,
				newArrayList(new JasperExecutionRequest.ReportParameter("parameter_1", newArrayList("value 1")))), "");
		assertEquals(jasperExecution.getReportURI(), REPORT_URI);
	}

	@Test(enabled = false)
	public void testLiteralExecuteReport() throws Exception {
		JasperExecution jasperExecution = jasperRestApiLiteral.executeReport(new JasperExecutionRequest(REPORT_URI,
				newArrayList(new JasperExecutionRequest.ReportParameter("parameter_1", newArrayList("value 1")))), "");
		assertEquals(jasperExecution.getReportURI(), REPORT_URI);
	}

	@Test(enabled = false)
	public void testLiteralGetOutput() throws Exception {
		Response response = jasperRestApiLiteral.getReportOutput(REQUEST_ID, "pdf", "");
		assertEquals(response.getMediaType().toString(), "application/pdf");
		byte[] bytes = response.readEntity(byte[].class);
		assertEquals(bytes.length, REPORT_OUTPUT_BYTES.length);
		assertEquals(bytes, REPORT_OUTPUT_BYTES);
	}

	@Test(enabled = false)
	public void testGetFolderLiteral() throws Exception {
		JasperFolder jasperFolder = jasperRestApiLiteral.getJasperFolder(FOLDER_URI);
		assertNotNull(jasperFolder);
		assertEquals(jasperFolder.getUri(), FOLDER_URI);
		assertEquals(jasperFolder.getLabel(), FOLDER_LABEL);
	}

	@ApplicationPath("/jasperserver")
	static class JasperRestApiImplTestApp extends Application {
		private final Set<Class<?>> classes;

		JasperRestApiImplTestApp() {
			HashSet<Class<?>> c = new HashSet<>();
			c.add(JasperRestApiImpl.class);
			classes = Collections.unmodifiableSet(c);
		}

		@Override
		public Set<Class<?>> getClasses() {
			return classes;
		}
	}

	public static class JasperRestApiImpl implements JasperRestApi {
		@Override
		public JasperReport getJasperReportUnit(final String uri) {
			return JASPER_REPORT;
		}

		@Override
		public List<InputControl> getInputControls(final String uri) {
			return INPUT_CONTROLS;
		}

		@Override
		public InputControl getInputControl(final String uri) {
			return INPUT_CONTROLS.get(0);
		}

		@Override
		public DataType getDataType(final String uri) {
			return new DataType();
		}

		@Override
		public JasperResources getResources(final ResourceType type, @DefaultValue("/reports/EVA") final String folderUri) {
			return JASPER_RESOURCES;
		}

		@Override
		public JasperExecution executeReport(final JasperExecutionRequest jasperExecutionRequest, String userLocale) {
			return new JasperExecution(1, REPORT_URI, REQUEST_ID, "execution", newArrayList(new JasperExecution.Export("pdf", "queued")));
		}

		@Override
		public Response getReportOutput(final String requestID, final String exportID, String locale) {
			return mock(Response.class);
		}

		@Override
		public ReportMetaData getReportMetaData(final String uri) {
			return null;
		}

		@Override
		public JasperFolder getJasperFolder(final String folderUri) {
			return null;
		}

		@Override
		public FolderMetaData getJasperFolderMetaData(final String folderUri) {
			return null;
		}

		@Override
		public VersionInfo getVersionInfo() {
			return new VersionInfo();
		}

		@Override
		public Response getPreGeneratedReportOutput(String fileName) {
			return null;
		}

		@Override
		public Response getReportExecutionStatus(String requestID) {
			return null;
		}
	}
}
