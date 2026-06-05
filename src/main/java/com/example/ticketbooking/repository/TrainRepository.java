package com.example.ticketbooking.repository;

import com.example.ticketbooking.model.Train;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TrainRepository extends JpaRepository<Train, Long> {
    List<Train> findBySellerId(Long sellerId);
    List<Train> findByDepartureStationContainingIgnoreCaseAndArrivalStationContainingIgnoreCaseAndDepartureDate(
            String departureStation, String arrivalStation, LocalDate departureDate);
    List<Train> findAllByOrderByDepartureDateAscDepartureTimeAsc();
}
