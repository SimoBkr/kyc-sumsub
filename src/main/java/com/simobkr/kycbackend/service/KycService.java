package com.simobkr.kycbackend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.simobkr.kycbackend.dto.CreateUserRequest;
import com.simobkr.kycbackend.entity.KycStatus;
import com.simobkr.kycbackend.entity.User;
import com.simobkr.kycbackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class KycService {

    private final UserRepository userRepository;

    private final SumsubService sumsubService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public User createUser(CreateUserRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("User with this email already exists");
        }

        // Create user in database
        User user = new User(request.getEmail(), request.getFirstName(),
                request.getLastName(), request.getPhone());
        user = userRepository.save(user);

        // Create applicant in Sumsub
        String applicantId = sumsubService.createApplicant(
                request.getEmail(),
                request.getFirstName(),
                request.getLastName(),
                request.getPhone()
        );

        user.setSumsubApplicantId(applicantId);
        user.setKycStatus(KycStatus.IN_PROGRESS);

        return userRepository.save(user);
    }

    public String generateVerificationToken(User user) {
        return sumsubService.generateAccessToken(user.getSumsubApplicantId(), "basic-kyc-level");
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public User refreshUserStatus(Long id) {
        User user = getUserById(id);

        Map<String, Object> status = sumsubService.getApplicantStatus(user.getSumsubApplicantId());
        KycStatus newStatus = mapSumSubStatus(status);

        if (newStatus != user.getKycStatus()) {
            user.setKycStatus(newStatus);
            user.setUpdatedAt(LocalDateTime.now());
            user = userRepository.save(user);
        }

        return user;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public void processWebhook(String payload) {
        try {
            JsonNode webhookData = objectMapper.readTree(payload);
            String applicantId = webhookData.get("applicantId").asText();

            User user = userRepository.findBySumsubApplicantId(applicantId)
                    .orElseThrow(() -> new RuntimeException("User not found for applicant ID: " + applicantId));

            String reviewStatus = webhookData.get("reviewStatus").asText();
            KycStatus newStatus = mapReviewStatus(reviewStatus);

            user.setKycStatus(newStatus);
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);

        } catch (Exception e) {
            throw new RuntimeException("Failed to process webhook", e);
        }
    }

    private KycStatus mapSumSubStatus(Map<String, Object> status) {
        String reviewStatus = (String) status.get("reviewStatus");
        return mapReviewStatus(reviewStatus);
    }

    private KycStatus mapReviewStatus(String reviewStatus) {
        switch (reviewStatus.toLowerCase()) {
            case "init":
            case "pending":
                return KycStatus.PENDING;
            case "queued":
            case "onhold":
                return KycStatus.IN_PROGRESS;
            case "completed":
                return KycStatus.APPROVED;
            case "rejected":
                return KycStatus.REJECTED;
            default:
                return KycStatus.REQUIRES_REVIEW;
        }
    }
}
