# NF-005 — No beans.xml anywhere, so CDI is off

Status: FIXED
Milestone: M2

## Symptom

    find . -name beans.xml   →   (nothing)

Without a CDI bean archive marker, injection does not happen. Expect
`NullPointerException` on injected fields, or `WELD-001408 Unsatisfied
dependencies` once deployment gets far enough to try.

## What is known

The codebase is thoroughly CDI-based: 93 classes carry `@Stateless`/`@Stateful`,
and the frontend has `no.valg.eva.admin.frontend.cdi.BeanManager`. Under Java EE 7
an archive with bean-defining annotations is an implicit bean archive, but this
code predates relying on that, and several producers (for example
`no.evote.util.EvaPropertiesProducer`) suggest an explicit archive with a
declared discovery mode.

`origin/attempt` commit `d2c1878` "Changing DI for to get stuff going" is the
relevant reference.

## How to approach

Determine which archives need markers: both WARs, and any JAR whose beans must be
discovered. Then decide `bean-discovery-mode` deliberately — `all` will try to
manage everything on the classpath, `annotated` only annotated types. Record why.

Every `beans.xml` is a new file: declare each as `ADDED` with its reason.

## Done when

Deployment proceeds past CDI initialisation, with the Weld startup lines quoted in
a finding.
