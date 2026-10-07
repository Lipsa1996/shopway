package com.shopway.cart.controller;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cart")
public class TestController {

    @GetMapping("/hello")
    public String hello() {
        return "Hello from Cart Service";
    }


}