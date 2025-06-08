package com.simobkr.kycbackend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
public class SumsubService {

    @Value("${sumsub.app-token}")
    private String appToken;

    @Value("${sumsub.secret-key}")
    private String secretKey;

    @Value("${sumsub.base-url}")
    private String baseUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String createApplicant(String email, String firstName, String lastName, String phone) {
        try {
            String endpoint = "/resources/applicants";
            String url = baseUrl + endpoint;

            Map<String, Object> applicantData = new HashMap<>();
            applicantData.put("externalUserId", email);

            Map<String, String> info = new HashMap<>();
            info.put("firstName", firstName);
            info.put("lastName", lastName);
            info.put("levelName", "id-only");
            if (phone != null && !phone.isEmpty()) {
                info.put("phone", phone);
            }
            applicantData.put("info", info);

            String requestBody = objectMapper.writeValueAsString(applicantData);

            System.out.println("Request body :::"+ requestBody);

            HttpHeaders headers = createAuthHeaders("POST", endpoint, requestBody);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> request = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            JsonNode responseJson = objectMapper.readTree(response.getBody());
            return responseJson.get("id").asText();

        } catch (Exception e) {
            throw new RuntimeException("Failed to create Sumsub applicant", e);
        }
    }

    public String generateAccessToken(String applicantId, String levelName) {
        try {
            String endpoint = "/resources/accessTokens";
            String url = baseUrl + endpoint;

            Map<String, Object> tokenData = new HashMap<>();
            tokenData.put("userId", applicantId);
            tokenData.put("levelName", levelName != null ? levelName : "basic-kyc-level");
            tokenData.put("ttlInSecs", 3600); // 1 hour

            String requestBody = objectMapper.writeValueAsString(tokenData);

            HttpHeaders headers = createAuthHeaders("POST", endpoint, requestBody);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> request = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            JsonNode responseJson = objectMapper.readTree(response.getBody());
            return responseJson.get("token").asText();

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate access token", e);
        }
    }

    public Map<String, Object> getApplicantStatus(String applicantId) {
        try {
            String endpoint = "/resources/applicants/" + applicantId + "/status";
            String url = baseUrl + endpoint;

            HttpHeaders headers = createAuthHeaders("GET", endpoint, "");
            HttpEntity<String> request = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);

            return objectMapper.readValue(response.getBody(), Map.class);

        } catch (Exception e) {
            throw new RuntimeException("Failed to get applicant status", e);
        }
    }

    private HttpHeaders createAuthHeaders(String method, String endpoint, String body)
            throws NoSuchAlgorithmException, InvalidKeyException {

        long timestamp = System.currentTimeMillis() / 1000;
        String message = timestamp + method + endpoint + body;

        Mac sha256Hmac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        sha256Hmac.init(secretKeySpec);

        byte[] signedBytes = sha256Hmac.doFinal(message.getBytes(StandardCharsets.UTF_8));
        String signature = Base64.getEncoder().encodeToString(signedBytes);

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-App-Token", appToken);
        headers.set("X-App-Access-Sig", signature);
        headers.set("X-App-Access-Ts", String.valueOf(timestamp));

        return headers;
    }
}
