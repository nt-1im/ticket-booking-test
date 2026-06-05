package com.example.ticketbooking.repository;

import com.example.ticketbooking.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByBuyerIdOrderByBookingDateDesc(Long buyerId);
    List<Ticket> findByTrainId(Long trainId);
    List<Ticket> findByTrainSellerIdOrderByBookingDateDesc(Long sellerId);
}

