package no.valg.eva.admin.valgnatt.domain.model.grunnlagsdata;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

public class PollingDistrictElectoralRoll extends BaseElectoralRoll {

	private final String boroughId;
    private final String boroughName;

    public PollingDistrictElectoralRoll(String id, String name, int voterTotal, String boroughId, String boroughName) {
		super(id, name);
        this.voterTotal = voterTotal;
        this.boroughId = boroughId;
        this.boroughName = boroughName;
    }

	JsonObject asJsonObject() {
		JsonObjectBuilder builder = Json.createObjectBuilder();
		builder.add("kretsnummer", this.getAreaId());
		builder.add("kretsnavn", this.getAreaName());
		builder.add("stemmeberettigede", getVoterTotal());
		builder.add("bydelsnummer", boroughId);
		builder.add("bydelsnavn", boroughName);
		return builder.build();
	}
}
