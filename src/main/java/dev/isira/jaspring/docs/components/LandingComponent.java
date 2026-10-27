package dev.isira.jaspring.docs.components;

import dev.isira.jaspring.component.Component;
import dev.isira.jaspring.signals.Signal;
import dev.isira.jaspring.signals.Signals;
import dev.isira.jaspring.signals.WritableSignal;

@Component(selector = "site-landing", templateUrl = "landing.component.html", preserveWhitespaces = true)
public class LandingComponent {
    public final WritableSignal<Integer> count = Signals.signal(0);
    public final Signal<Integer> doubled = Signals.computed(() -> count.get() * 2);
    public final WritableSignal<String> tab = Signals.signal("java");
    public final String javaExample = """
            @Component(selector = "app-counter",
                       templateUrl = "counter.component.html")
            public class CounterComponent {
                public final WritableSignal<Integer> count =
                    Signals.signal(0);

                public final Signal<Integer> doubled =
                    Signals.computed(() -> count.get() * 2);
            }
            """;
    public final String htmlExample = """
            <button (click)="count.set(count() + 1)">
              Increment
            </button>

            <span>{{ count() }}</span>
            <p>Doubled: {{ doubled() }}</p>
            """;
    public final String dependency = "implementation 'dev.isira:jaspring-spring-boot-starter:0.2.0'";
}
