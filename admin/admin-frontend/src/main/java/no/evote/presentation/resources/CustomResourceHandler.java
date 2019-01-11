package no.evote.presentation.resources;

import javax.faces.application.Resource;
import javax.faces.application.ResourceHandler;

public class CustomResourceHandler extends javax.faces.application.ResourceHandlerWrapper {
	private final ResourceHandler wrapped;

	public CustomResourceHandler(final ResourceHandler wrapped) {
		this.wrapped = wrapped;
	}

	@Override
	public ResourceHandler getWrapped() {
		return this.wrapped;
	}

	@Override
	public Resource createResource(final String resourceName, final String libraryName) {
		Resource resource = super.createResource(resourceName, libraryName);

		if (resource != null && ("css".equals(libraryName) || "javascript".equals(libraryName) || resource.getRequestPath().contains("/javascript/"))) {
			return new VersionedResource(resource);
		}

		return resource;
	}
}
