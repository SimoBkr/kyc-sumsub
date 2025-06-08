package com.simobkr.kycbackend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.sql.Timestamp;
import java.util.UUID;

@Data
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "kyc_verifications")
public class KycVerification {
    @Id
    private UUID id;
    @Column(name = "user_id", nullable = false)
    private UUID userId;
    private String status;
    private Timestamp submittedAt;
    private Timestamp verifiedAt;
}
