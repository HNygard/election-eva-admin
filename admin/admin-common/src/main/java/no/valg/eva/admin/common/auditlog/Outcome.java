package no.valg.eva.admin.common.auditlog;

public enum Outcome {
	Success(true),
	/**
	 * Asynchronous operation invoked - unknown outcome
	 */
	UnknownOperator(false),
	GenericError(false);

	private boolean isSuccess;

	private Outcome(boolean isSuccess) {
		this.isSuccess = isSuccess;
	}

	public boolean isSuccess() {
		return isSuccess;
	}
}
