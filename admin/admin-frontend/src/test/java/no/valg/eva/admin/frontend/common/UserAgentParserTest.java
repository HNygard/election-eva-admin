package no.valg.eva.admin.frontend.common;

import static org.assertj.core.api.Assertions.assertThat;

import no.valg.eva.admin.BaseFrontendTest;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;


public class UserAgentParserTest extends BaseFrontendTest {

	private static final String USER_AGENT_IE_8 = "Mozilla/5.0 (compatible; MSIE 8.0; Windows NT 6.1; Trident/4.0; GTB7.4; InfoPath.2; SV1; .NET CLR 3.3.69573; WOW64; "
			+ "en-US)";
	public static final String USER_AGENT_IE_9 = "Mozilla/5.0 (Windows; U; MSIE 9.0; WIndows NT 9.0; en-US))";
	public static final String USER_AGENT_IE_10 = "Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.1; WOW64; Trident/6.0)";
	private static final String USER_AGENT_IE_11 = "Mozilla/5.0 (Windows NT 6.1; WOW64; Trident/7.0; AS; rv:11.0) like Gecko";
	private static final String USER_AGENT_CHROME_8 = "Mozilla/5.0 (Windows; U; Windows NT 5.2; en-US) AppleWebKit/534.10 (KHTML, like Gecko) Chrome/8.0.558.0 "
			+ "Safari/534.10";
	private static final String USER_AGENT_CHROME_40 = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/40.0.2214.115 "
			+ "Safari/537.36";
	private static final String USER_AGENT_SAFARI_8 = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_2) AppleWebKit/600.3.18 "
		+ "(KHTML, like Gecko) Version/8.0.3 Safari/600.3.18";
	private static final String USER_AGENT_FIREFOX_8 = "Mozilla/5.0 (Windows NT 5.1; rv:8.0; en_us) Gecko/20100101 Firefox/8.0";
	private static final String USER_AGENT_FIREFOX_36 = "Mozilla/5.0 (Windows NT 6.3; rv:36.0) Gecko/20100101 Firefox/36.0";

	@Test(dataProvider = "parseDataProvider")
	public void parse_withDataProvider_verifyExpected(String userAgentString, boolean msie, boolean chrome, boolean safari, boolean firefox, int versionMajor,
			boolean
			isVersionLowerThan10)
			throws Exception {
		UserAgentParser userAgentParser = initializeMocks(UserAgentParser.class);
		userAgentParser.init();
		getServletContainer().setHeader("User-Agent", userAgentString);

		UserAgent userAgent = userAgentParser.parse(getFacesContextMock());

		assertThat(userAgent.isMSIE()).isEqualTo(msie);
		assertThat(userAgent.isChrome()).isEqualTo(chrome);
		assertThat(userAgent.isSafari()).isEqualTo(safari);
		assertThat(userAgent.isForefox()).isEqualTo(firefox);
		assertThat(userAgent.getVersionMajor()).isEqualTo(versionMajor);
		assertThat(userAgent.isVersionLowerThan(10)).isEqualTo(isVersionLowerThan10);
	}

	@DataProvider(name = "parseDataProvider")
	public static Object[][] parseDataProvider() {
		return new Object[][] {
				{ USER_AGENT_IE_8, true, false, false, false, 8, true },
				{ USER_AGENT_IE_9, true, false, false, false, 9, true },
				{ USER_AGENT_IE_10, true, false, false, false, 10, false },
				{ USER_AGENT_IE_11, true, false, false, false, 11, false },
				{ USER_AGENT_CHROME_8, false, true, false, false, 8, true },
				{ USER_AGENT_CHROME_40, false, true, false, false, 40, false },
				{ USER_AGENT_SAFARI_8, false, false, true, false, 8, true },
				{ USER_AGENT_FIREFOX_8, false, false, false, true, 8, true },
				{ USER_AGENT_FIREFOX_36, false, false, false, true, 36, false }
		};
	}
}

