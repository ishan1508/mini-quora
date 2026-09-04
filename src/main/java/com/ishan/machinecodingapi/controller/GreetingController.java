package com.ishan.machinecodingapi.controller;

import com.ishan.machinecodingapi.dto.GreetingResponse;
import com.ishan.machinecodingapi.service.GreetingService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/greetings")
public class GreetingController {

    private final GreetingService greetingService;

    public GreetingController(GreetingService greetingService) {
        this.greetingService = greetingService;
    }

    @GetMapping("/{name}")
    public ResponseEntity<GreetingResponse> getGreeting(@PathVariable @NotBlank @Size(max = 50) String name) {
        return ResponseEntity.ok(greetingService.greet(name));
    }
}
