package no.evote.security;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import no.valg.eva.admin.common.rbac.Accesses;

import org.apache.commons.lang3.StringUtils;

public class AccessCache implements Serializable {

	private transient Set<String> securableObjects;
	private final String securableObjectsAsString;
	private final byte[] signature;
	
	public AccessCache(final Set<String> securableObjects, final byte[] signature) {
		this.securableObjects = securableObjects;
		this.securableObjectsAsString = StringUtils.join(securableObjects.toArray(new String[] {}), ",");
		if (signature != null) {
			this.signature = signature.clone();
		} else {
			this.signature = null;
		}
	}

	public boolean hasAccess(Accesses... accesses) {
		for (Accesses access : accesses) {
			if (hasSingleAccess(access)) {
				return true;
			}
		}
		return false;
	}

	private boolean hasSingleAccess(Accesses access) {
		for (String path : access.paths()) {
			if (securableObjects.contains(path)) {
				return true;
			}
		}
		return false;
	}

	public byte[] getSignature() {
		return signature;
	}

	public String getSecurableObjectsAsString() {
		return securableObjectsAsString;
	}

	private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		securableObjects = new HashSet<>(Arrays.asList(securableObjectsAsString.split(",")));
	}
}
