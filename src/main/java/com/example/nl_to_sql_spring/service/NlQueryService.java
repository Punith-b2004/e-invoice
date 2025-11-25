package com.example.nl_to_sql_spring.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class NlQueryService {
    @Autowired
    private JdbcTemplate jdbcTemplate;  // For executing SQL on DB

    @Value("${flask.api.url}")
    private String flaskApiUrl;  // Injected from application.properties

    @Autowired
    private final RestTemplate restTemplate; // For calling Flask API we can also do it @Autowired and creating construcor and

    public NlQueryService(RestTemplate restTemplate,JdbcTemplate jdbcTemplate) {
        this.restTemplate = restTemplate;
        this.jdbcTemplate=jdbcTemplate;
    }

    public List<Map<String, Object>> executeQuery(String naturalLanguageQuery) {

        String sql = callFlaskApi(naturalLanguageQuery).trim();

        if (sql.startsWith("ERROR")) {
            throw new RuntimeException("Failed to generate SQL: " + sql);
        }

        if (!sql.toUpperCase().startsWith("SELECT")) {
            throw new IllegalArgumentException("Only SELECT queries are allowed!");
        }

        return jdbcTemplate.queryForList(sql.replaceAll(";$","").trim());
    }

    private String callFlaskApi(String query) {

        Map<String, String> payload = Map.of("query", query);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, String>> request = new HttpEntity<>(payload, headers);

        // Call Flask and extract SQL from response
        Map response = restTemplate.postForObject(flaskApiUrl, request, Map.class);
        return (String) response.get("sql");
    }
}