package no.valg.eva.admin.configuration.repository;

import java.math.BigInteger;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import no.evote.model.views.ContestRelArea;
import no.valg.eva.admin.backend.common.repository.BaseRepository;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;

public class ContestRelAreaRepository extends BaseRepository {
	public ContestRelAreaRepository() {
	}

	protected ContestRelAreaRepository(final EntityManager entityManager) {
		super(entityManager);
	}

	public List<ContestRelArea> findAllAllowed(final MvElection mvElection, final MvArea mvArea) {
		final TypedQuery<ContestRelArea> query = getEm().createNamedQuery("ContestRelArea.findAllAllowed", ContestRelArea.class)
				.setParameter(1, mvElection.getPk()).setParameter(2, mvArea.getPk());
		return query.getResultList();
	}

	public ContestRelArea findUnique(Long mvElectionPk, Long mvAreaPk) {
		TypedQuery<ContestRelArea> query = getEm().createNamedQuery("ContestRelArea.findUnique", ContestRelArea.class)
				.setParameter("mvElectionPk", mvElectionPk).setParameter("mvAreaPk", mvAreaPk);
		return query.getSingleResult();
	}

	/**
	 * Count all polling district that have counted on a lower level
	 */
	public int countPollingDistrictBelow(Long mvElectionPk, Long mvAreaPk, String vccID) {
		// @formatter:off
		String sqlQuery = "select count(*) from ( "
				+ "select ru.name_line, ap.area_path, ap.polling_district_name, vcc.vote_count_category_id, count_qualifier_id, central_preliminary_count, "
				+ "polling_district_count, manual_count, approved_ballots, vote_count_status_id, "
				+ "dense_rank() over (order by a.area_level) as level from contest_rel_area ca "
				+ "join mv_election e on (text2ltree(e.election_path) @> text2ltree(ca.election_path)) "
				+ "join mv_area a on (text2ltree(a.area_path) <@ text2ltree(ca.area_path) and a.area_level <= 5) "
				+ "join reporting_unit ru on (ru.mv_election_pk = e.mv_election_pk  and ru.mv_area_pk = a.mv_area_pk) "
				+ "join mv_area ap on (text2ltree(ap.area_path) <@ text2ltree(a.area_path) and ap.area_level = 5) "
				+ "join polling_district p on (p.polling_district_pk = ap.polling_district_pk) "
				+ "join report_count_category rcc on (rcc.election_group_pk = ca.election_group_pk and rcc.municipality_pk = ap.municipality_pk) "
				+ "join vote_count_category vcc on (vcc.vote_count_category_pk = rcc.vote_count_category_pk) "
				+ "left join contest_report cr on (cr.reporting_unit_pk = ru.reporting_unit_pk  and cr.contest_pk = ca.contest_pk) "
				+ "left join vote_count vc on (vc.contest_report_pk = cr.contest_report_pk  and vc.polling_district_pk = ap.polling_district_pk "
				+ "		and vc.vote_count_category_pk = rcc.vote_count_category_pk) "
				+ "left join vote_count_status vcs on (vcs.vote_count_status_pk = vc.vote_count_status_pk) "
				+ "left join count_qualifier cq on (cq.count_qualifier_pk = vc.count_qualifier_pk) "
				+ "where ca.mv_election_pk = ?1 and ca.mv_area_pk = ?2 ) as rus "
				+ "where level = 2 and vote_count_category_id = ?3 and count_qualifier_id is not null and vote_count_status_id = 2;";
		// @formatter:on

		Query query = getEm().createNativeQuery(sqlQuery);
		query.setParameter(1, mvElectionPk);
		query.setParameter(2, mvAreaPk);
		
		query.setParameter(3, vccID);
		
		return ((BigInteger) query.getSingleResult()).intValue();
	}

	/**
	 * Count all polling district that have counted on a lower level
	 */
	public int countCountsOnPollingDistrict(Long mvElectionPk, Long mvAreaPk, String vccID, Long pollingDistrictPk) {
		// @formatter:off
		String sqlQuery = "select count(*) from ( select "
				+ "ap.polling_district_pk, "
				+ "vcc.vote_count_category_id, "
				+ "vote_count_status_id, "
				+ "dense_rank() over (order by a.area_level) as level "
				+ "from contest_rel_area ca "
				+ "join mv_election e on (text2ltree(e.election_path) @> text2ltree(ca.election_path)) "
				+ "join mv_area a on (text2ltree(a.area_path) <@ text2ltree(ca.area_path) and a.area_level <= 5) "
				+ "join reporting_unit ru on (ru.mv_election_pk = e.mv_election_pk  and ru.mv_area_pk = a.mv_area_pk) "
				+ "join mv_area ap on (text2ltree(ap.area_path) <@ text2ltree(a.area_path) and ap.area_level = 5) "
				+ "join polling_district p on (p.polling_district_pk = ap.polling_district_pk) "
				+ "join report_count_category rcc on (rcc.election_group_pk = ca.election_group_pk and rcc.municipality_pk = ap.municipality_pk) "
				+ "left join vote_count_category vcc on (vcc.vote_count_category_pk = rcc.vote_count_category_pk) "
				+ "left join contest_report cr on (cr.reporting_unit_pk = ru.reporting_unit_pk and cr.contest_pk = ca.contest_pk) "
				+ "left join vote_count vc on (vc.contest_report_pk = cr.contest_report_pk and vc.polling_district_pk = ap.polling_district_pk "
				+ "	and vc.vote_count_category_pk = rcc.vote_count_category_pk) "
				+ "left join vote_count_status vcs on (vcs.vote_count_status_pk = vc.vote_count_status_pk) "
				+ "where ca.mv_election_pk = ?1  and ca.mv_area_pk = ?2 ) as rus "
				+ "where level = 2 and vote_count_category_id = ?3 and vote_count_status_id = 2 and polling_district_pk = ?4 ";
		// @formatter:on

		Query query = getEm().createNativeQuery(sqlQuery);
		query.setParameter(1, mvElectionPk);
		query.setParameter(2, mvAreaPk);
		
		query.setParameter(3, vccID);
		query.setParameter(4, pollingDistrictPk);
		
		return ((BigInteger) query.getSingleResult()).intValue();
	}
}
