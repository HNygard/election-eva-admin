package no.evote.service;

import no.evote.constants.AreaLevelEnum;
import no.evote.constants.EvoteConstants;
import no.evote.constants.SQLConstants;
import no.evote.dto.BatchInfoDto;
import no.evote.exception.EvoteException;
import no.evote.model.Batch;
import no.evote.model.BatchStatus;
import no.evote.model.BinaryData;
import no.evote.security.UserData;
import no.evote.service.configuration.CountryServiceBean;
import no.valg.eva.admin.util.IOUtil;
import no.valg.eva.admin.util.XMLUtil;
import no.valg.eva.admin.backend.bakgrunnsjobb.domain.service.BakgrunnsjobbDomainService;
import no.valg.eva.admin.backend.common.repository.BatchRepository;
import no.valg.eva.admin.backend.common.repository.BinaryDataRepository;
import no.valg.eva.admin.backend.common.repository.LocaleTextRepository;
import no.valg.eva.admin.backend.configuration.repository.ElectionEventRepository;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.ContestArea;
import no.valg.eva.admin.configuration.domain.model.Country;
import no.valg.eva.admin.configuration.domain.model.County;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.ReportingUnit;
import no.valg.eva.admin.configuration.domain.model.VoteCountCategory;
import no.valg.eva.admin.configuration.repository.ContestRepository;
import no.valg.eva.admin.configuration.repository.CountryRepository;
import no.valg.eva.admin.configuration.repository.MunicipalityRepository;
import no.valg.eva.admin.configuration.repository.MvAreaRepository;
import no.valg.eva.admin.configuration.repository.ReportingUnitRepository;
import no.valg.eva.admin.configuration.repository.VoteCountCategoryRepository;
import no.valg.eva.admin.felles.bakgrunnsjobb.domain.model.Jobbkategori;
import no.valg.eva.admin.rbac.domain.model.OperatorRole;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.jdbc.Work;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.inject.Inject;
import javax.validation.ConstraintViolationException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static no.evote.constants.EvoteConstants.BATCH_STATUS_FAILED_ID;
import static no.evote.constants.EvoteConstants.BATCH_STATUS_STARTED_ID;
import static no.valg.eva.admin.common.counting.constants.ReportingUnitTypeId.VALGSTYRET;
import static no.valg.eva.admin.felles.bakgrunnsjobb.domain.model.Jobbkategori.CONFIGURATION_DOWNLOAD;
import static no.valg.eva.admin.felles.bakgrunnsjobb.domain.model.Jobbkategori.ELECTORAL_ROLL_DOWNLOAD;

/**
 * Exports EML and electoral roll as zip files.
 */
@Default
@ApplicationScoped
public class ExportServiceBean {
    private static final String ISO_DATE_FORMAT_W_TZ = "yyyy-MM-dd HH:mm:ssZ";
    private static final String BATCH_TABLE_FK = "batch_binary_data_pk";
    private static final String BATCH_TABLE = "batch";
    private static final String ZIP_MIME_TYPE = "application/zip";
    private static final String LOCALE_ID = "locale_id";
    private static final String ELECTION_EVENT_TEXT_NAME = "election_event_text_name";
    private static final String CONTEST_PK = "contest_pk";
    private static final String ELECTION_PK = "election_pk";
    private static final String GET_LOCALE_ID = " select locale_id from locale" + " where locale_pk = ?";
    private static final String OPENING_HOURS = " select to_char(election_day_date,'YYYY-MM-DD') as election_day_date,"
            + " to_char(start_time,'hh24:MI:SS') as start_time, to_char(end_time,'hh24:MI:SS') as end_time"
            + " from opening_hours oh join election_day ed  on (ed.election_day_pk = oh.election_day_pk)"
            + " where polling_place_pk = ? order by election_day_date, start_time";
    private static final String POLLING_STATION = " select polling_station_id, polling_station_first, polling_station_last" + " from polling_station"
            + " where polling_place_pk = ? order by polling_station_id";
    private static final String POLLING_PLACES = " select a.area_path, p.polling_place_id, p.polling_place_name, address_line1, address_line2, address_line3,"
            + " postal_code, post_town, p.polling_place_pk" + " from contest_area ca" + " join mv_area ac" + "  on (ac.mv_area_pk = ca.mv_area_pk)"
            + " join mv_area a" + "  on (text2ltree(a.area_path) <@ text2ltree(ac.area_path))" + " join polling_place p"
            + "  on (p.polling_place_pk = a.polling_place_pk)" + " where contest_pk = ?  and a.area_level = 6" + " order by a.area_path";
    private static final String AREA_NODES = " select area_path, parent_area, child_area" + " from contest_area ca" + " join mv_area a"
            + "  on (a.mv_area_pk = ca.mv_area_pk)" + " where contest_pk = ?" + " order by area_path";
    private static final String RESULTS_REPORTED = " select vote_count_category_id, vote_count_category_name, central_preliminary_count, polling_district_count, "
            + " ap.area_path, p.polling_district_name"
            + " from contest_rel_area ca"
            + " join election e"
            + "   on (e.election_pk = ca.election_pk)"
            + " join mv_area ap"
            + "  on (text2ltree(ap.area_path) <@ text2ltree(ca.area_path)"
            + "  and ap.area_level = 5"
            // Check 'samlekrets'.
            + "  and ap.parent_polling_district_pk is null)"
            + " join mv_area ac"
            + "   on (text2ltree(ac.area_path) @> text2ltree(ap.area_path)"
            + "   and ac.area_level = ca.contest_area_level)"
            + " join contest_area ca2"
            + "   on (ca2.contest_pk = ca.contest_pk"
            + "   and ca2.mv_area_pk = ac.mv_area_pk)"
            + " join polling_district p"
            + "  on (p.polling_district_pk = ap.polling_district_pk)"
            + " left join polling_place pp"
            + "  on (pp.polling_district_pk = p.polling_district_pk"
            + "  and pp.election_day_voting)"

            + " cross join vote_count_category vcc"
            + " join report_count_category rcc "
            + "  on rcc.election_group_pk = ca.election_group_pk "
            + "  and rcc.municipality_pk = ap.municipality_pk "
            + "  and rcc.vote_count_category_pk = vcc.vote_count_category_pk "
            + "  and ((not rcc.polling_district_count and not rcc.technical_polling_district_count and p.municipality) "
            + "    or (rcc.polling_district_count and p.parent_polling_district_pk IS NULL "
            + "		and (not p.municipality) and not p.technical_polling_district) "
            + "    or (rcc.technical_polling_district_count and p.technical_polling_district)) "

            + " where ca.contest_pk = ?"
            + " and ca.mv_area_pk = ? and (ca.area_level < 5 or not central_preliminary_count)"
            + " and not (vcc.vote_count_category_id = 'VO'"
            + "          and rcc.polling_district_count"
            + "          and not p.parent_polling_district"
            + "          and pp.polling_district_pk is null)"
            + " order by vote_count_category_id, ap.area_path";
    private static final String REP_UNITS_BY_CONTEST = " select e.election_path, ca.area_path, rut.reporting_unit_type_name, "
            + "name_line, address_line1, address_line2, address_line3," + " postal_code, post_town, email, telephone_number, reporting_unit_pk, ru.mv_area_pk"
            + " from contest_rel_area ca" + " join mv_election e" + "  on (text2ltree(e.election_path) @> text2ltree(ca.election_path))"
            + " join reporting_unit ru" + "  on (ru.mv_election_pk = e.mv_election_pk" + "  and ru.mv_area_pk = ca.mv_area_pk)"
            + " join reporting_unit_type rut" + "  on (rut.reporting_unit_type_pk = ru.reporting_unit_type_pk)" + " where ca.contest_pk = ?"
            + " order by ca.area_level, e.election_path, ca.area_path";
    private static final String CONTEST_TEXT = " select contest_text_name, contest_text" + " from contest_text" + " where contest_pk = ?"
            + " order by contest_text_name";
    private static final String CONTESTS_DATA = "select contest_id, contest_name, max_votes, min_votes, max_write_in, max_renumber, number_of_positions,"
            + " coalesce(c.penultimate_recount, e.penultimate_recount) as penultimate_recount, contest_pk"
            + " from election e join contest c on (c.election_pk = e.election_pk)" + " where e.election_pk = ?" + " order by contest_id";
    private static final String ELECTION_EVENT_LOCALES = "select l.locale_id" + " from election_event_locale eel" + " join locale l"
            + "   on (l.locale_pk = eel.locale_pk)" + " where eel.election_event_pk = ?" + " order by l.locale_id";
    private static final String ELECTION_DAYS = " select to_char(election_day_date,'YYYY-MM-DD') as election_day_date" + " from election_day"
            + " where election_event_pk = ?" + " order by election_day_date";
    private static final String ELECTION_TEXTS = " select election_text_name, election_text" + " from election_text" + " where election_pk = ?"
            + " order by election_text_name";
    private static final String ELECTION_NODES_DATA = "select eg.*, election_type_id, election_type_name, election_id, election_name, area_level,"
            + " personal, renumber, writein, strikeout, baseline_vote_factor, election_pk, ee.electoral_roll_cut_off_date,"
            + " ee.voting_card_electoral_roll_date, ee.voting_card_deadline" + " from election_group eg" + " join election e"
            + "  on (e.election_group_pk = eg.election_group_pk) join election_event ee on ee.election_event_pk = eg.election_event_pk"
            + " join election_type et" + "  on (et.election_type_pk = e.election_type_pk)" + " where eg.election_event_pk = ? order by election_id";
    private static final String ELECTION_EVENT_TEXTS = " select election_event_text_name, election_event_text" + " from election_event_text"
            + " where election_event_pk = ?" + " order by election_event_text_name";
    private static final String MESSAGES_ALL = " select t.text_id_pk, coalesce(t.election_event_pk, 0) as election_event_pk,"
            + " text_id, locale_id, locale_text" + " from text_id t" + " join locale_text lt" + "  on (lt.text_id_pk = t.text_id_pk)" + " join locale l"
            + "  on (l.locale_pk = lt.locale_pk)" + " where coalesce(t.election_event_pk, 0) in (0, ?)";
    private static final String PROPOSERS_BY_AFF = " select proposer_id, proposer_role_id, proposer_role_name, name_line, "
            + "first_name, middle_name, last_name, date_of_birth,"
            + " address_line1, address_line2, address_line3, postal_code, post_town, email, telephone_number" + " from proposer p"
            + " left join proposer_role pr" + "   on (pr.proposer_role_pk = p.proposer_role_pk)" + " where p.ballot_pk = ?" + " and p.approved"
            + " order by last_name, first_name, middle_name";
    private static final String CANDIDATES_BY_AFF = "select candidate_id, display_order, name_line, first_name, middle_name, last_name, date_of_birth, "
            + " baseline_votes, address_line1, address_line2, address_line3, postal_code, post_town, email, telephone_number, residence, profession"
            + " from candidate c" + " where c.affiliation_pk = ? " + " order by c.display_order";
    // @formatter:off
    private static final String AFFILIATIONS_BY_CONTEST =
            "SELECT "
                    + "  party_id, "
                    + "  party_name, "
                    + "  p.short_code, "
                    + "  party_category_id, "
                    + "  b.ballot_pk, "
                    + "  a.show_candidate_residence, "
                    + "  a.show_candidate_profession, "
                    + "  affiliation_pk "
                    + "FROM ballot b "
                    + "JOIN affiliation a "
                    + "  ON (a.ballot_pk = b.ballot_pk) "
                    + "JOIN party p "
                    + "  ON (p.party_pk = a.party_pk) "
                    + "JOIN party_category pc "
                    + "  ON (pc.party_category_pk = p.party_category_pk) "
                    + "WHERE b.contest_pk = ? "
                    + "  AND b.approved "
                    + "  AND a.approved "
                    + "  AND p.approved "
                    + "ORDER BY b.display_order ";
    private static final String ELECTION_NODES = "select election_group_id, election_group_name, election_id, election_name, election_type_id,"
            + " election_pk from election_group eg join election e on (e.election_group_pk = eg.election_group_pk)" + " join election_type et"
            + "   on (et.election_type_pk = e.election_type_pk)" + " where election_event_pk = ? order by election_id";
    // @formatter:off
    private static final String MESSAGES =
            "SELECT "
                    + "  text_id, "
                    + "  locale_text "
                    + "FROM text_id t "
                    + "JOIN locale_text lt "
                    + "  ON (lt.text_id_pk = t.text_id_pk) "
                    + "JOIN locale l "
                    + "  ON (l.locale_pk = lt.locale_pk) "
                    + "WHERE t.election_event_pk = ? "
                    + "  AND l.locale_pk = ? ";
    // Exports entire electoral roll - if a person has not voted, the row is marked pvoting, and approved is set to false
    // @formatter:off
    private static final String ELECTORAL_ROLL_DATA =
            "SELECT "
                    + "  e.election_group_id, "
                    + "  e.election_id, "
                    + "  e.contest_id, "
                    + "  v.voter_id, "
                    + "  'pvoting', "
                    + "  pvote.voter_pk is not null as approved, "
                    + "  substr(a.area_path, strpos(a.area_path, '.') + 1) "
                    + "FROM "
                    + "  voter v "
                    + "JOIN mv_area a "
                    + "  ON a.mv_area_pk = v.mv_area_pk "
                    + "JOIN mv_area ac "
                    + "  ON text2ltree(ac.area_path) @> text2ltree(a.area_path) "
                    + "  AND a.area_level = 5 "
                    + "JOIN contest_area ca "
                    + "  ON ca.mv_area_pk = ac.mv_area_pk "
                    + "JOIN mv_election e "
                    + "  ON e.contest_pk = ca.contest_pk "
                    + "  AND e.election_level = 3 "
                    + "  AND v.date_of_birth <= COALESCE(e.contest_end_date_of_birth, e.election_end_date_of_birth) "
                    + "LEFT JOIN ( "
                    + "  SELECT "
                    + "    cv.election_group_pk, "
                    + "    cv.voter_pk "
                    + "  FROM "
                    + "    voting cv "
                    + "  JOIN voting_category vc "
                    + "    ON (vc.voting_category_pk = cv.voting_category_pk) "
                    + "  WHERE cv.approved) AS pvote "
                    + "ON "
                    + "  (pvote.election_group_pk = e.election_group_pk "
                    + "   AND pvote.voter_pk = v.voter_pk) "
                    + "WHERE "
                    + "  v.election_event_pk = ? "
                    + "  AND a.municipality_pk = ? "
                    + "  AND v.approved = true "
                    + "ORDER BY "
                    + "  v.voter_id, e.election_id ";
    // @formatter:on
    private static final String CONTESTS_BY_ELECTION = "select contest_id, contest_name, contest_pk" + " from contest"
            + " where election_pk = ? order by contest_id";
    private static final String GET_ELECTION_EVENT = "select e.*, locale_id from election_event e join locale l   on (l.locale_pk = e.locale_pk) "
            + "where e.election_event_id = ?";
    private static final String LOCALE_TEXT = "locale_text";
    private static final String TEXT_ID = "text_id";
    // @formatter:on
    private static final String ELECTION_EVENT = "election_event";
    private final Logger logger = Logger.getLogger(ExportServiceBean.class);
    @Inject
    private CountryServiceBean countryService;
    @Inject
    private LocaleTextRepository localeTextRepository;
    @Inject
    private ElectionEventRepository electionEventRepository;
    @Inject
    private BinaryDataRepository binaryDataRepository;
    @Inject
    private BatchServiceBean batchService;
    @Inject
    private BatchRepository batchRepository;
    @Inject
    private BakgrunnsjobbDomainService bakgrunnsjobbDomainService;
    @Inject
    private CryptoServiceBean cryptoService;
    @Inject
    private CountryRepository countryRepository;
    @Inject
    private MunicipalityRepository municipalityRepository;
    @Inject
    private MvAreaRepository mvAreaRepository;
    @Inject
    private ContestRepository contestRepository;
    @Inject
    private VoteCountCategoryRepository voteCountCategoryRepository;
    @Inject
    private ReportingUnitRepository reportingUnitRepository;

    public ExportServiceBean() {

    }

    public void generateEML(UserData userData, Long electionEventPk) {
        ElectionEvent electionEvent = electionEventRepository.findByPk(electionEventPk);
        Batch batch = startBatch(userData, CONFIGURATION_DOWNLOAD, null);
        generateBatch(userData, electionEvent, batch, new BatchContent[]{
                (userData1, electionEventId, batch1, zout) -> {
                    Document doc = getCandidateList(userData1, electionEventId);
                    addFileAndSignatureToZip(userData1, xslTransform("CandidateList.xsl", doc), zout, "CandidateList.xml");
                },
                (userData1, electionEventId, batch1, zout) -> {
                    Document doc = getElectionEvent(userData1, electionEventId);
                    addFileAndSignatureToZip(userData1, xslTransform("ElectionEvent.xsl", doc), zout, "ElectionEvent.xml");
                },
                (userData1, electionEventId, batch1, zout) -> {
                    byte[] data = getMunicipalityData(electionEventId);
                    addFileAndSignatureToZip(userData1, data, zout, "Municipalities.csv");
                },
                (userData1, electionEventId, batch1, zout) -> {
                    Document doc = getVersionData(electionEventId, batch1);
                    addFileAndSignatureToZip(userData1, documentToBytes(doc), zout, "MetaData.xml");
                }
        });
    }

    private Document getVersionData(final String electionEventId, final Batch batch) {
        ElectionEvent electionEvent = electionEventRepository.findById(electionEventId);
        final no.valg.eva.admin.configuration.domain.model.Locale locale = electionEvent.getLocale();
        String pattern = "YYYY-MM-dd'T'HH:mm:ssZ";
        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern(pattern).withLocale(locale.toJavaLocale());
        Batch localBatch = batchRepository.findByPk(batch.getPk());

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        try {
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new EvoteException(e.getMessage(), e);
        }
        Document doc = builder.newDocument();
        Element rootEl = doc.createElement("metaData");
        doc.appendChild(rootEl);

        OperatorRole operatorRole = localBatch.getOperatorRole();

        String roleNameId = operatorRole.getRole().getName();
        String roleName = localeTextRepository.findByElectionEventLocaleAndTextId(electionEvent.getPk(), locale.getPk(), roleNameId).getLocaleText();

        insertNode(doc, rootEl, "electionEventId", electionEventId);
        insertNode(doc, rootEl, "accessPath", localBatch.getCategory().toAccessPath());
        insertNode(doc, rootEl, "version", Integer.toString(localBatch.getNumber()));

        Element operatorRoleEl = insertNode(doc, rootEl, "operatorRole");
        insertNode(doc, operatorRoleEl, "operator", localBatch.getAuditOperator());
        insertNode(doc, operatorRoleEl, "areaPath", operatorRole.getMvArea().getPath());
        insertNode(doc, operatorRoleEl, "electionPath", operatorRole.getMvElection().getPath());

        Element roleEl = insertNode(doc, operatorRoleEl, "role", roleName);
        roleEl.setAttribute("textid", roleNameId);
        Element timestampEl = insertNode(doc, rootEl, "timestamp", dateTimeFormatter.print(localBatch.getAuditTimestamp()));
        timestampEl.setAttribute("pattern", pattern);

        return doc;
    }

    private byte[] getMunicipalityData(String electionEventId) {
        StringBuilder csv = new StringBuilder();
        ElectionEvent electionEvent = electionEventRepository.findById(electionEventId);
        logger.debug("CountryService: " + countryService);
        logger.debug("ElectionEvent: " + electionEvent);
        List<Country> countries = countryRepository.getCountriesForElectionEvent(electionEvent.getPk());
        for (Country country : countries) {
            List<Municipality> municipalities = municipalityRepository.findMunicipalitiesByCountryPk(country.getPk());
            for (Municipality municipality : municipalities) {
                County county = municipality.getCounty();
                csv.append(String.format("%s,%s,%s,%s,%s,%s\n", country.getId(), country.getName(), county.getId(), county.getName(), municipality.getId(),
                        municipality.getName()));
            }
        }
        try {
            return csv.toString().getBytes(EvoteConstants.CHARACTER_SET);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Generates a zipfile, where the content is specified by the list of generators supplied as the last argument.
     */
    private void generateBatch(final UserData userData, final ElectionEvent electionEvent, final Batch batch, final BatchContent[] batchContents) {
        String electionEventId = electionEvent.getId();

        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        ZipOutputStream zout = new ZipOutputStream(byteStream);
        try {
            String filename = generateFilename(electionEventId);

            for (BatchContent batchContent : batchContents) {
                batchContent.generate(userData, electionEventId, batch, zout);
            }
            zout.close();
            byteStream.close();

            byte[] bytes = byteStream.toByteArray();

            if (bytes.length > 0) {
                // Create new binary data element in database, attach it to the batch, mark it as completed
                addBinaryDataAndComplete(userData, electionEvent, filename, batch, bytes);
            } else {
                markAsFailed(userData, batch);
            }
        } catch (RuntimeException e) {
            // On error, try to mark batch as failed and rethrow
            logger.fatal("Failed to generate EML, trying to mark batch as failed", e);
            if (batch != null) {
                markAsFailed(userData, batch);
            }
            throw e;
        } catch (IOException e) {
            // On error, try to mark batch as failed
            logger.fatal("Failed to generate EML, trying to mark batch as failed", e);
            if (batch != null) {
                markAsFailed(userData, batch);
            }
            throw new EvoteException(e.getMessage(), e);
        } finally {
            try {
                zout.close();
            } catch (IOException e) {
                logger.warn(e.getMessage(), e);
            }
        }
    }

    private void markAsFailed(final UserData userData, final Batch emlBatch) {
        bakgrunnsjobbDomainService.oppdaterBakgrunnsjobb(userData, emlBatch, BATCH_STATUS_FAILED_ID);
    }

    private void addBinaryDataAndComplete(final UserData userData, final ElectionEvent electionEvent, final String filename, final Batch emlBatch,
                                          final byte[] bytes) {
        BinaryData binaryData = new BinaryData();
        binaryData.setBinaryData(bytes);
        binaryData.setElectionEvent(electionEvent);
        binaryData.setFileName(filename);
        binaryData.setMimeType(ZIP_MIME_TYPE);

        // Table info
        binaryData.setTableName(BATCH_TABLE);
        binaryData.setColumnName(BATCH_TABLE_FK);

        try {
            binaryDataRepository.createBinaryData(userData, binaryData);
        } catch (ConstraintViolationException e) {
            throw new EvoteException("Unable to store generated EML. Most likely due to storage size limitations.", e);
        }

        emlBatch.setBinaryData(binaryData);
        emlBatch.setBatchStatus(getBatchStatus(EvoteConstants.BATCH_STATUS_COMPLETED_ID));

        batchRepository.update(userData, emlBatch);
    }

    private Batch startBatch(UserData userData, Jobbkategori category, ExportType exportType) {
        return bakgrunnsjobbDomainService.lagBakgrunnsjobb(userData, category, BATCH_STATUS_STARTED_ID, exportType != null ? exportType.getId() : null, null);
    }

    private BatchStatus getBatchStatus(final Integer id) {
        return batchRepository.findBatchStatusById(id);
    }

    private String generateFilename(final String electionEventId) {
        return File.separator + "EML_" + electionEventId + ".zip";
    }

    private void addFileAndSignatureToZip(final UserData userData, final byte[] data, final ZipOutputStream zout, final String name) {
        zip(data, zout, name);
        addSignatureToZip(userData, data, zout, name.subSequence(0, name.indexOf('.')) + ".pem");
    }

    private void addSignatureToZip(final UserData userData, final byte[] bytesToSign, final ZipOutputStream zout, final String filename) {
        byte[] signature;
        signature = cryptoService.signDataWithCurrentElectionEventCertificate(userData, bytesToSign);
        if (signature != null) {
            zip(signature, zout, filename);
        }
    }

    private boolean zip(final byte[] data, final ZipOutputStream zout, final String filename) {
        try {
            zout.putNextEntry(new ZipEntry(filename));
            zout.write(data);
            zout.closeEntry();
            return true;
        } catch (Exception e) {
            throw new EvoteException(e.getMessage(), e);
        }
    }

    private byte[] xslTransform(final String xslFilename, final Document doc) {
        InputStream is;
        is = this.getClass().getClassLoader().getResourceAsStream("xsl" + File.separator + xslFilename);

        if (is == null) {
            throw new EvoteException("Unable to find file: " + xslFilename);
        }

        Source xslSource = new StreamSource(is);
        TransformerFactory transFact = TransformerFactory.newInstance();
        Transformer transformer;
        try {
            Templates templates = transFact.newTemplates(xslSource);
            transformer = templates.newTransformer();
        } catch (TransformerConfigurationException e) {
            throw new EvoteException("Error creating xslTransformer for stylesheet: " + xslFilename, e);
        }

        ByteArrayOutputStream emlOutputStream = new ByteArrayOutputStream();
        try {
            transformer.transform(new DOMSource(doc), new StreamResult(emlOutputStream));
        } catch (TransformerException e) {
            throw new EvoteException("Error transforming document. Stylesheet: " + xslFilename, e);
        }

        return emlOutputStream.toByteArray();
    }

    private byte[] documentToBytes(final Document doc) {
        byte[] bytes;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DOMSource domSource = new DOMSource(doc);

            StreamResult result = new StreamResult(baos);
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer;

            transformer = tf.newTransformer();

            transformer.transform(domSource, result);
            bytes = baos.toByteArray();
        } catch (TransformerException e) {
            throw new EvoteException("Error transforming document.", e);
        }

        return bytes;
    }

    private Document getCandidateList(UserData userData, String electionEventId) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        try {
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new EvoteException(e.getMessage(), e);
        }
        Document doc = builder.newDocument();

        // Insert the root element node
        Element electionEventElement = doc.createElement(ELECTION_EVENT);
        doc.appendChild(electionEventElement);

        // Insert a comment in front of the element node
        doc.insertBefore(doc.createComment("EML CandidateList"), doc.getFirstChild());

        List<LinkedHashMap<String, Object>> textIds = searchSql(MESSAGES, new Object[]{userData.getElectionEventPk(), userData.getLocale().getPk()});
        Map<String, String> text = new HashMap<>();
        for (LinkedHashMap<String, Object> textIdRow : textIds) {
            text.put(String.valueOf(textIdRow.get(TEXT_ID)), String.valueOf(textIdRow.get(LOCALE_TEXT)));
        }

        // Insert the election_event nodes
        Map<String, Object> electionEventRow = insertElectionEvents(electionEventId, doc, electionEventElement);

        // Insert the election nodes
        List<LinkedHashMap<String, Object>> elections = searchSql(ELECTION_NODES, new Object[]{electionEventRow.get(SQLConstants.ELECTION_EVENT_PK)});
        for (LinkedHashMap<String, Object> electionRow : elections) {
            Element electionElement = insertNode(doc, electionEventElement, "election");
            for (Entry<String, Object> column : electionRow.entrySet()) {
                insertNode(doc, electionElement, column.getKey(), getString(column.getValue()));
            }

            insertContestNodes(doc, text, electionRow, electionElement);
        }

        // Insert the area nodes
        List<MvArea> countingMvAreas = sortedCountingMvAreas(electionEventId);
        for (MvArea countingMvArea : countingMvAreas) {
            if (countingMvArea.isBoroughLevel() && countingMvArea.getBorough().isMunicipality1()) {
                continue;
            }
            Element areaElement = insertNode(doc, electionEventElement, "area");
            insertNode(doc, areaElement, "area_path", countingMvArea.getAreaPath());
            insertNode(doc, areaElement, "area_name", countingMvArea.getAreaName());
        }

        return doc;
    }

    private List<MvArea> sortedCountingMvAreas(String electionEventId) {
        AreaPath electionEventPath = AreaPath.from(electionEventId);
        List<MvArea> countingMvAreas = mvAreaRepository.findByPathAndLevel(electionEventPath, AreaLevelEnum.POLLING_DISTRICT);
        AreaPath osloPath = AreaPath.from(electionEventId + ".47.03.0301");
        List<MvArea> boroughMvAreas = mvAreaRepository.findByPathAndLevel(osloPath, AreaLevelEnum.BOROUGH);
        countingMvAreas.addAll(boroughMvAreas);
        Collections.sort(countingMvAreas, new Comparator<MvArea>() {
            @Override
            public int compare(MvArea mvArea1, MvArea mvArea2) {
                return mvArea1.getAreaPath().compareTo(mvArea2.getAreaPath());
            }
        });
        return countingMvAreas;
    }

    private void insertContestNodes(final Document doc, final Map<String, String> text, final Map<String, Object> electionRow, final Element electionElement) {
        List<LinkedHashMap<String, Object>> contests = searchSql(CONTESTS_BY_ELECTION, electionRow.get(ELECTION_PK));
        for (LinkedHashMap<String, Object> contestRow : contests) {
            Element contestElement = insertNode(doc, electionElement, "contest");
            for (Entry<String, Object> column : contestRow.entrySet()) {
                insertNode(doc, contestElement, column.getKey(), getString(column.getValue()));
            }

            // Insert the affiliation nodes
            List<LinkedHashMap<String, Object>> affiliations = searchSql(AFFILIATIONS_BY_CONTEST, new Object[]{contestRow.get(CONTEST_PK)});
            for (LinkedHashMap<String, Object> affiliationRow : affiliations) {
                Element affiliationElement = insertNode(doc, contestElement, "affiliation");
                for (Entry<String, Object> column : affiliationRow.entrySet()) {
                    insertNode(doc, affiliationElement, column.getKey(), getString(column.getValue()));
                }
                String partyName = getString(affiliationRow.get("party_name"));
                insertNode(doc, affiliationElement, "affiliation_name", text.get(partyName));

                // Insert the candidate nodes
                List<LinkedHashMap<String, Object>> candidates = searchSql(CANDIDATES_BY_AFF, new Object[]{affiliationRow.get("affiliation_pk")});
                for (LinkedHashMap<String, Object> candidateRow : candidates) {
                    Element candidateElement = insertNode(doc, affiliationElement, "candidate");
                    for (Entry<String, Object> column : candidateRow.entrySet()) {
                        if (column.getKey().equalsIgnoreCase("email") && column.getValue() != null
                                && !Pattern.matches("^[^@]+@[^@]+$", column.getValue().toString())) {
                            // if not valid email -> goto next
                            continue;
                        }

                        insertNode(doc, candidateElement, column.getKey(), getString(column.getValue()));
                    }
                }

                // Insert the proposer nodes
                insertProposerNodes(doc, affiliationRow, affiliationElement);
            }

        }
    }

    private void insertProposerNodes(final Document doc, final Map<String, Object> affiliationRow, final Element affiliationElement) {
        List<LinkedHashMap<String, Object>> proposers = searchSql(PROPOSERS_BY_AFF, new Object[]{affiliationRow.get("ballot_pk")});
        for (LinkedHashMap<String, Object> proposerRow : proposers) {
            Element proposerElement = insertNode(doc, affiliationElement, "proposer");
            for (Entry<String, Object> column : proposerRow.entrySet()) {
                if (column.getKey().equalsIgnoreCase("email") && column.getValue() != null && !Pattern.matches("^[^@]+@[^@]+$", column.getValue().toString())) {
                    // if not valid email -> goto next
                    continue;
                }

                insertNode(doc, proposerElement, column.getKey(), getString(column.getValue()));
            }
        }
    }

    private Map<String, Object> insertElectionEvents(final String electionEventId, final Document doc, final Element electionEventElement) {
        List<LinkedHashMap<String, Object>> electionEvents = searchSql(GET_ELECTION_EVENT, electionEventId);
        if (electionEvents.isEmpty()) { // ElectionEventId not found, just return
            throw new EvoteException("No election event found for id: " + electionEventId);
        }
        LinkedHashMap<String, Object> electionEventRow = electionEvents.get(0);
        for (Entry<String, Object> column : electionEventRow.entrySet()) {
            insertNode(doc, electionEventElement, column.getKey(), getString(column.getValue()));
        }
        return electionEventRow;
    }

    private Document getElectionEvent(final UserData userData, final String electionEventId) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        try {
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new EvoteException(e.getMessage(), e);
        }
        Document doc = builder.newDocument();

        // Insert the root element node
        Element electionEventElement = doc.createElement(ELECTION_EVENT);
        doc.appendChild(electionEventElement);

        // Insert a comment in front of the element node
        doc.insertBefore(doc.createComment("EML ElectionEvent"), doc.getFirstChild());

        String localeId = getLocaleId(userData.getLocale().getPk());

        // insert the election_event nodes
        Map<String, Object> electionEventRow = insertElectionEvents(electionEventId, doc, electionEventElement);
        Integer electionEventPk = (Integer) electionEventRow.get(SQLConstants.ELECTION_EVENT_PK);

        insertElectionEventLocales(doc, electionEventElement, electionEventPk);

        // insert the global texts and all texts in the election event
        Map<String, HashMap<String, HashMap<String, String>>> text = insertGlobalTexts(doc, electionEventElement, electionEventPk);

        // insert text name ids and corresponding text ids for the election event.
        insertElectionEventTexts(doc, electionEventElement, electionEventPk, text);

        // insert the election nodes
        List<LinkedHashMap<String, Object>> elections = searchSql(ELECTION_NODES_DATA, electionEventPk);
        for (LinkedHashMap<String, Object> electionRow : elections) {
            Element electionElement = insertNode(doc, electionEventElement, "election");
            for (Entry<String, Object> column : electionRow.entrySet()) {
                insertNode(doc, electionElement, column.getKey(), getString(column.getValue()));
            }

            // insert the text name ids and corresponding text ids for the election.
            List<LinkedHashMap<String, Object>> electionTexts = searchSql(ELECTION_TEXTS, electionRow.get(ELECTION_PK));
            for (LinkedHashMap<String, Object> electionTextRow : electionTexts) {
                Element descriptionElement = insertNode(doc, electionElement, "description");
                Element textIdElement = insertNode(doc, descriptionElement, TEXT_ID);
                for (Entry<String, Object> column : electionTextRow.entrySet()) {
                    insertNode(doc, textIdElement, column.getKey(), getString(column.getValue()));
                }

                Map<String, String> lHash = text.get(electionEventPk.toString()).get(getString(electionTextRow.get("election_text_name")));
                ArrayList<String> sortedKeys = new ArrayList<>(lHash.keySet());
                Collections.sort(sortedKeys);
                for (String key : sortedKeys) {
                    Element textNameElement = insertNode(doc, descriptionElement, "text_name");
                    insertNode(doc, textNameElement, TEXT_ID, getString(electionTextRow.get("election_text_name")));
                    insertNode(doc, textNameElement, LOCALE_ID, key);
                    insertNode(doc, textNameElement, LOCALE_TEXT, lHash.get(key));

                }
                lHash = text.get(electionEventPk.toString()).get(getString(electionTextRow.get("election_text")));
                sortedKeys = new ArrayList<>(lHash.keySet());
                Collections.sort(sortedKeys);
                for (String key : sortedKeys) {
                    Element textPlainElement = insertNode(doc, descriptionElement, "text_plain");
                    insertNode(doc, textPlainElement, TEXT_ID, getString(electionTextRow.get("election_text_name")));
                    insertNode(doc, textPlainElement, LOCALE_ID, key);
                    insertNode(doc, textPlainElement, LOCALE_TEXT, lHash.get(key));

                }
            }

            insertElectionDays(doc, electionEventPk, electionElement);

            // insert the contest nodes
            insertContestNodes(doc, localeId, electionEventPk, text, electionRow, electionElement);
        }

        return doc;
    }

    private void insertElectionDays(final Document doc, final Integer electionEventPk, final Element electionElement) {
        // election day
        List<LinkedHashMap<String, Object>> electionDays = searchSql(ELECTION_DAYS, electionEventPk);
        for (LinkedHashMap<String, Object> electionDayRow : electionDays) {
            Element electionDayElement = insertNode(doc, electionElement, "election_day");
            for (Entry<String, Object> column : electionDayRow.entrySet()) {
                insertNode(doc, electionDayElement, column.getKey(), getString(column.getValue()));
            }
        }
    }

    private void insertElectionEventLocales(final Document doc, final Element electionEventElement, final Integer electionEventPk) {
        // election event locales
        List<LinkedHashMap<String, Object>> electionEventLocales = searchSql(ELECTION_EVENT_LOCALES, electionEventPk);
        for (LinkedHashMap<String, Object> electionEventLocaleRow : electionEventLocales) {
            Element electionEventLocaleElement = insertNode(doc, electionEventElement, "election_event_locale");
            for (Entry<String, Object> column : electionEventLocaleRow.entrySet()) {
                insertNode(doc, electionEventLocaleElement, column.getKey(), getString(column.getValue()));
            }
        }
    }

    private void insertContestNodes(final Document doc,
                                    final String localeId,
                                    final Integer electionEventPk,
                                    final Map<String, HashMap<String, HashMap<String, String>>> text,
                                    final Map<String, Object> electionRow,
                                    final Element electionElement) {
        List<LinkedHashMap<String, Object>> contests = searchSql(CONTESTS_DATA, electionRow.get(ELECTION_PK));
        for (LinkedHashMap<String, Object> contestRow : contests) {
            Element contestElement = insertNode(doc, electionElement, "contest");
            for (Entry<String, Object> column : contestRow.entrySet()) {
                insertNode(doc, contestElement, column.getKey(), getString(column.getValue()));
            }

            // insert text name ids and corresponding text ids for the contest.
            insertTextsForContest(doc, electionEventPk, text, contestRow, contestElement);

            // insert the reporting_unit nodes
            insertReportingUnits(doc, localeId, text, contestRow, contestElement);

            // insert the area nodes
            List<LinkedHashMap<String, Object>> areas = searchSql(AREA_NODES, contestRow.get(CONTEST_PK));
            for (LinkedHashMap<String, Object> areaRow : areas) {
                Element areaElement = insertNode(doc, contestElement, "area");
                for (Entry<String, Object> column : areaRow.entrySet()) {
                    insertNode(doc, areaElement, column.getKey(), getString(column.getValue()));
                }
            }

            // insert the polling_place nodes
            insertPollingPlaces(doc, contestRow, contestElement);
        }
    }

    private void insertTextsForContest(final Document doc, final Integer electionEventPk, final Map<String, HashMap<String, HashMap<String, String>>> text,
                                       final Map<String, Object> contestRow, final Element contestElement) {
        List<LinkedHashMap<String, Object>> contestTexts = searchSql(CONTEST_TEXT, contestRow.get(CONTEST_PK));
        for (LinkedHashMap<String, Object> contestTextRow : contestTexts) {
            Element contestDescriptionElement = insertNode(doc, contestElement, "description");
            Element textIdElement = insertNode(doc, contestDescriptionElement, TEXT_ID);
            for (Entry<String, Object> column : contestTextRow.entrySet()) {
                insertNode(doc, textIdElement, column.getKey(), getString(column.getValue()));
            }
            Map<String, String> lHash = text.get(electionEventPk.toString()).get(getString(contestTextRow.get("contest_text_name")));
            ArrayList<String> sortedKeys = new ArrayList<>(lHash.keySet());
            Collections.sort(sortedKeys);
            for (String key : sortedKeys) {
                Element textNameElement = insertNode(doc, contestDescriptionElement, "text_name");
                insertNode(doc, textNameElement, TEXT_ID, getString(contestTextRow.get("contest_text_name")));
                insertNode(doc, textNameElement, LOCALE_ID, key);
                insertNode(doc, textNameElement, LOCALE_TEXT, lHash.get(key));

            }
            lHash = text.get(electionEventPk.toString()).get(getString(contestTextRow.get("contest_text")));
            sortedKeys = new ArrayList<>(lHash.keySet());
            Collections.sort(sortedKeys);
            for (String key : sortedKeys) {
                Element textPlainElement = insertNode(doc, contestDescriptionElement, "text_plain");
                insertNode(doc, textPlainElement, TEXT_ID, getString(contestTextRow.get("contest_text_name")));
                insertNode(doc, textPlainElement, LOCALE_ID, key);
                insertNode(doc, textPlainElement, LOCALE_TEXT, lHash.get(key));
            }
        }
    }

    private void insertPollingPlaces(final Document doc, final Map<String, Object> contestRow, final Element contestElement) {
        List<LinkedHashMap<String, Object>> places = searchSql(POLLING_PLACES, contestRow.get(CONTEST_PK));
        for (LinkedHashMap<String, Object> placeRow : places) {
            Element pollingPlaceElement = insertNode(doc, contestElement, "polling_place");
            for (Entry<String, Object> column : placeRow.entrySet()) {
                insertNode(doc, pollingPlaceElement, column.getKey(), getString(column.getValue()));
            }

            List<LinkedHashMap<String, Object>> stations = searchSql(POLLING_STATION, placeRow.get("polling_place_pk"));
            for (LinkedHashMap<String, Object> stationRow : stations) {
                Element stationElement = insertNode(doc, pollingPlaceElement, "polling_station");
                for (Entry<String, Object> column : stationRow.entrySet()) {
                    insertNode(doc, stationElement, column.getKey(), getString(column.getValue()));
                }
            }

            List<LinkedHashMap<String, Object>> times = searchSql(OPENING_HOURS, placeRow.get("polling_place_pk"));
            for (LinkedHashMap<String, Object> timeRow : times) {
                Element timeElement = insertNode(doc, pollingPlaceElement, "time_available");
                for (Entry<String, Object> column : timeRow.entrySet()) {
                    insertNode(doc, timeElement, column.getKey(), getString(column.getValue()));
                }
            }
        }
    }

    private void insertReportingUnits(
            Document doc, String localeId, Map<String, HashMap<String, HashMap<String, String>>> text, Map<String, Object> contestRow, Element contestElement) {
        Integer contestPk = (Integer) contestRow.get(CONTEST_PK);
        Contest contest = contestRepository.findByPk(contestPk.longValue());
        if (contest.isOnBoroughLevel()) {
            insertReportingUnitsForBoroughContest(doc, contestElement, contest, text, localeId);
        }

        List<LinkedHashMap<String, Object>> reportingUnits = searchSql(REP_UNITS_BY_CONTEST, contestPk);
        for (LinkedHashMap<String, Object> reportingUnitRow : reportingUnits) {
            Element reportingUnitElement = insertNode(doc, contestElement, "reporting_unit");
            for (Entry<String, Object> column : reportingUnitRow.entrySet()) {
                if (StringUtils.equalsIgnoreCase(column.getKey(), "reporting_unit_type_name")) {
                    // Text string lookup.
                    String reportingUnitTypeName = translateMessage(text, localeId, getString(column.getValue()));
                    insertNode(doc, reportingUnitElement, column.getKey(), reportingUnitTypeName);
                } else {
                    insertNode(doc, reportingUnitElement, column.getKey(), getString(column.getValue()));
                }
            }

            List<LinkedHashMap<String, Object>> resultsReported = searchSql(RESULTS_REPORTED,
                    new Object[]{contestPk, reportingUnitRow.get("mv_area_pk")});
            for (LinkedHashMap<String, Object> resultReportedRow : resultsReported) {
                Element resultReportedElement = insertNode(doc, reportingUnitElement, "results_reported");
                for (Entry<String, Object> column : resultReportedRow.entrySet()) {
                    if (StringUtils.equalsIgnoreCase(column.getKey(), "vote_count_category_name")) {
                        // Text string lookup.
                        String votingCountCategoryName = translateMessage(text, localeId, getString(column.getValue()));
                        insertNode(doc, resultReportedElement, column.getKey(), votingCountCategoryName);
                    } else {
                        insertNode(doc, resultReportedElement, column.getKey(), getString(column.getValue()));
                    }
                }
            }
        }
    }

    private String translateMessage(Map<String, HashMap<String, HashMap<String, String>>> text, String localeId, String message) {
        return text.get("0").get(message).get(localeId);
    }

    private void insertReportingUnitsForBoroughContest(
            Document doc, Element contestElement, Contest boroughContest, Map<String, HashMap<String, HashMap<String, String>>> text, String localeId) {
        ContestArea contestArea = boroughContest.getContestAreaList().get(0);
        AreaPath boroughAreaPath = AreaPath.from(contestArea.getMvArea().getAreaPath());
        ReportingUnit valgstyret = reportingUnitRepository.findByAreaPathAndType(boroughAreaPath.toMunicipalityPath(), VALGSTYRET);

        Element reportingUnitElement = insertNode(doc, contestElement, "reporting_unit");
        String electionPath = valgstyret.getMvElection().getElectionPath();
        insertNode(doc, reportingUnitElement, "election_path", electionPath);
        String areaPath = valgstyret.getMvArea().getAreaPath();
        insertNode(doc, reportingUnitElement, "area_path", areaPath);
        String reportingUnitTypeName = valgstyret.getReportingUnitType().getName();
        insertNode(doc, reportingUnitElement, "reporting_unit_type_name", translateMessage(text, localeId, reportingUnitTypeName));
        String nameLine = valgstyret.getNameLine();
        insertNode(doc, reportingUnitElement, "name_line", nameLine);

        MvArea valgstyretMvArea = valgstyret.getMvArea();
        List<VoteCountCategory> voteCountCategories = voteCountCategoryRepository
                .findByMunicipality(valgstyretMvArea.getMunicipality().getPk(), boroughContest.getElection().getElectionGroup().getPk(), true);
        for (VoteCountCategory voteCountCategory : voteCountCategories) {
            Element resultReportedElement = insertNode(doc, reportingUnitElement, "results_reported");
            insertNode(doc, resultReportedElement, "vote_count_category_id", voteCountCategory.getId());
            insertNode(doc, resultReportedElement, "area_path", boroughAreaPath.path());
            String voteCountCategoryName = voteCountCategory.getName();
            insertNode(doc, resultReportedElement, "vote_count_category_name", translateMessage(text, localeId, voteCountCategoryName));
        }
    }

    private void insertElectionEventTexts(final Document doc, final Element electionEventElement, final Integer electionEventPk,
                                          final Map<String, HashMap<String, HashMap<String, String>>> text) {
        List<LinkedHashMap<String, Object>> electionEventTexts = searchSql(ELECTION_EVENT_TEXTS, electionEventPk);
        for (LinkedHashMap<String, Object> electionEventTextRow : electionEventTexts) {
            Element descriptionElement = insertNode(doc, electionEventElement, "description");

            Element textIdElement = insertNode(doc, descriptionElement, TEXT_ID);
            insertNode(doc, textIdElement, ELECTION_EVENT_TEXT_NAME, getString(electionEventTextRow.get(ELECTION_EVENT_TEXT_NAME)));
            insertNode(doc, textIdElement, "election_event_text", getString(electionEventTextRow.get("election_event_text")));

            HashMap<String, HashMap<String, String>> tmp = text.get(electionEventPk.toString());
            Map<String, String> lHash = tmp.get(getString(electionEventTextRow.get(ELECTION_EVENT_TEXT_NAME)));
            ArrayList<String> sortedKeys = new ArrayList<>(lHash.keySet());
            Collections.sort(sortedKeys);
            for (String key : sortedKeys) {
                Element textNameElement = insertNode(doc, descriptionElement, "text_name");
                insertNode(doc, textNameElement, TEXT_ID, getString(electionEventTextRow.get(ELECTION_EVENT_TEXT_NAME)));
                insertNode(doc, textNameElement, LOCALE_ID, key);
                insertNode(doc, textNameElement, LOCALE_TEXT, lHash.get(key));
            }
            lHash = tmp.get(getString(electionEventTextRow.get("election_event_text")));
            sortedKeys = new ArrayList<>(lHash.keySet());
            Collections.sort(sortedKeys);
            for (String key : sortedKeys) {
                Element textNameElement = insertNode(doc, descriptionElement, "text_plain");
                insertNode(doc, textNameElement, TEXT_ID, getString(electionEventTextRow.get(ELECTION_EVENT_TEXT_NAME)));
                insertNode(doc, textNameElement, LOCALE_ID, key);
                insertNode(doc, textNameElement, LOCALE_TEXT, lHash.get(key));
            }
        }
    }

    private Map<String, HashMap<String, HashMap<String, String>>> insertGlobalTexts(Document doc, Element electionEventElement, Integer electionEventPk) {
        List<LinkedHashMap<String, Object>> textIds = searchSql(MESSAGES_ALL, new Object[]{electionEventPk});
        HashMap<String, HashMap<String, HashMap<String, String>>> text = new HashMap<>();

        for (LinkedHashMap<String, Object> textRow : textIds) {
            if (text.get(getString(textRow.get(SQLConstants.ELECTION_EVENT_PK))) != null) {
                if (text.get(getString(textRow.get(SQLConstants.ELECTION_EVENT_PK))).get(getString(textRow.get(TEXT_ID))) != null) {
                    text.get(getString(textRow.get(SQLConstants.ELECTION_EVENT_PK))).get(getString(textRow.get(TEXT_ID)))
                            .put(getString(textRow.get(LOCALE_ID)), getString(textRow.get(LOCALE_TEXT)));
                } else { // add locale_text
                    HashMap<String, String> localeIdHash = new HashMap<>();
                    localeIdHash.put(getString(textRow.get(LOCALE_ID)), getString(textRow.get(LOCALE_TEXT)));

                    text.get(getString(textRow.get(SQLConstants.ELECTION_EVENT_PK))).put(getString(textRow.get(TEXT_ID)), localeIdHash);
                }
            } else {
                HashMap<String, String> localeIdHash = new HashMap<>();
                localeIdHash.put(getString(textRow.get(LOCALE_ID)), getString(textRow.get(LOCALE_TEXT)));

                HashMap<String, HashMap<String, String>> textId = new HashMap<>();
                textId.put(getString(textRow.get(TEXT_ID)), localeIdHash);

                text.put(getString(textRow.get(SQLConstants.ELECTION_EVENT_PK)), textId);
            }
        }

        ArrayList<String> sortedElectionEventPkKeys = new ArrayList<>(text.keySet());
        Collections.sort(sortedElectionEventPkKeys);
        for (String electionEventPkKey : sortedElectionEventPkKeys) {
            ArrayList<String> sortedTextIdKeys = new ArrayList<>(text.get(electionEventPkKey).keySet());
            Collections.sort(sortedTextIdKeys);
            for (String textIdKey : sortedTextIdKeys) {
                ArrayList<String> sortedLocaleIdKeys = new ArrayList<>(text.get(electionEventPkKey).get(textIdKey).keySet());
                Collections.sort(sortedLocaleIdKeys);
                for (String localeIdKey : sortedLocaleIdKeys) {
                    Element textNameElement = insertNode(doc, electionEventElement, "messages");
                    insertNode(doc, textNameElement, TEXT_ID, textIdKey);
                    insertNode(doc, textNameElement, LOCALE_ID, localeIdKey);
                    insertNode(doc, textNameElement, LOCALE_TEXT, text.get(electionEventPkKey).get(textIdKey).get(localeIdKey));
                }
            }
        }
        return text;
    }

    private String getLocaleId(final Long localePk) {
        List<LinkedHashMap<String, Object>> locale = searchSql(GET_LOCALE_ID, new Object[]{localePk});
        return locale.get(0).get(LOCALE_ID).toString();
    }

    private List<LinkedHashMap<String, Object>> searchSql(final String sql, final Object parameter) {
        return searchSql(sql, new Object[]{parameter});
    }

    /**
     * Perform a query against the database in a generalized manner.
     */
    private List<LinkedHashMap<String, Object>> searchSql(final String sql, final Object[] parameters) {
        final List<LinkedHashMap<String, Object>> resultData = new ArrayList<>();
        Session session = batchRepository.getSession();
        session.doWork(new Work() {
            @Override
            public void execute(final Connection con) throws SQLException {
                ResultSet res = null;
                PreparedStatement stmt = null;
                LinkedHashMap<String, Object> rowData;

                try {
                    // PostgresSQL uses a slow query plan for the normal electoral roll export, but increasing cpu_tuple_cost makes it choose a better one. Note
                    // that we only do this for the normal export, since we don't know if it will have any unintended side effects on the voter card export.
                    if (sql.equals(ELECTORAL_ROLL_DATA)) {
                        con.prepareStatement("set cpu_tuple_cost = 1;").execute();
                    }

                    stmt = con.prepareStatement(sql);

                    if (parameters != null) {
                        int paramIdx = 1;
                        for (Object param : parameters) {
                            if (param instanceof Long) {
                                stmt.setLong(paramIdx, (Long) param);
                            } else if (param instanceof Integer) {
                                stmt.setInt(paramIdx, (Integer) param);
                            } else if (param instanceof String) {
                                stmt.setString(paramIdx, (String) param);
                            } else {
                                throw new EvoteException("Unrecognized parameter type: " + param.getClass().toString());
                            }

                            ++paramIdx;
                        }
                    }

                    res = stmt.executeQuery();
                    ResultSetMetaData resMeta = res.getMetaData();
                    while (res.next()) {
                        rowData = new LinkedHashMap<>();
                        resultData.add(rowData);
                        for (int columnIndex = 1; columnIndex <= resMeta.getColumnCount(); columnIndex++) {
                            rowData.put(resMeta.getColumnName(columnIndex), res.getObject(columnIndex));
                        }
                    }
                } finally {
                    if (res != null) {
                        res.close();
                    }

                    if (stmt != null) {
                        stmt.close();
                    }
                }
            }
        });

        return resultData;
    }

    private String getString(final Object string) {
        if (string instanceof Timestamp) {
            Timestamp tstamp = (Timestamp) string;
            DateTimeFormatter isoDateTimeFormatter = DateTimeFormat.forPattern(ISO_DATE_FORMAT_W_TZ).withLocale(Locale.ENGLISH);

            String formattedTs = isoDateTimeFormatter.print(new DateTime(tstamp));

            if (formattedTs.matches(".*\\+\\d\\d\\d\\d")) {

                formattedTs = formattedTs.substring(0, 22) + ':' + formattedTs.substring(22);

            }

            return formattedTs;
        }

        return string == null ? null : string.toString();
    }

    private Element insertNode(final Document doc, final Element parentNode, final String nodeName, final String nodeValue) {
        Element element = doc.createElement(nodeName);
        element.appendChild(doc.createTextNode(nodeValue == null ? "" : nodeValue));
        parentNode.appendChild(element);
        return element;
    }

    private Element insertNode(final Document doc, final Element parentNode, final String nodeName) {
        Element element = doc.createElement(nodeName);
        parentNode.appendChild(element);
        return element;
    }

    public byte[] getGeneratedEML(Long batchPk) {
        return batchService.getBinaryDataFromBatch(batchPk);
    }

    public boolean validateGeneratedEML(Long batchPk) {
        byte[] bytes = getGeneratedEML(batchPk);
        ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(bytes));
        try {
            ZipEntry zipEntry;
            while ((zipEntry = zip.getNextEntry()) != null) {
                if (!zipEntry.isDirectory()) {
                    if (zipEntry.getName().equalsIgnoreCase("ElectionEvent.xml")) {
                        XMLUtil.validateAgainstSchema(getZipDoc(zip), "OASIS-EML-v50-OS/110-electionevent-v5-0.xsd");
                    } else if (zipEntry.getName().equalsIgnoreCase("CandidateList.xml")) {
                        XMLUtil.validateAgainstSchema(getZipDoc(zip), "OASIS-EML-v50-OS/230-candidatelist-v5-0.xsd", "xsd/configuration-v1-0.xsd");
                    }
                }
            }
        } catch (SAXException e) {
            return false;
        } catch (Exception e) {
            throw new EvoteException(e.getMessage());
        }
        return true;
    }

    private String getZipDoc(ZipInputStream zip) throws IOException {
        return new String(IOUtil.getBytes(zip), EvoteConstants.CHARACTER_SET);
    }

    public List<BatchInfoDto> getGeneratedEMLBatches(String electionEventId) {
        return getGeneratedBatches(electionEventId, CONFIGURATION_DOWNLOAD);
    }

    @SuppressWarnings("unchecked")
    private List<BatchInfoDto> getGeneratedBatches(String electionEventId, Jobbkategori category) {
        List<Batch> batches = batchRepository.findByElectionEventIdAndCategory(electionEventId, category);
        List<BatchInfoDto> result = new ArrayList<>();
        for (Batch batch : batches) {
            result.add(new BatchInfoDto(batch.getPk(), batch.getAuditTimestamp(), batch.getBatchStatus().getId(), batch.getInfoText()));
        }
        return result;
    }

    public List<BatchInfoDto> getGeneratedElectoralRollBatches(String electionEventId) {
        return getGeneratedBatches(electionEventId, ELECTORAL_ROLL_DOWNLOAD);
    }

    private enum ExportType {
        ALL("@electoralRoll.type.all"), READY("@electoralRoll.type.ready"), CARD("@electoralRoll.type.card");

        private final String id;

        ExportType(final String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }
    }

    private interface BatchContent {
        void generate(UserData userData, String electionEventId, Batch batch, ZipOutputStream zout);
    }

}
