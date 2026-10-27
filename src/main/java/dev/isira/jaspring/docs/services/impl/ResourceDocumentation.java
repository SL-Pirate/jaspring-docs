package dev.isira.jaspring.docs.services.impl;

import dev.isira.jaspring.docs.models.DocPage;
import dev.isira.jaspring.docs.services.Documentation;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;

@Service
public class ResourceDocumentation implements Documentation {
    private final List<DocPage> pages;

    public ResourceDocumentation() throws IOException {
        try (final var input = new ClassPathResource("docs/catalog.json").getInputStream()) {
            pages = List.copyOf(Arrays.asList(JsonMapper.builder().build().readValue(input, DocPage[].class)));
        }
    }

    @Override
    public List<DocPage> pages() { return pages; }

    @Override
    public Optional<DocPage> find(String slug) {
        return pages.stream().filter(page -> page.slug().equals(slug)).findFirst();
    }

    @Override
    public List<DocPage> search(String query) {
        final var terms = query.strip().toLowerCase(Locale.ROOT).split("\\s+");
        return pages.stream().filter(page -> {
            final var content = (page.title() + " " + page.description() + " " + page.sections().stream()
                    .map(section -> section.title() + " " + String.join(" ", section.paragraphs()) + " " + section.code())
                    .reduce("", (left, right) -> left + " " + right)).toLowerCase(Locale.ROOT);
            return Arrays.stream(terms).allMatch(content::contains);
        }).toList();
    }
}
