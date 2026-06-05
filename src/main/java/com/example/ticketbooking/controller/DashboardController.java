package com.example.ticketbooking.controller;

import com.example.ticketbooking.model.Train;
import com.example.ticketbooking.model.Role;
import com.example.ticketbooking.model.Ticket;
import com.example.ticketbooking.model.User;
import com.example.ticketbooking.service.TrainService;
import com.example.ticketbooking.service.TicketService;
import com.example.ticketbooking.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class DashboardController {

    private final UserService userService;
    private final TrainService trainService;
    private final TicketService ticketService;

    @Autowired
    public DashboardController(UserService userService, TrainService trainService, TicketService ticketService) {
        this.userService = userService;
        this.trainService = trainService;
        this.ticketService = ticketService;
    }

    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            return null;
        }
        return (User) userService.loadUserByUsername(auth.getName());
    }

    @GetMapping("/dashboard")
    public String dashboardRedirect() {
        User currentUser = getAuthenticatedUser();
        if (currentUser == null) {
            return "redirect:/login";
        }
        if (currentUser.getRole() == Role.ROLE_ADMIN) {
            return "redirect:/dashboard/admin";
        } else if (currentUser.getRole() == Role.ROLE_SELLER) {
            return "redirect:/dashboard/seller";
        } else {
            return "redirect:/dashboard/buyer";
        }
    }

    @GetMapping("/dashboard/admin")
    public String adminDashboard(Model model) {
        User currentUser = getAuthenticatedUser();
        if (currentUser == null) return "redirect:/login";

        List<User> users = userService.findAllUsers();
        List<Train> trains = trainService.getAllTrains();
        List<Ticket> tickets = ticketService.getAllTickets();

        // Calculate statistics
        long totalBuyers = users.stream().filter(u -> u.getRole() == Role.ROLE_BUYER).count();
        long totalSellers = users.stream().filter(u -> u.getRole() == Role.ROLE_SELLER).count();
        double totalRevenue = tickets.stream()
                .filter(t -> t.getStatus() == Ticket.Status.BOOKED)
                .mapToDouble(Ticket::getTotalPrice)
                .sum();
        long totalBookings = tickets.stream()
                .filter(t -> t.getStatus() == Ticket.Status.BOOKED)
                .count();

        model.addAttribute("users", users);
        model.addAttribute("trains", trains);
        model.addAttribute("tickets", tickets);
        model.addAttribute("totalBuyers", totalBuyers);
        model.addAttribute("totalSellers", totalSellers);
        model.addAttribute("totalRevenue", totalRevenue);
        model.addAttribute("totalBookings", totalBookings);
        model.addAttribute("currentUser", currentUser);

        return "dashboard/admin";
    }

    @GetMapping("/dashboard/seller")
    public String sellerDashboard(Model model) {
        User currentUser = getAuthenticatedUser();
        if (currentUser == null) return "redirect:/login";

        List<Train> trains = trainService.getTrainsBySeller(currentUser.getId());
        List<Ticket> tickets = ticketService.getTicketsBySeller(currentUser.getId());

        // Calculate statistics
        double totalRevenue = tickets.stream()
                .filter(t -> t.getStatus() == Ticket.Status.BOOKED)
                .mapToDouble(Ticket::getTotalPrice)
                .sum();
        long totalTicketsSold = tickets.stream()
                .filter(t -> t.getStatus() == Ticket.Status.BOOKED)
                .mapToLong(Ticket::getQuantity)
                .sum();

        model.addAttribute("trains", trains);
        model.addAttribute("tickets", tickets);
        model.addAttribute("totalRevenue", totalRevenue);
        model.addAttribute("totalTicketsSold", totalTicketsSold);
        model.addAttribute("currentUser", currentUser);

        return "dashboard/seller";
    }

    @GetMapping("/dashboard/buyer")
    public String buyerDashboard(Model model) {
        User currentUser = getAuthenticatedUser();
        if (currentUser == null) return "redirect:/login";

        List<Ticket> tickets = ticketService.getTicketsByBuyer(currentUser.getId());

        model.addAttribute("tickets", tickets);
        model.addAttribute("currentUser", currentUser);

        return "dashboard/buyer";
    }

    @PostMapping("/dashboard/admin/toggle-user/{id}")
    public String toggleUserStatus(@PathVariable("id") Long id) {
        User currentUser = getAuthenticatedUser();
        if (currentUser == null || currentUser.getRole() != Role.ROLE_ADMIN) {
            return "redirect:/login";
        }
        
        // Prevent disabling yourself
        if (!currentUser.getId().equals(id)) {
            userService.toggleUserStatus(id);
        }
        return "redirect:/dashboard/admin";
    }

    @PostMapping("/dashboard/admin/update-role/{id}")
    public String updateUserRole(@PathVariable("id") Long id, @RequestParam("role") Role role) {
        User currentUser = getAuthenticatedUser();
        if (currentUser == null || currentUser.getRole() != Role.ROLE_ADMIN) {
            return "redirect:/login";
        }

        // Prevent changing yourself
        if (!currentUser.getId().equals(id)) {
            userService.updateUserRole(id, role);
        }
        return "redirect:/dashboard/admin";
    }
}
