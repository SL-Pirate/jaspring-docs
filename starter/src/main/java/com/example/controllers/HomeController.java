package com.example.controllers;

import com.example.components.CounterComponent;
import dev.isira.jaspring.boot.JaspringView;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    @GetMapping("/")
    public JaspringView home() {
        return JaspringView.of(CounterComponent.class);
    }
}
