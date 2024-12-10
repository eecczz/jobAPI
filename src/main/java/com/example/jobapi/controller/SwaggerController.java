package com.example.jobapi.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;

@RestController
@RequestMapping
public class SwaggerController {

    @GetMapping("/swagger.yaml")
    public ResponseEntity<String> getSwaggerYaml() throws IOException {
        Resource resource = new ClassPathResource("swagger.yaml");
        String yamlContent = new String(Files.readAllBytes(resource.getFile().toPath()));
        return ResponseEntity.ok(yamlContent);
    }
}
