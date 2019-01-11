package no.valg.eva.admin.backend.reporting.jasperserver.api;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessOrder;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorOrder;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.collect.Lists;

@XmlRootElement(name = "status")
@XmlAccessorOrder(XmlAccessOrder.ALPHABETICAL)
@XmlAccessorType(XmlAccessType.FIELD)
public class ReportExecutionStatus {
	private Status value;
	private ErrorDescriptor errorDescriptor;

	public Status getValue() {
		return value;
	}

	public void setValue(Status value) {
		this.value = value;
	}

	public ErrorDescriptor getErrorDescriptor() {
		return errorDescriptor;
	}

	public void setErrorDescriptor(ErrorDescriptor errorDescriptor) {
		this.errorDescriptor = errorDescriptor;
	}

	public static class ErrorDescriptor {
		private String errorCode;
		private String message;
		private List<String> parameters;

		public ErrorDescriptor() {
		}

		public ErrorDescriptor(String errorCode, String message, List<String> parameters) {
			this.errorCode = errorCode;
			this.message = message;
			this.parameters = Lists.newArrayList(parameters);
		}

		public String getErrorCode() {
			return errorCode;
		}

		public void setErrorCode(String errorCode) {
			this.errorCode = errorCode;
		}

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}

		@XmlElementWrapper(name = "parameters")
		@XmlElement(name = "parameter")
		public List<String> getParameters() {
			return parameters;
		}

		public void setParameters(List<String> parameters) {
			this.parameters = parameters;
		}
	}

	@XmlRootElement
	public enum Status {
		@XmlEnumValue("failed")
		FAILED,
		@XmlEnumValue("ready")
		READY,
		@XmlEnumValue("execution")
		EXECUTION
	}
}
