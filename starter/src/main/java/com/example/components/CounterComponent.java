package com.example.components;

import dev.isira.jaspring.component.Component;
import dev.isira.jaspring.signals.Signals;
import dev.isira.jaspring.signals.WritableSignal;

@Component(selector = "app-counter", templateUrl = "counter.component.html")
public class CounterComponent {
    public final WritableSignal<Integer> count = Signals.signal(0);
}
