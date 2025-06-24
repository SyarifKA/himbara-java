package com.example.himbarav1.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.himbarav1.dto.CheckClient;
import com.example.himbarav1.services.ClientService;

@RestController
@RequestMapping("/api/client")
public class ClientController {

    private static final Logger logger = LoggerFactory.getLogger(ClientController.class);

    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    @PostMapping("/check")
    public ResponseEntity<String> checkClient(@RequestHeader("X-Session-ID") String sessionId,
            @RequestBody CheckClient checkClient) {
        // Kirim DTO ke service untuk diteruskan ke endpoint lain
        long startTime = System.currentTimeMillis();
        long duration = System.currentTimeMillis() - startTime; // Calculate duration
        MDC.put("duration", String.valueOf(duration));
        logger.info("Check data user to endpoin whitelist", checkClient);
        MDC.clear();
        return clientService.postToExternalApi(checkClient, sessionId);
    }
}
