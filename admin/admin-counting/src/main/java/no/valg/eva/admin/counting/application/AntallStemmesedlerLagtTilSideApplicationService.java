package no.valg.eva.admin.counting.application;

import static no.valg.eva.admin.common.rbac.Accesses.Aggregert_Opptelling_Lagt_Til_Side;
import static no.valg.eva.admin.common.rbac.Accesses.Opptelling_Lagt_Til_Side_Rediger;
import static no.valg.eva.admin.common.rbac.SecurityType.READ;
import static no.valg.eva.admin.common.rbac.SecurityType.WRITE;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.inject.Inject;

import no.evote.security.UserData;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.AuditLog;
import no.valg.eva.admin.common.counting.model.AntallStemmesedlerLagtTilSide;
import no.valg.eva.admin.common.counting.service.AntallStemmesedlerLagtTilSideService;
import no.valg.eva.admin.common.rbac.Security;
import no.valg.eva.admin.configuration.domain.model.Municipality;
import no.valg.eva.admin.configuration.repository.MvAreaRepository;
import no.valg.eva.admin.counting.domain.auditevents.AntallStemmesedlerLagtTilSideAuditEvent;
import no.valg.eva.admin.counting.domain.service.AntallStemmesedlerLagtTilSideDomainService;
import no.valg.eva.admin.felles.sti.valggeografi.KommuneSti;

@Stateless(name = "AntallStemmesedlerLagtTilSideService")
@Remote(AntallStemmesedlerLagtTilSideService.class)
public class AntallStemmesedlerLagtTilSideApplicationService implements AntallStemmesedlerLagtTilSideService {
	private MvAreaRepository mvAreaRepository;
	private AntallStemmesedlerLagtTilSideDomainService domainService;

	@SuppressWarnings("unused")
	public AntallStemmesedlerLagtTilSideApplicationService() {
	}

	@Inject
	public AntallStemmesedlerLagtTilSideApplicationService(MvAreaRepository mvAreaRepository, AntallStemmesedlerLagtTilSideDomainService domainService) {
		this.mvAreaRepository = mvAreaRepository;
		this.domainService = domainService;
	}

	@Override
	@Security(accesses = Opptelling_Lagt_Til_Side_Rediger, type = WRITE)
	@AuditLog(eventClass = AntallStemmesedlerLagtTilSideAuditEvent.class, eventType = AuditEventTypes.Save)
	public void lagreAntallStemmesedlerLagtTilSide(UserData userData, AntallStemmesedlerLagtTilSide antallStemmesedlerLagtTilSide) {
		AreaPath municipalityPath = antallStemmesedlerLagtTilSide.getMunicipalityPath();
		municipalityPath.assertMunicipalityLevel();
		Municipality municipality = mvAreaRepository.findSingleByPath(municipalityPath).getMunicipality();
		domainService.lagreAntallStemmesedlerLagtTilSide(userData, municipality, antallStemmesedlerLagtTilSide);
	}

	@Override
	@Security(accesses = Aggregert_Opptelling_Lagt_Til_Side, type = READ)
	public AntallStemmesedlerLagtTilSide hentAntallStemmesedlerLagtTilSide(UserData userData, KommuneSti kommuneSti) {
		Municipality municipality = mvAreaRepository.findSingleByPath(kommuneSti.areaPath()).getMunicipality();
		return domainService.hentAntallStemmesedlerLagtTilSide(municipality);
	}
}
