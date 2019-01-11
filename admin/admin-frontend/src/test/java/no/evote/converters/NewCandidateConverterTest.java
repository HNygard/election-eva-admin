package no.evote.converters;

import no.evote.util.Wrapper;
import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.common.counting.model.modifiedballots.Candidate;
import no.valg.eva.admin.common.counting.model.modifiedballots.CandidateRef;
import org.testng.annotations.Test;

import javax.faces.context.FacesContext;
import javax.faces.convert.ConverterException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class NewCandidateConverterTest extends BaseFrontendTest {

    private static final String CANDIDATE_REF_STRING = "100";
    private static final long CANDIDATE_REF = Long.valueOf(CANDIDATE_REF_STRING);

	@Test
	public void getAsObject_withEmptyValue_returnsNull() throws Exception {
		assertThat(getConverter().getAsObject(getFacesContextMock(), null, null)).isNull();
	}

	@Test(expectedExceptions = ConverterException.class, expectedExceptionsMessageRegExp = "error")
	public void getAsObject_withStringValue_throwsException() throws Exception {
		NewCandidateConverter converter = getConverter();
		when(getFacesContextMock().getApplication().evaluateExpressionGet(any(FacesContext.class), anyString(), any(Class.class))).thenReturn("error");

		converter.getAsObject(getFacesContextMock(), null, "test");
	}

	@Test
	public void getAsObject_withLongValue_returnsCandidate() throws Exception {
		NewCandidateConverter converter = getConverter();

		Candidate candidate = (Candidate) converter.getAsObject(getFacesContextMock(), null, CANDIDATE_REF_STRING);

		assertThat(candidate.getCandidateRef().getPk()).isEqualTo(CANDIDATE_REF);
	}

	@Test
	public void getAsString_withNull_returnsNull() throws Exception {
		assertThat(getConverter().getAsString(getFacesContextMock(), null, null)).isNull();
	}

	@Test
	public void getAsString_withNonCandidateWrapper_returnsNull() throws Exception {
		Wrapper<Candidate> wrapper = new Wrapper<>();
		assertThat(getConverter().getAsString(getFacesContextMock(), null, wrapper)).isNull();
	}

	@Test
	public void getAsString_withCandidateWrapper_returnsLongAsString() throws Exception {
		Wrapper<Candidate> wrapper = new Wrapper<>();
		wrapper.setValue(new Candidate(new CandidateRef(CANDIDATE_REF)));
		assertThat(getConverter().getAsString(getFacesContextMock(), null, wrapper)).isEqualTo(CANDIDATE_REF_STRING);
	}

	private NewCandidateConverter getConverter() throws Exception {
		return initializeMocks(new NewCandidateConverter());
	}
}
