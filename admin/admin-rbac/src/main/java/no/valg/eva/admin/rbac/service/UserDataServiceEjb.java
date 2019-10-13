package no.valg.eva.admin.rbac.service;

import no.evote.constants.ElectionLevelEnum;
import no.evote.exception.EvoteSecurityException;
import no.evote.security.SecurityLevel;
import no.evote.security.UserData;
import no.valg.eva.admin.backend.common.auditlog.AuditLogServiceBean;
import no.valg.eva.admin.backend.common.repository.LocaleRepository;
import no.valg.eva.admin.common.auditlog.Outcome;
import no.valg.eva.admin.common.auditlog.auditevents.OperatorLoginAuditEvent;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.common.rbac.Accesses;
import no.valg.eva.admin.common.rbac.SecurityNone;
import no.valg.eva.admin.common.rbac.UserMenuMetadata;
import no.valg.eva.admin.common.rbac.service.UserDataService;
import no.valg.eva.admin.configuration.domain.model.*;
import no.valg.eva.admin.configuration.domain.service.BoroughElectionDomainService;
import no.valg.eva.admin.configuration.domain.service.OpptellingskategoriDomainService;
import no.valg.eva.admin.configuration.repository.CountyRepository;
import no.valg.eva.admin.configuration.repository.ElectionRepository;
import no.valg.eva.admin.configuration.repository.MunicipalityRepository;
import no.valg.eva.admin.configuration.repository.MvElectionRepository;
import no.valg.eva.admin.rbac.domain.model.OperatorRole;
import no.valg.eva.admin.rbac.repository.OperatorRepository;
import no.valg.eva.admin.rbac.repository.OperatorRoleRepository;
import org.apache.log4j.Logger;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.enterprise.inject.Default;
import javax.inject.Inject;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import static no.valg.eva.admin.common.rbac.Accesses.Opptelling_Forhånd_Se;
import static no.valg.eva.admin.common.rbac.Accesses.Opptelling_Valgting_Se;
import static no.valg.eva.admin.configuration.domain.model.ElectionType.TYPE_PROPORTIONAL_REPRESENTATION;

@Stateless(name = "UserDataService")
@Remote(UserDataService.class)
@Default
public class UserDataServiceEjb implements UserDataService {
    private static final String USER_PASSED_ID_PORTEN_LOGIN_BUT_DID_NOT_HAVE_AN_OPERATOR_IN_THE_SYSTEM =
            "User passed ID-porten login but did not have an operator in the system";
    private static final Logger LOGGER = Logger.getLogger(UserDataService.class);
    private static final String SPACE = " ";

    @Inject
    private LocaleRepository localeRepository;
    // TODO: The inject didnt work...
    //@Inject
    private AuditLogServiceBean auditLogService = null;
    @Inject
    private AccessServiceBean accessService;
    @Inject
    private OperatorRoleRepository operatorRoleRepository;
    @Inject
    private OperatorRepository operatorRepository;
    @Inject
    private ElectionRepository electionRepository;
    @Inject
    private MvElectionRepository mvElectionRepository;
    @Inject
    private MunicipalityRepository municipalityRepository;
    @Inject
    private CountyRepository countyRepository;
    @Inject
    private OpptellingskategoriDomainService opptellingskategoriDomainService;
    @Inject
    private BoroughElectionDomainService boroughElectionDomainService;

    public UserDataServiceEjb() {
    }

    @Override
    @SecurityNone
    public UserData createUserDataAndCheckOperator(String uid, String securityLevel, String locale, InetAddress clientAddress) {
        UserData userData = createUserData(uid, securityLevel, locale, clientAddress);

        if (!operatorRepository.hasOperator(userData.getUid())) {
            try {
                auditLogService.addToAuditTrail(new OperatorLoginAuditEvent(userData, Outcome.UnknownOperator));
            } catch (Exception e) {
                LOGGER.warn("addToAuditTrail() feilet: " + e.getMessage(), e);
            }
            LOGGER.warn(userData.getClientAddress() + SPACE + userData.getUid() + SPACE + new Exception().getStackTrace()[0].getMethodName() + SPACE
                    + this.getClass().getName() + SPACE + USER_PASSED_ID_PORTEN_LOGIN_BUT_DID_NOT_HAVE_AN_OPERATOR_IN_THE_SYSTEM);
            throw new EvoteSecurityException(USER_PASSED_ID_PORTEN_LOGIN_BUT_DID_NOT_HAVE_AN_OPERATOR_IN_THE_SYSTEM + ": " + userData.getUid());
        }

        return userData;
    }

    private UserData createUserData(String uid, String securityLevel, String locale, InetAddress clientAddress) {
        SecurityLevel sl = SecurityLevel.fromLevel(Integer.valueOf(securityLevel.replaceAll("[a-zA-Z]", "")));
        Locale l = "nn".equals(locale) ? localeRepository.findById("nn-NO") : localeRepository.findById("nb-NO");
        return new UserData(uid, sl, l, clientAddress);
    }

    @Override
    @SecurityNone
    public UserData setAccessCacheOnUserData(final UserData userData, final OperatorRole operatorRole) {
        // 1. sjekker om signaturen til userData er gyldig
        // 2. sjekker om brukeren faktisk har den tilgangen som operatorRole sier at brukeren har ved aa sjekke i databasen
        // 3. hvis 1 og 2 ok, henter ut accessCache og legger til paa userData-objekter

        OperatorRole operatorRoleFromDatabase = operatorRoleRepository.findUnique(operatorRole.getRole(), operatorRole.getOperator(),
                operatorRole.getMvArea(), operatorRole.getMvElection());

        if (operatorRoleFromDatabase == null) {
            throw new EvoteSecurityException("specified operatorRole does not exist in database");
        }

        if (!operatorRole.getOperator().getId().equals(userData.getUid())) {
            throw new EvoteSecurityException("userid in userData and operatorRole is not equal");
        }

        userData.setOperatorRole(operatorRole);
        userData.setAccessCache(accessService.findAccessCacheFor(userData));
        return userData;
    }

    @Override
    @SecurityNone
    public UserMenuMetadata findUserMenuMetadata(UserData userData) {
        boolean electionPathAndMvAreaHasAccessToBoroughs = boroughElectionDomainService.electionPathAndMvAreaHasAccessToBoroughs(userData.getOperatorElectionPath(), 
                userData.getOperatorMvArea().areaPath());
        
        return UserMenuMetadata.builder()
                .areaPath(userData.getOperatorAreaPath())
                .electionPath(userData.getOperatorElectionPath())
                .hasElectionsWithTypeProportionalRepresentation(hasElectionsWithTypeProportionalRepresentation(userData))
                .validVoteCountCategories(getValidVoteCountCategories(userData))
                .electronicMarkOffsConfigured(isElectronicMarkoffsConfigured(userData))
                .accessToBoroughs(electionPathAndMvAreaHasAccessToBoroughs)
                .scanningEnabled(isScanningEnabled(userData))
                .build();
    }

    private boolean hasElectionsWithTypeProportionalRepresentation(UserData userData) {
        MvElection mvElection = userData.getOperatorRole().getMvElection();
        if (mvElection.getElectionLevel() > ElectionLevelEnum.ELECTION.getLevel()) {
            return mvElection.getElection().getElectionType().getId().equals(TYPE_PROPORTIONAL_REPRESENTATION);
        } else {
            ElectionType electionType = electionRepository.findElectionTypeById(TYPE_PROPORTIONAL_REPRESENTATION);
            return mvElectionRepository.hasElectionsWithElectionType(mvElection, electionType);
        }
    }

    private List<CountCategory> getValidVoteCountCategories(UserData userData) {
        if (hasAccess(userData, Opptelling_Forhånd_Se, Opptelling_Valgting_Se)) {
            return opptellingskategoriDomainService.countCategories(userData.operatorValggeografiSti());
        }
        return new ArrayList<>();
    }

    private boolean isScanningEnabled(UserData userData) {
        if (userData.isElectionEventAdminUser()) {
            return true;
        } else if (!isScanningEnabledInElectionGroup(userData)) {
            return false;
        } else {
            return isOperatorInMunicipalityWithScanning(userData) || isOperatorInCountyWithScanning(userData);
        }
    }

    private boolean isScanningEnabledInElectionGroup(UserData userData) {
        return userData.getOperatorMvElection().getElectionEvent().isScanningEnabledInElectionGroup();
    }

    private boolean isOperatorInMunicipalityWithScanning(UserData userData) {
        Municipality municipality = userData.getOperatorMvArea().getMunicipality();
        if (municipality == null) {
            return false;
        } else {
            municipality = municipalityRepository.findByPkWithScanningConfig(municipality.getPk());
            return municipality.getScanningConfig() != null && municipality.getScanningConfig().isScanning();
        } 
    }

    private boolean isOperatorInCountyWithScanning(UserData userData) {
        County county = userData.getOperatorMvArea().getCounty();
        if (county == null) {
            return false;
        } else {
            county = countyRepository.findByPkWithScanningConfig(county.getPk());
            return county.getScanningConfig() != null && county.getScanningConfig().isScanning();
        }
    }

    private boolean isElectronicMarkoffsConfigured(UserData userData) {
        Municipality municipality = userData.getOperatorRole().getMvArea().getMunicipality();
        if (municipality != null) {
            municipality = municipalityRepository.findByPk(municipality.getPk()); // Lookup again to find latest status for isElectronicMarkoffs
            return municipality.isElectronicMarkoffs();
        }
        return true;
    }

    private boolean hasAccess(UserData userData, Accesses... accesses) {
        return userData.hasAccess(accesses);
    }
    
}
