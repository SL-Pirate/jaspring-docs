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
java --enable-native-access=ALL-UNNAMED -jar build/libs/jaspring-docs.jar
```

The integration suite starts an isolated server on a random port. It renders every documentation route,
checks page metadata and missing routes, exercises the live counter through the HTTP event transport,
verifies session isolation, and tests documentation search.

## Containers

The Dockerfile builds and tests the application in one stage, then copies only the application JAR
into a non-root runtime stage. It uses the official Oracle GraalVM image matching Jaspring's
GraalJS 25.2.4, with no dependency on the host JDK or Gradle installation.
See [Oracle's container image documentation](https://docs.oracle.com/en/graalvm/jdk/25i/docs/getting-started/container-images/).

Build and run locally with Podman:

```sh
podman build -t localhost/jaspring-docs:latest .
podman run --rm --name jaspring-docs -p 8095:8095 localhost/jaspring-docs:latest
```

Build and deploy on the server with Docker:

```sh
docker build -t jaspring-docs:latest .
docker run -d --name jaspring-docs --restart unless-stopped -p 8095:8095 jaspring-docs:latest
```

Open http://localhost:8095. To use another host port, change only the left side of the mapping,
for example `-p 9000:8095`. Stop an existing local `bootRun` preview before binding its port.
For a registry-based deployment, tag and push the image to your registry, then pull and run that tag on the server.

Spring Boot settings can be supplied as environment variables. `SERVER_PORT` defaults to 8095;
`JAVA_TOOL_OPTIONS` can supply additional JVM options. `JAVA_IMAGE` is an optional build argument
for overriding the base image when upgrading the framework and GraalVM together.

If a reverse proxy terminates TLS, forward WebSocket upgrades for `/_jaspring/ws` as well as normal HTTP
requests. Live page state belongs to one application process; use session affinity if running multiple replicas.

## Content and components

- `src/main/resources/docs/catalog.json` is the ordered source for all 21 documentation pages, their sections, code examples, navigation, and search.
- `src/main/java/dev/isira/jaspring/docs/components` contains the page, header/search, landing page, and documentation components.
- `src/main/resources/dev/isira/jaspring/docs/components` contains their Angular HTML templates.
- `src/main/resources/jaspring/index.html` is the document layout. The controller inserts escaped page titles and descriptions before sending the rendered document.
- `src/main/resources/static/site.css` contains the responsive visual system and orbital illustration.
- `src/main/resources/static/site.js` handles syntax coloring, clipboard access, keyboard shortcuts, modal focus, and table-of-contents highlighting. Application state stays in Java.

To add a documentation page, add an entry to the catalog using an existing group: `Start here`, `Core concepts`, `Browser`, `Go further`, or `Cookbook`.
The groups are listed in `DocsComponent`; keep catalog order matching that list, because previous/next links follow catalog order.
Use a unique slug and unique section IDs within each page. Navigation, search, previous/next links, and the table of contents follow automatically.

## Framework versions

The site runs on the published **Jaspring 0.3.0** starter, and the documentation describes that release,
including client hooks, browser calls, and Java-to-WebAssembly browser closures. The site itself uses no
browser closures, so it does not apply the Jaspring Gradle plugin or need the WebAssembly toolchain.

Framework repository: [SL-Pirate/jaspring](https://github.com/SL-Pirate/jaspring).
Source and license links use its `master` branch.

## Fonts

Red Hat Display and Source Code Pro are self-hosted. Their license files are included beside the fonts
under `src/main/resources/static/fonts`. The design requires no external font or image service.
