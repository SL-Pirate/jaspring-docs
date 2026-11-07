# Agent notes

## Keep the starter project in sync

`starter/` is the downloadable starter project. The build zips it into `/downloads/jaspring-starter.zip`,
which is linked from the Installation page and the landing page. Users get exactly what is in `starter/`.

Every docs update that changes setup must update the starter in the same change. This includes:

- A new Jaspring version. Bump the plugin and every `dev.isira:jaspring-*` dependency in `starter/build.gradle`.
- A new Spring Boot or dependency-management plugin version.
- Changes to the Installation page: `settings.gradle`, `build.gradle`, JVM args, required dependencies,
  the counter component, its template, or the controller.
- A new Gradle wrapper version. Copy `gradle/wrapper/` and `gradlew`/`gradlew.bat` into `starter/`.
- Changes to the TypeScript editor setup (`starter/tsconfig.json`).

Also update `starter/README.md` when the versions or the file layout change.

Verify both projects before finishing:

```sh
(cd starter && ./gradlew test && rm -rf build .gradle)
./gradlew test bootJar
```

The docs test `starterArchiveIsServedAndMatchesTheInstallationGuide` fails when the starter's Jaspring version,
Spring Boot version or JVM args differ from the Installation page. It does not check the other files, so compare
them by hand.
