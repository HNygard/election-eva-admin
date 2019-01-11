package no.valg.eva.admin.valgnatt.domain.model.grunnlagsdata;

import java.util.Map;
import java.util.TreeMap;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import no.valg.eva.admin.configuration.domain.model.valgnatt.ElectoralRollCount;

/**
 *
 */
public class CountyElectoralRoll extends BaseElectoralRoll {
    
    private Map<String, MunicipalityElectoralRoll> municipalityElectoralRolls = new TreeMap<>();

    public CountyElectoralRoll(String countyId, String countyName) {
        super(countyId, countyName);
    }

    void add(ElectoralRollCount electoralRollCount) {
        MunicipalityElectoralRoll municipalityElectoralRoll;
        if (municipalityElectoralRolls.containsKey(electoralRollCount.getMunicipalityId())) {
            municipalityElectoralRoll = municipalityElectoralRolls.get(electoralRollCount.getMunicipalityId());
        } else {
            municipalityElectoralRoll = new MunicipalityElectoralRoll(electoralRollCount.getMunicipalityId(),
					electoralRollCount.getMunicipalityName(), electoralRollCount.getValgdistriktId());
            municipalityElectoralRolls.put(electoralRollCount.getMunicipalityId(), municipalityElectoralRoll);
        }
        municipalityElectoralRoll.add(electoralRollCount);
        incrementNoOfVoters(electoralRollCount);
    }

    JsonObject asJsonObject() {
        JsonObjectBuilder builder = Json.createObjectBuilder();
        builder.add("fylkesnummer", this.getAreaId());
        builder.add("fylke", this.getAreaName());
        builder.add("stemmeberettigede", getVoterTotal());

        JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        for (MunicipalityElectoralRoll municipalityElectoralRoll : municipalityElectoralRolls.values()) {
            arrayBuilder.add(municipalityElectoralRoll.asJsonObject());
        }

        builder.add("kommuner", arrayBuilder.build());

        return builder.build();
    }
}
