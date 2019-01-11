package no.valg.eva.admin.frontend.common;

import no.evote.exception.ErrorCode;

/**
 * Error code handler to be used in conjunction with process(RuntimeException...) in BaseController.
 */
public interface ErrorCodeHandler {
	/**
	 * Called with RuntimeException in process method is of type EvoteException and it has an ErrorCode.
	 * 
	 * @param errorCode The error code from EvoteException.
	 *                     
	 * @return Resolved error message to be displayed, or null if errorCode not handled.
	 */
	String onError(ErrorCode errorCode, String...params);
}
