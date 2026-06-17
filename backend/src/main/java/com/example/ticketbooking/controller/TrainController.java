package com.example.ticketbooking.controller;

import com.example.ticketbooking.model.Train;
import com.example.ticketbooking.model.Role;
import com.example.ticketbooking.model.User;
import com.example.ticketbooking.model.Route;
import com.example.ticketbooking.service.TrainService;
import com.example.ticketbooking.service.RouteService;
import com.example.ticketbooking.service.UserService;
import com.example.ticketbooking.service.TicketService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Controller REST xu ly cac yeu cau lien quan den Chuyen tau (Train).
 */
@RestController
@RequestMapping("/api/trains")
public class TrainController {

    private final TrainService trainService;
    private final UserService userService;
    private final RouteService routeService;
    private final TicketService ticketService;

    @Autowired
    public TrainController(TrainService trainService, UserService userService, RouteService routeService, TicketService ticketService) {
        this.trainService = trainService;
        this.userService = userService;
        this.routeService = routeService;
        this.ticketService = ticketService;
    }

    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            return null;
        }
        return (User) userService.loadUserByUsername(auth.getName());
    }

    private boolean isAuthorizedToManage(Train train, User currentUser) {
        if (currentUser == null)
            return false;
        if (currentUser.getRole() == Role.ROLE_ADMIN)
            return true;
        return train.getSeller() != null && train.getSeller().getId().equals(currentUser.getId());
    }

    /**
     * Lay danh sach hoac tim kiem cac chuyen tau.
     */
    @GetMapping
    public ResponseEntity<List<TrainResponse>> getAllTrains(
            @RequestParam(value = "departure", required = false) String departure,
            @RequestParam(value = "arrival", required = false) String arrival,
            @RequestParam(value = "date", required = false) String dateStr) {

        List<Train> trains;
        LocalDate date = null;
        if (dateStr != null && !dateStr.isBlank()) {
            date = LocalDate.parse(dateStr);
        }

        if ((departure != null && !departure.isBlank()) || (arrival != null && !arrival.isBlank()) || date != null) {
            trains = trainService.searchTrains(departure, arrival, date);
        } else {
            trains = trainService.getAllTrains();
        }

        List<TrainResponse> responses = trains.stream()
                .map(this::mapToTrainResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    /**
     * Lay chi tiet mot chuyen tau cung danh sach ghe da dat.
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getTrainDetail(@PathVariable("id") Long id) {
        Optional<Train> trainOpt = trainService.getTrainById(id);
        if (trainOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Train train = trainOpt.get();
        List<Integer> occupiedSeats = ticketService.getOccupiedSeats(id);
        
        Map<String, Object> response = new HashMap<>();
        response.put("train", mapToTrainResponse(train));
        response.put("occupiedSeats", occupiedSeats);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Tao moi mot chuyen tau (Seller/Admin).
     */
    @PostMapping
    public ResponseEntity<?> createTrain(@Valid @RequestBody TrainRequest trainRequest) {
        User currentUser = getAuthenticatedUser();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"message\": \"Unauthorized\"}");
        }
        if (currentUser.getRole() != Role.ROLE_SELLER && currentUser.getRole() != Role.ROLE_ADMIN) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("{\"message\": \"Only sellers and admins can create trains\"}");
        }

        Route route = null;
        if (trainRequest.getRouteId() != null) {
            Optional<Route> routeOpt = routeService.getRouteById(trainRequest.getRouteId());
            if (routeOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("{\"message\": \"Route not found with ID: " + trainRequest.getRouteId() + "\"}");
            }
            route = routeOpt.get();
        } else {
            String dep = trainRequest.getDepartureStation();
            String arr = trainRequest.getArrivalStation();
            if (dep == null || dep.isBlank() || arr == null || arr.isBlank()) {
                return ResponseEntity.badRequest().body("{\"message\": \"Either Route ID or Departure/Arrival Stations must be provided\"}");
            }
            route = routeService.getOrCreateRoute(dep.trim(), arr.trim(), currentUser);
        }

        try {
            Train train = new Train(
                    trainRequest.getTrainNumber(),
                    route,
                    trainRequest.getDepartureDate(),
                    trainRequest.getDepartureTime(),
                    trainRequest.getArrivalDate(),
                    trainRequest.getArrivalTime(),
                    trainRequest.getPrice(),
                    trainRequest.getTotalSeats(),
                    currentUser
            );
            Train savedTrain = trainService.saveTrain(train, currentUser);
            return ResponseEntity.ok(mapToTrainResponse(savedTrain));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"message\": \"Failed to create train: " + e.getMessage() + "\"}");
        }
    }

    /**
     * Tao hang loat cac chuyen tau theo lich dinh ky hang ngay (Seller/Admin).
     */
    @PostMapping("/recurring")
    public ResponseEntity<?> createRecurringTrains(@Valid @RequestBody RecurringTrainRequest request) {
        User currentUser = getAuthenticatedUser();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"message\": \"Unauthorized\"}");
        }
        if (currentUser.getRole() != Role.ROLE_SELLER && currentUser.getRole() != Role.ROLE_ADMIN) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("{\"message\": \"Only sellers and admins can create trains\"}");
        }

        Route route = null;
        if (request.getRouteId() != null) {
            Optional<Route> routeOpt = routeService.getRouteById(request.getRouteId());
            if (routeOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("{\"message\": \"Route not found with ID: " + request.getRouteId() + "\"}");
            }
            route = routeOpt.get();
        } else {
            String dep = request.getDepartureStation();
            String arr = request.getArrivalStation();
            if (dep == null || dep.isBlank() || arr == null || arr.isBlank()) {
                return ResponseEntity.badRequest().body("{\"message\": \"Either Route ID or Departure/Arrival Stations must be provided\"}");
            }
            route = routeService.getOrCreateRoute(dep.trim(), arr.trim(), currentUser);
        }

        if (request.getStartDate().isAfter(request.getEndDate())) {
            return ResponseEntity.badRequest().body("{\"message\": \"Start date must be before or equal to End date\"}");
        }

        List<Train> createdTrains = new java.util.ArrayList<>();
        LocalDate curDate = request.getStartDate();
        while (!curDate.isAfter(request.getEndDate())) {
            Train train = new Train(
                    request.getTrainNumber(),
                    route,
                    curDate,
                    request.getDepartureTime(),
                    curDate.plusDays(request.getArrivalOffsetDays()),
                    request.getArrivalTime(),
                    request.getPrice(),
                    request.getTotalSeats(),
                    currentUser
            );
            trainService.saveTrain(train, currentUser);
            createdTrains.add(train);
            curDate = curDate.plusDays(1);
        }

        return ResponseEntity.ok("{\"message\": \"Created " + createdTrains.size() + " trains successfully\"}");
    }

    /**
     * Cap nhat thong tin chuyen tau.
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateTrain(@PathVariable("id") Long id, @Valid @RequestBody TrainRequest trainRequest) {
        User currentUser = getAuthenticatedUser();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"message\": \"Unauthorized\"}");
        }

        Optional<Train> existingTrainOpt = trainService.getTrainById(id);
        if (existingTrainOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Train existingTrain = existingTrainOpt.get();
        if (!isAuthorizedToManage(existingTrain, currentUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("{\"message\": \"Unauthorized to manage this train\"}");
        }

        Route route = null;
        if (trainRequest.getRouteId() != null) {
            Optional<Route> routeOpt = routeService.getRouteById(trainRequest.getRouteId());
            if (routeOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("{\"message\": \"Route not found\"}");
            }
            route = routeOpt.get();
        } else {
            String dep = trainRequest.getDepartureStation();
            String arr = trainRequest.getArrivalStation();
            if (dep == null || dep.isBlank() || arr == null || arr.isBlank()) {
                return ResponseEntity.badRequest().body("{\"message\": \"Either Route ID or Departure/Arrival Stations must be provided\"}");
            }
            route = routeService.getOrCreateRoute(dep.trim(), arr.trim(), currentUser);
        }

        try {
            existingTrain.setTrainNumber(trainRequest.getTrainNumber());
            existingTrain.setRoute(route);
            existingTrain.setDepartureDate(trainRequest.getDepartureDate());
            existingTrain.setDepartureTime(trainRequest.getDepartureTime());
            existingTrain.setArrivalDate(trainRequest.getArrivalDate());
            existingTrain.setArrivalTime(trainRequest.getArrivalTime());
            existingTrain.setPrice(trainRequest.getPrice());
            existingTrain.setTotalSeats(trainRequest.getTotalSeats());

            Train updated = trainService.updateTrain(id, existingTrain);
            return ResponseEntity.ok(mapToTrainResponse(updated));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"message\": \"Failed to update train: " + e.getMessage() + "\"}");
        }
    }

    /**
     * Xoa chuyen tau.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTrain(@PathVariable("id") Long id) {
        User currentUser = getAuthenticatedUser();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"message\": \"Unauthorized\"}");
        }

        Optional<Train> trainOpt = trainService.getTrainById(id);
        if (trainOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Train train = trainOpt.get();
        if (!isAuthorizedToManage(train, currentUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("{\"message\": \"Unauthorized to delete this train\"}");
        }

        try {
            trainService.deleteTrain(id);
            return ResponseEntity.ok("{\"message\": \"Train route deleted successfully\"}");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"message\": \"Failed to delete train: " + e.getMessage() + "\"}");
        }
    }

    private TrainResponse mapToTrainResponse(Train train) {
        return new TrainResponse(
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

    // --- DTO Classes ---

    public static class TrainRequest {
        @NotBlank(message = "Train number is required")
        private String trainNumber;

        private Long routeId;
        private String departureStation;
        private String arrivalStation;

        @NotNull(message = "Departure date is required")
        private LocalDate departureDate;

        @NotNull(message = "Departure time is required")
        private LocalTime departureTime;

        @NotNull(message = "Arrival date is required")
        private LocalDate arrivalDate;

        @NotNull(message = "Arrival time is required")
        private LocalTime arrivalTime;

        @Min(value = 0, message = "Price must be non-negative")
        private double price;

        @Min(value = 1, message = "Total seats must be at least 1")
        private int totalSeats;

        public String getTrainNumber() { return trainNumber; }
        public void setTrainNumber(String trainNumber) { this.trainNumber = trainNumber; }
        public Long getRouteId() { return routeId; }
        public void setRouteId(Long routeId) { this.routeId = routeId; }
        public String getDepartureStation() { return departureStation; }
        public void setDepartureStation(String departureStation) { this.departureStation = departureStation; }
        public String getArrivalStation() { return arrivalStation; }
        public void setArrivalStation(String arrivalStation) { this.arrivalStation = arrivalStation; }
        public LocalDate getDepartureDate() { return departureDate; }
        public void setDepartureDate(LocalDate departureDate) { this.departureDate = departureDate; }
        public LocalTime getDepartureTime() { return departureTime; }
        public void setDepartureTime(LocalTime departureTime) { this.departureTime = departureTime; }
        public LocalDate getArrivalDate() { return arrivalDate; }
        public void setArrivalDate(LocalDate arrivalDate) { this.arrivalDate = arrivalDate; }
        public LocalTime getArrivalTime() { return arrivalTime; }
        public void setArrivalTime(LocalTime arrivalTime) { this.arrivalTime = arrivalTime; }
        public double getPrice() { return price; }
        public void setPrice(double price) { this.price = price; }
        public int getTotalSeats() { return totalSeats; }
        public void setTotalSeats(int totalSeats) { this.totalSeats = totalSeats; }
    }

    public static class RecurringTrainRequest {
        @NotBlank(message = "Train number is required")
        private String trainNumber;

        private Long routeId;
        private String departureStation;
        private String arrivalStation;

        @NotNull(message = "Start date is required")
        private LocalDate startDate;

        @NotNull(message = "End date is required")
        private LocalDate endDate;

        @NotNull(message = "Departure time is required")
        private LocalTime departureTime;

        @NotNull(message = "Arrival time is required")
        private LocalTime arrivalTime;

        private int arrivalOffsetDays;

        @Min(value = 0, message = "Price must be non-negative")
        private double price;

        @Min(value = 1, message = "Total seats must be at least 1")
        private int totalSeats;

        public String getTrainNumber() { return trainNumber; }
        public void setTrainNumber(String trainNumber) { this.trainNumber = trainNumber; }
        public Long getRouteId() { return routeId; }
        public void setRouteId(Long routeId) { this.routeId = routeId; }
        public String getDepartureStation() { return departureStation; }
        public void setDepartureStation(String departureStation) { this.departureStation = departureStation; }
        public String getArrivalStation() { return arrivalStation; }
        public void setArrivalStation(String arrivalStation) { this.arrivalStation = arrivalStation; }
        public LocalDate getStartDate() { return startDate; }
        public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
        public LocalDate getEndDate() { return endDate; }
        public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
        public LocalTime getDepartureTime() { return departureTime; }
        public void setDepartureTime(LocalTime departureTime) { this.departureTime = departureTime; }
        public LocalTime getArrivalTime() { return arrivalTime; }
        public void setArrivalTime(LocalTime arrivalTime) { this.arrivalTime = arrivalTime; }
        public int getArrivalOffsetDays() { return arrivalOffsetDays; }
        public void setArrivalOffsetDays(int arrivalOffsetDays) { this.arrivalOffsetDays = arrivalOffsetDays; }
        public double getPrice() { return price; }
        public void setPrice(double price) { this.price = price; }
        public int getTotalSeats() { return totalSeats; }
        public void setTotalSeats(int totalSeats) { this.totalSeats = totalSeats; }
    }

    public static class TrainResponse {
        private Long id;
        private String trainNumber;
        private Long routeId;
        private String departureStation;
        private String arrivalStation;
        private LocalDate departureDate;
        private LocalTime departureTime;
        private LocalDate arrivalDate;
        private LocalTime arrivalTime;
        private double price;
        private int totalSeats;
        private int availableSeats;
        private Long sellerId;
        private String sellerName;

        public TrainResponse(Long id, String trainNumber, Long routeId, String departureStation, String arrivalStation,
                             LocalDate departureDate, LocalTime departureTime, LocalDate arrivalDate, LocalTime arrivalTime,
                             double price, int totalSeats, int availableSeats, Long sellerId, String sellerName) {
            this.id = id;
            this.trainNumber = trainNumber;
            this.routeId = routeId;
            this.departureStation = departureStation;
            this.arrivalStation = arrivalStation;
            this.departureDate = departureDate;
            this.departureTime = departureTime;
            this.arrivalDate = arrivalDate;
            this.arrivalTime = arrivalTime;
            this.price = price;
            this.totalSeats = totalSeats;
            this.availableSeats = availableSeats;
            this.sellerId = sellerId;
            this.sellerName = sellerName;
        }

        public Long getId() { return id; }
        public String getTrainNumber() { return trainNumber; }
        public Long getRouteId() { return routeId; }
        public String getDepartureStation() { return departureStation; }
        public String getArrivalStation() { return arrivalStation; }
        public LocalDate getDepartureDate() { return departureDate; }
        public LocalTime getDepartureTime() { return departureTime; }
        public LocalDate getArrivalDate() { return arrivalDate; }
        public LocalTime getArrivalTime() { return arrivalTime; }
        public double getPrice() { return price; }
        public int getTotalSeats() { return totalSeats; }
        public int getAvailableSeats() { return availableSeats; }
        public Long getSellerId() { return sellerId; }
        public String getSellerName() { return sellerName; }
    }
}
