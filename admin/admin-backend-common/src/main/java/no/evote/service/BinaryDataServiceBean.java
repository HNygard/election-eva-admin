package no.evote.service;

import java.io.File;
import java.io.IOException;

import javax.activation.MimetypesFileTypeMap;
import javax.inject.Inject;

import no.evote.model.BinaryData;
import no.evote.security.UserData;
import no.valg.eva.admin.util.IOUtil;
import no.valg.eva.admin.backend.common.repository.BinaryDataRepository;
import no.valg.eva.admin.configuration.domain.model.ElectionEvent;

/**
 * NOTE! This is legacy code and based on the old architecture and should not be extended and or build upon!
 */
public class BinaryDataServiceBean {
	@Inject
	private BinaryDataRepository binaryDataRepository;

	public BinaryData createBinaryData(UserData userData, byte[] bytes, String fileName, ElectionEvent electionEvent,
			String tableName, String columnName, String mimeTypeParam) throws IOException {

		File file = IOUtil.makeFile(bytes, fileName);

		String mimeType = mimeTypeParam;
		if (mimeType == null) {
			mimeType = new MimetypesFileTypeMap().getContentType(file);
		}
		BinaryData binaryData = new BinaryData();
		binaryData.setMimeType(mimeType);
		binaryData.setElectionEvent(electionEvent);
		binaryData.setBinaryData(bytes);
		binaryData.setTableName(tableName);
		binaryData.setColumnName(columnName);
		binaryData.setFileName(fileName);
		binaryData = binaryDataRepository.createBinaryData(userData, binaryData);

		IOUtil.deleteContainingFolder(file);

		return binaryData;
	}
}
