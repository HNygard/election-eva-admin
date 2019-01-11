package no.evote.presentation.resolver;

import static org.assertj.core.api.Assertions.assertThat;

import javax.el.ELContext;

import no.evote.constants.VoteCountStatusEnum;
import no.valg.eva.admin.BaseFrontendTest;

import org.testng.annotations.Test;

public class EvoteEnumResolverTest extends BaseFrontendTest {

	@Test
	public void getValue_withNullBaseAndNonExistingConstant_returnsNull() throws Exception {
		EvoteEnumResolver resolver = new EvoteEnumResolver();
		ELContext elContextMock = createMock(ELContext.class);
		
		Object result = resolver.getValue(elContextMock, null, "test");
		
		assertThat(result).isNull();
	}

	@Test
	public void getValue_withNullBaseAndExistingConstant_returnsVoteCountStatusEnumClass() throws Exception {
		EvoteEnumResolver resolver = new EvoteEnumResolver();
		ELContext elContextMock = createMock(ELContext.class);

		Object result = resolver.getValue(elContextMock, null, "VoteCountStatusEnum");

		assertThat(result).isEqualTo(VoteCountStatusEnum.class);
	}

	@Test
	public void getValue_withBaseAndNonExistingConstant_returnsNull() throws Exception {
		EvoteEnumResolver resolver = new EvoteEnumResolver();
		ELContext elContextMock = createMock(ELContext.class);

		Object result = resolver.getValue(elContextMock, VoteCountStatusEnum.class, "ASFSD");

		assertThat(result).isNull();
	}

	@Test
	public void getValue_withBaseAndExistingConstant_returnsCOUNTING() throws Exception {
		EvoteEnumResolver resolver = new EvoteEnumResolver();
		ELContext elContextMock = createMock(ELContext.class);

		Object result = resolver.getValue(elContextMock, VoteCountStatusEnum.class, "COUNTING");

		assertThat(result).isSameAs(VoteCountStatusEnum.COUNTING);
	}
}
