package com.example.himbarav1.services;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class ExternalPostService {

    public void sendPostRequest() {
        RestTemplate restTemplate = new RestTemplate();

        // URL tujuan
        String url = "http://localhost:8888/users";

        // Data (bisa pakai Map, DTO, atau Object)
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("name", "Syarif");
        requestBody.put("email", "syarif@example.com");

        // Header (optional)
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Gabung body + header
        HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);

        // Kirim POST dan dapatkan respons (misal tipe String)
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

        System.out.println("Response: " + response.getBody());
    }

}
