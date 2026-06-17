package com.example.ticketbooking.controller;

import com.example.ticketbooking.model.Route;
import com.example.ticketbooking.model.Role;
import com.example.ticketbooking.model.User;
import com.example.ticketbooking.service.RouteService;
import com.example.ticketbooking.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * REST Controller cho cac API quan ly Tuyen duong co dinh (Route).
 */
@RestController
@RequestMapping("/api/routes")
public class RouteController {

    private final RouteService routeService;
    private final UserService userService;

    @Autowired
    public RouteController(RouteService routeService, UserService userService) {
        this.routeService = routeService;
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
     * Lay danh sach tuyen duong.
     * Seller lay cac tuyen duong cua minh, Admin lay toan bo tuyen duong.
     */
    @GetMapping
    public ResponseEntity<?> getRoutes() {
        User currentUser = getAuthenticatedUser();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }

        if (currentUser.getRole() == Role.ROLE_ADMIN) {
            return ResponseEntity.ok(routeService.getAllRoutes());
        } else if (currentUser.getRole() == Role.ROLE_SELLER) {
            return ResponseEntity.ok(routeService.getRoutesBySeller(currentUser.getId()));
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only admin and sellers can view routes");
        }
    }

    /**
     * Tao moi mot tuyen duong co dinh (Seller).
     */
    @PostMapping
    public ResponseEntity<?> createRoute(@Valid @RequestBody Route route) {
        User currentUser = getAuthenticatedUser();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }
        if (currentUser.getRole() != Role.ROLE_SELLER && currentUser.getRole() != Role.ROLE_ADMIN) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only sellers can create routes");
        }

        try {
            Route savedRoute = routeService.saveRoute(route, currentUser);
            return ResponseEntity.ok(savedRoute);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to create route: " + e.getMessage());
        }
    }

    /**
     * Xoa mot tuyen duong co dinh.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRoute(@PathVariable("id") Long id) {
        User currentUser = getAuthenticatedUser();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }

        Optional<Route> routeOpt = routeService.getRouteById(id);
        if (routeOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Route route = routeOpt.get();
        // Chi cho phep Admin hoac chinh Seller da tao tuyen duong xoa
        if (currentUser.getRole() != Role.ROLE_ADMIN && !route.getSeller().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not authorized to delete this route");
        }

        try {
            routeService.deleteRoute(id);
            return ResponseEntity.ok().body("{\"message\": \"Route deleted successfully\"}");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to delete route: " + e.getMessage());
        }
    }
}
