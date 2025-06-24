package com.example.himbarav1.services;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.himbarav1.dto.CheckClient;
import com.example.himbarav1.models.entities.Client;
import com.example.himbarav1.models.repository.ClientRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class ClientService {

    private static final Logger logger = LoggerFactory.getLogger(ClientService.class);

    private final RestTemplate restTemplate = new RestTemplate();

    @Autowired
    public ClientRepository clientRepository;

    public Client save(CheckClient checkClient) {

        ModelMapper mapper = new ModelMapper();
        Client client = mapper.map(checkClient, Client.class);
        return clientRepository.save(client);
    }

    public ResponseEntity<String> postToExternalApi(CheckClient dto) {
        long startTime = System.currentTimeMillis();

        try {
            MDC.put("msisdn", dto.getPhoneNumber());
            Instant instant = Instant.ofEpochMilli(startTime);
            ZonedDateTime zonedDateTime = instant.atZone(ZoneId.of("Asia/Jakarta"));
            MDC.put("start_timestamp", String.valueOf(zonedDateTime));
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<CheckClient> request = new HttpEntity<>(dto, headers);

            String url = "http://localhost:8888/users";

            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            long duration = System.currentTimeMillis() - startTime;
            MDC.put("duration", String.valueOf(duration + "ms"));
            logger.info("Request sukses ke endpoint eksternal");
            if (response.getStatusCode().is2xxSuccessful()) {
                save(dto);
            }
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