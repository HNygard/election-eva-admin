package no.valg.eva.admin.integration;

import lombok.extern.log4j.Log4j;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;

import static java.lang.String.format;
import static no.valg.eva.admin.util.StringUtil.isNotBlank;

@Log4j
public class ResteasyProxyClientFactory {
    
    private static final String HTTP_PROXY_HOST = "http.proxyHost";
    private static final String HTTP_PROXY_PORT = "http.proxyPort";

    public static ResteasyClient buildHttpProxyClient() {
        return createProxyClientFor(HTTP_PROXY_HOST, HTTP_PROXY_PORT);
    }

    private ResteasyProxyClientFactory() {
    }

    private static ResteasyClient createProxyClientFor(String hostEnv, String portEnv) {

        final ResteasyClientBuilder builder = new ResteasyClientBuilder();
        final String host = System.getProperty(hostEnv);
        final String port = System.getProperty(portEnv);


        if (isNotBlank(host, port)) {
            try {
                log.info(format("Creating proxy client to http host: %s and port: %s", host, port));
                return builder
                        .defaultProxy(host, Integer.valueOf(port))
                        .build();
            } catch (Exception e) {
                log.error(format("Failed creating proxy client to http host: %s and port: %s", host, port), e);
            }
        }

        log.info("Creating client without proxy config");
        return builder.build();
    }
}
