package com.example.himbarav1.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.himbarav1.dto.CheckOut;
import com.example.himbarav1.services.CheckOutService;

@RestController
@RequestMapping("/api/checkout")
public class CheckOutController {

    private static final Logger logger = LoggerFactory.getLogger(CheckOutController.class);

    private final CheckOutService checkOutService;

    public CheckOutController(CheckOutService checkOutService) {
        this.checkOutService = checkOutService;
    }

    @PostMapping
    public ResponseEntity<String> checkout(
            @RequestHeader("X-Session-ID") String sessionId,
            @RequestBody CheckOut checkOut) throws Exception {

        long startTime = System.currentTimeMillis();
        MDC.put("session_id", sessionId);
        MDC.put("product_id", checkOut.getProductId());

        logger.info("Memulai proses checkout untuk product: {}", checkOut.getProductName());

        ResponseEntity<String> response = checkOutService.postToMidtrans(checkOut, sessionId);

        long duration = System.currentTimeMillis() - startTime;
        MDC.put("duration", duration + "ms");
        logger.info("Checkout selesai dalam {} ms", duration);

        MDC.clear();
        return response;
    }
}
