package no.valg.eva.admin.common.auditlog;

import java.io.StringWriter;
import java.util.List;
import java.util.Set;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;
import javax.json.JsonWriter;

import no.valg.eva.admin.configuration.domain.model.Locale;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.base.AbstractPartial;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class JsonBuilder {
	private final JsonObjectBuilder objectBuilder = Json.createObjectBuilder();
	private DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("yyyy-MM-dd");
	private DateTimeFormatter timeFormatter = DateTimeFormat.forPattern("HH:mm");
	private DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ssZ");

	public JsonBuilder add(String name, String value) {
		if (value != null) {
			objectBuilder.add(name, value);
		} else {
			objectBuilder.addNull(name);
		}
		return this;
	}

	public JsonBuilder add(String name, Boolean value) {
		if (value != null) {
			objectBuilder.add(name, value);
		} else {
			objectBuilder.addNull(name);
		}
		return this;
	}

	public JsonBuilder add(String name, Integer value) {
		if (value != null) {
			objectBuilder.add(name, value);
		} else {
			objectBuilder.addNull(name);
		}
		return this;
	}

	public JsonBuilder addDateTime(String name, DateTime value) {
		if (value != null) {
			objectBuilder.add(name, dateTimeFormatter.print(value));
		} else {
			objectBuilder.addNull(name);
		}
		return this;
	}

	public JsonBuilder addDate(String name, LocalDate value) {
		return addTemporalValue(name, value, dateFormatter);
	}

	public JsonBuilder addTime(String name, LocalTime value) {
		return addTemporalValue(name, value, timeFormatter);
	}

	public JsonBuilder addTemporalValue(String name, AbstractPartial dateAndOrTime, DateTimeFormatter temporalFormatter) {
		if (dateAndOrTime != null) {
			objectBuilder.add(name, temporalFormatter.print(dateAndOrTime));
		} else {
			objectBuilder.addNull(name);
		}
		return this;
	}

	public JsonBuilder addLocales(String name, Set<Locale> locales) {
		JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
		for (Locale locale : locales) {
			JsonObjectBuilder localeObjectBuilder = Json.createObjectBuilder();
			localeObjectBuilder.add("id", locale.getId());
			arrayBuilder.add(localeObjectBuilder);
		}
		objectBuilder.add(name, arrayBuilder);
		return this;
	}

	public JsonBuilder add(String name, Long value) {
		if (value != null) {
			objectBuilder.add(name, value);
		} else {
			objectBuilder.addNull(name);
		}
		return this;
	}

	public JsonBuilder addNull(String name) {
		objectBuilder.addNull(name);
		return this;
	}

	public static String jsonArrayToString(JsonArray jsonArray) {
		StringWriter writer = new StringWriter();
		JsonWriter jsonWriter = Json.createWriter(writer);
		jsonWriter.writeArray(jsonArray);
		return writer.toString();
	}

	public static String jsonObjectToString(JsonObject jsonObject) {
		StringWriter writer = new StringWriter();
		JsonWriter jsonWriter = Json.createWriter(writer);
		jsonWriter.writeObject(jsonObject);
		return writer.toString();
	}

	public String toJson() {
		return jsonObjectToString(objectBuilder.build());
	}

	public JsonObject asJsonObject() {
		return objectBuilder.build();
	}

	public JsonBuilder add(String name, JsonArray array) {
		objectBuilder.add(name, array);
		return this;
	}

	public JsonBuilder add(String name, JsonValue jsonValue) {
		objectBuilder.add(name, jsonValue);
		return this;
	}

	public JsonBuilder add(String name, JsonObjectBuilder builder) {
		objectBuilder.add(name, builder);
		return this;
	}

	public JsonBuilder addStringArray(String name, List<String> strings) {
		JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
		for (String string : strings) {
			arrayBuilder.add(string);
		}
		objectBuilder.add(name, arrayBuilder);
		return this;
	}
}
