package no.valg.eva.admin.valgnatt.domain.model.grunnlagsdata;

import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import no.valg.eva.admin.configuration.domain.model.valgnatt.ElectoralRollCount;

public class MunicipalityElectoralRoll extends BaseElectoralRoll {

	private Set<PollingDistrictElectoralRoll> pollingDistrictElectoralRolls = new TreeSet(new Comparator<PollingDistrictElectoralRoll>() {
		@Override
		public int compare(PollingDistrictElectoralRoll o1, PollingDistrictElectoralRoll o2) {
			return o1.getAreaId().compareTo(o2.getAreaId());
		}
	});
    private final String valgdistrikt;

    public MunicipalityElectoralRoll(String id, String name, String valgdistrikt) {
		super(id, name);
        this.valgdistrikt = valgdistrikt;
	}

	void add(ElectoralRollCount electoralRollCount) {
		pollingDistrictElectoralRolls.add(new PollingDistrictElectoralRoll(
				electoralRollCount.getPollingDistrictId(),
				electoralRollCount.getPollingDistrictName(),
                electoralRollCount.getVoterTotal(),
				electoralRollCount.getBoroughId(),
                electoralRollCount.getBoroughName()));
		incrementNoOfVoters(electoralRollCount);
	}

	JsonObject asJsonObject() {
		JsonObjectBuilder builder = Json.createObjectBuilder();
		builder.add("kommunenummer", this.getAreaId());
		builder.add("kommune", this.getAreaName());
		builder.add("stemmeberettigede", getVoterTotal());
		builder.add("valgdistrikt", this.valgdistrikt);

		JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
		for (PollingDistrictElectoralRoll pollingDistrictElectoralRoll : pollingDistrictElectoralRolls) {
			arrayBuilder.add(pollingDistrictElectoralRoll.asJsonObject());
		}
		builder.add("kretser", arrayBuilder.build());

		return builder.build();
	}
}
