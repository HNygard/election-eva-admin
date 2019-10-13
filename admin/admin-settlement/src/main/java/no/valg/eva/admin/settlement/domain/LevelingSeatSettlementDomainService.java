package no.valg.eva.admin.settlement.domain;

import static java.util.stream.Collectors.toList;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.inject.Inject;

import no.evote.security.UserData;
import no.valg.eva.admin.configuration.domain.model.Election;
import no.valg.eva.admin.settlement.domain.builder.LevelingSeatSettlementBuilder;
import no.valg.eva.admin.settlement.domain.builder.LevelingSeatSettlementBuilderFactory;
import no.valg.eva.admin.settlement.domain.model.LevelingSeatSettlement;
import no.valg.eva.admin.settlement.domain.model.Settlement;
import no.valg.eva.admin.settlement.repository.LevelingSeatSettlementRepository;
import no.valg.eva.admin.settlement.repository.SettlementRepository;

@Default
@ApplicationScoped
public class LevelingSeatSettlementDomainService {
	@Inject
	private LevelingSeatSettlementBuilderFactory levelingSeatSettlementBuilderFactory;
	@Inject
	private SettlementRepository settlementRepository;
	@Inject
	private LevelingSeatSettlementRepository levelingSeatSettlementRepository;

	public LevelingSeatSettlementDomainService() {

	}
	public LevelingSeatSettlementDomainService(
			LevelingSeatSettlementBuilderFactory levelingSeatSettlementBuilderFactory, SettlementRepository settlementRepository,
			LevelingSeatSettlementRepository levelingSeatSettlementRepository) {
		this.levelingSeatSettlementBuilderFactory = levelingSeatSettlementBuilderFactory;
		this.settlementRepository = settlementRepository;
		this.levelingSeatSettlementRepository = levelingSeatSettlementRepository;
	}

	public void distributeLevelingSeats(UserData userData, Election election) {
		LevelingSeatSettlementBuilder levelingSeatSettlementBuilder = levelingSeatSettlementBuilderFactory.levelingSeatSettlementBuilder(election,
				settlements(election));
		LevelingSeatSettlement levelingSeatSettlement = levelingSeatSettlementBuilder.build();
		levelingSeatSettlementRepository.create(userData, levelingSeatSettlement);
	}

	private List<Settlement> settlements(Election election) {
		return election.getContests()
				.stream()
				.map(contest -> settlementRepository.findSettlementByContest(contest))
				.collect(toList());
	}
}
