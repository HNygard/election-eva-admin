package no.valg.eva.admin.frontend.util;


import no.valg.eva.admin.test.TestGroups;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

@Test(groups = TestGroups.RESOURCES)
public class VersionPropertiesTest {
	
	@DataProvider(name = "hostNames")
	public static Object[][] hostNames() {
		return new Object[][] {
				{ "admin-fe01.example.com", "fe01" },
				{ "qa-reg-admin-fe01.valg.no", "fe01" },
				{ "ci-admin-fe02", "fe02" },
				{ "dev-prod-admin-fe01.valg.no", "fe01" },
				{ "prod-admin-fe02.valg.no", "fe02" },
				{ "prod-osl-adm-fe101.valg.no", "fe101" }
		};
	}
	
	@Test(dataProvider = "hostNames")
	public void findHostId_givenAHostName_returnsTheRelevantLastPartOfTheName(String hostName, String expectedReturnValue) {
		String hostId = VersionProperties.findHostId(hostName);
		
		assertThat(hostId).isEqualTo(expectedReturnValue);
	}
	
	@DataProvider(name = "properties")
	public static Object[][] properties() {
		return new Object[][] {
				{ createProperties("4.0.14-SNAPSHOT", "1adade8", "master"), "4.0.14-SNAPSHOT-dev (master, 1adade8)" },
				{ createProperties("4.0.15-SNAPSHOT", "1234567", "EVAADMIN-123-A-BRANCH"), "4.0.15-SNAPSHOT-dev (EVAADMIN-123-A-BRANCH, 1234567)" },
				{ createProperties("4.0.15", "1adade8", "master"), "4.0.15 (master)" },
				{ createProperties("4.0.541", "abcdef9", "evaadmin-2015"), "4.0.541 (evaadmin-2015)" }
		};
	}
	
	@Test(dataProvider = "properties")
	public void getVersion_givenProperties_returnsAFormattedVersionStringWithVersionBuildNoBranchAndCommitId(Properties properties, String expected) {
		String result = VersionProperties.getVersion(properties);
		
		assertThat(result).isEqualTo(expected);
	}
	
	private static Properties createProperties(String version, String commitId, String branch) {
		Properties properties = new Properties();
		properties.put("version", version);
		properties.put("commitId", commitId);
		properties.put("branch", branch);
		return properties;
	}
}
