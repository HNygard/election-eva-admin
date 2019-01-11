package no.valg.eva.admin.backend.reporting.jasperserver.api;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * This is a JAX-RS specification for the Jasper Server REST API and represents a sub set of the JasperServer REST API used by EVA
 */
@Path("/rest_v2")
@Produces(MediaType.APPLICATION_XML)
@Consumes(MediaType.APPLICATION_XML)

public interface JasperRestApi {

	/**
	 * Retrieves description of one resource identified by its uri
	 * @param uri
	 *            resource' uri
	 * @return A description of the resource
	 */
	@GET
	@Path("resources{uri:.*}")
	@Produces("application/repository.reportUnit+xml")
	JasperReport getJasperReportUnit(@PathParam("uri") String uri);

	/**
	 * Retrieves input controls for a report identified by supplied uri. Note that uri supplied from JasperServer should be stripped of leading '/'.
	 * @param uri
	 * @return
	 */
	@GET
	@Path("reports/{reportUri}/inputControls")
	List<InputControl> getInputControls(@PathParam("reportUri") String uri);

	@GET
	@Path("resources{uri:.*}")
	@Produces("application/repository.inputControl+xml")
	InputControl getInputControl(@PathParam("uri") String uri);

	@GET
	@Path("resources{uri:.*}")
	@Produces("application/repository.dataType+xml")
	DataType getDataType(@PathParam("uri") String uri);

	/**
	 * Retrieve a list of resources of supplied type
	 * 
	 * @param type
	 *            e.g. {@link JasperRestApi.ResourceType#reportUnit}
	 * @return a list of resources
	 */
	@GET
	@Path("resources")
	@Produces(MediaType.APPLICATION_XML)
	JasperResources getResources(@QueryParam("type") ResourceType type, @QueryParam("folderUri") @DefaultValue("/reports/EVA") String folderUri);

	/**
	 * See <a href="http://community.jaspersoft.com/documentation/jasperreports-server-web-services-guide/v550/running-report-asynchronously">JasperServer REST
	 * Documentation</a>
	 * @param jasperExecutionRequest
	 * @param userLocale
	 * @return
	 */
	@POST
	@Path("reportExecutions")
	JasperExecution executeReport(JasperExecutionRequest jasperExecutionRequest, @QueryParam("userLocale") String userLocale);

	/**
	 * @param requestID
	 *            request id
	 * @param exportID
	 *            export id (usually "pdf")
	 * @return
	 */
	@GET
	@Path("reportExecutions/{requestID}/exports/{exportID}/outputResource")
	@Produces({ "application/pdf", MediaType.APPLICATION_XML })
	Response getReportOutput(@PathParam("requestID") String requestID,
			@PathParam("exportID") @DefaultValue("pdf") String exportID,
			@QueryParam("userLocale") String locale);

	@GET
	@Path("resources{uri:.*metadata\\.xml}")
	@Produces(MediaType.APPLICATION_XML)
	ReportMetaData getReportMetaData(@PathParam("uri") String uri);

	@GET
	@Path("resources{uri:.*}")
	@Produces("application/repository.folder+xml")
	JasperFolder getJasperFolder(@PathParam("uri") String folderUri);

	@GET
	@Path("resources{folderUri}/metadata.xml")
	@Produces(MediaType.APPLICATION_XML)
	FolderMetaData getJasperFolderMetaData(@PathParam("folderUri") String folderUri);

	@GET
	@Path("/resources/reports/EVA/versionInfo/version_info.xml")
	@Produces(MediaType.APPLICATION_XML)
	VersionInfo getVersionInfo();

	@GET
	@Path("/resources/reports/PregeneratedOutput/{fileName}")
	Response getPreGeneratedReportOutput(@PathParam("fileName") String fileName);

	@GET
	@Path("reportExecutions/{requestID}/status/")
	Response getReportExecutionStatus(@PathParam("requestID") String requestID);

	enum ResourceType {
		reportUnit
	}
}
