# Jaspring · Documentation & website

The landing page and documentation for Jaspring, the reactive meta-framework for Java.
Built with Jaspring itself: Java components, Angular templates, Spring Boot, and server-side signals.

## Run locally

Requires Java 25. Oracle GraalVM 25.2.4 matches the bundled GraalJS runtime and provides optimized compilation.

```sh
./gradlew bootRun
```

Open **http://localhost:8095**. This project deliberately uses 8095 to leave 8080 available for other applications.

Override the port when needed:

```sh
./gradlew bootRun --args='--server.port=9000'
```

No Node.js installation or frontend build is needed to run or package the application.

## Validate and package

```sh
./gradlew test bootJar
java --enable-native-access=ALL-UNNAMED -jar build/libs/docs-0.0.1-SNAPSHOT.jar
```

The integration suite starts an isolated server on a random port. It renders every documentation route,
checks page metadata and missing routes, exercises the live counter through the HTTP event transport,
verifies session isolation, and tests documentation search.

## Content and components

- `src/main/resources/docs/catalog.json` is the ordered source for all 14 documentation pages, their sections, code examples, navigation, and search.
- `src/main/java/dev/isira/jaspring/docs/components` contains the page, header/search, landing page, and documentation components.
- `src/main/resources/dev/isira/jaspring/docs/components` contains their Angular HTML templates.
- `src/main/resources/jaspring/index.html` is the document layout. The controller inserts escaped page titles and descriptions before sending the rendered document.
- `src/main/resources/static/site.css` contains the responsive visual system and orbital illustration.
- `src/main/resources/static/site.js` handles syntax coloring, clipboard access, keyboard shortcuts, modal focus, and table-of-contents highlighting. Application state stays in Java.

To add a documentation page, add an entry to the catalog using an existing group: `Start here`, `Core concepts`, or `Go further`.
Use a unique slug and unique section IDs within each page. Navigation, search, previous/next links, and the table of contents follow automatically.

## Framework versions

The site runs on the published **Jaspring 0.2.0** starter. Documentation was researched against
the local Jaspring repository and distinguishes that release from newer APIs on `master`.
Client hooks, browser calls, and Java-to-WebAssembly browser closures require a matching source build;
the relevant chapters identify that requirement.

Framework repository: [SL-Pirate/jaspring](https://github.com/SL-Pirate/jaspring).
Source and license links use its `master` branch.

## Fonts

Red Hat Display and Source Code Pro are self-hosted. Their license files are included beside the fonts
under `src/main/resources/static/fonts`. The design requires no external font or image service.
