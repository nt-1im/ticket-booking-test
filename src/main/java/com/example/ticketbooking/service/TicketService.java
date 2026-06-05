package com.example.ticketbooking.service;

import com.example.ticketbooking.model.Train;
import com.example.ticketbooking.model.Ticket;
import com.example.ticketbooking.model.User;
import com.example.ticketbooking.repository.TrainRepository;
import com.example.ticketbooking.repository.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;
    private final TrainRepository trainRepository;

    @Autowired
    public TicketService(TicketRepository ticketRepository, TrainRepository trainRepository) {
        this.ticketRepository = ticketRepository;
        this.trainRepository = trainRepository;
    }

    public List<Ticket> getAllTickets() {
        return ticketRepository.findAll();
    }

    public List<Ticket> getTicketsByBuyer(Long buyerId) {
        return ticketRepository.findByBuyerIdOrderByBookingDateDesc(buyerId);
    }

    public List<Ticket> getTicketsBySeller(Long sellerId) {
        return ticketRepository.findByTrainSellerIdOrderByBookingDateDesc(sellerId);
    }

    public Optional<Ticket> getTicketById(Long id) {
        return ticketRepository.findById(id);
    }

    @Transactional
    public Ticket bookTicket(Long trainId, User buyer, int quantity, String passengerName, String passengerIdCard) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be at least 1");
        }
        if (passengerName == null || passengerName.isBlank()) {
            throw new IllegalArgumentException("Passenger name is required");
        }
        if (passengerIdCard == null || passengerIdCard.isBlank()) {
            throw new IllegalArgumentException("Passenger ID Card / Passport is required");
        }

        // Protect against concurrent bookings using string interning of the train id as lock
        synchronized (String.valueOf(trainId).intern()) {
            Train train = trainRepository.findById(trainId)
                    .orElseThrow(() -> new IllegalArgumentException("Train not found with ID: " + trainId));

            if (train.getAvailableSeats() < quantity) {
                throw new IllegalArgumentException("Not enough seats available! Only " + train.getAvailableSeats() + " left.");
            }

            // Calculate carriage and seat numbers:
            // Assume 10 seats per carriage. First booked gets Carriage 1 Seat 1.
            int startSeatIndex = train.getTotalSeats() - train.getAvailableSeats() + 1;
            int carriageNumber = (startSeatIndex - 1) / 10 + 1;
            int seatNumber = (startSeatIndex - 1) % 10 + 1;

            // Deduct seats
            train.setAvailableSeats(train.getAvailableSeats() - quantity);
            trainRepository.save(train);

            // Create ticket (For simplicity in this basic web app, we save quantity tickets in a single pass 
            // or save 1 ticket record containing total quantity and seat assignments)
            double totalPrice = train.getPrice() * quantity;
            Ticket ticket = new Ticket(train, buyer, passengerName, passengerIdCard, carriageNumber, seatNumber, quantity, totalPrice, LocalDateTime.now());
            return ticketRepository.save(ticket);
        }
    }

    @Transactional
    public void cancelTicket(Long ticketId, User currentUser) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found with ID: " + ticketId));

        // Check authorization: only the buyer or an admin can cancel
        boolean isAdmin = currentUser.getRole().name().equals("ROLE_ADMIN");
        boolean isBuyer = ticket.getBuyer().getId().equals(currentUser.getId());

        if (!isAdmin && !isBuyer) {
            throw new SecurityException("You are not authorized to cancel this ticket!");
        }

        if (ticket.getStatus() == Ticket.Status.CANCELLED) {
            throw new IllegalArgumentException("Ticket is already cancelled!");
        }

        synchronized (String.valueOf(ticket.getTrain().getId()).intern()) {
            Train train = ticket.getTrain();
            // Restore seats
            train.setAvailableSeats(train.getAvailableSeats() + ticket.getQuantity());
            trainRepository.save(train);

            // Mark ticket status
            ticket.setStatus(Ticket.Status.CANCELLED);
            ticketRepository.save(ticket);
        }
    }
}

