package com.example.location_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LocationBroadcast {
    private Long objectId;
    private String name;
    private String type;
    private Double latitude;
    private Double longitude;
    private Boolean outsideGeofence;
}
