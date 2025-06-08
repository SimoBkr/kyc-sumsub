package com.simobkr.kycbackend.events;

import lombok.*;

import java.util.UUID;

@Data
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class KycEvent {

    private UUID userId;
    private String type;

}
