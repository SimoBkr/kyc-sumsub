package com.simobkr.kycbackend.repository;

import com.simobkr.kycbackend.entity.KycVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface KycVerificationRepository extends JpaRepository<KycVerification, UUID> {
}
