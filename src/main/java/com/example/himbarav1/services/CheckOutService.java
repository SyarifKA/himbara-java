package com.example.himbarav1.services;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
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

import com.example.himbarav1.dto.CheckOut;
import com.example.himbarav1.models.entities.Client;
import com.example.himbarav1.models.repository.ClientRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

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

    // public Client save(CheckOut checkOut, Integer price, String trxId, String
    // status, String sessionId) {

    // ModelMapper mapper = new ModelMapper();
    // mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
    // Client client = mapper.map(checkOut, Client.class);
    // client.setPrice(price);
    // client.setStatus(status);
    // client.setOrderId(trxId);
    // client.setSessionId(sessionId);
    // return clientRepository.save(client);
    // }

    public Client save(CheckOut checkOut, Integer price, String trxId, String status, String sessionId) {
        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

        // Cari client existing berdasarkan sessionId
        Optional<Client> optionalClient = clientRepository.findBySessionId(sessionId);

        Client client;

        if (optionalClient.isPresent()) {
            // Update existing
            client = optionalClient.get();
        } else {
            // Insert baru
            client = new Client();
            client.setSessionId(sessionId);
        }

        // Mapping field dari CheckOut (hati-hati kalau ingin overwrite data)
        mapper.map(checkOut, client);

        client.setPrice(price);
        client.setStatus(status);
        client.setOrderId(trxId);

        return clientRepository.save(client);
    }

    public ResponseEntity<String> postToMidtrans(CheckOut dto, String sessionId) throws Exception {
        long startTime = System.currentTimeMillis();

        try {
            // Validasi sessionId
            if (!clientRepository.existsBySessionId(sessionId)) {
                logger.warn("sessionId tidak ditemukan di database: {}", sessionId);
                return ResponseEntity.status(401).body("Session ID tidak valid atau tidak ditemukan.");
            }

            MDC.put("product_id", dto.getProductId());
            Instant instant = Instant.ofEpochMilli(startTime);
            ZonedDateTime zonedDateTime = instant.atZone(ZoneId.of("Asia/Jakarta"));
            MDC.put("start_timestamp", String.valueOf(zonedDateTime));

            // Bangun request body untuk service internal
            Map<String, String> requestPayload = Map.of(
                    "phoneNumber", dto.getPhoneNumber(),
                    "bank", dto.getBank());

            HttpHeaders requestHeaders = new HttpHeaders();
            requestHeaders.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(requestPayload, requestHeaders);

            // Call ke http://localhost:8888/users
            String priceUrl = "http://localhost:8888/users";
            ResponseEntity<Map> responseEntity = restTemplate.postForEntity(priceUrl, requestEntity, Map.class);

            Map<String, Object> productResponse = responseEntity.getBody();
            if (productResponse == null || !Boolean.TRUE.equals(productResponse.get("success"))) {
                throw new IllegalStateException("Gagal mengambil data produk dari service.");
            }

            // Ambil results
            Object resultsObj = productResponse.get("results");
            if (!(resultsObj instanceof Iterable<?> results)) {
                throw new IllegalStateException("Response results tidak valid.");
            }
            logger.debug("Check product response : {}", resultsObj);

            Integer price = null;
            for (Object itemObj : results) {
                if (!(itemObj instanceof Map<?, ?> item))
                    continue;
                Object idValue = item.get("Id");
                if (dto.getProductId().equals(idValue)) {
                    Object rawPrice = item.get("Price");
                    if (rawPrice instanceof Number num) {
                        price = num.intValue();
                    } else {
                        throw new IllegalStateException("Price bukan tipe numerik.");
                    }
                    break;
                }
            }

            if (price == null) {
                throw new IllegalStateException("Price tidak ditemukan untuk productId: " + dto.getProductId());
            }

            // Payload untuk Midtrans
            Map<String, Object> transactionDetails = Map.of(
                    "order_id", "ORDER-" + System.currentTimeMillis(),
                    "gross_amount", price);

            Map<String, Object> chargePayload = new HashMap<>();
            chargePayload.put("payment_type", "bank_transfer");
            chargePayload.put("transaction_details", transactionDetails);
            chargePayload.put("bank_transfer", Map.of("bank", dto.getBank()));

            // Headers Midtrans
            HttpHeaders midtransHeaders = new HttpHeaders();
            midtransHeaders.setContentType(MediaType.APPLICATION_JSON);
            String base64Key = Base64.getEncoder()
                    .encodeToString((midtransServerKey + ":").getBytes(StandardCharsets.UTF_8));
            midtransHeaders.set("Authorization", "Basic " + base64Key);

            // Request Midtrans
            HttpEntity<Map<String, Object>> midtransRequest = new HttpEntity<>(chargePayload, midtransHeaders);
            ResponseEntity<String> response = restTemplate.postForEntity(MIDTRANS_CHARGE_URL, midtransRequest,
                    String.class);

            long duration = System.currentTimeMillis() - startTime;
            MDC.put("duration", duration + "ms");
            logger.info("Mengirim data ke midtrans : {}", midtransRequest);
            logger.info("Request ke Midtrans berhasil dalam {} ms", duration);

            // Jika sukses, simpan data ke database
            if (response.getStatusCode().is2xxSuccessful()) {
                ObjectMapper mapper = new ObjectMapper();
                String responseBody = response.getBody();
                Map<String, Object> jsonMap = mapper.readValue(responseBody, Map.class);

                String orderId = (String) jsonMap.get("order_id");
                String status = (String) jsonMap.get("transaction_status");
                save(dto, price, orderId, status, sessionId);
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

    public void updateStatus(String orderId, String transactionStatus) {
        Client order = clientRepository.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order tidak ditemukan: " + orderId));

        // Kamu bisa mapping status Midtrans ke status internal kalau mau
        order.setStatus(transactionStatus);
        OffsetDateTime timestamp = OffsetDateTime.now().withNano(0);
        order.setUpdated_at(timestamp);

        clientRepository.save(order);
    }
}
