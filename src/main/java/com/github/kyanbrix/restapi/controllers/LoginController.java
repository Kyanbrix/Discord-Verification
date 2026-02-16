package com.github.kyanbrix.restapi.controllers;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    @GetMapping("/")
    public String index() {
        return "forward:/welcome.html";
    }

    @GetMapping("/robots.txt")
    public String robots() {
        return "forward:/robots.txt";
    }

    @GetMapping("/verify")
    public String welcomePage() {
        return "login.html";
    }

}
