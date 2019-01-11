package no.valg.eva.admin.frontend.util;

import javax.faces.context.FacesContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static java.util.Arrays.stream;

public class CookieStore {
    
    private static final int DEFAULT_MAX_AGE_IN_DAYS = 30;
    private static final int SECONDS_IN_DAY = 24 * 60 * 60;
    private static FacesContext context;
    
    private CookieStore() {
    }
    
    public static synchronized boolean save(String name, String value) {
        if (doesNotHaveContext()) {
            return false;
        }
        
        touchExpiryDateAndSave(
                getOrCreateCookie(name, value)
        );
        return true;
    }
    
    private static boolean doesNotHaveContext() {
        return getContext() == null || getContext().getExternalContext() == null;
    }

    private static FacesContext getContext() {
        if (context != null) {
            return context;
        }
        return FacesContext.getCurrentInstance();
    }

    private static Cookie getOrCreateCookie(String name, String value) {
        Cookie cookie = getCookieFromRequest(name);
        if (cookie == null) {
            cookie = new Cookie(name, value);
            cookie.setHttpOnly(true);
            cookie.setSecure(true);
            cookie.setPath(
                    ((HttpServletRequest) getContext().getExternalContext().getRequest()).getContextPath()
            );
        }
        else {
            cookie.setValue(value);
        }
        return cookie;
    }
    
    
    public static synchronized Cookie getCookie(String name) {
        if (doesNotHaveContext()) {
            return null;
        }
        
        return touchExpiryDateAndSave(
                getCookieFromRequest(name)
        );
    }
    
    private static Cookie getCookieFromRequest(String name) {
        return stream(((HttpServletRequest) getContext().getExternalContext().getRequest()).getCookies())
                .filter(c -> c.getName().equals(name))
                .findFirst()
                .orElse(null);
    }

    private static Cookie touchExpiryDateAndSave(Cookie cookie) {
        if (cookie != null) {
            cookie.setMaxAge(DEFAULT_MAX_AGE_IN_DAYS * SECONDS_IN_DAY);
            ((HttpServletResponse) getContext().getExternalContext().getResponse()).addCookie(cookie);
        }
        return cookie;
    }
    
    // For testing purposes
    public static void setContext(FacesContext context) {
        CookieStore.context = context;
    }
}
