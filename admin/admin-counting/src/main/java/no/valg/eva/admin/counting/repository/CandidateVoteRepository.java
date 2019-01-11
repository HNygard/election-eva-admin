package no.valg.eva.admin.counting.repository;

import lombok.NoArgsConstructor;
import no.evote.constants.AreaLevelEnum;
import no.valg.eva.admin.backend.common.repository.BaseRepository;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.configuration.domain.model.Party;
import no.valg.eva.admin.configuration.repository.party.PartyRepository;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

@NoArgsConstructor
public class CandidateVoteRepository extends BaseRepository {

    @Inject
    private PartyRepository partyRepository;

    private static final List<String> COLUMN_HEADER = Arrays.asList(
            "Liste",
            "Kandidatnr",
            "Ny rangering",
            "Navn",
            "Personstemmer"
    );

	public List<List<String>> findCandidateVotes(AreaPath areaPath, ElectionPath electionPath) {
        Set<String> parties = getPartyIdsForWriteins(areaPath, electionPath);
        String query = buildQuery(parties);

        List<String> header = new ArrayList<>(COLUMN_HEADER);
        header.addAll(parties);

        List<List<String>> rows = new ArrayList<>();
        rows.add(header);

        rows.addAll(getQueryResults(header.size(), getEm()
                .createNativeQuery(query)
                .setParameter("electionPath", electionPath.toString())
                .getResultList()));

        return rows;
    }

    private Set<String> getPartyIdsForWriteins(AreaPath areaPath, ElectionPath electionPath) {
        if (!areaPath.getLevel().equals(AreaLevelEnum.MUNICIPALITY)) {
            return new HashSet<>();
        }
        List<Party> parties = partyRepository.findAllForAreaPathAndElectionPath(areaPath, electionPath);
        return parties.stream()
                .map(Party::getId)
                .collect(toSet());
    }

    private String buildQuery(Set<String> parties) {
        StringBuilder query = new StringBuilder();
        query.append("with modified as ( " +
                "    select " +
                "      ca.candidate_pk, p2.party_id, vc.vote_category_id, floor(cvc.votes) as votes " +
                "    from candidate ca " +
                "      join candidate_vote_count cvc on (cvc.candidate_pk = ca.candidate_pk) " +
                "      join vote_category vc on (vc.vote_category_pk = cvc.vote_category_pk) " +
                "      join affiliation a2 on (a2.affiliation_pk = cvc.affiliation_pk) " +
                "      join party p2 on (p2.party_pk = a2.party_pk) " +
                "      join settlement s on (s.settlement_pk = cvc.settlement_pk) " +
                "      join mv_election mve on (mve.contest_pk = s.contest_pk) " +
                "    where text2ltree(mve.election_path) <@ text2ltree(:electionPath) " +
                "      and vc.vote_category_id in ('writein', 'personal') " +
                ") " +
                "select p.party_id, ca.display_order, cr.rank_number, ca.name_line, " +
                "coalesce((select m.votes from modified m where m.candidate_pk = ca.candidate_pk and m.vote_category_id = 'personal'),0) as personal");

        for (String party : parties) {
            query.append(", coalesce((select m.votes from modified m where m.candidate_pk = ca.candidate_pk and m.party_id = '")
                    .append(party)
                    .append("' and m.vote_category_id = 'writein'),0) as ")
                    .append(party);
        }
        query.append(" from candidate ca " +
                "  join candidate_rank cr using (candidate_pk) " +
                "  join affiliation a on (a.affiliation_pk = ca.affiliation_pk) " +
                "  join party p on (p.party_pk = a.party_pk) " +
                "  join ballot b on (b.ballot_pk = ca.ballot_pk) " +
                "  join mv_election mve on (mve.contest_pk = b.contest_pk) " +
                "where text2ltree(mve.election_path) <@ text2ltree(:electionPath) " +
                "order by p.party_id, cr.rank_number");
        return query.toString();
    }

    private List<List<String>> getQueryResults(int columnNumber, List res) {
        List<List<String>> rows = new ArrayList<>();
        for (Object o : res) {
            List<String> row = new ArrayList<>();
            for (int columnIndex = 0; columnIndex < columnNumber; columnIndex++) {
                Object object = ((Object[]) o)[columnIndex];
                row.add((object == null) ? "" : object.toString());
            }
            rows.add(row);
        }
        return rows;
    }
}
