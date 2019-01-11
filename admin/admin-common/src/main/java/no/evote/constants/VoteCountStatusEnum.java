package no.evote.constants;

public enum VoteCountStatusEnum {

	NONE(-1), COUNTING(0), TO_APPROVAL(1), APPROVED(2), TO_SETTLEMENT(3), REJECTED(4);

	private final int status;

	VoteCountStatusEnum(final int status) {
		this.status = status;
	}

	public static VoteCountStatusEnum getStatus(final int status) {
		for (VoteCountStatusEnum voteCountStatusEnum : VoteCountStatusEnum.values()) {
			if (status == voteCountStatusEnum.getStatus()) {
				return voteCountStatusEnum;
			}
		}

		return NONE;
	}

	public int getStatus() {
		return status;
	}

	public boolean lowerThan(VoteCountStatusEnum otherStatus) {
		return this.status < otherStatus.status;
	}
}
