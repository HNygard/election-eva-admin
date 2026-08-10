# NF-008 — Around 340 classes lack the no-arg constructors CDI needs

Status: OPEN
Milestone: M2

## Symptom

Not yet observed here, because deployment does not get that far. On
`origin/attempt` it was the bulk of the work: CDI and EJB proxying require a
non-private constructor with no parameters, and these classes only have
constructor injection.

## What is known

`git diff master origin/attempt --stat` shows 342 files changed, +2266/-301.
Sampling the diff shows the recurring pattern is a protected or public no-arg
constructor added to domain services, repositories and application services
across `admin-settlement`, `admin-valgnatt`, `admin-voting` and others, typically:

    +	public SomeDomainService() {
    +		// CDI
    +	}

Inspect a specific case with:

    git diff master origin/attempt -- admin/admin-voting/src/main/java/no/valg/eva/admin/voting/domain/service/VotingConfirmationDomainService.java

## How to approach

Do not bulk-apply. Work from actual deployment errors: each one names the class
that failed proxying. Fix that class, redeploy, take the next error. This keeps
the diff to classes that genuinely need it, and some will not — the count on
`attempt` includes changes made while chasing other problems.

Every touched file needs its own `MODIFIED` row. That is deliberate friction: the
row count is the honest measure of how much the release had to be altered.

## Done when

The backend deploys without proxying failures, and the number of classes that
actually required a constructor is recorded — with a note on how it compares to
`attempt`'s 342.
