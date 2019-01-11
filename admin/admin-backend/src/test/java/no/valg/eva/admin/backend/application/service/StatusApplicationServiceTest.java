package no.valg.eva.admin.backend.application.service;

import no.evote.util.VersionResourceStreamProvider;
import no.valg.eva.admin.test.MockUtilsTestCase;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.sql.DataSource;
import java.io.ByteArrayInputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import static org.apache.commons.codec.binary.StringUtils.getBytesIso8859_1;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class StatusApplicationServiceTest extends MockUtilsTestCase {

	@Test(dataProvider = "statusData")
	public void getStatus_givenSystemStatus_returnsAppropriateTextMessages(boolean isSystempwdSet, boolean isDbConnectionOk, String expectedResult) throws Exception {
		StatusApplicationService statusService = initializeMocks(StatusApplicationService.class);
		when(getInjectMock(SystemPasswordApplicationService.class).isPasswordSet()).thenReturn(isSystempwdSet);
		stubDbConnection(isDbConnectionOk);

		assertThat(statusService.getStatus()).isEqualTo(expectedResult);
	}
	
	@DataProvider
	private Object[][] statusData() {
		return new Object[][] {	
			{ true,  true,  StatusApplicationService.OK },
			{ false, true,  StatusApplicationService.SYSTEM_PWD_NOT_SET },
			{ true,  false, StatusApplicationService.DATABASE_UNAVAILABLE }
		};
	}

	@Test(dataProvider = "statusPropertiesData")
	public void getStatusAndConfiguredVersionProperties_givenSystemStatus_returnsAppropriatePropertyValues(boolean isSystempwdSet, boolean isDbConnectionOk) throws Exception {
		StatusApplicationService statusService = initializeMocks(StatusApplicationService.class);
		when(getInjectMock(SystemPasswordApplicationService.class).isPasswordSet()).thenReturn(isSystempwdSet);
		stubDbConnection(isDbConnectionOk);
		when(getInjectMock(VersionResourceStreamProvider.class).getVersionPropertiesInputStream()).thenReturn(new ByteArrayInputStream(
			getBytesIso8859_1("property1=value1\nproperty2=value2\n")));

		Properties properties = statusService.getStatusAndConfiguredVersionProperties();
		
		assertThat(properties.getProperty("backend-systemPasswordSet")).isEqualTo(Boolean.toString(isSystempwdSet));
		assertThat(properties.getProperty("backend-databaseConnectionOk")).isEqualTo(Boolean.toString(isDbConnectionOk));
		assertThat(properties.getProperty("backend-property1")).isEqualTo("value1");
		assertThat(properties.getProperty("backend-property2")).isEqualTo("value2");
	}

	@DataProvider
	private Object[][] statusPropertiesData() {
		return new Object[][] {
			{ true,  true },
			{ false, true },
			{ true,  false }
		};
	}

	private void stubDbConnection(boolean shallSucceed) throws SQLException {
		DataSource dataSourceStub = getInjectMock(DataSource.class);
		Connection connectionStub = mock(Connection.class);
		when(dataSourceStub.getConnection()).thenReturn(connectionStub);
		Statement statementStub = mock(Statement.class);
		when(connectionStub.createStatement()).thenReturn(statementStub);
		ResultSet resultSetStub = mock(ResultSet.class);

		if (shallSucceed) {
			when(statementStub.executeQuery(anyString())).thenReturn(resultSetStub);
			when(resultSetStub.next()).thenReturn(true);
			when(resultSetStub.getString(1)).thenReturn("ping");
		} else {
			when(statementStub.executeQuery(anyString())).thenThrow(new RuntimeException());
		}
	}

}
