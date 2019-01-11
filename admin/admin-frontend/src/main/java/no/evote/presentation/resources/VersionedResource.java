package no.evote.presentation.resources;

import javax.faces.application.Resource;

import no.valg.eva.admin.frontend.util.VersionProperties;

public class VersionedResource extends javax.faces.application.ResourceWrapper {
	private final javax.faces.application.Resource resource;
	private static final VersionProperties PROPS = new VersionProperties();

	public VersionedResource(final Resource resource) {
		this.resource = resource;
	}

	@Override
	public Resource getWrapped() {
		return this.resource;
	}

	@Override
	public String getRequestPath() {
		String requestPath = resource.getRequestPath();

		// get current revision
		String revision = PROPS.getVersion();
		StringBuilder newRequestPath = new StringBuilder(requestPath);

		if (requestPath.contains("?")) {
			newRequestPath.append("&rv=");
		} else {
			newRequestPath.append("?rv=");
		}
		newRequestPath.append(revision);

		return newRequestPath.toString();
	}

	@Override
	public String getContentType() {
		return getWrapped().getContentType();
	}
}
