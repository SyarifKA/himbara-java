package com.example.himbarav1.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/welcome")
public class WelcomeClient {

    @GetMapping
    public String welcome() {
        return "welcome to Spring API";
    }
}
