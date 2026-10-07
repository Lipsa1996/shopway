package com.shopway.catalog.controller;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/catalog")
public class TestController {

    @GetMapping("/hello")
    public String hello() {
        return "Hello from Catalog Service";
    }


}
