package dev.isira.jaspring.docs.components;

import dev.isira.jaspring.component.Component;
import dev.isira.jaspring.component.Input;
import dev.isira.jaspring.docs.models.DocPage;
import dev.isira.jaspring.docs.services.Documentation;
import dev.isira.jaspring.signals.Signal;
import dev.isira.jaspring.signals.Signals;
import dev.isira.jaspring.signals.WritableSignal;
import java.util.List;

@Component(selector = "site-header", templateUrl = "header.component.html")
public class HeaderComponent {
    @Input public boolean docs = false;
    public final WritableSignal<Boolean> menuOpen = Signals.signal(false);
    public final WritableSignal<Boolean> searchOpen = Signals.signal(false);
    public final WritableSignal<String> query = Signals.signal("");
    public final Signal<List<DocPage>> results;

    public HeaderComponent(Documentation documentation) {
        results = Signals.computed(() -> documentation.search(query.get()));
    }

    public void openSearch() {
        query.set("");
        searchOpen.set(true);
        menuOpen.set(false);
    }
}
