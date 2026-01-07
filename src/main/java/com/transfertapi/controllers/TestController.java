package com.transfertapi.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
    @GetMapping("/check")
    public String check() {
        return "TestController détecté ✅";
    }
}
