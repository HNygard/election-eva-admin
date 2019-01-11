package no.valg.eva.admin.frontend.security;

import static no.valg.eva.admin.common.rbac.Accesses.Aggregert_Valghendelse;
import static no.valg.eva.admin.common.rbac.Accesses.Konfigurasjon_Opptellingsmåter;
import static no.valg.eva.admin.common.rbac.Accesses.Konfigurasjon_Oversikt;
import static no.valg.eva.admin.common.rbac.Accesses.Manntall_Historikk;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.common.rbac.Accesses;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class PageAccessTest extends BaseFrontendTest {

	@BeforeTest
	public void init() throws Exception {
		initializeMocks();
	}

	@Test
	public void hasAccess_withPageWithNoAccess_returnsFalse() throws Exception {
		PageAccess pageAccess = new PageAccess();
		pageAccess.init();
		assertThat(pageAccess.hasAccess(getUserDataMock(), "/secure/PAGE_WITH_NO_ACCESS.xhtml")).isFalse();
	}

	@Test(dataProvider = "hasAccessProvider")
	public void hasAccess(String url, Accesses access, boolean expected) throws Exception {
		if (access != null) {
			when(getUserDataMock().hasAccess(access)).thenReturn(true);
		}
		PageAccess pageAccess = new PageAccess();
		pageAccess.init();
		assertThat(pageAccess.hasAccess(getUserDataMock(), url)).isEqualTo(expected);
	}

	@DataProvider
	public static Object[][] hasAccessProvider() {
		return new Object[][] {
				{ "/a/b.xhtml", null, true },
				{ "/a/b/c.xhtml", Manntall_Historikk, false },
				{ "/a/b/c.xhtml", Aggregert_Valghendelse, true },
				{ "/a/b/d.xhtml", Konfigurasjon_Opptellingsmåter, true },
				{ "/a/b/d.xhtml", Konfigurasjon_Oversikt, true }
		};
	}

	@Test
	public void getId_withPage_checkId() throws Exception {
		PageAccess pageAccess = new PageAccess();
		pageAccess.init();

		assertThat(pageAccess.getId("sdfsdf")).isNull();
		assertThat(pageAccess.getId("/a/b/d.xhtml")).isEqualTo("1");
		assertThat(pageAccess.getId("/a/b.xhtml")).isEqualTo("2");
		assertThat(pageAccess.getId("/a/b/c.xhtml")).isEqualTo("3");
	}

	@Test
	public void getPage_withId_checkPage() throws Exception {
		PageAccess pageAccess = new PageAccess();
		pageAccess.init();

		assertThat(pageAccess.getPage("10")).isNull();
		assertThat(pageAccess.getPage("1")).isEqualTo("/a/b/d.xhtml");
		assertThat(pageAccess.getPage("2")).isEqualTo("/a/b.xhtml");
		assertThat(pageAccess.getPage("3")).isEqualTo("/a/b/c.xhtml");
	}
}
