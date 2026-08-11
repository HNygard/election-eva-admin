# NF-021 — Nothing in the published source can enter the system passphrase

Status: FIXED — see D011
Milestone: M3

## Symptom

Every URL returns HTTP 200 with a plain-text body and nothing else:

    System passphrase has not been entered, unable to continue.

`LifecycleFilter` (`admin-frontend`, line 53) refuses to pass any request down the
chain until `ApplicationStatus` is `ENABLED`, and the status stays
`SYSTEM_PASSPHRASE_NOT_SET` until a passphrase is entered.

## Why this cannot be resolved from the release as published

The passphrase is held in memory, not in the database:

    SystemPasswordApplicationService.isPasswordSet()
        return systemPasswordStore.getPassword() != null;

    SystemPasswordApplicationService.setSystemPassword(String)
        if (!isPasswordSet()) { systemPasswordStore.setPassword(password); }

so it cannot be seeded with SQL. It has to be *called*.

The only remotely reachable surface is `SystemPasswordApplicationServiceApi`:

    @Stateless(name = "SystemPasswordService")
    @Remote(SystemPasswordService.class)

and its remote interface, in full, is:

    public interface SystemPasswordService {
        boolean isPasswordSet();
    }

A getter. There is no remote setter. `setSystemPassword` exists only on the CDI
bean inside the deployment, and no XHTML page in `admin-frontend` references it —
`grep -rl "systemPassword" admin/admin-frontend/src/main/webapp` finds nothing.

**Whatever entered the passphrase in the real system was not published.** It was
presumably part of `admin-other`, along with the Flyway migrations and the report
templates.

## The one piece of luck

`isPasswordCorrect` short-circuits on an empty database:

    List<SigningKey> adminSigningkeys = signingKeyRepository.getAllSigningKeyForElectionEventSigning();
    if (adminSigningkeys.isEmpty()) {
        LOGGER.debug("None p12 in the database, not possible to check if system password is correct");
        return true;
    }

With no signing keys loaded, *any* passphrase is accepted. So this does not
require reconstructing EVA's key material — only a way to make the call.

## How to approach

Add a small WAR of our own, deployed alongside the two release WARs, that injects
`SystemPasswordApplicationService` and calls `setSystemPassword`. It adds code but
modifies nothing: the release keeps its own semantics, and the new component is
declared `ADDED` like any other.

Constraints worth respecting:

- Put it in `tools/`, not in `admin/`. It is our scaffolding, not a reconstruction of something withheld, and the distinction should stay visible.
- Make it obviously non-production: it exists because the real operator tool was not published. Name and document it so nobody mistakes it for part of EVA Admin.
- Do not add a setter to the released `SystemPasswordService` interface. That would change the release's remote API surface to work around a missing tool.

## Done when

`LifecycleFilter` reports `ENABLED` and a request reaches a JSF page instead of
the plain-text gate.

Expect the next blocker immediately behind it: `MISSING_SCANNING_COUNT_CERT` and
`DIFFERENT_VERSION_ON_FRONTEND_AND_BACKEND` are the other two statuses that keep
the application disabled, and neither has been reached yet.
