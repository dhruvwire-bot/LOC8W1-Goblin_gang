package com.saathi.backend.repository;

import com.saathi.backend.entity.Booking;
import com.saathi.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByCustomer(User customer);

    List<Booking> findByWorker_User_Email(String email);

    List<Booking> findByStatus(Booking.Status status);

    // NEW — check if customer has any active booking
    boolean existsByCustomerAndStatusIn(
            User customer,
            List<Booking.Status> statuses
    );

    // get completed bookings for a specific worker
    List<Booking> findByWorker_User_EmailAndStatus(
            String email,
            Booking.Status status
    );

}