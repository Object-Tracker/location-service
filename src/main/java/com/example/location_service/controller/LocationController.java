package com.example.location_service.controller;

import com.example.location_service.dto.LocationBroadcast;
import com.example.location_service.dto.LocationUpdateRequest;
import com.example.location_service.entity.User;
import com.example.location_service.repository.UserRepository;
import com.example.location_service.service.JwtService;
import com.example.location_service.service.LocationService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/locations")
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    @PostMapping("/update")
    public ResponseEntity<LocationBroadcast> updateLocation(
            @RequestBody LocationUpdateRequest request,
            HttpServletRequest httpRequest
    ) {
        Long userId = extractUserId(httpRequest);
        return ResponseEntity.ok(locationService.updateLocation(request, userId));
    }

    @GetMapping
    public ResponseEntity<List<LocationBroadcast>> getAllLocations(HttpServletRequest httpRequest) {
        Long userId = extractUserId(httpRequest);
        return ResponseEntity.ok(locationService.getAllLocations(userId));
    }

    // WebSocket endpoint for location updates
    @MessageMapping("/location.update")
    public void handleLocationUpdate(@Payload LocationUpdateRequest request, SimpMessageHeaderAccessor headerAccessor) {
        // Extract userId from session attributes (set during WebSocket handshake)
        Map<String, Object> sessionAttributes = headerAccessor.getSessionAttributes();
        if (sessionAttributes != null && sessionAttributes.containsKey("userId")) {
            Long userId = (Long) sessionAttributes.get("userId");
            locationService.updateLocation(request, userId);
        }
    }

    private Long extractUserId(HttpServletRequest request) {
        String token = extractJwtFromCookie(request);
        if (token == null) {
            throw new IllegalArgumentException("No authentication token found");
        }

        String username = jwtService.extractUsername(token);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return user.getUserId();
    }

    private String extractJwtFromCookie(HttpServletRequest request) {
        if (request.getCookies() == null) return null;

        for (Cookie cookie : request.getCookies()) {
            if ("token".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
