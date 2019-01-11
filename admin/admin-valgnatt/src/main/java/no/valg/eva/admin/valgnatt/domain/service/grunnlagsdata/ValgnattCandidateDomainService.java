package no.valg.eva.admin.valgnatt.domain.service.grunnlagsdata;

import static java.util.stream.Collectors.toList;
import static no.valg.eva.admin.valgnatt.domain.model.resultat.ValgnattskjemaJsonBuilderFactory.ANTALL_MANDATER;
import static no.valg.eva.admin.valgnatt.domain.model.resultat.ValgnattskjemaJsonBuilderFactory.BOSTED;
import static no.valg.eva.admin.valgnatt.domain.model.resultat.ValgnattskjemaJsonBuilderFactory.FODSELSDATO;
import static no.valg.eva.admin.valgnatt.domain.model.resultat.ValgnattskjemaJsonBuilderFactory.GVPK_GRUNNLAG_VALGDISTRIKTER_PARTIER_KANDIDATER;
import static no.valg.eva.admin.valgnatt.domain.model.resultat.ValgnattskjemaJsonBuilderFactory.KANDIDATER;
import static no.valg.eva.admin.valgnatt.domain.model.resultat.ValgnattskjemaJsonBuilderFactory.KJONN;
import static no.valg.eva.admin.valgnatt.domain.model.resultat.ValgnattskjemaJsonBuilderFactory.NAVN;
import static no.valg.eva.admin.valgnatt.domain.model.resultat.ValgnattskjemaJsonBuilderFactory.PARTIER;
import static no.valg.eva.admin.valgnatt.domain.model.resultat.ValgnattskjemaJsonBuilderFactory.PARTIKATEGORI;
import static no.valg.eva.admin.valgnatt.domain.model.resultat.ValgnattskjemaJsonBuilderFactory.PARTIKODE;
import static no.valg.eva.admin.valgnatt.domain.model.resultat.ValgnattskjemaJsonBuilderFactory.PARTINAVN;
import static no.valg.eva.admin.valgnatt.domain.model.resultat.ValgnattskjemaJsonBuilderFactory.PLASSNUMMER;
import static no.valg.eva.admin.valgnatt.domain.model.resultat.ValgnattskjemaJsonBuilderFactory.VALGDISTRIKT;
import static no.valg.eva.admin.valgnatt.domain.model.resultat.ValgnattskjemaJsonBuilderFactory.VALGDISTRIKTER;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;

import no.evote.exception.EvoteException;
import no.valg.eva.admin.common.Foedselsnummer;
import no.valg.eva.admin.common.auditlog.JsonBuilder;
import no.valg.eva.admin.common.configuration.model.ballot.PartyData;
import no.valg.eva.admin.configuration.domain.model.Candidate;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.ContestArea;
import no.valg.eva.admin.configuration.domain.model.Election;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.repository.BallotRepository;
import no.valg.eva.admin.configuration.repository.CandidateRepository;
import no.valg.eva.admin.valgnatt.domain.model.grunnlagsdata.candidates.CandidatesReport;
import no.valg.eva.admin.valgnatt.domain.model.grunnlagsdata.candidates.ContestHolder;
import no.valg.eva.admin.valgnatt.domain.model.resultat.ValgnattskjemaJsonBuilderFactory;

import org.apache.log4j.Logger;

public class ValgnattCandidateDomainService {

	private static final Logger LOGGER = Logger.getLogger(ValgnattCandidateDomainService.class);

	private final BallotRepository ballotRepository;
	private final CandidateRepository candidateRepository;

	private final ContestHolder contestHolder;
	private final ValgnattskjemaJsonBuilderFactory valgnattskjemaJsonBuilderFactory = new ValgnattskjemaJsonBuilderFactory();

	public ValgnattCandidateDomainService(Collection<Contest> contests, boolean sametingsvalg, BallotRepository ballotRepository,
										  CandidateRepository candidateRepository) {
		this.ballotRepository = ballotRepository;
		this.candidateRepository = candidateRepository;

		Map<Long, MvArea> contestMvAreaMap = new HashMap<>();
		for (Contest contest : contests) {
			MvArea contestMvArea = findContestMvArea(contest, sametingsvalg);

			contestMvAreaMap.put(contest.getPk(), contestMvArea);
		}

		contestHolder = new ContestHolder(contests, contestMvAreaMap);
	}

	private MvArea findContestMvArea(final Contest contest, final boolean sametingsvalg) {
		List<ContestArea> contestAreasForContest = contest.getContestAreaList();
		if (!sametingsvalg) {
			return contestAreasForContest.get(0).getMvArea();
		}
		for (ContestArea contestArea : contestAreasForContest) {
			if (contestArea.isMunicipalityForSamiDistrict()) {
				return contestArea.getMvArea();
			}
		}
		throw new EvoteException("Fant ikke mv_area for contest (contest_pk=" + contest.getPk() + ") i sametingsvalg.");
	}

	/**
	 * @return JSON representation of Candidates for each Contest
	 */
	public CandidatesReport createCandidatesReport() {

		Contest aContest = contestHolder.getContests().iterator().next();
		Election election = aContest.getElection();
		ElectionEvent electionEvent = election.getElectionGroup().getElectionEvent();
		String electionYear = electionEvent.getElectionDays().iterator().next().electionYear();

		JsonBuilder builder = valgnattskjemaJsonBuilderFactory.createJsonBuilder(GVPK_GRUNNLAG_VALGDISTRIKTER_PARTIER_KANDIDATER, electionEvent.getId(),
				electionEvent.getName(), election.getValgtype(),
				electionYear, election.getLevelingSeats());
		builder.add(VALGDISTRIKTER, createContestArrayBuilder().build());

		return new CandidatesReport(builder.toJson());
	}

	/**
	 * Creates json array builder for all contests in election. This is a CPU intensive operation.
	 * A parallel stream shortens response time to about 30 seconds, down from 30 minutes, for kommunestyrevalg with over 400 contests.
	 * @return json array builder with json data for all contests
	 */
	private JsonArrayBuilder createContestArrayBuilder() {
		JsonArrayBuilder contestArrayBuilder = Json.createArrayBuilder();

		List<JsonComparableContestBuilder> jsonObjectBuilders = contestHolder.getContests()
				.parallelStream()
				.map(this::createJsonContestBuilder)
				.sorted()
				.collect(toList());

		jsonObjectBuilders
				.stream()
				.map(JsonComparableContestBuilder::build)
				.forEach(jsonObject -> contestArrayBuilder.add(jsonObject));

		return contestArrayBuilder;
	}

	private JsonComparableContestBuilder createJsonContestBuilder(Contest contest) {
		JsonObjectBuilder contestBuilder = Json.createObjectBuilder();
		contestBuilder.add(VALGDISTRIKT, contestHolder.getMvArea(contest.getPk()).getAreaId());
		contestBuilder.add(NAVN, contestHolder.getMvArea(contest.getPk()).getAreaName());
		contestBuilder.add(ANTALL_MANDATER, contest.getNumberOfPositions());

		JsonArrayBuilder partyArrayBuilder = Json.createArrayBuilder();

		List<PartyData> partyDataList = findPartyData(contest);
		for (PartyData partyData : partyDataList) {

			LOGGER.debug(contest.getName() + ": Prosesserer parti: " + partyData.getPartyId() + ", antall kandidater: " + partyData.getCandidateList().size());

			JsonObjectBuilder partyBuilder = Json.createObjectBuilder();
			partyBuilder.add(PARTIKODE, partyData.getPartyId());
			partyBuilder.add(PARTINAVN, partyData.getPartyName());
			partyBuilder.add(PARTIKATEGORI, partyData.getPartyCategoryId());

			JsonArrayBuilder candidateArrayBuilder = Json.createArrayBuilder();
			for (Candidate candidate : partyData.getCandidateList()) {

				JsonObjectBuilder candidateBuilder = Json.createObjectBuilder();
				candidateBuilder.add(PLASSNUMMER, candidate.getDisplayOrder());
				candidateBuilder.add(NAVN, candidate.getNameLine());
				Foedselsnummer foedselsnummer = new Foedselsnummer(candidate.getId());
				candidateBuilder.add(FODSELSDATO, foedselsnummer.dateOfBirth().toString());
				candidateBuilder.add(BOSTED, candidate.getResidence() != null ? candidate.getResidence() : "");
				candidateBuilder.add(KJONN, foedselsnummer.gender());

				candidateArrayBuilder.add(candidateBuilder.build());
			}
			partyBuilder.add(KANDIDATER, candidateArrayBuilder.build());
			partyArrayBuilder.add(partyBuilder.build());
		}
		contestBuilder.add(PARTIER, partyArrayBuilder.build());
		return new JsonComparableContestBuilder(Integer.valueOf(contest.getId()), contestBuilder);
	}

	private List<PartyData> findPartyData(Contest contest) {
		List<PartyData> partyDataList = ballotRepository.partiesForContest(contest);
		for (PartyData partyData : partyDataList) {
			partyData.addCandidates(candidateRepository.findByAffiliation(partyData.getAffiliationPk().longValue()));
		}
		partyDataList.add(PartyData.createBlanke());
		return partyDataList;
	}
}
