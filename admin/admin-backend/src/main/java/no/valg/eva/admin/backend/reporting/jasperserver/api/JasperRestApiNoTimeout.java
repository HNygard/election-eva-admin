package no.valg.eva.admin.backend.reporting.jasperserver.api;

import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * This is a marker interface to be exposed from a client stub without connection and socket timeout
 */
@Path("/rest_v2")
@Produces(MediaType.APPLICATION_XML)
@Consumes(MediaType.APPLICATION_XML)
public interface JasperRestApiNoTimeout extends JasperRestApi {
}
