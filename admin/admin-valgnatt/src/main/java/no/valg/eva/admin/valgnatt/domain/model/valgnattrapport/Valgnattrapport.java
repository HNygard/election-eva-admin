package no.valg.eva.admin.valgnatt.domain.model.valgnattrapport;

import static no.valg.eva.admin.valgnatt.domain.model.valgnattrapport.ValgnattrapportStatus.OK;
import static no.valg.eva.admin.valgnatt.domain.model.valgnattrapport.ValgnattrapportStatus.RESENDES;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.ColumnResult;
import javax.persistence.ConstructorResult;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedNativeQueries;
import javax.persistence.NamedNativeQuery;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.SqlResultSetMapping;
import javax.persistence.SqlResultSetMappings;
import javax.persistence.Table;
import javax.persistence.Transient;

import no.evote.model.VersionedEntity;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.Election;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.counting.domain.model.report.ReportType;

import org.joda.time.DateTime;

@Entity
@Table(name = "valgnattrapport")
@AttributeOverride(name = "pk", column = @Column(name = "valgnattrapport_pk"))
@NamedQueries({
		@NamedQuery(
				name = "Valgnattrapport.byContestAndMunicipality",
				query = "select v from Valgnattrapport v where v.contest.pk = :contestPk and v.municipality.pk = :municipalityPk"),
		@NamedQuery(
				name = "Valgnattrapport.byContestAndReportType",
				query = "select v from Valgnattrapport v where v.contest.pk = :contestPk and v.reportType = :reportType"),
		@NamedQuery(
				name = "Valgnattrapport.byContestReportTypeAndMvArea",
				query = "select v from Valgnattrapport v where v.contest.pk = :contestPk and v.reportType = :reportType and v.mvArea.pk = :mvAreaPk"),
		@NamedQuery(
				name = "Valgnattrapport.byElectionAndReportType",
				query = "SELECT v FROM Valgnattrapport v WHERE v.election.pk = :electionPk "
						+ "AND v.reportType = :reportType ") })
@NamedNativeQueries({
		//           for å beregne antall rapporter til media som er utestående. Den domenelogikken ytte ikke spesielt godt for store kommuner
		//           med mange kretser og førte til at siden lastet veldig langsomt. Spørringen er lang og kompleks fordi
		//           statusen (klar for rapportering) som beregnes ikke blir lagret i valgnattrapport-tabellen.
		//               beregne statusen hver gang valgnattrapporter hentes ut
		@NamedNativeQuery(
				name = "Valgnattrapport.countByContestAndMunicipality",
				query = "WITH single_contest AS (\n" +
						"    SELECT *\n" +
						"    FROM mv_election\n" +
						"    WHERE contest_pk = ?0\n" +
						"), single_municipality AS (\n" +
						"    SELECT m.*\n" +
						"    FROM municipality m\n" +
						"    WHERE municipality_pk = ?1\n" +
						"), vc AS (\n" +
						"    SELECT\n" +
						"      vc.vote_count_id,\n" +
						"      vc.vote_count_pk,\n" +
						"      vc.mv_area_pk,\n" +
						"      mva_vc.borough_pk,\n" +
						"      mva_vc.area_path,\n" +
						"      mva_vc.polling_district_pk,\n" +
						"      vcc.vote_count_category_id,\n" +
						"      cq.count_qualifier_id,\n" +
						"      vcs.vote_count_status_id\n" +
						"    FROM vote_count vc\n" +
						"      JOIN mv_area mva_vc ON mva_vc.mv_area_pk = vc.mv_area_pk\n" +
						"      JOIN single_municipality sm ON sm.municipality_pk = mva_vc.municipality_pk\n" +
						"      JOIN vote_count_category vcc USING (vote_count_category_pk)\n" +
						"      JOIN vote_count_status vcs USING (vote_count_status_pk)\n" +
						"      JOIN count_qualifier cq USING (count_qualifier_pk)\n" +
						"      JOIN contest_report cr USING (contest_report_pk)\n" +
						"      JOIN single_contest sc USING (contest_pk)\n" +
						"      JOIN reporting_unit ru USING (reporting_unit_pk)\n" +
						"      JOIN mv_area mva_ru ON mva_ru.mv_area_pk = ru.mv_area_pk AND mva_ru.area_level <> 2\n" +
						"    WHERE vcs.vote_count_status_id IN (2, 3) AND cq.count_qualifier_id IN ('F', 'E')\n" +
						"), filtered_report_count_category AS (\n" +
						"    SELECT\n" +
						"      sc.election_event_pk,\n" +
						"      sc.area_level AS contest_area_level,\n" +
						"      rcc.municipality_pk,\n" +
						"      vote_count_category_id,\n" +
						"      polling_district_count,\n" +
						"      technical_polling_district_count\n" +
						"    FROM report_count_category rcc\n" +
						"      JOIN vote_count_category vcc USING (vote_count_category_pk)\n" +
						"      JOIN single_contest sc ON sc.election_group_pk = rcc.election_group_pk\n" +
						"      JOIN single_municipality sm ON sm.municipality_pk = rcc.municipality_pk\n" +
						"), mva AS (\n" +
						"    SELECT mva.*\n" +
						"    FROM mv_area mva\n" +
						"      JOIN single_municipality sm USING (municipality_pk)\n" +
						"    WHERE mva.area_level = 5\n" +
						"), mva_antall_fo AS (\n" +
						"    SELECT\n" +
						"      mva.municipality_pk,\n" +
						"      count(DISTINCT mva.mv_area_pk) AS antall_omrader_fo\n" +
						"    FROM mva\n" +
						"      JOIN filtered_report_count_category frcc ON frcc.municipality_pk = mva.municipality_pk AND frcc.vote_count_category_id = 'FO'\n" +
						"      JOIN polling_district pd ON pd.polling_district_pk = mva.polling_district_pk\n" +
						"                                  AND (frcc.technical_polling_district_count AND pd.technical_polling_district\n" +
						"                                       OR NOT frcc.technical_polling_district_count AND pd.municipality)\n" +
						"    GROUP BY mva.municipality_pk\n" +
						"), mva_antall_fs AS (\n" +
						"    SELECT\n" +
						"      mva.municipality_pk,\n" +
						"      count(DISTINCT mva.mv_area_pk) AS antall_omrader_fs\n" +
						"    FROM mva\n" +
						"      JOIN filtered_report_count_category frcc ON frcc.municipality_pk = mva.municipality_pk AND frcc.vote_count_category_id = 'FS'\n" +
						"      JOIN polling_district pd ON pd.polling_district_pk = mva.polling_district_pk AND pd.municipality\n" +
						"    GROUP BY mva.municipality_pk\n" +
						"), mva_vo AS (\n" +
						"    SELECT\n" +
						"      mva.mv_area_pk\n" +
						"    FROM mva\n" +
						"      JOIN filtered_report_count_category frcc ON frcc.municipality_pk = mva.municipality_pk AND frcc.vote_count_category_id = 'VO'\n" +
						"      JOIN polling_district pd ON pd.polling_district_pk = mva.polling_district_pk\n" +
						"                                  AND (frcc.polling_district_count AND NOT pd.technical_polling_district\n" +
						"                                       AND NOT pd.municipality AND pd.parent_polling_district_pk IS NULL\n" +
						"                                       OR NOT frcc.polling_district_count AND pd.municipality)\n" +
						"), mva_vf AS (\n" +
						"    SELECT\n" +
						"      mva.mv_area_pk\n" +
						"    FROM mva\n" +
						"      JOIN filtered_report_count_category frcc ON frcc.municipality_pk = mva.municipality_pk AND frcc.vote_count_category_id = 'VF'\n" +
						"      JOIN polling_district pd ON pd.polling_district_pk = mva.polling_district_pk\n" +
						"                                  AND (frcc.polling_district_count AND NOT pd.technical_polling_district\n" +
						"                                       AND NOT pd.municipality AND pd.parent_polling_district_pk IS NULL\n" +
						"                                       OR NOT frcc.polling_district_count AND pd.municipality)\n" +
						"), mva_vb AS (\n" +
						"    SELECT\n" +
						"      mva.mv_area_pk\n" +
						"    FROM mva\n" +
						"      JOIN filtered_report_count_category frcc ON frcc.municipality_pk = mva.municipality_pk AND frcc.vote_count_category_id = 'VB'\n" +
						"      JOIN polling_district pd ON pd.polling_district_pk = mva.polling_district_pk AND pd.municipality\n" +
						"), mva_vs AS (\n" +
						"    SELECT\n" +
						"      mva.mv_area_pk\n" +
						"    FROM mva\n" +
						"      JOIN filtered_report_count_category frcc ON frcc.municipality_pk = mva.municipality_pk AND frcc.vote_count_category_id = 'VS'\n" +
						"      JOIN polling_district pd ON pd.polling_district_pk = mva.polling_district_pk AND pd.municipality\n" +
						"), vr_status AS (\n" +
						"    SELECT\n" +
						"      vr.report_type,\n" +
						"      mva.area_path,\n" +
						"      vr.status,\n" +
						"      COALESCE(AVG(mva_antall_fo.antall_omrader_fo), 0) + COALESCE(AVG(mva_antall_fs.antall_omrader_fs), 0)\n" +
						"      + COUNT(DISTINCT mva_vo.mv_area_pk) + COUNT(DISTINCT mva_vf.mv_area_pk)\n" +
						"      + COUNT(DISTINCT mva_vb.mv_area_pk) + COUNT(DISTINCT mva_vs.mv_area_pk) AS antall_telleomrader,\n" +
						"      COUNT(DISTINCT vc.vote_count_pk)                                           antall_tellinger_godkjent\n" +
						"    FROM valgnattrapport vr\n" +
						"      JOIN mv_area mva USING (mv_area_pk)\n" +
						"      JOIN single_contest sc USING (contest_pk)\n" +
						"      JOIN single_municipality sm ON sm.municipality_pk = vr.municipality_pk\n" +
						"      LEFT JOIN mva_antall_fo ON vr.report_type IN ('STEMMESKJEMA_FF', 'STEMMESKJEMA_FE')\n" +
						"        AND mva_antall_fo.municipality_pk = vr.municipality_pk\n" +
						"      LEFT JOIN mva_antall_fs ON vr.report_type = 'STEMMESKJEMA_FE' AND mva_antall_fs.municipality_pk = vr.municipality_pk\n" +
						"      LEFT JOIN mva_vo ON vr.report_type IN ('STEMMESKJEMA_VF', 'STEMMESKJEMA_VE') AND mva_vo.mv_area_pk = vr.mv_area_pk\n" +
						"      LEFT JOIN mva_vf ON vr.report_type = 'STEMMESKJEMA_VE' AND mva_vf.mv_area_pk = vr.mv_area_pk\n" +
						"      LEFT JOIN mva_vb ON vr.report_type = 'STEMMESKJEMA_VE' AND mva_vb.mv_area_pk = vr.mv_area_pk\n" +
						"      LEFT JOIN mva_vs ON vr.report_type = 'STEMMESKJEMA_VE' AND mva_vs.mv_area_pk = vr.mv_area_pk\n" +
						"      LEFT JOIN vc ON (vr.report_type = 'STEMMESKJEMA_FF' AND vc.borough_pk = mva.borough_pk\n" +
						"                        AND vc.vote_count_category_id = 'FO' AND vc.count_qualifier_id = 'F')\n" +
						"                      OR (vr.report_type = 'STEMMESKJEMA_FE' AND vc.borough_pk = mva.borough_pk\n" +
						"                           AND vc.vote_count_category_id IN ('FO', 'FS') AND vc.count_qualifier_id = 'E')\n" +
						"                      OR (vr.report_type = 'STEMMESKJEMA_VF' AND vc.mv_area_pk = mva.mv_area_pk\n" +
						"                           AND vc.vote_count_category_id = 'VO' AND vc.count_qualifier_id = 'F')\n" +
						"                      OR (vr.report_type = 'STEMMESKJEMA_VE' AND vc.mv_area_pk = mva.mv_area_pk\n" +
						"                           AND vc.vote_count_category_id IN ('VO', 'VF', 'VB', 'VS') AND vc.count_qualifier_id = 'E')\n" +
						"                      OR (vr.report_type = 'VALGOPPGJOR' AND sc.area_level = 3)\n" +
						"    GROUP BY vr.report_type, mva.area_path, vr.status\n" +
						")\n" +
						"SELECT \n" +
						"  CAST(COUNT(*) AS INTEGER) AS antall_rapporter,\n" +
						"  CAST(COALESCE(SUM(CASE WHEN antall_tellinger_godkjent >= antall_telleomrader\n" +
						"                      AND status IN ('NOT_SENT', 'RESENDES') THEN 1 ELSE 0 END), 0) AS INTEGER) AS antall_rapporterbare,\n" +
						"  CAST(COALESCE(SUM(CASE WHEN status <> 'OK' THEN 1 ELSE 0 END), 0) AS INTEGER) AS antall_ikke_ferdig\n" +
						"FROM vr_status",
				resultSetMapping = "ValgnattrapportAntall") })
@SqlResultSetMappings({
		@SqlResultSetMapping(
				name = "ValgnattrapportAntall",
				classes = {
						@ConstructorResult(
								targetClass = ValgnattrapportAntall.class,
								columns = {
										@ColumnResult(name = "antall_rapporter"),
										@ColumnResult(name = "antall_rapporterbare"),
										@ColumnResult(name = "antall_ikke_ferdig")
								}
						)
				}
		)})
/**
 * Metainformasjon om en valgnattrapportering.
 */
public class Valgnattrapport extends VersionedEntity {

    private static final Map<ReportType, String> REPORT_TYPE_TO_QUALIFIER_MAP = new HashMap<>();

	public static final String FORHAAND_FORELOPIG = "ff";
	public static final String FORHAAND_ENDELIG = "fe";
	public static final String VALGTING_FORELOPIG = "vf";
	public static final String VALGTING_ENDELIG = "ve";

	static {
        REPORT_TYPE_TO_QUALIFIER_MAP.put(ReportType.STEMMESKJEMA_FF, FORHAAND_FORELOPIG);
        REPORT_TYPE_TO_QUALIFIER_MAP.put(ReportType.STEMMESKJEMA_FE, FORHAAND_ENDELIG);
        REPORT_TYPE_TO_QUALIFIER_MAP.put(ReportType.STEMMESKJEMA_VF, VALGTING_FORELOPIG);
        REPORT_TYPE_TO_QUALIFIER_MAP.put(ReportType.STEMMESKJEMA_VE, VALGTING_ENDELIG);
    }

    private MvArea mvArea;
	private Municipality municipality;
	private Contest contest;
	private Election election;
	private ReportType reportType;
	private ValgnattrapportStatus status;
	private String jsonContent;

	private boolean readyForReport;

	protected Valgnattrapport() {
		// for hibernate
	}

	public Valgnattrapport(MvArea mvArea, Municipality municipality, Contest contest, Election election, ReportType reportType, ValgnattrapportStatus status,
			String jsonContent, boolean readyForReport) {
		setMvArea(mvArea);
		setMunicipality(municipality);
		setContest(contest);
		setElection(election);
		setReportType(reportType);
		setStatus(status);
		this.jsonContent = jsonContent;
		this.readyForReport = readyForReport;
	}

	public Valgnattrapport(Election election, ReportType reportType) {
		this(null, null, null, election, reportType, ValgnattrapportStatus.NOT_SENT, null, true);
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "mv_area_pk")
	public MvArea getMvArea() {
		return mvArea;
	}

	private void setMvArea(MvArea mvArea) {
		this.mvArea = mvArea;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "municipality_pk")
	public Municipality getMunicipality() {
		return municipality;
	}

	private void setMunicipality(Municipality municipality) {
		this.municipality = municipality;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "contest_pk")
	public Contest getContest() {
		return contest;
	}

	private void setContest(Contest contest) {
		this.contest = contest;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "election_pk")
	public Election getElection() {
		return election;
	}

	private void setElection(Election election) {
		this.election = election;
	}

	@Column(name = "report_type", length = 30)
	@Enumerated(EnumType.STRING)
	public ReportType getReportType() {
		return reportType;
	}

	private void setReportType(ReportType reportType) {
		this.reportType = reportType;
	}

	@Column(name = "status", nullable = false, length = 8)
	@Enumerated(EnumType.STRING)
	public ValgnattrapportStatus getStatus() {
		return status;
	}

	private void setStatus(ValgnattrapportStatus status) {
		this.status = status;
	}

	@Column(name = "json_content")
	public String getJsonContent() {
		return jsonContent;
	}

	public void setJsonContent(String jsonContent) {
		this.jsonContent = jsonContent;
	}

	@Transient
	public boolean isReadyForReport() {
		return readyForReport;
	}

	public void setReadyForReport(boolean readyForReport) {
		this.readyForReport = readyForReport;
	}

	public void oppdaterTilStatusOk() {
		setStatus(OK);
		setAuditTimestamp(new DateTime());
	}

    public String countQualifier() {
        return REPORT_TYPE_TO_QUALIFIER_MAP.containsKey(reportType) ? REPORT_TYPE_TO_QUALIFIER_MAP.get(reportType) : "";
    }

    @Transient
	public boolean isNotSent() {
		return getStatus() != OK;
	}
	
	@Transient
	public boolean isOk() {
		return getStatus() == OK;
	}

	public void maaRapporteresPaaNytt() {
		setStatus(RESENDES);
	}
}
