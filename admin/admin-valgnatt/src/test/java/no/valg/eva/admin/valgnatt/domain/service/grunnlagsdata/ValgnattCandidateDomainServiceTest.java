package no.valg.eva.admin.valgnatt.domain.service.grunnlagsdata;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import no.valg.eva.admin.common.configuration.model.ballot.PartyData;
import no.valg.eva.admin.configuration.domain.model.Candidate;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.ContestArea;
import no.valg.eva.admin.configuration.domain.model.Election;
import no.valg.eva.admin.configuration.domain.model.ElectionDay;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.ElectionGroup;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.Valgtype;
import no.valg.eva.admin.configuration.repository.BallotRepository;
import no.valg.eva.admin.configuration.repository.CandidateRepository;
import no.valg.eva.admin.valgnatt.domain.model.grunnlagsdata.candidates.ContestHolder;

import org.joda.time.LocalDate;
import org.testng.annotations.Test;


public class ValgnattCandidateDomainServiceTest {

	private static final long CONTEST_OSTFOLD_PK = 495L;
	private static final long CONTEST_VESTFOLD_PK = 1L;
	private static final int ANTALL_KANDIDATER_OSTFOLD = 5;
	private static final int ANTALL_KANDIDATER_VESTFOLD = 7;
	
	@Test
	public void createCandidatesReport_returnsJson() {
        BallotRepository fakeBallotRepository = mock(BallotRepository.class);
        List<PartyData> partyDataList0 = createPartyDataList();
        List<PartyData> partyDataList1 = createPartyDataList();
        List<Contest> contests = new ArrayList<>(createContestHolder().getContests());
        when(fakeBallotRepository.partiesForContest(contests.get(0))).thenReturn(partyDataList0);
        when(fakeBallotRepository.partiesForContest(contests.get(1))).thenReturn(partyDataList1);
        CandidateRepository fakeCandidateRepository = mock(CandidateRepository.class);
        ValgnattCandidateDomainService rs = new ValgnattCandidateDomainService(contests, false, fakeBallotRepository, fakeCandidateRepository);

		String jsonReport = rs.createCandidatesReport().toJson();
		assertThat(jsonReport).contains("2017");
		assertThat(jsonReport).contains("antall_utjevningsmandater");
	}

	private List<PartyData> createPartyDataList() {
		List<PartyData> partyDataList = new ArrayList<>();
		partyDataList.add(createPartyData("V", "Venstre", "1"));
		partyDataList.add(createPartyData("H", "Høyre", "1"));
		return partyDataList;
	}

	private PartyData createPartyData(final String partyId, final String partyName, String partiCategoryId) {
		PartyData partyData = new PartyData(partyId, partyName, partiCategoryId, 1);
		partyData.addCandidates(createCandidateList());
		return partyData;
	}

	private List<Candidate> createCandidateList() {
		List<Candidate> kandidatListe = new ArrayList<>();
		kandidatListe.add(createCandidate("Halgeir I. Bremnes", "11111111111", "Fredrikstad"));
		kandidatListe.add(createCandidate("Vivian Dolen Sørdal", "11111111111", "Sarpsborg"));
		kandidatListe.add(createCandidate("Jarl Arthur Dyrvik", "11111111111", "Trøgstad"));
		kandidatListe.add(createCandidate("Olaf Reppe", "11111111111", "Moss"));
		return kandidatListe;
	}

	private Candidate createCandidate(final String nameLine, final String fnr, String bosted) {
		Candidate candidate = new Candidate();
		candidate.setNameLine(nameLine);
		candidate.setId(fnr);
		candidate.setResidence(bosted);
		
		candidate.setDateOfBirth(new LocalDate(1974, 7, 22));
		
		return candidate;
	}

	private ContestHolder createContestHolder() {
		List<Contest> contests = new ArrayList<>();
        MvArea ostfoldArea = createMvArea("Østfold", "01");
        Contest ostfold = createContest(CONTEST_OSTFOLD_PK, "Østfold", ANTALL_KANDIDATER_OSTFOLD, ostfoldArea);
        contests.add(ostfold);
        MvArea vestfoldArea = createMvArea("Vestfold", "02");
        Contest vestfold = createContest(CONTEST_VESTFOLD_PK, "Vestfold", ANTALL_KANDIDATER_VESTFOLD, vestfoldArea);
        contests.add(vestfold);
        Map<Long, MvArea> contestMvAreaMap = new HashMap<>();
        contestMvAreaMap.put(ostfold.getPk(), ostfoldArea);
        contestMvAreaMap.put(vestfold.getPk(), vestfoldArea);
		
		return new ContestHolder(contests, contestMvAreaMap);
	}

	private MvArea createMvArea(String name, String countyId) {
		MvArea mvArea = new MvArea();
		mvArea.setAreaLevel(2);
		mvArea.setCountyName(name);
		mvArea.setCountyId(countyId);
		mvArea.setAreaPath("950001.47." + countyId);
		return mvArea;
	}

	private Contest createContest(Long pk, String name, int positions, MvArea ostfoldArea) {
		Contest contest = new Contest();
		contest.setPk(pk);
        contest.setId(pk.toString());
		contest.setName(name);
		contest.setNumberOfPositions(positions);
		contest.setElection(createElection());
        contest.setContestAreaSet(createContestAreas(contest, ostfoldArea));
		return contest;
	}

    private Set<ContestArea> createContestAreas(Contest contest, MvArea mvArea) {
        Set<ContestArea> contestAreas = new HashSet<>();
        contestAreas.add(createContestArea(contest, mvArea));
        return contestAreas;
    }

    private ContestArea createContestArea(Contest contest, MvArea mvArea) {
        ContestArea contestArea = new ContestArea();
        contestArea.setMvArea(mvArea);
        contestArea.setContest(contest);
        return contestArea;
    }

    private Election createElection() {
		Election election = new Election();
		election.setValgtype(Valgtype.STORTINGSVALG);
		election.setElectionGroup(createElectionGroup());
		return election;
	}

	private ElectionGroup createElectionGroup() {
		ElectionGroup eg = new ElectionGroup();
		eg.setElectionEvent(createElectionEvent());
		return eg;
	}

	private ElectionEvent createElectionEvent() {
		ElectionEvent ee = new ElectionEvent();
		ee.setElectionDays(createElectionDays());
		ee.setId("970001");
		ee.setName("Stortingsvalget 2017");
		return ee;
	}

	private Set<ElectionDay> createElectionDays() {
		Set<ElectionDay> electionDays = new HashSet<>();
		electionDays.add(createElectionDay());
		return electionDays;
	}

	private ElectionDay createElectionDay() {
		ElectionDay ed = new ElectionDay();
		
		ed.setDate(new LocalDate(2017, 9, 11));
		
		return ed;
	}
}
