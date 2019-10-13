package no.evote.service.configuration;

import no.evote.constants.AreaLevelEnum;
import no.evote.constants.EvoteConstants;
import no.evote.exception.EvoteException;
import no.evote.security.UserData;
import no.valg.eva.admin.util.CSVUtil;
import no.valg.eva.admin.util.DateUtil;
import no.valg.eva.admin.backend.common.repository.LocaleRepository;
import no.valg.eva.admin.backend.configuration.repository.ElectionEventRepository;
import no.valg.eva.admin.configuration.domain.model.Borough;
import no.valg.eva.admin.configuration.domain.model.Country;
import no.valg.eva.admin.configuration.domain.model.County;
import no.valg.eva.admin.configuration.domain.model.ElectionDay;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.OpeningHours;
import no.valg.eva.admin.configuration.domain.model.PollingDistrict;
import no.valg.eva.admin.configuration.domain.model.PollingPlace;
import no.valg.eva.admin.configuration.domain.service.PollingPlaceDomainService;
import no.valg.eva.admin.configuration.repository.BoroughRepository;
import no.valg.eva.admin.configuration.repository.CountryRepository;
import no.valg.eva.admin.configuration.repository.MunicipalityRepository;
import no.valg.eva.admin.configuration.repository.PollingDistrictRepository;
import no.valg.eva.admin.configuration.repository.PollingPlaceRepository;
import org.joda.time.LocalTime;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.inject.Inject;
import javax.persistence.PersistenceException;
import javax.validation.ConstraintViolationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * NOTE! This is legacy code and based on the old architecture and should not be extended and or build upon!
 */
@Default
@ApplicationScoped
public class AreaImportServiceBean {
    private static final Pattern OPENING_HOURS = Pattern.compile("\\s*(\\d{2}.\\d{2}.\\d{4})\\s+(\\d{2}).(\\d{2})\\s*-\\s*(\\d{2}).(\\d{2})\\s*");
    private static final char PERIOD = '.';
    private static final int NAME_IDX = 1;

    @Inject
    private CountryServiceBean countryService;
    @Inject
    private CountyServiceBean countyService;
    @Inject
    private BoroughServiceBean boroughService;
    @Inject
    private BoroughRepository boroughRepository;
    @Inject
    private LocaleRepository localeRepository;
    @Inject
    private ElectionEventRepository electionEventRepository;
    @Inject
    private PollingPlaceDomainService pollingPlaceDomainService;
    @Inject
    private CountryRepository countryRepository;
    @Inject
    private PollingDistrictRepository pollingDistrictRepository;
    @Inject
    private MunicipalityRepository municipalityRepository;
    @Inject
    private PollingPlaceRepository pollingPlaceRepository;

    public void importAreaHierarchy(final UserData userData, final byte[] data) throws IOException {
        ElectionEvent electionEvent = electionEventRepository.findByPk(userData.getElectionEventPk());

        // Check if there actually is data to import
        if (data == null || data.length == 0) {
            emptyFileError();
        }

        List<List<String>> rows = CSVUtil.getRowsFromFile(new ByteArrayInputStream(data), 0, ";", EvoteConstants.CHARACTER_SET);
        // Check *again* if there is any data to report
        if (rows.isEmpty()) {
            emptyFileError();
        }

        deleteExistingAreaHierarchy(userData, electionEvent);

        int line = 1;
        for (List<String> row : rows) {
            if (row.get(0).charAt(0) != '#') {
                try {
                    String areaPath = row.get(0);
                    AreaLevelEnum areaLevel = AreaLevelEnum.getLevel(findAreaLevel(areaPath));
                    switch (areaLevel) {
                        case COUNTRY:
                            importCountry(userData, areaPath, electionEvent, row);
                            break;
                        case COUNTY:
                            importCounty(userData, areaPath, electionEvent, row);
                            break;
                        case MUNICIPALITY:
                            importMunicipality(userData, areaPath, electionEvent, row);
                            break;
                        case BOROUGH:
                            importBorough(userData, areaPath, electionEvent, row);
                            break;
                        case POLLING_DISTRICT:
                            importPollingDistrict(userData, areaPath, electionEvent, row);
                            break;
                        case POLLING_PLACE:
                            importPollingPlace(userData, areaPath, electionEvent, row);
                            break;
                        default:
                            throw new EvoteException("@area.import.error_unknown_area_level", Integer.toString(areaLevel.getLevel()), areaPath);
                    }
                } catch (EvoteException | PersistenceException | ConstraintViolationException e) {
                    throw new EvoteException("@area.import.error_line", Integer.toString(line), e.getMessage());
                }
            }
            ++line;
        }
    }

    private void emptyFileError() {
        throw new EvoteException("@area.import.error_empty");
    }

    private void deleteExistingAreaHierarchy(final UserData userData, final ElectionEvent electionEvent) {
        List<Country> countries = countryRepository.getCountriesForElectionEvent(electionEvent.getPk());
        for (Country c : countries) {
            countryService.deleteByPk(userData, c.getPk());
        }
    }

    /**
     * Import a polling place. Example CSV entry:
     * <p>
     * <pre>
     * ID;Navn;Valgting/Forhånd;Addr1;Addr2;Addr3;PostalCode;PostalTown;Infotext;Åpningstider
     * XXXXXX.YY.ZZ.ÆÆ.ØØ.ÅÅ.NN;Blabla;V;____;____;____;0101;Oslo;Ingen tekst;dd.MM.yyyy HH:MM,dd.MM.yyyy HH:MM
     * </pre>
     */
    private void importPollingPlace(final UserData userData, final String areaPath, final ElectionEvent electionEvent, final List<String> row) {
        PollingPlace pollingPlace = new PollingPlace();
        pollingPlace.setId(getId(areaPath));
        pollingPlace.setName(row.get(NAME_IDX));

        if ("V".equals(row.get(NAME_IDX + 1))) {
            pollingPlace.setElectionDayVoting(true);
        } else {
            pollingPlace.setElectionDayVoting(false);
        }


        pollingPlace.setAddressLine1(row.get(NAME_IDX + 2));
        pollingPlace.setAddressLine2(row.get(NAME_IDX + 3));
        pollingPlace.setAddressLine3(row.get(NAME_IDX + 4));
        pollingPlace.setPostalCode(row.get(NAME_IDX + 5));
        pollingPlace.setPostTown(row.get(NAME_IDX + 6));
        pollingPlace.setInfoText(row.get(NAME_IDX + 7));


        pollingPlace.setPollingDistrict(find(PollingDistrict.class, getParentPath(areaPath), electionEvent));
        pollingPlace = pollingPlaceDomainService.create(userData, pollingPlace);

        if (pollingPlace.isElectionDayVoting()) {
            // Opening hours
            List<ElectionDay> electionDays = electionEventRepository.findElectionDaysByElectionEvent(electionEvent);
            List<String> formattedDates = new ArrayList<>(electionDays.size());
            for (ElectionDay day : electionDays) {
                formattedDates.add(DateUtil.getFormattedShortDate(day.getDate()));
            }


            String[] openingHours = row.get(NAME_IDX + 8).split(",");

            for (String openingHour : openingHours) {
                Matcher openingHoursMatcher = OPENING_HOURS.matcher(openingHour);
                if (!openingHoursMatcher.matches()) {
                    throw new EvoteException("@area.import.error_opening_hour_invalid", openingHour);
                }

                ElectionDay electionDay = null;
                for (int electionDayIdx = 0; electionDayIdx < electionDays.size(); electionDayIdx++) {
                    if (formattedDates.get(electionDayIdx).equals(openingHoursMatcher.group(1))) {
                        electionDay = electionDays.get(electionDayIdx);
                    }
                }
                if (electionDay == null) {
                    throw new EvoteException("@area.import.error_election_day_invalid", openingHour);
                }


                OpeningHours currentOpeningHours = new OpeningHours();
                currentOpeningHours.setElectionDay(electionDay);
                currentOpeningHours.setStartTime(new LocalTime()
                        .withHourOfDay(Integer.valueOf(openingHoursMatcher.group(2)))
                        .withMinuteOfHour(Integer.valueOf(openingHoursMatcher.group(3))));
                currentOpeningHours.setEndTime(new LocalTime()
                        .withHourOfDay(Integer.valueOf(openingHoursMatcher.group(4)))
                        .withMinuteOfHour(Integer.valueOf(openingHoursMatcher.group(5))));
                currentOpeningHours.setPollingPlace(pollingPlace);

                pollingPlace.addOpeningHour(currentOpeningHours);
                pollingPlaceRepository.update(userData, pollingPlace);
            }
        }
    }

    /**
     * Import a municipality. Example CSV entry:
     * <p>
     * <pre>
     * ID;Navn;Gjelder hele kommunen?;Tellekrets?;Andre kretser som inngår
     * XXXXXX.YY.ZZ.ÆÆ.ØØ.ÅÅ;Hele kommunen;1;1;NNNN,MMMM,ZZZZ
     * </pre>
     */
    private void importPollingDistrict(final UserData userData, final String areaPath, final ElectionEvent electionEvent, final List<String> row) {
        PollingDistrict pd = new PollingDistrict();
        pd.setId(getId(areaPath));
        pd.setName(row.get(NAME_IDX));
        if ("1".equals(row.get(NAME_IDX + 1))) {
            pd.setMunicipality(true);
        }

        Borough borough = find(Borough.class, getParentPath(areaPath), electionEvent);
        pd.setBorough(borough);

        pd = pollingDistrictRepository.create(userData, pd);

        // It's a tellekrets. Find all children and set the new pd as their parent
        if ("1".equals(row.get(NAME_IDX + 2))) {
            pd.setParentPollingDistrict(true);

            for (String child : row.get(NAME_IDX + 3).split("\\s*,\\s*")) {
                PollingDistrict childPd = pollingDistrictRepository.findPollingDistrictById(borough.getPk(), child);
                childPd.setPollingDistrict(pd);
                pollingDistrictRepository.update(userData, childPd);
            }

        }
    }

    /**
     * Import a municipality. Example CSV entry:
     * <p>
     * <pre>
     * XXXXXX.YY.ZZ.ÆÆ.ØØ;Hele kommunen;1
     * </pre>
     */
    private void importBorough(final UserData userData, final String areaPath, final ElectionEvent electionEvent, final List<String> row) {
        Borough b = new Borough();
        b.setId(getId(areaPath));
        b.setName(row.get(NAME_IDX));
        if ("1".equals(row.get(NAME_IDX + 1))) {
            b.setMunicipality1(true);
        }
        b.setMunicipality(find(Municipality.class, getParentPath(areaPath), electionEvent));
        boroughService.create(userData, b);
    }

    /**
     * Import a municipality. Example CSV entry:
     * <p>
     * <pre>
     * XXXXXX.YY.ZZ.ÆÆ;Halden;nb-NO;P
     * </pre>
     */
    private void importMunicipality(final UserData userData, final String areaPath, final ElectionEvent electionEvent, final List<String> row) {
        Municipality m = new Municipality();
        m.setId(getId(areaPath));
        m.setName(row.get(NAME_IDX));
        String localeId = row.get(NAME_IDX + 1);
        m.setLocale(localeRepository.findById(localeId));

        if (m.getLocale() == null) {
            throw new EvoteException("@area.import.error_no_such_locale", localeId);
        }

        String voting = row.get(NAME_IDX + 2);
        if (voting.indexOf('E') != -1) {
            m.setElectronicMarkoffs(true);
        }

        m.setCounty(find(County.class, getParentPath(areaPath), electionEvent));
        municipalityRepository.create(userData, m);
    }

    /**
     * Import a county. Example CSV entry:
     * <p>
     * <pre>
     * XXXXXX.YY.ZZ;Sørfold
     * </pre>
     */
    private void importCounty(final UserData userData, final String areaPath, final ElectionEvent electionEvent, final List<String> row) {
        County c = new County();
        c.setId(getId(areaPath));
        c.setName(row.get(NAME_IDX));
        String localeId = row.get(NAME_IDX + 1);
        c.setLocale(localeRepository.findById(localeId));

        if (c.getLocale() == null) {
            throw new EvoteException("@area.import.error_no_such_locale", localeId);
        }
        c.setCountry(find(Country.class, getParentPath(areaPath), electionEvent));
        countyService.create(userData, c);
    }

    /**
     * Import a country. Example CSV entry:
     * <p>
     * <pre>
     * XXXXXX.YY;Langtvekkistan
     * </pre>
     */
    private void importCountry(final UserData userData, final String areaPath, final ElectionEvent electionEvent, final List<String> row) {
        Country c = new Country();
        c.setElectionEvent(electionEvent);
        c.setId(getId(areaPath));
        c.setName(row.get(NAME_IDX));
        countryService.create(userData, c);
    }

    private <T> T find(final Class<T> entityClass, final String areaPath, final ElectionEvent electionEvent) {
        Object entity = null;
        AreaLevelEnum areaLevel = AreaLevelEnum.getLevel(findAreaLevel(areaPath));
        String id = getId(areaPath);
        switch (areaLevel) {
            case COUNTRY:
                entity = countryService.findCountryById(electionEvent.getPk(), id);
                break;
            case COUNTY:
                entity = countyService.findCountyById(find(Country.class, getParentPath(areaPath), electionEvent).getPk(), id);
                break;
            case MUNICIPALITY:
                entity = municipalityRepository.findMunicipalityById(find(County.class, getParentPath(areaPath), electionEvent).getPk(), id);
                break;
            case BOROUGH:
                entity = boroughRepository.findBoroughById(find(Municipality.class, getParentPath(areaPath), electionEvent).getPk(), id);
                break;
            case POLLING_DISTRICT:
                entity = pollingDistrictRepository
                        .findPollingDistrictById(find(Borough.class, getParentPath(areaPath), electionEvent).getPk(), id);
                break;
            default:
                raiseError("Unable to find entity for level " + areaLevel.getLevel() + " " + areaPath);
        }

        return entityClass.cast(entity);
    }

    private String getId(final String areaPath) {
        return areaPath.substring(areaPath.lastIndexOf('.') + 1);
    }

    /**
     * Get the ID of the second to last element in the area path.
     */
    private String getParentPath(final String areaPath) {
        int lastIndex = areaPath.lastIndexOf(PERIOD);
        return areaPath.substring(0, areaPath.lastIndexOf(PERIOD, lastIndex));
    }

    private int findAreaLevel(final String areaPath) {
        // Number of periods in area path is the same as the area level
        return frequency(areaPath.toCharArray(), PERIOD);
    }

    private int frequency(final char[] charArray, final char subject) {
        int frequency = 0;
        for (char c : charArray) {
            if (c == subject) {
                frequency++;
            }
        }
        return frequency;
    }

    private void raiseError(final String msg) {
        throw new EvoteException(msg);
    }
}
