# NF-015 — The security filter chain order cannot be recovered

Status: OPEN
Milestone: M2

## Symptom

Ten servlet filters and listeners in `admin-frontend` carry no annotations, so
they exist only if declared in `web.xml` — which was stripped (NF-004). Five of
them are security controls:

    no/evote/service/security/PageAccessFilter
    no/evote/service/security/SelectRoleFilter
    no/valg/eva/admin/frontend/security/SessionHijackingDetector
    no/valg/eva/admin/frontend/security/TmpLoginFilter
    no/valg/eva/admin/frontend/security/OidcFilter

A filter chain runs in `<filter-mapping>` order, and that order is recorded
**nowhere else**. Not in the classes, not in the poms, not in the XHTML.

## Why this matters more than the other missing files

Every other reconstruction fails loudly: a missing `beans.xml` throws, a missing
schema refuses connections. A wrong filter order fails **silently**. The
application starts, pages render, and the only symptom is that a control runs at
the wrong point — authorisation before authentication, hijacking detection after
a session is already trusted, or a filter skipped for a path it should cover.

For a system whose published purpose is inspection and evaluation, that is the
worst failure mode: a reconstruction that looks like the real thing while behaving
differently in exactly the area an inspector would be examining.

## How to approach

Do not guess quietly. Whatever order is chosen:

1. Write it down as an assumption in the reconstructed `web.xml`, in a comment naming this item.
2. Derive as much as possible from the code rather than intuition — read what each filter does, what it puts in the session, and what the next one expects to find there. A filter that reads an attribute must run after whichever filter sets it, and that is recoverable.
3. Record the derivation in a finding, separating "forced by the code" from "chosen by us".
4. Never present a page as verified against reference material without stating that the filter order underneath it is reconstructed.

## Done when

The chain is declared, each position is justified in writing as either forced or
assumed, and the assumptions are listed somewhere an evaluator would find them —
not buried in a commit message.
