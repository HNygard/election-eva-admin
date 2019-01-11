package no.valg.eva.admin.configuration.application.party;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import no.evote.security.UserData;
import no.valg.eva.admin.backend.common.repository.LocaleTextRepository;
import no.valg.eva.admin.common.Area;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.configuration.model.party.Parti;
import no.valg.eva.admin.common.configuration.model.party.Partikategori;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.LocaleText;
import no.valg.eva.admin.configuration.domain.model.MvAreaDigest;
import no.valg.eva.admin.configuration.domain.model.Party;
import no.valg.eva.admin.configuration.domain.model.PartyContestArea;
import no.valg.eva.admin.configuration.repository.MvAreaRepository;
import no.valg.eva.admin.configuration.repository.party.PartyCategoryRepository;

import org.apache.log4j.Logger;

public class PartyMapper {

	private static final Logger LOG = Logger.getLogger(PartyMapper.class);
	private PartyCategoryRepository partyCategoryRepository;
	private LocaleTextRepository localeTextRepository;
	private MvAreaRepository mvAreaRepository;

	@Inject
	public PartyMapper(PartyCategoryRepository partyCategoryRepository, LocaleTextRepository localeTextRepository, MvAreaRepository mvAreaRepository) {
		this.partyCategoryRepository = partyCategoryRepository;
		this.localeTextRepository = localeTextRepository;
		this.mvAreaRepository = mvAreaRepository;
	}

	public Party toParty(Parti parti, ElectionEvent electionEvent) {
		Party party = new Party();
		party.setElectionEvent(electionEvent);
		party.setPk(parti.getPartyPk());
		updateParty(party, parti);
		return party;
	}

	public void updateParty(Party party, Parti parti) {
		party.setId(parti.getId());
		party.setApproved(parti.isGodkjent());
		party.setForenkletBehandling(parti.isForenkletBehandling());
		party.setPartyCategory(partyCategoryRepository.findById(parti.getPartikategori().getId()));
		party.setShortCode(parti.getPartikode());
		party.setTranslatedPartyName(parti.getOversattNavn());
		lagPartyContestAreas(party, parti.getOmrader());
	}

	private void lagPartyContestAreas(Party party, List<Area> omrader) {
		Set<PartyContestArea> partyContestAreas = party.getPartyContestAreas();
		if (omrader.isEmpty()  || !party.skalHaOmradetilknytning()) {
			partyContestAreas.clear();
		}
		omrader.forEach(omrade -> {
			String countyId = omrade.getAreaPath().isCountyLevel() ? omrade.getAreaPath().getCountyId() : null;
			String municipalityId = omrade.getAreaPath().isMunicipalityLevel() ? omrade.getAreaPath().getMunicipalityId() : null;
			String boroughId = omrade.getAreaPath().isBoroughLevel() ? omrade.getAreaPath().getBoroughId() : null;
			partyContestAreas.add(new PartyContestArea(party, countyId, municipalityId, boroughId));
		});
	}

	public Parti toParti(UserData userData, Party party) {
		Parti parti = new Parti(Partikategori.fromId(party.getPartyCategory().getId()), party.getId());
		parti.setForenkletBehandling(party.isForenkletBehandling());
		parti.setGodkjent(party.isApproved());
		parti.setPartikode(party.getShortCode());
		parti.setPartyPk(party.getPk());

		Set<PartyContestArea> partyContestAreas = party.partyContestAreasForPartyCategory();
		parti.setOmrader(createAreas(userData, partyContestAreas));

		LocaleText localeText = localeTextRepository.findByElectionEventLocaleAndTextId(userData.getElectionEventPk(), userData.getLocale().getPk(),
				party.getName());
		parti.setOversattNavn(localeText != null ? localeText.getLocaleText() : "");

		return parti;
	}

	private List<Area> createAreas(UserData userData, Set<PartyContestArea> partyContestAreas) {
		List<Area> areas = new ArrayList<>();

		if (partyContestAreas == null) {
			return areas;
		}

		for (PartyContestArea partyContestArea : partyContestAreas) {

			String electionEventId = userData.getElectionEventId();

			AreaPath areaPath = partyContestArea.areaPath(electionEventId);
			MvAreaDigest mvAreaDigest = mvAreaRepository.findSingleDigestByPath(areaPath);

			if (mvAreaDigest == null) {
				LOG.warn("Fant ikke MvAreaDigest entitet for " + partyContestArea);
				continue;
			}

			String areaName = getAreaName(areaPath, mvAreaDigest);
			
			areas.add(new Area(areaPath, areaName));

		}

		return areas;
	}

	private String getAreaName(AreaPath areaPath, MvAreaDigest mvAreaDigest) {
		StringBuilder builder = new StringBuilder();

		builder.append(mvAreaDigest.getCountyName());

		if (areaPath.isMunicipalityLevel() || areaPath.isBoroughLevel()) {
			builder.append(" / ").append(mvAreaDigest.getMunicipalityName());
		}

		if (areaPath.isBoroughLevel()) {
			builder.append(" / ").append(mvAreaDigest.getBoroughName());
		}
		return builder.toString();
	}
}
