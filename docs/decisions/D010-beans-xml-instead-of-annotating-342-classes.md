# D010 — One beans.xml per archive, instead of annotating 342 classes

Date: 2026-08-11

## Decision

CDI is enabled by adding a `beans.xml` with `bean-discovery-mode="all"` to each
bean archive — 10 module JARs and both WARs, 12 files. No release class is
annotated.

## Why

The release ships no `beans.xml` at all (NF-005), so every archive is an
*implicit* bean archive. Under CDI 1.1 an implicit archive only discovers classes
carrying a **bean-defining annotation** — a scope, or a stereotype. An `@Inject`
constructor is not one.

Most EVA Admin services are exactly that shape. `ValghierarkiDomainService` is
typical: no scope annotation, one `@Inject` constructor. Weld therefore never
defines it, and deployment fails with 16 of these:

    WELD-001408: Unsatisfied dependencies for type ValghierarkiDomainService
                 with qualifiers @Default

`bean-discovery-mode="all"` makes each archive explicit, so every class is a
candidate bean and constructor injection resolves as written.

`origin/attempt` solved the same problem the other way — `d2c1878`, "Changing DI
for to get stuff going". Its fix per class was to add `@Default`,
`@ApplicationScoped`, a no-arg constructor, and convert constructor injection to
field injection:

    +@Default
    +@ApplicationScoped
     public class ValghierarkiDomainService {
    -	private final MvElectionRepository mvElectionRepository;
    -
    	@Inject
    +	private MvElectionRepository mvElectionRepository;
    +
    +	public ValghierarkiDomainService() {
    +
    +	}

across roughly 342 files. That is not a neutral change: converting a `final`
field set by a constructor into a mutable field set by reflection removes the
guarantee that the object is fully initialised when constructed, and no-arg
constructors make it possible to build invalid instances. It rewrites the
codebase's construction discipline to work around a missing configuration file.

Twelve added files against 342 modified ones, and the source keeps its original
semantics.

## The tradeoff, stated honestly

`bean-discovery-mode="all"` is broader than the release probably used. The real
`beans.xml` files may have been `annotated` with the classes annotated
accordingly, or may have carried interceptor, decorator or alternative
declarations we know nothing about.

What we lose is fidelity in one direction: any `<interceptors>`,
`<decorators>` or `<alternatives>` stanza the originals contained is absent, and
its absence is silent. If a released interceptor turns out not to run, this is
the first place to look. Recorded in NF-005.

## Alternatives rejected

- **Annotate the classes, as `attempt` did.** 342 modifications and a change to construction semantics, to substitute for a config file.
- **`bean-discovery-mode="annotated"`.** Closer to the CDI default, but discovers nothing here, so it would require the annotations anyway.
