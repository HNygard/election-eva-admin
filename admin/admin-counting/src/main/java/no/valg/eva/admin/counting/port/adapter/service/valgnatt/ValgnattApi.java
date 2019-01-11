package no.valg.eva.admin.counting.port.adapter.service.valgnatt;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * This is a JAX-RS specification for Valgnatt REST API used by Admin for uploading election results
 */

public interface ValgnattApi {

	@POST
	@Path("valg_mottak/")
    @Produces(MediaType.APPLICATION_JSON + ";" + MediaType.CHARSET_PARAMETER + "=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
	void upload(String valgnattJsonRequest);

}
