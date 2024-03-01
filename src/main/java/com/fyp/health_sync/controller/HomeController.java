package com.fyp.health_sync.controller;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class HomeController {


    @Operation(summary = "Home", description = "Welcome to Health Sync", tags = {"Home"})
    @GetMapping
    public String home(){
        return "{\n\t\"Status\" : \"api is working\" \n\t \"Message\" : \"Welcome to Health Sync\" \n\t \"Author\" : \"Labhesh Shah\" \n\t \"Swagger\" : \"http://localhost:8086/swagger-ui.html\" \n  }";
    }
}
