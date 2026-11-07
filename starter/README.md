# Jaspring starter

A ready-to-run Jaspring application: Spring Boot 4.1, Jaspring 0.5.0 and one live counter page.

## Run

Requires Java 25. Oracle GraalVM 25.2.4 matches the bundled GraalJS runtime and starts faster.

```sh
./gradlew bootRun
```

Open http://localhost:8080 and click the button.

## Test and package

```sh
./gradlew test bootJar
java --enable-native-access=ALL-UNNAMED --sun-misc-unsafe-memory-access=allow -jar build/libs/jaspring-starter-0.0.1-SNAPSHOT.jar
```

## What is inside

- `settings.gradle` resolves the Jaspring Gradle plugin from Maven Central.
- `build.gradle` applies the plugin and adds the Spring Boot starter.
- `src/main/java/com/example/components/CounterComponent.java` and its template in `src/main/resources/com/example/components/`.
- `src/main/java/com/example/controllers/HomeController.java` returns the page.
- `src/main/resources/jaspring/index.html` is the document layout. Edit it to change the title or head.
- `tsconfig.json` is ready for browser scripts in `src/main/typescript`.

Rename the `com.example` package to your own. Keep components under the `@SpringBootApplication` package
so Jaspring can find them.
