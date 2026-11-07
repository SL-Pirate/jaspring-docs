# Match Jaspring's GraalJS 25.2.4 with Oracle GraalVM innovation release 2.
ARG JAVA_IMAGE=container-registry.oracle.com/graalvm/jdk:25i2-25.0.4

FROM ${JAVA_IMAGE} AS build
WORKDIR /workspace

COPY --chmod=755 gradlew ./
COPY gradle/ gradle/
COPY build.gradle settings.gradle jnpm.lock ./
RUN ./gradlew --no-daemon --console=plain dependencies

COPY src/ src/
COPY starter/ starter/
RUN ./gradlew --no-daemon --console=plain test bootJar

FROM ${JAVA_IMAGE} AS runtime
WORKDIR /app

RUN mkdir -p /app/.cache && chown 10001:10001 /app/.cache
COPY --from=build --chown=10001:10001 /workspace/build/libs/jaspring-docs.jar ./jaspring-docs.jar

USER 10001:10001
ENV SERVER_PORT=8095
# User 10001 has no home directory; runtime caches go to the writable directory created above.
ENV XDG_CACHE_HOME=/app/.cache
EXPOSE 8095
STOPSIGNAL SIGTERM

ENTRYPOINT ["java", "--enable-native-access=ALL-UNNAMED", "--sun-misc-unsafe-memory-access=allow", "-XX:MaxRAMPercentage=75.0", "-XX:+ExitOnOutOfMemoryError", "-jar", "/app/jaspring-docs.jar"]
