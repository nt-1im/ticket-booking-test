package com.example.ticketbooking.controller;

import com.example.ticketbooking.model.Ticket;
import com.example.ticketbooking.model.User;
import com.example.ticketbooking.service.TicketService;
import com.example.ticketbooking.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
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

    @PostMapping("/tickets/book/{trainId}")
    public String bookTicket(@PathVariable("trainId") Long trainId,
                             @RequestParam("quantity") int quantity,
                             @RequestParam("passengerName") String passengerName,
                             @RequestParam("passengerIdCard") String passengerIdCard,
                             RedirectAttributes redirectAttributes) {
        User currentUser = getAuthenticatedUser();
        if (currentUser == null) return "redirect:/login";

        try {
            Ticket ticket = ticketService.bookTicket(trainId, currentUser, quantity, passengerName, passengerIdCard);
            redirectAttributes.addFlashAttribute("successMessage", "Train ticket booked successfully! Carriage: " + ticket.getCarriageNumber() + ", Seat: " + ticket.getSeatNumber());
            return "redirect:/dashboard/buyer";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Booking failed: " + e.getMessage());
            return "redirect:/trains/detail/" + trainId;
        }
    }

    @PostMapping("/tickets/cancel/{ticketId}")
    public String cancelTicket(@PathVariable("ticketId") Long ticketId,
                               RedirectAttributes redirectAttributes) {
        User currentUser = getAuthenticatedUser();
        if (currentUser == null) return "redirect:/login";

        try {
            ticketService.cancelTicket(ticketId, currentUser);
            redirectAttributes.addFlashAttribute("successMessage", "Ticket booking cancelled successfully. Funds refunded.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Cancellation failed: " + e.getMessage());
        }

        // Redirect based on role
        if (currentUser.getRole().name().equals("ROLE_ADMIN")) {
            return "redirect:/dashboard/admin";
        }
        return "redirect:/dashboard/buyer";
    }
}
