package dev.isira.jaspring.docs;

import dev.isira.jaspring.docs.services.Documentation;
import java.io.ByteArrayInputStream;
import java.net.CookieManager;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.zip.ZipInputStream;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.web.util.HtmlUtils;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class JaspringDocsApplicationTests {
    @Autowired private Environment environment;
    @Autowired private Documentation documentation;

    @Test
    void everyDocumentRendersItsTitleAndSections() throws Exception {
        try (final var client = HttpClient.newHttpClient()) {
            for (final var page : documentation.pages()) {
                final var response = get(client, "/docs/" + page.slug());
                assertEquals(200, response.statusCode(), page.slug());
                assertTrue(response.body().contains(HtmlUtils.htmlEscape(page.title())), page.slug());
                assertTrue(response.body().contains("<title>" + HtmlUtils.htmlEscape(page.title() + " — Jaspring") + "</title>"));
                for (final var section : page.sections()) {
                    assertTrue(response.body().contains("id=\"" + section.id() + "\""), section.id());
                    for (final var link : section.links()) {
                        assertTrue(response.body().contains("href=\"" + HtmlUtils.htmlEscape(link.href()) + "\""), link.href());
                        assertTrue(response.body().contains(HtmlUtils.htmlEscape(link.label())), link.label());
                        final var target = URI.create(link.href());
                        if (!target.getPath().startsWith("/docs/")) {
                            assertEquals(200, get(client, target.getPath()).statusCode(), link.href());
                            continue;
                        }
                        final var linkedPage = documentation.find(target.getPath().substring("/docs/".length())).orElseThrow();
                        if (target.getFragment() != null) {
                            assertTrue(linkedPage.sections().stream().anyMatch(item -> item.id().equals(target.getFragment())), link.href());
                        }
                    }
                }
            }
        }
    }

    @Test
    void sidebarListsEveryDocument() throws Exception {
        try (final var client = HttpClient.newHttpClient()) {
            final var sidebar = extract("(?s)(<nav aria-label=\"Documentation topics\">.*?</nav>)",
                    get(client, "/docs/introduction").body());
            for (final var page : documentation.pages()) {
                assertTrue(sidebar.contains("href=\"/docs/" + page.slug() + "\""), page.slug());
            }
        }
    }

    @Test
    void liveCounterReturnsPatchesAndRemainsOwnedByItsSession() throws Exception {
        try (final var client = HttpClient.newBuilder().cookieHandler(new CookieManager()).build();
             final var stranger = HttpClient.newHttpClient()) {
            final var document = get(client, "/").body();
            final var page = extract("data-jsp-page=\"([^\"]+)\"", document);
            final var button = extract("(<button[^>]*id=\"counter-increment\"[^>]*>)", document);
            final var handler = extract("data-jsp-on=\"click:([^:\"]+)", button);
            final var message = JsonMapper.builder().build().writeValueAsString(Map.of(
                    "t", "event", "page", page, "h", handler, "ev", Map.of("type", "click")));
            final var event = HttpRequest.newBuilder(uri("/_jaspring/event"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(message)).build();

            final var update = client.send(event, HttpResponse.BodyHandlers.ofString());
            assertEquals(200, update.statusCode());
            final var json = JsonMapper.builder().build().readTree(update.body());
            assertEquals("patch", json.at("/messages/0/t").asString());
            final var patch = json.at("/messages/0/patches/0/html").asString();
            assertTrue(Pattern.compile("class=\"counter-value\"[^>]*>\\s*1\\s*</div>").matcher(patch).find(), patch);
            assertTrue(patch.contains(">2</span>"), patch);
            assertEquals(410, stranger.send(event, HttpResponse.BodyHandlers.ofString()).statusCode());
            assertTrue(Pattern.compile("class=\"counter-value\"[^>]*>\\s*0\\s*</div>")
                    .matcher(get(client, "/").body()).find());
        }
    }

    @Test
    void starterArchiveIsServedAndMatchesTheInstallationGuide() throws Exception {
        try (final var client = HttpClient.newHttpClient()) {
            final var response = client.send(HttpRequest.newBuilder(uri("/downloads/jaspring-starter.zip")).GET().build(),
                    HttpResponse.BodyHandlers.ofByteArray());
            assertEquals(200, response.statusCode());

            final var entries = new HashMap<String, String>();
            try (final var zip = new ZipInputStream(new ByteArrayInputStream(response.body()))) {
                for (var entry = zip.getNextEntry(); entry != null; entry = zip.getNextEntry()) {
                    entries.put(entry.getName(), new String(zip.readAllBytes(), StandardCharsets.UTF_8));
                }
            }
            for (final var file : List.of("gradlew", "gradlew.bat", "gradle/wrapper/gradle-wrapper.jar",
                    "gradle/wrapper/gradle-wrapper.properties", "settings.gradle", "build.gradle",
                    "src/main/java/com/example/components/CounterComponent.java",
                    "src/main/resources/com/example/components/counter.component.html",
                    "src/main/java/com/example/controllers/HomeController.java")) {
                assertTrue(entries.containsKey("jaspring-starter/" + file), file);
            }
            assertTrue(entries.keySet().stream().noneMatch(name -> name.contains("/build/") || name.contains("/.gradle/")),
                    entries.keySet().toString());

            // The starter must not drift from the setup the Installation page teaches.
            final var guide = documentation.find("installation").orElseThrow().sections().stream()
                    .filter(section -> section.id().equals("gradle")).findFirst().orElseThrow().code();
            final var version = extract("id 'dev\\.isira\\.jaspring' version '([^']+)'", guide);
            final var starterBuild = entries.get("jaspring-starter/build.gradle");
            assertTrue(starterBuild.contains("id 'dev.isira.jaspring' version '" + version + "'"), starterBuild);
            assertTrue(starterBuild.contains("dev.isira:jaspring-spring-boot-starter:" + version), starterBuild);
            assertTrue(starterBuild.contains(extract("(id 'org\\.springframework\\.boot' version '[^']+')", guide)), starterBuild);
            assertTrue(starterBuild.contains(extract("(jvmArgs [^\\n]+)", guide)), starterBuild);
        }
    }

    @Test
    void documentationIndexRedirectsAndMissingDocumentsReturn404() throws Exception {
        try (final var client = HttpClient.newHttpClient()) {
            final var index = get(client, "/docs");
            assertEquals(302, index.statusCode());
            assertEquals("/docs/introduction", URI.create(index.headers().firstValue("Location").orElseThrow()).getPath());
            assertEquals(404, get(client, "/docs/does-not-exist").statusCode());
        }
    }

    @Test
    void compiledSiteScriptIsInjectedAndServed() throws Exception {
        try (final var client = HttpClient.newHttpClient()) {
            final var src = extract("<script type=\"module\" src=\"(/_jaspring/ts/site\\.js\\?v=[0-9a-f]{8})\"></script>",
                    get(client, "/docs/introduction").body());
            final var script = get(client, src);
            assertEquals(200, script.statusCode());
            assertTrue(script.body().contains("new MutationObserver(syncBrowserDetails)"), script.body());
        }
    }

    @Test
    void searchFindsApisAndRequiresAllSearchTerms() {
        assertTrue(documentation.search("SignalWriteOutsideTurnException").stream()
                .anyMatch(page -> page.slug().equals("signals")));
        assertTrue(documentation.search("  WEBSOCKET fallback ").stream()
                .anyMatch(page -> page.slug().equals("events")));
        assertTrue(documentation.search("data-jsp-ignore").stream()
                .anyMatch(page -> page.slug().equals("client-hooks")));
        assertTrue(documentation.search("compileTypescript").stream()
                .anyMatch(page -> page.slug().equals("typescript")));
        assertTrue(documentation.search("jnpm.lock").stream()
                .anyMatch(page -> page.slug().equals("jnpm")));
        assertTrue(documentation.search("@fortawesome/fontawesome-free").stream()
                .anyMatch(page -> page.slug().equals("font-awesome")));
        assertTrue(documentation.search("no-such-jaspring-api").isEmpty());
        assertEquals(documentation.pages(), documentation.search(" "));
    }

    private URI uri(String path) {
        return URI.create("http://localhost:" + environment.getProperty("local.server.port") + path);
    }

    private HttpResponse<String> get(HttpClient client, String path) throws Exception {
        return client.send(HttpRequest.newBuilder(uri(path)).GET().build(), HttpResponse.BodyHandlers.ofString());
    }

    private String extract(String expression, String input) {
        final var matcher = Pattern.compile(expression).matcher(input);
        assertTrue(matcher.find(), "Expected rendered markup matching: " + expression);
        return matcher.group(1);
    }
}
