package no.valg.eva.admin.frontend.security;

import lombok.extern.log4j.Log4j;
import no.evote.exception.EvoteException;
import no.evote.security.UserData;
import no.valg.eva.admin.common.rbac.Accesses;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import static java.nio.charset.StandardCharsets.UTF_8;

@Named
@ApplicationScoped
@Log4j
public class PageAccess implements Serializable {

    private static final long serialVersionUID = 5862899057322083663L;

    private Set<String> open = new HashSet<>();
    private Map<String, Set<Accesses>> accessMap = new HashMap<>();
    private Map<String, String> pageToIdMap = new HashMap<>();
    private Map<String, String> idToPageMap = new HashMap<>();

    @PostConstruct
    public void init() {
        Properties props = load();
        open = new HashSet<>();
        accessMap = new HashMap<>();
        int count = 1;
        for (String page : props.stringPropertyNames()) {
            String[] values = props.getProperty(page).split(",");
            for (String value : values) {
                value = value.trim();
                if ("*".equals(value)) {
                    open.add(page);
                    break;
                } else {
                    Set<Accesses> set = accessMap.computeIfAbsent(page, k -> new HashSet<>());
                    try {
                        set.add(Accesses.valueOf(value));
                    } catch (IllegalArgumentException e) {
                        log.fatal(e.getMessage(), e);
                        throw e;
                    }
                }
            }
            pageToIdMap.put(page, String.valueOf(count));
            idToPageMap.put(String.valueOf(count), page);
            count++;
        }

        log.info("Open pages");
        for (String page : open) {
            log.info("\t" + page);
        }
        log.info("Secured pages");
        for (Map.Entry<String, Set<Accesses>> e : accessMap.entrySet()) {
            log.info("\t" + e.getKey() + ": " + e.getValue());
        }
    }

    public String getId(String page) {
        return pageToIdMap.get(page);
    }

    public String getPage(String id) {
        return idToPageMap.get(id);
    }

    private Properties load() {
        Properties result = new Properties();
        String resource = "/" + getClass().getSimpleName() + ".properties";
        try (
                InputStream stream = getClass().getResourceAsStream(resource);
                Reader reader = new InputStreamReader(stream, UTF_8)) {
            result.load(reader);
        } catch (IOException e) {
            throw new EvoteException("Failed to load resource " + resource, e);
        }
        return result;
    }

    public boolean hasAccess(UserData userData, String url) {
        // Strip query params
        int index = url.indexOf('?');
        if (index != -1) {
            url = url.substring(0, index);
        }
        if (open.contains(url)) {
            return true;
        }
        if (accessMap.containsKey(url)) {
            for (Accesses access : accessMap.get(url)) {
                if (userData.hasAccess(access)) {
                    return true;
                }
            }
        }
        return false;
    }
}
