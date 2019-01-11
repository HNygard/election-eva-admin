package no.valg.eva.admin.backend.reporting.jasperserver.api;

import static java.util.Collections.EMPTY_LIST;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Mapping for jasperServer rest API
 */
@XmlRootElement(name = "resources")
public class JasperResources {
	private List<JasperReport> resources;

	public JasperResources() {
	}

	public JasperResources(final List<JasperReport> jasperReports) {
		this.resources = jasperReports;
	}

	public static JasperResources empty() {
		return new JasperResources() {
			@Override
			public List<JasperReport> getResources() {
				return EMPTY_LIST;
			}
		};
	}

	@XmlElement(name = "resourceLookup")
	public List<JasperReport> getResources() {
		return resources;
	}

	public void setResources(final List<JasperReport> resources) {
		this.resources = resources;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		JasperResources that = (JasperResources) o;
		if (resources != null ? !resources.equals(that.resources) : that.resources != null) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		return resources != null ? resources.hashCode() : 0;
	}
}
