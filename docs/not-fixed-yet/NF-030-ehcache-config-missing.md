# NF-030 — The EhCache configuration is missing

Status: FIXED
Milestone: M3

## Symptom

    /templates/layout.xhtml @26,99
    value="#{userDataController.currentElectionEventDisabled}":
    java.lang.NullPointerException
        at no.evote.presentation.cache.EntityCache.get(EntityCache.java:49)

Eleven screens, all reported as a null somewhere in the shared page template.

## Cause

`no.evote.presentation.cache.GenericCacheManager` loads its configuration by
name:

    CacheManager.create(getClass().getClassLoader()
        .getResourceAsStream("ehcache-local.xml"));

The release ships no `ehcache-local.xml`. `CacheManager.create(null)` builds a
manager with no caches, every `getCache(...)` returns null, and `EntityCache.get`
dereferences it at line 49.

This is a good example of how the withheld files fail: nothing said "cache
configuration missing". It surfaced as a null in an EL expression in
`layout.xhtml`, which is the template every screen shares, so a single absent
file looked like eleven unrelated broken pages.

## Resolution

`admin/admin-frontend/src/main/resources/ehcache-local.xml`, defining the three
caches the code asks for by name:

    entityCache             EntityCache.get / .remove
    serviceInvocationCache  ServiceInvocationHandler
    election-event-pkcs12   election event signing keys

Result: **30 of 71 screens rendering to 41 of 71.**

## What is reconstructed and what is invented

The cache **names** come from the code that requests them, so those are certain.

The **sizes and timeouts are ours**. What the release configured is unknown.
They are chosen to be safe rather than fast — modest heap counts, no disk
overflow, eviction by time so a stale entity cannot live forever. The
`election-event-pkcs12` cache is deliberately small and never overflows to disk,
since it holds key material and `overflowToDisk` would write it to the
filesystem.

Wrong values here surface as memory pressure or as stale data on screen, not as
an error, so they are worth revisiting if either appears.
