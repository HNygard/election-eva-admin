package no.evote.service.web;

import static no.evote.exception.ErrorCode.ERROR_CODE_9101_UNABLE_TO_GET_USER_DATA;
import static no.evote.exception.ErrorCode.ERROR_CODE_9104_ROLE_IS_INVALID;
import static no.evote.exception.ErrorCode.ERROR_CODE_9106_ACCESS_IS_INVALID;
import static no.valg.eva.admin.util.ExceptionUtil.unwrapThrowable;
import static no.valg.eva.admin.common.rbac.Accesses.Opptelling_Importer;

import java.net.UnknownHostException;

import javax.annotation.Resource;
import javax.ejb.EJBException;
import javax.inject.Inject;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.ws.WebServiceContext;

import no.evote.exception.ErrorCode;
import no.evote.exception.EvoteException;
import no.evote.exception.EvoteNoRollbackException;
import no.evote.model.Batch;
import no.evote.security.UserData;
import no.evote.service.BatchService;
import no.evote.service.LegacyUserDataService;
import no.evote.service.web.exception.EvoteWsException;
import no.valg.eva.admin.felles.bakgrunnsjobb.domain.model.Jobbkategori;
import no.valg.eva.admin.frontend.i18n.MessageProvider;

import org.apache.log4j.Logger;

@WebService
public class GenericFileUploadWS {
	private static final String AREA_PATH = "areaPath";
	private static final String ELECTION_PATH = "electionPath";
	private static final String USER_ID = "userId";
	private static final String ROLE_ID = "roleId";
	private static final String TOKEN = "token";
	private static final String FILE_ID = "fileId";
	private static final String STATUS = "status";
	private static final String FILE = "file";
	private static final String ACCESS = "access";
	private static final String BATCH_ID = "batchId";
	private static final Logger LOGGER = Logger.getLogger(GenericFileUploadWS.class);
	private static final int STATUS_OFFSET = 1000;

	@Inject
	private LegacyUserDataService legacyUserDataService;
	@Inject
	private BatchService batchService;
	@Resource
	private WebServiceContext wsContext;
	@Inject
	private MessageProvider messageProvider;

	@WebMethod
	@WebResult(name = FILE_ID)
	public long uploadFile(
			@WebParam(name = TOKEN) byte[] token,
			@WebParam(name = USER_ID) String userId,
			@WebParam(name = ROLE_ID) String roleId,
			@WebParam(name = AREA_PATH) String areaPath,
			@WebParam(name = ELECTION_PATH) String electionPath,
			@WebParam(name = FILE) byte[] file,
			@WebParam(name = ACCESS) String access)
			throws EvoteWsException, UnknownHostException {

		UserData userData = getUserData(userId, roleId, areaPath, electionPath);
		if (!userData.hasAccess(Opptelling_Importer)) {
			throw new EvoteWsException(ERROR_CODE_9104_ROLE_IS_INVALID, new IllegalArgumentException());
		}
		Jobbkategori category;
		try {
			category = Jobbkategori.fromAccessPath(access);
		} catch (IllegalArgumentException e) {
			throw new EvoteWsException(ERROR_CODE_9106_ACCESS_IS_INVALID, e);
		}
		validateFileUploadDownloadToken(userData, token, userId);

		int id;
		long pk;
		try {
			Batch batch = batchService.saveFile(userData, file, "ws.zip", category);
			id = batch == null ? -1 : batch.getNumber();
			pk = batch == null ? -1 : batch.getPk();
		} catch (Exception e) {
			EvoteException evoteException = unwrapThrowable(e, EvoteException.class);
			if (evoteException != null && evoteException.getCode() != null) {
				ErrorCode errorCode = evoteException.getErrorCode();
				String[] params = evoteException.getParams();
				logException(errorCode, params, evoteException);
				throw new EvoteWsException(evoteException);
			}
			EvoteNoRollbackException evoteNoRollbackException = unwrapThrowable(e, EvoteNoRollbackException.class);
			if (evoteNoRollbackException != null && evoteNoRollbackException.getCode() != null) {
				ErrorCode errorCode = evoteNoRollbackException.getErrorCode();
				String[] params = evoteNoRollbackException.getParams();
				logException(errorCode, params, evoteNoRollbackException);
				throw new EvoteWsException(evoteNoRollbackException);
			}
			LOGGER.error(e.getMessage(), e);
			throw new EvoteWsException(ErrorCode.ERROR_CODE_9102_UNEXPECTED_FILE_UPLOAD_ERROR, e);
		}
		if (id != -1) {
			// Asynchronous import
			batchService.importFile(userData, id, userData.getElectionEventPk(), category);
		}
		return pk;
	}

	private void logException(ErrorCode errorCode, String[] params, Exception exception) {
		String message = messageProvider.get("@count.error.upload.io") + ": ";
		if (errorCode != null) {
			message += messageProvider.get(errorCode.formatMessage(params), params);
		} else {
			message += messageProvider.get(exception.getMessage(), params);
		}

		LOGGER.info(message);
	}

	@WebMethod
	@WebResult(name = STATUS)
	public int checkStatus(@WebParam(name = BATCH_ID) long batchId) throws EvoteWsException, UnknownHostException {
		// lagt på 1000 for at EVA Skanning lettere skal kunne tolke status (særlig status == 0)
		return STATUS_OFFSET + batchService.checkStatus(batchId);
	}

	private void validateFileUploadDownloadToken(final UserData userData, final byte[] token, final String userId) throws EvoteWsException {
		try {
			legacyUserDataService.isFileUploadDownloadTokenValid(userData, token, userId);
		} catch (Exception e) {
			EvoteException evoteException = unwrapThrowable(e, EvoteException.class);
			if (evoteException != null && evoteException.getCode() != null) {
				throw new EvoteWsException(evoteException);
			}
			LOGGER.warn(e.getMessage(), e);
			throw new EvoteWsException(ErrorCode.ERROR_CODE_9103_TOKEN_IS_INVALID, e);
		}
	}

	private UserData getUserData(String userId, String roleId, String areaPath, String electionPath) throws EvoteWsException, UnknownHostException {
		try {
			UserData userData = legacyUserDataService.getUserData(userId, roleId, areaPath, electionPath, WSUtil.getClientAddress(wsContext));
			userData.setAccessCache(legacyUserDataService.getAccessCache(userData));
			return userData;
		} catch (EJBException e) {
			EvoteException evoteException = unwrapThrowable(e, EvoteException.class);
			if (evoteException != null && evoteException.getCode() != null) {
				throw new EvoteWsException(evoteException);
			}
			LOGGER.warn(e.getMessage(), e);
			throw new EvoteWsException(ERROR_CODE_9101_UNABLE_TO_GET_USER_DATA, e);
		}
	}
}
