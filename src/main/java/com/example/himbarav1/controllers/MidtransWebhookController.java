package com.example.himbarav1.controllers;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.himbarav1.services.CheckOutService;

@RestController
@RequestMapping("/api/webhook")
public class MidtransWebhookController {

    @Autowired
    private CheckOutService orderService;

    @PostMapping
    public ResponseEntity<String> handleWebhook(@RequestBody Map<String, Object> payload) {
        String orderId = (String) payload.get("order_id");
        String transactionStatus = (String) payload.get("transaction_status");
        // String fraudStatus = (String) payload.get("fraud_status");

        System.out.printf("Webhook diterima untuk order %s status %s%n", orderId, transactionStatus);

        // Update status di database
        orderService.updateStatus(orderId, transactionStatus);

        return ResponseEntity.ok("Webhook processed");
    }
}
