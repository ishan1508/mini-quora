package com.ishan.machinecodingapi.service;

import com.ishan.machinecodingapi.dto.GreetingResponse;
import org.springframework.stereotype.Service;

@Service
public class GreetingService {

    public GreetingResponse greet(String name) {
        return new GreetingResponse("Hello, " + name.trim() + "!");
    }
}
