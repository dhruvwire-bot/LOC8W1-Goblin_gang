package com.saathi.backend.repository;

import com.saathi.backend.entity.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {

    // check if rating already exists for this booking
    boolean existsByBookingId(Long bookingId);

    // get rating by booking
    Optional<Rating> findByBookingId(Long bookingId);
}