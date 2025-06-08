package com.simobkr.kycbackend.controller;

import com.simobkr.kycbackend.dto.CreateUserRequest;
import com.simobkr.kycbackend.dto.KycResponse;
import com.simobkr.kycbackend.entity.User;
import com.simobkr.kycbackend.events.KycEventProducer;
import com.simobkr.kycbackend.service.KycService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/kyc")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class KycController {

    private final KycService kycService;

    private final KycEventProducer producer;

    @GetMapping("/users/{id}/status")
    public ResponseEntity<KycResponse> getUserStatus(@PathVariable Long id) {
        User user = kycService.refreshUserStatus(id);

        return ResponseEntity.ok(new KycResponse(
                user.getId(),
                user.getEmail(),
                user.getKycStatus(),
                user.getSumsubApplicantId(),
                null
        ));
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = kycService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<User> getUser(@PathVariable Long id) {
        User user = kycService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/start")
    public ResponseEntity<String> startKyc(@RequestBody Map<String, String> payload) {
        UUID userId = UUID.fromString(payload.get("userId"));
        producer.publishKycStarted(userId);
        return ResponseEntity.ok("KYC started for user " + userId);
    }

    @PostMapping("/users")
    public ResponseEntity<KycResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        User user = kycService.createUser(request);
        String accessToken = kycService.generateVerificationToken(user);

        return ResponseEntity.ok(new KycResponse(
                user.getId(),
                user.getEmail(),
                user.getKycStatus(),
                user.getSumsubApplicantId(),
                accessToken
        ));
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(@RequestBody String payload) {
        kycService.processWebhook(payload);
        return ResponseEntity.ok("OK");
    }
}
