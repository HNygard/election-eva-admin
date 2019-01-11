package no.evote.service.web.exception;

import javax.ejb.ApplicationException;
import javax.xml.ws.WebFault;

import no.evote.exception.ErrorCode;
import no.evote.exception.EvoteException;
import no.evote.exception.EvoteNoRollbackException;

@ApplicationException(rollback = true)
@WebFault(name = "EvoteWsFault")
public class EvoteWsException extends Exception {
	private EvoteWsFault fault;
	
	public EvoteWsException(ErrorCode errorCode, Throwable cause, Object... messageParameters) {
		super(errorCode.formatMessage(messageParameters), cause);
		this.fault = new EvoteWsFault(errorCode.getCode(), getMessage());
	}

	public EvoteWsException(EvoteException evoteException) {
		super(evoteException.getMessage(), evoteException);
		this.fault = new EvoteWsFault(evoteException.getCode(), evoteException.getMessage());
	}
	
	public EvoteWsException(EvoteNoRollbackException evoteNoRollbackException) {
		super(evoteNoRollbackException.getMessage(), evoteNoRollbackException);
		this.fault = new EvoteWsFault(evoteNoRollbackException.getCode(), evoteNoRollbackException.getMessage());
	}
	
	public EvoteWsFault getFaultInfo() {
		return fault;
	}
}
