package dev.isira.jaspring.docs.components;

import dev.isira.jaspring.component.Component;
import dev.isira.jaspring.component.Input;
import dev.isira.jaspring.docs.models.DocPage;

@Component(selector = "app-root", templateUrl = "page.component.html")
public class PageComponent {
    @Input public DocPage page;
}
