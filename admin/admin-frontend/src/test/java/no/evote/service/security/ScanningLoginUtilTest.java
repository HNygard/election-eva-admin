package no.evote.service.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.servlet.http.HttpServletRequest;

import org.testng.annotations.Test;

public class ScanningLoginUtilTest {

	@Test
	public void isScanningLogin_withScanningURL_returnsTrue() throws Exception {
		HttpServletRequest request = getHttpServletRequest("scanningLoginSelectElectionEvent.xhtml");

		assertThat(ScanningLoginUtil.isScanningLogin(request)).isTrue();
	}

	@Test
	public void isScanningLogin_withoutScanningURL_returnsFalse() throws Exception {
		HttpServletRequest request = getHttpServletRequest("someOtherURL.xhtml");

		assertThat(ScanningLoginUtil.isScanningLogin(request)).isFalse();
	}

	@Test
	public void startPage_returnsStartPage() throws Exception {
		assertThat(ScanningLoginUtil.startPage()).isEqualTo("scanningLoginSelectElectionEvent.xhtml");
	}

	private HttpServletRequest getHttpServletRequest(String servletPath) {
		HttpServletRequest stub = mock(HttpServletRequest.class, RETURNS_DEEP_STUBS);
		when(stub.getServletPath()).thenReturn(servletPath);
		return stub;
	}
}
