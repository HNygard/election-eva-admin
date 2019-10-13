package no.valg.eva.admin.configuration.application;

import static no.valg.eva.admin.common.auditlog.AuditEventTypes.Delete;
import static no.valg.eva.admin.common.auditlog.AuditEventTypes.Save;
import static no.valg.eva.admin.common.rbac.Accesses.Konfigurasjon_Grunnlagsdata_Redigere;
import static no.valg.eva.admin.common.rbac.SecurityType.READ;
import static no.valg.eva.admin.common.rbac.SecurityType.WRITE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.inject.Inject;

import no.evote.constants.AreaLevelEnum;
import no.evote.exception.EvoteException;
import no.evote.security.UserData;
import no.evote.service.configuration.PollingDistrictServiceBean;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.auditlog.AuditLog;
import no.valg.eva.admin.common.auditlog.auditevents.config.ParentPollingDistrictAuditEvent;
import no.valg.eva.admin.common.auditlog.auditevents.config.TechnicalPollingDistrictAuditEvent;
import no.valg.eva.admin.common.configuration.model.local.ParentPollingDistrict;
import no.valg.eva.admin.common.configuration.model.local.ParentPollingDistricts;
import no.valg.eva.admin.common.configuration.model.local.PlaceSortById;
import no.valg.eva.admin.common.configuration.model.local.RegularPollingDistrict;
import no.valg.eva.admin.common.configuration.model.local.TechnicalPollingDistrict;
import no.valg.eva.admin.common.configuration.service.PollingDistrictService;
import no.valg.eva.admin.common.rbac.Security;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.domain.model.PollingDistrict;
import no.valg.eva.admin.configuration.domain.model.PollingDistrictType;
import no.valg.eva.admin.configuration.repository.MunicipalityRepository;
import no.valg.eva.admin.configuration.repository.PollingDistrictRepository;
import no.valg.eva.admin.configuration.repository.ReportingUnitRepository;
import no.valg.eva.admin.configuration.repository.ResponsibleOfficerRepository;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;

@Stateless(name = "PollingDistrictService")


@Default
@Remote(PollingDistrictService.class)
public class PollingDistrictApplicationService implements PollingDistrictService {

	// Injected
	@Inject
	private MunicipalityRepository municipalityRepository;
	@Inject
	private ResponsibleOfficerRepository responsibleOfficerRepository;
	@Inject
	private ReportingUnitRepository reportingUnitRepository;
	@Inject
	private PollingDistrictServiceBean pollingDistrictService;
	@Inject
	private PollingDistrictRepository pollingDistrictRepository;

	public PollingDistrictApplicationService() {

	}
	public PollingDistrictApplicationService(MunicipalityRepository municipalityRepository,
			ResponsibleOfficerRepository responsibleOfficerRepository, ReportingUnitRepository reportingUnitRepository,
			PollingDistrictServiceBean pollingDistrictService, PollingDistrictRepository pollingDistrictRepository) {
		this.municipalityRepository = municipalityRepository;
		this.responsibleOfficerRepository = responsibleOfficerRepository;
		this.reportingUnitRepository = reportingUnitRepository;
		this.pollingDistrictService = pollingDistrictService;
		this.pollingDistrictRepository = pollingDistrictRepository;
	}

	@Override
	@Security(accesses = Konfigurasjon_Grunnlagsdata_Redigere, type = READ)
	public List<no.valg.eva.admin.common.configuration.model.local.PollingDistrict> findRegularPollingDistrictsByArea(
			UserData userData, AreaPath areaPath, boolean includeParents) {
		areaPath.assertLevel(AreaLevelEnum.MUNICIPALITY);
		Municipality municipality = getMunicipality(userData, areaPath);
		List<no.valg.eva.admin.common.configuration.model.local.PollingDistrict> result = new ArrayList<>();
		for (PollingDistrict pd : municipality.regularPollingDistricts(includeParents, true)) {
			long reportingUnitPk = reportingUnitRepository.findReportingUnitByAreaLevel(pd.areaPath()).getPk();
			no.valg.eva.admin.common.configuration.model.local.PollingDistrict pdResult = PollingDistrictMapper.toPollingDistrict(pd);
			pdResult.setHasResponsibleOffiers(responsibleOfficerRepository.hasResponsibleOfficersForReportingUnit(reportingUnitPk));
			result.add(pdResult);
		}
		Collections.sort(result, new PlaceSortById<>());
		return result;
	}

	@Override
	@Security(accesses = Konfigurasjon_Grunnlagsdata_Redigere, type = READ)
	public List<TechnicalPollingDistrict> findTechnicalPollingDistrictsByArea(UserData userData, AreaPath areaPath) {
		areaPath.assertLevel(AreaLevelEnum.MUNICIPALITY);
		Municipality municipality = getMunicipality(userData, areaPath);
		return municipality.technicalPollingDistricts()
				.stream()
				.map(PollingDistrictMapper::toTechnicalPollingDistrict)
				.sorted(new PlaceSortById<>())
				.collect(Collectors.toList());
	}

	@Override
	@Security(accesses = Konfigurasjon_Grunnlagsdata_Redigere, type = READ)
	public TechnicalPollingDistrict findTechnicalPollingDistrictByAreaAndId(UserData userData, AreaPath areaPath, String id) {
		return PollingDistrictMapper.toTechnicalPollingDistrict(getMunicipality(userData, areaPath).technicalPollingDistrictById(id));
	}

	@Override
	@Security(accesses = Konfigurasjon_Grunnlagsdata_Redigere, type = READ)
	public ParentPollingDistricts findParentPollingDistrictsByArea(UserData userData, AreaPath areaPath) {
		areaPath.assertLevel(AreaLevelEnum.MUNICIPALITY);
		Municipality municipality = getMunicipality(userData, areaPath);
		ParentPollingDistricts result = new ParentPollingDistricts();
		for (PollingDistrict pd : municipality.pollingDistricts()) {
			if (pd.type() == PollingDistrictType.REGULAR) {
				result.getSelectableDistricts().add(PollingDistrictMapper.toRegularPollingDistrict(pd));
			} else if (pd.type() == PollingDistrictType.PARENT) {
				result.getParentPollingDistricts().add(PollingDistrictMapper.toParentPollingDistrict(pd));
			}
		}
		Collections.sort(result.getSelectableDistricts(), new PlaceSortById<>());
		Collections.sort(result.getParentPollingDistricts(), new PlaceSortById<>());
		return result;
	}

	@Override
	@Security(accesses = Konfigurasjon_Grunnlagsdata_Redigere, type = WRITE)
	@AuditLog(eventClass = TechnicalPollingDistrictAuditEvent.class, eventType = Save)
	public TechnicalPollingDistrict saveTechnicalPollingDistrict(UserData userData, TechnicalPollingDistrict district) {
		PollingDistrict pollingDistrict;
		if (district.getPk() == null) {
			district.getPath().assertLevel(AreaLevelEnum.MUNICIPALITY);
			Municipality municipality = getMunicipality(userData, district.getPath());
			pollingDistrict = PollingDistrictMapper.toPollingDistrict(new PollingDistrict(), district);
			pollingDistrict.setBorough(municipality.getMunicipalityBorough());
			pollingDistrict = pollingDistrictRepository.create(userData, pollingDistrict);
		} else {
			district.getPath().assertLevel(AreaLevelEnum.POLLING_DISTRICT);
			pollingDistrict = pollingDistrictRepository.findByPk(district.getPk());
			pollingDistrict.checkVersion(district);
			pollingDistrict = PollingDistrictMapper.toPollingDistrict(pollingDistrict, district);
			pollingDistrict = pollingDistrictRepository.update(userData, pollingDistrict);
		}
		return PollingDistrictMapper.toTechnicalPollingDistrict(pollingDistrict);
	}

	@Override
	@Security(accesses = Konfigurasjon_Grunnlagsdata_Redigere, type = WRITE)
	@AuditLog(eventClass = ParentPollingDistrictAuditEvent.class, eventType = Save)
	public ParentPollingDistrict saveParentPollingDistrict(UserData userData, ParentPollingDistrict district) {
		PollingDistrict pollingDistrict;
		if (district.getPk() == null) {
			district.getPath().assertLevel(AreaLevelEnum.MUNICIPALITY);
			List<PollingDistrict> children = new ArrayList<>();
			for (RegularPollingDistrict child : district.getChildren()) {
				PollingDistrict dbChild = pollingDistrictRepository.findByPk(child.getPk());
				dbChild.checkVersion(child);
				children.add(dbChild);
			}
			Municipality municipality = getMunicipality(userData, district.getPath());
			pollingDistrict = PollingDistrictMapper.toPollingDistrict(new PollingDistrict(), district);
			pollingDistrict.setBorough(municipality.getMunicipalityBorough());
			pollingDistrict = pollingDistrictService.createParentPollingDistrict(userData, pollingDistrict, children);
		} else {
			throw new EvoteException("ParentPollingDistrict update not implemented");
		}
		return PollingDistrictMapper.toParentPollingDistrict(pollingDistrict);
	}

	@Override
	@Security(accesses = Konfigurasjon_Grunnlagsdata_Redigere, type = WRITE)
	@AuditLog(eventClass = TechnicalPollingDistrictAuditEvent.class, eventType = Delete)
	public void deleteTechnicalPollingDistrict(UserData userData, TechnicalPollingDistrict district) {
		PollingDistrict dbDistrict = pollingDistrictRepository.findByPk(district.getPk());
		dbDistrict.checkVersion(district);
		pollingDistrictRepository.delete(userData, dbDistrict.getPk());
	}

	@Override
	@Security(accesses = Konfigurasjon_Grunnlagsdata_Redigere, type = WRITE)
	@AuditLog(eventClass = ParentPollingDistrictAuditEvent.class, eventType = Delete)
	public void deleteParentPollingDistrict(UserData userData, ParentPollingDistrict district) {
		PollingDistrict dbDistrict = pollingDistrictRepository.findByPk(district.getPk());
		dbDistrict.checkVersion(district);
		pollingDistrictRepository.deleteParentPollingDistrict(userData, dbDistrict);
	}

	private Municipality getMunicipality(UserData userData, AreaPath areaPath) {
		return municipalityRepository.municipalityByElectionEventAndId(userData.getElectionEventPk(), areaPath.getMunicipalityId());
	}
}
