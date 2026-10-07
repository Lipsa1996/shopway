package com.shopway.notification.controller;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notification")
public class TestController {

    @GetMapping("/hello")
    public String hello() {
        return "Hello from Notification Service";
    }

}
