package no.evote.util;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;

@Startup
@Singleton
public class Log4jConfigurator {

	@PostConstruct
	public void init() {
		Log4jUtil.configure(this.getClass().getClassLoader());
	}
}
