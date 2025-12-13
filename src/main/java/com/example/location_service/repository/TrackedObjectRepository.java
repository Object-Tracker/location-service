package com.example.location_service.repository;

import com.example.location_service.entity.TrackedObject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrackedObjectRepository extends JpaRepository<TrackedObject, Long> {
    List<TrackedObject> findByUserId(Long userId);
    Optional<TrackedObject> findByIdAndUserId(Long id, Long userId);
}
