package no.valg.eva.admin.counting.application;

import static no.valg.eva.admin.common.rbac.Accesses.Opptelling_Forkastelser_Skannet;
import static no.valg.eva.admin.common.rbac.SecurityType.READ;
import static no.valg.eva.admin.common.rbac.SecurityType.WRITE;
import static no.valg.eva.admin.counting.domain.model.CastBallot.Type.MODIFIED;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.inject.Inject;

import no.evote.security.UserData;
import no.valg.eva.admin.common.auditlog.AuditEventTypes;
import no.valg.eva.admin.common.auditlog.AuditLog;
import no.valg.eva.admin.common.counting.model.ApprovedBallot;
import no.valg.eva.admin.common.counting.model.ApprovedFinalCountRef;
import no.valg.eva.admin.common.counting.model.RejectedBallot;
import no.valg.eva.admin.common.counting.service.CastBallotService;
import no.valg.eva.admin.common.rbac.Security;
import no.valg.eva.admin.counting.domain.auditevents.FinalCountAuditEvent;
import no.valg.eva.admin.counting.domain.model.BallotCount;
import no.valg.eva.admin.counting.domain.model.CastBallot;
import no.valg.eva.admin.counting.domain.model.VoteCount;
import no.valg.eva.admin.counting.domain.service.CastBallotDomainService;
import no.valg.eva.admin.counting.domain.service.VoteCountService;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;

@Stateless(name = "CastBallotService")


@Remote(CastBallotService.class)
@Default
public class CastBallotApplicationService implements CastBallotService {
	@Inject
	private VoteCountService voteCountService;
	@Inject
	private CastBallotDomainService castBallotDomainService;

	@Override
	@Security(accesses = Opptelling_Forkastelser_Skannet, type = READ)
	public List<RejectedBallot> rejectedBallots(UserData userData, ApprovedFinalCountRef approvedFinalCountRef) {
		VoteCount voteCount = voteCountService.findApprovedFinalVoteCount(approvedFinalCountRef, userData.getOperatorAreaPath());
		return rejectedBallots(voteCount);
	}

	private List<RejectedBallot> rejectedBallots(VoteCount voteCount) {
		List<RejectedBallot> rejectedBallots = new ArrayList<>();
		for (BallotCount ballotCount : voteCount.getRejectedBallotCountMap().values()) {
			for (CastBallot castBallot : ballotCount.getCastBallots()) {
				rejectedBallots.add(new RejectedBallot(castBallot.getId(), castBallot.getBallotCount().getBallotRejectionId()));
			}
		}
		return rejectedBallots;
	}

	@Override
	@Security(accesses = Opptelling_Forkastelser_Skannet, type = READ)
	public List<ApprovedBallot> approvedBallots(UserData userData, ApprovedFinalCountRef approvedFinalCountRef) {
		VoteCount voteCount = voteCountService.findApprovedFinalVoteCount(approvedFinalCountRef, userData.getOperatorAreaPath());
		return approvedBallots(voteCount);
	}

	private List<ApprovedBallot> approvedBallots(VoteCount voteCount) {
		List<ApprovedBallot> approvedBallots = new ArrayList<>();
		for (BallotCount ballotCount : voteCount.getBallotCountMap().values()) {
			for (CastBallot castBallot : ballotCount.getCastBallots()) {
				boolean modified = castBallot.getType() == MODIFIED;
				approvedBallots.add(new ApprovedBallot(castBallot.getId(), castBallot.getBallotCount().getBallotId(), modified));
			}
		}
		return approvedBallots;
	}

	@Override
	@Security(accesses = Opptelling_Forkastelser_Skannet, type = WRITE)
	@AuditLog(eventClass = FinalCountAuditEvent.class, eventType = AuditEventTypes.ProcessRejectedBallots)
	public void processRejectedBallots(UserData userData, ApprovedFinalCountRef approvedFinalCountRef, List<RejectedBallot> rejectedBallots) {
		castBallotDomainService.processRejectedBallots(userData, approvedFinalCountRef, rejectedBallots);
	}
}
