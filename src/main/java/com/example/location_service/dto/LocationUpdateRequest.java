package com.example.location_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LocationUpdateRequest {
    private Long objectId;
    private Double latitude;
    private Double longitude;
}
