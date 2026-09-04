package com.ishan.machine_coding_api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/greetings")
public class GreetingController {

    @GetMapping("/{name}")
    public ResponseEntity<GreetingResponse> getGreeting(
            @PathVariable String name) {

        GreetingResponse response =
                new GreetingResponse("Hello, " + name + "!");

        return ResponseEntity.ok(response);
    }

    public record GreetingResponse(String message) {
    }
}
