# Running EVA admin without Maven installed in a docker container

Create common repo for Maven downloads. This volume will cache dependencies Maven downloads.

`docker volume create --name maven-repo`

Add the non-maven-central dependency (uk.org.mygrid.resources.jcoord:jcoord) to our maven repo. The JAR for this dependency is part of the EVA Admin release.
```
docker run -it --rm --name eva-admin -v maven-repo:/root/.m2 --volume `pwd`:/usr/src/eva-admin -w /usr/src/eva-admin maven:3-jdk-8 mvn install:install-file \
   -Dfile=/usr/src/eva-admin/admin-maven-repository/uk/org/mygrid/resources/jcoord/jcoord/1.0/jcoord-1.0.jar \
   -DgroupId=uk.org.mygrid.resources.jcoord \
   -DartifactId=jcoord \
   -Dversion=1.0 \
   -Dpackaging=jar \
   -DgeneratePom=true
```

Running 'mvn clean install'

```
docker run -it --rm --name eva-admin \
    --volume maven-repo:/root/.m2 \
    --volume `pwd`:/usr/src/eva-admin \
    -w /usr/src/eva-admin maven:3-jdk-8 \
    mvn -Dmaven.test.skip clean install
```

Looking at the result (I used it to find out what mvn created):
```
docker run -it --rm --name eva-admin \
    --volume maven-repo:/root/.m2 \
    maven:3-jdk-8 \
    find /root/.m2|grep eva
```

Running the application server Wildfly (JBoss):
```
docker build --tag=jboss/wildfly-eva-admin added-stuff/jboss
docker run -p 8080:8080 -p 9990:9990 \
    --volume maven-repo:/.m2 \
    -it jboss/wildfly-eva-admin
```


## Notes - build and start up
- Running mvn without target will list the possible operations. Good to have here in the notes:
```
[ERROR] No goals have been specified for this build. You must specify a valid lifecycle phase or a goal in the 
format <plugin-prefix>:<goal> or <plugin-group-id>:<plugin-artifact-id>[:<plugin-version>]:<goal>. Available 
lifecycle phases are: validate, initialize, generate-sources, process-sources, generate-resources, 
process-resources, compile, process-classes, generate-test-sources, process-test-sources, generate-test-resources,
process-test-resources, test-compile, process-test-classes, test, prepare-package, package, pre-integration-test,
integration-test, post-integration-test, verify, install, deploy, pre-clean, clean, post-clean, pre-site, site,
post-site, site-deploy. -> [Help 1]
```

- Could not run 'maven:latest' as this is using JDK 11. Got a null pointer in Surefire. This is fixed in later versions of Surefire.

```
[ERROR] Failed to execute goal org.apache.maven.plugins:maven-surefire-plugin:2.20.1:test (default-test) on project admin-common: Execution default-test of goal org.apache.maven.plugins:maven-surefire-plugin:2.20.1:test failed.: NullPointerException -> [Help 1]
org.apache.maven.lifecycle.LifecycleExecutionException: Failed to execute goal org.apache.maven.plugins:maven-surefire-plugin:2.20.1:test (default-test) on project admin-common: Execution default-test of goal org.apache.maven.plugins:maven-surefire-plugin:2.20.1:test failed.
    at org.apache.maven.lifecycle.internal.MojoExecutor.execute (MojoExecutor.java:215)
    at org.apache.maven.lifecycle.internal.MojoExecutor.execute (MojoExecutor.java:156)
    at org.apache.maven.lifecycle.internal.MojoExecutor.execute (MojoExecutor.java:148)
    (...)
Caused by: org.apache.maven.plugin.PluginExecutionException: Execution default-test of goal org.apache.maven.plugins:maven-surefire-plugin:2.20.1:test failed.
    at org.apache.maven.plugin.DefaultBuildPluginManager.executeMojo (DefaultBuildPluginManager.java:148)
    at org.apache.maven.lifecycle.internal.MojoExecutor.execute (MojoExecutor.java:210)
    at org.apache.maven.lifecycle.internal.MojoExecutor.execute (MojoExecutor.java:156)
    (...)
Caused by: java.lang.NullPointerException
    at org.apache.maven.surefire.shade.org.apache.commons.lang3.SystemUtils.isJavaVersionAtLeast (SystemUtils.java:1626)
    at org.apache.maven.plugin.surefire.AbstractSurefireMojo.getEffectiveJvm (AbstractSurefireMojo.java:2107)
    at org.apache.maven.plugin.surefire.AbstractSurefireMojo.getForkConfiguration (AbstractSurefireMojo.java:1976)
    (...)

```

- There is one dependency that is not in Maven Central. Adding it to the Maven repo manually.
- EvoteProperties is crashing during construction. It requires properties to be defined. This will result in failing "mvn clean install". This command should not fail in a normal Maven based Java project. Skipping tests for now. Had to remove stuff from POM files for scope=tests.
Some of the error messages:
```
java.lang.NoClassDefFoundError: Could not initialize class no.valg.eva.admin.util.IOUtil
	at no.evote.service.impl.CryptoServiceBeanTest.readResourceAsBytes(CryptoServiceBeanTest.java:195)
	at no.evote.service.impl.CryptoServiceBeanTest.verifyScanningCountSignature_signatureIsABuypassSignatureCrlValididationFailsDueToMissingProvider_returnsFalse(CryptoServiceBeanTest.java:135)
... Removed 30 stack frames


java.lang.ExceptionInInitializerError
	at no.valg.eva.admin.backend.common.port.adapter.service.buypass.CertificateRevocationListService.<init>(CertificateRevocationListService.java:38)
	at no.valg.eva.admin.backend.common.port.adapter.service.buypass.CertificateRevocationListServiceTest$1.<init>(CertificateRevocationListServiceTest.java:71)
	at no.valg.eva.admin.backend.common.port.adapter.service.buypass.CertificateRevocationListServiceTest.makeCertificateRevocationListService(CertificateRevocationListServiceTest.java:71)
	at no.valg.eva.admin.backend.common.port.adapter.service.buypass.CertificateRevocationListServiceTest.readCrlFromBuypass_noUrlFoundForPrincipal_returnsCrl(CertificateRevocationListServiceTest.java:46)
Caused by: java.lang.IllegalArgumentException: EVOTE_PROPERTIES is not defined
	at no.evote.util.EvoteProperties.readProperties(EvoteProperties.java:92)
	at no.evote.util.EvoteProperties.<clinit>(EvoteProperties.java:84)
	... 34 more
... Removed 30 stack frames


java.lang.NoClassDefFoundError: Could not initialize class no.evote.util.EvoteProperties
	at no.valg.eva.admin.backend.common.port.adapter.service.buypass.CertificateRevocationListService.<init>(CertificateRevocationListService.java:38)
	at no.valg.eva.admin.backend.common.port.adapter.service.buypass.CertificateRevocationListServiceTest$1.<init>(CertificateRevocationListServiceTest.java:71)
	at no.valg.eva.admin.backend.common.port.adapter.service.buypass.CertificateRevocationListServiceTest.makeCertificateRevocationListService(CertificateRevocationListServiceTest.java:71)
	at no.valg.eva.admin.backend.common.port.adapter.service.buypass.CertificateRevocationListServiceTest.readCrlFromBuypass_returnsCrl(CertificateRevocationListServiceTest.java:36)
... Removed 30 stack frames
```

- Got docker container setup so that JBoss WildFly starts. Got this exception on both WAR files admin-frontend and 
admin-backend when trying to start.
```
10:55:43,099 ERROR [org.jboss.msc.service.fail] (MSC service thread 1-3) MSC000001: Failed to start service jboss.deployment.unit."admin-frontend-2019.22-SNAPSHOT.war".PARSE: org.jboss.msc.service.StartException in service jboss.deployment.unit."admin-frontend-2019.22-SNAPSHOT.war".PARSE: WFLYSRV0153: Failed to process phase PARSE of deployment "admin-frontend-2019.22-SNAPSHOT.war"
        at org.jboss.as.server@9.0.2.Final//org.jboss.as.server.deployment.DeploymentUnitPhaseService.start(DeploymentUnitPhaseService.java:183)
        at org.jboss.msc@1.4.8.Final//org.jboss.msc.service.ServiceControllerImpl$StartTask.startService(ServiceControllerImpl.java:1737)
        at org.jboss.msc@1.4.8.Final//org.jboss.msc.service.ServiceControllerImpl$StartTask.execute(ServiceControllerImpl.java:1699)
        at org.jboss.msc@1.4.8.Final//org.jboss.msc.service.ServiceControllerImpl$ControllerTask.run(ServiceControllerImpl.java:1557)
        at org.jboss.threads@2.3.3.Final//org.jboss.threads.ContextClassLoaderSavingRunnable.run(ContextClassLoaderSavingRunnable.java:35)
        at org.jboss.threads@2.3.3.Final//org.jboss.threads.EnhancedQueueExecutor.safeRun(EnhancedQueueExecutor.java:1982)
        at org.jboss.threads@2.3.3.Final//org.jboss.threads.EnhancedQueueExecutor$ThreadBody.doRunTask(EnhancedQueueExecutor.java:1486)
        at org.jboss.threads@2.3.3.Final//org.jboss.threads.EnhancedQueueExecutor$ThreadBody.run(EnhancedQueueExecutor.java:1377)
        at java.base/java.lang.Thread.run(Thread.java:834)
Caused by: org.jboss.as.server.deployment.DeploymentUnitProcessingException: WFLYUT0027: Failed to parse XML descriptor "/content/admin-frontend-2019.22-SNAPSHOT.war/WEB-INF/web.xml" at [2,0]
        at org.wildfly.extension.undertow@17.0.1.Final//org.wildfly.extension.undertow.deployment.WebParsingDeploymentProcessor.deploy(WebParsingDeploymentProcessor.java:134)
        at org.jboss.as.server@9.0.2.Final//org.jboss.as.server.deployment.DeploymentUnitPhaseService.start(DeploymentUnitPhaseService.java:176)
        ... 8 more
Caused by: com.ctc.wstx.exc.WstxEOFException: Unexpected EOF in prolog
 at [row,col {unknown-source}]: [2,0]
        at org.codehaus.woodstox@5.0.3//com.ctc.wstx.sr.StreamScanner.throwUnexpectedEOF(StreamScanner.java:687)
        at org.codehaus.woodstox@5.0.3//com.ctc.wstx.sr.BasicStreamReader.handleEOF(BasicStreamReader.java:2220)
        at org.codehaus.woodstox@5.0.3//com.ctc.wstx.sr.BasicStreamReader.nextFromProlog(BasicStreamReader.java:2126)
        at org.codehaus.woodstox@5.0.3//com.ctc.wstx.sr.BasicStreamReader.next(BasicStreamReader.java:1181)
        at org.jboss.metadata.web@13.0.0.Final//org.jboss.metadata.parser.servlet.WebMetaDataParser.parse(WebMetaDataParser.java:63)
        at org.jboss.metadata.web@13.0.0.Final//org.jboss.metadata.parser.servlet.WebMetaDataParser.parse(WebMetaDataParser.java:51)
        at org.wildfly.extension.undertow@17.0.1.Final//org.wildfly.extension.undertow.deployment.WebParsingDeploymentProcessor.deploy(WebParsingDeploymentProcessor.java:96)
        ... 9 more
```

## Notes - documentation

- Using Wildfly. "EVA Admin kjører i Wildfly som implementerer Java EE spesifikasjonen.", eva-admin-2019-systemdokumentasjon.pdf
- JasperSoft mentioned in documentation: "Rapporter genereres med JasperSoft rapportserver som er koblet 1:1 med hver backend node.", eva-admin-2019-systemdokumentasjon.pdf