package com.example.himbarav1.services;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.himbarav1.dto.CheckClient;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class ClientService {

    private final RestTemplate restTemplate = new RestTemplate();

    public ResponseEntity<String> postToExternalApi(CheckClient dto) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<CheckClient> request = new HttpEntity<>(dto, headers);

        String url = "http://localhost:8888/users";

        return restTemplate.postForEntity(url, request, String.class);
    }

}
