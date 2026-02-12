package com.github.kyanbrix.restapi.controllers;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {


    @GetMapping("/verification")
    public String verificationPage() {

        return "login.html";

    }
}
