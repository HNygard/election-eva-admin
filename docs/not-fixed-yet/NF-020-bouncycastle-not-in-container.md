# NF-020 — BouncyCastle is `provided` but the container has no module for it

Status: OPEN
Milestone: M3

## Symptom

    WELD-000119: Not generating any bean definitions from
    no.valg.eva.admin.crypto.CmsEncoder because of underlying class loading
    error: Type org.bouncycastle.cms.CMSException not found.

    ... no.valg.eva.admin.crypto.CmsDecoder ... org.bouncycastle.util.encoders.DecoderException

Two classes. Deployment succeeds regardless — nothing has needed them yet.

## What is known

`admin-common/pom.xml` declares `bcprov-jdk15on` and `bcpkix-jdk15on` with
`<scope>provided</scope>`, so they are deliberately excluded from the WAR and
expected from the container. WildFly 13 ships no `org.bouncycastle` module, so
the real EVA Admin deployment must have had one installed — more configuration
that was not published.

The affected classes are CMS encode/decode. Related code includes
`no.valg.eva.admin.crypto` and the Buypass certificate services in
`admin-backend-common`, which are also unusable here (NF-010).

## How to approach

Leave it until something actually needs crypto. When it does, add a WildFly
module for BouncyCastle in `docker/wildfly/Dockerfile` the same way the
PostgreSQL driver is added, pinning the version the release used
(`bc-jdk15on.version` is 1.60 in the root pom).

Do not "fix" it by changing the scope in the pom: that would modify a release
file to work around a container we control.

## Done when

Either the crypto path is exercised and works, or it is established that nothing
in scope for M3 needs it, and this is closed as out of scope with that reasoning
written down.
