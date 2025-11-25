package com.example.nl_to_sql_spring.controller;

import com.example.nl_to_sql_spring.service.NlQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class NlQueryController {

    @Autowired
    private NlQueryService nlQueryService;

    @PostMapping("/execute-nl-query")
    public ResponseEntity<List<Map<String, Object>>> executeNlQuery(@RequestBody String naturalLanguageQuery) {
        List<Map<String, Object>> results = nlQueryService.executeQuery(naturalLanguageQuery);
        return ResponseEntity.ok(results);
    }
}