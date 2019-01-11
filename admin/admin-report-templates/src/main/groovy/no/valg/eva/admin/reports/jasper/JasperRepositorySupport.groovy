package no.valg.eva.admin.reports.jasper

import groovyx.net.http.HTTPBuilder
import org.apache.log4j.Logger
import org.joda.time.format.ISODateTimeFormat

import static groovyx.net.http.ContentType.XML
import static groovyx.net.http.Method.HEAD
import static groovyx.net.http.Method.PUT

class JasperRepositorySupport {
    private static final Logger logger = Logger.getLogger(JasperRepositorySupport.class);
    def jasperContext = 'jasperserver'
    protected def dateTimeFormatter = ISODateTimeFormat.dateTimeParser()
    protected def root = 'resources/'
    protected def tmp = 'reports/tmp'
    protected def tmpHome = tmp + '/EVA/'
    protected def finalHome = 'reports/EVA/'
    protected HTTPBuilder rest
    protected String jasperUser
    protected String ftpHost
    protected String ftpUser
    protected String ftpPwd
    protected Iterable<String> repositories

    JasperRepositorySupport(String jasperUrl, String jasperUser, String jasperPwd, String context, Iterable<String> repositories,
                            String ftpHost, String ftpUser, String ftpPwd) {
        this.repositories = repositories
        this.ftpPwd = ftpPwd
        this.ftpUser = ftpUser
        this.ftpHost = ftpHost
        this.jasperUser = jasperUser
        this.jasperContext = context
        rest = new HTTPBuilder("${jasperUrl}${jasperContext}/rest_v2/")
        rest.headers['Authorization'] = 'Basic ' + (jasperUser + ':' + jasperPwd).bytes.encodeBase64()
    }

    protected JasperRepositorySupport(HTTPBuilder rest) {
        this.rest = rest
    }

    protected void createRepositoryFolder(path, folderUri, name) {
        boolean alreadyExisting = false;
        rest.request(HEAD) {
            uri.path = path + folderUri
            response.success = { resp ->
                if (resp.status == 200) {
                    alreadyExisting = true;
                }
            }
            response.failure = {
                alreadyExisting = false;
            }
        }
        if (!alreadyExisting) {
            logger.info("Not found, creating")
            rest.request(PUT, XML) {
                headers['Content-Type'] = 'application/repository.folder+xml'

                uri.path = path + folderUri
                uri.query = ['createFolders': true, 'overwrite': true]

                body = {
                    folder {
                        uri folderUri
                        label name
                    }
                }
                response.success = { resp, xml ->
                    logger.info("Created folder: " + rest.uri + xml.uri)
                }
                response.failure = { fresp2 ->
                    throw new ScriptException("Failed to create folder at ${uri.path}: ${fresp2.status}")
                }
            }
        }
    }
}
