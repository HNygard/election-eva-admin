package no.valg.eva.admin.rbac.service;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;

import static no.evote.constants.AreaLevelEnum.MUNICIPALITY;
import static no.valg.eva.admin.common.rbac.Accesses.Tilgang_Brukere_Importer_Forhånd;
import static no.valg.eva.admin.common.rbac.Accesses.Tilgang_Brukere_Importer_Valgting;
import static no.valg.eva.admin.common.rbac.SecurityType.WRITE;

import java.util.List;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.inject.Inject;

import no.evote.constants.AreaLevelEnum;
import no.evote.exception.EvoteException;
import no.evote.security.UserData;
import no.valg.eva.admin.backend.configuration.repository.ElectionEventRepository;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.rbac.ImportOperatorRoleInfo;
import no.valg.eva.admin.common.rbac.PollingPlaceResponsibleOperator;
import no.valg.eva.admin.common.rbac.Security;
import no.valg.eva.admin.common.rbac.VoteReceiver;
import no.valg.eva.admin.common.rbac.service.ImportOperatorService;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.MvElection;
import no.valg.eva.admin.configuration.domain.model.Voter;
import no.valg.eva.admin.configuration.repository.MvAreaRepository;
import no.valg.eva.admin.configuration.repository.VoterRepository;
import no.valg.eva.admin.rbac.domain.model.Operator;
import no.valg.eva.admin.rbac.domain.model.OperatorRole;
import no.valg.eva.admin.rbac.domain.model.Role;
import no.valg.eva.admin.rbac.repository.OperatorRepository;
import no.valg.eva.admin.rbac.repository.OperatorRoleRepository;
import no.valg.eva.admin.rbac.repository.RoleRepository;

import org.apache.log4j.Logger;

@Stateless(name = "ImportOperatorService")
@Remote(ImportOperatorService.class)
@Default
@Dependent
public class ImportOperatorServiceEjb implements ImportOperatorService {

	private static final String EARLY_VOTE_RECEIVER = "stemmemottak_forhånd";
	private static final String OPPTELLING_STEMMEKRETS = "ansvarlig_valglokale";
	private static final String VOTE_RECEIVER = "stemmemottak_valgting";
	private final Logger logger = Logger.getLogger(ImportOperatorServiceEjb.class);
	@Inject
	private RoleRepository roleRepository;
	@Inject
	private OperatorRoleRepository operatorRoleRepository;
	@Inject
	private OperatorRepository operatorRepository;
	@Inject
	private ElectionEventRepository electionEventRepository;
	@Inject
	private MvAreaRepository mvAreaRepository;
	@Inject
	private VoterRepository voterRepository;

	@Override
	@Security(accesses = Tilgang_Brukere_Importer_Forhånd, type = WRITE)
	public void importEarlyVoteReceiverOperator(final UserData userData, List<ImportOperatorRoleInfo> earlyVotingOperatorList) {
		if (userData.getOperatorMvArea().getAreaLevel() < MUNICIPALITY.getLevel()) {
			throw new EvoteException("@rbac.import_operators.invalid_user_arealevel");
		}
		importOperators(userData, earlyVotingOperatorList, EARLY_VOTE_RECEIVER);
	}

	@Override
	@Security(accesses = Tilgang_Brukere_Importer_Valgting, type = WRITE)
	public void importVotingAndPollingPlaceResponsibleOperators(UserData userData, List<VoteReceiver> votingOperatorList,
			List<PollingPlaceResponsibleOperator> pollingPlaceResponsibleOperatorList) {
		if (userData.getOperatorMvArea().getAreaLevel() < MUNICIPALITY.getLevel()) {
			throw new EvoteException("@rbac.import_operators.invalid_user_arealevel");
		}
		importOperators(userData, pollingPlaceResponsibleOperatorList, OPPTELLING_STEMMEKRETS);
		importOperators(userData, votingOperatorList, VOTE_RECEIVER);
	}

	private void importOperators(UserData userData, List<? extends ImportOperatorRoleInfo> operatorInfoList, String roleId) {
		if (operatorInfoList == null) {
			return;
		}

		ElectionEvent ee = electionEventRepository.findByPk(userData.getElectionEventPk());
		MvElection mvElection = userData.getOperatorRole().getMvElection();
		Role role = findImportRole(ee, roleId);
		for (ImportOperatorRoleInfo importOperator : operatorInfoList) {
			MvArea mvArea = retrieveMvArea(userData, importOperator);
			if (mvArea == null) {
				throw new EvoteException("@rbac.import_operators.unknown_area_id_error", importOperator.getAreaId(), importOperator.getOperatorId());
			}
			Operator operator = operatorRepository.findByElectionEventsAndId(ee.getPk(), importOperator.getOperatorId());
			if (operator == null) {
				Voter voter = voterRepository.voterOfId(importOperator.getOperatorId(), ee.getPk());
				if (!isImportOperatorInElectoralRoll(voter)) {
					if (!isImportOperatorNameGiven(importOperator.getLastName(), importOperator.getFirstName())) {
						throw new EvoteException("@rbac.import_operators.unknown_name_error", importOperator.getOperatorId());
					}
				} else {
					importOperator.setName(voter.getFirstName(), voter.getLastName());
				}
				createOperator(userData, ee, role, importOperator, mvArea, mvElection);
			} else {
				if (!doesOperatorRoleAlreadyExist(operator, role, mvArea, mvElection)) {
					addOperatorRole(userData, operator, role, mvArea, mvElection);
				}
			}
		}
	}

	private boolean isImportOperatorInElectoralRoll(Voter electoralRollEntry) {
		return electoralRollEntry != null;
	}

	private boolean isImportOperatorNameGiven(String lastName, String firstName) {
		return lastName != null && firstName != null && !(lastName.trim().equals("") || firstName.trim().equals(""));
	}

	private void createOperator(UserData userData, ElectionEvent ee, Role role, ImportOperatorRoleInfo importOperatorInfo, MvArea mvArea,
			MvElection mvElection) {
		Operator operator = initNewOperator(ee, importOperatorInfo);
		try {
			operatorRepository.create(userData, operator);
		} catch (Exception exception) {
			logger.error(exception.getMessage(), exception);
			throw new EvoteException("@rbac.import_operators.operator_error", exception, operator.getId(), mvArea.getPath());

		}
		addOperatorRole(userData, operator, role, mvArea, mvElection);
	}

	private void addOperatorRole(UserData userdata, Operator operator, Role role, MvArea mvArea, MvElection mvElection) {
		OperatorRole operatorRole = new OperatorRole();
		operatorRole.setRole(role);
		operatorRole.setOperator(operator);
		operatorRole.setMvArea(mvArea);
		operatorRole.setMvElection(mvElection);
		try {
			operatorRoleRepository.create(userdata, operatorRole);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new EvoteException("@rbac.import_operators.operator_role_error", operator.getId(), mvArea.getPath());
		}
	}

	private Operator initNewOperator(ElectionEvent electionEvent, ImportOperatorRoleInfo operatorInfo) {
		// kan etterhvert bruke OperatorFactory
		Operator newOperator = new Operator();
		newOperator.setElectionEvent(electionEvent);
		newOperator.setId(operatorInfo.getOperatorId());
		newOperator.setNameLine(operatorInfo.getFirstName() + " " + operatorInfo.getLastName());
		newOperator.setFirstName(operatorInfo.getFirstName());
		newOperator.setLastName(operatorInfo.getLastName());
		newOperator.setEmail(operatorInfo.getEmail());
		newOperator.setTelephoneNumber(operatorInfo.getTelephoneNumber());
		newOperator.setActive(true);
		return newOperator;
	}

	private boolean doesOperatorRoleAlreadyExist(Operator operator, Role role, MvArea mvArea, MvElection mvElection) {
		return operatorRoleRepository.findUnique(role, operator, mvArea, mvElection) != null;
	}

	private Role findImportRole(ElectionEvent ee, String roleId) {
		return roleRepository.findByElectionEventAndId(ee, roleId);
	}

	/**
	 * Henter areaPath og MvArea, basert på innlogget brukers/userData MvAreaPath og angitt stemmekrets ELLER stemmested.
	 *
	 * @param userData data på innlogget bruker
	 */
	private MvArea retrieveMvArea(UserData userData, ImportOperatorRoleInfo operatorInfo) {
		if (!isAreaDefined(operatorInfo.getAreaId())) {
			AreaPath ap = new AreaPath(userData.getOperatorMvArea().getAreaPath());
			return mvAreaRepository.findByPathAndLevel(ap.toMunicipalityPath().toString(), AreaLevelEnum.MUNICIPALITY.getLevel()).get(0);
		}

		if (operatorInfo.areaIsPollingDistrict()) {
			return mvAreaRepository.findSingleByPollingDistrictIdAndMunicipalityPk(operatorInfo.getAreaId(), userData.getOperatorMvArea().getMunicipality()
					.getPk());
		} else {
			return mvAreaRepository.findSingleByPollingPlaceIdAndMunicipalityPk(operatorInfo.getAreaId(), userData.getOperatorMvArea().getMunicipality()
					.getPk());
		}
	}

	/**
	 * Sjekker om stemmekrets/forhåndsstemmested er angitt.
	 */
	private boolean isAreaDefined(String pollingDistrictOrPlace) {
		return pollingDistrictOrPlace != null && !pollingDistrictOrPlace.trim().equals("");
	}

	public void setMvAreaService(MvAreaRepository mvAreaRepository) {
		this.mvAreaRepository = mvAreaRepository;
	}

	public void setOperatorRepository(OperatorRepository operatorRepository) {
		this.operatorRepository = operatorRepository;
	}

	public void setElectionEventRepository(ElectionEventRepository electionEventRepository) {
		this.electionEventRepository = electionEventRepository;
	}

	public void setRoleRepository(RoleRepository roleRepository) {
		this.roleRepository = roleRepository;
	}

	public void setOperatorRoleRepository(OperatorRoleRepository operatorRoleRepository) {
		this.operatorRoleRepository = operatorRoleRepository;
	}

	public void setVoterRepository(VoterRepository voterRepository) {
		this.voterRepository = voterRepository;
	}
}
