package no.valg.eva.admin.common.rbac;

import java.io.Serializable;

import no.evote.util.Treeable;

/**
 * RBAC: Hierarchy of access to securable objects
 */
public class Access implements Serializable, Treeable {

	private Access parent;
	private final String path;
	private String name;

	public Access(String path) {
		this(null, path);
	}

	public Access(Access parent, String path) {
		if (parent == null) {
			if (path.indexOf('.') == -1) {
				this.path = path;
			} else {
				int index = path.lastIndexOf('.');
				this.parent = new Access(path.substring(0, index));
				this.path = this.parent.getPath() + "." + path.substring(index + 1);
			}
		} else {
			this.parent = parent;
			this.path = parent.getPath() + "." + path;
		}
		this.name = "@access." + path;
	}

	@Override
	public String getPath() {
		return this.path;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public Access getParent() {
		return parent;
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj instanceof Access) {
			return ((Access) obj).getPath().equals(this.getPath());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return getPath().hashCode();
	}

	@Override
	public String toString() {
		return path;
	}

}
