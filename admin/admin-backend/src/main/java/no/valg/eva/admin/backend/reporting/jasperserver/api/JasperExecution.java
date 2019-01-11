package no.valg.eva.admin.backend.reporting.jasperserver.api;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "reportExecution")
public class JasperExecution {
	public static final String READY = "ready";
	private int currentPage;
	private String reportURI;
	private String requestId;
	private String status;
	private Integer totalPages;

	public JasperExecution() {
	}

	public JasperExecution(final int currentPage, final String reportURI, final String requestId, final String status, final List<Export> exports) {
		this.currentPage = currentPage;
		this.reportURI = reportURI;
		this.requestId = requestId;
		this.status = status;
		this.exports = exports;
	}

	public int getCurrentPage() {
		return currentPage;
	}

	public void setCurrentPage(final int currentPage) {
		this.currentPage = currentPage;
	}

	public String getReportURI() {
		return reportURI;
	}

	public void setReportURI(final String reportURI) {
		this.reportURI = reportURI;
	}

	public String getRequestId() {
		return requestId;
	}

	public void setRequestId(final String requestId) {
		this.requestId = requestId;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(final String status) {
		this.status = status;
	}

	public boolean isReady() {
		if (!READY.equals(getStatus())) {
			return false;
		}
		for (Export export : exports) {
			if (!READY.equals(export.getStatus())) {
				return false;
			}
		}
		return true;
	}

	@XmlElementWrapper(name = "exports")
	@XmlElement(name = "export")
	public List<Export> getExports() {
		return exports;
	}

	public void setExports(final List<Export> exports) {
		this.exports = exports;
	}

	public Integer getTotalPages() {
		return totalPages;
	}

	public void setTotalPages(final Integer totalPages) {
		this.totalPages = totalPages;
	}

	private List<JasperExecution.Export> exports;

	public String getExportId() {
		for (Export export : exports) {
			return export.getId();
		}
		return null;
	}

	public static class Export {
		private String id;
		private String status;

		public Export() {
		}

		public Export(final String id, final String status) {
			this.id = id;
			this.status = status;
		}

		public String getId() {
			return id;
		}

		public void setId(final String id) {
			this.id = id;
		}

		public String getStatus() {
			return status;
		}

		public void setStatus(final String status) {
			this.status = status;
		}
	}

}
