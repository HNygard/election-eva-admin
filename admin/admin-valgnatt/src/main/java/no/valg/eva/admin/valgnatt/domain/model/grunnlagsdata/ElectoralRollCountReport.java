package no.valg.eva.admin.valgnatt.domain.model.grunnlagsdata;

import java.util.Map;
import java.util.TreeMap;

import javax.json.Json;
import javax.json.JsonArrayBuilder;

import no.valg.eva.admin.common.auditlog.JsonBuilder;
import no.valg.eva.admin.common.auditlog.auditevents.valgnatt.ValgnattAuditable;
import no.valg.eva.admin.configuration.domain.model.Valgtype;
import no.valg.eva.admin.configuration.domain.model.valgnatt.ElectoralRollCount;
import no.valg.eva.admin.valgnatt.domain.model.resultat.ValgnattskjemaJsonBuilderFactory;

/**
 * Root node in hierarchy for representing electoral roll for Valgnatt / EVA Resultat.
 */
public class ElectoralRollCountReport extends BaseElectoralRoll implements ValgnattAuditable {

	private final Valgtype valgtype;
	private final String electionYear;
	private final ValgnattskjemaJsonBuilderFactory valgnattskjemaJsonBuilderFactory = new ValgnattskjemaJsonBuilderFactory();

	private Map<String, CountyElectoralRoll> countyElectoralRolls = new TreeMap<>();

	public ElectoralRollCountReport(String id, String electionName, Valgtype valgtype, String electionYear) {
		super(id, electionName);
		this.valgtype = valgtype;
		this.electionYear = electionYear;
	}

	public void add(ElectoralRollCount electoralRollCount) {
		CountyElectoralRoll countyElectoralRoll;
		if (countyElectoralRolls.containsKey(electoralRollCount.getCountyId())) {
			countyElectoralRoll = countyElectoralRolls.get(electoralRollCount.getCountyId());
		} else {
			countyElectoralRoll = new CountyElectoralRoll(electoralRollCount.getCountyId(), electoralRollCount.getCountyName());
			countyElectoralRolls.put(electoralRollCount.getCountyId(), countyElectoralRoll);
		}
		countyElectoralRoll.add(electoralRollCount);
		incrementNoOfVoters(electoralRollCount);
	}

	@Override
	public String toJson() {
		JsonBuilder builder = valgnattskjemaJsonBuilderFactory.createJsonBuilder("GSK (Grunnlag Stemmekretser)", getAreaId(), getAreaName(), valgtype, electionYear);
        builder.add("stemmeberettigede", getVoterTotal());

        JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        for (CountyElectoralRoll countyElectoralRoll : countyElectoralRolls.values()) {
            arrayBuilder.add(countyElectoralRoll.asJsonObject());
        }
        
        builder.add("fylker", arrayBuilder.build());
        return builder.toJson();
	}
}
