package com.example.himbarav1.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.himbarav1.dto.CheckClient;
import com.example.himbarav1.services.ClientService;

@RestController
@RequestMapping("/api/client")
public class ClientController {

    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    @PostMapping("/check")
    public ResponseEntity<String> checkClient(@RequestBody CheckClient checkClient) {
        // Kirim DTO ke service untuk diteruskan ke endpoint lain
        return clientService.postToExternalApi(checkClient);
    }

}
