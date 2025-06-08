package com.simobkr.kycbackend.events;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class KycEventProducer {

    @Autowired
    private KafkaTemplate<String, KycEvent> kafkaTemplate;

    public void publishKycStarted(UUID userId) {
        KycEvent event = new KycEvent(userId, "KYC_STARTED");
        kafkaTemplate.send("kyc-events", event);
    }
}
