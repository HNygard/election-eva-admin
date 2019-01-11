package no.evote.service.util;

import lombok.extern.log4j.Log4j;
import no.evote.exception.EvoteException;
import no.evote.security.UserData;
import no.evote.service.rbac.OperatorRoleServiceBean;
import no.evote.service.rbac.OperatorServiceBean;
import no.valg.eva.admin.backend.common.repository.LocaleTextRepository;
import no.valg.eva.admin.backend.configuration.repository.ElectionEventRepository;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.rbac.AreaAndElectionLevelVerifier;
import no.valg.eva.admin.common.rbac.ImportOperatorMessage;
import no.valg.eva.admin.common.rbac.OperatorExportFormat;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.configuration.repository.MvAreaRepository;
import no.valg.eva.admin.configuration.repository.MvElectionRepository;
import no.valg.eva.admin.rbac.domain.model.Operator;
import no.valg.eva.admin.rbac.domain.model.OperatorRole;
import no.valg.eva.admin.rbac.domain.model.Role;
import no.valg.eva.admin.rbac.repository.OperatorRepository;
import no.valg.eva.admin.rbac.repository.RoleRepository;
import no.valg.eva.admin.util.ExcelUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.hibernate.Session;
import org.hibernate.jdbc.Work;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;
import static no.valg.eva.admin.common.rbac.Accesses.Tilgang_Brukere_Administrere_Brukere_Underliggende_Nivå;
import static no.valg.eva.admin.common.rbac.OperatorExportFormat.VALGANSVARLIG_CIM;
import static no.valg.eva.admin.common.rbac.OperatorExportFormat.VALGANSVARLIG_EVA;
import static no.valg.eva.admin.util.ExcelUtil.VALUES_FROM_PAIRS_F;

@Log4j
public class ExportImportOperatorsServiceBean {

    private static final int EXPORT_HEADER_COLUMNS = 16;

    private static final int ROLE_ID_COLUMN = 13;

    private static final int AREA_PATH_COLUMN = 14;

    private static final int ELECTION_PATH_COLUMN = 15;

    private static final List<String> OPERATOR_EXPORT_HEADER = Arrays.asList(
            "Fødselsnummer",
            "Fornavn",
            "Mellomnavn",
            "Etternavn",
            "Adresselinje 1",
            "Adresselinje 2",
            "Adresselinje 3",
            "Postnummer",
            "Sted",
            "E-post",
            "Telefonnummer",
            "Infotekst",
            "Parti",
            "Rolle",
            "Områdehierarki",
            "Valghierarki"
    );

    private static final List<String> CIM_EXPORT_HEADER = Arrays.asList(
            "lastname",
            "firstname",
            "username",
            "job_title",
            "job_mobile",
            "job_phone",
            "job_phone_2",
            "private_phone",
            "email",
            "email_secondary",
            "company",
            "department",
            "password",
            "user_type",
            "primary_role",
            "roles"
    );

    @PersistenceContext(unitName = "evotePU")
    private EntityManager entityManager;

    @Inject
    private OperatorServiceBean operatorService;
    @Inject
    private OperatorRepository operatorRepository;
    @Inject
    private OperatorRoleServiceBean operatorRoleService;
    @Inject
    private ElectionEventRepository electionEventRepository;
    @Inject
    private RoleRepository roleRepository;
    @Inject
    private MvElectionRepository mvElectionRepository;
    @Inject
    private LocaleTextRepository localeTextRepository;
    @Inject
    private MvAreaRepository mvAreaRepository;

    private String evaOperators = "select o.operator_id, "
            + "  o.first_name, "
            + "  o.middle_name, "
            + "  o.last_name, "
            + "  o.address_line1, "
            + "  o.address_line2, "
            + "  o.address_line3, "
            + "  o.postal_code, "
            + "  o.post_town, "
            + "  o.email, "
            + "  o.telephone_number, "
            + "  o.info_text, "
            + "  p.party_id, "
            + "  r.role_id, "
            + "  mva.area_path, "
            + "  mve.election_path "
            + "from"
            + "   operator o "
            + "   left join party p on o.party_pk = p.party_pk and o.election_event_pk = p.election_event_pk "
            + "   left join operator_role oro on o.operator_pk = oro.operator_pk "
            + "   left join role r on oro.role_pk = r.role_pk "
            + "   left join mv_area mva on oro.mv_area_pk = mva.mv_area_pk "
            + "  left join mv_election mve on oro.mv_election_pk = mve.mv_election_pk "
            + "  where o.election_event_pk = ? and oro.role_pk is not null ";

    private String cimOperators = "select o.last_name as lastname,"
            + "  o.first_name as firstname, "
            + "  o.email as username, "
            + "  '' as job_title, "
            + "  o.telephone_number as job_mobile, "
            + "  '' as job_phone, "
            + "  '' as job_phone_2, "
            + "  '' as private_phone, "
            + "  o.email as email, "
            + "  '' as email_secondary, "
            + "  mva.municipality_id as company, "
            + "  mva.municipality_name as department, "
            + "  '' as password, "
            + "  'contact' as user_type, "
            + "  '' as primary_role, "
            + "  '' as roles "
            + "from"
            + "   operator o "
            + "   left join party p on o.party_pk = p.party_pk and o.election_event_pk = p.election_event_pk "
            + "   left join operator_role oro on o.operator_pk = oro.operator_pk "
            + "   left join role r on oro.role_pk = r.role_pk "
            + "   left join mv_area mva on oro.mv_area_pk = mva.mv_area_pk "
            + "  left join mv_election mve on oro.mv_election_pk = mve.mv_election_pk "
            + "  where o.election_event_pk = ? and oro.role_pk is not null "
            + "  and r.role_id like 'valgansvarlig_kommune' or r.role_id like 'valgadmin_kommune_oslo' ";

    public byte[] exportOperatorRoles(final UserData userData, final Long electionEventPk, OperatorExportFormat format) {
        List<String> header = new ArrayList<>();
        if (format == VALGANSVARLIG_CIM) {
            header.addAll(CIM_EXPORT_HEADER);
        } else {
            header.addAll(OPERATOR_EXPORT_HEADER);
        }

        for (String headerCell : header) {
            if (headerCell.charAt(0) == '@') {
                log.warn("Støtter ikke lenger oversettelse av " + headerCell);
            } else {
                log.info(headerCell);
            }
        }

        final List<List<String>> rows = new ArrayList<>();
        rows.add(header);

        Session session = (Session) entityManager.getDelegate();
        session.doWork(new Work() {
            @Override
            public void execute(final Connection con) throws SQLException {
                final boolean cantSeeBelow = userCannotAdministrateUsersOnLowerLevel(userData);

                StringBuilder sqlBuilder = new StringBuilder();
                sqlBuilder.append((format == VALGANSVARLIG_CIM) ? cimOperators : evaOperators);

                if (format == VALGANSVARLIG_EVA) {
                    sqlBuilder.append(" and (r.role_id like 'valgansvarlig%' or r.role_id like 'valgadmin%')");
                }

                sqlBuilder.append(cantSeeBelow ? " AND mva.area_path = ?" : " AND text2ltree(mva.area_path) <@ text2ltree(?)");

                try (PreparedStatement stmt = con.prepareStatement(sqlBuilder.toString())) {

                    stmt.setLong(1, electionEventPk);
                    stmt.setString(2, userData.getOperatorRole().getMvArea().getAreaPath());

                    try (ResultSet res = stmt.executeQuery()) {
                        while (res.next()) {
                            List<String> row = new ArrayList<>();

                            for (int columnIndex = 1; columnIndex <= EXPORT_HEADER_COLUMNS; columnIndex++) {

                                Object object = res.getObject(columnIndex);
                                row.add((object == null) ? "" : makeExportString(object));
                            }
                            rows.add(row);
                        }
                    }
                }
            }

            private String makeExportString(final Object object) {
                return object instanceof MvElection ? ((MvElection) object).getElectionPath() : object.toString();
            }
        });

        if (format == VALGANSVARLIG_CIM) {
            return createCimFrom(rows);
        }
        return ExcelUtil.createXlsxFromRowData(rows);
    }

    private boolean userCannotAdministrateUsersOnLowerLevel(final UserData userData) {
        return !userData.hasAccess(Tilgang_Brukere_Administrere_Brukere_Underliggende_Nivå);
    }

    private byte[] createCimFrom(List<List<String>> rows) {
        String data = rows.stream().reduce("", this::rowAccumulator, (s1, s2) -> s1 + "\n" + s2);
        return data.getBytes(UTF_8);
    }

    private String rowAccumulator(String previousData, List<String> columns) {
        return previousData + columns.stream().reduce(null, this::columnAccumulator, this::columnAccumulator) + "\n";
    }

    private String columnAccumulator(String previousData, String column) {
        return previousData == null ? column : previousData + "," + column;
    }

    public List<ImportOperatorMessage> importOperatorRoles(final UserData userData, final Long electionEventPk, final byte[] data) {
        final ImportMessages messages = new ImportMessages();
        final ElectionEvent electionEvent = electionEventRepository.findByPk(electionEventPk);
        try {
            List<String[]> rows = ExcelUtil.getRowDataFromExcelFile(new ByteArrayInputStream(data)).getRows().stream()
                    .map(VALUES_FROM_PAIRS_F)
                    .collect(Collectors.toList());

            if (isHeader(rows.get(0))) {
                rows.remove(0); // First row contains header, so we skip it
            }

            Map<String, Operator> operatorCache = new HashMap<>();
            for (String[] row : rows) {
                Logger.getLogger(this.getClass()).info(StringUtils.join(row, ';'));

                if (hasCorrectElectionEventId(electionEvent, row)) {

                    String operatorId = row[0];
                    Operator operator = operatorCache.get(operatorId);
                    if (operator == null) {
                        operator = importOperator(userData, messages, electionEvent, row);
                    }

                    if (operator != null) {
                        operatorCache.put(operatorId, operator);
                        importOperatorRole(userData, messages, electionEvent, row, operator);
                    }
                } else {
                    messages.add(true, "@rbac.import_operators.wrong_eevent");
                }
            }
        } catch (InvalidFormatException | IOException | ArrayIndexOutOfBoundsException e) {
            throw new EvoteException(e.getMessage(), e);
        }

        return messages.getMessageList();
    }

    private boolean hasCorrectElectionEventId(final ElectionEvent electionEvent, final String[] row) {

        String areaPath = row[AREA_PATH_COLUMN].trim();
        String electionPath = row[ELECTION_PATH_COLUMN].trim();

        if (areaPath.length() == 0 && electionPath.length() == 0) {
            return true;
        }


        return getElectionEventIdFromPath(areaPath).equals(electionEvent.getId()) && getElectionEventIdFromPath(electionPath).equals(electionEvent.getId());
    }

    private String getElectionEventIdFromPath(final String path) {
        return path.indexOf('.') != -1 ? path.split("\\.")[0] : path;
    }

    private boolean isHeader(final String[] row) {
        return !row[0].matches("\\d+"); // We assume the row isn't a header if the first cell is something that looks like an SSN
    }

    private Operator importOperator(final UserData userData, final ImportMessages messages, final ElectionEvent electionEvent, final String[] row) {
        String operatorId = row[0];
        if (operatorId != null) {
            operatorId = operatorId.trim();
        }

        // Fetch operator if it exists, or create a new one
        Operator operator = operatorRepository.findByElectionEventsAndId(electionEvent.getPk(), operatorId);
        if (operator != null) {
            return operator;
        }

        // Validate operator. Common error message if one or more fields are not valid
        operator = populateOperator(electionEvent, row);

        List<String> validationFeedback = operatorService.validateOperator(operator, electionEvent);
        if (!validationFeedback.isEmpty()) {
            boolean first = true;
            for (String error : validationFeedback) {
                if (error.startsWith("{@") && error.endsWith("}")) {
                    error = error.substring(1, error.length() - 1);
                }
                messages.add(first, error);
                first = false;
            }
            return null;
        }
        operator = operatorRepository.create(userData, operator);
        messages.add(false, "@rbac.import_operators.created_new", operator.getId());
        return operator;
    }

    private void importOperatorRole(final UserData userData, final ImportMessages messages, final ElectionEvent electionEvent, final String[] row,
                                    final Operator operator) {

        String roleId = row[ROLE_ID_COLUMN];
        Role role = findImportRole(electionEvent, roleId);
        if (role == null) {
            messages.add(true, "@rbac.import_operators.unknown_role", roleId);
            return;
        }

        String areaPath = row[AREA_PATH_COLUMN].trim();
        MvArea mvArea = findImportMvArea(electionEvent, areaPath);

        if (mvArea == null) {
            messages.add(true, "@rbac.import_operators.unknown_area", areaPath);
            return;
        }

        String electionPath = row[ELECTION_PATH_COLUMN].trim();

        MvElection mvElection = findImportMvElection(electionEvent, electionPath);

        if (mvElection == null) {
            messages.add(true, "@rbac.import_operators.unknown_election", electionPath);
            return;
        }

        OperatorRole operatorRole = new OperatorRole();
        operatorRole.setOperator(operator);
        operatorRole.setRole(role);
        operatorRole.setMvArea(mvArea);
        operatorRole.setMvElection(mvElection);

        if (isOnHigherLevel(userData, operatorRole)) {
            messages.add(true, "@rbac.import_operators.is_on_higher_level");
        } else if (userCannotAdministrateUsersOnLowerLevel(userData) && isNotOnSameLevel(userData, operatorRole)) {
            messages.add(true, "@rbac.import_operators.is_not_on_same_level");
        } else {
            if (!operatorAlreadyHasRole(operatorRole)) {
                operatorRoleService.create(userData, operatorRole);

                String namedAreaPath = mvArea.getNamedPath();
                boolean areaIsTopLevel = namedAreaPath.length() == 0;
                String namedElectionPath = mvElection.getNamedPath();
                boolean electionIsTopLevel = namedElectionPath.length() == 0;

                if (!areaIsTopLevel && !electionIsTopLevel) {
                    messages.add(true, "@rbac.import_operators.msg1",
                            localeTextRepository.findByElectionEventLocaleAndTextId(electionEvent.getPk(), userData.getLocale().getPk(), role.getName())
                                    .getLocaleText(),
                            namedAreaPath, namedElectionPath);
                } else if (areaIsTopLevel && electionIsTopLevel) {
                    messages.add(true, "@rbac.import_operators.msg2",
                            localeTextRepository.findByElectionEventLocaleAndTextId(electionEvent.getPk(), userData.getLocale().getPk(), role.getName())
                                    .getLocaleText());
                } else if (areaIsTopLevel) {
                    messages.add(true, "@rbac.import_operators.msg3",
                            localeTextRepository.findByElectionEventLocaleAndTextId(electionEvent.getPk(), userData.getLocale().getPk(), role.getName())
                                    .getLocaleText(),
                            namedElectionPath);
                } else {
                    messages.add(true, "@rbac.import_operators.msg4",
                            localeTextRepository.findByElectionEventLocaleAndTextId(electionEvent.getPk(), userData.getLocale().getPk(), role.getName())
                                    .getLocaleText(),
                            namedAreaPath);
                }
            } else {
                messages.add(true, "@rbac.import_operators.has_role");
            }
        }
    }

    private MvElection findImportMvElection(ElectionEvent electionEvent, String electionPathString) {
        MvElection mvElection;
        if (electionPathString.length() != 0) {
            mvElection = mvElectionRepository.finnEnkeltMedSti(ElectionPath.from(electionPathString).tilValghierarkiSti());
        } else {
            mvElection = mvElectionRepository.findRoot(electionEvent.getPk());
        }
        return mvElection;
    }

    private MvArea findImportMvArea(ElectionEvent electionEvent, String areaPathString) {
        MvArea mvArea;
        if (areaPathString.length() != 0) {
            mvArea = mvAreaRepository.findSingleByPath(AreaPath.from(areaPathString));
        } else {
            mvArea = mvAreaRepository.findRoot(electionEvent.getPk());
        }
        return mvArea;
    }

    private Role findImportRole(final ElectionEvent electionEvent, final String roleId) {
        return roleRepository.findByElectionEventAndId(electionEvent, roleId);
    }

    private boolean isNotOnSameLevel(final UserData userData, final OperatorRole operatorRole) {
        return !userData.getOperatorRole().getMvArea().getAreaPath().equals(operatorRole.getMvArea().getAreaPath());
    }

    private boolean isOnHigherLevel(final UserData userData, final OperatorRole operatorRole) {
        try {
            AreaAndElectionLevelVerifier.getInstance().verifyAreaAndElectionLevels(userData, operatorRole);
        } catch (EvoteException e) {
            log.warn("Exception verifying area and election levels: " + e.getMessage(), e);
            return true;
        }
        return false;
    }

    /**
     * Check if user already has a role
     */
    private boolean operatorAlreadyHasRole(final OperatorRole operatorRole) {
        MvArea mvArea = operatorRole.getMvArea();
        MvElection mvElection = operatorRole.getMvElection();
        for (OperatorRole existingOperatorRole : operatorRoleService.getOperatorRoles(operatorRole.getOperator())) {
            if (mvArea.getAreaPath().equals(existingOperatorRole.getMvArea().getAreaPath())
                    && mvElection.getElectionPath().equals(existingOperatorRole.getMvElection().getElectionPath())
                    && existingOperatorRole.getRole().getId().equals(operatorRole.getRole().getId())) {
                return true;
            }
        }

        return false;
    }

    private Operator populateOperator(final ElectionEvent electionEvent, final String[] row) {
        Operator operator = new Operator();

        int index = 0;
        String id = row[index++];
        if (id != null) {
            id = id.trim();
        }
        operator.setElectionEvent(electionEvent);
        operator.setId(id);
        operator.setFirstName(row[index++]);
        operator.setMiddleName(row[index++]);
        operator.setLastName(row[index++]);
        operator.setAddressLine1(row[index++]);
        operator.setAddressLine2(row[index++]);
        operator.setAddressLine3(row[index++]);
        operator.setPostalCode(row[index++]);
        operator.setPostTown(row[index++]);
        operator.setEmail(row[index++]);
        operator.setTelephoneNumber(row[index++]);
        operator.setInfoText(row[index]);
        operator.setActive(true);
        setNameLine(operator);

        return operator;
    }

    /**
     * This method ensures that the name of operator is presented in the format Lastname FirstName
     */
    private void setNameLine(final Operator operator) {
        StringBuilder nameLine = new StringBuilder();
        if (operator.getLastName() != null) {
            nameLine.append(operator.getLastName());
            nameLine.append(" ");
        }
        if (operator.getFirstName() != null) {
            nameLine.append(operator.getFirstName());
            nameLine.append(" ");
        }
        if (operator.getMiddleName() != null) {
            nameLine.append(operator.getMiddleName());
        }
        operator.setNameLine(nameLine.toString().trim());
    }

    /**
     * Keeps track of messages for each line in the imported file
     */
    static class ImportMessages {
        private final List<ImportOperatorMessage> messages = new ArrayList<>();
        private int line = 1;

        public void add(boolean changeLine, String message, Object... args) {
            messages.add(new ImportOperatorMessage(line, message, args));

            if (changeLine) {
                line++;
            }
        }

        List<ImportOperatorMessage> getMessageList() {
            return messages;
        }
    }
}
