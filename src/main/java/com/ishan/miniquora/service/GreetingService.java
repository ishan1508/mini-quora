package com.ishan.miniquora.service;

import com.ishan.miniquora.dto.GreetingResponse;
import org.springframework.stereotype.Service;

@Service
public class GreetingService {

    public GreetingResponse greet(String name) {
        return new GreetingResponse("Hello, " + name.trim() + "!");
    }
}
