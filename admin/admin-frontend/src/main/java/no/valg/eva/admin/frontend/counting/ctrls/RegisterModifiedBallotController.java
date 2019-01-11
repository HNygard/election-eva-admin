package no.valg.eva.admin.frontend.counting.ctrls;

import java.io.Serializable;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.inject.Named;

import no.evote.exception.ValidateException;
import no.valg.eva.admin.common.counting.model.modifiedballots.Candidate;

@Named
public class RegisterModifiedBallotController extends ModifiedBallotsNavigationController implements Serializable {

	private PersonVoteCandidateConverter personVoteCandidateConverter = new PersonVoteCandidateConverter();

	@Override
	protected void doInit() {
		// Nothing to do right now
	}

	@Override
	public void gotoNextBallot() {
		try {
			validateAndSave();
			super.gotoNextBallot();
		} catch (ValidateException e) {
			showMessage(e);
		}
	}

	@Override
	public void gotoPreviousBallot() {
		try {
			validateAndSave();
			super.gotoPreviousBallot();
		} catch (ValidateException e) {
			showMessage(e);
		}
	}

	public void validateAndShowErrorMessage() {
		try {
			validate();
		} catch (ValidateException e) {
			showMessage(e);
		}
	}
	
	private void validate() {

		if (currentModifiedBallot != null) {
			currentModifiedBallot.addCandidatesForPersonVotes(getPersonVotes().getCandidatesVoteSet());
			currentModifiedBallot.setWriteIns(writeInAutoComplete.getWriteInsFromAutoComplete());
			currentModifiedBallot.validate(modifiedBallots.getBallot());
		}

	}

	private void validateAndSave() {
		if (currentModifiedBallot != null) {
			validate();
			currentModifiedBallotIsDone();
			modifiedBallotService.update(userData, currentModifiedBallot);
		}
	}

	public PersonVoteCandidateConverter getPersonVoteCandidateConverter() {
		return personVoteCandidateConverter;
	}

	protected void currentModifiedBallotIsDone() {
	}

	public void finished() {
		try {
			validateAndSave();
			super.finished();
		} catch (ValidateException e) {
			showMessage(e);
		}
	}

	private void showMessage(ValidateException e) {
		getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, messageProvider.get(e.getMessage(), e.getParams()), ""));
	}

	public class PersonVoteCandidateConverter implements Converter {

		@Override
		public Object getAsObject(FacesContext context, UIComponent component, String value) {

			for (Candidate model : getPersonVotes().getCandidatesForPersonVotes()) {
				String pk = model.getCandidateRef().getPk() + "";
				if (pk.equals(value)) {
					return model;
				}
			}

			return null;
		}

		@Override
		public String getAsString(FacesContext context, UIComponent component, Object value) {
			if (value == null) {
				return "";
			}
			return ((Candidate) value).getCandidateRef().getPk() + "";
		}
	}
}
