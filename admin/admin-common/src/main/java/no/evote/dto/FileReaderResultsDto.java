package no.evote.dto;

import java.io.Serializable;

import javax.faces.bean.NoneScoped;

@NoneScoped
public class FileReaderResultsDto implements Serializable {

	private Integer noOfNotReadEntries = 0;
	private Integer noOfReadEntries = 0;
	private Integer noOfStoredEntries = 0;
	private Integer noOfNotStoredEntries = 0;
	private StringBuilder exceptionMessage = null;
	private final String newline;

	public FileReaderResultsDto() {
		newline = System.getProperty("line.separator");
	}
 
	public void incrementNotReadEntries() {
		noOfNotReadEntries++;
	}

	public void incrementReadEntries() {
		noOfReadEntries++;
	}

	public void incrementStoredEntries() {
		noOfStoredEntries++;
	}

	public void incrementNotStoredEntries() {
		noOfNotStoredEntries++;
	}

	public Integer getNoOfNotReadEntries() {
		return noOfNotReadEntries;
	}

	public Integer getNoOfReadEntries() {
		return noOfReadEntries;
	}

	public Integer getNoOfStoredEntries() {
		return noOfStoredEntries;
	}

	public Integer getNoOfNotStoredEntries() {
		return noOfNotStoredEntries;
	}

	public Boolean hasFailedEntries() {
		return !((noOfNotReadEntries == 0) && (noOfNotStoredEntries == 0));
	}

	public Boolean hasExceptionErrors() {
		if (exceptionMessage == null) {
			return Boolean.FALSE;
		}
		return Boolean.TRUE;
	}

	public StringBuilder getExceptionMessage() {
		return exceptionMessage;
	}

	public void addExceptionMessage(final StringBuilder exceptionMsg) {
		if (exceptionMessage == null) {
			this.exceptionMessage = new StringBuilder(exceptionMsg);
			this.exceptionMessage.append(newline);
		} else {
			this.exceptionMessage.append(exceptionMsg).append(newline);
		}
	}
}
