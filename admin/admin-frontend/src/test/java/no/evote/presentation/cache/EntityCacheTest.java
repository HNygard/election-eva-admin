package no.evote.presentation.cache;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import no.evote.security.UserData;
import no.evote.service.GenericService;
import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.rbac.domain.model.Access;
import no.valg.eva.admin.test.TestGroups;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Test(groups = TestGroups.RESOURCES)
public class EntityCacheTest extends BaseFrontendTest {

	private EntityCache entityCache;
	private Cache cache;

	@BeforeMethod
	public void setUp() throws Exception {
		this.entityCache = initializeMocks(EntityCache.class);
		CacheManager.create(getClass().getClassLoader().getResourceAsStream("ehcache-local.xml"));
		cache = CacheManager.getInstance().getCache("entityCache");
		mockFieldValue("cache", cache);
	}

	@Test
    public void init_verifyGetCache() {
		entityCache.init();

		verify(getInjectMock(GenericCacheManager.class)).getCache("entityCache");
	}

	@Test
	public void shutdown_verifyDispose() throws Exception {
		Cache cacheMock = createMock(Cache.class);
		mockFieldValue("cache", cacheMock);
		entityCache.shutdown();

		verify(cacheMock).dispose();
	}

	@Test
    public void get_withElementFound_returnsGetValue() {
		when(getInjectMock(GenericService.class).findByPk(any(UserData.class), any(Class.class), anyLong())).thenReturn(new Access());

		Access result = entityCache.get(getUserDataMock(), Access.class, 1L);
		assertThat(result).isNotNull();

		result = entityCache.get(getUserDataMock(), Access.class, 1L);
		assertThat(result).isNotNull();
	}

	@Test
    public void remove() {
		when(getInjectMock(GenericService.class).findByPk(any(UserData.class), any(Class.class), anyLong())).thenReturn(new Access());

		Access result = entityCache.get(getUserDataMock(), Access.class, 1L);
		assertThat(result).isNotNull();

		entityCache.remove(Access.class, 1L);

		assertThat(cache.get("Access-1")).isNull();
	}
}
