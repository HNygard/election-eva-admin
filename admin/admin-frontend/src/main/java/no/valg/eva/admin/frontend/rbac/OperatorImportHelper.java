package no.valg.eva.admin.frontend.rbac;

import static com.google.common.collect.Collections2.filter;
import static com.google.common.collect.Collections2.transform;
import static com.google.common.collect.Lists.newArrayList;
import static no.evote.constants.AreaLevelEnum.MUNICIPALITY;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.inject.Inject;

import no.evote.exception.EvoteException;
import no.valg.eva.admin.frontend.util.MessageUtil;
import no.evote.security.UserData;
import no.valg.eva.admin.util.ExcelUtil;
import no.valg.eva.admin.common.rbac.ImportOperatorRoleInfo;
import no.valg.eva.admin.common.rbac.PollingPlaceResponsibleOperator;
import no.valg.eva.admin.common.rbac.VoteReceiver;
import no.valg.eva.admin.common.rbac.service.ImportOperatorService;
import no.valg.eva.admin.frontend.faces.FacesContextBroker;
import no.valg.eva.admin.frontend.i18n.MessageProvider;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

import com.google.common.base.Function;
import com.google.common.base.Predicate;

/**
 * Helper class that imports operators, and generates JSF error messages if any.
 */
public class OperatorImportHelper implements Serializable {
	private static final long serialVersionUID = 1L;
	private static final transient Logger LOGGER = Logger.getLogger(OperatorImportHelper.class);
	private static final Predicate<ImportOperatorRoleInfo> VOTE_RECEIVER_FILTER = new Predicate<ImportOperatorRoleInfo>() {
		@Override
		public boolean apply(ImportOperatorRoleInfo input) {
			return input instanceof VoteReceiver;
		}
	};
	private static final Predicate<ImportOperatorRoleInfo> POLLING_PLACE_RESPONSIBLE = new Predicate<ImportOperatorRoleInfo>() {
		@Override
		public boolean apply(ImportOperatorRoleInfo input) {
			return input instanceof PollingPlaceResponsibleOperator;
		}
	};
	private static final Function<ImportOperatorRoleInfo, VoteReceiver> VOTE_RECEIVER_CAST = new Function<ImportOperatorRoleInfo, VoteReceiver>() {
		@Override
		public VoteReceiver apply(ImportOperatorRoleInfo input) {
			return (VoteReceiver) input;
		}
	};
	private static final Function<ImportOperatorRoleInfo, PollingPlaceResponsibleOperator> POLLING_PLACE_RESPONSIBLE_CAST
		= new Function<ImportOperatorRoleInfo, PollingPlaceResponsibleOperator>() {
		@Override
		public PollingPlaceResponsibleOperator apply(ImportOperatorRoleInfo input) {
			return (PollingPlaceResponsibleOperator) input;
		}
	};
	public static final String FODSELSNUMMER = "Fødselsnummer";
	public static final String FORNAVN = "Fornavn";
	public static final String ETTERNAVN = "Etternavn";
	public static final String E_POST = "E-post";
	public static final String MOBILTELEFON = "Mobiltelefon";
	public static final String FORHANDSSTEMMESTED = "Forhåndsstemmested";
	public static final String STEMMEKRETS = "Stemmekrets";
	public static final String ANSVARLIG = "Ansvarlig";

	@Inject
	private EarlyVoteReceiverParser earlyVoteReceiverParser;

	@Inject
	private ElectionDayOperatorParser voteReceiverAndPollingPlaceResponsibleParser;

	@Inject
	private ImportOperatorService importOperatorService;

	@Inject
	private UserData userData;

	@Inject
	private MessageProvider messageProvider;

	@Inject
	private FacesContextBroker facesBroker;

	/**
	 * Imports early voter receivers from spreadsheet, available as an InputStream wrapped in supplied InputStreamWrapper
	 * 
	 * @param inputStreamWrapper wraps an inpiut stream with spreadsheet content
	 */
	public void importAdvanceVoteReceivers(final InputStreamWrapper inputStreamWrapper) throws SpreadSheetValidationException {
		if (userData.getOperatorMvArea().getAreaLevel() < MUNICIPALITY.getLevel()) {
			throw new EvoteException("@rbac.import_operators.invalid_user_arealevel");
		}
		List<ImportOperatorRoleInfo> earlyVotingOperatorInfos = new ImporterTemplate(inputStreamWrapper.getWrappedInputStream()) {
			@Override
			protected List<ImportOperatorRoleInfo> transform(InputStream inputStream) throws IOException, InvalidFormatException,
					SpreadSheetValidationException {
				if (inputStream != null) {
					ExcelUtil.RowData rowData = getRowDataFromExcelFile(inputStream);
					validateHeader(rowData, FODSELSNUMMER, FORNAVN, ETTERNAVN, E_POST, MOBILTELEFON, FORHANDSSTEMMESTED);
					return earlyVoteReceiverParser.toOperatorList(rowData.getRows());
				}
				return Collections.emptyList();
			}
		}.execute();
		if (!earlyVotingOperatorInfos.isEmpty()) {
			importOperatorService.importEarlyVoteReceiverOperator(userData, earlyVotingOperatorInfos);
			MessageUtil.buildFacesMessage(facesBroker.getContext(), null, "@rbac.roles.imported", new String[] { "" }, FacesMessage.SEVERITY_INFO);
		}
	}

	/**
	 * Imports election day voting receivers and polling place responsibles from spreadsheet, available as an InputStream wrapped in supplied InputStreamWrapper
	 * 
	 * @param inputStreamWrapper wraps an inpiut stream with spreadsheet content
	 */
	public void importVoteReceiverAndPollingPlaceResponsibles(InputStreamWrapper inputStreamWrapper) throws SpreadSheetValidationException {
		if (userData.getOperatorMvArea().getAreaLevel() < MUNICIPALITY.getLevel()) {
			throw new EvoteException("@rbac.import_operators.invalid_user_arealevel");
		}
		List<ImportOperatorRoleInfo> voteReceiversAndPollingPlaceResponsibles = new ImporterTemplate(inputStreamWrapper.getWrappedInputStream()) {
			@Override
			protected List<ImportOperatorRoleInfo> transform(InputStream inputStream) throws IOException, InvalidFormatException,
					SpreadSheetValidationException {
				if (inputStream != null) {
					ExcelUtil.RowData rowData = getRowDataFromExcelFile(inputStream);
					validateHeader(rowData, FODSELSNUMMER, FORNAVN, ETTERNAVN, E_POST, MOBILTELEFON, STEMMEKRETS, ANSVARLIG);
					return voteReceiverAndPollingPlaceResponsibleParser.toOperatorList(rowData.getRows());
				}
				return Collections.emptyList();
			}
		}.execute();
		if (!voteReceiversAndPollingPlaceResponsibles.isEmpty()) {
			importOperatorService.importVotingAndPollingPlaceResponsibleOperators(userData,
					newArrayList(transform(filter(voteReceiversAndPollingPlaceResponsibles, VOTE_RECEIVER_FILTER), VOTE_RECEIVER_CAST)),
					newArrayList(transform(filter(voteReceiversAndPollingPlaceResponsibles, POLLING_PLACE_RESPONSIBLE), POLLING_PLACE_RESPONSIBLE_CAST)));

			MessageUtil.buildFacesMessage(facesBroker.getContext(), null, "@rbac.roles.imported", new String[] { "" }, FacesMessage.SEVERITY_INFO);
		}
	}

	public abstract static class InputStreamWrapper {
		public abstract InputStream getInputStream() throws IOException;

		public InputStream getWrappedInputStream() {
			try {
				return getInputStream();
			} catch (IOException e) {
				MessageUtil.buildFacesMessage(null, e.getMessage(), null, FacesMessage.SEVERITY_ERROR);
				return null;
			}
		}
	}

	ExcelUtil.RowData getRowDataFromExcelFile(InputStream inputStream) throws IOException, InvalidFormatException {
		return ExcelUtil.getRowDataFromExcelFile(inputStream);
	}

	private void validateHeader(ExcelUtil.RowData rowData, String... columns) throws SpreadSheetValidationException {
		List<Pair<String, String>> header = rowData.getHeader();
		List<String> errors = new ArrayList<>();
		if (header == null || header.size() < columns.length) {
			int size = header == null ? 0 : header.size();
			errors.add(messageProvider.get("@excel.import.invalidNumberOfColumns",
					new String[] { String.valueOf(columns.length), String.valueOf(size) }));
		} else {
			for (int i = 0; i < columns.length; i++) {
				if (!columns[i].equalsIgnoreCase(header.get(i).getValue())) {
					errors.add(messageProvider.get("@excel.import.invalidColumnName", new String[] { String.valueOf(i + 1), columns[i] }));
				}
			}
		}
		if (!errors.isEmpty()) {
			throw new SpreadSheetValidationException(errors);
		}
	}

	/**
	*/
	private abstract class ImporterTemplate {
		private InputStream file;

		public ImporterTemplate(InputStream file) {
			this.file = file;
		}

		protected abstract List<ImportOperatorRoleInfo> transform(final InputStream part) throws IOException, InvalidFormatException,
				SpreadSheetValidationException;

		/**
		 * Common execute method that wraps transformation in common exception handling
		 */
		public List<ImportOperatorRoleInfo> execute() throws SpreadSheetValidationException {
			try {
				return transform(file);
			} catch (IOException | InvalidFormatException e) {
				logAndPrintErrorMessage("@common.message.exception.general", e);
			} catch (EvoteException e) {
				logAndPrintErrorMessage(e.getMessage(), e);
			}
			return Collections.emptyList();
		}

		protected void logAndPrintErrorMessage(String message, Exception exception) {
			if (exception instanceof EvoteException) {
				EvoteException evoteException = (EvoteException) exception;
				MessageUtil
						.buildFacesMessage(facesBroker.getContext(), null, messageProvider.getWithTranslatedParams(message, evoteException.getParams()), null,
								FacesMessage.SEVERITY_ERROR);
			} else {
				MessageUtil.buildFacesMessage(facesBroker.getContext(), null, message, null, FacesMessage.SEVERITY_ERROR);
			}
			LOGGER.error(exception.getMessage(), exception);
		}
	}

}
