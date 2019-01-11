package no.valg.eva.admin.frontend.manntall.ctrls;

import static no.valg.eva.admin.felles.valggeografi.model.ValggeografiNivaa.KOMMUNE;
import static no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerElement.geografi;
import static org.apache.commons.lang3.StringUtils.isEmpty;

import java.util.List;

import no.valg.eva.admin.common.configuration.status.ElectionEventStatusEnum;
import no.valg.eva.admin.configuration.domain.model.MvArea;
import no.valg.eva.admin.configuration.domain.model.Voter;
import no.valg.eva.admin.felles.sti.valggeografi.StemmekretsSti;
import no.valg.eva.admin.frontend.common.PageTitleMetaModel;
import no.valg.eva.admin.frontend.kontekstvelger.Kontekst;
import no.valg.eva.admin.frontend.kontekstvelger.KontekstAvhengigController;
import no.valg.eva.admin.frontend.kontekstvelger.oppsett.KontekstvelgerOppsett;

import org.apache.commons.lang3.StringUtils;

public abstract class BaseManntallController extends KontekstAvhengigController {

	private MvArea kommune;

	@Override
	public KontekstvelgerOppsett getKontekstVelgerOppsett() {
		KontekstvelgerOppsett setup = new KontekstvelgerOppsett();
		setup.leggTil(geografi(KOMMUNE));
		return setup;
	}

	@Override
	public void initialized(Kontekst kontekst) {
		kommune = getMvAreaService().findSingleByPath(kontekst.kommuneSti());
	}

	public boolean isEditerManntallTilgjengelig() {
		return getUserDataController().getElectionEvent().isDemoElection()
				|| getUserDataController().getElectionEvent().getElectionEventStatus().getId() == ElectionEventStatusEnum.APPROVED_CONFIGURATION.id();
	}

	public List<PageTitleMetaModel> getPageTitleMeta() {
		return getPageTitleMetaBuilder().area(kommune);
	}

	public MvArea getKommune() {
		return kommune;
	}

	String byggNavnelinje(Voter voter) {
		StringBuilder nameLine = new StringBuilder(voter.getLastName());
		nameLine.append(" ");
		nameLine.append(voter.getFirstName());
		nameLine.append(StringUtils.isEmpty(voter.getMiddleName()) ? "" : " " + voter.getMiddleName());
		return nameLine.toString().trim();
	}

	void endreStemmekrets(Voter voter, StemmekretsSti stemmekretsSti) {
		MvArea mvArea = getMvAreaService().findSingleByPath(stemmekretsSti);
		if (mvArea != null) {
			voter.setPollingDistrictId(mvArea.getPollingDistrictId());
			voter.setCountryId(mvArea.getCountryId());
			voter.setCountyId(mvArea.getCountyId());
			voter.setMunicipalityId(mvArea.getMunicipalityId());
			voter.setBoroughId(mvArea.getBoroughId());
			voter.setMvArea(mvArea);
		}
	}

	void setMailingAddressSpecified(Voter voter) {
		boolean mailingAddressSpecified = !isEmpty(voter.getMailingAddressLine1())
				|| !isEmpty(voter.getMailingAddressLine2())
				|| !isEmpty(voter.getMailingAddressLine3());
		voter.setMailingAddressSpecified(mailingAddressSpecified);
	}
}
