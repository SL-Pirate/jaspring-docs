package dev.isira.jaspring.docs.components;

import dev.isira.jaspring.component.Component;
import dev.isira.jaspring.component.Input;
import dev.isira.jaspring.docs.models.DocPage;
import dev.isira.jaspring.docs.services.Documentation;
import java.util.List;

@Component(selector = "site-docs", templateUrl = "docs.component.html", preserveWhitespaces = true)
public class DocsComponent {
    @Input public DocPage page;
    public final List<String> groups = List.of("Start here", "Core concepts", "Go further");
    private final Documentation documentation;

    public DocsComponent(Documentation documentation) { this.documentation = documentation; }

    public List<DocPage> pagesIn(String group) {
        return documentation.pages().stream().filter(item -> item.group().equals(group)).toList();
    }

    public DocPage previous() {
        final var index = documentation.pages().indexOf(page);
        return index > 0 ? documentation.pages().get(index - 1) : null;
    }

    public DocPage next() {
        final var index = documentation.pages().indexOf(page);
        return index < documentation.pages().size() - 1 ? documentation.pages().get(index + 1) : null;
    }
}
