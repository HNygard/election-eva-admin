package no.valg.eva.admin.configuration.repository;

import no.evote.constants.EvoteConstants;
import no.evote.security.UserData;
import no.valg.eva.admin.backend.common.repository.BaseRepository;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.PollingDistrict;
import no.valg.eva.admin.configuration.domain.model.Voter;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.List;

import static java.lang.String.valueOf;

public class VoterRepository extends BaseRepository {
	private static final String ID = "id";
	private static final String ELECTION_EVENT_PK = "electionEventPk";
	private static final String COUNTY_ID = "countyId";
	private static final String MUNICIPALITY_ID = "municipalityId";

	protected VoterRepository(EntityManager entityManager) {
		super(entityManager);
	}

	public VoterRepository() {
	}

	public Voter voterOfId(final String id, final Long electionEventPk) {
		TypedQuery<Voter> query = getEm().createNamedQuery("Voter.findById", Voter.class).setParameter(ID, id.trim())
				.setParameter(ELECTION_EVENT_PK, electionEventPk);
		List<Voter> resultList = query.getResultList();
		return resultList.isEmpty() ? null : resultList.get(0);
	}

	public List<Voter> votersByName(Long electionEventPk, String nameLine) {
		TypedQuery<Voter> query = getEm().createNamedQuery("Voter.findByName", Voter.class);
		query.setParameter(ELECTION_EVENT_PK, electionEventPk.intValue());
		query.setParameter("nameLine", nameLine);
		return query.getResultList();
	}

	public boolean hasVoters(Long mvAreaPk) {
		Query query = getEm().createNamedQuery("Voter.countByMvArea");
		query.setParameter("mvAreaPk", mvAreaPk);
		return ((Long) query.getSingleResult()) != 0;
	}

	public List<Voter> getElectoralRollForPollingDistrict(PollingDistrict pollingDistrict) {
		TypedQuery<Voter> query = getEm().createNamedQuery("Voter.electoralRollByPollingDistrict", Voter.class);
		query.setParameter("pollingDistrictPk", pollingDistrict.getPk());
		return query.getResultList();
	}

	public void updateLastLineLastPageNumber(final PollingDistrict pollingDistrict, final int lastLineNumber, final int lastPageNumber) {
		getEm()
				.createNamedQuery("Voter.updateLineNumber")
				.setParameter("lastPage", lastPageNumber)
				.setParameter("lastLine", lastLineNumber)
				.setParameter("pollingDistrictPk", pollingDistrict.getPk())
				.executeUpdate();
	}

	public List<Voter> updateVoters(UserData userData, List<Voter> voters) {
		return updateEntities(userData, voters);
	}

	public int genererManntallsnumre(Long electionEventPk) {
		String queryString = "/* NO LOAD BALANCE */select assign_voter_numbers(?)";
		Query query = getEm().createNativeQuery(queryString);
		query.setParameter(1, electionEventPk.intValue());
		return (Integer) query.getSingleResult();
	}

	public Voter create(UserData userData, Voter voter) {
		return super.createEntity(userData, voter);
	}

	public Voter findByPk(Long pk) {
		return super.findEntityByPk(Voter.class, pk);
	}

	public Voter update(UserData userData, Voter voter) {
		return super.updateEntity(userData, voter);
	}

	public List<Voter> findByElectionEventAndId(Long electionEventPk, String id) {
		return getEm()
				.createNamedQuery("Voter.findById", Voter.class)
				.setParameter(ID, id.trim())
				.setParameter(ELECTION_EVENT_PK, electionEventPk)
				.getResultList();
	}

	public List<Voter> findByElectionEventAreaAndId(Long electionEventPk, AreaPath areaPath, String id) {
		return getEm()
				.createNamedQuery("Voter.findByAreaAndId", Voter.class)
				.setParameter(ID, id.trim())
				.setParameter(ELECTION_EVENT_PK, electionEventPk)
				.setParameter(COUNTY_ID, areaPath.getCountyId())
				.setParameter(MUNICIPALITY_ID, areaPath.isMunicipalityLevel() ? areaPath.getMunicipalityId() : "")
				.getResultList();
	}

	public List<Voter> findByVoterNumber(ElectionEvent electionEvent, Long voterNumber) {
		TypedQuery<Voter> query = getEm().createNamedQuery("Voter.findByVoterNumber", Voter.class);
		query.setParameter(ELECTION_EVENT_PK, electionEvent.getPk());
		query.setParameter("number", voterNumber);
		return query.getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<Voter> searchVoter(Voter voter, String countyId, String municipalityId, Integer maxResultSize, boolean approved, Long electionEventPk) {
		return searchVoter(voter, countyId, municipalityId, null, maxResultSize, approved, electionEventPk);
	}

	@SuppressWarnings("unchecked")
	public List<Voter> searchVoter(Voter voter, String countyId, String municipalityId, String boroughId, Integer maxResultSize, boolean approved,
			Long electionEventPk) {
		StringBuilder searchString = new StringBuilder("SELECT * FROM Voter v");
		searchString.append(" where election_event_pk = :electionEventPk");
		if (voter != null && !StringUtils.isEmpty(voter.getNameLine())) {
			searchString.append(" and soundex_tsvector(election_event_pk, name_line");
			searchString.append(") @@ soundex_tsquery(:electionEventPk");
			searchString.append(",:nameLine)");
		}
		if (voter != null && voter.getDateOfBirth() != null && voter.getDateOfBirth().toString().length() >= 1) {
			searchString.append(" AND date_of_birth = :dateOfBirth");
		}
		if (voter != null && !StringUtils.isEmpty(voter.getAddressLine1())) {
			searchString.append(" AND UPPER(address_line1) like :addressLine1 || '%'");
		}
		if (countyId != null && countyId.length() > 0) {
			searchString.append(" AND county_id = :countyId");
		}
		if (municipalityId != null && municipalityId.length() > 0) {
			searchString.append(" AND municipality_id = :municipalityId");
		}
		if (boroughId != null && boroughId.length() > 0) {
			searchString.append(" AND borough_id = :boroughId");
		}
		if (searchString.length() > 0 && approved) {
			searchString.append(" AND approved = :approved ");
		}
		searchString.append(" order by name_line");
		Query query = getEm().createNativeQuery(searchString.toString(), Voter.class);
		includeMaxResultSize(maxResultSize, query);

		query.setParameter(ELECTION_EVENT_PK, electionEventPk.intValue());
		if (voter != null && !StringUtils.isEmpty(voter.getNameLine())) {
			query.setParameter("nameLine", voter.getNameLine().replaceAll(EvoteConstants.REGEXP_REMOVE_SEARCH, ""));
		}
		if (voter != null && !StringUtils.isEmpty(voter.getAddressLine1())) {
			query.setParameter("addressLine1", voter.getAddressLine1().replaceAll(EvoteConstants.REGEXP_REMOVE_SEARCH, "").toUpperCase().trim());
		}
		if (voter != null && voter.getDateOfBirth() != null && voter.getDateOfBirth().toString().length() >= 1) {
			query.setParameter("dateOfBirth", voter.getDateOfBirth().toDate());
		}
		if (countyId != null && countyId.length() > 0) {
			query.setParameter(COUNTY_ID, countyId);
		}
		if (municipalityId != null && municipalityId.length() > 0) {
			query.setParameter(MUNICIPALITY_ID, municipalityId);
		}
		if (boroughId != null && boroughId.length() > 0) {
			query.setParameter("boroughId", boroughId);
		}
		if (approved && searchString.length() > 0) {
			query.setParameter("approved", true);
		}

		return query.getResultList();
	}

	private void includeMaxResultSize(final Integer maxResultSize, final Query query) {
		if (maxResultSize != null && maxResultSize > 0) {
			query.setMaxResults(maxResultSize + 1);
		}
	}

	public boolean areVotersInElectionEvent(Long electionEventPk) {
		Query query = getEm().createNativeQuery("select exists(select * from voter where election_event_pk = ?1)");
		query.setParameter(1, electionEventPk);
		return (Boolean) query.getSingleResult();
	}

	public void deleteVoters(String electionPath, String areaPath) {
		String queryString = "/* NO LOAD BALANCE */select delete_voters(?, ?)";
		Query query = getEm().createNativeQuery(queryString);
		query.setParameter(1, electionPath);
		query.setParameter(2, areaPath);
		query.getSingleResult();
	}

	public void deleteAuditVoters(String electionPath) {
		Query query = getEm().createNativeQuery(
				"DELETE FROM audit.voter v "
						+ "USING mv_election e "
						+ "WHERE e.election_event_pk = v.election_event_pk "
						+ "AND e.election_level = 0 "
						+ "AND text2ltree(e.election_path) <@ text2ltree(?1) "
		);
		query.setParameter(1, electionPath);
		query.executeUpdate();
	}

	public void deleteVotersWithoutMvArea(Long electionEventPk) {
		Query query = getEm().createNamedQuery("Voter.deleteWhereMvAreaIsNull").setParameter(ELECTION_EVENT_PK, electionEventPk);
		query.executeUpdate();
	}

	public boolean existsVoterWithSSN(String proposedSSN, Long electionEventPk) {
		Query query = getEm()
				.createNativeQuery("select 1 from voter where voter_id = ?1 and election_event_pk = ?2")
				.setParameter(1, proposedSSN)
				.setParameter(2, electionEventPk);
		return !query.getResultList().isEmpty();
	}

	@SuppressWarnings("unchecked")
	public List<String[]> getVotersWithoutPollingDistricts(Long electionEventPk) {
		List<String[]> ids = new ArrayList<>();
		String sql = "select distinct country_id, county_id, municipality_id, borough_id, polling_district_id "
				+ "from voter where mv_area_pk is null and election_event_pk = ?1";
		List<Object[]> resultList = getEm().createNativeQuery(sql).setParameter(1, electionEventPk).getResultList();
		for (Object[] objects : resultList) {
			
			ids.add(new String[] { valueOf(objects[0]), valueOf(objects[1]), valueOf(objects[2]), valueOf(objects[3]), valueOf(objects[4]) });
			
		}
		return ids;
	}

	public void deleteVotersByElectionEvent(Long electionEventPk) {
		Query query = getEm().createNativeQuery("delete from voter where election_event_pk = " + electionEventPk);
		query.executeUpdate();
	}

	public void delete(UserData userData, Long voterPk) {
		super.deleteEntity(userData, Voter.class, voterPk);
	}

	public void delete(UserData userData, List<Voter> voters) {
		super.deleteEntities(userData, voters);
	}

	public List<Voter> findVotersByElectionEvent(Long electionEventPk) {
		return super.findEntitiesByElectionEvent(Voter.class, electionEventPk);
	}

	public List<Voter> findByOmraadesti(AreaPath omraadesti) {
		return getEm().createNamedQuery("Voter.findByOmraadesti", Voter.class).setParameter("omraadesti", omraadesti.path()).getResultList();
	}

	public List<Voter> findVotersForValgkortgrunnlag(ElectionEvent valghendelse) {
		return getEm().createNamedQuery("Voter.findForValgkortgrunnlag", Voter.class).setParameter("valghendelsePk", valghendelse.getPk()).getResultList();
	}

	public void flyttVelgere(MvArea fraOmraade, MvArea tilKrets) {
		getEm().createNamedQuery("Voter.flyttVelgereFraOmraade")
			.setParameter(1, tilKrets.getPk())
			.setParameter(2, fraOmraade.getAreaPath())
			.executeUpdate();
	}
}
