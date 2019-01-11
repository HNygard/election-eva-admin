package no.valg.eva.admin.common.auditlog.auditevents;

import java.util.List;

import javax.json.Json;
import javax.json.JsonArrayBuilder;

import no.valg.eva.admin.common.auditlog.JsonBuilder;
import no.valg.eva.admin.configuration.domain.model.Candidate;

public class CandidateJsonBuilder {

	public String toJson(Candidate candidate) {
		JsonBuilder builder = new JsonBuilder();
		buildCandidateJson(candidate, null, null, builder);
		return builder.toJson();
	}

	public String toJson(List<Candidate> candidates) {

		JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
		for (Candidate candidate : candidates) {
			JsonBuilder builder = new JsonBuilder();
			buildCandidateJson(candidate, null, null, builder);
			arrayBuilder.add(builder.asJsonObject());
		}
		return arrayBuilder.build().toString();
	}

	public String toJson(Candidate candidate, Integer reorderFrom, Integer reorderTo) {
		JsonBuilder builder = new JsonBuilder();
		buildCandidateJson(candidate, reorderFrom, reorderTo, builder);
		return builder.toJson();
	}

	private void buildCandidateJson(Candidate candidate, Integer reorderFrom, Integer reorderTo, JsonBuilder builder) {
		String contestName = getContestName(candidate);
		String partyId = getPartyId(candidate);

		builder.add("contestName", contestName);
		builder.add("partyId", partyId);

		if (candidate.isIdSet()) {
			builder.add("id", candidate.getId());
		} else {
			builder.addNull("id");
		}

		builder.add("firstName", candidate.getFirstName());
		builder.add("middleName", candidate.getMiddleName());
		builder.add("lastName", candidate.getLastName());
		builder.addDate("dateOfBirth", candidate.getDateOfBirth());

		if (reorderFrom != null && reorderTo != null) {
			builder.add("reorderFrom", reorderFrom);
			builder.add("reorderTo", reorderTo);
		} else {
			builder.add("displayOrder", candidate.getDisplayOrder());
		}
	}

	private String getContestName(Candidate candidate) {
		try {
			if (candidate.getBallot() != null) {
				if (candidate.getBallot().getContest() != null) {
					return candidate.getBallot().getContest().getName();
				}
			}
			return null;
		} catch (Exception e) {
			return null; // in case of uninitialized proxies
		}
	}

	private String getPartyId(Candidate candidate) {
		try {
			if (candidate.getAffiliation() != null) {
				if (candidate.getAffiliation().getParty() != null) {
					return candidate.getAffiliation().getParty().getId();
				}
			}
			return null;
		} catch (Exception e) {
			return null; // in case of uninitialized proxies
		}
	}
}
