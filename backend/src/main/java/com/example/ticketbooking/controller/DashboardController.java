package com.example.ticketbooking.controller;

import com.example.ticketbooking.model.Train;
import com.example.ticketbooking.model.Role;
import com.example.ticketbooking.model.Ticket;
import com.example.ticketbooking.model.User;
import com.example.ticketbooking.model.Route;
import com.example.ticketbooking.service.TrainService;
import com.example.ticketbooking.service.RouteService;
import com.example.ticketbooking.service.TicketService;
import com.example.ticketbooking.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controller REST phuc vu cac du lieu thong ke va danh sach cho Admin, Seller va Buyer.
 */
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final UserService userService;
    private final TrainService trainService;
    private final TicketService ticketService;
    private final RouteService routeService;

    @Autowired
    public DashboardController(UserService userService, TrainService trainService, TicketService ticketService, RouteService routeService) {
        this.userService = userService;
        this.trainService = trainService;
        this.ticketService = ticketService;
        this.routeService = routeService;
    }

    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            return null;
        }
        return (User) userService.loadUserByUsername(auth.getName());
    }

    /**
     * Endpoint lay du lieu Dashboard cho Admin.
     */
    @GetMapping("/admin")
    public ResponseEntity<?> getAdminDashboard() {
        User currentUser = getAuthenticatedUser();
        if (currentUser == null || currentUser.getRole() != Role.ROLE_ADMIN) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"message\": \"Unauthorized\"}");
        }

        List<User> users = userService.findAllUsers();
        List<Train> trains = trainService.getAllTrains();
        List<Ticket> tickets = ticketService.getAllTickets();

        long totalBuyers = users.stream().filter(u -> u.getRole() == Role.ROLE_BUYER).count();
        long totalSellers = users.stream().filter(u -> u.getRole() == Role.ROLE_SELLER).count();
        double totalRevenue = tickets.stream()
                .filter(t -> t.getStatus() == Ticket.Status.BOOKED)
                .mapToDouble(Ticket::getTotalPrice)
                .sum();
        long totalBookings = tickets.stream()
                .filter(t -> t.getStatus() == Ticket.Status.BOOKED)
                .count();

        Map<String, Object> data = new HashMap<>();
        data.put("totalBuyers", totalBuyers);
        data.put("totalSellers", totalSellers);
        data.put("totalRevenue", totalRevenue);
        data.put("totalBookings", totalBookings);
        
        data.put("users", users.stream()
                .map(u -> new AuthController.UserResponse(u.getId(), u.getUsername(), u.getEmail(), u.getRole()))
                .collect(Collectors.toList()));
        
        data.put("trains", trains.stream()
                .map(this::mapToTrainResponse)
                .collect(Collectors.toList()));
        
        data.put("tickets", tickets.stream()
                .map(this::mapToTicketResponse)
                .collect(Collectors.toList()));

        return ResponseEntity.ok(data);
    }

    /**
     * Endpoint lay du lieu Dashboard cho Seller.
     */
    @GetMapping("/seller")
    public ResponseEntity<?> getSellerDashboard() {
        User currentUser = getAuthenticatedUser();
        if (currentUser == null || currentUser.getRole() != Role.ROLE_SELLER) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"message\": \"Unauthorized\"}");
        }

        List<Train> trains = trainService.getTrainsBySeller(currentUser.getId());
        List<Ticket> tickets = ticketService.getTicketsBySeller(currentUser.getId());
        List<Route> routes = routeService.getRoutesBySeller(currentUser.getId());

        double totalRevenue = tickets.stream()
                .filter(t -> t.getStatus() == Ticket.Status.BOOKED)
                .mapToDouble(Ticket::getTotalPrice)
                .sum();
        long totalTicketsSold = tickets.stream()
                .filter(t -> t.getStatus() == Ticket.Status.BOOKED)
                .mapToLong(Ticket::getQuantity)
                .sum();

        Map<String, Object> data = new HashMap<>();
        data.put("totalRevenue", totalRevenue);
        data.put("totalTicketsSold", totalTicketsSold);
        
        data.put("trains", trains.stream()
                .map(this::mapToTrainResponse)
                .collect(Collectors.toList()));
        
        data.put("tickets", tickets.stream()
                .map(this::mapToTicketResponse)
                .collect(Collectors.toList()));

        data.put("routes", routes.stream()
                .map(r -> new RouteResponse(r.getId(), r.getDepartureStation(), r.getArrivalStation()))
                .collect(Collectors.toList()));

        return ResponseEntity.ok(data);
    }

    /**
     * Endpoint lay du lieu Dashboard cho Buyer.
     */
    @GetMapping("/buyer")
    public ResponseEntity<?> getBuyerDashboard() {
        User currentUser = getAuthenticatedUser();
        if (currentUser == null || currentUser.getRole() != Role.ROLE_BUYER) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"message\": \"Unauthorized\"}");
        }

        List<Ticket> tickets = ticketService.getTicketsByBuyer(currentUser.getId());
        List<TicketController.TicketResponse> responses = tickets.stream()
                .map(this::mapToTicketResponse)
                .collect(Collectors.toList());

        Map<String, Object> data = new HashMap<>();
        data.put("tickets", responses);
        return ResponseEntity.ok(data);
    }

    /**
     * Admin khoa/mo khoa tai khoan.
     */
    @PostMapping("/admin/toggle-user/{id}")
    public ResponseEntity<?> toggleUserStatus(@PathVariable("id") Long id) {
        User currentUser = getAuthenticatedUser();
        if (currentUser == null || currentUser.getRole() != Role.ROLE_ADMIN) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"message\": \"Unauthorized\"}");
        }

        if (currentUser.getId().equals(id)) {
            return ResponseEntity.badRequest().body("{\"message\": \"You cannot toggle your own status\"}");
        }

        userService.toggleUserStatus(id);
        return ResponseEntity.ok("{\"message\": \"User status toggled successfully\"}");
    }

    /**
     * Admin cap nhat vai tro (Role) cua nguoi dung.
     */
    @PostMapping("/admin/update-role/{id}")
    public ResponseEntity<?> updateUserRole(@PathVariable("id") Long id, @RequestParam("role") Role role) {
        User currentUser = getAuthenticatedUser();
        if (currentUser == null || currentUser.getRole() != Role.ROLE_ADMIN) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"message\": \"Unauthorized\"}");
        }

        if (currentUser.getId().equals(id)) {
            return ResponseEntity.badRequest().body("{\"message\": \"You cannot update your own role\"}");
        }

        userService.updateUserRole(id, role);
        return ResponseEntity.ok("{\"message\": \"User role updated successfully\"}");
    }

    private TrainController.TrainResponse mapToTrainResponse(Train train) {
        return new TrainController.TrainResponse(
                train.getId(),
                train.getTrainNumber(),
                train.getRoute() != null ? train.getRoute().getId() : null,
                train.getDepartureStation(),
                train.getArrivalStation(),
                train.getDepartureDate(),
                train.getDepartureTime(),
                train.getArrivalDate(),
                train.getArrivalTime(),
                train.getPrice(),
                train.getTotalSeats(),
                train.getAvailableSeats(),
                train.getSeller() != null ? train.getSeller().getId() : null,
                train.getSeller() != null ? train.getSeller().getUsername() : null
        );
    }

    private TicketController.TicketResponse mapToTicketResponse(Ticket ticket) {
        return new TicketController.TicketResponse(
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

    // --- Inner DTO for Route ---
    public static class RouteResponse {
        private Long id;
        private String departureStation;
        private String arrivalStation;

        public RouteResponse(Long id, String departureStation, String arrivalStation) {
            this.id = id;
            this.departureStation = departureStation;
            this.arrivalStation = arrivalStation;
        }

        public Long getId() { return id; }
        public String getDepartureStation() { return departureStation; }
        public String getArrivalStation() { return arrivalStation; }
    }
}
