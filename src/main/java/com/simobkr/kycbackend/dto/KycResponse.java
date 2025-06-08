package com.simobkr.kycbackend.dto;

import com.simobkr.kycbackend.entity.KycStatus;
import lombok.*;

@Data
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class KycResponse {
    private Long userId;
    private String email;
    private KycStatus status;
    private String applicantId;
    private String accessToken;
}
