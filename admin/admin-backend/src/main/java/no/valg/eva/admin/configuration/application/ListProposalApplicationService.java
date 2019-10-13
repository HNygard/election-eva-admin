package no.valg.eva.admin.configuration.application;

import static no.evote.exception.ErrorCode.ERROR_CODE_0550_UNIQUE_CONSTRAINT_VIOLATION;
import static no.valg.eva.admin.common.rbac.Accesses.Konfigurasjon_Grunnlagsdata_Redigere;
import static no.valg.eva.admin.common.rbac.SecurityType.READ;
import static no.valg.eva.admin.common.rbac.SecurityType.WRITE;

import java.util.List;
import java.util.stream.Collectors;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.inject.Inject;

import no.evote.exception.EvoteException;
import no.evote.security.UserData;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.AuditLog;
import no.valg.eva.admin.common.auditlog.auditevents.config.ListProposalConfigAuditEvent;
import no.valg.eva.admin.common.configuration.model.local.ListProposalConfig;
import no.valg.eva.admin.common.configuration.service.ListProposalService;
import no.valg.eva.admin.common.rbac.Security;
import no.valg.eva.admin.configuration.domain.model.Borough;
import no.valg.eva.admin.configuration.domain.model.Contest;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.repository.ContestRepository;
import no.valg.eva.admin.configuration.repository.MunicipalityRepository;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;

@Stateless(name = "ListProposalService")


@Default
@Remote(ListProposalService.class)
public class ListProposalApplicationService implements ListProposalService {

	// Injected
	@Inject
	private ContestRepository contestRepository;
	@Inject
	private MunicipalityRepository municipalityRepository;
	@Inject
	private ContestMapper contestMapper;

	public ListProposalApplicationService() {

	}

	public ListProposalApplicationService(ContestRepository contestRepository, MunicipalityRepository municipalityRepository, ContestMapper contestMapper) {
		this.contestRepository = contestRepository;
		this.municipalityRepository = municipalityRepository;
		this.contestMapper = contestMapper;
	}

	@Override
	@Security(accesses = Konfigurasjon_Grunnlagsdata_Redigere, type = READ)
	public ListProposalConfig findByArea(UserData userData, AreaPath areaPath) {
		ListProposalConfig result = findByAreaAndMap(userData, areaPath);
		if (result == null) {
			return null;
		}
		return handleChildren(userData, result);
	}

	private ListProposalConfig findByAreaAndMap(UserData userData, AreaPath areaPath) {
		List<Contest> contests = contestRepository.findByElectionEventAndArea(userData.getElectionEventPk(), areaPath);
		if (contests.isEmpty()) {
			return null;
		} else if (contests.size() > 1) {
			throw new EvoteException(ERROR_CODE_0550_UNIQUE_CONSTRAINT_VIOLATION);
		}
		Contest contest = contests.get(0);
		return contestMapper.toListProposalConfig(areaPath, contest);
	}

	private ListProposalConfig handleChildren(UserData userData, ListProposalConfig result) {
		if (result.getAreaPath().isMunicipalityLevel()) {
			Municipality municipality = getMunicipality(userData, result.getAreaPath());
			for (Borough borough : municipality.getBoroughs()) {
				ListProposalConfig child = findByAreaAndMap(userData, borough.areaPath());
				if (child != null) {
					result.getChildren().add(child);
				}
			}
			result.setChildren(result.getChildren().stream().sorted(ListProposalConfig.areaComparator()).collect(Collectors.toList()));
		}
		result.getContestListProposalData().recalculate();
		return result;
	}

	@Override
	@Security(accesses = Konfigurasjon_Grunnlagsdata_Redigere, type = WRITE)
	@AuditLog(eventClass = ListProposalConfigAuditEvent.class, eventType = AuditEventTypes.Save)
	public ListProposalConfig save(UserData userData, ListProposalConfig listProposal, boolean saveChildren) {
		doSave(userData, listProposal);

		if (saveChildren) {
			for (ListProposalConfig child : listProposal.getChildren()) {
				doSave(userData, child);
			}
		}

		return findByArea(userData, listProposal.getAreaPath());
	}

	private void doSave(UserData userData, ListProposalConfig listProposal) {
		Contest dbContest = contestRepository.findByPk(listProposal.getContestPk());
		dbContest.checkVersion(listProposal);
		contestMapper.updateEntityFromListProposalData(dbContest, listProposal.getContestListProposalData());
		contestRepository.update(userData, dbContest);
	}

	private Municipality getMunicipality(UserData userData, AreaPath areaPath) {
		return municipalityRepository.municipalityByElectionEventAndId(userData.getElectionEventPk(), areaPath.getMunicipalityId());
	}
}
