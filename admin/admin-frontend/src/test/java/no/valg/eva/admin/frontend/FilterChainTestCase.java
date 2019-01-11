package no.valg.eva.admin.frontend;

import no.evote.security.UserData;
import no.evote.security.UserDataProducer;
import no.evote.util.MockUtils;
import no.valg.eva.admin.BaseFrontendTest;
import org.apache.log4j.Logger;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import javax.enterprise.inject.Instance;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static no.evote.util.MockUtils.getMock;
import static no.evote.util.MockUtils.mockInjects;
import static no.evote.util.MockUtils.setPrivateField;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;


public abstract class FilterChainTestCase extends BaseFrontendTest implements FilterChain {

	private static final Logger LOG = Logger.getLogger(FilterChainTestCase.class);
	private FilterConfig filterConfig;
	private FilterChainConfig[] filters;
	private int chainIndex;
	private Map<Filter, List<Object>> mocks = new HashMap<>();

	protected abstract FilterChainConfig[] getFilterChain() throws Exception;

	protected void filtersInitialized() throws Exception {
	}

	@BeforeMethod
	public final void setup() throws Exception {
		initializeMocks();
		this.filterConfig = createMock(FilterConfig.class);
		this.filters = getFilterChain();
		this.chainIndex = -1;
		setUserDataInstance(null);
		initFilters();
		filtersInitialized();
		// Setup HttpServletResponse.sendRedirect behaviour
		on(new Call() {
			public void perform(Object[] args) throws Exception {
				log("\tRedirecting to " + args[0] + ". Run chain again.");
				request((String) args[0]);
			}
		}).when(getServletContainer().getResponseMock()).sendRedirect(anyString());
		// Setup session invalidate behaviour
		on(new Call() {
			public void perform(Object[] args) throws Exception {
				log("\tSession invalidated");
				setUserDataInstance(null);
			}
		}).when(getServletContainer().getHttpSessionMock()).invalidate();
		// Setup session setAttribute behaviour
		on(new Call() {
			public void perform(Object[] args) {
				String key = (String) args[0];
				Object value = args[1];
				log("\tSession.setAttribute(" + key + ", " + value + ")");
				when(getServletContainer().getHttpSessionMock().getAttribute(key)).thenReturn(value);
			}
		}).when(getServletContainer().getHttpSessionMock()).setAttribute(anyString(), any(Object.class));
		// Setup session removeAttribute behaviour
		on(new Call() {
			public void perform(Object[] args) {
				String key = (String) args[0];
				log("\tSession.removeAttribute(" + key + ")");
				when(getServletContainer().getHttpSessionMock().getAttribute(key)).thenReturn(null);
			}
		}).when(getServletContainer().getHttpSessionMock()).removeAttribute(anyString());
		// Setup invalidateRoleSelection behaviour
		on(new Call() {
			public void perform(Object[] args) {
				when(getUserDataMock().getOperatorRole()).thenReturn(null);
			}
		}).when(getUserDataMock()).invalidateRoleSelection();
	}

	@AfterMethod
	public final void tearDown() {
		for (FilterChainConfig filter : filters) {
			filter.filter.destroy();
		}
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException {
		chainIndex = chainIndex + 1;
		if (chainThrough()) {
			log("\tStop filtering, end of chain");
			return;
		}
		String uri = ((HttpServletRequest) request).getRequestURI();
		FilterChainConfig filterChainConfig = filters[chainIndex];
		if (filterChainConfig.matches(request)) {
			log("\t" + filterChainConfig.filter.getClass().getSimpleName() + " on " + uri);
			filterChainConfig.filter.doFilter(request, response, this);
		} else {
			log("\t" + filterChainConfig.filter.getClass().getSimpleName() + " on " + uri);
			doFilter(request, response);
		}
	}

	protected <T> T getInjectMock(Filter filter, Class<T> mockClass) {
		try {
			return getMock(mocks.get(filter), mockClass, false);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected void request(String uri) throws Exception {
		request(uri, "GET");
	}

	protected void request(String uri, String method) throws Exception {
		log("Requesting " + uri + ", " + method);
		this.chainIndex = -1;
		getServletContainer().setContextPath("");
		getServletContainer().setRequestURI(uri);
		getServletContainer().setServletPath(uri);
		getServletContainer().setMethod(method);
		doFilter(getServletContainer().getRequestMock(), getServletContainer().getResponseMock());
	}

	public Filter[] getFilters() {
		Filter[] result = new Filter[filters.length];
		for (int i = 0; i < filters.length; i++) {
			result[i] = filters[i].filter;
		}
		return result;
	}

	public boolean chainAt(Class<? extends Filter> filter) {
		if (chainThrough() || chainIndex < 0) {
			return false;
		}
		return filters[chainIndex].filter.getClass().getName().equals(filter.getName());
	}

	public boolean chainPast(Class<? extends Filter> filter) {
		if (chainThrough() || chainIndex <= 0) {
			return false;
		}
		for (int i = chainIndex; i < filters.length; i++) {
			if (filters[chainIndex].filter.getClass().getName().equals(filter.getName())) {
				return false;
			}
		}
		return true;
	}

	public boolean chainThrough() {
		return chainIndex >= filters.length;
	}

	public void assertUserData(UserData userData) throws Exception {
		for (FilterChainConfig filter : filters) {
			Field instanceField = getInstanceField(filter.filter);
			if (instanceField != null) {
				instanceField.setAccessible(true);
				Object o = getActualTypeArgument(instanceField);
				if (isUserDataProducerInstance(o)) {
					Instance<UserDataProducer> userDataProducerInstance = (Instance<UserDataProducer>) instanceField.get(filter.filter);
					assertThat(userDataProducerInstance.get().getUserData()).isSameAs(userData);
				} else if (isUserDataInstance(o)) {
					Instance<UserData> userDataInstance = (Instance<UserData>) instanceField.get(filter.filter);
					assertThat(userDataInstance.get()).isSameAs(userData);
				}
			}
		}
	}

	private boolean isUserDataProducerInstance(Object o) {
		return o != null && o.toString().endsWith(".UserDataProducer");
	}

	private boolean isUserDataInstance(Object o) {
		return o != null && o.toString().endsWith(".UserData");
	}

	protected void log(String s) {
		LOG.info(s);
	}

	private void initFilters() throws ServletException {
		for (FilterChainConfig filter : filters) {
			filter.filter.init(filterConfig);
		}
	}

	protected void setUserDataInstance(UserData userData) throws Exception {
		log("\tSetting current userData " + userData);
		for (FilterChainConfig filter : filters) {
			setUserDataInstance(filter.filter, userData);
		}
	}

	protected void setUserDataInstance(Filter filter, UserData userData) throws Exception {
		Field instance = getInstanceField(filter);
		if (instance != null) {
			boolean created = false;
			Instance instanceMock = getPrivateField(filter, instance.getName(), Instance.class);
			if (instanceMock == null) {
				instanceMock = createMock(Instance.class);
				created = true;
			}
			Object o = getActualTypeArgument(instance);
			if (isUserDataProducerInstance(o)) {
				UserDataProducer producer = created ? createMock(UserDataProducer.class) : (UserDataProducer) instanceMock.get();
				when(producer.getUserData()).thenReturn(userData);
				if (created) {
					when(instanceMock.get()).thenReturn(producer);
					setPrivateField(filter, instance.getName(), instanceMock);
				}
				on(new Call() {
					public void perform(Object[] args) throws Exception {
						setUserDataInstance((UserData) args[0]);
					}
				}).when(producer).setUserData(any(UserData.class));
			} else if (isUserDataInstance(o)) {
				when(instanceMock.get()).thenReturn(userData);
				setPrivateField(filter, instance.getName(), instanceMock);
			}
		}
	}

	private Field getInstanceField(Filter filter) {
		Field[] fields = filter.getClass().getDeclaredFields();
		for (Field field : fields) {
			if (Instance.class.isAssignableFrom(field.getType())) {
				return field;
			}
		}
		return null;
	}

	private Object getActualTypeArgument(Field field) {
		Type type = field.getGenericType();
		if (type instanceof ParameterizedType) {
			ParameterizedType pType = (ParameterizedType) type;
			return pType.getActualTypeArguments() == null || pType.getActualTypeArguments().length == 0 ? null : pType.getActualTypeArguments()[0];
		}
		return null;
	}

	public class FilterChainConfig {
		private Filter filter;
		private Pattern[] patterns;

		public FilterChainConfig(Class<? extends Filter> filter, String... patterns) throws Exception {
			MockUtils.MockContent<? extends Filter> mockContent = mockInjects(filter);
			this.filter = mockContent.getInstance();
			mocks.put(this.filter, mockContent.getMocks());
			this.patterns = new Pattern[patterns.length];
			for (int i = 0; i < patterns.length; i++) {
				this.patterns[i] = Pattern.compile(patterns[i].replace("*", ".*"));
			}
		}

		private boolean matches(ServletRequest request) {
			for (Pattern pattern : patterns) {
				if (pattern.matcher(((HttpServletRequest) request).getRequestURI()).matches()) {
					return true;
				}
			}
			return false;
		}
	}
}

