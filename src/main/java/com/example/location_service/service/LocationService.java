package com.example.location_service.service;

import com.example.location_service.config.RabbitMQConfig;
import com.example.location_service.dto.LocationBroadcast;
import com.example.location_service.dto.LocationUpdateMessage;
import com.example.location_service.dto.LocationUpdateRequest;
import com.example.location_service.entity.TrackedObject;
import com.example.location_service.entity.User;
import com.example.location_service.repository.TrackedObjectRepository;
import com.example.location_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LocationService {

    private final TrackedObjectRepository trackedObjectRepository;
    private final UserRepository userRepository;
    private final RabbitTemplate rabbitTemplate;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public LocationBroadcast updateLocation(LocationUpdateRequest request, Long userId) {
        TrackedObject obj = trackedObjectRepository.findByIdAndUserId(request.getObjectId(), userId)
                .orElseThrow(() -> new IllegalArgumentException("Object not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        obj.setLatitude(request.getLatitude());
        obj.setLongitude(request.getLongitude());
        trackedObjectRepository.save(obj);

        // Publish to RabbitMQ for geofence checking
        LocationUpdateMessage message = LocationUpdateMessage.builder()
                .objectId(obj.getId())
                .userId(userId)
                .objectName(obj.getName())
                .objectType(obj.getType())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .geofenceCenterLat(user.getGeofenceCenterLat())
                .geofenceCenterLng(user.getGeofenceCenterLng())
                .geofenceRadiusMeters(user.getGeofenceRadiusMeters())
                .build();

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.LOCATION_EXCHANGE,
                RabbitMQConfig.LOCATION_ROUTING_KEY,
                message
        );

        log.info("Location update sent to RabbitMQ for object {}", obj.getId());

        // Build response
        LocationBroadcast broadcast = LocationBroadcast.builder()
                .objectId(obj.getId())
                .name(obj.getName())
                .type(obj.getType())
                .latitude(obj.getLatitude())
                .longitude(obj.getLongitude())
                .outsideGeofence(obj.getOutsideGeofence())
                .build();

        // Broadcast to WebSocket subscribers
        messagingTemplate.convertAndSend("/topic/locations/" + userId, broadcast);

        return broadcast;
    }

    public List<LocationBroadcast> getAllLocations(Long userId) {
        return trackedObjectRepository.findByUserId(userId)
                .stream()
                .map(obj -> LocationBroadcast.builder()
                        .objectId(obj.getId())
                        .name(obj.getName())
                        .type(obj.getType())
                        .latitude(obj.getLatitude())
                        .longitude(obj.getLongitude())
                        .outsideGeofence(obj.getOutsideGeofence())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public void updateOutsideGeofence(Long objectId, Boolean outsideGeofence) {
        TrackedObject obj = trackedObjectRepository.findById(objectId)
                .orElseThrow(() -> new IllegalArgumentException("Object not found"));

        obj.setOutsideGeofence(outsideGeofence);
        trackedObjectRepository.save(obj);

        // Broadcast updated status
        LocationBroadcast broadcast = LocationBroadcast.builder()
                .objectId(obj.getId())
                .name(obj.getName())
                .type(obj.getType())
                .latitude(obj.getLatitude())
                .longitude(obj.getLongitude())
                .outsideGeofence(outsideGeofence)
                .build();

        messagingTemplate.convertAndSend("/topic/locations/" + obj.getUserId(), broadcast);
    }
}
