package no.valg.eva.admin.frontend.util;

import lombok.Getter;
import no.valg.eva.admin.BaseFrontendTest;
import no.valg.eva.admin.ServletContainer;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.servlet.http.Cookie;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

public class CookieStoreTest extends BaseFrontendTest {
    
    private static final String CONTEXT_PATH = "/secure";
    
    @Getter
    private List<Cookie> inMemoryCookieStore;
    
    public void setCookieStoreTestContext() {
        initializeMocks();
        inMemoryCookieStore = new ArrayList<>();
        ServletContainer servletContainer = new ServletContainer(this);
        servletContainer.setContextPath(CONTEXT_PATH);
        doAnswer(invocation -> inMemoryCookieStore.stream().toArray(Cookie[]::new)).when(servletContainer.getRequestMock()).getCookies();
        doAnswer(invocation -> inMemoryCookieStore.add(invocation.getArgument(0))).when(servletContainer.getResponseMock()).addCookie(any());
        CookieStore.setContext(getFacesContextMock());
    }
    
    @BeforeMethod
    public void setUp() {
        setCookieStoreTestContext();
    }
    
    @Test
    public void noCookieFoundReturnsNull() {
        Cookie cookie = CookieStore.getCookie("cookieThatDoesntExistInStore");
        assertNull(cookie);
    }

    @Test
    public void maxAgeTouchedOnSaveCookie() {
        assertStoreEmpty();
        CookieStore.save("cookie1", "test");

        assertEquals(inMemoryCookieStore.size(), 1);
        assertTrue(inMemoryCookieStore.get(0).getMaxAge() > 0);
    }
    
    private void assertStoreEmpty() {
        assertEquals(inMemoryCookieStore.size(), 0);
    }

    @Test
    public void maxAgeTouchedOnGetCookie() {
        String theCookieName = "theCookie";
        Cookie cookieCreatedOutsideStore = new Cookie(theCookieName, "test");
        assertFalse(cookieCreatedOutsideStore.getMaxAge() > 0);
        
        assertStoreEmpty();
        inMemoryCookieStore.add(cookieCreatedOutsideStore);
        
        Cookie cookieFetchedThroughStore = CookieStore.getCookie(theCookieName);
        assertTrue(cookieFetchedThroughStore.getMaxAge() > 0);
    }

    @Test
    public void valueUpdatedOnSave() {
        String theCookieName = "theCookie";
        String expectedValue = "test1";
        
        CookieStore.save(theCookieName, expectedValue);
        assertCookieWithValue(theCookieName, expectedValue);
        
        expectedValue = "New Value Here!";
        CookieStore.save(theCookieName, expectedValue);
        assertCookieWithValue(theCookieName, expectedValue);
    }
    
    private void assertCookieWithValue(String cookieName, String expectedValue) {
        assertEquals(CookieStore.getCookie(cookieName).getValue(), expectedValue);
    }

    @Test
    public void contextPathIsSetOnCookie() {
        CookieStore.save("test", "test");
        assertEquals(CookieStore.getCookie("test").getPath(), CONTEXT_PATH);
    }
}