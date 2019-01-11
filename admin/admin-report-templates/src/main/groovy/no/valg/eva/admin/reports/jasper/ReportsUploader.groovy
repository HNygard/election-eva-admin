package no.valg.eva.admin.reports.jasper

import groovy.text.SimpleTemplateEngine
import groovy.xml.MarkupBuilder
import groovy.xml.StreamingMarkupBuilder
import groovyx.net.http.HTTPBuilder
import org.apache.commons.lang3.tuple.Pair
import org.apache.log4j.Logger
import org.joda.time.DateTime

import static groovy.xml.XmlUtil.serialize
import static groovyx.net.http.ContentType.XML
import static groovyx.net.http.Method.*

class ReportsUploader extends JasperRepositorySupport {
    public static final HashSet MUNICIPALITY_PATH_PARAMETERS = new HashSet(["EE1", "EE1.CO1", "EE1.CO1.CNT1", "EE1.CO1.CNT1.MUN1"])
    protected String templateSourceDir = null
    private static final Logger logger = Logger.getLogger(ReportsUploader.class);
    private Object commonInputControls

    static final dataTypes = [
            int : [label: 'Integer', type: 'number', javaType: "java.lang.Long"],
            long: [label: 'Long', type: 'number', javaType: "java.lang.Long"],
            text: [label: 'Text', type: 'text', javaType: "java.lang.String"],
            date: [label: 'Date', type: 'date', javaType: "java.util.Date"]
    ]

    static final
            dataTypeMap = ['java.lang.String': 'text', 'java.lang.Integer': 'int', 'java.lang.Long': 'long', 'java.util.Date': 'date', 'java.lang.Boolean': 'text']

    static final blankTemplate = "Rapport 0/0 Blank.jrxml"

    def dbHost = "admindb"
    def dbPort = "5432"
    def dbUser = 'adminrep'
    def dbPwd = "adminrep"

    def dataSources = 'resources/datasources'
    def dataSourceLabel = 'Evote_datasource'
    def resourceBundleFolder = '/reports/EVA/resources'
    private String nightlyStartTime
    private boolean useLiveTemplates

    ReportsUploader(String jasperUrl, String jasperUser, String jasperPwd, String context, String nightlyStartTime,
                    String templateSourceDir, boolean useLiveTemplates,
                    Iterable<String> repositories, String ftpHost, String ftpUser, String ftpPwd) {
        super(jasperUrl, jasperUser, jasperPwd, context, repositories, ftpHost, ftpUser, ftpPwd)
        this.nightlyStartTime = nightlyStartTime
        this.templateSourceDir = templateSourceDir;
        this.useLiveTemplates = useLiveTemplates;
    }

    public void uploadAllTemplates(
            ReportUploadConfiguration configuration,
            List<Pair<String, byte[]>> resourceBundles,
            boolean resourceBundlesOnly,
            DateTime latestTextTimestamp,
            List<Map<String, String>> allMunicipalitiesParameters,
            List<Map<String, String>> allCountiesParameters) {

        if (!resourceBundlesOnly) {
            deleteTemporaryHome()
        }

        logger.info "Uploading resource bundles"
        def resourcesPath = root + tmpHome + '/resources'
        if (resourceBundlesOnly) {
            resourcesPath = root + resourceBundleFolder
        }
        def uploadedResourceBundleUris = uploadResourceBundles(
                resourceBundles,
                resourcesPath,
                resourceBundlesOnly,
                root,
                resourceBundleFolder,
                latestTextTimestamp)

        def discoveredNewResourceBundles = uploadedResourceBundleUris.any { it.newResource }
        // due to a bug in jasperserver, if we now have actually added er resource bundle, most probably because a new
        // election event has been added we need to re-create the entire template structure. Otherwise, sub report templates
        // disappears for some reason. 

        if (discoveredNewResourceBundles) {
            resourcesPath = root + tmpHome + '/resources'
            uploadedResourceBundleUris = uploadResourceBundles(
                    resourceBundles,
                    resourcesPath,
                    resourceBundlesOnly,
                    root,
                    resourceBundleFolder,
                    latestTextTimestamp)

        }
        if (!resourceBundlesOnly || discoveredNewResourceBundles) {
            logger.info "Looking for existing data source"
            if (needToCreateDataSource()) {
                logger.info "Creating data source"
                createDataSource()
            }

            logger.info "Uploading data types"
            uploadDataTypes(dataTypes)

            logger.info "Collecting report parameter types"
            commonInputControls = collectParameterTypesFromTemplates(configuration.reportTemplates)

            logger.info "Uploading input controls"
            uploadInputControls(commonInputControls)

            // create category folders
            logger.info "Uploading report category folders"
            uploadReportCategories(configuration.categoryFolders)

            // post metadata files
            logger.info "Uploading templates and metadata"
            def (templateMetaData, parameterNames) = uploadReportTemplates(configuration.reportTemplates, uploadedResourceBundleUris)

            logger.info "Uploading version info"
            uploadVersionInfo()

            logger.info "Moving to final location"
            moveToFinalLocation()

            logger.info "Deleting temporary folder"
            deleteTemporaryHome()

            logger.info "Generating scheduled jobs"
            def jobUploader = new ReportJobUploader(rest, this.nightlyStartTime, repositories, ftpHost, ftpUser, ftpPwd)
            configuration.reportTemplates.each { template ->
                if ('true' == template.reportMetaData.runNightly.text()) {
                    jobUploader.generateJasperReportJobs(
                            templateMetaData[template].reportId.text(),
                            templateMetaData[template].reportUri.text(),
                            templateMetaData[template].prioritizedExecution,
                            templateMetaData[template].format,
                            new HashSet(parameterNames[template]).equals(MUNICIPALITY_PATH_PARAMETERS) ? allMunicipalitiesParameters : allCountiesParameters)
                }
            }
            logger.info "Done uploading report templates and jobs"
        }
    }

    private void moveToFinalLocation() {
        rest.request(PUT, XML) {
            headers['Content-Location'] = '/reports/tmp/EVA'
            uri.path = 'resources/reports'
            uri.query = [overwrite: "true"]
            response.success = { resp, xml ->
                logger.info("Moved to " + rest.uri + uri.path)
            }
            response.failure = { fresp ->
                throw new ScriptException("Move from temporary folder to final folder failed: ${fresp.status}")
            }
        }
    }

    private void uploadVersionInfo() {
        def versionInfoResource = getClass().getClassLoader().getResource("version-info.xml")
        rest.request(POST, XML) {
            headers['Content-Type'] = 'application/repository.file+xml'
            uri.path = root + tmpHome + "/versionInfo"
            body = {
                file {
                    label 'version-info.xml'
                    type 'xml'
                    content new SimpleTemplateEngine().createTemplate(versionInfoResource).make().toString().bytes.encodeBase64()
                }
            }
            response.success = { resp, xml ->
                logger.info("Uploaded version info to " + rest.uri + uri.path)
            }
            response.failure = { fresp ->
                throw new ScriptException("Failed to upload version info to ${uri.path}: ${fresp.status}")
            }
        }
    }

    private String replaceSubReportAbsoluteFilePaths(String xmlContent) {
        def template = new XmlSlurper().parseText(xmlContent)
        def subReportExpressions = template.'**'.findAll { node ->
            node.name() == 'subreportExpression';
        }
        subReportExpressions.each {
            // If sub report reference does not already contain repo: reference, it it probably a windows path. 
            // Extract file name part in that case
            if (!it.text().contains("repo:")) {
                def subReportFileName = it.text().split("\\\\|/").last().replace(".jasper", "").replace(".jrxml", "").replace("\"", "")
                it.replaceBody "\"repo:" + subReportFileName + "\""
            }
        }

        def streamingMarkupBuilder = new StreamingMarkupBuilder()
        streamingMarkupBuilder.encoding = "UTF-8"
        return serialize(streamingMarkupBuilder.bind { mkp.yield template }.toString())
    }

    private String uploadJrXml(HTTPBuilder rest, String reportUri, String reportId, String jrxmlFile, String home, logger) {
        rest.request(POST, XML) {
            headers['Content-Type'] = 'application/repository.file+xml' // but it isn't plain XML
            uri.path = root + tmpHome + reportUri
            uri.query = ['createFolders': true]
            body = {
                file {
                    label reportId
                    type 'jrxml'
                    content jrxmlFile.bytes.encodeBase64()
                }
            }
            response.success = { resp, xml ->
                return xml.uri
            }
            response.failure = { fresp ->
                throw new ScriptException("Failed to upload report template to ${uri.path}: ${fresp.status}")
            }
        }
    }

    private boolean needToCreateDataSource() {
        boolean needCreateDatasource = false;
        rest.request(GET, XML) {
            uri.path = dataSources + "/" + dataSourceLabel
            response.success = { resp, xml ->
                logger.info "Found " + xml.label;
            }
            response.failure = { resp ->
                if (resp.status == 404) {
                    needCreateDatasource = true;
                }
            }
        }
        return needCreateDatasource
    }

    private void createDataSource() {
        logger.info("Creating data source")
        rest.request(POST, XML) {
            headers['Content-Type'] = 'application/repository.jdbcDataSource+xml'
            uri.path = dataSources
            body = {
                jdbcDataSource {
                    label this.dataSourceLabel
                    connectionUrl 'jdbc:postgresql://' + dbHost + ':' + dbPort + '/evote'
                    driverClass 'org.postgresql.Driver'
                    username dbUser
                    password dbPwd
                }
            }
            response.success = { resp, xml ->
                logger.info("Created datasource " + rest.uri + xml.uri)
            }
            response.failure = { fresp ->
                throw new ScriptException("Failed to create datasource ${uri.path}: ${fresp.status}")
            }
        }
    }

    private Map<String, LinkedHashMap<String, String>> uploadDataTypes(LinkedHashMap<String, LinkedHashMap<String, String>> dataTypes) {
        dataTypes.each { dType ->
            rest.request(POST, XML) {
                headers['Content-Type'] = 'application/repository.dataType+xml'
                uri.path = root + tmpHome + "/datatypes"
                body = {
                    dataType {
                        label '' + dType.value.label
                        type '' + dType.value.type
                    }
                }
                response.success = { resp, xml ->
                    dType.value.uri = '' + xml.uri;
                    logger.info "Created datatype: ${xml.uri}"
                }
                response.failure = { fresp ->
                    throw new ScriptException("Failed to upload datatype ${dType.value.label}: ${fresp.status}")
                }
            }
        }
    }

    private Object deleteTemporaryHome() {
        rest.request(DELETE) {
            uri.path = root + tmp
            response.success = { resp, xml ->
                logger.info "Deleted temporary folder"
            }
            response.failure = { fresp ->
                logger.info("Failed to delete ${uri.path}: ${fresp.status}")
            }
        }
    }

    private Object collectParameterTypesFromTemplates(List<String> templates) {
        def commonInputControls = new HashMap()
        Set allPars = new TreeSet()
        templates.each { template ->
            if (!shouldReplaceWithBlankTemplate(template)) {
                def resource = getReportTemplateUrl(template.path)
                if (resource == null) {
                    throw new ScriptException("Missing resource " + template.path);
                } else {
                    def templateContent = resource.text
                    def parameters = new XmlSlurper().parseText(templateContent).parameter
                    parameters.each { p ->
                        allPars.add('' + p.@class + ' ' + p.@name)
                        commonInputControls[p.@name.toString()] = [label: p.@name, type: dataTypeMap[p.@class.toString()], uri: dataTypes[dataTypeMap[p.@class.toString()]].uri]
                    }
                }
            }
        }
        commonInputControls
    }

    private uploadReportTemplates(List templates, uploadedResourceBundleUris) {
        def Map<Object, Object> templateToMetadata = [:]
        def Map<Object, Set<String>> parameterNames = [:]
        templates.each { template ->
            def actualTemplate = template.path
            if (shouldReplaceWithBlankTemplate(template)) {
                actualTemplate = blankTemplate
            }
            def jrXmlUri = ''
            def metaData = template.reportMetaData

            if (shouldReplaceWithBlankTemplate(template)) {
                metaData.runNightly.replaceNode {}
            }
            if (shouldReplaceWithBlankTemplateButNotHere(template)) {
                metaData.description.replaceBody 'Denne rapporten er ennå ikke frigjort, men er tilgjengelig her for testing'
            }
            def streamingMarkupBuilder = new StreamingMarkupBuilder()
            streamingMarkupBuilder.encoding = "UTF-8"
            def metaDataContent = serialize(streamingMarkupBuilder.bind {
                mkp.yield metaData
            }.toString())

            templateToMetadata[template] = metaData
            def metaFileUri = ''
            rest.request(POST, XML) {
                headers['Content-Type'] = 'application/repository.file+xml'
                uri.path = root + tmpHome + metaData.reportUri + '/metadata'
                uri.query = ['createFolders': true]
                body = {
                    file {
                        label 'metadata.xml'
                        type 'xml'
                        content metaDataContent.bytes.encodeBase64()
                    }
                }
                response.success = { resp, xml ->
                    logger.info("Uploaded metadata to " + rest.uri + uri.path)
                    metaFileUri = '' + xml.uri
                }
                response.failure = { fresp ->
                    throw new ScriptException("Failed to metadata to ${uri.path}: ${fresp.status}")
                }
            }

            // upload jrxml file
            def reportFilesUri = metaData.reportUri.toString()

            // find sub report references and replace absolute path with jasper server repository reference
            def reportTemplateResource = getReportTemplateUrl(actualTemplate)
            def xmlContent = reportTemplateResource.text
            String result = replaceSubReportAbsoluteFilePaths(xmlContent)

            jrXmlUri = uploadJrXml(rest, reportFilesUri, metaData.reportId.toString() + "_template", result, tmpHome.toString(), logger)

            // now to create the report itself
            rest.request(POST, XML) {
                headers['Content-Type'] = 'application/repository.reportUnit+xml' // but it isn't plain XML

                uri.path = root + tmpHome + metaData.reportUri
                uri.query = ['createFolders': true]
                def writer = new StringWriter()
                def reportUnitXml = new MarkupBuilder(writer)
                reportUnitXml.reportUnit {
                    label '' + metaData.reportId
                    jrxmlFileReference {
                        uri jrXmlUri
                    }
                    dataSourceReference {
                        uri "/datasources/" + dataSourceLabel
                    }
                    resources {
                        resource {
                            name 'metaData'
                            fileReference {
                                uri metaFileUri
                            }
                        }
                        uploadedResourceBundleUris.each { uploadedResourceBundleUri ->
                            resource {
                                name uploadedResourceBundleUri.fileLabel
                                fileReference {
                                    uri uploadedResourceBundleUri.uri
                                }
                            }
                        }
                    }

                    // find input controls
                    def jrxmlXml = new XmlSlurper(false, true).parseText(xmlContent)
                    def parameters = jrxmlXml.parameter;

                    inputControls {
                        parameters.each { p ->
                            def parameter = this.commonInputControls['' + p.@name]
                            if (!('false' == '' + p.@isForPrompting)) {
                                List params = parameterNames.get(template, [])
                                params.add p.@name.text()
                                parameterNames[template] = params
                                inputControlReference {
                                    uri parameter.uri
                                }
                            }
                        }
                    }
                }
                body = writer.toString()
                response.success = { resp, xml ->
                    logger.info("Uploaded template " + actualTemplate + " to " + rest.uri + uri.path)
                }
                response.failure = { fresp ->
                    throw new ScriptException("Failed to upload version info to ${uri.path}: ${fresp.status}")
                }
            }

            // also upload any sub reports
            if (actualTemplate != blankTemplate) {
                metaData.subreport.each { subreport ->
                    def subReportResourceName = '' + subreport.@jrxml
                    def subReportPath = actualTemplate.substring(0, actualTemplate.lastIndexOf('/') + 1) + subReportResourceName
                    def resource = getReportTemplateUrl(subReportPath)
                    if (resource == null) {
                        throw new ScriptException("Couldn't find subreport ${subReportPath} of report ${actualTemplate}")
                    }
                    String content = resource.text;
                    logger.debug "Parsing ${subReportResourceName}"
                    String updatedXml = replaceSubReportAbsoluteFilePaths(content)
                    uploadJrXml(rest, reportFilesUri + '/' + metaData.reportId + '_files', '' + subreport.@name, updatedXml, tmpHome, logger)
                }
            }
        }
        return [templateToMetadata, parameterNames]
    }

    private boolean shouldReplaceWithBlankTemplate(template) {
        return template.replaceWithBlank && !useLiveTemplates
    }

    private boolean shouldReplaceWithBlankTemplateButNotHere(template) {
        return template.replaceWithBlank && useLiveTemplates
    }

    private URL getReportTemplateUrl(template) {
        this.templateSourceDir ? new File("${this.templateSourceDir}/${template}").toURI().toURL() : getClass().getClassLoader().getResource(template)
    }

    private uploadReportCategories(List<Map<String, String>> categoryFolders) {
        categoryFolders.each { categoryFolder ->
            createRepositoryFolder(root + tmpHome, categoryFolder.uri, categoryFolder.name)

            def writer = new StringWriter()
            def metaDataContent = new MarkupBuilder(writer)
            metaDataContent.folderMetaData {
                access categoryFolder.access
            }

            // upload folder metadata
            rest.request(POST, XML) {
                headers['Content-Type'] = 'application/repository.file+xml'

                uri.path = root + tmpHome + categoryFolder.uri
                uri.query = ['createFolders': true]

                body = {
                    file {
                        label 'metadata.xml'
                        type 'xml'
                        content writer.toString().bytes.encodeBase64()
                    }
                }
                response.success = { resp, xml ->
                    logger.info("Created folder metadata: ${rest.uri}${xml.uri}")
                }
                response.failure = { fresp ->
                    throw new ScriptException("Failed to upload report template content ${uri.path}: ${fresp.status}")
                }
            }
        }
    }


    private Object uploadInputControls(commonInputControls) {
        commonInputControls.each { ic ->
            rest.request(POST, XML) {
                headers['Content-Type'] = 'application/repository.inputControl+xml'
                uri.path = root + tmpHome + "inputControls/"
                body = {
                    inputControl {
                        label ic.key
                        description ic.value.label
                        type '2'  // single value
                        dataTypeReference {
                            uri ic.value.uri
                        }
                        mandatory true
                        visible true
                    }
                }
                response.success = { resp, xml ->
                    ic.value.uri = xml.uri
                }
                response.failure = { fresp ->
                    logger.info("Failed to upload input control ${ic.value.label} to ${uri.path}: ${fresp.status}")
                }
            }
        }
    }

// Uploads a set of resources files to the JasperServer. Each resourceBundle is in the format <name>@<absolute_file_path> 
    def uploadResourceBundles(List<Pair<String, byte[]>> resourceBundles, path, boolean replace, resourcesRoot, resourceFolder, DateTime latestTextTimestamp) {
        def uploadedResourceBundleUris = [];
        resourceBundles.each { resourceBundle ->
            String fileLabel = resourceBundle.key
            byte[] bundleBytes = resourceBundle.value
            boolean skipResource = false;
            boolean newResource = false;
            def resourceVersion = "0";
            if (replace) {
                // determine existing version number
                rest.request(GET, XML) {
                    uri.path = resourcesRoot
                    uri.query = [folderUri: resourceFolder, q: fileLabel]

                    response.success = { resp, xml ->
                        if (resp.status == 200) {
                            try {
                                resourceVersion = xml.resourceLookup.version.text()
                                if (latestTextTimestamp && latestTextTimestamp.isBefore(DateTime.parse(xml.resourceLookup.updateDate.text(), dateTimeFormatter))) {
                                    skipResource = true;
                                }
                                logger.info("Determined previous version of ${fileLabel}: ${resourceVersion}")
                            } catch (NullPointerException e) {
                                logger.error("Error while checking version of ${fileLabel}", e)
                            }
                        }
                        if (resp.status == 204) {
                            newResource = true;
                        }
                    }

                    response.failure = { fresp ->
                        if (resp.status == 404) {
                            newResource = true;
                        } else {
                            throw new ScriptException("Failed to determine existing version of ${fileLabel}, response status was ${fresp.status}")
                        }
                    }
                }
            }
            if (!skipResource) {
                rest.request(PUT, XML) {
                    headers['Content-Type'] = 'application/repository.file+xml'
                    uri.path = path + "/" + fileLabel
                    uri.query = ['createFolders': true, 'overwrite': true]
                    body = {
                        file {
                            label fileLabel
                            version resourceVersion
                            type 'prop'
                            content bundleBytes.encodeBase64()
                        }
                    }
                    response.success = { resp, xml ->
                        logger.info("Uploaded resource bundle to ${uri.path}")
                        uploadedResourceBundleUris.add([fileLabel: fileLabel, uri: xml.uri.text(), newResource: newResource])
                    }
                    response.failure = { fresp ->
                        throw new ScriptException("Failed to upload resource to ${uri.path}: ${fresp.status}")
                    }
                }
            }
        }
        return uploadedResourceBundleUris;
    }
}
