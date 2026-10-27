package dev.isira.jaspring.docs.controllers;

import dev.isira.jaspring.boot.JaspringView;
import dev.isira.jaspring.boot.JaspringRuntime;
import dev.isira.jaspring.docs.components.PageComponent;
import dev.isira.jaspring.docs.services.Documentation;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.View;
import org.springframework.web.util.HtmlUtils;

@Controller
public class SiteController {
    private final Documentation documentation;
    private final JaspringRuntime runtime;

    public SiteController(Documentation documentation, JaspringRuntime runtime) {
        this.documentation = documentation;
        this.runtime = runtime;
    }

    @GetMapping("/")
    public View home() {
        return render(JaspringView.of(PageComponent.class),
                "Jaspring — The web framework Java always deserved.",
                "The reactive meta-framework for Java. Build live interfaces with Java components, Spring Boot, and Angular templates.");
    }

    @GetMapping({"/docs", "/docs/"})
    public String docs() { return "redirect:/docs/introduction"; }

    @GetMapping("/docs/{slug}")
    public View document(@PathVariable String slug) {
        final var page = documentation.find(slug)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Documentation page not found"));
        return render(JaspringView.of(PageComponent.class).input("page", page),
                page.title() + " — Jaspring", page.description());
    }

    /// The framework layout is shared; insert route metadata before writing the rendered document.
    private View render(JaspringView view, String title, String description) {
        return (model, request, response) -> {
            final var document = runtime.renderDocument(view.spec(), request.getSession(true).getId(), null)
                    .replace("__SITE_TITLE__", HtmlUtils.htmlEscape(title))
                    .replace("__SITE_DESCRIPTION__", HtmlUtils.htmlEscape(description));
            response.setContentType("text/html;charset=UTF-8");
            response.getWriter().write(document);
        };
    }
}
