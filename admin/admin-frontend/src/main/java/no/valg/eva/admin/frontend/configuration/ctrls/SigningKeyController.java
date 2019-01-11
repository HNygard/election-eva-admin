package no.valg.eva.admin.frontend.configuration.ctrls;

import com.google.common.io.ByteStreams;
import no.evote.model.KeyDomain;
import no.evote.model.SigningKey;
import no.valg.eva.admin.frontend.util.MessageUtil;
import no.evote.security.UserData;
import no.evote.service.security.SigningKeyService;
import no.valg.eva.admin.common.configuration.model.SigningKeyData;
import no.valg.eva.admin.common.configuration.service.ElectionEventService;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;
import no.valg.eva.admin.crypto.Pkcs12Decoder;
import no.valg.eva.admin.frontend.BaseController;
import org.apache.log4j.Logger;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.primefaces.model.UploadedFile;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.InputStream;
import java.security.Security;
import java.util.List;

@Named
@ViewScoped
public class SigningKeyController extends BaseController {

	private static final Logger LOGGER = Logger.getLogger(SigningKeyController.class);

	// Injected
	private SigningKeyService signingKeyService;
	private ElectionEventService electionEventService;
	private UserData userData;

	private List<SigningKeyData> signingKeys;
	private ElectionEvent currentElectionEvent;
	private KeyDomain keyDomain;
	private SigningKey signingKey;
	private UploadedFile file;
	private String password;

	public SigningKeyController() {
	}

	@Inject
	public SigningKeyController(UserData userData, ElectionEventService electionEventService, SigningKeyService signingKeyService) {
		this.userData = userData;
		this.electionEventService = electionEventService;
		this.signingKeyService = signingKeyService;
	}

	@PostConstruct
	public void init() {
		signingKeys = signingKeyService.findAllSigningKeys(userData);
		file = null;
		password = null;
	}

	/**
	 * Set data for selected row and go the upload page
	 */
	public void add(SigningKeyData signingKeyData) {
		signingKey = new SigningKey();
		currentElectionEvent = electionEventService.findById(userData, signingKeyData.getElectionEventId());
		keyDomain = signingKeyService.findKeyDomainById(userData, signingKeyData.getKeyDomainId());
		signingKey.setElectionEvent(currentElectionEvent);
		signingKey.setKeyDomain(keyDomain);
	}

	/**
	 * Set data for selected row and go the upload page
	 */
	public void edit(SigningKeyData signingKeyData) {
		signingKey = signingKeyService.findSigningKeyByPk(userData, signingKeyData.getSigningKeyPk());
		currentElectionEvent = electionEventService.findByPk(signingKey.getElectionEvent().getPk());
		keyDomain = signingKey.getKeyDomain();
	}

	public void save() {
		try {
			if (!validate()) {
				MessageUtil.buildDetailMessage("@config.certificate_management.error.wrong_pass", FacesMessage.SEVERITY_ERROR);
				return;
			}
			byte[] bytes = ByteStreams.toByteArray(file.getInputstream());
			signingKey = signingKeyService.create(userData, signingKey, bytes, file.getFileName(), password, currentElectionEvent);
			init();
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			MessageUtil.buildDetailMessage("@config.certificate_management.error.error", FacesMessage.SEVERITY_ERROR);
		}
	}

	private boolean validate() {
		if (signingKey.getKeyDomain().getPublicKey()) {
			return true;
		}
		synchronized (SigningKeyController.class) {
			Security.removeProvider("BC");
			Security.addProvider(new BouncyCastleProvider());
		}
		try (InputStream ins = file.getInputstream()) {
			Pkcs12Decoder pkcs12Decoder = getPkcs12Decoder();
			return pkcs12Decoder.validerPassord(ins, password);
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			return false;
		}
	}

	Pkcs12Decoder getPkcs12Decoder() {
		return new Pkcs12Decoder();
	}

	public List<SigningKeyData> getSigningKeys() {
		return signingKeys;
	}

	public ElectionEvent getElectionEvent() {
		return currentElectionEvent;
	}

	public KeyDomain getKeyDomain() {
		return keyDomain;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(final String password) {
		this.password = password;
	}

	public UploadedFile getFile() {
		return file;
	}

	public void setFile(final UploadedFile file) {
		this.file = file;
	}
}
