package no.valg.eva.admin.valgnatt.domain.model.resultat;

import java.io.Serializable;

import no.valg.eva.admin.common.auditlog.JsonBuilder;
import no.valg.eva.admin.configuration.domain.model.Valgtype;

import org.joda.time.DateTime;

public class ValgnattskjemaJsonBuilderFactory implements Serializable {

	public static final String GVPK_GRUNNLAG_VALGDISTRIKTER_PARTIER_KANDIDATER = "GVPK (Grunnlag Valgdistrikter/Partier/Kandidater)";
	public static final String VALGDISTRIKT = "valgdistrikt";
	public static final String NAVN = "navn";
	public static final String PARTIKODE = "partikode";
	public static final String PARTINAVN = "partinavn";
	public static final String PARTIKATEGORI = "partikategori";
	public static final String PLASSNUMMER = "plassnummer";
	public static final String FODSELSDATO = "fødselsdato";
	public static final String BOSTED = "bosted";
	public static final String KJONN = "kjønn";
	public static final String KANDIDATER = "kandidater";
	public static final String PARTIER = "partier";
	public static final String VALGDISTRIKTER = "valgdistrikter";
	public static final String ANTALL_MANDATER = "antall_mandater";
	private static final String VALGNATT_DATO_FORMAT = "yyyyMMdd-HHmmss";
	private static final String TIDSPUNKT = "tidspunkt";
	private static final String SKJEMATYPE = "skjematype";
	private static final String VALGHENDELSE = "valghendelse";
	private static final String VALG = "valg";
	private static final String VALGTYPE = "valgtype";
	private static final String VALGÅR = "valgår";
	private static final String ANTALL_UTJEVNINGSMANDATER = "antall_utjevningsmandater";

	public JsonBuilder createJsonBuilder(String type, String electionEventId, String electionEventName, Valgtype valgtype, String electionYear,
										 int utjevningsmandater) {
		JsonBuilder builder = createJsonBuilder(type, electionEventId, electionEventName, valgtype, electionYear);
		builder.add(ANTALL_UTJEVNINGSMANDATER, utjevningsmandater);
		return builder;
	}

	public JsonBuilder createJsonBuilder(String type, String electionEventId, String electionEventName, Valgtype valgtype, String electionYear) {
		JsonBuilder builder = new JsonBuilder();
		builder.add(TIDSPUNKT, new DateTime().toString(VALGNATT_DATO_FORMAT));
		builder.add(SKJEMATYPE, type);
		builder.add(VALGHENDELSE, electionEventId);
		builder.add(VALG, electionEventName);
		builder.add(VALGTYPE, valgtype != null ? valgtype.getId() : "");
		builder.add(VALGÅR, electionYear);
		return builder;
	}
}
