package no.evote.exception;

public class ModifiedBallotBatchCreationFailed extends EvoteException {

	protected static final String DEFAULT_MESSAGE = "Invalid modified ballot batch size requested";
	
	public ModifiedBallotBatchCreationFailed(int modifiedBallotBatchSizeRequested, int castVotesRemaining) {
		super(DEFAULT_MESSAGE, Integer.toString(modifiedBallotBatchSizeRequested), Integer.toString(castVotesRemaining));
	}
	
	public String getModifiedBallotBatchSizeRequested() {
		return params[0];
	}
	
	public String getModifiedBallotsRemaining() {
		return params[1];
	}
}
