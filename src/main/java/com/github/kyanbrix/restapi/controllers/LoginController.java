package com.github.kyanbrix.restapi.controllers;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {


    @GetMapping("/")
    public String verificationPage() {return "welcome.html";}

    @GetMapping("/verify")
    public String welcomePage() {
        return "login.html";
    }

}
