# NF-017 — WildFly does not expose Hibernate's API to the deployment

Status: FIXED
Milestone: M2

## Symptom

    WELD-000119: Not generating any bean definitions from
    no.valg.eva.admin.backend.common.repository.GenericRepository because of
    underlying class loading error: Type org.hibernate.Session from
    [Module "deployment.admin-backend-2019.22-SNAPSHOT.war"] not found.

The whole repository layer, plus `no.evote.service.ExportServiceBean` and
`ExportImportOperatorsServiceBean` which use `org.hibernate.jdbc.Work`.

## Cause

WildFly's JPA subsystem provides Hibernate internally but exposes only
`javax.persistence` to a deployment. EVA Admin uses the Hibernate API directly,
so it has to ask for the module.

## Resolution

`admin/admin-backend/src/main/webapp/WEB-INF/jboss-deployment-structure.xml`
declaring a dependency on `org.hibernate`. The release must have carried an
equivalent descriptor, or a `Dependencies:` manifest entry; neither was published.

## Note for later

Two classes still report WELD-000119, for BouncyCastle:

    no.valg.eva.admin.crypto.CmsEncoder   needs org.bouncycastle.cms.CMSException
    no.valg.eva.admin.crypto.CmsDecoder   needs org.bouncycastle.util.encoders.DecoderException

`bcprov-jdk15on` and `bcpkix-jdk15on` are `provided` scope in admin-common's pom,
so they are expected from the container and are not in the WAR. WildFly 13 has no
`org.bouncycastle` module as standard, so the real deployment must have had one
installed. It does not block startup, and nothing has exercised the crypto path
yet, so it is left alone until something needs it. Tracked in NF-020.
