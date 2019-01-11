package no.valg.eva.admin.frontend.common;

import net.sf.uadetector.ReadableUserAgent;

public class UserAgent {

	private ReadableUserAgent readableUserAgent;
	private String familyName;
	private int versionMajor = -1;

	public UserAgent(ReadableUserAgent readableUserAgent) {
		this.readableUserAgent = readableUserAgent;
		this.familyName = readableUserAgent.getFamily().getName();
		try {
			this.versionMajor = Integer.parseInt(readableUserAgent.getVersionNumber().getMajor());
		} catch (Exception e) {
			;
		}
	}

	public boolean isMSIE() {
		return "IE".equals(familyName);
	}

	public boolean isChrome() {
		return "Chrome".equals(familyName);
	}

	public boolean isSafari() {
		return "Safari".equals(familyName);
	}

	public boolean isForefox() {
		return "Firefox".equals(familyName);
	}

	public int getVersionMajor() {
		return versionMajor;
	}

	public boolean isVersionHigherThan(int version) {
		return versionMajor > version;
	}

	public boolean isVersionLowerThan(int version) {
		return versionMajor < version;
	}

	@Override
	public String toString() {
		return readableUserAgent.toString();
	}
}
