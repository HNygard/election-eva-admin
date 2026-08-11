# The login chain works, and the filter order corrected itself

    1. GET  /secure/index.xhtml   -> 302 /tmpLogin?scanning=false
    2. POST /tmpLogin             -> 302 /secure/selectRole.xhtml
    3. TmpLoginFilter: "/172.22.0.1 01017012345 is not a valid operator in the system!"

Three hops of EVA Admin's own authentication, ending in a correct rejection: the
credential was accepted, the operator lookup ran, and the database has no
operators. That is NF-010, not a fault in the chain.

The `/tmpLogin` screen renders the release's own form:

    Bruker ID: [        ]   Security level: [3]   [Login]

## Three orderings moved from ASSUMED to FORCED

NF-015 warned that a wrong filter order fails silently. In practice it failed
loudly three times, and each failure pinned a mapping the code had determined all
along.

**OidcFilter belongs on `/secure/*`, not `/*`.** It redirects failures to
`/welcome.xhtml?type=error` (`OidcFilter:114`). Mapped at `/*` it filters its own
error page, so every request became an endless redirect, one "OIDC_CLAIM_pid is
blank" per hop. The error page must lie outside the filter.

**TmpLoginFilter belongs on `/secure/*`, not `/tmpLogin`** — the opposite of what
its name suggests. It *guards* the secure area and redirects *to* `/tmpLogin`
when the session holds no `TmpLoginForm` (`TmpLoginFilter:84`). Mapped at
`/tmpLogin` it intercepted its own target: `GET /tmpLogin` → 302 `/tmpLogin`,
forever. It then collects the submitted form when `TmpLoginServlet` redirects to
`/secure/selectRole.xhtml`, which is inside `/secure/*`.

**TmpLoginFilter must run before OidcFilter.** `OidcFilter` has no concept of tmp
login: it passes through only if `UserData` already exists or a role is being
switched, and otherwise fails. `TmpLoginFilter` skips itself unless
`no.valg.eva.admin.login.tmp.enabled` is true. The enabled-aware filter therefore
has to come first, or the tmp login path is unreachable — which is exactly what
happened: `/secure/*` redirected to `?type=error` before `TmpLoginFilter` ever
ran.

With that order, both paths work from one configuration. Tmp login on:
`TmpLoginFilter` establishes `UserData`, `OidcFilter` sees it and passes through.
Tmp login off: `TmpLoginFilter` stands aside and `OidcFilter` authenticates from
the reverse-proxy header, which is how production ran it.

## What this does and does not tell us

It is real evidence, and better than the data-flow reasoning it replaced: these
three mappings are now the only ones consistent with the code, not merely
plausible.

It does not vindicate the whole file. The relative order of
`SessionHijackingDetector`, `SelectRoleFilter` and `PageAccessFilter` is still
assumed — all three are readers of `UserData`, all three sit behind the
authenticators, and nothing observed so far distinguishes their sequence. A wrong
order among them would still fail silently. NF-015 stays open.
