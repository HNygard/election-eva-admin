package no.valg.eva.admin.configuration.domain.service;

import lombok.NoArgsConstructor;
import no.valg.eva.admin.configuration.domain.model.County;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.ScanningConfig;
import no.valg.eva.admin.configuration.repository.CountyRepository;
import no.valg.eva.admin.configuration.repository.MunicipalityRepository;
import no.valg.eva.admin.util.ExcelUtil;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor
public class ScanningConfigDomainService {

    private final List<String> scanningConfigHeader = new ArrayList<>();
    
    private MunicipalityRepository municipalityRepository;
    private CountyRepository countyRepository;
    
    @Inject
    public ScanningConfigDomainService(MunicipalityRepository municipalityRepository, CountyRepository countyRepository) {
        this.municipalityRepository = municipalityRepository;
        this.countyRepository = countyRepository;
        initializeHeader();
    }

    private void initializeHeader() {
        scanningConfigHeader.add("Fylkesnr");
        scanningConfigHeader.add("Fylkenavn");
        scanningConfigHeader.add("Kommunenr");
        scanningConfigHeader.add("Kommunenavn");
        scanningConfigHeader.add("Leverandør");
        scanningConfigHeader.add("Skannsamarbeid");
        scanningConfigHeader.add("Samarbeidsansvarlig");
        scanningConfigHeader.add("Skanningansvarlig");
        scanningConfigHeader.add("Telefonnummer");
        scanningConfigHeader.add("E-post");
    }
    
    public byte[] exportScanningConfigAsExcelFile(Long electionEventPk) {
        List<Municipality> municipalities = municipalityRepository.findByElectionEventWithScanningConfig(electionEventPk);
        List<County> counties = countyRepository.findByElectionEventWithScanningConfig(electionEventPk);
        List<List<String>> rows = new ArrayList<>();

        rows.addAll(getScanningConfigForMunicipalities(municipalities));
        rows.addAll(getScanningConfigForCounties(counties));
        rows = sortByCountyAndMunicipality(rows);
        rows.add(0, scanningConfigHeader);

        return ExcelUtil.createXlsxFromRowData(rows);
    }

    private List<List<String>> getScanningConfigForMunicipalities(List<Municipality> municipalities) {
        List<List<String>> rows = new ArrayList<>();
        for (Municipality municipality : municipalities) {
            rows.add(getScanningConfigRow(municipality.getScanningConfig()));
        }
        
        return rows;
    }

    private List<String> getScanningConfigRow(ScanningConfig sc) {
        List<String> row = new ArrayList<>();
        if (sc.getCounty() != null) {
            row.add(sc.getCounty().getId());
            row.add(sc.getCounty().getName());
            row.add("");
            row.add("");
        } else {
            row.add(sc.getMunicipality().getCounty().getId());
            row.add(sc.getMunicipality().getCounty().getName());
            row.add(sc.getMunicipality().getId());
            row.add(sc.getMunicipality().getName());
        }
        
        row.add(sc.getVendor());
        row.add(sc.getCollaborationParticpants());
        row.add(sc.getCollaborationResponsible());
        row.add(sc.getResponsibleFullName());
        row.add(sc.getResponsiblePhoneNumber());
        row.add(sc.getResponsibleEmail());
        
        return row;
    }

    private List<List<String>> getScanningConfigForCounties(List<County> counties) {
        List<List<String>> rows = new ArrayList<>();
        for (County county : counties) {
            rows.add(getScanningConfigRow(county.getScanningConfig()));
        }

        return rows;
    }

    private List<List<String>> sortByCountyAndMunicipality(List<List<String>> rows) {
        return rows.stream()
                .sorted(Comparator.comparing((List<String> o) -> o.get(0)).thenComparing(o -> o.get(2)))
                .collect(Collectors.toList());
    }
}
