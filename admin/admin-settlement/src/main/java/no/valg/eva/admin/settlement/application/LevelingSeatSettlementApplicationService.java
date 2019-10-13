package no.valg.eva.admin.settlement.application;

import static no.evote.constants.ElectionLevelEnum.ELECTION;
import static no.valg.eva.admin.common.rbac.Accesses.Beskyttet_Slett_Utjevningsmandater;
import static no.valg.eva.admin.common.rbac.Accesses.Opptelling_Utjevningsmandater_Gjennomføre;
import static no.valg.eva.admin.common.rbac.Accesses.Opptelling_Utjevningsmandater_Se;
import static no.valg.eva.admin.common.rbac.SecurityType.READ;
import static no.valg.eva.admin.common.rbac.SecurityType.WRITE;

import java.util.List;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.inject.Inject;

import no.evote.exception.EvoteException;
import no.evote.security.UserData;
import no.valg.eva.admin.common.ElectionPath;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.AuditLog;
import no.valg.eva.admin.common.auditlog.auditevents.DeleteLevelingSeatSettlementAuditEvent;
import no.valg.eva.admin.common.auditlog.auditevents.DistributeLevelingSeatsAuditEvent;
import no.valg.eva.admin.common.rbac.Security;
import no.valg.eva.admin.common.settlement.model.LevelingSeatSettlementSummary;
import no.valg.eva.admin.common.settlement.service.LevelingSeatSettlementService;
import no.valg.eva.admin.configuration.domain.model.Election;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.configuration.repository.MvElectionRepository;
import no.valg.eva.admin.settlement.application.mapper.LevelingSeatMapper;
import no.valg.eva.admin.settlement.domain.LevelingSeatSettlementDomainService;
import no.valg.eva.admin.settlement.repository.LevelingSeatSettlementRepository;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;

@Stateless(name = "LevelingSeatSettlementService")
@Default
@Remote(LevelingSeatSettlementService.class)
public class LevelingSeatSettlementApplicationService implements LevelingSeatSettlementService {

	@Inject
	private LevelingSeatSettlementDomainService levelingSeatSettlementDomainService;
	@Inject
	private LevelingSeatSettlementRepository levelingSeatSettlementRepository;
	@Inject
	private MvElectionRepository mvElectionRepository;

	public LevelingSeatSettlementApplicationService(LevelingSeatSettlementDomainService levelingSeatSettlementDomainService,
													LevelingSeatSettlementRepository levelingSeatSettlementRepository, MvElectionRepository mvElectionRepository) {
		this.levelingSeatSettlementDomainService = levelingSeatSettlementDomainService;
		this.levelingSeatSettlementRepository = levelingSeatSettlementRepository;
		this.mvElectionRepository = mvElectionRepository;
	}
	public LevelingSeatSettlementApplicationService() {

	}

	@Override
	@Security(accesses = Opptelling_Utjevningsmandater_Gjennomføre, type = WRITE)
	@AuditLog(eventClass = DistributeLevelingSeatsAuditEvent.class, eventType = AuditEventTypes.Create)
	public LevelingSeatSettlementSummary distributeLevelingSeats(UserData userData) {
		Election electionWithLevelingSeats = electionWithLevelingSeats(userData);
		if (!levelingSeatSettlementRepository.areAllSettlementsInElectionFinished(electionWithLevelingSeats)) {
			throw new EvoteException("@leveling_seats.error.settlement_not_done");
		}
		levelingSeatSettlementDomainService.distributeLevelingSeats(userData, electionWithLevelingSeats);
		return levelingSeatSettlementSummary(electionWithLevelingSeats);
	}

	@Override
	@Security(accesses = Opptelling_Utjevningsmandater_Se, type = READ)
	public LevelingSeatSettlementSummary levelingSeatSettlementSummary(UserData userData) {
		return levelingSeatSettlementSummary(electionWithLevelingSeats(userData));
	}

	@Override
	@Security(accesses = Beskyttet_Slett_Utjevningsmandater, type = WRITE)
	@AuditLog(eventClass = DeleteLevelingSeatSettlementAuditEvent.class, eventType = AuditEventTypes.Delete)
	public void deleteLevelingSeatSettlement(UserData userData) {
		levelingSeatSettlementRepository.deleteLevelingSeatSettlement(electionWithLevelingSeats(userData));
	}

	private Election electionWithLevelingSeats(UserData userData) {
		ElectionPath electionEventPath = userData.getOperatorElectionPath().toElectionEventPath();
		return mvElectionRepository.findByPathAndLevel(electionEventPath, ELECTION)
				.stream()
				.map(MvElection::getElection)
				.filter(Election::hasLevelingSeats)
				.findFirst()
				.orElseThrow(this::electionWithLevelingSeatsMissing);
	}

	private EvoteException electionWithLevelingSeatsMissing() {
		return new EvoteException("@leveling_seats.error.missing_election");
	}

	private LevelingSeatSettlementSummary levelingSeatSettlementSummary(Election election) {
		List<no.valg.eva.admin.settlement.domain.model.LevelingSeat> levelingSeats = levelingSeatSettlementRepository.findLevelingSeatsByElection(election);
		if (levelingSeats.isEmpty()) {
			if (levelingSeatSettlementRepository.areAllSettlementsInElectionFinished(election)) {
				return new LevelingSeatSettlementSummary(LevelingSeatSettlementSummary.Status.READY);
			}
			return new LevelingSeatSettlementSummary(LevelingSeatSettlementSummary.Status.NOT_READY);
		} else {
			return new LevelingSeatSettlementSummary(LevelingSeatSettlementSummary.Status.DONE, LevelingSeatMapper.levelingSeats(levelingSeats));
		}
	}
}
