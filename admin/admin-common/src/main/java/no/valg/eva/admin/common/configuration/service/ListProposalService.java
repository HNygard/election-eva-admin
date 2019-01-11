package no.valg.eva.admin.common.configuration.service;

import no.evote.security.UserData;
import no.valg.eva.admin.common.AreaPath;
import no.valg.eva.admin.common.configuration.model.local.ListProposalConfig;

public interface ListProposalService {

	ListProposalConfig findByArea(UserData userData, AreaPath areaPath);

	ListProposalConfig save(UserData userData, ListProposalConfig listProposal, boolean saveChildren);
}
