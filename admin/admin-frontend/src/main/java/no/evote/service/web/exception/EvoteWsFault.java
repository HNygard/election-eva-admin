package no.evote.service.web.exception;

/**
 * Used by JAX-WS to detail {@link no.evote.service.web.exception.EvoteWsException} in SOAP fault.
 */
public class EvoteWsFault {
	private String faultCode;
	private String faultString;

	public EvoteWsFault(String faultCode, String faultString) {
		this.faultCode = faultCode;
		this.faultString = faultString;
	}

	public String getFaultCode() {
		return faultCode;
	}

	public void setFaultCode(String faultCode) {
		this.faultCode = faultCode;
	}

	public String getFaultString() {
		return faultString;
	}

	public void setFaultString(String faultString) {
		this.faultString = faultString;
	}
}
