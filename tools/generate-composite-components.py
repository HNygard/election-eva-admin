#!/usr/bin/env python3
"""
Generates stub JSF composite components for the resources/ tree the release does
not ship (NF-024).

Without them every page that uses one dies at render time:

    <widget:message> Tag Library supports namespace:
    http://java.sun.com/jsf/composite/widget, but no tag was defined for
    name: message

What IS recoverable: which components exist, which library each belongs to, and
what attributes each is passed. All of that is written at every call site, so
scanning the pages recovers the component's interface exactly.

What is NOT recoverable: what each component renders. The markup lived in the
withheld resources/ directory. Each stub therefore declares the real interface
and renders a labelled placeholder plus its children, so pages compose and the
structure is visible while the appearance is obviously provisional.

This is the "what we technically can" step. Matching the screenshots is later
work, and these stubs are where that work goes.
"""

import os
import re
import sys
from collections import defaultdict

WEBAPP = "admin/admin-frontend/src/main/webapp"
RESOURCES = os.path.join(WEBAPP, "resources")

NS_RE = re.compile(r'xmlns:([a-zA-Z0-9_]+)="http://java\.sun\.com/jsf/composite/([a-zA-Z0-9_/]+)"')
ATTR_RE = re.compile(r'([a-zA-Z_][a-zA-Z0-9_:.-]*)\s*=\s*"')


def scan():
    """component -> (library, {attribute names}), from every call site."""
    components = defaultdict(lambda: set())
    library_of = {}

    for root, _dirs, files in os.walk(WEBAPP):
        if os.path.abspath(root).startswith(os.path.abspath(RESOURCES)):
            continue
        for name in files:
            if not name.endswith(".xhtml"):
                continue
            path = os.path.join(root, name)
            with open(path, encoding="utf-8", errors="replace") as handle:
                text = handle.read()

            prefixes = {p: lib for p, lib in NS_RE.findall(text)}
            if not prefixes:
                continue

            for prefix, library in prefixes.items():
                for match in re.finditer(
                    r"<%s:([a-zA-Z0-9_.]+)((?:[^>\"']|\"[^\"]*\"|'[^']*')*)" % re.escape(prefix),
                    text,
                ):
                    tag, attrs = match.group(1), match.group(2)
                    key = (library, tag)
                    library_of[key] = library
                    for attr in ATTR_RE.findall(attrs):
                        if ":" not in attr:
                            components[key].add(attr)
    return components


TEMPLATE = """<?xml version="1.0" encoding="UTF-8"?>
<!--
  GENERATED STUB by tools/generate-composite-components.py. Not EVA Admin markup.

  The release ships no resources/ directory, so this composite component and the
  rest of its library were missing and every page using them failed to render.

  The INTERFACE below is accurate: these are the attributes {name} is
  actually passed, recovered from its call sites in the released pages.

  The IMPLEMENTATION is a placeholder. What this component really rendered was in
  the withheld resources/ directory and is not recoverable from the source. It
  renders its own name and any children, so page structure is visible and it is
  obvious on screen that the appearance is not EVA Admin's.

  See docs/not-fixed-yet/NF-024.
-->
<ui:composition xmlns="http://www.w3.org/1999/xhtml"
                xmlns:ui="http://java.sun.com/jsf/facelets"
                xmlns:cc="http://java.sun.com/jsf/composite"
                xmlns:h="http://java.sun.com/jsf/html">

    <cc:interface>
{attributes}    </cc:interface>

    <cc:implementation>
        <div class="eva-stub eva-stub-{library}-{name}"
             data-eva-stub="{library}:{name}">
            <cc:insertChildren/>
        </div>
    </cc:implementation>
</ui:composition>
"""


def main():
    components = scan()
    if not components:
        print("No composite components referenced; nothing to do.", file=sys.stderr)
        return 1

    written = 0
    per_library = defaultdict(int)
    for (library, name), attributes in sorted(components.items()):
        directory = os.path.join(RESOURCES, library)
        os.makedirs(directory, exist_ok=True)

        declared = "".join(
            '        <cc:attribute name="%s"/>\n' % attribute
            for attribute in sorted(attributes)
        )
        # A component taking no attributes still needs a valid, empty interface.
        with open(os.path.join(directory, name + ".xhtml"), "w", encoding="utf-8") as handle:
            handle.write(
                TEMPLATE.format(
                    name=name,
                    library=library,
                    attributes=declared,
                )
            )
        written += 1
        per_library[library] += 1

    for library in sorted(per_library):
        print("  %-16s %d components" % (library, per_library[library]))
    print("Wrote %d composite component stubs under %s" % (written, RESOURCES))
    return 0


if __name__ == "__main__":
    sys.exit(main())
