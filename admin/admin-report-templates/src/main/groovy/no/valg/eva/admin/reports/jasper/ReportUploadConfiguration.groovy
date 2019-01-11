package no.valg.eva.admin.reports.jasper


public class ReportUploadConfiguration {
    private List categoryFolders = []
    private List reportTemplates = []
    private Map<String, byte[]> resourceBundles = new HashMap()

    public ReportUploadConfiguration(InputStream configStream) {
        def config = new XmlSlurper().parse(configStream)
        config.categories.category.each { category ->
            categoryFolders.add([
                    name  : category.@name.text(),
                    uri   : category.@uri.text(),
                    access: category.@access.text()
            ])
        }
        config.templates.template.each { template ->
            reportTemplates.add([
                    path            : template.@path.text(),
                    replaceWithBlank: "true" == template.@replaceWithBlank.text(),
                    reportMetaData: template.reportMetaData
            ])
        }
    }

    List getCategoryFolders() {
        return categoryFolders
    }

    List getReportTemplates() {
        return reportTemplates
    }
}
