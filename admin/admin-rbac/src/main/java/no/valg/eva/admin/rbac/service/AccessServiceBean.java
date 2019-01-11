package no.valg.eva.admin.rbac.service;

import static no.valg.eva.admin.common.ElectionPath.ROOT_ELECTION_EVENT_ID;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import no.evote.security.AccessCache;
import no.evote.security.UserData;
import no.evote.service.CryptoServiceBean;
import no.valg.eva.admin.backend.configuration.repository.ElectionEventRepository;
import no.valg.eva.admin.common.rbac.Access;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.rbac.repository.AccessRepository;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

public class AccessServiceBean {
	private static final Logger LOG = Logger.getLogger(AccessServiceBean.class);
	
	@Inject
	private ElectionEventRepository electionEventRepository;
	@Inject
	private CryptoServiceBean cryptoService;
	@Inject
	private AccessRepository accessRepository;

	public AccessCache findAccessCacheFor(final UserData userData) {

		Set<String> accessPaths = accessRepository.getIncludedAccessesNoDisabledRoles(userData.getRole());

		String asString = StringUtils.join(accessPaths.toArray(new String[accessPaths.size()]), ",");

		// Don't try to sign data for the admin event, since it doesn't have any key.
		ElectionEvent electionEvent = electionEventRepository.findByPk(userData.getElectionEventPk());
		byte[] signature = null;
		if (!electionEvent.getId().equals(ROOT_ELECTION_EVENT_ID)) {
			try {
				signature = cryptoService.signDataWithCurrentElectionEventCertificate(userData, asString.getBytes("UTF-8"));
			} catch (UnsupportedEncodingException e) {
				LOG.error(e);
			}
		}
		return new AccessCache(accessPaths, signature);
	}

	public List<Access> findAll() {
		List<no.valg.eva.admin.rbac.domain.model.Access> accesses = accessRepository.findAllAccesses();
		return accesses.stream().map(no.valg.eva.admin.rbac.domain.model.Access::toViewObject).collect(Collectors.toList());
	}

}
