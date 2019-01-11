package no.evote.security;

import java.io.Serializable;

import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Named;

@Named("userDataProducer")
@SessionScoped
public class UserDataProducer implements Serializable {

	private UserData userData;

	@Produces
	public UserData getUserData() {
		return userData;
	}

	public void setUserData(final UserData userData) {
		this.userData = userData;
	}

}
