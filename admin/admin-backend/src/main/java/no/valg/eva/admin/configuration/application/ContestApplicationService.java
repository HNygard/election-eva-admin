package no.valg.eva.admin.configuration.application;

import static no.valg.eva.admin.common.rbac.Accesses.Konfigurasjon_Valg_Valgdistrikt;
import static no.valg.eva.admin.common.rbac.SecurityType.READ;
import static no.valg.eva.admin.common.rbac.SecurityType.WRITE;

import javax.annotation.Resource;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.transaction.TransactionSynchronizationRegistry;

import no.evote.security.UserData;
import no.evote.service.configuration.ContestServiceBean;
import no.valg.eva.admin.util.StringUtil;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.AuditLog;
import no.valg.eva.admin.common.auditlog.auditevents.ContestAuditEvent;
import no.valg.eva.admin.common.configuration.model.election.Contest;
import no.valg.eva.admin.common.configuration.service.ContestService;
import no.valg.eva.admin.common.rbac.Security;
import no.valg.eva.admin.configuration.domain.model.Election;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.repository.ContestRepository;
import no.valg.eva.admin.configuration.repository.ElectionRepository;
import no.valg.eva.admin.configuration.repository.MvAreaRepository;

@Stateless(name = "ContestService")
@Remote(ContestService.class)
public class ContestApplicationService implements ContestService {

	private static final int ID_LENGTH = 6;

	@Resource
	private TransactionSynchronizationRegistry registry;
	private ContestRepository contestRepository;
	private ContestMapper contestMapper;
	private ContestServiceBean contestService;
	private MvAreaRepository mvAreaRepository;
	private ElectionRepository electionRepository;

	@Inject
	public ContestApplicationService(ContestRepository contestRepository, ContestMapper contestMapper, ContestServiceBean contestService,
			MvAreaRepository mvAreaRepository, ElectionRepository electionRepository) {
		this.contestRepository = contestRepository;
		this.contestMapper = contestMapper;
		this.contestService = contestService;
		this.mvAreaRepository = mvAreaRepository;
		this.electionRepository = electionRepository;
	}

	@Override
	@Security(accesses = Konfigurasjon_Valg_Valgdistrikt, type = READ)
	public Contest get(UserData userData, ElectionPath contestPath) {
		return contestMapper.toCommon(contestRepository.findSingleByPath(contestPath));
	}

	@Override
	@Security(accesses = Konfigurasjon_Valg_Valgdistrikt, type = WRITE)
	@AuditLog(eventClass = ContestAuditEvent.class, eventType = AuditEventTypes.Save)
	public Contest save(UserData userData, Contest contest) {
		no.valg.eva.admin.configuration.domain.model.Contest saved;
		if (contest.getPk() == null) {
			saved = create(userData, contest);
		} else {
			saved = update(userData, contest);
		}
		return contestMapper.toCommon(saved);
	}

	@Override
	@Security(accesses = Konfigurasjon_Valg_Valgdistrikt, type = WRITE)
	@AuditLog(eventClass = ContestAuditEvent.class, eventType = AuditEventTypes.Delete)
	public void delete(UserData userData, ElectionPath contestPath) {
		no.valg.eva.admin.configuration.domain.model.Contest dbContest = contestRepository.findSingleByPath(contestPath);
		contestService.delete(userData, dbContest.getPk(), registry);
	}

	private no.valg.eva.admin.configuration.domain.model.Contest create(UserData userData, Contest contest) {
		if (contest.getContestAreas().size() != 1) {
			throw new IllegalArgumentException("Create Contest requires one contest area");
		}
		Election election = electionRepository.findSingleByPath(contest.getParentElectionPath());
		MvArea mvArea = mvAreaRepository.findSingleByPath(userData.getElectionEventId(), contest.getContestAreas().get(0));

		no.valg.eva.admin.configuration.domain.model.Contest dbContest = new no.valg.eva.admin.configuration.domain.model.Contest();
		dbContest.setElection(election);

		contest.setId(StringUtil.prefixString(mvArea.getAreaId(), ID_LENGTH, '0'));
		contest.setName(mvArea.getAreaName());
		contestMapper.updateEntity(dbContest, contest);
		contestService.create(userData, dbContest, mvArea, !election.isSingleArea(), false);
		return dbContest;
	}

	private no.valg.eva.admin.configuration.domain.model.Contest update(UserData userData, Contest contest) {
		no.valg.eva.admin.configuration.domain.model.Contest dbContest = contestRepository.findSingleByPath(contest.getElectionPath());
		dbContest.checkVersion(contest);
		contestMapper.updateEntity(dbContest, contest);
		contestService.update(userData, dbContest, registry);
		return dbContest;
	}
}
