# D011 — Disabling the system passphrase gate

Date: 2026-08-11

## Decision

A startup bean, `no.valg.eva.admin.reconstruction.SystemPassphraseAutoUnlock`,
sets EVA Admin's system passphrase automatically when the backend deploys. It is
an added file; no release class was modified. Setting `EVA_AUTO_UNLOCK=false`
restores the released behaviour.

## Why

`LifecycleFilter` serves nothing until the passphrase is entered, and the
published source contains no way to enter it (NF-021): the value is held in
memory so it cannot be seeded with SQL, the only remote interface is a getter,
and no page references the setter. The component that did this was withheld along
with `admin-other`. Without a substitute, the reconstruction stops at a
plain-text error page and nothing else in EVA Admin can be examined.

## What is actually being disabled

Not a login. In production this passphrase is the key to EVA's signing material:
`isPasswordCorrect` decrypts an election-signing PKCS#12 with it, so an operator
typing it is the act that authorises a deployment to sign election data. This
bean replaces that human decision with a constant, which means no operator has
approved that this deployment may run, and any signing key present in the
database would be unlocked automatically at boot.

On an empty database that is inert — `isPasswordCorrect` short-circuits to `true`
when no signing keys exist, so nothing is being guessed or broken. It stops being
inert the moment real key material is loaded.

Correspondingly: this belongs in a local reconstruction for inspection and
nowhere else. It must never be part of a deployment holding genuine keys, and the
bean logs a warning block at every startup so its presence cannot go unnoticed.

## Alternatives rejected

- **Modify `LifecycleFilter` to skip the check.** Changes released security code and hides the gate rather than satisfying it. The bean satisfies it through the application's own API.
- **Add a setter to the released `SystemPasswordService` remote interface.** Changes the release's remote API surface to work around a missing tool.
- **Reconstruct the withheld operator tool.** No specification for it exists; anything built would be invention presented as reconstruction.
