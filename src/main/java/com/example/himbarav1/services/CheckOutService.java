package com.example.himbarav1.services;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.himbarav1.dto.CheckClient;
import com.example.himbarav1.dto.CheckOut;
import com.example.himbarav1.models.entities.Client;
import com.example.himbarav1.models.repository.ClientRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class CheckOutService {

    @Value("${midtrans.server-key}")
    private String midtransServerKey;

    private static final Logger logger = LoggerFactory.getLogger(CheckOutService.class);
    private final RestTemplate restTemplate = new RestTemplate();

    // private static final String SERVER_KEY = "YOUR_SERVER_KEY_HERE";
    private static final String MIDTRANS_CHARGE_URL = "https://api.sandbox.midtrans.com/v2/charge";

    @Autowired
    private ClientRepository clientRepository;

    public Client save(CheckClient checkClient, String sessionId) {

        ModelMapper mapper = new ModelMapper();
        Client client = mapper.map(checkClient, Client.class);
        client.setSessionId(sessionId);
        return clientRepository.save(client);
    }

    public ResponseEntity<String> postToMidtrans(CheckOut dto, String sessionId) {
        long startTime = System.currentTimeMillis();

        try {
            // ✅ Validasi sessionId
            if (!clientRepository.existsBySessionId(sessionId)) {
                logger.warn("sessionId tidak ditemukan di database: {}", sessionId);
                return ResponseEntity.status(401).body("Session ID tidak valid atau tidak ditemukan.");
            }

            MDC.put("product_id", dto.getProductId());
            Instant instant = Instant.ofEpochMilli(startTime);
            ZonedDateTime zonedDateTime = instant.atZone(ZoneId.of("Asia/Jakarta"));
            MDC.put("start_timestamp", String.valueOf(zonedDateTime));

            // Ambil data produk
            String priceUrl = "http://localhost:8888/users";

            // Bangun request body
            Map<String, String> requestPayload = Map.of(
                    "phoneNumber", dto.getPhoneNumber(),
                    "bank", dto.getBank());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(requestPayload, headers);

            // Kirim POST ke /users
            ResponseEntity<Map> responseEntity = restTemplate.postForEntity(priceUrl, requestEntity, Map.class);
            Map<String, Object> productResponse = responseEntity.getBody();

            Object price = null;
            if (productResponse != null && Boolean.TRUE.equals(productResponse.get("success"))) {
                var results = (Iterable<Map<String, Object>>) productResponse.get("results");
                for (Map<String, Object> item : results) {
                    if (dto.getProductId().equals(item.get("Id"))) {
                        price = item.get("Price");
                        break;
                    }
                }
            }

            if (price == null) {
                throw new IllegalStateException("Price tidak ditemukan untuk productId: " + dto.getProductId());
            }

            // Payload ke Midtrans
            Map<String, Object> chargePayload = new HashMap<>();
            chargePayload.put("payment_type", "bank_transfer");

            Map<String, Object> transactionDetails = Map.of(
                    "order_id", "ORDER-" + System.currentTimeMillis(),
                    "gross_amount", price);
            chargePayload.put("transaction_details", transactionDetails);

            Map<String, String> bankTransfer = Map.of("bank", "bca");
            chargePayload.put("bank_transfer", bankTransfer);

            // HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            String base64Key = Base64.getEncoder()
                    .encodeToString((midtransServerKey + ":").getBytes(StandardCharsets.UTF_8));
            headers.set("Authorization", "Basic " + base64Key);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(chargePayload, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(MIDTRANS_CHARGE_URL, request, String.class);

            long duration = System.currentTimeMillis() - startTime;
            MDC.put("duration", duration + "ms");
            logger.info("Request ke Midtrans berhasil");
            if (response.getStatusCode().is2xxSuccessful()) {
                // save()
            }
            return response;

        } catch (Exception e) {
            MDC.put("error_code", "500");
            logger.error("Gagal melakukan request ke Midtrans", e);
            throw e;
        } finally {
            MDC.clear();
        }
    }
}
