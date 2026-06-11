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

/**
 * Controller xu ly cac yeu cau lien quan den dat va huy ve tau.
 */
@Controller
public class TicketController {

    private final TicketService ticketService;
    private final UserService userService;

    /**
     * Ham khoi dung TicketController.
     *
     * @param ticketService Dich vu quan ly ve tau
     * @param userService Dich vu quan ly nguoi dung
     */
    @Autowired
    public TicketController(TicketService ticketService, UserService userService) {
        this.ticketService = ticketService;
        this.userService = userService;
    }

    /**
     * Helper method de lay thong tin tai khoan nguoi dung dang dang nhap.
     *
     * @return Doi tuong User hoac null neu chua xac thuc
     */
    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            return null;
        }
        return (User) userService.loadUserByUsername(auth.getName());
    }

    /**
     * Xu ly gui yeu cau dat ve tau.
     *
     * @param trainId ID cua chuyen tau can dat ve
     * @param quantity So luong ve muon dat
     * @param passengerName Ho ten hanh khach di tau
     * @param passengerIdCard So CMND/CCCD cua hanh khach
     * @param redirectAttributes Luu thong bao dat ve thanh cong hoac loi de hien thi sau khi chuyen huong
     * @return Chuyen huong toi trang Dashboard nguoi mua neu thanh cong, nguoc lai quay ve trang chi tiet chuyen tau
     */
    @PostMapping("/tickets/book/{trainId}")
    public String bookTicket(@PathVariable("trainId") Long trainId,
                             @RequestParam("selectedSeats") java.util.List<Integer> selectedSeats,
                             @RequestParam("passengerName") String passengerName,
                             @RequestParam("passengerIdCard") String passengerIdCard,
                             RedirectAttributes redirectAttributes) {
        User currentUser = getAuthenticatedUser();
        if (currentUser == null) return "redirect:/login";

        try {
            java.util.List<Ticket> tickets = ticketService.bookTicketWithSeats(trainId, currentUser, selectedSeats, passengerName, passengerIdCard);
            StringBuilder sb = new StringBuilder("Train ticket(s) booked successfully! ");
            for (Ticket ticket : tickets) {
                sb.append("Carriage: ").append(ticket.getCarriageNumber())
                  .append(", Seat: ").append(ticket.getSeatNumber()).append("; ");
            }
            redirectAttributes.addFlashAttribute("successMessage", sb.toString());
            return "redirect:/dashboard/buyer";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Booking failed: " + e.getMessage());
            return "redirect:/trains/detail/" + trainId;
        }
    }

    /**
     * Xu ly yeu cau huy ve tau da dat.
     * Tra lai so ghe trong va cap nhat trang thai ve thanh CANCELLED.
     *
     * @param ticketId ID cua ve can huy
     * @param redirectAttributes Luu thong bao ket qua thao tac huy ve
     * @return Chuyen huong ve Dashboard tuong ung dua tren vai tro cua nguoi dung thuc hien huy ve
     */
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

        // Dieu huong sau khi huy dua tren vai tro nguoi dung
        if (currentUser.getRole().name().equals("ROLE_ADMIN")) {
            return "redirect:/dashboard/admin";
        }
        return "redirect:/dashboard/buyer";
    }
}
