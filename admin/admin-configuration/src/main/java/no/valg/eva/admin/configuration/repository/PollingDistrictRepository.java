package no.valg.eva.admin.configuration.repository;

import no.evote.constants.AreaLevelEnum;
import no.evote.constants.EvoteConstants;
import no.evote.dto.ConfigurationDto;
import no.evote.security.UserData;
import no.valg.eva.admin.backend.common.repository.BaseRepository;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.configuration.domain.model.Borough;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.PollingDistrict;
import org.hibernate.Session;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.math.BigInteger;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

@Default
@ApplicationScoped
public class PollingDistrictRepository extends BaseRepository {
    private static final String ID = "id";
    private static final String MUNICIPALITY_PK_PARAMETER = "municipalityPk";
    private static final String PARAM_BOROUGH_PK = "boroughPk";

    public PollingDistrict create(UserData userData, PollingDistrict pollingDistrict) {
        return createEntity(userData, pollingDistrict);
    }

    public PollingDistrict update(UserData userData, PollingDistrict pollingDistrict) {
        return updateEntity(userData, pollingDistrict);
    }

    public void delete(UserData userData, Long pk) {
        deleteEntity(userData, PollingDistrict.class, pk);
    }

    public PollingDistrict findByPk(Long pk) {
        return findEntityByPk(PollingDistrict.class, pk);
    }

    public PollingDistrict findPollingDistrictById(Long boroughPk, String id) {
        TypedQuery<PollingDistrict> query = getEm().createNamedQuery("PollingDistrict.findById", PollingDistrict.class);
        query.setParameter(PARAM_BOROUGH_PK, boroughPk);
        query.setParameter(ID, id);

        final List<PollingDistrict> result = query.getResultList();
        if (result != null && !result.isEmpty()) {
            return fixChildPollingDistrictValues(result).get(0);
        } else {
            return null;
        }
    }

    private List<PollingDistrict> fixChildPollingDistrictValues(final List<PollingDistrict> pollingDistricts) {
        for (PollingDistrict pd : pollingDistricts) {
            fixChildPollingDistrictValue(pd);
        }
        return pollingDistricts;
    }

    private void fixChildPollingDistrictValue(final PollingDistrict pd) {
        if (pd.getPollingDistrict() != null) {
            pd.setChildPollingDistrict(true);
        }
    }

    public List<PollingDistrict> findPollingDistrictsForParent(PollingDistrict pollingDistrictParent) {
        TypedQuery<PollingDistrict> query = getEm().createNamedQuery("PollingDistrict.findPollingDistrictsForParent", PollingDistrict.class);
        query.setParameter("parentPollingDistrictPk", pollingDistrictParent.getPk());
        List<PollingDistrict> pollingDistrictList = query.getResultList();
        for (PollingDistrict pollingDistrict : pollingDistrictList) {
            pollingDistrict.setChildPollingDistrict(true);
        }
        return pollingDistrictList;
    }

    public Boolean municipalityProxyExists(Long municipalityPk) {
        TypedQuery<PollingDistrict> query = getEm().createNamedQuery("PollingDistrict.findPollingDistrictByMunicipalityProxy", PollingDistrict.class);
        query.setParameter(MUNICIPALITY_PK_PARAMETER, municipalityPk);
        return !query.getResultList().isEmpty();
    }

    public PollingDistrict findMunicipalityProxy(Long municipalityPk) {
        TypedQuery<PollingDistrict> query = getEm().createNamedQuery("PollingDistrict.findPollingDistrictByMunicipalityProxy", PollingDistrict.class);
        query.setParameter(MUNICIPALITY_PK_PARAMETER, municipalityPk);
        return query.getSingleResult();
    }

    public List<PollingDistrict> getPollingDistrictsByMunicipality(Long municipalityPk) {
        TypedQuery<PollingDistrict> query = getEm().createNamedQuery("PollingDistrict.findByMunicipality", PollingDistrict.class);
        query.setParameter(MUNICIPALITY_PK_PARAMETER, municipalityPk);
        return query.getResultList();
    }

    public List<ConfigurationDto> getPollingDistrictsWithoutVoters(final Long electionEventPk) {
        final List<ConfigurationDto> districts = new ArrayList<>();

        Session session = (Session) getEm().getDelegate();
        session.doWork(con -> {
            String sql = "SELECT " + "  mvpd.municipality_id, " + "  mvpd.borough_id, " + "  mvpd.polling_district_id, "
                    + "  mvpd.municipality_name, " + "  mvpd.borough_name, " + "  mvpd.polling_district_name " + "FROM mv_area mvpd "
                    + "JOIN polling_district pd " + "  ON pd.polling_district_pk = mvpd.polling_district_pk " + "  AND pd.municipality = FALSE "
                    + "WHERE mvpd.election_event_pk = ? " + "AND mvpd.area_level = 5 " + "AND mvpd.parent_polling_district = FALSE "
                    + "AND NOT exists ( " + "  SELECT 1 " + "  FROM voter v " + "  WHERE v.mv_area_pk = mvpd.mv_area_pk " + ") " + "ORDER BY "
                    + "  mvpd.municipality_id, " + "  mvpd.borough_id, " + "  mvpd.polling_district_id";

            try (PreparedStatement stmt = con.prepareStatement(sql)) {
                stmt.setLong(1, electionEventPk);
                ResultSet res = stmt.executeQuery();
                while (res.next()) {

                    districts.add(new ConfigurationDto(res.getString("municipality_id") + "." + res.getString("borough_id")
                            + "." + res.getString("polling_district_id"), res.getString("municipality_name") + ", "
                            + res.getString("borough_name") + ", " + res.getString("polling_district_name")));

                }
            }
        });
        return districts;
    }

    public List<PollingDistrict> findPollingDistrictsForBorough(Borough borough) {
        TypedQuery<PollingDistrict> query = getEm().createNamedQuery("PollingDistrict.findByBorough", PollingDistrict.class);
        query.setParameter(PARAM_BOROUGH_PK, borough.getPk());
        return query.getResultList();
    }

    public List<PollingDistrictAreaId> findVotersWithoutPollingDistricts(final Long electionEventPk, final Municipality municipality) {
        final List<PollingDistrictAreaId> ids = new ArrayList<>();
        Session session = (Session) getEm().getDelegate();
        session.doWork(con -> {
            String sql = "SELECT DISTINCT country_id, county_id, municipality_id, borough_id, polling_district_id "
                    + "FROM voter WHERE mv_area_pk IS NULL AND municipality_id = ? AND election_event_pk = ?";
            try (PreparedStatement stmt = con.prepareStatement(sql)) {
                stmt.setString(1, municipality.getId());
                stmt.setLong(2, electionEventPk);
                ResultSet res = stmt.executeQuery();

                while (res.next()) {

                    PollingDistrictAreaId areaId = PollingDistrictAreaId.builder()
                            .countryId(res.getString("country_id"))
                            .countyId(res.getString("county_id"))
                            .municipalityId(res.getString("municipality_id"))
                            .boroughId(res.getString("borough_id"))
                            .pollingDistrictId(res.getString("polling_district_id"))
                            .build();

                    ids.add(areaId);
                }

            }
        });

        return ids;
    }

    public int countProtocolPollingDistrictsByMunicipality(Long municipalityPk) {
        // @formatter:off
        String sqlQuery = "SELECT COUNT(p.*) "
                + "FROM polling_district p "
                + "JOIN borough b ON b.borough_pk = p.borough_pk "
                + "JOIN municipality m ON (m.municipality_pk = b.municipality_pk) "
                + "WHERE b.municipality_pk = ?1 AND NOT p.municipality AND NOT p.parent_polling_district";
        // @formatter:on
        Query query = getEm().createNativeQuery(sqlQuery);
        query.setParameter(1, municipalityPk);

        return ((BigInteger) query.getSingleResult()).intValue();
    }

    public int countReadyProtocolPollingDistrictsByContestMunicipality(Long contestPk, Long municipalityPk) {
        // @formatter:off
        String sqlQuery = "SELECT count(*) "
                + "FROM polling_district p "
                + "JOIN borough b ON b.borough_pk = p.borough_pk "
                + "JOIN municipality m ON (m.municipality_pk = b.municipality_pk) "
                + "JOIN contest_report cr ON cr.contest_pk = ?1 "
                + "JOIN vote_count vc ON (vc.contest_report_pk = cr.contest_report_pk AND vc.polling_district_pk = p.polling_district_pk) "
                + "JOIN vote_count_status vcs ON (vcs.vote_count_status_pk = vc.vote_count_status_pk AND vote_count_status_id = 2) "
                + "JOIN count_qualifier cq ON (cq.count_qualifier_pk = vc.count_qualifier_pk AND cq.count_qualifier_id = 'P' ) "
                + "JOIN vote_count_category vcc ON (vcc.vote_count_category_pk = vc.vote_count_category_pk AND vcc.vote_count_category_id = 'VO') "
                + "WHERE b.municipality_pk = ?2";
        // @formatter:on
        Query query = getEm().createNativeQuery(sqlQuery);
        query.setParameter(1, contestPk);
        query.setParameter(2, municipalityPk);
        return ((BigInteger) query.getSingleResult()).intValue();
    }

    public int countChildPollingDistricts(Long pollingDistrictPk) {
        // @formatter:off
        String sqlQuery = "	SELECT count(*) "
                + "FROM polling_district pp "
                + "JOIN polling_district ptot ON (pp.polling_district_pk = ptot.parent_polling_district_pk) "
                + "WHERE pp.polling_district_pk = ?1";

        // @formatter:on
        Query query = getEm().createNativeQuery(sqlQuery);
        query.setParameter(1, pollingDistrictPk);
        return ((BigInteger) query.getSingleResult()).intValue();
    }

    public int countReadyChildPollingDistricts(Long contestPk, Long pollingDistrictPk) {

        // @formatter:off
        String sqlQuery = "SELECT count(*) "
                + "FROM polling_district pp "
                + "JOIN polling_district p ON (pp.polling_district_pk = p.parent_polling_district_pk) "
                + "JOIN contest_report cr ON cr.contest_pk = ?1 "
                + "JOIN vote_count vc ON (vc.contest_report_pk = cr.contest_report_pk AND vc.polling_district_pk = p.polling_district_pk) "
                + "JOIN vote_count_status vcs ON (vcs.vote_count_status_pk = vc.vote_count_status_pk AND vote_count_status_id = 2) "
                + "JOIN count_qualifier cq ON (cq.count_qualifier_pk = vc.count_qualifier_pk AND cq.count_qualifier_id = 'P' ) "
                + "JOIN vote_count_category vcc ON (vcc.vote_count_category_pk = vc.vote_count_category_pk AND vcc.vote_count_category_id = 'VO') "
                + "WHERE pp.polling_district_pk = ?2";

        // @formatter:on
        Query query = getEm().createNativeQuery(sqlQuery);
        query.setParameter(1, contestPk);
        query.setParameter(2, pollingDistrictPk);
        return ((BigInteger) query.getSingleResult()).intValue();
    }

    @SuppressWarnings(EvoteConstants.WARNING_UNCHECKED)
    public List<PollingDistrict> findPollingDistrictsUsingPollingStation(final Long electionEventPk) {
        Query query = getEm().createNamedQuery("PollingDistrict.findIsUsingPollingStation").setParameter("electionEventPk", electionEventPk);
        return query.getResultList();
    }

    public void deleteParentPollingDistrict(UserData userData, PollingDistrict pollingDistrict) {
        // Removes reference to parent from children
        final List<PollingDistrict> pollingDistrictsForParentList = findPollingDistrictsForParent(pollingDistrict);
        if (pollingDistrictsForParentList != null) {
            for (PollingDistrict childPollingDistrict : pollingDistrictsForParentList) {
                childPollingDistrict.setPollingDistrict(null);
            }
        }

        // Deletes parent
        delete(userData, pollingDistrict.getPk());
    }

    public List<PollingDistrict> findByPathAndLevel(AreaPath areaPath, AreaLevelEnum areaLevel) {
        TypedQuery<PollingDistrict> query = getEm().createNamedQuery("PollingDistrict.findByPathAndLevel", PollingDistrict.class);
        query.setParameter(1, areaPath);
        query.setParameter(2, areaLevel);
        return query.getResultList();
    }
}
