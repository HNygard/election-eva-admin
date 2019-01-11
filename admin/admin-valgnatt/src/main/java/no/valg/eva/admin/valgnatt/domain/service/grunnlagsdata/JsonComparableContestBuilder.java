package no.valg.eva.admin.valgnatt.domain.service.grunnlagsdata;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

/**
 * Wraps a JsonObjectBuilder and associates it with contestId for sorting.
 */
class JsonComparableContestBuilder implements Comparable<JsonComparableContestBuilder> {
    
    private final Integer contestId;
    private final JsonObjectBuilder jsonObjectBuilder;

    JsonComparableContestBuilder(Integer contestId, JsonObjectBuilder jsonObjectBuilder) {
        this.contestId = contestId;
        this.jsonObjectBuilder = jsonObjectBuilder;
    }

    public JsonObject build() {
        return jsonObjectBuilder.build();
    }

    @Override
    public int compareTo(JsonComparableContestBuilder builder) {
        return this.contestId.compareTo(builder.contestId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        JsonComparableContestBuilder that = (JsonComparableContestBuilder) o;

        return contestId.equals(that.contestId);

    }

    @Override
    public int hashCode() {
        return contestId.hashCode();
    }
    
}
