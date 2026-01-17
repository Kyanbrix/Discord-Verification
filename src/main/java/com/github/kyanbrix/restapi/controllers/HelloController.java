package com.github.kyanbrix.restapi.controllers;


import com.github.kyanbrix.restapi.RandomQuote;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller

public class HelloController {

    @RequestMapping("/")
    public String hello() {
        RandomQuote qt = new RandomQuote();

        System.out.println(qt.randomQuote());

        return "portfolio.html";
    }


    @RequestMapping("/homepage")
    public String website() {
        return "index.html";
    }
}
