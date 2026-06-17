package com.example.ticketbooking.controller;

import com.example.ticketbooking.model.Ticket;
import com.example.ticketbooking.model.Role;
import com.example.ticketbooking.model.User;
import com.example.ticketbooking.service.TicketService;
import com.example.ticketbooking.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller REST xu ly cac yeu cau lien quan den dat va huy ve tau.
 */
@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketService ticketService;
    private final UserService userService;

    @Autowired
    public TicketController(TicketService ticketService, UserService userService) {
        this.ticketService = ticketService;
        this.userService = userService;
    }

    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            return null;
        }
        return (User) userService.loadUserByUsername(auth.getName());
    }

    /**
     * Xu ly gui yeu cau dat ve tau.
     */
    @PostMapping("/book/{trainId}")
    public ResponseEntity<?> bookTicket(@PathVariable("trainId") Long trainId,
                                        @Valid @RequestBody BookTicketRequest bookRequest) {
        User currentUser = getAuthenticatedUser();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"message\": \"Unauthorized\"}");
        }
        if (currentUser.getRole() != Role.ROLE_BUYER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("{\"message\": \"Only buyers can book tickets\"}");
        }

        try {
            List<Ticket> tickets = ticketService.bookTicketWithSeats(
                    trainId, 
                    currentUser, 
                    bookRequest.getSelectedSeats(), 
                    bookRequest.getPassengerName(), 
                    bookRequest.getPassengerIdCard()
            );

            List<TicketResponse> responses = tickets.stream()
                    .map(this::mapToTicketResponse)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"message\": \"Booking failed: " + e.getMessage() + "\"}");
        }
    }

    /**
     * Xu ly yeu cau huy ve tau da dat.
     */
    @PostMapping("/cancel/{ticketId}")
    public ResponseEntity<?> cancelTicket(@PathVariable("ticketId") Long ticketId) {
        User currentUser = getAuthenticatedUser();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"message\": \"Unauthorized\"}");
        }
        if (currentUser.getRole() != Role.ROLE_BUYER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("{\"message\": \"Only buyers can cancel tickets\"}");
        }

        try {
            ticketService.cancelTicket(ticketId, currentUser);
            return ResponseEntity.ok("{\"message\": \"Ticket booking cancelled successfully. Funds refunded.\"}");
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("{\"message\": \"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"message\": \"Cancellation failed: " + e.getMessage() + "\"}");
        }
    }

    private TicketResponse mapToTicketResponse(Ticket ticket) {
        return new TicketResponse(
                ticket.getId(),
                ticket.getTrain().getId(),
                ticket.getTrain().getTrainNumber(),
                ticket.getTrain().getDepartureStation(),
                ticket.getTrain().getArrivalStation(),
                ticket.getTrain().getDepartureDate().toString(),
                ticket.getTrain().getDepartureTime().toString(),
                ticket.getPassengerName(),
                ticket.getPassengerIdCard(),
                ticket.getCarriageNumber(),
                ticket.getSeatNumber(),
                ticket.getQuantity(),
                ticket.getTotalPrice(),
                ticket.getBookingDate().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                ticket.getStatus().name()
        );
    }

    // --- DTO Classes ---

    public static class BookTicketRequest {
        @NotEmpty(message = "At least one seat must be selected")
        private List<Integer> selectedSeats;

        @NotBlank(message = "Passenger name is required")
        private String passengerName;

        @NotBlank(message = "Passenger ID card is required")
        private String passengerIdCard;

        public List<Integer> getSelectedSeats() { return selectedSeats; }
        public void setSelectedSeats(List<Integer> selectedSeats) { this.selectedSeats = selectedSeats; }
        public String getPassengerName() { return passengerName; }
        public void setPassengerName(String passengerName) { this.passengerName = passengerName; }
        public String getPassengerIdCard() { return passengerIdCard; }
        public void setPassengerIdCard(String passengerIdCard) { this.passengerIdCard = passengerIdCard; }
    }

    public static class TicketResponse {
        private Long id;
        private Long trainId;
        private String trainNumber;
        private String departureStation;
        private String arrivalStation;
        private String departureDate;
        private String departureTime;
        private String passengerName;
        private String passengerIdCard;
        private int carriageNumber;
        private int seatNumber;
        private int quantity;
        private double totalPrice;
        private String bookingDate;
        private String status;

        public TicketResponse(Long id, Long trainId, String trainNumber, String departureStation, String arrivalStation,
                              String departureDate, String departureTime, String passengerName, String passengerIdCard,
                              int carriageNumber, int seatNumber, int quantity, double totalPrice, String bookingDate, String status) {
            this.id = id;
            this.trainId = trainId;
            this.trainNumber = trainNumber;
            this.departureStation = departureStation;
            this.arrivalStation = arrivalStation;
            this.departureDate = departureDate;
            this.departureTime = departureTime;
            this.passengerName = passengerName;
            this.passengerIdCard = passengerIdCard;
            this.carriageNumber = carriageNumber;
            this.seatNumber = seatNumber;
            this.quantity = quantity;
            this.totalPrice = totalPrice;
            this.bookingDate = bookingDate;
            this.status = status;
        }

        public Long getId() { return id; }
        public Long getTrainId() { return trainId; }
        public String getTrainNumber() { return trainNumber; }
        public String getDepartureStation() { return departureStation; }
        public String getArrivalStation() { return arrivalStation; }
        public String getDepartureDate() { return departureDate; }
        public String getDepartureTime() { return departureTime; }
        public String getPassengerName() { return passengerName; }
        public String getPassengerIdCard() { return passengerIdCard; }
        public int getCarriageNumber() { return carriageNumber; }
        public int getSeatNumber() { return seatNumber; }
        public int getQuantity() { return quantity; }
        public double getTotalPrice() { return totalPrice; }
        public String getBookingDate() { return bookingDate; }
        public String getStatus() { return status; }
    }
}
