package no.valg.eva.admin.configuration.domain.service;

import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.counting.repository.CandidateVoteRepository;
import no.valg.eva.admin.test.MockUtilsTestCase;
import no.valg.eva.admin.util.ExcelUtil;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class ExportCandidateVotesDomainServiceTest extends MockUtilsTestCase {

    private ExportCandidateVotesDomainService exportCandidateVotesService;
    
    private static final AreaPath AREA_PATH = AreaPath.from("111111.11.11.1111");
    private static final ElectionPath ELECTION_PATH = ElectionPath.from("111111.11.11.111111");
    
    @BeforeMethod
    public void setUp() throws Exception {
        exportCandidateVotesService = initializeMocks(ExportCandidateVotesDomainService.class);
    }
    
    @Test
    public void exportCandidateVotes_withAreaPathAndElectionPath_returnsCorrectResults() throws IOException, InvalidFormatException {
        List<List<String>> expectedResults = expectedResults();
        when(getInjectMock(CandidateVoteRepository.class).findCandidateVotes(any(AreaPath.class), any(ElectionPath.class))).thenReturn(expectedResults);
        
        byte[] res = exportCandidateVotesService.exportCandidateVotes(AREA_PATH, ELECTION_PATH);
        
        ExcelUtil.RowData rd = ExcelUtil.getRowDataFromExcelFile(new ByteArrayInputStream(res));
        assertRow(expectedResults, rd.getHeader(), 0);
        List<List<Pair<String, String>>> rows = rd.getRows();
        assertThat(rows).hasSize(expectedResults.size() - 1);
        for (int i = 0; i < rows.size(); i++) {
            assertRow(expectedResults, rows.get(i), i+1);
        }
    }

    private void assertRow(List<List<String>> expectedResults, List<Pair<String, String>> cells, int index) {
        ArrayList<String> row = new ArrayList<>();
        cells.forEach(e -> row.add(e.getValue()));
        assertThat(row).isEqualTo(expectedResults.get(index));
    }

    private List<List<String>> expectedResults() {
        List<List<String>> expectedResults = new ArrayList<>();
        expectedResults.add(new ArrayList<>(Arrays.asList("Liste", "Kandidatnr", "Ny rangering", "Navn", "Personstemmer", "A", "H")));
        expectedResults.add(new ArrayList<>(Arrays.asList("A", "1", "1", "Fornavn Etternavn", "5", "0", "5")));
        expectedResults.add(new ArrayList<>(Arrays.asList("H", "1", "1", "Fornavn2 Etternavn2", "4", "5", "0")));
        return expectedResults;
    }
}
