package no.valg.eva.admin.frontend.configuration.ctrls;

import no.evote.model.SigningKey;
import no.evote.security.UserData;
import no.evote.service.security.SigningKeyService;
import no.evote.util.MockUtils;
import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.common.configuration.model.SigningKeyData;
import no.valg.eva.admin.common.configuration.service.ElectionEventService;
import no.valg.eva.admin.crypto.Pkcs12Decoder;
import org.primefaces.model.UploadedFile;
import org.testng.annotations.Test;

import javax.faces.application.FacesMessage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SigningKeyControllerTest extends BaseFrontendTest {

	@Test
	public void init_verifyState() throws Exception {
		SigningKeyController ctrl = initializeMocks(SigningKeyController.class);

		ctrl.init();

		verify_init(ctrl, getInjectMock(SigningKeyService.class));
	}

	@Test
	public void add_withData_verifyAddState() throws Exception {
		SigningKeyController ctrl = initializeMocks(SigningKeyController.class);
		SigningKeyData data = createMock(SigningKeyData.class);

		ctrl.add(data);

		SigningKey signingKey = getPrivateField("signingKey", SigningKey.class);
		assertThat(signingKey).isNotNull();
		assertThat(ctrl.getElectionEvent()).isNotNull();
		assertThat(ctrl.getKeyDomain()).isNotNull();
		assertThat(signingKey.getElectionEvent()).isSameAs(ctrl.getElectionEvent());
		assertThat(signingKey.getKeyDomain()).isSameAs(ctrl.getKeyDomain());
	}

	@Test
	public void edit_withData_verifyEditState() throws Exception {
		SigningKeyController ctrl = initializeMocks(SigningKeyController.class);
		SigningKeyData data = createMock(SigningKeyData.class);

		ctrl.edit(data);

		SigningKey signingKey = getPrivateField("signingKey", SigningKey.class);
		assertThat(signingKey).isNotNull();
		assertThat(ctrl.getElectionEvent()).isNotNull();
		assertThat(ctrl.getKeyDomain()).isNotNull();
	}

	@Test
	public void save_withValidationError_returnsErrorMessage() throws Exception {
		SigningKeyController ctrl = initializeMocks(SigningKeyController.class);
		stub_signingKey(false);

		ctrl.save();

		assertFacesMessage(FacesMessage.SEVERITY_ERROR, "@config.certificate_management.error.wrong_pass");
	}

	@Test
	public void save_withNoValidationAndError_returnsErrorMessage() throws Exception {
		SigningKeyController ctrl = initializeMocks(SigningKeyController.class);
		stub_signingKey(true);
		ctrl.setFile(null);

		ctrl.save();

		assertFacesMessage(FacesMessage.SEVERITY_ERROR, "@config.certificate_management.error.error");
	}

	@Test
	public void save_withValidationOK_verifySave() throws Exception {
		SigningKeyController ctrl = initializeMocks(new Controller());
		when(ctrl.getPkcs12Decoder().validerPassord(any(InputStream.class), anyString())).thenReturn(true);
		stub_signingKey(false);
		UploadedFile file = createMock(UploadedFile.class);
		when(file.getInputstream()).thenReturn(new ByteArrayInputStream("test".getBytes()));
		ctrl.setFile(file);
		ctrl.setPassword("pw");

		ctrl.save();

		SigningKeyService signingKeyService = getPrivateField("signingKeyService", SigningKeyService.class);
		verify(signingKeyService).create(any(), any(), eq("test".getBytes()), any(), anyString(), any());
		verify_init(ctrl, signingKeyService);
	}

	private void stub_signingKey(boolean publicKey) throws Exception {
		SigningKey signingKey = createMock(SigningKey.class);
		when(signingKey.getKeyDomain().getPublicKey()).thenReturn(publicKey);
		MockUtils.setPrivateField(assertTestObject(), "signingKey", signingKey);
	}

	public class Controller extends SigningKeyController {

		private Pkcs12Decoder pkcs12Decoder;

		public Controller() {
			super(createMock(UserData.class), createMock(ElectionEventService.class), createMock(SigningKeyService.class));
			pkcs12Decoder = createMock(Pkcs12Decoder.class);
		}

		@Override
		Pkcs12Decoder getPkcs12Decoder() {
			return pkcs12Decoder;
		}
	}

	private void verify_init(SigningKeyController ctrl, SigningKeyService serviceMock) {
		verify(serviceMock).findAllSigningKeys(any(UserData.class));
		assertThat(ctrl.getPassword()).isNull();
		assertThat(ctrl.getFile()).isNull();
	}

}
