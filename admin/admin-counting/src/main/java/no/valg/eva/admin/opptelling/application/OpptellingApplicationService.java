package no.valg.eva.admin.opptelling.application;

import static no.valg.eva.admin.common.rbac.Accesses.Beskyttet_Slett_Opptelling;
import static no.valg.eva.admin.common.rbac.SecurityType.WRITE;
import static org.apache.commons.lang3.ArrayUtils.isEmpty;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.inject.Inject;

import no.evote.security.UserData;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.AuditLog;
import no.valg.eva.admin.common.counting.model.CountCategory;
import no.valg.eva.admin.common.rbac.Security;
import no.valg.eva.admin.felles.konfigurasjon.model.Styretype;
import no.valg.eva.admin.felles.opptelling.service.OpptellingService;
import no.valg.eva.admin.felles.sti.valggeografi.ValggeografiSti;
import no.valg.eva.admin.felles.sti.valghierarki.ValghierarkiSti;
import no.valg.eva.admin.opptelling.domain.auditevent.OpptellingerAuditEvent;
import no.valg.eva.admin.opptelling.domain.service.OpptellingDomainService;

@Stateless(name = "OpptellingService")
@Remote(OpptellingService.class)
public class OpptellingApplicationService implements OpptellingService {
	@Inject
	private OpptellingDomainService opptellingDomainService;

	@Override
	@Security(accesses = Beskyttet_Slett_Opptelling, type = WRITE)
	@AuditLog(eventClass = OpptellingerAuditEvent.class, eventType = AuditEventTypes.DeletedAllInArea)
	public void slettOpptellinger(UserData userData, ValghierarkiSti valghierarkiSti, ValggeografiSti valggeografiSti,
								  CountCategory[] countCategories, Styretype[] styretyper) {
		if (isEmpty(countCategories) && isEmpty(styretyper)) {
			opptellingDomainService.slettOpptellinger(valghierarkiSti, valggeografiSti);
		} else if (isEmpty(countCategories)) {
			opptellingDomainService.slettOpptellinger(valghierarkiSti, valggeografiSti, styretyper);
		} else if (isEmpty(styretyper)) {
			opptellingDomainService.slettOpptellinger(valghierarkiSti, valggeografiSti, countCategories);
		} else {
			opptellingDomainService.slettOpptellinger(valghierarkiSti, valggeografiSti, countCategories, styretyper);
		}
	}
}
