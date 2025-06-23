package com.example.himbarav1.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
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

    private static final Logger logger = LoggerFactory.getLogger(ClientService.class);

    private final RestTemplate restTemplate = new RestTemplate();

    public ResponseEntity<String> postToExternalApi(CheckClient dto) {
        long startTime = System.currentTimeMillis();

        try {
            MDC.put("msisdn", dto.getPhoneNumber());
            MDC.put("start_timestamp", String.valueOf(startTime));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<CheckClient> request = new HttpEntity<>(dto, headers);

            String url = "http://localhost:8888/users";

            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            long duration = System.currentTimeMillis() - startTime;
            MDC.put("duration", String.valueOf(duration + "ms"));
            logger.info("Request sukses ke endpoint eksternal");

            return response;
        } catch (Exception e) {
            MDC.put("error_code", "500");
            logger.error("Gagal request", e);
            throw e;
        } finally {
            MDC.clear();
        }
    }

}