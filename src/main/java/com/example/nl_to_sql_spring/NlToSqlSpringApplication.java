package com.example.nl_to_sql_spring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class NlToSqlSpringApplication {

    public static void main(String[] args) {
        SpringApplication.run(NlToSqlSpringApplication.class, args);


    }
    @Bean
    public RestTemplate restTemplate()
    {
        return new RestTemplate();
    }
}