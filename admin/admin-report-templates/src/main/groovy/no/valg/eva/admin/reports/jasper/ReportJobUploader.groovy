package no.valg.eva.admin.reports.jasper

import groovy.util.slurpersupport.GPathResult
import groovy.xml.MarkupBuilder
import groovyx.net.http.HTTPBuilder
import org.apache.log4j.Logger
import org.joda.time.DateTime

import static groovyx.net.http.ContentType.XML
import static groovyx.net.http.Method.PUT
import static org.apache.commons.lang3.StringUtils.isNotBlank
import static org.joda.time.DateTime.now

public class ReportJobUploader extends JasperRepositorySupport {
    private static final Logger logger = Logger.getLogger(ReportJobUploader.class);
    public static final String PREGENERATED_OUTPUT = "reports/PregeneratedOutput"

    def europeanTimeZone = "Europe/Berlin"
    def reportLabel = "Scheduled report for "
    private String nightlyStartTimeHours = '2'
    private String nightlyStartTimeMinutes = '0'

    ReportJobUploader(HTTPBuilder rest, String nightlyStartTime, Iterable<String> repositories, String ftpHost, String ftpUser, String ftpPwd) {
        super(rest)
        this.repositories = repositories
        this.ftpHost = ftpHost
        this.ftpUser = ftpUser
        this.ftpPwd = ftpPwd
        (nightlyStartTimeHours, nightlyStartTimeMinutes) = nightlyStartTime.tokenize(':')
    }

    public void generateJasperReportJobs(String reportName, String reportFolderUri, GPathResult prioritizedExecutionNode, GPathResult format, List<Map<String, String>> allJobsParameterList) {
        createRepositoryFolder(root, PREGENERATED_OUTPUT, "Destination for pre-generated report output")
        allJobsParameterList.each { ids ->
            def basename = [reportName, ids['EE1'], ids['EE1.CO1'], ids['EE1.CO1.CNT1'], ids['EE1.CO1.CNT1.MUN1']].findAll({ it != null }).join("_")
            int headStartMinutes = 0;
            if (prioritizedExecutionNode != null) {
                prioritizedExecutionNode.parameter.each { param ->
                    if (ids[param.@name.text()] == param.@value.text()) {
                        headStartMinutes = Integer.valueOf(prioritizedExecutionNode.@headStartMinutes.text())
                    }
                }
            }
            StringWriter xmlFile = createJasperJobXML(reportFolderUri, reportName, ids, basename, headStartMinutes, format.text())
            createJob xmlFile, reportName, basename
        }
    }

    def StringWriter createJasperJobXML(String reportFolderUri, String reportName, Map<String, String> ids, String basename, int headStartMinutes, String format) {
        def reportDescriptionLabel = "jasperReportScheduler"

        def writer = new StringWriter()
        def xml = new MarkupBuilder(writer)
        xml.job {
            baseOutputFilename basename
            repositoryDestination {
                folderURI "/" + PREGENERATED_OUTPUT
                saveToRepository repositories.any({ it.equals("jasperserver") })
                sequentialFilenames false
                overwriteFiles true
                usingDefaultReportOutputFolderURI false
                if (repositories.any({ it.equals("ftp") })) {
                    outputFTPInfo {
                        folderPath "PregeneratedOutput"
                        implicit "true"
                        password ftpPwd
                        pbsz "0"
                        port "21"
                        serverName ftpHost
                        type "ftp"
                        userName ftpUser
                    }
                }
            }
            label reportLabel + basename
            description reportDescriptionLabel
            outputFormats {
                outputFormat isNotBlank(format) ? format.toUpperCase() : 'PDF'
            }
            outputTimeZone europeanTimeZone
            source {
                parameters {
                    parameterValues {
                        ids.each { id ->
                            entry {
                                key id.key
                                value("xsi:type": "xs:string", "xmlns:xsi": "http://www.w3.org/2001/XMLSchema-instance", "xmlns:xs": "http://www.w3.org/2001/XMLSchema", id.value)
                            }
                        }
                    }
                }
                reportUnitURI "/" + finalHome + reportFolderUri + "/" + reportName
            }
            simpleTrigger {
                timezone europeanTimeZone
                startType 2
                misfireInstruction 0
                occurrenceCount "-1"
                recurrenceInterval 1
                recurrenceIntervalUnit "DAY"

                def startTime = nextMidNight().plusHours(this.nightlyStartTimeHours.toInteger()).plusMinutes(this.nightlyStartTimeMinutes.toInteger()).plusMinutes(-headStartMinutes)
                if (startTime.isAfter((now().plusDays(1)))) {
                    startTime = startTime.minusDays(1);
                }
                startDate startTime.toString()
            }
            username jasperUser
        }
        return writer
    }

    private DateTime nextMidNight() {
        now().plusDays(1).withTimeAtStartOfDay()
    }


    def createJob(StringWriter xmlFile, String reportName, String municipalityId) {
        try {
            logger.info("Uploading job ${reportName} for ${municipalityId}")
            rest.request(PUT, XML) {
                uri.path = "jobs"
                headers['Content-Type'] = 'application/xml'
                body = xmlFile.toString()

                response.success = { resp, xml ->
                    logger.info("Done uploading job...${xml.uri}")
                }
                response.failure = { error ->
                    logger.error("Uploading job failed" + error.status)
                    throw new ScriptException("Failed to upload report template to ${uri.path}: ${error.status}")
                }

            }
        } catch (Throwable t) {
            logger.error(t.getMessage())
        }
    }
}
