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

/**
 * Controller xu ly cac trang Dashboard rieng biet dua tren vai tro cua nguoi dung (Admin, Seller, Buyer).
 * Cung cap cac cong cu thong ke doanh so, quan ly chuyen tau va phan quyen nguoi dung.
 */
@Controller
public class DashboardController {

    private final UserService userService;
    private final TrainService trainService;
    private final TicketService ticketService;

    /**
     * Ham khoi dung DashboardController.
     *
     * @param userService Dich vu quan ly nguoi dung
     * @param trainService Dich vu quan ly chuyen tau
     * @param ticketService Dich vu quan ly ve tau
     */
    @Autowired
    public DashboardController(UserService userService, TrainService trainService, TicketService ticketService) {
        this.userService = userService;
        this.trainService = trainService;
        this.ticketService = ticketService;
    }

    /**
     * Helper method de lay thong tin nguoi dung dang dang nhap tu SecurityContextHolder.
     *
     * @return Doi tuong User dai dien cho nguoi dung da xac thuc, hoac null neu chua dang nhap
     */
    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            return null;
        }
        return (User) userService.loadUserByUsername(auth.getName());
    }

    /**
     * Endpoint trung gian dieu huong nguoi dung da dang nhap toi Dashboard tuong ung voi vai tro cua ho.
     *
     * @return Duong dan chuyen huong redirect dua theo Role (Admin, Seller, Buyer)
     */
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

    /**
     * Hien thi trang Dashboard danh cho Quan tri vien (Admin).
     * Thong ke tong quan so luong khach hang, nguoi ban, tong so luot dat ve va doanh thu toan he thong.
     *
     * @param model Doi tuong Model chua thong tin danh sach tai khoan, chuyen tau, ve tau va cac so lieu thong ke
     * @return Ten template hien thi Dashboard cua Admin ("dashboard/admin")
     */
    @GetMapping("/dashboard/admin")
    public String adminDashboard(Model model) {
        User currentUser = getAuthenticatedUser();
        if (currentUser == null)
            return "redirect:/login";

        List<User> users = userService.findAllUsers();
        List<Train> trains = trainService.getAllTrains();
        List<Ticket> tickets = ticketService.getAllTickets();

        // Tinh toan so lieu thong ke
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

    /**
     * Hien thi trang Dashboard danh cho Nguoi ban (Seller).
     * Thong ke danh sach chuyen tau thuoc so huu, ve da ban, tong doanh thu va tong so ve da ban ra.
     *
     * @param model Doi tuong Model chua danh sach chuyen tau cua seller, ve lien quan va thong so doanh thu
     * @return Ten template hien thi Dashboard cua Seller ("dashboard/seller")
     */
    @GetMapping("/dashboard/seller")
    public String sellerDashboard(Model model) {
        User currentUser = getAuthenticatedUser();
        if (currentUser == null)
            return "redirect:/login";

        List<Train> trains = trainService.getTrainsBySeller(currentUser.getId());
        List<Ticket> tickets = ticketService.getTicketsBySeller(currentUser.getId());

        // Tinh toan doanh thu va luong ve da ban
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

    /**
     * Hien thi trang Dashboard danh cho Nguoi mua (Buyer/Khach hang).
     * Hien thi lich su dat ve ca nhan cung thong tin chi tiet tung ve.
     *
     * @param model Doi tuong Model chua danh sach ve cua khach hang hien tai
     * @return Ten template hien thi Dashboard cua Buyer ("dashboard/buyer")
     */
    @GetMapping("/dashboard/buyer")
    public String buyerDashboard(Model model) {
        User currentUser = getAuthenticatedUser();
        if (currentUser == null)
            return "redirect:/login";

        List<Ticket> tickets = ticketService.getTicketsByBuyer(currentUser.getId());

        model.addAttribute("tickets", tickets);
        model.addAttribute("currentUser", currentUser);

        return "dashboard/buyer";
    }

    /**
     * Kich hoat hoac vo hieu hoa mot tai khoan nguoi dung trong he thong (Chi danh cho Admin).
     * Ngan chan viec Admin tu khoa tai khoan cua chinh minh.
     *
     * @param id ID cua tai khoan can thay doi trang thai hoat dong
     * @return Duong dan chuyen huong ve Dashboard Admin
     */
    @PostMapping("/dashboard/admin/toggle-user/{id}")
    public String toggleUserStatus(@PathVariable("id") Long id) {
        User currentUser = getAuthenticatedUser();
        if (currentUser == null || currentUser.getRole() != Role.ROLE_ADMIN) {
            return "redirect:/login";
        }

        // Khong cho phep tu vo hieu hoa tai khoan cua ban than
        if (!currentUser.getId().equals(id)) {
            userService.toggleUserStatus(id);
        }
        return "redirect:/dashboard/admin";
    }

    /**
     * Thay doi vai tro (Role) cua nguoi dung (Chi danh cho Admin).
     * Ngan chan viec Admin tu thay doi quyen han cua chinh minh.
     *
     * @param id ID cua tai khoan can doi quyen
     * @param role Vai tro moi can cap nhat (ROLE_ADMIN, ROLE_SELLER, ROLE_BUYER)
     * @return Duong dan chuyen huong ve Dashboard Admin
     */
    @PostMapping("/dashboard/admin/update-role/{id}")
    public String updateUserRole(@PathVariable("id") Long id, @RequestParam("role") Role role) {
        User currentUser = getAuthenticatedUser();
        if (currentUser == null || currentUser.getRole() != Role.ROLE_ADMIN) {
            return "redirect:/login";
        }

        // Khong cho phep tu doi quyen cua ban than
        if (!currentUser.getId().equals(id)) {
            userService.updateUserRole(id, role);
        }
        return "redirect:/dashboard/admin";
    }
}
