package com.simobkr.kycbackend.events;

import com.simobkr.kycbackend.entity.KycVerification;
import com.simobkr.kycbackend.repository.KycVerificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.UUID;

@Service
public class KycEventListener {

    @Autowired
    private KycVerificationRepository repository;

    @KafkaListener(topics = "kyc-events", groupId = "kyc-group")
    public void handleKycEvent(@Payload KycEvent event) {
        System.out.println("EVENT :::::: "+event.getType());
        if ("KYC_STARTED".equals(event.getType())) {
            KycVerification verification = new KycVerification();
            verification.setId(UUID.randomUUID());
            verification.setUserId(event.getUserId());
            verification.setStatus("IN_PROGRESS");
            verification.setSubmittedAt(new Timestamp(System.currentTimeMillis()));
            repository.save(verification);
        }
    }
}
