package no.evote.service.security;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SystemPasswordStore {
	private String password;

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public boolean hasPassword() {
		return password != null;
	}
}
