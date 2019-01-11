package no.valg.eva.admin.frontend.util;

import no.evote.model.BinaryData;
import no.evote.security.UserData;
import no.valg.eva.admin.BaseFrontendTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.el.ELContext;
import javax.el.ValueExpression;
import javax.faces.FacesException;
import javax.faces.context.ExternalContext;
import javax.servlet.ServletContext;
import java.util.Arrays;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class FacesUtilTest extends BaseFrontendTest {

	private static final String SEND_FILE_FILENAME = "file.txt";
	private static final String SEND_FILE_CONTENT = "content";
	private static final String SEND_FILE_CONTENT_TYPE = "application/force-download";

	@BeforeMethod
	public void setUp() throws Exception {
		initializeMocks();
		FacesUtil.setContext(getFacesContextMock());
		FacesUtil.setRequestContext(getRequestContextMock());
		when(getFacesContextMock().getApplication().evaluateExpressionGet(eq(getFacesContextMock()), eq("#{userDataProducer.userData}"), any(Class.class)))
				.thenReturn(getUserDataMock());
	}

	@Test
    public void resolveExpression_withExpression_shouldReturnString() {
		ValueExpression valueExpressionStub = createMock(ValueExpression.class);
		when(getFacesContextMock().getApplication().getExpressionFactory().createValueExpression(any(ELContext.class), anyString(), any(Class.class)))
				.thenReturn(valueExpressionStub);
		when(valueExpressionStub.getValue(any(ELContext.class))).thenReturn("string");

		String result = (String) FacesUtil.resolveExpression("#{cc.attrs.topLevelFilter}");

		assertThat(result).isEqualTo("string");
	}

	@Test
    public void getUserData_withMock_returnMock() {
		UserData userData = FacesUtil.getUserData();

		assertThat(userData).isNotNull();
		assertThat(userData).isSameAs(getUserDataMock());
	}

	@Test
	public void sendFile_withStringAndBytes_verifyResponseOperations() throws Exception {
		FacesUtil.sendFile(SEND_FILE_FILENAME, SEND_FILE_CONTENT.getBytes());

		verifySendFile();
	}

	@Test
	public void sendFile_withBinaryData_verifyResponseOperations() throws Exception {
		BinaryData binaryData = new BinaryData();
		binaryData.setFileName(SEND_FILE_FILENAME);
		binaryData.setBinaryData(SEND_FILE_CONTENT.getBytes());
		binaryData.setMimeType(SEND_FILE_CONTENT_TYPE);

		FacesUtil.sendFile(binaryData);
	}

	@Test
    public void setSessionAttribute_withKeyValue_shouldCallSessionWithKeyValue() {
		FacesUtil.setSessionAttribute("key", "value");

		verify(getServletContainer().getHttpSessionMock()).setAttribute("key", "value");
	}

	@Test
	public void redirect_withEncode_verifyRedirectWithEncodedURL() throws Exception {
        when(getFacesContextMock().getExternalContext().encodeActionURL(any())).thenReturn("/encoded");

		FacesUtil.redirect("/url", true);

		verify(getFacesContextMock().getExternalContext()).redirect("/encoded");
	}

	@Test
	public void redirect_withoutEncode_verifyRedirectWithURL() throws Exception {
		FacesUtil.redirect("/url", false);

		verify(getFacesContextMock().getExternalContext()).redirect("/url");
	}

	@Test(expectedExceptions = FacesException.class, expectedExceptionsMessageRegExp = "java.io.IOException: IOException")
	public void redirect_withIOException_expectFacesException() throws Exception {
		ExternalContext ctx = getFacesContextMock().getExternalContext();
		ioException().when(ctx).redirect("/url");

		FacesUtil.redirect("/url", false);
	}

	@Test
    public void getIntFromStringOrInteger_withString1_shouldReturn1() {
		int i = FacesUtil.getIntFromStringOrInteger("1");

		assertThat(i).isEqualTo(1);
	}

	@Test
    public void getIntFromStringOrInteger_withInteger1_shouldReturn1() {
		int i = FacesUtil.getIntFromStringOrInteger(1);

		assertThat(i).isEqualTo(1);
	}

	@Test(expectedExceptions = IllegalStateException.class, expectedExceptionsMessageRegExp = "Object is null, unable to convert to integer value")
    public void getIntFromStringOrInteger_withNull_shouldThrowIllegalStateException() {
		FacesUtil.getIntFromStringOrInteger(null);
	}

	@Test(expectedExceptions = IllegalStateException.class, expectedExceptionsMessageRegExp = "Object is of java.lang.Double type, unable to convert to integer value")
    public void getIntFromStringOrInteger_withDouble_shouldThrowIllegalStateException() {
		FacesUtil.getIntFromStringOrInteger(1D);
	}

	@Test
    public void getServletContext_withServletContextMock_verifySame() {
		ServletContext ctx = FacesUtil.getServletContext();

		assertThat(ctx).isSameAs(getServletContainer().getServletContextMock());
	}

	@Test
    public void updateDom_withName_verifyUpdate() {
		FacesUtil.updateDom("source");

		verify(getRequestContextMock()).update("source");
	}

	@Test
    public void updateDom_withNames_verifyUpdate() {
		Collection<String> names = Arrays.asList("1", "2");

		FacesUtil.updateDom(names);

		verify(getRequestContextMock()).update(names);
	}

	@Test
    public void executeJS_withJS_verifyExecute() {
		FacesUtil.executeJS("test()");

		verify(getRequestContextMock()).execute("test()");
	}

	private void verifySendFile() throws Exception {
		verify(getServletContainer().getResponseMock()).setContentType(SEND_FILE_CONTENT_TYPE);
		verify(getServletContainer().getResponseMock()).addHeader("Content-Disposition", "attachment; filename=\"" + SEND_FILE_FILENAME + "\"");
		verify(getServletContainer().getResponseMock()).setContentLength(SEND_FILE_CONTENT.getBytes().length);
		verify(getServletContainer().getResponseMock().getOutputStream()).write(SEND_FILE_CONTENT.getBytes());
		verify(getServletContainer().getResponseMock().getOutputStream()).close();
		verify(getFacesContextMock()).responseComplete();
	}

}
